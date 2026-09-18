package com.mauricio.porciones.aplicacion.dto;

/** Comando DTO con el host y el puerto del servidor al que se quiere conectar el cliente. */
public record ConectarCommand(String host, int puerto) {
}
