package com.mauricio.porciones.dominio.vo;

import com.mauricio.porciones.dominio.excepciones.DestinatarioIncorrectoException;
import com.mauricio.porciones.dominio.excepciones.PuertoIncorrectoException;
import java.util.Objects;

/**
 * Value Object que representa la dirección de red de un cliente UDP.
 *
 * <p>En UDP no existe una conexión establecida, por eso cada respuesta debe indicar de forma
 * explícita a qué IP y puerto se dirige. Esos datos se toman del datagrama recibido.</p>
 */
public record Destinatario(String ip, int puerto) {

    public Destinatario {
        if (Objects.isNull(ip) || ip.isBlank()) {
            throw new DestinatarioIncorrectoException(
                    "La dirección IP del destinatario no puede ser nula ni vacía.");
        }
        if (puerto <= 0 || puerto > 65535) {
            throw new PuertoIncorrectoException(
                    "El puerto del destinatario debe ser un número entre 1 y 65535.");
        }
        ip = ip.trim();
    }

    public String endpoint() {
        return ip + ":" + puerto;
    }
}
