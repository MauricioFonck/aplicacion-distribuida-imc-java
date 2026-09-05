# Parcial Corte 1 — Porciones de comida para una reunión

Aplicación distribuida cliente–servidor en Java que estima la cantidad de porciones de comida
necesarias para una reunión. El cliente solicita la cantidad de personas que asistirán y las
porciones previstas por persona; el servidor recibe los datos por un socket TCP, calcula el
total de porciones y devuelve el resultado.

> **Rama:** `parcial-corte-1` · **Enunciado:** Ejercicio 35 · **Base:** rama `main` de este
> mismo repositorio.

## Origen del proyecto

Este parcial se desarrolló **a partir de la base de código y la documentación del proyecto
que se encuentra en la rama `main`** de este repositorio (aplicación distribuida para
calcular el IMC). De ese proyecto se conservaron la arquitectura por capas, el diseño del
servidor TCP multihilo, el estilo del protocolo binario, la estructura de las ventanas Swing
y el esquema de pruebas y documentación; sobre esa base se implementó la regla de negocio del
Ejercicio 35.

La rama `main` permanece intacta y **esta rama nunca se mezcla hacia `main`**: cada rama del
repositorio contiene un proyecto distinto. El detalle de lo reutilizado y de lo que es propio
del parcial está en [`docs/relacion-con-el-proyecto-base.md`](docs/relacion-con-el-proyecto-base.md).

## Enunciado

> Desarrolle una aplicación que permita estimar la cantidad de porciones de comida necesarias
> para una reunión. El cliente solicitará la cantidad de personas que asistirán y la cantidad
> de porciones previstas por persona. El servidor calculará el total de porciones necesarias y
> devolverá el resultado.

## Requisitos

Se necesita un JDK 8 o una versión posterior. El proyecto utiliza solamente clases de la
biblioteca estándar de Java. También incluye un archivo `pom.xml` para abrirlo como proyecto
Maven en NetBeans.

## Organización del proyecto

| Carpeta o archivo | Función |
|---|---|
| `src/main/java/com/mauricio/porciones/core` | Cálculo de porciones, resultado de dominio y protocolo. |
| `src/main/java/com/mauricio/porciones/server` | Servidor TCP e interfaz del servidor. |
| `src/main/java/com/mauricio/porciones/client` | Cliente TCP e interfaz del cliente. |
| `src/test/java/com/mauricio/porciones/core` | Pruebas del cálculo y de la conexión TCP. |
| `scripts/compile.sh` | Compila las clases en la carpeta `out`. |
| `scripts/test.sh` | Compila y ejecuta las pruebas. |
| `scripts/run-server.sh` | Inicia el servidor. |
| `scripts/run-client.sh` | Inicia el cliente. |
| `docs/informe-academico.md` | Informe del parcial: objetivo, arquitectura y conclusiones. |
| `docs/relacion-con-el-proyecto-base.md` | Qué se reutilizó del proyecto de `main` y qué es propio. |
| `docs/resultado-pruebas.md` | Plan de pruebas y comprobación manual. |
| `docs/arquitectura.puml` | Diagrama de componentes de la solución. |

## Compilar y probar

Desde una terminal situada en la carpeta principal del proyecto:

```bash
./scripts/test.sh
```

El script compila las clases de producción y de prueba con `javac --release 8`. Después
ejecuta la prueba del cálculo y la prueba de integración TCP.

## Ejecutar la aplicación

Primero se inicia el servidor:

```bash
./scripts/run-server.sh
```

En la ventana del servidor se deja el puerto `9008` y se pulsa **INICIAR**. En otra terminal
se inicia el cliente:

```bash
./scripts/run-client.sh
```

En la ventana del cliente se escribe `localhost` como dirección y `9008` como puerto, y se
pulsa **CONECTAR**. En la pestaña **CALCULAR PORCIONES** se introducen la cantidad de personas
y las porciones por persona.

Por ejemplo, con `40` personas y `3` porciones por persona, el total es `120` porciones y la
sugerencia con el margen de reserva es `132`. La misma conexión puede utilizarse para realizar
varios cálculos.

Para trabajar con dos equipos de la misma red, el servidor se ejecuta en uno de ellos y el
cliente utiliza la dirección IP local del equipo servidor. En ese caso se debe permitir el
puerto elegido en el firewall.

## Regla de cálculo

```
total de porciones = cantidad de personas × porciones por persona
```

El servidor admite únicamente enteros mayores que cero, con un máximo de `100.000` personas y
`100` porciones por persona. El total se calcula en `long`, de modo que el caso máximo
(`10.000.000` de porciones) no produce desbordamiento.

Además del total, el servidor clasifica la reunión según la cantidad de asistentes (pequeña,
mediana, grande o evento masivo) y sugiere preparar un 10 % adicional como margen de reserva.

## Protocolo de comunicación

La solicitud enviada por el cliente contiene los siguientes valores, en este orden:

| Orden | Tipo | Dato |
|---|---|---|
| 1 | `int` | Cantidad de personas que asistirán. |
| 2 | `int` | Cantidad de porciones previstas por persona. |

La respuesta del servidor contiene:

| Orden | Tipo | Dato |
|---|---|---|
| 1 | `boolean` | Indica si el cálculo fue aceptado. |
| 2 | `long` | Total de porciones necesarias (`0` cuando la solicitud se rechaza). |
| 3 | `UTF` | Mensaje con la clasificación o con el motivo del rechazo. |

El servidor atiende cada conexión en un hilo independiente y una misma conexión admite varias
solicitudes consecutivas. Una solicitud rechazada no cierra la conexión. El cliente utiliza
`SwingWorker` para que las operaciones de red no congelen la ventana.

## Validaciones

La aplicación valida el puerto, la dirección del servidor, la cantidad de personas y las
porciones por persona antes de utilizarlos, y controla el caso en que el usuario intenta
calcular sin estar conectado. El servidor vuelve a validar todos los datos recibidos, porque
no debe confiar en la validación hecha en el cliente.

## Abrir el proyecto en NetBeans

En NetBeans se puede seleccionar **File > Open Project** y elegir la carpeta del repositorio.
Si se utiliza el archivo `pom.xml`, NetBeans reconocerá el proyecto como una aplicación Java.
Las clases principales para ejecutar son `com.mauricio.porciones.server.ServerWindow` y
`com.mauricio.porciones.client.ClientWindow`.

## Uso académico

El proyecto fue elaborado como entrega del Parcial Corte 1, para practicar interfaces Swing,
programación con sockets TCP, manejo de hilos, validación de datos y separación de
responsabilidades en una aplicación distribuida.
