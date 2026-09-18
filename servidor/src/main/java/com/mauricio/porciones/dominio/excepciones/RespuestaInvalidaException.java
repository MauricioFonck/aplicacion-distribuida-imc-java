package com.mauricio.porciones.dominio.excepciones;

import java.io.Serial;

/** Excepción lanzada cuando una respuesta al cliente está incompleta o es incoherente. */
public final class RespuestaInvalidaException extends DominioException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RespuestaInvalidaException(final String mensaje) {
        super(mensaje);
    }
}
