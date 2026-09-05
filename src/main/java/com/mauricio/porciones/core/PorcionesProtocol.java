package com.mauricio.porciones.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Protocolo binario de la aplicación de porciones.
 *
 * <p>Solicitud: cantidad de asistentes como {@code int}, seguida de las porciones
 * por persona como {@code int}.</p>
 *
 * <p>Respuesta: indicador de validez como {@code boolean}, total de porciones como
 * {@code long} y mensaje como {@code UTF}.</p>
 */
public final class PorcionesProtocol {
    /** Puerto de servicio utilizado por defecto. */
    public static final int DEFAULT_PORT = 9008;

    private PorcionesProtocol() {
        // Clase utilitaria.
    }

    public static PorcionesRequest readRequest(DataInputStream input) throws IOException {
        int asistentes = input.readInt();
        int porcionesPorPersona = input.readInt();
        return new PorcionesRequest(asistentes, porcionesPorPersona);
    }

    public static void writeRequest(DataOutputStream output, int asistentes, int porcionesPorPersona)
            throws IOException {
        output.writeInt(asistentes);
        output.writeInt(porcionesPorPersona);
        output.flush();
    }

    public static void writeResponse(DataOutputStream output, PorcionesResult result) throws IOException {
        output.writeBoolean(result.isValid());
        output.writeLong(result.getValue());
        output.writeUTF(result.getMessage());
        output.flush();
    }

    public static PorcionesResult readResponse(DataInputStream input) throws IOException {
        boolean valid = input.readBoolean();
        long value = input.readLong();
        String message = input.readUTF();
        return valid ? PorcionesResult.valid(value, message) : PorcionesResult.invalid(message);
    }

    /** Datos enviados por el cliente en una solicitud. */
    public static final class PorcionesRequest {
        private final int asistentes;
        private final int porcionesPorPersona;

        private PorcionesRequest(int asistentes, int porcionesPorPersona) {
            this.asistentes = asistentes;
            this.porcionesPorPersona = porcionesPorPersona;
        }

        public int getAsistentes() {
            return asistentes;
        }

        public int getPorcionesPorPersona() {
            return porcionesPorPersona;
        }
    }
}
