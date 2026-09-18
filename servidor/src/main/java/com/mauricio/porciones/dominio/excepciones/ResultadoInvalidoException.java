package com.mauricio.porciones.dominio.excepciones;

import java.io.Serial;

/** Excepción lanzada cuando un resultado de porciones viola sus invariantes. */
public final class ResultadoInvalidoException extends DominioException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ResultadoInvalidoException(final String mensaje) {
        super(mensaje);
    }
}
