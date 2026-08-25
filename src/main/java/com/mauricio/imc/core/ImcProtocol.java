package com.mauricio.imc.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Protocolo binario sencillo de la aplicación.
 *
 * <p>Solicitud: peso como float, seguido de altura como float.</p>
 * <p>Respuesta: IMC como float, seguido de un mensaje UTF.</p>
 */
public final class ImcProtocol {
    private ImcProtocol() {
        // Clase utilitaria.
    }

    public static ImcRequest readRequest(DataInputStream input) throws IOException {
        return new ImcRequest(input.readFloat(), input.readFloat());
    }

    public static void writeRequest(DataOutputStream output, float weightKg, float heightMeters)
            throws IOException {
        output.writeFloat(weightKg);
        output.writeFloat(heightMeters);
        output.flush();
    }

    public static void writeResponse(DataOutputStream output, ImcResult result) throws IOException {
        output.writeFloat(result.getValue());
        output.writeUTF(result.getMessage());
        output.flush();
    }

    public static ImcResult readResponse(DataInputStream input) throws IOException {
        float value = input.readFloat();
        String message = input.readUTF();
        return ImcResult.valid(value, message);
    }

    public static final class ImcRequest {
        private final float weightKg;
        private final float heightMeters;

        private ImcRequest(float weightKg, float heightMeters) {
            this.weightKg = weightKg;
            this.heightMeters = heightMeters;
        }

        public float getWeightKg() {
            return weightKg;
        }

        public float getHeightMeters() {
            return heightMeters;
        }
    }
}
