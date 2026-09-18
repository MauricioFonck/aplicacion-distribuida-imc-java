package com.mauricio.porciones.dominio.puertos.salida;

import com.mauricio.porciones.dominio.enums.EstadoConexion;
import com.mauricio.porciones.dominio.modelos.EventoCliente;
import com.mauricio.porciones.dominio.modelos.ResultadoPorciones;

/** Puerto de salida para notificar a los observadores (la interfaz gráfica) lo que ocurre en el núcleo. */
public interface PuertoNotificacionCliente {

    void notificarEstado(EstadoConexion estado, String endpoint);

    void notificarEvento(EventoCliente evento);

    void notificarResultado(ResultadoPorciones resultado);

    void notificarError(String mensaje);
}
