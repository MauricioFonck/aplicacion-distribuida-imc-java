package com.mauricio.porciones.dominio.puertos.salida;

import com.mauricio.porciones.dominio.modelos.RespuestaCliente;

/**
 * Puerto de salida para la comunicación de red con los clientes. El núcleo no sabe que la
 * respuesta viaja por UDP: solo entrega un objeto del dominio a quien implemente este puerto.
 */
public interface PuertoSalidaRed {

    void enviarRespuesta(RespuestaCliente respuesta);
}
