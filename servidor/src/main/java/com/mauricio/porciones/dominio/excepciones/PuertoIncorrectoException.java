package com.mauricio.porciones.dominio.excepciones;

import java.io.Serial;

/** Excepción lanzada cuando un puerto no pertenece al rango permitido. */
public final class PuertoIncorrectoException extends DominioException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PuertoIncorrectoException(final String mensaje) {
        super(mensaje);
    }
}
