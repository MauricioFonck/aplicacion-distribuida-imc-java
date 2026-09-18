# Resultado de pruebas

## Entorno de trabajo

| Elemento | Valor |
|---|---|
| Sistema operativo | Windows 11 (64 bits). |
| JDK | Java 26.0.2.1 (`javac 26.0.2.1`). |
| Nivel de compilación | `javac --release 17`. Con `-Xlint:all` la compilación no emite advertencias. |
| Dependencias externas | Ninguna. Las pruebas son clases con método `main`. |
| Puertos | `9008` en ejecución normal; `19008` (servidor) y `19018`–`19019` (cliente) en las pruebas de integración. |
| Comando | `./scripts/test.sh` |

## Resultado general

El comando `./scripts/test.sh` terminó correctamente:

```
Compilación completada en /c/Users/andre/Music/aplicacion-distribuida-imc-java/out
== Servidor ==
Todas las pruebas de DominioTest pasaron.
Todas las pruebas de AplicacionTest pasaron.
Todas las pruebas de AdaptadoresTest pasaron.
Todas las pruebas de RedUdpIntegracionTest pasaron.
== Cliente ==
Todas las pruebas de DominioYProtocoloTest pasaron.
Todas las pruebas de ClienteServiceTest pasaron.
Todas las pruebas de RedIntegracionTest pasaron.
Todas las pruebas pasaron.
```

## Servidor

### Dominio (`DominioTest`)

| Prueba | Entrada | Resultado esperado | Resultado |
|---|---|---|---|
| Multiplicación básica | 25 personas, 3 porciones | Total `75`, reserva `83`, «Reunión mediana», mensaje sin `;`. | Correcto. |
| Una sola persona | 1 persona, 1 porción | Total `1`, mensaje en singular. | Correcto. |
| Valores no positivos | 0 personas; −2 porciones | `AsistentesIncorrectosException`; `PorcionesIncorrectasException`. | Correcto. |
| Límites | 100.001 personas; 101 porciones | Se rechazan. | Correcto. |
| Caso máximo sin desbordamiento | 100.000 personas, 100 porciones | Total `10.000.000`, reserva `11.000.000`, «Evento masivo». | Correcto. |
| Margen de reserva | 75, 10, 120 y 0 porciones | `83`, `11`, `132` y `0`. | Correcto. |
| Reserva exacta sin redondeo de más | 200 y 1.000 porciones | `220` y `1.100`. | Correcto. |
| Clasificación | 10, 11, 200 y 201 personas | Pequeña, mediana, grande y evento masivo. | Correcto. |
| Estimación incompleta | Personas o porciones nulas | `EstimacionInvalidaException`. | Correcto. |
| Invariantes del resultado | Total 0, reserva menor que el total, sin clasificación, sin mensaje | `ResultadoInvalidoException`. | Correcto. |
| Destinatario y puerto | IP vacía, puerto 0, puertos 1023 y 65536 | Se rechazan; el endpoint queda sin espacios. | Correcto. |
| Respuestas al cliente | Cálculo sin resultado, respuesta sin destinatario | `RespuestaInvalidaException`. | Correcto. |

### Casos de uso (`AplicacionTest`)

Los puertos de salida se reemplazan por listas en memoria; ninguna de estas pruebas abre un socket.

| Prueba | Petición | Resultado esperado | Resultado |
|---|---|---|---|
| Caso de uso de cálculo | 40 y 3 | DTO con `120`, `132` y «Reunión mediana». | Correcto. |
| Comandos nulos | `null` en los mappers | `ComandoInvalidoException`. | Correcto. |
| Saludo | `CONECTAR` | Respuesta `CONECTADO` dirigida a `127.0.0.1:5000` y un evento. | Correcto. |
| Despedida | `DESCONECTAR` | Respuesta `DESCONECTADO`. | Correcto. |
| Cálculo | `CALCULAR;40;3` | `OK_CALCULO` con `120` y `132`; el evento describe lo recibido y lo enviado. | Correcto. |
| Tolerancia de formato | `  calcular; 10 ; 2  ` | Total `20`. | Correcto. |
| Mensaje vacío | Solo espacios | `ERROR` «Mensaje vacío». | Correcto. |
| Comando desconocido | `SUMAR;1;2` | `ERROR` «Comando no reconocido». | Correcto. |
| Formato incorrecto | `CALCULAR;40` y `CALCULAR;40;3;9` | `ERROR` «Formato inválido». | Correcto. |
| Valores no numéricos | `CALCULAR;cuarenta;3` y `CALCULAR;40;2.5` | `ERROR` «números enteros». | Correcto. |
| Valores fuera del dominio | `CALCULAR;0;3` y `CALCULAR;10;101` | `ERROR` con el motivo del dominio. | Correcto. |
| Ciclo de vida | Iniciar en `9008` y detener | Estados `EN_LINEA` y `DETENIDO`, dos eventos. | Correcto. |
| Puerto inválido | Iniciar en `80` | `PuertoIncorrectoException` sin tocar la red. | Correcto. |

### Adaptadores (`AdaptadoresTest`)

| Prueba | Resultado esperado | Resultado |
|---|---|---|
| Serialización del cálculo | `OK_CALCULO;120;132;Reunión mediana;...` con cinco campos, en UTF-8 y con la IP y el puerto de destino. | Correcto. |
| Respuestas de sesión | `CONECTADO_OK;...` y `DESCONECTADO_OK;...`. | Correcto. |
| Errores | `ERROR;motivo`; sin mensaje, `ERROR;Error desconocido.`. | Correcto. |
| Respuesta nula | `RespuestaInvalidaException`. | Correcto. |
| Notificaciones | Un observador registrado dos veces recibe cada aviso una sola vez; al removerlo deja de recibir. | Correcto. |
| IP local | Siempre se resuelve alguna dirección. | Correcto. |

### Integración UDP (`RedUdpIntegracionTest`)

Se arma el servidor completo sin ventana, se inicia en el puerto `19008` y se le envían datagramas
desde un `DatagramSocket` de prueba.

| Prueba | Resultado esperado | Resultado |
|---|---|---|
| Saludo y despedida | `CONECTADO_OK;` y `DESCONECTADO_OK;`. | Correcto. |
| Cálculo | `CALCULAR;40;3` → `OK_CALCULO;120;132;Reunión mediana;...`. | Correcto. |
| Caso máximo | `CALCULAR;100000;100` → `OK_CALCULO;10000000;11000000;...`. | Correcto. |
| Peticiones inválidas | `CALCULAR;0;3`, `CALCULAR;abc;3` y `HOLA` → `ERROR;`. | Correcto. |
| Continuidad | Después de un error, `CALCULAR;25;3` → `OK_CALCULO;75;83;`. | Correcto. |
| Varios clientes | Dos sockets envían a la vez y cada uno recibe su propio resultado. | Correcto. |
| Parada | El servidor y el receptor quedan detenidos. | Correcto. |
| Puerto ocupado | Iniciar en un puerto en uso lanza `ServidorRedException`. | Correcto. |

## Cliente

### Dominio y protocolo (`DominioYProtocoloTest`)

| Prueba | Resultado esperado | Resultado |
|---|---|---|
| Datos de la reunión | Se rechazan 0 y 100.001 personas, 0 y 101 porciones, y los datos nulos. | Correcto. |
| Destino | Se rechazan el host vacío y los puertos 80 y 70000; el endpoint queda sin espacios. | Correcto. |
| Mapper de comandos | Convierte a `DestinoServidor` y `DatosReunion` y propaga la validación. | Correcto. |
| Serialización | 100.000 personas y 100 porciones → `CALCULAR;100000;100`. | Correcto. |
| Saludo | Acepta `CONECTADO_OK;...`; rechaza `ERROR;...` y `null`. | Correcto. |
| Interpretación del cálculo | Total `120`, reserva `132`, clasificación y mensaje. | Correcto. |
| Mensaje con `;` | El mensaje final no se parte. | Correcto. |
| Error del servidor | `ERROR;motivo` se convierte en excepción y conserva el motivo. | Correcto. |
| Respuestas mal formadas | Vacía, con campos de menos, de otro tipo, total no numérico, reserva menor que el total. | Correcto. |

### Casos de uso (`ClienteServiceTest`)

| Prueba | Resultado esperado | Resultado |
|---|---|---|
| Conexión | Notifica `CONECTANDO`, `CONECTADO` y el evento. | Correcto. |
| Fallo de conexión | Vuelve a `DESCONECTADO`, notifica el error y el futuro termina con `ClienteRedException`. | Correcto. |
| Destino inválido | Se rechaza antes de notificar o usar la red. | Correcto. |
| Cálculo | El puerto de red recibe objetos del dominio y se notifica el resultado. | Correcto. |
| Fallo del cálculo | Se notifica el error. | Correcto. |
| Datos inválidos | No llegan al puerto de red. | Correcto. |
| Desconexión | Notifica `DESCONECTADO` y el evento. | Correcto. |

### Integración UDP (`RedIntegracionTest`)

El adaptador real del cliente se prueba contra un servidor UDP mínimo en el puerto `19018`.

| Prueba | Resultado esperado | Resultado |
|---|---|---|
| Sesión completa | El servidor recibe `CONECTAR`, `CALCULAR;40;3`, `CALCULAR;25;3` y `DESCONECTAR`, en ese orden; los totales son `120` y `75`. | Correcto. |
| Error del servidor | Se convierte en `ClienteRedException` con el motivo y la sesión sigue abierta. | Correcto. |
| Calcular sin conectarse | `ClienteRedException`. | Correcto. |
| Nadie responde | Con 300 ms de espera, la conexión a un puerto sin servidor falla y el cliente queda desconectado. | Correcto. |
| Host inexistente | `ClienteRedException`. | Correcto. |

## Comprobación entre procesos

Además de las pruebas automáticas, se ejecutaron el servidor real y el cliente real en dos procesos
de Java distintos, en el puerto `9008`, registrando las notificaciones de cada uno:

```
[CLI] estado CONECTANDO localhost:9008
[CLI] estado CONECTADO localhost:9008
[CLI] resultado total=120, totalConReserva=132, clasificacion=Reunión mediana
[CLI] resultado total=1, totalConReserva=2, clasificacion=Reunión pequeña
[CLI] resultado total=10000000, totalConReserva=11000000, clasificacion=Evento masivo
[CLI] estado DESCONECTADO

[SRV] estado EN_LINEA 9008
[SRV] EVENTO 127.0.0.1 - conectado --> Solicitud de verificación recibida y aceptada
[SRV] EVENTO 127.0.0.1 - datos recibidos (Personas: 40, Porciones por persona: 3) --> datos enviados (Total: 120, Con reserva: 132, Reunión mediana)
[SRV] EVENTO 127.0.0.1 - datos recibidos (Personas: 1, Porciones por persona: 1) --> datos enviados (Total: 1, Con reserva: 2, Reunión pequeña)
[SRV] EVENTO 127.0.0.1 - datos recibidos (Personas: 100000, Porciones por persona: 100) --> datos enviados (Total: 10.000.000, Con reserva: 11.000.000, Evento masivo)
[SRV] EVENTO 127.0.0.1 - desconectado --> Cliente ha cerrado la sesión
[SRV] estado DETENIDO 0
```

Las dos ventanas (`./scripts/run-server.sh` y `./scripts/run-client.sh`) se abrieron sin errores en
la consola.

## Guion para probar con las ventanas

| Paso | Acción | Resultado esperado |
|---|---|---|
| 1 | Iniciar el servidor en el puerto `9008`. | Estado «ONLINE (puerto 9008)» y una línea `[SERVICIO]` en el log. |
| 2 | En el cliente, conectar a `localhost:9008`. | Estado «CONECTADO a localhost:9008»; en el servidor aparece «conectado -->». |
| 3 | Calcular con `40` personas y `3` porciones. | Total `120`, reserva `132`, «Reunión mediana». |
| 4 | Calcular con `0` personas. | El cliente muestra el error de validación y no envía nada. |
| 5 | Calcular con texto en un campo. | Mensaje «debe ser un número entero». |
| 6 | Abrir un segundo cliente y calcular. | El servidor registra las peticiones de ambos, cada una con su puerto de origen. |
| 7 | Detener el servidor y calcular desde el cliente. | A los tres segundos aparece «El servidor no respondió al cálculo.». |
| 8 | Con el servidor detenido, pulsar CONECTAR. | «El servidor no respondió dentro del tiempo esperado.» y el estado vuelve a «DESCONECTADO». |
| 9 | Intentar iniciar el servidor en el puerto `80`. | Error «El puerto debe ser un número entre 1024 y 65535.». |
