package com.mauricio.porciones.cliente;
import com.mauricio.porciones.remoto.PorcionesResponse;
public final class ClientMain {
    private ClientMain() { }
    public static void main(String[] args) throws Exception {
        try (PorcionesClient client = new PorcionesClient()) {
            client.connect("localhost", 1099, "PorcionesService", "cliente-consola");
            PorcionesResponse response = client.calcular(40, 3);
            System.out.println(response.getMensaje());
        }
    }
}
