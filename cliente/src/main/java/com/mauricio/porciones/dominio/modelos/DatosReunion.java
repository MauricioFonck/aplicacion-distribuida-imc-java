package com.mauricio.porciones.dominio.modelos;

import com.mauricio.porciones.dominio.vo.Asistentes;
import com.mauricio.porciones.dominio.vo.PorcionesPorPersona;
import java.util.Objects;

/** Datos de la reunión que el cliente envía al servidor para que calcule las porciones. */
public record DatosReunion(Asistentes asistentes, PorcionesPorPersona porcionesPorPersona) {

    public DatosReunion {
        Objects.requireNonNull(asistentes, "La cantidad de personas es obligatoria.");
        Objects.requireNonNull(porcionesPorPersona, "Las porciones por persona son obligatorias.");
    }
}
