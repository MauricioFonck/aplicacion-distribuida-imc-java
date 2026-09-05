# Relación con el proyecto base de la rama `main`

Este documento explica de forma explícita qué se tomó de la rama `main` y qué se
construyó específicamente para el **Parcial Corte 1**.

## Origen del trabajo

El parcial no partió de un repositorio vacío. Se desarrolló sobre la base de código y la
documentación de la aplicación distribuida de IMC que se encuentra en la rama `main` de
este mismo repositorio:

- Repositorio: `MauricioFonck/aplicacion-distribuida-imc-java`
- Rama base: `main`
- Rama del parcial: `parcial-corte-1`

La rama `main` conserva el proyecto de IMC sin modificaciones funcionales y **no recibe
mezclas** desde la rama del parcial. Ambas ramas son proyectos independientes que
comparten una misma arquitectura de referencia.

## Qué se reutilizó de `main`

| Elemento del proyecto base | Cómo se reutilizó en el parcial |
|---|---|
| Separación en paquetes `core`, `server` y `client` | Se mantuvo idéntica, cambiando el paquete raíz a `com.mauricio.porciones`. |
| Clase de dominio inmutable con estado válido/inválido (`ImcResult`) | Sirvió de molde para `PorcionesResult`. |
| Clase utilitaria de cálculo y validación (`ImcCalculator`) | Sirvió de molde para `PorcionesCalculator`. |
| Protocolo binario sobre `DataInputStream` / `DataOutputStream` (`ImcProtocol`) | Sirvió de molde para `PorcionesProtocol`, con los tipos de dato propios del ejercicio. |
| Servidor TCP multihilo con registro de eventos (`ImcServer`) | Sirvió de molde para `PorcionesServer`: hilo aceptador, un hilo por cliente, cierre ordenado y log con marca de tiempo. |
| Cliente TCP con tiempos de espera (`ImcClient`) | Sirvió de molde para `PorcionesClient`. |
| Ventanas Swing con pestañas y `SwingWorker` (`ClientWindow`, `ServerWindow`) | Se conservó la estructura visual y el manejo de hilos de la interfaz. |
| Pruebas sin dependencias externas, ejecutables con `main` | Se conservó el mismo estilo en `PorcionesCalculatorTest` y `PorcionesSocketIntegrationTest`. |
| Scripts `compile.sh`, `test.sh`, `run-server.sh`, `run-client.sh` | Se conservaron y se adaptaron a las nuevas clases principales. |
| Estructura de `docs/` y del `README.md` | Se conservó el mismo esquema de documentación. |

## Qué es propio del parcial

- Regla de negocio del **Ejercicio 35**: el total de porciones se obtiene multiplicando la
  cantidad de asistentes por las porciones previstas para cada persona.
- Tipos de dato del protocolo: la solicitud pasó de dos `float` a dos `int`, y la respuesta
  pasó de `float` + `UTF` a `boolean` + `long` + `UTF`.
- Validaciones de dominio propias: valores estrictamente mayores que cero y límites
  superiores (`MAX_ASISTENTES` y `MAX_PORCIONES_POR_PERSONA`).
- Cálculo del total en `long` para evitar desbordamiento en el caso máximo.
- Clasificación de la reunión por cantidad de asistentes y sugerencia de un 10 % de reserva.
- Interfaz del cliente con los campos y resultados propios del ejercicio.
- Batería de pruebas propia del cálculo de porciones y del protocolo.

## Mejora aplicada sobre el protocolo base

En el proyecto de `main`, el método `ImcProtocol.readResponse` reconstruye siempre un
resultado válido, de modo que el cliente no puede distinguir un cálculo correcto de un
mensaje de validación del servidor.

En el parcial se corrigió esa limitación: la respuesta incluye un `boolean` inicial que
indica si el cálculo fue aceptado. Con ese indicador, `PorcionesProtocol.readResponse`
reconstruye un resultado válido o inválido según corresponda, y la interfaz del cliente
puede mostrar el mensaje de validación como un error en lugar de presentar un total de
cero como si fuera un resultado correcto.
