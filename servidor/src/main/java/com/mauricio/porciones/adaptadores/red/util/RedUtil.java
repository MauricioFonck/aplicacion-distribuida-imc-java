package com.mauricio.porciones.adaptadores.red.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/** Utilidad de red. Responsabilidad única: resolver la dirección IP local del equipo. */
public final class RedUtil {

    private RedUtil() {
        // Clase utilitaria.
    }

    /**
     * Obtiene la dirección IPv4 local que no sea de loopback. Si no hay ninguna interfaz activa,
     * devuelve la dirección de loopback.
     */
    public static String obtenerIpLocal() {
        try {
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                final Enumeration<InetAddress> direcciones = iface.getInetAddresses();
                while (direcciones.hasMoreElements()) {
                    final InetAddress direccion = direcciones.nextElement();
                    if (!direccion.isLoopbackAddress() && direccion instanceof Inet4Address) {
                        return direccion.getHostAddress();
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (final SocketException | UnknownHostException excepcion) {
            return InetAddress.getLoopbackAddress().getHostAddress();
        }
    }
}
