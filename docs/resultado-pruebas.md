# Resultado de pruebas — RMI

## Comando ejecutado

```bash
./scripts/test.sh
```

## Resultado

| Verificación | Resultado |
|---|---|
| Compilación del servidor con `javac --release 8` | Correcta |
| Compilación del cliente con `javac --release 8` | Correcta |
| Cálculo de 40 personas y 3 porciones | 120 porciones |
| Reserva del 10 % | 132 porciones |
| Rechazo de personas fuera de rango | Correcto |
| Cálculo máximo sin desbordamiento | Correcto (`10.000.000`) |
| Inicio de registro RMI en puerto de prueba | Correcto |
| Búsqueda del servicio por el cliente | Correcto |
| Llamada remota `calcular` | Correcto |
| Validación realizada en el servidor | Correcto |
| Cierre del registro y objetos exportados | Correcto |
| `git diff --check` | Sin errores |

Las advertencias mostradas por el JDK indican que el nivel de fuente 8 es antiguo; no impiden la compilación ni las pruebas. Se conserva Java 8 porque es compatible con la guía y con la API estándar de RMI.
