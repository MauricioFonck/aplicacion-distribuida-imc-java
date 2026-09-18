package com.mauricio.porciones.aplicacion.puertos.entrada;

import com.mauricio.porciones.aplicacion.dto.ConectarCommand;
import java.util.concurrent.CompletableFuture;

/** Puerto de entrada (caso de uso) para abrir y cerrar la sesión con el servidor. */
public interface GestionarConexionInputPort {

    CompletableFuture<Void> conectar(ConectarCommand comando);

    CompletableFuture<Void> desconectar();

    boolean estaConectado();
}
