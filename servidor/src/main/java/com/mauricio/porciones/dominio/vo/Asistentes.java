package com.mauricio.porciones.dominio.vo;

import com.mauricio.porciones.dominio.excepciones.AsistentesIncorrectosException;

/** Value Object inmutable que encapsula y valida la cantidad de personas que asistirán. */
public record Asistentes(int valor) {

    /** Cantidad máxima de asistentes admitida por el servicio. */
    public static final int MAXIMO = 100_000;

    public Asistentes {
        if (valor <= 0) {
            throw new AsistentesIncorrectosException(
                    "Cantidad de personas incorrecta: debe ser mayor que 0.");
        }
        if (valor > MAXIMO) {
            throw new AsistentesIncorrectosException(
                    "Cantidad de personas incorrecta: no puede superar 100.000.");
        }
    }
}
