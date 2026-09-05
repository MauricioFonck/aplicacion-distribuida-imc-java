# Plan y resultado de pruebas

> **Estado de ejecución:** el código de las pruebas está incluido en el repositorio, pero
> todavía no se ejecutó porque el equipo donde se preparó esta entrega no tiene un JDK
> instalado. Para dejar constancia del resultado, ejecute `./scripts/test.sh` en un equipo
> con JDK 8 o superior y registre en este documento la salida obtenida.

## Entorno de trabajo previsto

| Elemento | Valor |
|---|---|
| Lenguaje | Java SE 8 (`javac --release 8`). |
| Dependencias externas | Ninguna. Las pruebas son clases con método `main`. |
| Puerto del servidor | `9008` en ejecución normal, `19008` en la prueba de integración. |
| Comando | `./scripts/test.sh` |

## Pruebas del cálculo (`PorcionesCalculatorTest`)

| Prueba | Entrada | Resultado esperado |
|---|---|---|
| Multiplicación básica | 25 personas, 3 porciones | Válido, total `75`, clasificación «Reunión mediana». |
| Una sola persona | 1 persona, 1 porción | Válido, total `1`, mensaje en singular. |
| Valores no positivos | 0 personas, 3 porciones | Inválido, mensaje «deben ser mayores que 0». |
| Valores negativos | 10 personas, −2 porciones | Inválido, total `0`. |
| Límite de personas | 100.001 personas, 1 porción | Inválido. |
| Límite de porciones | 10 personas, 101 porciones | Inválido. |
| Caso máximo sin desbordamiento | 100.000 personas, 100 porciones | Válido, total `10.000.000`. |
| Margen de reserva | 75, 10 y 0 porciones | `83`, `11` y `0` respectivamente. |
| Clasificación | 10, 11, 200 y 201 personas | Pequeña, mediana, grande y evento masivo. |
| Formato de enteros | 75 y 10.000 | `75` sin separador y un valor de seis caracteres con separador de miles. |

## Prueba de integración TCP (`PorcionesSocketIntegrationTest`)

Levanta un servidor real en el puerto `19008`, conecta un cliente real y verifica el
intercambio completo de mensajes:

| Paso | Entrada | Resultado esperado |
|---|---|---|
| Primera solicitud | 25 personas, 3 porciones | Válido, total `75`, mensaje con «mediana». |
| Segunda solicitud en la misma conexión | 300 personas, 2 porciones | Válido, total `600`, mensaje con «masivo». |
| Solicitud rechazada | 0 personas, 5 porciones | Inválido, total `0`, sin cierre de la conexión. |
| Solicitud posterior al rechazo | 8 personas, 4 porciones | Válido, total `32`. |
| Estado del servidor | — | El servidor sigue activo y registró eventos en el log. |

Esta prueba confirma tres puntos: que el protocolo binario serializa y deserializa
correctamente, que una misma conexión atiende varias solicitudes y que un rechazo del
servidor no interrumpe la sesión del cliente.

## Comprobación manual sugerida

1. Ejecutar `./scripts/run-server.sh`, dejar el puerto `9008` y pulsar **INICIAR**.
2. Ejecutar `./scripts/run-client.sh`, escribir `localhost` y `9008`, y pulsar **CONECTAR**.
3. En la pestaña **CALCULAR PORCIONES**, introducir `40` personas y `3` porciones por
   persona. El total esperado es `120` y la sugerencia con reserva es `132`.
4. Introducir `0` personas y comprobar que la aplicación muestra el mensaje de validación sin
   perder la conexión.
5. Abrir un segundo cliente contra el mismo servidor y comprobar que ambos reciben respuesta
   y que el log del servidor registra las dos conexiones.
6. Cerrar el servidor y comprobar que el cliente informa el error al intentar calcular.
