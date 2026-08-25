package com.mauricio.imc.core;

/**
 * Resultado inmutable de una operación de cálculo de IMC.
 */
public final class ImcResult {
    private final float value;
    private final String message;
    private final boolean valid;

    private ImcResult(float value, String message, boolean valid) {
        this.value = value;
        this.message = message;
        this.valid = valid;
    }

    public static ImcResult valid(float value, String message) {
        return new ImcResult(value, message, true);
    }

    public static ImcResult invalid(String message) {
        return new ImcResult(0.0f, message, false);
    }

    public float getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        return "ImcResult{" +
                "value=" + value +
                ", message='" + message + '\'' +
                ", valid=" + valid +
                '}';
    }
}
