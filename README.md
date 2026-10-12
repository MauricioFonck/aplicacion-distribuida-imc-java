# Actividad 2 — Cliente-servidor RMI para porciones

Aplicación distribuida Java para resolver el **Ejercicio 35: porciones de comida para una reunión**. Esta versión reemplaza la comunicación UDP de `actividad-corte-2` por **Java RMI**, manteniendo la regla de negocio y las interfaces Swing.

## Arquitectura

- `servidor/`: publica `PorcionesRemote` en un registro RMI y ejecuta la regla de negocio.
- `cliente/`: busca el servicio remoto por nombre y realiza las operaciones mediante llamadas RMI.
- `PorcionesResponse`: DTO `Serializable` enviado entre ambos procesos.
- El servidor valida nuevamente los datos; el cliente no puede sustituir la validación del servidor.

## Regla de cálculo

`total = personas × porciones por persona`; se agrega una reserva del 10 % redondeada hacia arriba. Se aceptan de 1 a 100.000 personas y de 1 a 100 porciones por persona.

## Compilar y probar

```bash
./scripts/test.sh
```

El proyecto usa únicamente la biblioteca estándar y compila con `javac --release 8`.

## Ejecutar

En una terminal:

```bash
./scripts/run-server.sh
```

En otra terminal:

```bash
./scripts/run-client.sh
```

El servidor publica el objeto `PorcionesService` en el registro RMI del puerto `1099`. El cliente permite indicar host, puerto del registro y nombre del servicio. Ejemplo: 40 personas y 3 porciones producen 120, o 132 con reserva.

## Diferencia frente a UDP

RMI ofrece un contrato remoto tipado, serialización de objetos y una llamada semejante a un método local. No se envían datagramas ni se implementan manualmente tiempos de espera o delimitadores de texto.
