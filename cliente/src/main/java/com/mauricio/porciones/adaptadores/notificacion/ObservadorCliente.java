package com.mauricio.porciones.adaptadores.notificacion;

import com.mauricio.porciones.dominio.enums.EstadoConexion;
import com.mauricio.porciones.dominio.modelos.EventoCliente;
import com.mauricio.porciones.dominio.modelos.ResultadoPorciones;

/** Observador para los componentes interesados en lo que ocurre en el núcleo del cliente. */
public interface ObservadorCliente {

    void onEstado(EstadoConexion estado, String endpoint);

    void onEvento(EventoCliente evento);

    void onResultado(ResultadoPorciones resultado);

    void onError(String mensaje);
}
