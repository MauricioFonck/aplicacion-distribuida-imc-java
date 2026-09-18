package com.mauricio.porciones.aplicacion.dto;

/** Comando DTO para procesar una petición entrante desde la red UDP. */
public record ProcesarPeticionUdpCommand(String ipCliente, int puertoCliente, String mensaje) {
}
