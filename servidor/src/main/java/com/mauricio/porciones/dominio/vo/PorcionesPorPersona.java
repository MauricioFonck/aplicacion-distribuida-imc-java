package com.mauricio.porciones.dominio.vo;

import com.mauricio.porciones.dominio.excepciones.PorcionesIncorrectasException;

/** Value Object inmutable que encapsula y valida las porciones previstas por persona. */
public record PorcionesPorPersona(int valor) {

    /** Cantidad máxima de porciones por persona admitida por el servicio. */
    public static final int MAXIMO = 100;

    public PorcionesPorPersona {
        if (valor <= 0) {
            throw new PorcionesIncorrectasException(
                    "Porciones por persona incorrectas: deben ser mayores que 0.");
        }
        if (valor > MAXIMO) {
            throw new PorcionesIncorrectasException(
                    "Porciones por persona incorrectas: no pueden superar 100.");
        }
    }
}
