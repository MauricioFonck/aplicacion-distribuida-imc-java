package com.mauricio.porciones.entrypoint;
import com.mauricio.porciones.servidor.RmiServer;
public final class ServerMain {
    private ServerMain() { }
    public static void main(String[] args) throws Exception {
        RmiServer server = new RmiServer(RmiServer.DEFAULT_REGISTRY_PORT, RmiServer.DEFAULT_SERVICE_NAME, System.out::println);
        server.start();
        System.out.println("Servidor listo. Presione Ctrl+C para detenerlo.");
        Thread.currentThread().join();
    }
}
