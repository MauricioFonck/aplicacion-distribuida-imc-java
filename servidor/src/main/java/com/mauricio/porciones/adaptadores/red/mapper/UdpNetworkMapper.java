package com.mauricio.porciones.adaptadores.red.mapper;

import com.mauricio.porciones.adaptadores.red.response.UdpResponse;
import com.mauricio.porciones.dominio.excepciones.RespuestaInvalidaException;
import com.mauricio.porciones.dominio.modelos.RespuestaCliente;
import com.mauricio.porciones.dominio.modelos.Resultado;
import java.util.Objects;

/**
 * Mapper del adaptador de red. Responsabilidad única: convertir una respuesta del dominio
 * ({@link RespuestaCliente}) al texto del protocolo UDP ({@link UdpResponse}).
 *
 * <p>Formato de la respuesta de cálculo:
 * {@code OK_CALCULO;total;totalConReserva;clasificacion;mensaje}.</p>
 */
public final class UdpNetworkMapper {

    public UdpResponse toNetworkResponse(final RespuestaCliente respuesta) {
        if (Objects.isNull(respuesta)) {
            throw new RespuestaInvalidaException("No se puede serializar una respuesta nula.");
        }

        final String payload = switch (respuesta.getTipo()) {
            case CONECTADO -> "CONECTADO_OK;" + respuesta.getMensaje();
            case DESCONECTADO -> "DESCONECTADO_OK;" + respuesta.getMensaje();
            case OK_CALCULO -> buildPayloadCalculo(respuesta.getResultado());
            case ERROR -> "ERROR;"
                    + (Objects.nonNull(respuesta.getMensaje()) ? respuesta.getMensaje() : "Error desconocido.");
        };

        return new UdpResponse(
                payload, respuesta.getDestinatario().ip(), respuesta.getDestinatario().puerto());
    }

    private static String buildPayloadCalculo(final Resultado resultado) {
        if (Objects.isNull(resultado)) {
            throw new RespuestaInvalidaException("No se puede serializar un cálculo sin resultado.");
        }
        return "OK_CALCULO;"
                + resultado.getTotal() + ";"
                + resultado.getTotalConReserva() + ";"
                + resultado.getClasificacion().getDescripcion() + ";"
                + resultado.getMensaje();
    }
}
