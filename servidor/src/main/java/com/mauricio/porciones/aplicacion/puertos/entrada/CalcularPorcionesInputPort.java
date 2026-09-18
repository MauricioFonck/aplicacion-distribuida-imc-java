package com.mauricio.porciones.aplicacion.puertos.entrada;

import com.mauricio.porciones.aplicacion.dto.CalcularPorcionesCommand;
import com.mauricio.porciones.aplicacion.dto.ResultadoPorcionesDto;

/** Puerto de entrada (caso de uso) para el cálculo de porciones. */
public interface CalcularPorcionesInputPort {

    ResultadoPorcionesDto calcular(CalcularPorcionesCommand comando);
}
