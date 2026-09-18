package com.mauricio.porciones.adaptadores.red;

import com.mauricio.porciones.dominio.enums.EstadoServidor;
import com.mauricio.porciones.dominio.modelos.EventoServidor;

/** Observador para los componentes interesados en los eventos y estados del servidor. */
public interface ObservadorServidor {

    void onEvento(EventoServidor evento);

    void onCambioEstado(EstadoServidor nuevoEstado, int puerto);
}
