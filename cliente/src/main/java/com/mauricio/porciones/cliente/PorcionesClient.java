package com.mauricio.porciones.cliente;

import com.mauricio.porciones.remoto.PorcionesRemote;
import com.mauricio.porciones.remoto.PorcionesResponse;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/** Adaptador cliente que convierte el registro RMI en operaciones del ejercicio. */
public final class PorcionesClient implements AutoCloseable {
    private PorcionesRemote remote;
    private String clientName;
    public synchronized void connect(String host, int registryPort, String serviceName, String name) throws Exception {
        if (host == null || host.trim().isEmpty()) throw new IllegalArgumentException("El host es obligatorio.");
        if (registryPort < 1 || registryPort > 65535) throw new IllegalArgumentException("El puerto debe estar entre 1 y 65535.");
        if (serviceName == null || serviceName.trim().isEmpty()) throw new IllegalArgumentException("El servicio es obligatorio.");
        close();
        Registry registry = LocateRegistry.getRegistry(host.trim(), registryPort);
        remote = (PorcionesRemote) registry.lookup(serviceName.trim());
        clientName = name == null || name.trim().isEmpty() ? "cliente-swing" : name.trim();
        remote.conectar(clientName);
    }
    public synchronized PorcionesResponse calcular(int personas, int porcionesPorPersona) throws Exception {
        ensureConnected();
        return remote.calcular(personas, porcionesPorPersona);
    }
    public synchronized boolean isConnected() { return remote != null; }
    @Override public synchronized void close() {
        if (remote != null) {
            try { remote.desconectar(clientName); } catch (Exception ignored) { }
        }
        remote = null;
        clientName = null;
    }
    private void ensureConnected() { if (!isConnected()) throw new IllegalStateException("El cliente no está conectado."); }
}
