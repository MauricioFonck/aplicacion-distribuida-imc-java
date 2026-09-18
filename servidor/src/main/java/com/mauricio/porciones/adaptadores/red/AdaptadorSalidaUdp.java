package com.mauricio.porciones.adaptadores.red;

import com.mauricio.porciones.adaptadores.red.mapper.UdpNetworkMapper;
import com.mauricio.porciones.adaptadores.red.response.UdpResponse;
import com.mauricio.porciones.dominio.modelos.EventoServidor;
import com.mauricio.porciones.dominio.modelos.RespuestaCliente;
import com.mauricio.porciones.dominio.puertos.salida.PuertoNotificacionEvento;
import com.mauricio.porciones.dominio.puertos.salida.PuertoSalidaRed;
import java.io.IOException;
import java.util.Objects;

/**
 * Adaptador de salida que implementa el puerto {@link PuertoSalidaRed}. Responsabilidad única:
 * transformar objetos del dominio en datagramas UDP y transmitirlos al cliente por el canal.
 */
public final class AdaptadorSalidaUdp implements PuertoSalidaRed {

    private final CanalUdp canalUdp;
    private final UdpNetworkMapper mapper;
    private final PuertoNotificacionEvento notificador;

    public AdaptadorSalidaUdp(
            final CanalUdp canalUdp,
            final UdpNetworkMapper mapper,
            final PuertoNotificacionEvento notificador) {
        this.canalUdp = Objects.requireNonNull(canalUdp, "El canal UDP es obligatorio.");
        this.mapper = Objects.requireNonNull(mapper, "El mapper UDP es obligatorio.");
        this.notificador = Objects.requireNonNull(notificador, "El notificador es obligatorio.");
    }

    @Override
    public void enviarRespuesta(final RespuestaCliente respuesta) {
        Objects.requireNonNull(respuesta, "La respuesta es obligatoria.");
        try {
            final UdpResponse udpResponse = mapper.toNetworkResponse(respuesta);
            canalUdp.enviar(udpResponse);
        } catch (final IOException excepcion) {
            notificador.notificarEvento(
                    new EventoServidor(
                            "ERROR",
                            respuesta.getDestinatario().endpoint(),
                            "Fallo al enviar datagrama UDP: " + excepcion.getMessage()));
        }
    }
}
