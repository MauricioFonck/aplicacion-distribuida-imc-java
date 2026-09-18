package com.mauricio.porciones;

import static com.mauricio.porciones.Verificador.assertEquals;
import static com.mauricio.porciones.Verificador.assertThrows;
import static com.mauricio.porciones.Verificador.assertTrue;

import com.mauricio.porciones.adaptadores.red.AdaptadorNotificacionEvento;
import com.mauricio.porciones.adaptadores.red.ObservadorServidor;
import com.mauricio.porciones.adaptadores.red.mapper.UdpNetworkMapper;
import com.mauricio.porciones.adaptadores.red.response.UdpResponse;
import com.mauricio.porciones.adaptadores.red.util.RedUtil;
import com.mauricio.porciones.dominio.enums.EstadoServidor;
import com.mauricio.porciones.dominio.excepciones.RespuestaInvalidaException;
import com.mauricio.porciones.dominio.modelos.Estimacion;
import com.mauricio.porciones.dominio.modelos.EventoServidor;
import com.mauricio.porciones.dominio.modelos.RespuestaCliente;
import com.mauricio.porciones.dominio.vo.Asistentes;
import com.mauricio.porciones.dominio.vo.Destinatario;
import com.mauricio.porciones.dominio.vo.PorcionesPorPersona;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Pruebas de los adaptadores que no necesitan red: serialización del protocolo y notificaciones. */
public final class AdaptadoresTest {

    private static final Destinatario DESTINATARIO = new Destinatario("192.168.1.20", 50123);

    private AdaptadoresTest() {
    }

    public static void main(final String[] args) {
        debeSerializarElCalculo();
        debeSerializarLasRespuestasDeSesion();
        debeSerializarLosErrores();
        debeRechazarUnaRespuestaNula();
        debeRepartirLasNotificaciones();
        debeResolverLaIpLocal();
        System.out.println("Todas las pruebas de AdaptadoresTest pasaron.");
    }

    private static void debeSerializarElCalculo() {
        final Estimacion estimacion = new Estimacion(new Asistentes(40), new PorcionesPorPersona(3));
        final UdpResponse respuesta = new UdpNetworkMapper()
                .toNetworkResponse(RespuestaCliente.calculoExitoso(DESTINATARIO, estimacion.calcular()));

        final String[] partes = respuesta.getPayload().split(";", -1);
        assertEquals(5, partes.length, "La respuesta de cálculo debe tener cinco campos.");
        assertEquals("OK_CALCULO", partes[0], "El primer campo es el tipo de respuesta.");
        assertEquals("120", partes[1], "El total viaja sin separadores de miles.");
        assertEquals("132", partes[2], "El total con reserva no coincide.");
        assertEquals("Reunión mediana", partes[3], "La clasificación no coincide.");
        assertEquals("192.168.1.20", respuesta.getIpDestino(), "La IP de destino no coincide.");
        assertEquals(50123, respuesta.getPuertoDestino(), "El puerto de destino no coincide.");
        assertEquals(respuesta.getPayload(), new String(respuesta.getBytes(), StandardCharsets.UTF_8),
                "Los bytes del datagrama deben estar en UTF-8.");
    }

    private static void debeSerializarLasRespuestasDeSesion() {
        final UdpNetworkMapper mapper = new UdpNetworkMapper();
        assertEquals("CONECTADO_OK;listo",
                mapper.toNetworkResponse(RespuestaCliente.conectado(DESTINATARIO, "listo")).getPayload(),
                "La confirmación de conexión no coincide.");
        assertEquals("DESCONECTADO_OK;adiós",
                mapper.toNetworkResponse(RespuestaCliente.desconectado(DESTINATARIO, "adiós")).getPayload(),
                "La confirmación de desconexión no coincide.");
    }

    private static void debeSerializarLosErrores() {
        final UdpNetworkMapper mapper = new UdpNetworkMapper();
        assertEquals("ERROR;motivo",
                mapper.toNetworkResponse(RespuestaCliente.error(DESTINATARIO, "motivo")).getPayload(),
                "El error no coincide.");
        assertEquals("ERROR;Error desconocido.",
                mapper.toNetworkResponse(RespuestaCliente.error(DESTINATARIO, null)).getPayload(),
                "Un error sin mensaje debería llevar un texto por defecto.");
    }

    private static void debeRechazarUnaRespuestaNula() {
        assertThrows(RespuestaInvalidaException.class, () -> new UdpNetworkMapper().toNetworkResponse(null),
                "Una respuesta nula no se puede serializar.");
    }

    private static void debeRepartirLasNotificaciones() {
        final AdaptadorNotificacionEvento adaptador = new AdaptadorNotificacionEvento();
        final List<String> recibidos = new ArrayList<>();
        final ObservadorServidor observador = new ObservadorServidor() {
            @Override
            public void onEvento(final EventoServidor evento) {
                recibidos.add(evento.getCategoria() + ":" + evento.getDescripcion());
            }

            @Override
            public void onCambioEstado(final EstadoServidor nuevoEstado, final int puerto) {
                recibidos.add(nuevoEstado + ":" + puerto);
            }
        };

        adaptador.registrarObservador(observador);
        adaptador.registrarObservador(observador);
        adaptador.notificarEvento(new EventoServidor("EVENTO", "LOCAL", "hola"));
        adaptador.notificarCambioEstado(EstadoServidor.EN_LINEA, 9008);
        assertEquals(List.of("EVENTO:hola", "EN_LINEA:9008"), recibidos,
                "El observador registrado dos veces debe recibir cada aviso una sola vez.");

        adaptador.removerObservador(observador);
        adaptador.notificarCambioEstado(EstadoServidor.DETENIDO, 0);
        assertEquals(2, recibidos.size(), "Un observador removido no debe recibir más avisos.");
    }

    private static void debeResolverLaIpLocal() {
        final String ip = RedUtil.obtenerIpLocal();
        assertTrue(ip != null && !ip.isBlank(), "Siempre debe resolverse alguna dirección local.");
    }
}
