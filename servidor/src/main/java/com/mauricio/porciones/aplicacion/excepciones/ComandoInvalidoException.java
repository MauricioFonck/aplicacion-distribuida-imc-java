package com.mauricio.porciones.aplicacion.excepciones;

import java.io.Serial;

/** Excepción lanzada cuando un caso de uso recibe un comando inválido. */
public final class ComandoInvalidoException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ComandoInvalidoException(final String mensaje) {
        super(mensaje);
    }
}
