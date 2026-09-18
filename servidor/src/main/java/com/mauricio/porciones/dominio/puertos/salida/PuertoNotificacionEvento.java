package com.mauricio.porciones.dominio.puertos.salida;

import com.mauricio.porciones.dominio.enums.EstadoServidor;
import com.mauricio.porciones.dominio.modelos.EventoServidor;

/**
 * Puerto de salida para emitir los eventos y los cambios de estado del servidor hacia
 * observadores externos, como la interfaz gráfica. Recibe únicamente objetos del dominio.
 */
public interface PuertoNotificacionEvento {

    void notificarEvento(EventoServidor evento);

    void notificarCambioEstado(EstadoServidor nuevoEstado, int puerto);
}
