package com.mauricio.porciones.aplicacion.servicios;

import com.mauricio.porciones.aplicacion.dto.ProcesarPeticionUdpCommand;
import com.mauricio.porciones.aplicacion.mapper.EstimacionMapper;
import com.mauricio.porciones.aplicacion.mapper.PeticionMapper;
import com.mauricio.porciones.aplicacion.puertos.entrada.ProcesarPeticionUdpInputPort;
import com.mauricio.porciones.dominio.excepciones.DominioException;
import com.mauricio.porciones.dominio.modelos.Estimacion;
import com.mauricio.porciones.dominio.modelos.EventoServidor;
import com.mauricio.porciones.dominio.modelos.RespuestaCliente;
import com.mauricio.porciones.dominio.modelos.Resultado;
import com.mauricio.porciones.dominio.puertos.salida.PuertoNotificacionEvento;
import com.mauricio.porciones.dominio.puertos.salida.PuertoSalidaRed;
import com.mauricio.porciones.dominio.vo.Destinatario;
import java.util.Locale;
import java.util.Objects;

/**
 * Servicio de aplicación que procesa las peticiones del protocolo UDP de porciones.
 * Responsabilidad única: interpretar el comando recibido, coordinar con el dominio y despachar
 * la respuesta a través de los puertos de salida.
 *
 * <p>Comandos admitidos: {@code CONECTAR}, {@code DESCONECTAR} y
 * {@code CALCULAR;personas;porcionesPorPersona}.</p>
 */
public final class ProcesarPeticionUdpService implements ProcesarPeticionUdpInputPort {

    private final PuertoSalidaRed puertoSalidaRed;
    private final PuertoNotificacionEvento puertoNotificacion;
    private final PeticionMapper peticionMapper;
    private final EstimacionMapper estimacionMapper;

    public ProcesarPeticionUdpService(
            final PuertoSalidaRed puertoSalidaRed,
            final PuertoNotificacionEvento puertoNotificacion,
            final PeticionMapper peticionMapper,
            final EstimacionMapper estimacionMapper) {
        this.puertoSalidaRed = Objects.requireNonNull(puertoSalidaRed, "El puerto de salida es obligatorio.");
        this.puertoNotificacion =
                Objects.requireNonNull(puertoNotificacion, "El puerto de notificación es obligatorio.");
        this.peticionMapper = Objects.requireNonNull(peticionMapper, "El mapper de petición es obligatorio.");
        this.estimacionMapper =
                Objects.requireNonNull(estimacionMapper, "El mapper de estimación es obligatorio.");
    }

    @Override
    public void procesar(final ProcesarPeticionUdpCommand comando) {
        Objects.requireNonNull(comando, "El comando es obligatorio.");
        final Destinatario destinatario = peticionMapper.toDestinatario(comando);
        final String mensaje = comando.mensaje();

        if (Objects.isNull(mensaje) || mensaje.isBlank()) {
            puertoSalidaRed.enviarRespuesta(RespuestaCliente.error(destinatario, "Mensaje vacío recibido."));
            notificarEvento(destinatario.endpoint(), "datos recibidos --> [Vacío]");
            return;
        }

        final String comandoTexto = mensaje.trim();

        if (comandoTexto.equalsIgnoreCase("CONECTAR")) {
            puertoSalidaRed.enviarRespuesta(
                    RespuestaCliente.conectado(
                            destinatario, "Servidor UDP listo para recibir cálculos de porciones"));
            notificarEvento(
                    destinatario.endpoint(), "conectado --> Solicitud de verificación recibida y aceptada");
            return;
        }

        if (comandoTexto.equalsIgnoreCase("DESCONECTAR")) {
            puertoSalidaRed.enviarRespuesta(RespuestaCliente.desconectado(destinatario, "Sesión finalizada"));
            notificarEvento(destinatario.endpoint(), "desconectado --> Cliente ha cerrado la sesión");
            return;
        }

        if (comandoTexto.toUpperCase(Locale.ROOT).startsWith("CALCULAR;")) {
            procesarCalculo(destinatario, comandoTexto);
            return;
        }

        puertoSalidaRed.enviarRespuesta(
                RespuestaCliente.error(destinatario, "Comando no reconocido por el servidor UDP."));
        notificarEvento(
                destinatario.endpoint(), "datos recibidos --> Comando desconocido: [" + comandoTexto + "]");
    }

    private void procesarCalculo(final Destinatario destinatario, final String comandoTexto) {
        final String[] partes = comandoTexto.split(";", -1);
        if (partes.length != 3) {
            puertoSalidaRed.enviarRespuesta(
                    RespuestaCliente.error(
                            destinatario, "Formato inválido. Se esperaba CALCULAR;personas;porcionesPorPersona"));
            notificarEvento(destinatario.endpoint(), "datos recibidos --> Formato incorrecto: " + comandoTexto);
            return;
        }

        try {
            final int asistentes = Integer.parseInt(partes[1].trim());
            final int porcionesPorPersona = Integer.parseInt(partes[2].trim());

            final Estimacion estimacion = estimacionMapper.toDomain(asistentes, porcionesPorPersona);
            final Resultado resultado = estimacion.calcular();

            puertoSalidaRed.enviarRespuesta(RespuestaCliente.calculoExitoso(destinatario, resultado));

            final String logInfo = String.format(
                    Locale.ROOT,
                    "datos recibidos (Personas: %d, Porciones por persona: %d) --> "
                            + "datos enviados (Total: %s, Con reserva: %s, %s)",
                    estimacion.getAsistentes().valor(),
                    estimacion.getPorcionesPorPersona().valor(),
                    resultado.getTotalFormateado(),
                    resultado.getTotalConReservaFormateado(),
                    resultado.getClasificacion().getDescripcion());
            notificarEvento(destinatario.endpoint(), logInfo);

        } catch (final NumberFormatException excepcion) {
            puertoSalidaRed.enviarRespuesta(
                    RespuestaCliente.error(
                            destinatario,
                            "La cantidad de personas y las porciones por persona deben ser números enteros."));
            notificarEvento(
                    destinatario.endpoint(), "datos recibidos --> Error de formato numérico: " + comandoTexto);
        } catch (final DominioException excepcion) {
            puertoSalidaRed.enviarRespuesta(RespuestaCliente.error(destinatario, excepcion.getMessage()));
            notificarEvento(
                    destinatario.endpoint(), "datos recibidos --> Validación fallida: " + excepcion.getMessage());
        }
    }

    private void notificarEvento(final String endpoint, final String descripcion) {
        puertoNotificacion.notificarEvento(new EventoServidor("EVENTO", endpoint, descripcion));
    }
}
