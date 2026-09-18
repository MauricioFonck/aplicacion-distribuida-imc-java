package com.mauricio.porciones.dominio.excepciones;

import java.io.Serial;

/** Excepción lanzada cuando la respuesta del servidor es un error o no respeta el protocolo. */
public final class RespuestaServidorException extends DominioException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RespuestaServidorException(final String mensaje) {
        super(mensaje);
    }
}
