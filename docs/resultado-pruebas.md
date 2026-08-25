# Resultado de pruebas

## Entorno de trabajo

La verificación se realizó en Linux con OpenJDK 21.0.12. El código se compiló con `javac --release 8` para conservar la compatibilidad con la versión de Java utilizada en la guía.

## Pruebas realizadas

| Verificación | Método utilizado | Resultado |
|---|---|---|
| Compilación del proyecto | `./scripts/compile.sh` | Correcta. |
| Cálculo de un IMC dentro del rango saludable | `ImcCalculatorTest` | Correcto. |
| Rechazo de peso igual a cero | `ImcCalculatorTest` | Correcto. |
| Rechazo de valores no numéricos o no finitos | `ImcCalculatorTest` | Correcto. |
| Presentación del resultado con dos decimales | `ImcCalculatorTest` | Correcto. |
| Inicio del servidor TCP | `ImcSocketIntegrationTest` | Correcto. |
| Conexión del cliente al servidor | `ImcSocketIntegrationTest` | Correcto. |
| Realización de dos cálculos con una misma conexión | `ImcSocketIntegrationTest` | Correcto. |
| Registro de los eventos de conexión | `ImcSocketIntegrationTest` | Correcto. |
| Revisión de formato de los archivos | `git diff --check` | Sin errores. |

## Resultado general

El comando `./scripts/test.sh` terminó correctamente. La prueba del cálculo verificó los casos válidos y las entradas incorrectas. La prueba de integración inició un servidor, conectó un cliente, envió dos solicitudes y comprobó las respuestas recibidas.

La compilación mostró únicamente avisos del JDK sobre el uso del nivel de fuente 8. Estos avisos no impidieron la compilación ni la ejecución de las pruebas.

## Comprobación manual

También se comprobó el flujo de uso de la aplicación. Se inicia el servidor en el puerto `9007`, se conecta el cliente utilizando `localhost` y se envían los valores `70` kg y `1.75` m. El resultado esperado es un IMC aproximado de `22.86`, acompañado del mensaje correspondiente.

Después se pueden probar campos vacíos, texto en lugar de números y valores iguales o menores que cero. En esos casos se muestra un mensaje de validación y la aplicación continúa funcionando.
