package com.mauricio.porciones.server;

import com.mauricio.porciones.core.PorcionesCalculator;
import com.mauricio.porciones.core.PorcionesProtocol;
import com.mauricio.porciones.core.PorcionesResult;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Servidor TCP que estima las porciones de comida de una reunión.
 *
 * <p>Cada conexión se atiende en un hilo independiente y una misma conexión puede
 * resolver varias solicitudes consecutivas.</p>
 */
public final class PorcionesServer implements AutoCloseable {
    private static final DateTimeFormatter LOG_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final int port;
    private final Consumer<String> logger;
    private final Set<Socket> clients =
            Collections.newSetFromMap(new ConcurrentHashMap<Socket, Boolean>());

    private volatile boolean running;
    private volatile ServerSocket serverSocket;
    private Thread acceptThread;

    public PorcionesServer(int port, Consumer<String> logger) {
        validatePort(port);
        this.port = port;
        this.logger = logger == null ? message -> { } : logger;
    }

    public synchronized void start() throws IOException {
        if (running) {
            return;
        }

        serverSocket = new ServerSocket(port);
        running = true;
        acceptThread = new Thread(this::acceptClients, "porciones-server-acceptor");
        acceptThread.setDaemon(true);
        acceptThread.start();
        log("Servidor iniciado en " + getBindAddress() + ":" + port);
    }

    private void acceptClients() {
        while (running) {
            try {
                Socket client = serverSocket.accept();
                clients.add(client);
                log("Cliente conectado: " + client.getRemoteSocketAddress());
                Thread worker = new Thread(() -> handleClient(client),
                        "porciones-client-" + client.getPort());
                worker.setDaemon(true);
                worker.start();
            } catch (SocketException ex) {
                if (running) {
                    log("Error de socket al aceptar conexiones: " + ex.getMessage());
                }
            } catch (IOException ex) {
                if (running) {
                    log("Error al aceptar una conexión: " + ex.getMessage());
                }
            }
        }
    }

    private void handleClient(Socket client) {
        try (Socket socket = client;
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            while (running && !socket.isClosed()) {
                PorcionesProtocol.PorcionesRequest request;
                try {
                    request = PorcionesProtocol.readRequest(input);
                } catch (EOFException ex) {
                    break;
                }

                PorcionesResult result = PorcionesCalculator.calculate(
                        request.getAsistentes(), request.getPorcionesPorPersona());
                PorcionesProtocol.writeResponse(output, result);
                log("Solicitud atendida desde " + socket.getRemoteSocketAddress()
                        + ": personas=" + request.getAsistentes()
                        + ", porciones/persona=" + request.getPorcionesPorPersona()
                        + ", total=" + (result.isValid()
                                ? PorcionesCalculator.format(result.getValue())
                                : "rechazada (" + result.getMessage() + ")"));
            }
        } catch (IOException ex) {
            if (running) {
                log("Cliente desconectado con error: " + ex.getMessage());
            }
        } finally {
            clients.remove(client);
            log("Cliente desconectado: " + client.getRemoteSocketAddress());
        }
    }

    public synchronized void stop() {
        if (!running && serverSocket == null) {
            return;
        }

        running = false;
        for (Socket client : clients) {
            closeQuietly(client);
        }
        clients.clear();
        closeQuietly(serverSocket);
        serverSocket = null;
        log("Servidor detenido");
    }

    @Override
    public void close() {
        stop();
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }

    public String getBindAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (IOException ex) {
            return "0.0.0.0";
        }
    }

    private void log(String message) {
        logger.accept("[" + LocalDateTime.now().format(LOG_FORMAT) + "] " + message);
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
            // El cierre es una operación de limpieza; no se propaga el error.
        }
    }
}
