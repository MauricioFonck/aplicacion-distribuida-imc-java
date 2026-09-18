package com.mauricio.porciones.entrypoint.udp;

import com.mauricio.porciones.adaptadores.red.CanalUdp;
import com.mauricio.porciones.aplicacion.dto.ProcesarPeticionUdpCommand;
import com.mauricio.porciones.aplicacion.puertos.entrada.ProcesarPeticionUdpInputPort;
import com.mauricio.porciones.dominio.modelos.EventoServidor;
import com.mauricio.porciones.dominio.puertos.salida.PuertoNotificacionEvento;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Entrypoint primario de las peticiones UDP entrantes. Responsabilidad única: escuchar
 * datagramas en un hilo de fondo, extraer el texto y el origen de cada uno y despachar el
 * comando al puerto de entrada.
 *
 * <p>Cada datagrama es independiente: la IP y el puerto del cliente se leen del propio paquete,
 * porque UDP no mantiene una conexión de la que se puedan obtener.</p>
 */
public final class ReceptorPeticionesUdp {

    private static final int BUFFER_SIZE = 2048;

    private final CanalUdp canalUdp;
    private final ProcesarPeticionUdpInputPort inputPort;
    private final PuertoNotificacionEvento notificador;

    private Thread hiloEscucha;
    private volatile boolean escuchando = false;

    public ReceptorPeticionesUdp(
            final CanalUdp canalUdp,
            final ProcesarPeticionUdpInputPort inputPort,
            final PuertoNotificacionEvento notificador) {
        this.canalUdp = Objects.requireNonNull(canalUdp, "El canal UDP es obligatorio.");
        this.inputPort = Objects.requireNonNull(inputPort, "El puerto de entrada es obligatorio.");
        this.notificador = Objects.requireNonNull(notificador, "El notificador es obligatorio.");
    }

    public synchronized void iniciar() {
        if (escuchando) {
            return;
        }
        escuchando = true;
        hiloEscucha = new Thread(
                this::cicloEscucha, "Thread-ReceptorPeticionesUdp-" + canalUdp.getPuertoActual());
        hiloEscucha.setDaemon(true);
        hiloEscucha.start();
    }

    public synchronized void detener() {
        escuchando = false;
        if (Objects.nonNull(hiloEscucha) && hiloEscucha.isAlive()) {
            hiloEscucha.interrupt();
        }
        hiloEscucha = null;
    }

    public boolean isEscuchando() {
        return escuchando;
    }

    private void cicloEscucha() {
        final byte[] buffer = new byte[BUFFER_SIZE];
        while (escuchando && canalUdp.isAbierto()) {
            try {
                final DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                canalUdp.recibir(paquete);

                final String texto =
                        new String(paquete.getData(), 0, paquete.getLength(), StandardCharsets.UTF_8).trim();
                final ProcesarPeticionUdpCommand comando = new ProcesarPeticionUdpCommand(
                        paquete.getAddress().getHostAddress(), paquete.getPort(), texto);

                inputPort.procesar(comando);

            } catch (final SocketException excepcion) {
                if (!escuchando) {
                    break;
                }
                notificarError("Error de socket al recibir paquete: " + excepcion.getMessage());
            } catch (final IOException excepcion) {
                if (escuchando) {
                    notificarError("Error I/O al recibir datagrama: " + excepcion.getMessage());
                }
            } catch (final RuntimeException excepcion) {
                // Un datagrama mal formado no debe tumbar el hilo que atiende a los demás clientes.
                notificarError("Petición descartada: " + excepcion.getMessage());
            }
        }
    }

    private void notificarError(final String mensaje) {
        notificador.notificarEvento(new EventoServidor("ERROR", "SOCKET", mensaje));
    }
}
