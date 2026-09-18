package com.mauricio.porciones.aplicacion.excepciones;

import java.io.Serial;

/** Excepción de los casos de uso del cliente ante fallos de la comunicación de red. */
public final class ClienteRedException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public ClienteRedException(final String mensaje, final Throwable causa) {
        super(mensaje, causa);
    }

    public ClienteRedException(final String mensaje) {
        super(mensaje);
    }
}
