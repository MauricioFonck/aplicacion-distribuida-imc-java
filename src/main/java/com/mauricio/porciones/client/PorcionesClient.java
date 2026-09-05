package com.mauricio.porciones.client;

import com.mauricio.porciones.core.PorcionesProtocol;
import com.mauricio.porciones.core.PorcionesResult;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Cliente TCP que consulta al servidor las porciones necesarias para una reunión.
 */
public final class PorcionesClient implements AutoCloseable {
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 15000;

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    public synchronized void connect(String host, int port) throws IOException {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección IP o nombre del servidor es obligatorio.");
        }
        validatePort(port);
        close();

        Socket newSocket = new Socket();
        try {
            newSocket.connect(new InetSocketAddress(host.trim(), port), CONNECT_TIMEOUT_MS);
            newSocket.setSoTimeout(READ_TIMEOUT_MS);
            socket = newSocket;
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException | RuntimeException ex) {
            closeQuietly(newSocket);
            throw ex;
        }
    }

    /**
     * Envía la solicitud al servidor y espera la estimación.
     *
     * @param asistentes          cantidad de personas que asistirán.
     * @param porcionesPorPersona porciones previstas para cada persona.
     * @return el resultado devuelto por el servidor.
     */
    public synchronized PorcionesResult calculate(int asistentes, int porcionesPorPersona)
            throws IOException {
        ensureConnected();
        PorcionesProtocol.writeRequest(output, asistentes, porcionesPorPersona);
        return PorcionesProtocol.readResponse(input);
    }

    public synchronized boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public synchronized void close() {
        closeQuietly(input);
        closeQuietly(output);
        closeQuietly(socket);
        input = null;
        output = null;
        socket = null;
    }

    private void ensureConnected() {
        if (!isConnected()) {
            throw new IllegalStateException("El cliente no está conectado al servidor.");
        }
    }

    private static void validatePort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("El puerto debe estar entre 1 y 65535.");
        }
    }

    private static void closeQuietly(AutoCloseable resource) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (Exception ignored) {
            // No se propaga un error producido durante la limpieza.
        }
    }
}
