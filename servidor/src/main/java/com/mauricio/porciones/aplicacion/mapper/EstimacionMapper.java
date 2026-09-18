package com.mauricio.porciones.aplicacion.mapper;

import com.mauricio.porciones.aplicacion.dto.CalcularPorcionesCommand;
import com.mauricio.porciones.aplicacion.dto.ResultadoPorcionesDto;
import com.mauricio.porciones.aplicacion.excepciones.ComandoInvalidoException;
import com.mauricio.porciones.dominio.modelos.Estimacion;
import com.mauricio.porciones.dominio.modelos.Resultado;
import com.mauricio.porciones.dominio.vo.Asistentes;
import com.mauricio.porciones.dominio.vo.PorcionesPorPersona;
import java.util.Objects;

/**
 * Mapper de la capa de aplicación. Responsabilidad única: convertir los comandos y DTO de
 * cálculo a objetos del dominio y viceversa.
 */
public final class EstimacionMapper {

    public Estimacion toDomain(final CalcularPorcionesCommand comando) {
        if (Objects.isNull(comando)) {
            throw new ComandoInvalidoException("El comando de cálculo no puede ser nulo.");
        }
        return toDomain(comando.asistentes(), comando.porcionesPorPersona());
    }

    public Estimacion toDomain(final int asistentes, final int porcionesPorPersona) {
        return new Estimacion(new Asistentes(asistentes), new PorcionesPorPersona(porcionesPorPersona));
    }

    public ResultadoPorcionesDto toDto(final Resultado resultado) {
        if (Objects.isNull(resultado)) {
            throw new ComandoInvalidoException("El resultado del cálculo no puede ser nulo.");
        }
        return new ResultadoPorcionesDto(
                resultado.getTotal(),
                resultado.getTotalConReserva(),
                resultado.getClasificacion().getDescripcion(),
                resultado.getMensaje());
    }
}
