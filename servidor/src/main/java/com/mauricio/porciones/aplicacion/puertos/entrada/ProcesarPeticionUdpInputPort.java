package com.mauricio.porciones.aplicacion.puertos.entrada;

import com.mauricio.porciones.aplicacion.dto.ProcesarPeticionUdpCommand;

/** Puerto de entrada para procesar las peticiones recibidas por el receptor UDP. */
public interface ProcesarPeticionUdpInputPort {

    void procesar(ProcesarPeticionUdpCommand comando);
}
