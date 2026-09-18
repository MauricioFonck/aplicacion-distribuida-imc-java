package com.mauricio.porciones.aplicacion.dto;

/** Comando DTO para solicitar el cálculo de porciones con valores primitivos. */
public record CalcularPorcionesCommand(int asistentes, int porcionesPorPersona) {
}
