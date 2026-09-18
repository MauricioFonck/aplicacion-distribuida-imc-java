# Porciones de comida para una reunión con UDP y arquitectura hexagonal

**Actividad Corte 2 · Ejercicio 35**

## Resumen

Se desarrolló una aplicación distribuida en Java que estima las porciones de comida necesarias
para una reunión. El cliente envía la cantidad de personas y las porciones previstas por persona;
el servidor calcula el total y lo devuelve. Es el mismo ejercicio entregado en el primer corte,
pero con dos cambios de fondo: la comunicación pasa de TCP a **UDP** y el código se reorganiza con
**arquitectura hexagonal**, siguiendo los repositorios de ejemplo del IMC vistos en clase.

## Enunciado

> Desarrolle una aplicación que permita estimar la cantidad de porciones de comida necesarias
> para una reunión. El cliente solicitará la cantidad de personas que asistirán y la cantidad
> de porciones previstas por persona. El servidor calculará el total de porciones necesarias y
> devolverá el resultado.

## Objetivo

Aplicar el protocolo UDP/IP y la arquitectura hexagonal a un problema ya conocido, de forma que
se note qué parte del programa cambia al cambiar el transporte y qué parte no debería cambiar.

## Punto de partida

La entrega del primer corte (rama `parcial-corte-1`) tenía tres paquetes: `core` con el cálculo y
el protocolo, `server` y `client`. Funcionaba, pero la ventana del servidor creaba directamente el
`PorcionesServer`, y este mezclaba sockets, hilos y llamadas al cálculo. Cambiar TCP por UDP en
ese diseño obligaba a tocar casi todas las clases.

De esa entrega se conservaron la regla de negocio, los límites, los mensajes, el diseño de las
ventanas y los scripts. La organización en capas se tomó de los ejemplos del profesor
(`imc-udp-server-java-hexagonal` e `imc-udp-client-java-hexagonal`), adaptando cada pieza del IMC
al dominio de las porciones.

## Herramientas utilizadas

| Herramienta | Uso |
|---|---|
| Java 17 o superior | `record` para objetos de valor y DTO, expresiones `switch`. |
| `java.net.DatagramSocket` y `DatagramPacket` | Comunicación UDP. |
| Swing | Ventanas del servidor y del cliente. |
| `CompletableFuture` y `ExecutorService` | Operaciones de red del cliente fuera del hilo de Swing. |
| Scripts Bash y `javac` | Compilación y pruebas sin dependencias externas. |

## UDP frente a TCP

UDP es un protocolo **sin conexión**. No hay `accept()`, no hay un socket por cliente y no hay un
flujo de bytes: cada `send` entrega un datagrama independiente, con su dirección de destino, y
cada `receive` devuelve un datagrama completo junto con la dirección de quien lo envió. A cambio
de esa sencillez, UDP no garantiza que el datagrama llegue, ni que llegue una sola vez, ni el
orden.

Esto se refleja en el código así:

- **Servidor.** `CanalUdp` abre un único `DatagramSocket(puerto)`. `ReceptorPeticionesUdp` corre
  un solo hilo que hace `receive` en bucle. De cada `DatagramPacket` saca el texto, la IP
  (`getAddress()`) y el puerto (`getPort()`) del cliente; con esos dos datos se arma el objeto de
  valor `Destinatario`, que acompaña a la respuesta hasta que `CanalUdp.enviar` construye el
  datagrama de vuelta. En TCP ese dato no hacía falta porque la respuesta viajaba por la misma
  conexión.
- **Cliente.** `CanalUdp` abre un `DatagramSocket()` sin puerto, para que el sistema operativo
  asigne uno efímero. El método `intercambiar` envía la petición y espera la respuesta. Como la
  respuesta puede no llegar nunca, el socket tiene `setSoTimeout(3000)`: pasados tres segundos se
  lanza `SocketTimeoutException` y el usuario ve un mensaje en lugar de una ventana congelada.
- **«Conexión».** En UDP no existe, pero al usuario le sirve saber si el servidor está disponible
  antes de calcular. Por eso el protocolo tiene un saludo: el cliente envía `CONECTAR` y, si recibe
  `CONECTADO_OK`, guarda la dirección del servidor para las peticiones siguientes. El servidor no
  guarda ninguna sesión: cada datagrama se atiende por sí solo.
- **Detener el servidor.** `receive` es bloqueante. Para detener el hilo se cierra el socket, lo
  que hace que `receive` lance `SocketException`; el receptor revisa la bandera `escuchando` y
  termina sin reportar un error.

El tamaño del búfer es de 2048 bytes, suficiente para cualquier mensaje del protocolo, de modo que
cada petición y cada respuesta caben en un solo datagrama.

## Arquitectura hexagonal

La idea es que el núcleo de la aplicación (dominio y casos de uso) no dependa de la tecnología.
El núcleo declara **puertos**, que son interfaces, y la infraestructura aporta **adaptadores** que
los implementan o los invocan.

| Concepto | En el servidor | En el cliente |
|---|---|---|
| Dominio | `Estimacion`, `Resultado`, `Asistentes`, `PorcionesPorPersona`, `ClasificacionReunion` | `DatosReunion`, `ResultadoPorciones`, `Asistentes`, `PorcionesPorPersona`, `DestinoServidor` |
| Puertos de entrada (lo que el núcleo ofrece) | `ProcesarPeticionUdpInputPort`, `GestionarServidorInputPort`, `CalcularPorcionesInputPort` | `GestionarConexionInputPort`, `CalcularPorcionesInputPort` |
| Casos de uso | `ProcesarPeticionUdpService`, `GestionarServidorService`, `CalcularPorcionesService` | `ClientePorcionesService` |
| Puertos de salida (lo que el núcleo necesita) | `PuertoSalidaRed`, `ControladorServidorRedPort`, `PuertoNotificacionEvento` | `ClienteUdpPort`, `PuertoNotificacionCliente` |
| Adaptadores de entrada (*entrypoints*) | `ReceptorPeticionesUdp`, `ServidorFrame` | `ClienteFrame` |
| Adaptadores de salida | `AdaptadorSalidaUdp`, `AdaptadorControlServidorRed`, `AdaptadorNotificacionEvento` | `AdaptadorClienteUdp`, `AdaptadorNotificacionCliente` |
| *Composition root* | `com.mauricio.Main` | `com.mauricio.Main` |

### Recorrido de una petición en el servidor

1. `ReceptorPeticionesUdp` recibe el datagrama `CALCULAR;40;3` y crea un
   `ProcesarPeticionUdpCommand` con la IP, el puerto y el texto.
2. Llama a `ProcesarPeticionUdpInputPort.procesar(...)`. El receptor solo conoce la interfaz.
3. `ProcesarPeticionUdpService` interpreta el comando, convierte los números con `EstimacionMapper`
   en `Asistentes` y `PorcionesPorPersona` (que validan los límites al construirse) y llama a
   `Estimacion.calcular()`.
4. Con el `Resultado` arma una `RespuestaCliente` y la entrega a `PuertoSalidaRed`. El servicio no
   sabe que detrás hay UDP.
5. `AdaptadorSalidaUdp` usa `UdpNetworkMapper` para producir el texto
   `OK_CALCULO;120;132;Reunión mediana;...` y `CanalUdp` lo envía en un datagrama al `Destinatario`.
6. El servicio también publica un `EventoServidor` por `PuertoNotificacionEvento`; la ventana lo
   recibe como `ObservadorServidor` y lo escribe en el log.

### Recorrido en el cliente

1. `ClienteFrame` crea un `CalcularPorcionesCommand` y llama a `CalcularPorcionesInputPort`.
2. `ClientePorcionesService` lo convierte a `DatosReunion` (la validación ocurre aquí, antes de
   usar la red) y ejecuta la petición en el hilo `cliente-udp` mediante `CompletableFuture`.
3. `ClienteUdpPort.solicitarCalculo` es implementado por `AdaptadorClienteUdp`, que serializa con
   `ProtocoloUdpMapper`, intercambia los datagramas con `CanalUdp` y devuelve un
   `ResultadoPorciones`.
4. El servicio notifica el resultado o el error por `PuertoNotificacionCliente`, y la ventana lo
   muestra como `ObservadorCliente`.

### Qué se gana

- La regla de negocio se prueba sin red y sin ventanas.
- Los casos de uso se prueban con dobles de los puertos (listas en memoria en lugar de sockets).
- Si se quisiera volver a TCP, bastaría con escribir otros adaptadores y cambiar el `Main`; el
  dominio y los servicios no se tocarían.
- Las ventanas no contienen lógica: solo traducen clics a comandos y notificaciones a etiquetas.

## Regla de negocio

```
total = personas × porciones por persona
total con reserva = total + techo(total × 10 / 100)
```

Los valores deben ser enteros mayores que cero, con un máximo de 100.000 personas y 100 porciones
por persona. El producto se hace en `long`. La reserva se calcula con aritmética entera,
`(total × 10 + 99) / 100`, porque con coma flotante `200 × 1.1` da `220.00000000000003` y el
redondeo hacia arriba produciría 221 en lugar de 220.

La reunión se clasifica por la cantidad de asistentes: hasta 10, pequeña; hasta 50, mediana; hasta
200, grande; más de 200, evento masivo.

## Protocolo de comunicación

| Petición | Respuesta |
|---|---|
| `CONECTAR` | `CONECTADO_OK;mensaje` |
| `CALCULAR;personas;porcionesPorPersona` | `OK_CALCULO;total;totalConReserva;clasificación;mensaje` |
| `DESCONECTAR` | `DESCONECTADO_OK;mensaje` |
| Petición inválida | `ERROR;motivo` |

Los totales viajan como números sin separadores de miles para que el cliente los pueda interpretar;
el formato con puntos se aplica solo al mostrarlos. El cliente divide la respuesta en un máximo de
cinco campos, de manera que el mensaje final se conserva completo.

## Concurrencia

El servidor usa un solo hilo receptor, marcado como *daemon*. Como cada petición es un cálculo
corto y no hay conexiones que mantener, no hace falta un hilo por cliente como en TCP. Los métodos
de `CanalUdp` que cambian el estado del socket están sincronizados, porque la ventana (hilo de
Swing) abre y cierra el canal mientras el receptor lo usa.

El cliente ejecuta la red en un `ExecutorService` de un solo hilo, así las peticiones salen en
orden y la ventana nunca se bloquea. Las notificaciones llegan desde ese hilo y la ventana las pasa
al hilo de Swing con `SwingUtilities.invokeLater`.

## Validación de datos

| Dónde | Qué se valida |
|---|---|
| Ventana del cliente | Que los campos no estén vacíos y sean enteros. |
| Dominio del cliente | Límites de personas y porciones, host y puerto (1024–65535). |
| Servicio del servidor | Comando conocido, cantidad de campos y formato numérico. |
| Dominio del servidor | Los mismos límites, de nuevo: el servidor no confía en el cliente. |
| Adaptador del cliente | Que la respuesta respete el protocolo y que la reserva no sea menor que el total. |

## Verificación

Las pruebas son clases con `main`, sin dependencias externas, y se ejecutan con
`./scripts/test.sh`. Cubren el dominio, los casos de uso con puertos falsos, los mappers del
protocolo y la integración con sockets UDP reales en la máquina local, incluidos varios clientes
simultáneos, el puerto ocupado, el servidor que no responde (tiempo de espera) y el host
inexistente. El detalle está en [`resultado-pruebas.md`](resultado-pruebas.md).

## Conclusiones

- Pasar de TCP a UDP simplificó el servidor (un socket y un hilo), pero trasladó al protocolo y al
  cliente responsabilidades que antes asumía TCP: saber a quién responder, saber si el servidor
  está vivo y decidir qué hacer cuando no llega respuesta.
- La arquitectura hexagonal exige más clases e interfaces que la versión del primer corte, pero
  cada una tiene una sola responsabilidad y las dependencias apuntan siempre hacia el núcleo.
- La regla de negocio del primer corte se trasladó sin cambios de comportamiento: los mismos casos
  de prueba (total, reserva exacta, límites, singular y plural) siguen pasando.
- Separar el servidor y el cliente en dos proyectos deja claro que lo único que comparten es el
  protocolo de texto.

## Referencias

- Oracle. *Java Platform SE — `java.net.DatagramSocket` y `java.net.DatagramPacket`*.
- Oracle. *The Java Tutorials — All About Datagrams*.
- Cockburn, A. *Hexagonal Architecture (Ports and Adapters)*.
- Arrieta, J. Repositorios de ejemplo `imc-udp-server-java-hexagonal` e
  `imc-udp-client-java-hexagonal`.
