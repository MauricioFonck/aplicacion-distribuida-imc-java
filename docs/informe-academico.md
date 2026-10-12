# Informe académico — Cliente-servidor RMI

## Objetivo

La aplicación resuelve el Ejercicio 35: estimar las porciones de comida para una reunión. El cliente solicita la cantidad de personas y las porciones por persona; el servidor calcula el total y una reserva adicional del 10 %.

## Arquitectura

La solución se separa en dos proyectos independientes:

- **Servidor:** contiene el dominio `Estimacion`, el contrato remoto, la implementación `PorcionesRemoteImpl` y el ciclo de vida de `RmiServer`.
- **Cliente:** contiene el adaptador `PorcionesClient`, que localiza el servicio en el registro RMI.
- **Contrato:** `PorcionesRemote` declara las operaciones `conectar`, `calcular` y `desconectar`. `PorcionesResponse` implementa `Serializable` para poder transportarse por RMI.
- **Interfaz:** las ventanas Swing usan `SwingWorker`, evitando bloquear el hilo de eventos mientras se realizan llamadas remotas.

El registro RMI se publica por defecto en el puerto `1099` con el nombre `PorcionesService`. El servidor atiende las llamadas concurrentes con los hilos administrados por RMI.

## Regla de negocio

El total se calcula como `personas × porcionesPorPersona` usando `long`. La reserva se redondea hacia arriba: `total + ceil(total × 10 / 100)`. El servidor valida de nuevo los límites de 1 a 100.000 personas y de 1 a 100 porciones por persona.

## Diferencia con UDP

La versión anterior transportaba texto delimitado dentro de datagramas y debía gestionar pérdida de paquetes. La versión actual utiliza un contrato remoto tipado, serialización de objetos y llamadas a métodos remotos. No se comparte el código del dominio entre procesos: el resultado y el contrato se mantienen idénticos en ambos proyectos para respetar la independencia de cliente y servidor.

## Conclusión

La aplicación conserva el ejercicio y la reserva del primer corte, pero cambia el mecanismo de comunicación a Java RMI. La lógica está aislada del transporte, el servidor es responsable de la validación definitiva y la interfaz continúa operando sin congelarse durante la comunicación.
