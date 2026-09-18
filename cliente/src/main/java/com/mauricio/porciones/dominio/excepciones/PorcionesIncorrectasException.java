package com.mauricio.porciones.dominio.excepciones;

import java.io.Serial;

/** Excepción lanzada cuando la cantidad de porciones por persona no es válida. */
public final class PorcionesIncorrectasException extends DominioException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PorcionesIncorrectasException(final String mensaje) {
        super(mensaje);
    }
}
