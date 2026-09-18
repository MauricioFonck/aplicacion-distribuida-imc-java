package com.mauricio;

import com.mauricio.porciones.adaptadores.notificacion.AdaptadorNotificacionCliente;
import com.mauricio.porciones.adaptadores.red.AdaptadorClienteUdp;
import com.mauricio.porciones.adaptadores.red.CanalUdp;
import com.mauricio.porciones.adaptadores.red.ProtocoloUdpMapper;
import com.mauricio.porciones.aplicacion.mapper.ClienteMapper;
import com.mauricio.porciones.aplicacion.servicios.ClientePorcionesService;
import com.mauricio.porciones.entrypoint.gui.ClienteFrame;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada del cliente (composition root). Responsabilidad única: ensamblar los
 * adaptadores, el servicio de aplicación y la interfaz gráfica.
 */
public final class Main {

    /** Tiempo máximo, en milisegundos, que el cliente espera cada respuesta del servidor. */
    private static final int TIMEOUT_MS = 3000;

    private Main() {
        // Clase de arranque.
    }

    public static void main(final String[] args) {
        // 1. Adaptadores de salida
        final AdaptadorNotificacionCliente notificador = new AdaptadorNotificacionCliente();
        final AdaptadorClienteUdp udp = new AdaptadorClienteUdp(new CanalUdp(TIMEOUT_MS), new ProtocoloUdpMapper());

        // 2. Hilo único para las operaciones de red, fuera del hilo de Swing
        final ExecutorService executor = Executors.newSingleThreadExecutor(tarea -> {
            final Thread hilo = new Thread(tarea, "cliente-udp");
            hilo.setDaemon(true);
            return hilo;
        });

        // 3. Casos de uso (servicio de aplicación)
        final ClientePorcionesService servicio =
                new ClientePorcionesService(udp, notificador, new ClienteMapper(), executor);

        // 4. Interfaz gráfica: recibe el servicio a través de sus dos puertos de entrada
        SwingUtilities.invokeLater(() -> {
            final ClienteFrame frame = new ClienteFrame(servicio, servicio);
            notificador.registrar(frame);
            frame.setVisible(true);
        });
    }
}
