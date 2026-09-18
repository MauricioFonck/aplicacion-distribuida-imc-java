package com.mauricio.porciones.aplicacion.puertos.entrada;

import com.mauricio.porciones.aplicacion.dto.CalcularPorcionesCommand;
import com.mauricio.porciones.dominio.modelos.ResultadoPorciones;
import java.util.concurrent.CompletableFuture;

/** Puerto de entrada (caso de uso) para solicitar al servidor el cálculo de porciones. */
public interface CalcularPorcionesInputPort {

    CompletableFuture<ResultadoPorciones> calcular(CalcularPorcionesCommand comando);
}
