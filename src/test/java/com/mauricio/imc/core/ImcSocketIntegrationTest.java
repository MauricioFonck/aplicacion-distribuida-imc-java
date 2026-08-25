package com.mauricio.imc.core;

import com.mauricio.imc.client.ImcClient;
import com.mauricio.imc.server.ImcServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Prueba de integración del protocolo TCP entre servidor y cliente.
 */
public final class ImcSocketIntegrationTest {
    private static final int TEST_PORT = 19007;

    private ImcSocketIntegrationTest() {
    }

    public static void main(String[] args) throws Exception {
        List<String> logs = new ArrayList<>();
        try (ImcServer server = new ImcServer(TEST_PORT, logs::add);
             ImcClient client = new ImcClient()) {
            server.start();
            client.connect("127.0.0.1", TEST_PORT);

            ImcResult first = client.calculate(70.0f, 1.75f);
            assertBetween(22.85f, first.getValue(), 22.87f,
                    "La primera respuesta no coincide.");
            assertTrue(first.getMessage().contains("saludable"),
                    "La primera clasificación no coincide.");

            ImcResult second = client.calculate(90.0f, 1.75f);
            assertTrue(second.getValue() > 29.0f,
                    "La segunda respuesta debería representar un IMC alto.");

            assertTrue(server.isRunning(), "El servidor debería seguir activo.");
        }

        assertTrue(!logs.isEmpty(), "El servidor debería haber registrado eventos.");
        System.out.println("La prueba de integración TCP pasó.");
    }

    private static void assertBetween(float minimum, float actual, float maximum, String message) {
        assertTrue(actual >= minimum && actual <= maximum, message + " Valor: " + actual);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
