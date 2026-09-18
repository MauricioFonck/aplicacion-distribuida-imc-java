# Actividad Corte 2 — Porciones de comida para una reunión (UDP + arquitectura hexagonal)

Aplicación distribuida cliente–servidor en Java que estima la cantidad de porciones de comida
necesarias para una reunión. Es el mismo ejercicio del primer corte, pero ahora la comunicación
se hace con el protocolo **UDP/IP** y el código se organiza con **arquitectura hexagonal**
(puertos y adaptadores).

> **Rama:** `actividad-corte-2` · **Enunciado:** Ejercicio 35 · **Base:** rama
> `parcial-corte-1` de este mismo repositorio.

## Origen del proyecto

Esta actividad parte de la entrega del primer corte, que está en la rama
[`parcial-corte-1`](../../tree/parcial-corte-1). De ella se conservaron la regla de negocio
(total, clasificación de la reunión y reserva del 10 %), los límites de validación, el estilo de
las ventanas Swing y la forma de compilar y probar con scripts. Lo que cambió es el transporte
(de TCP a UDP) y la organización interna del código (de tres paquetes a capas hexagonales).

La estructura de capas, puertos y adaptadores sigue los repositorios de ejemplo entregados en
clase para el cálculo del IMC:

- Servidor: <https://github.com/arrietajohn/imc-udp-server-java-hexagonal>
- Cliente: <https://github.com/arrietajohn/imc-udp-client-java-hexagonal>

Igual que en esos ejemplos, el servidor y el cliente son **dos proyectos independientes**
(carpetas `servidor/` y `cliente/`): no comparten ninguna clase y solo se entienden a través del
protocolo de texto que viaja en los datagramas.

Las ramas `main` y `parcial-corte-1` permanecen intactas y esta rama no se mezcla hacia ellas.

## Enunciado

> Desarrolle una aplicación que permita estimar la cantidad de porciones de comida necesarias
> para una reunión. El cliente solicitará la cantidad de personas que asistirán y la cantidad
> de porciones previstas por persona. El servidor calculará el total de porciones necesarias y
> devolverá el resultado.

## Requisitos

Se necesita un JDK 17 o una versión posterior, porque el código usa `record` y expresiones
`switch`. El proyecto utiliza solamente clases de la biblioteca estándar de Java. Cada proyecto
incluye un `pom.xml` para abrirlo como proyecto Maven en NetBeans o IntelliJ IDEA.

## Organización del proyecto

| Carpeta o archivo | Función |
|---|---|
| `servidor/` | Proyecto del servidor UDP. |
| `cliente/` | Proyecto del cliente UDP. |
| `scripts/compile.sh` | Compila los dos proyectos en la carpeta `out`. |
| `scripts/test.sh` | Compila y ejecuta todas las pruebas. |
| `scripts/run-server.sh` | Inicia el servidor. |
| `scripts/run-client.sh` | Inicia el cliente. |
| `docs/informe-academico.md` | Informe de la actividad: UDP, arquitectura hexagonal y conclusiones. |
| `docs/resultado-pruebas.md` | Resultados de las pruebas ejecutadas y comprobación manual. |
| `docs/arquitectura.puml` | Diagrama de la arquitectura hexagonal del servidor y del cliente. |

Dentro de cada proyecto el paquete raíz es `com.mauricio.porciones` y las capas son las mismas:

| Paquete | Capa | Contenido |
|---|---|---|
| `dominio` | Núcleo | Objetos de valor, modelos, enumeraciones, excepciones y **puertos de salida**. No conoce Swing ni sockets. |
| `aplicacion` | Núcleo | Casos de uso (servicios), **puertos de entrada**, DTO y mappers. |
| `adaptadores` | Infraestructura | Implementaciones de los puertos de salida: el canal UDP y las notificaciones. |
| `entrypoint` | Infraestructura | Lo que dispara los casos de uso: la ventana Swing y, en el servidor, el receptor de datagramas. |
| `com.mauricio.Main` | Arranque | *Composition root*: crea las clases concretas y las conecta entre sí. |

### Servidor

```
servidor/src/main/java/com/mauricio/
├── Main.java
└── porciones/
    ├── dominio/
    │   ├── vo/            Asistentes, PorcionesPorPersona, Destinatario, PuertoRed
    │   ├── modelos/       Estimacion, Resultado, RespuestaCliente, EventoServidor
    │   ├── enums/         ClasificacionReunion, TipoRespuesta, EstadoServidor
    │   ├── excepciones/   DominioException y sus especializaciones
    │   └── puertos/salida/  PuertoSalidaRed, PuertoNotificacionEvento, ControladorServidorRedPort
    ├── aplicacion/
    │   ├── puertos/entrada/ ProcesarPeticionUdpInputPort, GestionarServidorInputPort, CalcularPorcionesInputPort
    │   ├── servicios/     ProcesarPeticionUdpService, GestionarServidorService, CalcularPorcionesService
    │   ├── dto/  mapper/  excepciones/
    ├── adaptadores/red/   CanalUdp, AdaptadorSalidaUdp, AdaptadorControlServidorRed,
    │                      AdaptadorNotificacionEvento, UdpNetworkMapper, UdpResponse, RedUtil
    └── entrypoint/
        ├── udp/           ReceptorPeticionesUdp
        └── gui/           ServidorFrame
```

### Cliente

```
cliente/src/main/java/com/mauricio/
├── Main.java
└── porciones/
    ├── dominio/
    │   ├── vo/            Asistentes, PorcionesPorPersona, DestinoServidor
    │   ├── modelos/       DatosReunion, ResultadoPorciones, EventoCliente
    │   ├── enums/         EstadoConexion
    │   ├── excepciones/
    │   └── puertos/salida/  ClienteUdpPort, PuertoNotificacionCliente
    ├── aplicacion/
    │   ├── puertos/entrada/ GestionarConexionInputPort, CalcularPorcionesInputPort
    │   ├── servicios/     ClientePorcionesService
    │   ├── dto/  mapper/  excepciones/
    ├── adaptadores/
    │   ├── red/           CanalUdp, AdaptadorClienteUdp, ProtocoloUdpMapper
    │   └── notificacion/  AdaptadorNotificacionCliente, ObservadorCliente
    └── entrypoint/gui/    ClienteFrame
```

## Compilar y probar

Desde una terminal situada en la carpeta principal del repositorio:

```bash
./scripts/test.sh
```

El script compila los dos proyectos con `javac --release 17` y ejecuta sus pruebas: dominio,
casos de uso, adaptadores e integración con sockets UDP reales en la máquina local.

## Ejecutar la aplicación

Primero se inicia el servidor:

```bash
./scripts/run-server.sh
```

En la ventana del servidor se deja el puerto `9008` y se pulsa **INICIAR**. En otra terminal se
inicia el cliente:

```bash
./scripts/run-client.sh
```

En la ventana del cliente se escribe `localhost` como dirección y `9008` como puerto, y se pulsa
**CONECTAR**. En la pestaña **CALCULAR PORCIONES** se introducen la cantidad de personas y las
porciones por persona.

Por ejemplo, con `40` personas y `3` porciones por persona, el total es `120` porciones y la
sugerencia con el margen de reserva es `132`. Se pueden abrir varios clientes a la vez: el
servidor los atiende a todos por el mismo socket.

Para trabajar con dos equipos de la misma red, el servidor se ejecuta en uno de ellos y el
cliente utiliza la dirección IP que muestra la ventana del servidor. En ese caso se debe permitir
el puerto **UDP** elegido en el firewall.

## Regla de cálculo

```
total de porciones = cantidad de personas × porciones por persona
```

El servidor admite únicamente enteros mayores que cero, con un máximo de `100.000` personas y
`100` porciones por persona. El total se calcula en `long`, de modo que el caso máximo
(`10.000.000` de porciones) no produce desbordamiento.

Además del total, el servidor clasifica la reunión según la cantidad de asistentes (pequeña,
mediana, grande o evento masivo) y sugiere preparar un 10 % adicional como margen de reserva,
redondeado hacia arriba con aritmética entera.

La regla vive en el dominio del servidor (`Estimacion`, `Asistentes`, `PorcionesPorPersona`) y no
depende de la red: se prueba sin abrir ningún socket.

## Protocolo de comunicación

Cada mensaje es un texto en UTF-8 que viaja dentro de **un datagrama UDP**. Los campos se separan
con punto y coma.

| Petición del cliente | Respuesta del servidor |
|---|---|
| `CONECTAR` | `CONECTADO_OK;mensaje` |
| `CALCULAR;personas;porcionesPorPersona` | `OK_CALCULO;total;totalConReserva;clasificación;mensaje` |
| `DESCONECTAR` | `DESCONECTADO_OK;mensaje` |
| Cualquier petición inválida | `ERROR;motivo` |

Ejemplo:

```
cliente  → CALCULAR;40;3
servidor → OK_CALCULO;120;132;Reunión mediana;Reunión mediana: 120 porciones para 40 personas, ...
```

### Diferencias con la versión TCP del primer corte

| Aspecto | `parcial-corte-1` (TCP) | `actividad-corte-2` (UDP) |
|---|---|---|
| Clases de Java | `ServerSocket` y `Socket` | `DatagramSocket` y `DatagramPacket` |
| Conexión | Real: `accept()` crea un socket por cliente. | No existe. Un único socket recibe los datagramas de todos los clientes. |
| «Conectar» | Establece la conexión TCP. | Es un saludo lógico: se envía `CONECTAR` y se espera `CONECTADO_OK` para saber que el servidor está vivo. |
| Destino de la respuesta | El mismo flujo de la conexión. | La IP y el puerto se leen de cada datagrama recibido (`Destinatario`). |
| Hilos del servidor | Un hilo por cliente conectado. | Un solo hilo receptor. |
| Formato | Binario con `DataInputStream`/`DataOutputStream`. | Texto delimitado dentro del datagrama. |
| Entrega | Garantizada y ordenada por TCP. | No garantizada: el cliente usa un tiempo de espera de 3 segundos (`setSoTimeout`) y avisa si no hay respuesta. |

## Validaciones

El cliente valida el host, el puerto, la cantidad de personas y las porciones por persona con sus
objetos de valor antes de enviar nada, y controla el caso en que el usuario intenta calcular sin
estar conectado. El servidor vuelve a validar todos los datos recibidos con sus propios objetos de
valor, porque no debe confiar en la validación hecha en el cliente, y responde `ERROR;motivo` sin
dejar de atender a los demás.

## Abrir el proyecto en un IDE

En NetBeans o IntelliJ IDEA se abren por separado las carpetas `servidor` y `cliente`; cada una
tiene su `pom.xml`. En ambos proyectos la clase principal es `com.mauricio.Main`.

## Uso académico

El proyecto fue elaborado como entrega de la actividad del segundo corte, para practicar la
programación con sockets UDP, la arquitectura hexagonal, las interfaces Swing, el manejo de hilos
y la validación de datos en una aplicación distribuida.
