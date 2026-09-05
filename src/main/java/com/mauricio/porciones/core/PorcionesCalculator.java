package com.mauricio.porciones.core;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Servicio de dominio para estimar las porciones de comida de una reunión.
 *
 * <p>El total se obtiene multiplicando la cantidad de asistentes por la cantidad
 * de porciones previstas para cada uno. La operación se realiza en el servidor.</p>
 */
public final class PorcionesCalculator {
    /** Cantidad máxima de asistentes admitida por el servicio. */
    public static final int MAX_ASISTENTES = 100_000;

    /** Cantidad máxima de porciones por persona admitida por el servicio. */
    public static final int MAX_PORCIONES_POR_PERSONA = 100;

    /** Porcentaje de porciones adicionales que se sugiere preparar como reserva. */
    public static final double MARGEN_RESERVA = 0.10;

    private static final Locale LOCALE = new Locale("es", "CO");

    private PorcionesCalculator() {
        // Clase utilitaria.
    }

    /**
     * Calcula el total de porciones necesarias.
     *
     * @param asistentes          cantidad de personas que asistirán a la reunión.
     * @param porcionesPorPersona cantidad de porciones previstas para cada persona.
     * @return el total de porciones con su mensaje, o un resultado inválido con el motivo.
     */
    public static PorcionesResult calculate(int asistentes, int porcionesPorPersona) {
        if (asistentes <= 0 || porcionesPorPersona <= 0) {
            return PorcionesResult.invalid(
                    "La cantidad de personas y las porciones por persona deben ser mayores que 0.");
        }
        if (asistentes > MAX_ASISTENTES) {
            return PorcionesResult.invalid(
                    "La cantidad de personas no puede superar " + format(MAX_ASISTENTES) + ".");
        }
        if (porcionesPorPersona > MAX_PORCIONES_POR_PERSONA) {
            return PorcionesResult.invalid(
                    "Las porciones por persona no pueden superar " + format(MAX_PORCIONES_POR_PERSONA) + ".");
        }

        long total = (long) asistentes * (long) porcionesPorPersona;
        return PorcionesResult.valid(total, describe(asistentes, porcionesPorPersona, total));
    }

    /**
     * Total sugerido incluyendo el margen de reserva.
     *
     * @param total porciones calculadas sin reserva.
     * @return el total redondeado hacia arriba tras aplicar el margen de reserva.
     */
    public static long withReserve(long total) {
        if (total <= 0L) {
            return 0L;
        }
        return (long) Math.ceil(total * (1.0 + MARGEN_RESERVA));
    }

    /** Clasifica la reunión según la cantidad de asistentes. */
    public static String classify(int asistentes) {
        if (asistentes <= 10) {
            return "Reunión pequeña";
        }
        if (asistentes <= 50) {
            return "Reunión mediana";
        }
        if (asistentes <= 200) {
            return "Reunión grande";
        }
        return "Evento masivo";
    }

    /** Da formato a una cantidad entera usando separadores de miles. */
    public static String format(long value) {
        return NumberFormat.getIntegerInstance(LOCALE).format(value);
    }

    private static String describe(int asistentes, int porcionesPorPersona, long total) {
        return classify(asistentes)
                + ": " + format(total) + " " + plural(total, "porción", "porciones")
                + " para " + format(asistentes) + " " + plural(asistentes, "persona", "personas")
                + ", a razón de " + format(porcionesPorPersona) + " "
                + plural(porcionesPorPersona, "porción", "porciones") + " por persona."
                + " Se sugiere preparar " + format(withReserve(total))
                + " para cubrir un 10 % de reserva.";
    }

    private static String plural(long amount, String singular, String pluralForm) {
        return amount == 1L ? singular : pluralForm;
    }
}
