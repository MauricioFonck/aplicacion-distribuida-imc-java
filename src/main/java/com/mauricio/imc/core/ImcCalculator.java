package com.mauricio.imc.core;

import java.util.Locale;

/**
 * Servicio de dominio para calcular y clasificar el índice de masa corporal.
 */
public final class ImcCalculator {
    private ImcCalculator() {
        // Clase utilitaria.
    }

    public static ImcResult calculate(float weightKg, float heightMeters) {
        if (!Float.isFinite(weightKg) || !Float.isFinite(heightMeters)) {
            return ImcResult.invalid("El peso y la altura deben ser números finitos.");
        }
        if (weightKg <= 0.0f || heightMeters <= 0.0f) {
            return ImcResult.invalid("El peso y la altura deben ser mayores que 0.");
        }

        float bmi = weightKg / (heightMeters * heightMeters);
        if (!Float.isFinite(bmi)) {
            return ImcResult.invalid("No fue posible calcular el IMC con esos valores.");
        }

        String message;
        if (bmi < 18.5f) {
            message = "Peso bajo: consulta con un profesional de la salud.";
        } else if (bmi <= 24.9f) {
            message = "Peso saludable según el rango de referencia.";
        } else if (bmi <= 29.9f) {
            message = "Sobrepeso: considera consultar con un profesional de la salud.";
        } else {
            message = "Obesidad: consulta con un profesional de la salud.";
        }

        return ImcResult.valid(bmi, message);
    }

    public static String format(float value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
