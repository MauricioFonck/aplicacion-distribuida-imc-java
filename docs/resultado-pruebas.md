# Resultado de pruebas

## Entorno de trabajo

| Elemento | Valor |
|---|---|
| Sistema operativo | Windows 11 (64 bits). |
| JDK | Java 26.0.2.1 (`javac 26.0.2.1`). |
| Nivel de compilación | `javac --release 8`, para conservar la compatibilidad con Java 8. |
| Dependencias externas | Ninguna. Las pruebas son clases con método `main`. |
| Puerto del servidor | `9008` en ejecución normal, `19008` en la prueba de integración. |
| Comando | `./scripts/test.sh` |

La compilación emite tres avisos del JDK (`source value 8 is obsolete`, `target value 8 is obsolete`) porque las versiones recientes del compilador marcan el nivel 8 como obsoleto. Son advertencias informativas: no impiden la compilación ni la ejecución.

## Resultado general

El comando `./scripts/test.sh` terminó correctamente:

```
Compilación completada en /c/Users/andre/Music/aplicacion-distribuida-imc-java/out
Todas las pruebas de PorcionesCalculator pasaron.
La prueba de integración TCP pasó.
Todas las pruebas pasaron.
```

## Pruebas del cálculo (`PorcionesCalculatorTest`)

| Prueba | Entrada | Resultado esperado | Resultado |
|---|---|---|---|
| Multiplicación básica | 25 personas, 3 porciones | Válido, total `75`, clasificación «Reunión mediana». | Correcto. |
| Una sola persona | 1 persona, 1 porción | Válido, total `1`, mensaje en singular. | Correcto. |
| Valores no positivos | 0 personas, 3 porciones | Inválido, mensaje «deben ser mayores que 0». | Correcto. |
| Valores negativos | 10 personas, −2 porciones | Inválido, total `0`. | Correcto. |
| Límite de personas | 100.001 personas, 1 porción | Inválido. | Correcto. |
| Límite de porciones | 10 personas, 101 porciones | Inválido. | Correcto. |
| Caso máximo sin desbordamiento | 100.000 personas, 100 porciones | Válido, total `10.000.000`. | Correcto. |
| Margen de reserva | 75, 10, 120 y 0 porciones | `83`, `11`, `132` y `0`. | Correcto. |
| Reserva exacta sin redondeo de más | 200, 1.000 y 10.000.000 porciones | `220`, `1.100` y `11.000.000`. | Correcto. |
| Clasificación | 10, 11, 200 y 201 personas | Pequeña, mediana, grande y evento masivo. | Correcto. |
| Formato de enteros | 75 y 10.000 | `75` sin separador y `10.000` con separador de miles. | Correcto. |

### Nota sobre el cálculo de la reserva

La primera versión de `PorcionesCalculator.withReserve` calculaba la reserva con coma flotante, mediante `Math.ceil(total * 1.1)`. Esa expresión falla justamente en los casos en que el resultado debería ser exacto: en `double`, `200 * 1.1` vale `220.00000000000003`, de modo que `Math.ceil` devolvía `221` en lugar de `220`.

La versión definitiva usa aritmética entera, que es exacta:

```java
long reserva = (total * PORCENTAJE_RESERVA + 99L) / 100L;
return total + reserva;
```

Se agregó la prueba `shouldNotRoundUpWhenTheReserveIsExact` para dejar cubierta esa regresión.

## Prueba de integración TCP (`PorcionesSocketIntegrationTest`)

Levanta un servidor real en el puerto `19008`, conecta un cliente real y verifica el intercambio completo de mensajes:

| Paso | Entrada | Resultado esperado | Resultado |
|---|---|---|---|
| Primera solicitud | 25 personas, 3 porciones | Válido, total `75`, mensaje con «mediana». | Correcto. |
| Segunda solicitud en la misma conexión | 300 personas, 2 porciones | Válido, total `600`, mensaje con «masivo». | Correcto. |
| Solicitud rechazada | 0 personas, 5 porciones | Inválido, total `0`, sin cierre de la conexión. | Correcto. |
| Solicitud posterior al rechazo | 8 personas, 4 porciones | Válido, total `32`. | Correcto. |
| Estado del servidor | — | Sigue activo y registró eventos en el log. | Correcto. |

Esta prueba confirma tres puntos: que el protocolo binario serializa y deserializa correctamente, que una misma conexión atiende varias solicitudes y que un rechazo del servidor no interrumpe la sesión del cliente.

## Comprobación de extremo a extremo

Se ejecutó además un cliente contra un servidor real para verificar los valores que aparecen en el `README.md`. Salida obtenida:

```
40 personas x 3 porciones
  valido = true
  total  = 120
  reserva= 132
  mensaje= Reunión mediana: 120 porciones para 40 personas, a razón de 3 porciones
           por persona. Se sugiere preparar 132 para cubrir un 10 % de reserva.

0 personas x 5 porciones (invalida)
  valido = false
  total  = 0
  mensaje= La cantidad de personas y las porciones por persona deben ser mayores que 0.

caso maximo
  valido = true
  total  = 10.000.000
  reserva= 11.000.000
```

El log del servidor registró cada solicitud, incluida la rechazada:

```
[2026-09-04 20:44:57] Servidor iniciado en 192.168.1.50:29008
[2026-09-04 20:44:57] Cliente conectado: /127.0.0.1:51684
[2026-09-04 20:44:57] Solicitud atendida desde /127.0.0.1:51684: personas=40,
                      porciones/persona=3, total=120
[2026-09-04 20:44:57] Solicitud atendida desde /127.0.0.1:51684: personas=0,
                      porciones/persona=5, total=rechazada (La cantidad de personas
                      y las porciones por persona deben ser mayores que 0.)
[2026-09-04 20:44:57] Servidor detenido
[2026-09-04 20:44:57] Cliente desconectado: /127.0.0.1:51684
```

## Comprobación de las interfaces gráficas

Se instanciaron `ServerWindow` y `ClientWindow` dentro del hilo de despacho de eventos de Swing para verificar que la construcción de los paneles no produce errores:

```
ServerWindow construida: Servidor de porciones - TCP java.awt.Dimension[width=544,height=505]
ClientWindow construida: Cliente de porciones - TCP java.awt.Dimension[width=400,height=334]
Las dos ventanas se construyeron sin error.
```

## Comprobación manual sugerida

1. Ejecutar `./scripts/run-server.sh`, dejar el puerto `9008` y pulsar **INICIAR**.
2. Ejecutar `./scripts/run-client.sh`, escribir `localhost` y `9008`, y pulsar **CONECTAR**.
3. En la pestaña **CALCULAR PORCIONES**, introducir `40` personas y `3` porciones por persona. El total esperado es `120` y la sugerencia con reserva es `132`.
4. Introducir `0` personas y comprobar que la aplicación muestra el mensaje de validación sin perder la conexión.
5. Abrir un segundo cliente contra el mismo servidor y comprobar que ambos reciben respuesta y que el log del servidor registra las dos conexiones.
6. Cerrar el servidor y comprobar que el cliente informa el error al intentar calcular.

> **Nota sobre la consola de Windows.** Si las pruebas se ejecutan desde una consola configurada con una página de códigos distinta de UTF-8, las tildes pueden verse alteradas en la salida de texto (`integraci?n` en lugar de `integración`). Es un efecto de la consola, no de la aplicación: las ventanas Swing muestran los acentos correctamente. Puede evitarse ejecutando `java` con `-Dstdout.encoding=UTF-8`.
