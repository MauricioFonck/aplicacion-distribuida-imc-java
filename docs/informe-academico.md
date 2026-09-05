# Aplicación distribuida para estimar porciones de comida en una reunión

**Parcial Corte 1 — Ejercicio 35**

## Resumen

Se desarrolló una aplicación distribuida cliente–servidor en Java que estima la cantidad de
porciones de comida necesarias para una reunión. El cliente solicita al usuario la cantidad
de personas que asistirán y la cantidad de porciones previstas por persona; el servidor
recibe esos datos por un socket TCP, calcula el total de porciones y devuelve el resultado
junto con un mensaje descriptivo.

## Enunciado

> Desarrolle una aplicación que permita estimar la cantidad de porciones de comida
> necesarias para una reunión. El cliente solicitará la cantidad de personas que asistirán y
> la cantidad de porciones previstas por persona. El servidor calculará el total de porciones
> necesarias y devolverá el resultado.

## Objetivo

Aplicar los conceptos de programación distribuida vistos en el corte: comunicación mediante
sockets TCP, definición de un protocolo de aplicación, atención concurrente de clientes,
validación de datos de entrada y separación de responsabilidades entre la capa de dominio,
la capa de red y la interfaz gráfica.

## Punto de partida

El desarrollo se realizó sobre la base de código y la documentación del proyecto de la rama
`main` de este repositorio (aplicación distribuida para calcular el IMC). De ese proyecto se
tomaron la arquitectura, la organización en paquetes, el estilo del protocolo binario, el
diseño del servidor multihilo y el esquema de pruebas y de documentación. El detalle de lo
reutilizado y de lo que es propio del parcial se encuentra en
[`relacion-con-el-proyecto-base.md`](relacion-con-el-proyecto-base.md).

## Herramientas utilizadas

| Herramienta | Uso |
|---|---|
| Java SE 8 | Lenguaje y biblioteca estándar (`java.net`, `java.io`, `javax.swing`). |
| Swing | Interfaces gráficas del cliente y del servidor. |
| Sockets TCP | Transporte de las solicitudes y respuestas. |
| Maven | Descriptor `pom.xml` para abrir el proyecto en NetBeans. |
| Git y GitHub | Control de versiones y separación por ramas. |
| PlantUML | Diagrama de componentes (`arquitectura.puml`). |

## Arquitectura

La solución se organiza en tres paquetes:

- `com.mauricio.porciones.core`: reglas de negocio y protocolo. Contiene
  `PorcionesCalculator` (cálculo y validación), `PorcionesResult` (resultado inmutable) y
  `PorcionesProtocol` (serialización de solicitudes y respuestas).
- `com.mauricio.porciones.server`: `PorcionesServer` (servidor TCP multihilo) y
  `ServerWindow` (interfaz de administración y log).
- `com.mauricio.porciones.client`: `PorcionesClient` (cliente TCP) y `ClientWindow`
  (interfaz de captura de datos y presentación del resultado).

El paquete `core` no depende de la interfaz gráfica, por lo que puede probarse de forma
aislada. El diagrama de componentes se encuentra en `arquitectura.puml`.

## Regla de negocio

```
total de porciones = cantidad de personas × porciones por persona
```

El servidor acepta únicamente valores enteros estrictamente mayores que cero. Además aplica
límites superiores (100.000 personas y 100 porciones por persona) para descartar entradas
sin sentido y acotar el tamaño del resultado. El total se calcula en `long` para que el caso
máximo (10.000.000 de porciones) no produzca desbordamiento.

Como información complementaria, el servidor clasifica la reunión según la cantidad de
asistentes (pequeña, mediana, grande o evento masivo) y sugiere preparar un 10 % adicional
de porciones como margen de reserva.

## Protocolo de comunicación

Solicitud enviada por el cliente:

| Orden | Tipo | Dato |
|---|---|---|
| 1 | `int` | Cantidad de personas que asistirán. |
| 2 | `int` | Cantidad de porciones previstas por persona. |

Respuesta enviada por el servidor:

| Orden | Tipo | Dato |
|---|---|---|
| 1 | `boolean` | Indica si el cálculo fue aceptado. |
| 2 | `long` | Total de porciones necesarias (`0` cuando la solicitud es rechazada). |
| 3 | `UTF` | Mensaje de clasificación o motivo del rechazo. |

Una misma conexión admite varias solicitudes consecutivas. El servidor no cierra la conexión
cuando rechaza una solicitud: responde con el indicador en `false` y queda a la espera de la
siguiente.

## Concurrencia

`PorcionesServer` utiliza un hilo aceptador en segundo plano y crea un hilo por cada cliente
conectado. Todos los hilos son de tipo demonio para que la aplicación pueda cerrarse sin
dejar procesos activos. El conjunto de clientes se mantiene en un `Set` respaldado por un
`ConcurrentHashMap`, de modo que el cierre del servidor puede liberar todos los sockets
abiertos de forma segura.

En el cliente, la conexión y el cálculo se ejecutan dentro de un `SwingWorker` para que las
operaciones de red no bloqueen el hilo de despacho de eventos de Swing.

## Validación de datos

La validación se aplica en dos niveles:

1. **En el cliente**, antes de enviar: se verifica que los campos no estén vacíos, que
   contengan números enteros y que sean mayores que cero. También se valida el puerto y la
   dirección del servidor antes de conectar.
2. **En el servidor**, siempre: `PorcionesCalculator` vuelve a validar los valores recibidos.
   El servidor no confía en la validación del cliente, ya que un cliente distinto podría
   enviar datos fuera de rango.

## Verificación

Las pruebas se ejecutan con `./scripts/test.sh` y se detallan en
[`resultado-pruebas.md`](resultado-pruebas.md). Cubren el cálculo, las validaciones, el caso
máximo sin desbordamiento, el margen de reserva, la clasificación y el intercambio real de
mensajes sobre un socket TCP.

## Conclusiones

- La arquitectura del proyecto base resultó reutilizable sin cambios estructurales: solo fue
  necesario sustituir la regla de dominio y los tipos de dato del protocolo.
- Separar el paquete `core` de la interfaz gráfica permitió probar la lógica y el protocolo
  sin abrir ventanas.
- Validar en el servidor y no solo en el cliente es indispensable en una aplicación
  distribuida, porque el servidor es el único punto que atiende a todos los clientes.
- Devolver un indicador de validez en la respuesta evita que el cliente interprete un
  rechazo como un resultado correcto.

## Referencias

- Documentación oficial de Java SE 8: `java.net.Socket`, `java.net.ServerSocket`,
  `java.io.DataInputStream`, `java.io.DataOutputStream`.
- Guía de Swing: `javax.swing.SwingWorker` y modelo de hilos de la interfaz gráfica.
- Proyecto base de la rama `main` de este repositorio.
