package com.mauricio.porciones.dominio.modelos;

import com.mauricio.porciones.dominio.excepciones.RespuestaServidorException;
import java.util.Objects;

/** Resultado del cálculo de porciones tal como lo devuelve el servidor. */
public record ResultadoPorciones(long total, long totalConReserva, String clasificacion, String mensaje) {

    public ResultadoPorciones {
        if (total <= 0L || totalConReserva < total
                || Objects.isNull(clasificacion) || clasificacion.isBlank()
                || Objects.isNull(mensaje)) {
            throw new RespuestaServidorException("El resultado recibido está incompleto o es inválido.");
        }
    }
}
