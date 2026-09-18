package com.mauricio.porciones.dominio.vo;

import com.mauricio.porciones.dominio.excepciones.PuertoIncorrectoException;

/** Value Object inmutable que representa y valida el puerto UDP en el que escucha el servidor. */
public record PuertoRed(int valor) {

    /** Puerto de servicio utilizado por defecto. */
    public static final int POR_DEFECTO = 9008;

    public PuertoRed {
        if (valor < 1024 || valor > 65535) {
            throw new PuertoIncorrectoException("El puerto debe ser un número entre 1024 y 65535.");
        }
    }
}
