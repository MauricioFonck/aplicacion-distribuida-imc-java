package com.mauricio.porciones.dominio;

import com.mauricio.porciones.remoto.PorcionesResponse;

/** Regla de negocio independiente de RMI. */
public final class Estimacion {
    public static final int MAX_PERSONAS = 100_000;
    public static final int MAX_PORCIONES_POR_PERSONA = 100;
    private Estimacion() { }

    public static PorcionesResponse calcular(int personas, int porcionesPorPersona) {
        if (personas < 1 || personas > MAX_PERSONAS) {
            return PorcionesResponse.error("La cantidad de personas debe estar entre 1 y " + MAX_PERSONAS + ".");
        }
        if (porcionesPorPersona < 1 || porcionesPorPersona > MAX_PORCIONES_POR_PERSONA) {
            return PorcionesResponse.error("Las porciones por persona deben estar entre 1 y " + MAX_PORCIONES_POR_PERSONA + ".");
        }
        long total = (long) personas * porcionesPorPersona;
        long reserva = total + (total * 10 + 99) / 100;
        String clasificacion = personas <= 20 ? "Reunión pequeña"
                : personas <= 100 ? "Reunión mediana"
                : personas <= 500 ? "Reunión grande" : "Evento masivo";
        String mensaje = clasificacion + ": " + total + " porciones para " + personas
                + " personas. Se recomiendan " + reserva + " con reserva del 10 %.";
        return PorcionesResponse.ok(total, reserva, clasificacion, mensaje);
    }
}
