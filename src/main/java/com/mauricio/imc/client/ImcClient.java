package com.mauricio.imc.client;

import com.mauricio.imc.core.ImcProtocol;
import com.mauricio.imc.core.ImcResult;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Cliente TCP para comunicarse con el servidor IMC.
 */
public final class ImcClient implements AutoCloseable {
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
            newSocket.connect(new InetSocketAddress(host.trim(), port), 5000);
            newSocket.setSoTimeout(15000);
            socket = newSocket;
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException | RuntimeException ex) {
            closeQuietly(newSocket);
            throw ex;
        }
    }

    public synchronized ImcResult calculate(float weightKg, float heightMeters) throws IOException {
        ensureConnected();
        ImcProtocol.writeRequest(output, weightKg, heightMeters);
        return ImcProtocol.readResponse(input);
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
