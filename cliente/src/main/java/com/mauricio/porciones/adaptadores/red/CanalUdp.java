package com.mauricio.porciones.adaptadores.red;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Encapsula el {@link DatagramSocket} del cliente.
 *
 * <p>UDP no garantiza la entrega: si la petición o la respuesta se pierden, {@code receive} se
 * quedaría bloqueado para siempre. Por eso el socket se abre con un tiempo máximo de espera
 * ({@code setSoTimeout}), tras el cual se lanza {@link java.net.SocketTimeoutException}.</p>
 */
public class CanalUdp {

    private static final int BUFFER_SIZE = 2048;

    private final int timeoutMs;
    private DatagramSocket socket;

    public CanalUdp(final int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public synchronized void abrir() throws IOException {
        cerrar();
        // Sin puerto explícito: el sistema operativo asigna un puerto efímero al cliente.
        final DatagramSocket nuevoSocket = new DatagramSocket();
        try {
            nuevoSocket.setSoTimeout(timeoutMs);
            socket = nuevoSocket;
        } catch (final IOException | RuntimeException excepcion) {
            nuevoSocket.close();
            throw excepcion;
        }
    }

    public synchronized void cerrar() {
        if (Objects.nonNull(socket) && !socket.isClosed()) {
            socket.close();
        }
        socket = null;
    }

    public synchronized boolean estaAbierto() {
        return Objects.nonNull(socket) && !socket.isClosed();
    }

    /** Envía un datagrama con el mensaje y espera el datagrama de respuesta. */
    public synchronized String intercambiar(final String mensaje, final InetAddress host, final int puerto)
            throws IOException {
        if (!estaAbierto()) {
            throw new IOException("El canal UDP está cerrado.");
        }
        final byte[] salida = mensaje.getBytes(StandardCharsets.UTF_8);
        final DatagramSocket socketAbierto = socket;
        socketAbierto.send(new DatagramPacket(salida, salida.length, host, puerto));

        final DatagramPacket entrada = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        socketAbierto.receive(entrada);
        return new String(entrada.getData(), 0, entrada.getLength(), StandardCharsets.UTF_8).trim();
    }

    /** Envía un datagrama sin esperar respuesta. */
    public synchronized void enviar(final String mensaje, final InetAddress host, final int puerto)
            throws IOException {
        if (!estaAbierto()) {
            return;
        }
        final byte[] salida = mensaje.getBytes(StandardCharsets.UTF_8);
        socket.send(new DatagramPacket(salida, salida.length, host, puerto));
    }
}
