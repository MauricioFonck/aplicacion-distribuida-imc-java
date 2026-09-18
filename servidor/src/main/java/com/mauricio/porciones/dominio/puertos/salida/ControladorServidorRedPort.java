package com.mauricio.porciones.dominio.puertos.salida;

import com.mauricio.porciones.aplicacion.excepciones.ServidorRedException;
import com.mauricio.porciones.dominio.vo.PuertoRed;

/**
 * Puerto de salida para el control de la infraestructura de red del servidor. Recibe
 * exclusivamente objetos del dominio.
 */
public interface ControladorServidorRedPort {

    void iniciar(PuertoRed puerto) throws ServidorRedException;

    void detener();

    boolean isActivo();

    PuertoRed getPuertoActual();
}
