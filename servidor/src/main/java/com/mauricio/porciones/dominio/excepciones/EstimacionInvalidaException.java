package com.mauricio.porciones.dominio.excepciones;

import java.io.Serial;

/** Excepción lanzada cuando no puede construirse una estimación de porciones válida. */
public final class EstimacionInvalidaException extends DominioException {

    @Serial
    private static final long serialVersionUID = 1L;

    public EstimacionInvalidaException(final String mensaje) {
        super(mensaje);
    }
}
