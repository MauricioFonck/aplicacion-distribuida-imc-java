package com.mauricio.porciones.dominio.excepciones;

import java.io.Serial;

/** Excepción lanzada cuando el destinatario de red de una respuesta no es válido. */
public final class DestinatarioIncorrectoException extends DominioException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DestinatarioIncorrectoException(final String mensaje) {
        super(mensaje);
    }
}
