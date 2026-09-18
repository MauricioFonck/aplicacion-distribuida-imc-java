package com.mauricio.porciones.aplicacion.servicios;

import com.mauricio.porciones.aplicacion.dto.CalcularPorcionesCommand;
import com.mauricio.porciones.aplicacion.dto.ResultadoPorcionesDto;
import com.mauricio.porciones.aplicacion.mapper.EstimacionMapper;
import com.mauricio.porciones.aplicacion.puertos.entrada.CalcularPorcionesInputPort;
import com.mauricio.porciones.dominio.modelos.Estimacion;
import com.mauricio.porciones.dominio.modelos.Resultado;
import java.util.Objects;

/**
 * Servicio de aplicación que implementa el caso de uso de calcular las porciones.
 * Responsabilidad única: convertir el comando al dominio, invocar la regla de negocio y devolver
 * el resultado como DTO.
 */
public final class CalcularPorcionesService implements CalcularPorcionesInputPort {

    private final EstimacionMapper estimacionMapper;

    public CalcularPorcionesService(final EstimacionMapper estimacionMapper) {
        this.estimacionMapper =
                Objects.requireNonNull(estimacionMapper, "El mapper de estimación es obligatorio.");
    }

    @Override
    public ResultadoPorcionesDto calcular(final CalcularPorcionesCommand comando) {
        final Estimacion estimacion = estimacionMapper.toDomain(comando);
        final Resultado resultado = estimacion.calcular();
        return estimacionMapper.toDto(resultado);
    }
}
