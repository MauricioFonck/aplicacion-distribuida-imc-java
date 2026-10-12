package com.mauricio.porciones.servidor;

import com.mauricio.porciones.dominio.Estimacion;
import com.mauricio.porciones.remoto.PorcionesRemote;
import com.mauricio.porciones.remoto.PorcionesResponse;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** Implementación remota; cada llamada se ejecuta en un hilo RMI. */
public final class PorcionesRemoteImpl extends UnicastRemoteObject implements PorcionesRemote {
    private final Set<String> clientes = ConcurrentHashMap.newKeySet();
    private final Consumer<String> logger;

    public PorcionesRemoteImpl(Consumer<String> logger) throws RemoteException {
        super();
        this.logger = logger == null ? message -> { } : logger;
    }
    @Override
    public String conectar(String cliente) {
        String nombre = normalizar(cliente);
        clientes.add(nombre);
        logger.accept("Cliente conectado: " + nombre);
        return "Conexión RMI establecida con el servidor.";
    }
    @Override
    public PorcionesResponse calcular(int personas, int porcionesPorPersona) {
        PorcionesResponse respuesta = Estimacion.calcular(personas, porcionesPorPersona);
        logger.accept("Solicitud RMI: personas=" + personas + ", porciones="
                + porcionesPorPersona + ", válida=" + respuesta.isValid());
        return respuesta;
    }
    @Override
    public String desconectar(String cliente) {
        String nombre = normalizar(cliente);
        clientes.remove(nombre);
        logger.accept("Cliente desconectado: " + nombre);
        return "Cliente desconectado del servidor.";
    }
    public int clientesConectados() { return clientes.size(); }
    private static String normalizar(String cliente) {
        return cliente == null || cliente.trim().isEmpty() ? "cliente-sin-nombre" : cliente.trim();
    }
}
