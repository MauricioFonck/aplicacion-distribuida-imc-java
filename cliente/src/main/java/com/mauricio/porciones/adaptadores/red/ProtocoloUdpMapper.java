package com.mauricio.porciones.adaptadores.red;

import com.mauricio.porciones.dominio.excepciones.RespuestaServidorException;
import com.mauricio.porciones.dominio.modelos.DatosReunion;
import com.mauricio.porciones.dominio.modelos.ResultadoPorciones;
import java.util.Objects;

/**
 * Traduce entre los objetos del dominio y el texto del protocolo UDP.
 *
 * <p>Petición: {@code CALCULAR;personas;porcionesPorPersona}. Respuesta:
 * {@code OK_CALCULO;total;totalConReserva;clasificacion;mensaje} o {@code ERROR;motivo}.</p>
 */
public final class ProtocoloUdpMapper {

    public static final String CONECTAR = "CONECTAR";
    public static final String DESCONECTAR = "DESCONECTAR";

    private static final String PREFIJO_ERROR = "ERROR;";

    public String serializarCalculo(final DatosReunion datos) {
        Objects.requireNonNull(datos, "Los datos de la reunión son obligatorios.");
        return "CALCULAR;" + datos.asistentes().valor() + ";" + datos.porcionesPorPersona().valor();
    }

    public void validarConexion(final String respuesta) {
        if (Objects.isNull(respuesta) || !respuesta.startsWith("CONECTADO_OK;")) {
            throw new RespuestaServidorException("Respuesta de conexión inesperada: " + respuesta);
        }
    }

    public ResultadoPorciones parsearCalculo(final String respuesta) {
        if (Objects.isNull(respuesta) || respuesta.isBlank()) {
            throw new RespuestaServidorException("El servidor envió una respuesta vacía.");
        }
        if (respuesta.startsWith(PREFIJO_ERROR)) {
            throw new RespuestaServidorException(
                    "Error del servidor: " + respuesta.substring(PREFIJO_ERROR.length()));
        }
        // Límite 5: si el mensaje final trajera un ';' no se parte en más campos.
        final String[] partes = respuesta.split(";", 5);
        if (partes.length != 5 || !"OK_CALCULO".equals(partes[0])) {
            throw new RespuestaServidorException("Respuesta de cálculo no estructurada: " + respuesta);
        }
        try {
            final long total = Long.parseLong(partes[1].trim());
            final long totalConReserva = Long.parseLong(partes[2].trim());
            return new ResultadoPorciones(total, totalConReserva, partes[3], partes[4]);
        } catch (final NumberFormatException excepcion) {
            throw new RespuestaServidorException("El total de porciones recibido no es numérico.");
        }
    }
}
