package com.mauricio.porciones.core;

/**
 * Resultado inmutable de una estimación de porciones de comida.
 */
public final class PorcionesResult {
    private final long value;
    private final String message;
    private final boolean valid;

    private PorcionesResult(long value, String message, boolean valid) {
        this.value = value;
        this.message = message;
        this.valid = valid;
    }

    public static PorcionesResult valid(long value, String message) {
        return new PorcionesResult(value, message, true);
    }

    public static PorcionesResult invalid(String message) {
        return new PorcionesResult(0L, message, false);
    }

    /** Total de porciones necesarias para la reunión. */
    public long getValue() {
        return value;
    }

    /** Mensaje de clasificación cuando el resultado es válido, o de validación cuando no lo es. */
    public String getMessage() {
        return message;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        return "PorcionesResult{" +
                "value=" + value +
                ", message='" + message + '\'' +
                ", valid=" + valid +
                '}';
    }
}
