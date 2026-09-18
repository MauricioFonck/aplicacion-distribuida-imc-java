package com.mauricio.porciones.aplicacion.puertos.entrada;

import com.mauricio.porciones.aplicacion.excepciones.ServidorRedException;

/**
 * Puerto de entrada (caso de uso) para el control del ciclo de vida del servidor UDP. Lo invoca
 * el entrypoint de la interfaz gráfica.
 */
public interface GestionarServidorInputPort {

    void iniciarServidor(int puerto) throws ServidorRedException;

    void detenerServidor();

    boolean estaCorriendo();

    int getPuertoActual();
}
