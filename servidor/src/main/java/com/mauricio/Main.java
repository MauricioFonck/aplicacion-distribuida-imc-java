package com.mauricio;

import com.mauricio.porciones.adaptadores.red.AdaptadorControlServidorRed;
import com.mauricio.porciones.adaptadores.red.AdaptadorNotificacionEvento;
import com.mauricio.porciones.adaptadores.red.AdaptadorSalidaUdp;
import com.mauricio.porciones.adaptadores.red.CanalUdp;
import com.mauricio.porciones.adaptadores.red.mapper.UdpNetworkMapper;
import com.mauricio.porciones.aplicacion.mapper.EstimacionMapper;
import com.mauricio.porciones.aplicacion.mapper.PeticionMapper;
import com.mauricio.porciones.aplicacion.puertos.entrada.GestionarServidorInputPort;
import com.mauricio.porciones.aplicacion.puertos.entrada.ProcesarPeticionUdpInputPort;
import com.mauricio.porciones.aplicacion.servicios.GestionarServidorService;
import com.mauricio.porciones.aplicacion.servicios.ProcesarPeticionUdpService;
import com.mauricio.porciones.dominio.puertos.salida.ControladorServidorRedPort;
import com.mauricio.porciones.dominio.puertos.salida.PuertoSalidaRed;
import com.mauricio.porciones.entrypoint.gui.ServidorFrame;
import com.mauricio.porciones.entrypoint.udp.ReceptorPeticionesUdp;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada del servidor (composition root). Responsabilidad única: ensamblar las
 * dependencias de la arquitectura hexagonal y lanzar la interfaz gráfica.
 *
 * <p>Es el único lugar donde se conocen las clases concretas. El núcleo (dominio y aplicación)
 * solo ve interfaces: los puertos.</p>
 */
public final class Main {

    private Main() {
        // Clase de arranque.
    }

    public static void main(final String[] args) {
        // 1. Infraestructura de red compartida (adaptadores de salida)
        final CanalUdp canalUdp = new CanalUdp();
        final AdaptadorNotificacionEvento notificador = new AdaptadorNotificacionEvento();
        final UdpNetworkMapper redMapper = new UdpNetworkMapper();
        final PuertoSalidaRed puertoSalidaRed = new AdaptadorSalidaUdp(canalUdp, redMapper, notificador);

        // 2. Mappers de aplicación
        final PeticionMapper peticionMapper = new PeticionMapper();
        final EstimacionMapper estimacionMapper = new EstimacionMapper();

        // 3. Casos de uso (servicios de aplicación)
        final ProcesarPeticionUdpInputPort procesarPeticionPort = new ProcesarPeticionUdpService(
                puertoSalidaRed, notificador, peticionMapper, estimacionMapper);

        // 4. Entrypoints
        final ReceptorPeticionesUdp receptorUdp =
                new ReceptorPeticionesUdp(canalUdp, procesarPeticionPort, notificador);
        final ControladorServidorRedPort controladorRed =
                new AdaptadorControlServidorRed(canalUdp, receptorUdp::iniciar, receptorUdp::detener);
        final GestionarServidorInputPort gestionarServidorPort =
                new GestionarServidorService(controladorRed, notificador);

        // 5. Interfaz gráfica en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            final ServidorFrame frame = new ServidorFrame(gestionarServidorPort);
            notificador.registrarObservador(frame);
            frame.setVisible(true);
        });
    }
}
