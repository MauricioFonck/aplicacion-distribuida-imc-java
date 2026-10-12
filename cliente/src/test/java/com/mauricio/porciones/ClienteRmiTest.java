package com.mauricio.porciones;
import com.mauricio.porciones.cliente.PorcionesClient;
import com.mauricio.porciones.remoto.PorcionesResponse;
import com.mauricio.porciones.servidor.RmiServer;
public final class ClienteRmiTest {
    public static void main(String[] args) throws Exception {
        int port = 11999;
        try (RmiServer server = new RmiServer(port, "TestService", message -> { });
             PorcionesClient client = new PorcionesClient()) {
            server.start();
            client.connect("127.0.0.1", port, "TestService", "prueba");
            PorcionesResponse response = client.calcular(40, 3);
            check(response.isValid(), "La respuesta RMI debería ser válida");
            check(response.getTotal() == 120 && response.getTotalConReserva() == 132, "El resultado RMI no coincide");
            PorcionesResponse invalid = client.calcular(0, 3);
            check(!invalid.isValid(), "El servidor debe validar la solicitud");
        }
        System.out.println("La prueba de integración RMI pasó.");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
