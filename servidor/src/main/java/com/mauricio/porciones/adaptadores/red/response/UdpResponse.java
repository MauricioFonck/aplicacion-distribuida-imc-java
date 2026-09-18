package com.mauricio.porciones.adaptadores.red.response;

import java.nio.charset.StandardCharsets;

/** Representación del datagrama de red que se transmitirá hacia un cliente UDP. */
public final class UdpResponse {

    private final String payload;
    private final String ipDestino;
    private final int puertoDestino;

    public UdpResponse(final String payload, final String ipDestino, final int puertoDestino) {
        this.payload = payload;
        this.ipDestino = ipDestino;
        this.puertoDestino = puertoDestino;
    }

    public String getPayload() {
        return payload;
    }

    public String getIpDestino() {
        return ipDestino;
    }

    public int getPuertoDestino() {
        return puertoDestino;
    }

    public byte[] getBytes() {
        return payload.getBytes(StandardCharsets.UTF_8);
    }
}
