package com.mauricio.porciones.core;

import com.mauricio.porciones.client.PorcionesClient;
import com.mauricio.porciones.server.PorcionesServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Prueba de integración del protocolo TCP entre el servidor y el cliente de porciones.
 */
public final class PorcionesSocketIntegrationTest {
    private static final int TEST_PORT = 19008;

    private PorcionesSocketIntegrationTest() {
    }

    public static void main(String[] args) throws Exception {
        List<String> logs = new ArrayList<>();
        try (PorcionesServer server = new PorcionesServer(TEST_PORT, logs::add);
             PorcionesClient client = new PorcionesClient()) {
            server.start();
            client.connect("127.0.0.1", TEST_PORT);

            PorcionesResult primera = client.calculate(25, 3);
            assertTrue(primera.isValid(), "La primera respuesta debería ser válida.");
            assertEquals(75L, primera.getValue(), "La primera respuesta no coincide.");
            assertTrue(primera.getMessage().contains("mediana"),
                    "La primera clasificación no coincide.");

            PorcionesResult segunda = client.calculate(300, 2);
            assertTrue(segunda.isValid(), "La segunda respuesta debería ser válida.");
            assertEquals(600L, segunda.getValue(), "La segunda respuesta no coincide.");
            assertTrue(segunda.getMessage().contains("masivo"),
                    "La segunda clasificación no coincide.");

            PorcionesResult invalida = client.calculate(0, 5);
            assertTrue(!invalida.isValid(),
                    "El servidor debería rechazar una reunión sin asistentes.");
            assertEquals(0L, invalida.getValue(),
                    "Una respuesta inválida no debería traer un total.");

            PorcionesResult tercera = client.calculate(8, 4);
            assertEquals(32L, tercera.getValue(),
                    "La conexión debería seguir sirviendo después de una solicitud inválida.");

            assertTrue(server.isRunning(), "El servidor debería seguir activo.");
        }

        assertTrue(!logs.isEmpty(), "El servidor debería haber registrado eventos.");
        System.out.println("La prueba de integración TCP pasó.");
    }

    private static void assertEquals(long expected, long actual, String message) {
        assertTrue(expected == actual,
                message + " Esperado: " + expected + ", obtenido: " + actual);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
