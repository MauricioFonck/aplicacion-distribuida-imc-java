package com.mauricio.porciones.servidor;

import com.mauricio.porciones.remoto.PorcionesRemote;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Consumer;

/** Ciclo de vida del registro y del servicio RMI. */
public final class RmiServer implements AutoCloseable {
    public static final int DEFAULT_REGISTRY_PORT = 1099;
    public static final String DEFAULT_SERVICE_NAME = "PorcionesService";
    private final int port;
    private final String serviceName;
    private final Consumer<String> logger;
    private Registry registry;
    private PorcionesRemoteImpl service;

    public RmiServer(int port, String serviceName, Consumer<String> logger) {
        if (port < 1 || port > 65535) throw new IllegalArgumentException("El puerto debe estar entre 1 y 65535.");
        if (serviceName == null || serviceName.trim().isEmpty()) throw new IllegalArgumentException("El nombre del servicio es obligatorio.");
        this.port = port;
        this.serviceName = serviceName.trim();
        this.logger = logger == null ? message -> { } : logger;
    }
    public synchronized void start() throws RemoteException {
        if (service != null) return;
        registry = LocateRegistry.createRegistry(port);
        service = new PorcionesRemoteImpl(logger);
        try {
            registry.bind(serviceName, service);
        } catch (AlreadyBoundException ex) {
            throw new RemoteException("El nombre del servicio ya está registrado: " + serviceName, ex);
        }
        logger.accept("Registro RMI activo en el puerto " + port + ", servicio " + serviceName);
    }
    public synchronized void stop() {
        if (registry == null) return;
        try { registry.unbind(serviceName); } catch (Exception ignored) { }
        try {
            if (service != null) UnicastRemoteObject.unexportObject(service, true);
            UnicastRemoteObject.unexportObject(registry, true);
        } catch (Exception ignored) {
            // El cierre debe ser idempotente aunque otro hilo ya haya liberado RMI.
        }
        service = null;
        registry = null;
        logger.accept("Servidor RMI detenido");
    }
    public boolean isRunning() { return service != null; }
    @Override public void close() { stop(); }
}
