package com.mauricio.porciones.dominio.modelos;

import com.mauricio.porciones.dominio.enums.ClasificacionReunion;
import com.mauricio.porciones.dominio.excepciones.ResultadoInvalidoException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Representa el resultado inmutable de una estimación de porciones.
 * Responsabilidad única: encapsular los datos calculados y su formato de presentación.
 */
public final class Resultado {

    private static final Locale LOCALE = Locale.forLanguageTag("es-CO");

    private final long total;
    private final long totalConReserva;
    private final ClasificacionReunion clasificacion;
    private final String mensaje;

    public Resultado(
            final long total,
            final long totalConReserva,
            final ClasificacionReunion clasificacion,
            final String mensaje) {
        if (total <= 0L || totalConReserva < total
                || Objects.isNull(clasificacion) || Objects.isNull(mensaje) || mensaje.isBlank()) {
            throw new ResultadoInvalidoException("El resultado de las porciones contiene datos inválidos.");
        }
        this.total = total;
        this.totalConReserva = totalConReserva;
        this.clasificacion = clasificacion;
        this.mensaje = mensaje;
    }

    public long getTotal() {
        return total;
    }

    public long getTotalConReserva() {
        return totalConReserva;
    }

    public ClasificacionReunion getClasificacion() {
        return clasificacion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getTotalFormateado() {
        return formatear(total);
    }

    public String getTotalConReservaFormateado() {
        return formatear(totalConReserva);
    }

    /** Da formato a una cantidad entera usando separadores de miles. */
    public static String formatear(final long valor) {
        return NumberFormat.getIntegerInstance(LOCALE).format(valor);
    }
}
