package com.mauricio.porciones.dominio.excepciones;

import java.io.Serial;

/** Excepción lanzada cuando la cantidad de personas que asistirán no es válida. */
public final class AsistentesIncorrectosException extends DominioException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AsistentesIncorrectosException(final String mensaje) {
        super(mensaje);
    }
}
