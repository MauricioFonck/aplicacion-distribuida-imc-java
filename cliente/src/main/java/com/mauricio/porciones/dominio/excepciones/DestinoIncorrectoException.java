package com.mauricio.porciones.dominio.excepciones;

import java.io.Serial;

/** Excepción lanzada cuando el host o el puerto del servidor de destino no son válidos. */
public final class DestinoIncorrectoException extends DominioException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DestinoIncorrectoException(final String mensaje) {
        super(mensaje);
    }
}
