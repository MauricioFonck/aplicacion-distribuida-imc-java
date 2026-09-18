package com.mauricio.porciones.aplicacion.mapper;

import com.mauricio.porciones.aplicacion.dto.ProcesarPeticionUdpCommand;
import com.mauricio.porciones.aplicacion.excepciones.ComandoInvalidoException;
import com.mauricio.porciones.dominio.vo.Destinatario;
import java.util.Objects;

/**
 * Mapper de la capa de aplicación. Responsabilidad única: convertir los datos de una petición
 * de red a objetos de valor del dominio.
 */
public final class PeticionMapper {

    public Destinatario toDestinatario(final ProcesarPeticionUdpCommand comando) {
        if (Objects.isNull(comando)) {
            throw new ComandoInvalidoException("El comando de petición no puede ser nulo.");
        }
        return new Destinatario(comando.ipCliente(), comando.puertoCliente());
    }
}
