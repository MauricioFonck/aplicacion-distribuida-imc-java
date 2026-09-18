package com.mauricio.porciones.dominio.puertos.salida;

import com.mauricio.porciones.aplicacion.excepciones.ClienteRedException;
import com.mauricio.porciones.dominio.modelos.DatosReunion;
import com.mauricio.porciones.dominio.modelos.ResultadoPorciones;
import com.mauricio.porciones.dominio.vo.DestinoServidor;

/**
 * Puerto de salida para la comunicación con el servidor. El núcleo del cliente solo conoce esta
 * interfaz; el detalle de los datagramas UDP queda en el adaptador que la implementa.
 */
public interface ClienteUdpPort {

    void conectar(DestinoServidor destino) throws ClienteRedException;

    void desconectar() throws ClienteRedException;

    ResultadoPorciones solicitarCalculo(DatosReunion datos) throws ClienteRedException;

    boolean estaConectado();
}
