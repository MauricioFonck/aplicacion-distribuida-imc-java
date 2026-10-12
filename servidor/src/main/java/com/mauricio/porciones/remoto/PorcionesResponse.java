package com.mauricio.porciones.remoto;

import java.io.Serializable;

/** Respuesta serializable que cruza la frontera RMI. */
public final class PorcionesResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private final boolean valid;
    private final long total;
    private final long totalConReserva;
    private final String clasificacion;
    private final String mensaje;

    private PorcionesResponse(boolean valid, long total, long totalConReserva,
                              String clasificacion, String mensaje) {
        this.valid = valid;
        this.total = total;
        this.totalConReserva = totalConReserva;
        this.clasificacion = clasificacion;
        this.mensaje = mensaje;
    }
    public static PorcionesResponse ok(long total, long reserva, String clasificacion, String mensaje) {
        return new PorcionesResponse(true, total, reserva, clasificacion, mensaje);
    }
    public static PorcionesResponse error(String mensaje) {
        return new PorcionesResponse(false, 0, 0, "", mensaje);
    }
    public boolean isValid() { return valid; }
    public long getTotal() { return total; }
    public long getTotalConReserva() { return totalConReserva; }
    public String getClasificacion() { return clasificacion; }
    public String getMensaje() { return mensaje; }
}
