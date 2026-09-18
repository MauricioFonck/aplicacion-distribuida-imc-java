package com.mauricio.porciones.adaptadores.red;

import com.mauricio.porciones.aplicacion.excepciones.ServidorRedException;
import com.mauricio.porciones.dominio.puertos.salida.ControladorServidorRedPort;
import com.mauricio.porciones.dominio.vo.PuertoRed;
import java.net.SocketException;
import java.util.Objects;

/**
 * Adaptador para el control de la infraestructura del servidor UDP. Implementa el puerto de
 * salida {@link ControladorServidorRedPort}: abre o cierra el canal y arranca o detiene el
 * receptor de peticiones.
 */
public final class AdaptadorControlServidorRed implements ControladorServidorRedPort {

    private final CanalUdp canalUdp;
    private final Runnable accionIniciarListener;
    private final Runnable accionDetenerListener;

    public AdaptadorControlServidorRed(
            final CanalUdp canalUdp,
            final Runnable accionIniciarListener,
            final Runnable accionDetenerListener) {
        this.canalUdp = Objects.requireNonNull(canalUdp, "El canal UDP es obligatorio.");
        this.accionIniciarListener =
                Objects.requireNonNull(accionIniciarListener, "La acción de inicio es obligatoria.");
        this.accionDetenerListener =
                Objects.requireNonNull(accionDetenerListener, "La acción de detención es obligatoria.");
    }

    @Override
    public synchronized void iniciar(final PuertoRed puerto) throws ServidorRedException {
        Objects.requireNonNull(puerto, "El puerto es obligatorio.");
        try {
            canalUdp.abrir(puerto.valor());
            accionIniciarListener.run();
        } catch (final SocketException excepcion) {
            throw new ServidorRedException(
                    "No se pudo abrir el servidor UDP en el puerto " + puerto.valor() + ".", excepcion);
        }
    }

    @Override
    public synchronized void detener() {
        accionDetenerListener.run();
        canalUdp.cerrar();
    }

    @Override
    public boolean isActivo() {
        return canalUdp.isAbierto();
    }

    @Override
    public PuertoRed getPuertoActual() {
        if (!canalUdp.isAbierto()) {
            return null;
        }
        return new PuertoRed(canalUdp.getPuertoActual());
    }
}
