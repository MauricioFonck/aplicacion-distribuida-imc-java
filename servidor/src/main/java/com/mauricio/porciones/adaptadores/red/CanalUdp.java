package com.mauricio.porciones.adaptadores.red;

import com.mauricio.porciones.adaptadores.red.response.UdpResponse;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Objects;

/**
 * Encapsula la administración y las operaciones sobre el {@link DatagramSocket} del servidor.
 *
 * <p>A diferencia de TCP, aquí no hay un {@code ServerSocket} que acepte conexiones ni un socket
 * por cliente: un único socket UDP recibe los datagramas de todos los clientes y por ese mismo
 * socket se envían las respuestas.</p>
 */
public class CanalUdp {

    private DatagramSocket socket;
    private volatile boolean abierto = false;
    private int puertoActual = 0;

    public synchronized void abrir(final int puerto) throws SocketException {
        if (abierto && Objects.nonNull(socket) && !socket.isClosed()) {
            return;
        }
        socket = new DatagramSocket(puerto);
        puertoActual = puerto;
        abierto = true;
    }

    public synchronized void cerrar() {
        abierto = false;
        if (Objects.nonNull(socket) && !socket.isClosed()) {
            socket.close();
        }
        socket = null;
        puertoActual = 0;
    }

    public synchronized boolean isAbierto() {
        return abierto && Objects.nonNull(socket) && !socket.isClosed();
    }

    public synchronized int getPuertoActual() {
        return puertoActual;
    }

    /**
     * Envía una respuesta dentro de un datagrama dirigido a la IP y al puerto del cliente.
     *
     * <p>El socket pertenece al canal y no se cierra aquí: su ciclo de vida va desde
     * {@link #abrir(int)} hasta {@link #cerrar()}.</p>
     */
    @SuppressWarnings("resource")
    public void enviar(final UdpResponse respuesta) throws IOException {
        final DatagramSocket socketAbierto = obtenerSocketAbierto();
        final byte[] buffer = respuesta.getBytes();
        final InetAddress direccion = InetAddress.getByName(respuesta.getIpDestino());
        final DatagramPacket paquete =
                new DatagramPacket(buffer, buffer.length, direccion, respuesta.getPuertoDestino());
        socketAbierto.send(paquete);
    }

    /**
     * Bloquea el hilo hasta que llega un datagrama. {@link #cerrar()} es quien interrumpe una
     * recepción bloqueada, porque al cerrar el socket {@code receive} lanza una excepción.
     */
    @SuppressWarnings("resource")
    public void recibir(final DatagramPacket paquete) throws IOException {
        obtenerSocketAbierto().receive(paquete);
    }

    private synchronized DatagramSocket obtenerSocketAbierto() throws SocketException {
        if (Objects.isNull(socket) || socket.isClosed()) {
            throw new SocketException("El socket UDP está cerrado.");
        }
        return socket;
    }
}
