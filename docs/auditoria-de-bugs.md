# Revisión de errores de la aplicación distribuida IMC

La guía describe una aplicación cliente-servidor desarrollada con Java Swing y sockets TCP. El servidor recibe el peso y la altura, calcula el índice de masa corporal y devuelve el resultado al cliente. Al revisar el código se encontraron los siguientes problemas.

## Problemas encontrados y solución aplicada

| Área | Problema | Solución aplicada |
|---|---|---|
| Puerto del servidor | La validación original podía aceptar valores nulos o fuera del rango permitido. | Se comprueba que el puerto esté entre 1 y 65535 antes de iniciar el servicio. |
| Atención de clientes | La respuesta volvía a llamar de forma recursiva al método que leía los datos. | Se utiliza un ciclo controlado por conexión, lo que permite realizar varios cálculos sin aumentar la pila de llamadas. |
| Conexiones simultáneas | La colección de clientes podía modificarse mientras se recorría. | Se usa una colección concurrente y cada socket se cierra de forma segura. |
| Estado del cliente | Se podía intentar calcular antes de crear la conexión. | Se comprueba que el cliente esté conectado antes de enviar una solicitud. |
| Entradas numéricas | Los campos se convertían directamente y podían generar excepciones. | Se validan campos vacíos, números incorrectos, valores no finitos y valores menores o iguales que cero. También se acepta la coma decimal. |
| Interfaz Swing | Algunas actualizaciones podían hacerse desde un hilo distinto al hilo de eventos. | Los cambios del log se envían al hilo de eventos con `SwingUtilities.invokeLater`. Las tareas de red se ejecutan con `SwingWorker`. |
| Cierre de recursos | Los streams y sockets no se cerraban siempre de manera uniforme. | Se centraliza el cierre mediante `try-with-resources` y métodos de limpieza seguros. |
| Protocolo | El orden de los datos enviados no estaba documentado en una clase común. | `ImcProtocol` define la lectura y escritura de peso, altura, resultado y mensaje. |
| Modelo de cálculo | La lógica de cálculo estaba mezclada con la ventana y utilizaba un resultado mutable. | `ImcCalculator` contiene el cálculo y `ImcResult` representa el resultado de forma inmutable. |
| Dirección del servidor | La interfaz podía confundir la dirección local con la dirección donde se escuchan conexiones. | El cliente permite escribir la dirección del equipo servidor y el servidor muestra su dirección local como información. |

## Comportamiento esperado

El servidor debe iniciar en un puerto válido, aceptar conexiones y continuar atendiendo solicitudes mientras esté activo. El cliente debe conectarse, enviar los datos y mostrar la respuesta sin congelar la interfaz. Si se introduce un dato incorrecto o se interrumpe la conexión, debe mostrarse un mensaje y la aplicación debe permanecer abierta.

## Resultado

Las correcciones se comprobaron mediante pruebas del cálculo y una prueba de integración que conecta un cliente con un servidor real. Los resultados se encuentran en `docs/resultado-pruebas.md`.
