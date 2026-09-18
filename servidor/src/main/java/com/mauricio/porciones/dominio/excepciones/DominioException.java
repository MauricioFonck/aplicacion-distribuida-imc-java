package com.mauricio.porciones.dominio.excepciones;

import java.io.Serial;

/** Excepción base para las violaciones de las reglas del dominio. */
public class DominioException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DominioException(final String mensaje) {
        super(mensaje);
    }
}
