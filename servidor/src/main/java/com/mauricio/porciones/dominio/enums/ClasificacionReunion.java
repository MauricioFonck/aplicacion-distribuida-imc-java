package com.mauricio.porciones.dominio.enums;

/** Clasificación de la reunión según la cantidad de personas que asistirán. */
public enum ClasificacionReunion {
    PEQUENA("Reunión pequeña"),
    MEDIANA("Reunión mediana"),
    GRANDE("Reunión grande"),
    MASIVA("Evento masivo");

    private final String descripcion;

    ClasificacionReunion(final String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Determina la clasificación que corresponde a una cantidad de asistentes.
     *
     * @param asistentes cantidad de personas que asistirán.
     * @return la categoría correspondiente.
     */
    public static ClasificacionReunion deAsistentes(final int asistentes) {
        if (asistentes <= 10) {
            return PEQUENA;
        } else if (asistentes <= 50) {
            return MEDIANA;
        } else if (asistentes <= 200) {
            return GRANDE;
        } else {
            return MASIVA;
        }
    }
}
