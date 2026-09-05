# Aplicación distribuida para calcular el IMC

Este proyecto contiene un cliente y un servidor desarrollados en Java. Ambos programas tienen una interfaz gráfica construida con Swing y se comunican mediante sockets TCP. El servidor recibe el peso y la altura, calcula el índice de masa corporal y devuelve el resultado al cliente.

> El IMC es un dato orientativo y no reemplaza la valoración de un profesional de la salud.

## Ramas del repositorio

Este repositorio aloja más de un proyecto. **Cada rama contiene una aplicación distinta** y las ramas de los parciales **no se mezclan hacia `main`**: la rama `main` es la base de código de referencia a partir de la cual se desarrollan los parciales, y se conserva sin cambios funcionales.

| Rama | Proyecto | Función |
|---|---|---|
| [`main`](../../tree/main) | Aplicación distribuida para calcular el IMC (este documento). | Base de código y documentación de referencia. |
| [`parcial-corte-1`](../../tree/parcial-corte-1) | **Parcial Corte 1** — Ejercicio 35: porciones de comida para una reunión. | Entrega del primer corte, desarrollada sobre esta base. |

El detalle del parcial se encuentra más abajo, en [Parcial Corte 1](#parcial-corte-1).

## Requisitos

Se necesita un JDK 8 o una versión posterior. El proyecto utiliza solamente clases de la biblioteca estándar de Java. También incluye un archivo `pom.xml` para abrirlo como proyecto Maven en NetBeans.

## Organización del proyecto

| Carpeta o archivo | Función |
|---|---|
| `src/main/java/com/mauricio/imc/core` | Cálculo del IMC y formato del protocolo. |
| `src/main/java/com/mauricio/imc/server` | Servidor TCP e interfaz del servidor. |
| `src/main/java/com/mauricio/imc/client` | Cliente TCP e interfaz del cliente. |
| `src/test/java/com/mauricio/imc/core` | Pruebas del cálculo y de la conexión TCP. |
| `scripts/compile.sh` | Compila las clases en la carpeta `out`. |
| `scripts/test.sh` | Compila y ejecuta las pruebas. |
| `scripts/run-server.sh` | Inicia el servidor. |
| `scripts/run-client.sh` | Inicia el cliente. |
| `docs/auditoria-de-bugs.md` | Revisión de los problemas encontrados en el código de la guía. |
| `docs/informe-academico.md` | Descripción general de la solución. |
| `docs/resultado-pruebas.md` | Resultados de las pruebas realizadas. |

## Compilar y probar

Desde una terminal situada en la carpeta principal del proyecto, se ejecuta:

```bash
./scripts/test.sh
```

El script compila las clases de producción y de prueba con `javac --release 8`. Después ejecuta la prueba del cálculo y la prueba de integración TCP.

## Ejecutar la aplicación

Primero se inicia el servidor:

```bash
./scripts/run-server.sh
```

En la ventana del servidor se deja el puerto `9007` y se pulsa **INICIAR**. En otra terminal se inicia el cliente:

```bash
./scripts/run-client.sh
```

En la ventana del cliente se escribe `localhost` como dirección y `9007` como puerto. Después se pulsa **CONECTAR**. En la pestaña **CALCULAR IMC** se introducen el peso en kilogramos y la altura en metros.

Por ejemplo, con un peso de `70` kg y una altura de `1.75` m, el resultado aproximado es `22.86`. La misma conexión puede utilizarse para realizar varios cálculos.

Para trabajar con dos equipos de la misma red, el servidor se ejecuta en uno de ellos y el cliente utiliza la dirección IP local del equipo servidor. En ese caso se debe permitir el puerto elegido en el firewall.

## Protocolo de comunicación

La solicitud enviada por el cliente contiene los siguientes valores, en este orden:

| Orden | Tipo | Dato |
|---|---|---|
| 1 | `float` | Peso en kilogramos. |
| 2 | `float` | Altura en metros. |

La respuesta del servidor contiene:

| Orden | Tipo | Dato |
|---|---|---|
| 1 | `float` | Resultado del IMC. |
| 2 | `UTF` | Mensaje con la clasificación o la validación. |

El servidor atiende cada conexión en un hilo independiente. El cliente utiliza `SwingWorker` para que las operaciones de red no congelen la ventana.

## Correcciones principales

La aplicación valida el puerto, la dirección del servidor, el peso y la altura antes de utilizarlos. También controla el caso en que el usuario intenta calcular sin estar conectado. Las conexiones se cierran de forma segura y el servidor puede atender varias solicitudes sin utilizar llamadas recursivas.

Los archivos `docs/arquitectura.puml` y `docs/arquitectura.png` muestran el flujo de la comunicación entre el cliente, el servidor y el módulo de cálculo.

## Abrir el proyecto en NetBeans

En NetBeans se puede seleccionar **File > Open Project** y elegir la carpeta del repositorio. Si se utiliza el archivo `pom.xml`, NetBeans reconocerá el proyecto como una aplicación Java. Las clases principales para ejecutar son `com.mauricio.imc.server.ServerWindow` y `com.mauricio.imc.client.ClientWindow`.

## Parcial Corte 1

El código del **Parcial Corte 1** no se encuentra en `main`. Está publicado en la rama [`parcial-corte-1`](../../tree/parcial-corte-1) de este mismo repositorio.

### Enunciado del parcial

> **Ejercicio 35: Porciones de comida para una reunión.** Desarrolle una aplicación que permita estimar la cantidad de porciones de comida necesarias para una reunión. El cliente solicitará la cantidad de personas que asistirán y la cantidad de porciones previstas por persona. El servidor calculará el total de porciones necesarias y devolverá el resultado.

### Relación con este proyecto

El parcial **se desarrolló tomando como base la documentación y la base de código de este proyecto de la rama `main`**. No se partió de un repositorio vacío: la aplicación del IMC descrita en este documento aportó la arquitectura, las decisiones de diseño y el estándar de documentación que la entrega del parcial conserva.

De este proyecto se reutilizaron los siguientes elementos:

| Elemento de `main` | Uso en el parcial |
|---|---|
| Separación en los paquetes `core`, `server` y `client`. | Se mantiene con el paquete raíz `com.mauricio.porciones`. |
| `ImcCalculator` (cálculo y validación en una clase utilitaria). | Molde de `PorcionesCalculator`. |
| `ImcResult` (resultado inmutable con estado válido o inválido). | Molde de `PorcionesResult`. |
| `ImcProtocol` (protocolo binario sobre `DataInputStream` y `DataOutputStream`). | Molde de `PorcionesProtocol`. |
| `ImcServer` (servidor TCP con hilo aceptador, un hilo por cliente y log con marca de tiempo). | Molde de `PorcionesServer`. |
| `ImcClient` (cliente TCP con tiempos de espera de conexión y de lectura). | Molde de `PorcionesClient`. |
| `ServerWindow` y `ClientWindow` (ventanas Swing con pestañas y `SwingWorker`). | Se conservan la estructura visual y el manejo de hilos de la interfaz. |
| Pruebas ejecutables con `main`, sin dependencias externas. | Se conserva el mismo estilo de pruebas. |
| Scripts `compile.sh`, `test.sh`, `run-server.sh` y `run-client.sh`. | Se conservan y se adaptan a las clases del parcial. |
| Estructura de `docs/` y esquema de este `README.md`. | Se conserva el mismo esquema de documentación. |

Lo que cambia en el parcial es la regla de negocio y el protocolo que la transporta:

| Aspecto | `main` (IMC) | `parcial-corte-1` (porciones) |
|---|---|---|
| Cálculo | `peso / altura²` | `personas × porciones por persona` |
| Solicitud | `float` peso + `float` altura | `int` personas + `int` porciones por persona |
| Respuesta | `float` resultado + `UTF` mensaje | `boolean` validez + `long` total + `UTF` mensaje |
| Puerto por defecto | `9007` | `9008` |
| Paquete raíz | `com.mauricio.imc` | `com.mauricio.porciones` |

La rama del parcial documenta además una mejora sobre el protocolo de esta base: al incluir un indicador de validez en la respuesta, el cliente puede distinguir un cálculo aceptado de una solicitud rechazada por el servidor.

### Cómo consultar el parcial

```bash
git fetch origin
git checkout parcial-corte-1
```

La documentación de la entrega está en el `README.md` de esa rama y en sus archivos `docs/informe-academico.md`, `docs/relacion-con-el-proyecto-base.md` y `docs/resultado-pruebas.md`.

> Esta rama `main` **no recibe mezclas** desde `parcial-corte-1`. Ambos proyectos evolucionan por separado.

## Uso académico

El proyecto fue elaborado para practicar interfaces Swing, programación con sockets TCP, manejo de hilos, validación de datos y separación de responsabilidades en una aplicación distribuida.
