package com.mauricio.porciones.aplicacion.dto;

/** DTO con el resultado del cálculo de porciones para la capa de aplicación. */
public record ResultadoPorcionesDto(
        long total, long totalConReserva, String clasificacion, String mensaje) {
}
