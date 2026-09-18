package com.mauricio.porciones.dominio.vo;

import com.mauricio.porciones.dominio.excepciones.DestinoIncorrectoException;
import java.util.Objects;

/** Value Object que representa el host y el puerto UDP del servidor al que se dirige el cliente. */
public record DestinoServidor(String host, int puerto) {

    /** Puerto de servicio utilizado por defecto. */
    public static final int PUERTO_POR_DEFECTO = 9008;

    public DestinoServidor {
        if (Objects.isNull(host) || host.isBlank()) {
            throw new DestinoIncorrectoException("El host del servidor es obligatorio.");
        }
        if (puerto < 1024 || puerto > 65535) {
            throw new DestinoIncorrectoException("El puerto debe estar entre 1024 y 65535.");
        }
        host = host.trim();
    }

    public String endpoint() {
        return host + ":" + puerto;
    }
}
