package com.mauricio.porciones.dominio.modelos;

import com.mauricio.porciones.dominio.enums.ClasificacionReunion;
import com.mauricio.porciones.dominio.excepciones.EstimacionInvalidaException;
import com.mauricio.porciones.dominio.vo.Asistentes;
import com.mauricio.porciones.dominio.vo.PorcionesPorPersona;
import java.util.Objects;

/**
 * Modelo del dominio para la estimación de las porciones de comida de una reunión.
 * Responsabilidad única: aplicar la regla de cálculo del total, la reserva sugerida y la
 * clasificación de la reunión.
 */
public final class Estimacion {

    /** Porcentaje de porciones adicionales que se sugiere preparar como reserva. */
    public static final int PORCENTAJE_RESERVA = 10;

    private final Asistentes asistentes;
    private final PorcionesPorPersona porcionesPorPersona;

    public Estimacion(final Asistentes asistentes, final PorcionesPorPersona porcionesPorPersona) {
        if (Objects.isNull(asistentes) || Objects.isNull(porcionesPorPersona)) {
            throw new EstimacionInvalidaException(
                    "La cantidad de personas y las porciones por persona son obligatorias.");
        }
        this.asistentes = asistentes;
        this.porcionesPorPersona = porcionesPorPersona;
    }

    /**
     * Calcula el total de porciones multiplicando las personas por las porciones previstas para
     * cada una. El producto se hace en {@code long} para que el caso máximo no se desborde.
     *
     * @return resultado con el total, el total con reserva, la clasificación y el mensaje.
     */
    public Resultado calcular() {
        final long total = (long) asistentes.valor() * (long) porcionesPorPersona.valor();
        final long totalConReserva = conReserva(total);
        final ClasificacionReunion clasificacion = ClasificacionReunion.deAsistentes(asistentes.valor());
        return new Resultado(total, totalConReserva, clasificacion, describir(total, totalConReserva, clasificacion));
    }

    /**
     * Total sugerido incluyendo el margen de reserva, redondeado hacia arriba.
     *
     * <p>Se usa aritmética entera porque con coma flotante 200 * 1.1 produce 220.00000000000003
     * y el redondeo hacia arriba se desvía en una porción.</p>
     */
    public static long conReserva(final long total) {
        if (total <= 0L) {
            return 0L;
        }
        final long reserva = (total * PORCENTAJE_RESERVA + 99L) / 100L;
        return total + reserva;
    }

    public Asistentes getAsistentes() {
        return asistentes;
    }

    public PorcionesPorPersona getPorcionesPorPersona() {
        return porcionesPorPersona;
    }

    private String describir(
            final long total, final long totalConReserva, final ClasificacionReunion clasificacion) {
        return clasificacion.getDescripcion()
                + ": " + Resultado.formatear(total) + " " + plural(total, "porción", "porciones")
                + " para " + Resultado.formatear(asistentes.valor()) + " "
                + plural(asistentes.valor(), "persona", "personas")
                + ", a razón de " + Resultado.formatear(porcionesPorPersona.valor()) + " "
                + plural(porcionesPorPersona.valor(), "porción", "porciones") + " por persona."
                + " Se sugiere preparar " + Resultado.formatear(totalConReserva)
                + " para cubrir un " + PORCENTAJE_RESERVA + " % de reserva.";
    }

    private static String plural(final long cantidad, final String singular, final String plural) {
        return cantidad == 1L ? singular : plural;
    }
}
