package com.mauricio.porciones.adaptadores.red;

import com.mauricio.porciones.aplicacion.excepciones.ClienteRedException;
import com.mauricio.porciones.dominio.excepciones.RespuestaServidorException;
import com.mauricio.porciones.dominio.modelos.DatosReunion;
import com.mauricio.porciones.dominio.modelos.ResultadoPorciones;
import com.mauricio.porciones.dominio.puertos.salida.ClienteUdpPort;
import com.mauricio.porciones.dominio.vo.DestinoServidor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Adaptador de salida que implementa {@link ClienteUdpPort} sobre datagramas UDP.
 *
 * <p>Como UDP no tiene conexión, la "conexión" del cliente es lógica: se envía {@code CONECTAR}
 * y, si el servidor contesta {@code CONECTADO_OK}, se recuerda su dirección para las peticiones
 * siguientes.</p>
 */
public final class AdaptadorClienteUdp implements ClienteUdpPort {

    private final CanalUdp canal;
    private final ProtocoloUdpMapper protocolo;
    private volatile ConexionActiva conexion;

    public AdaptadorClienteUdp(final CanalUdp canal, final ProtocoloUdpMapper protocolo) {
        this.canal = Objects.requireNonNull(canal, "El canal UDP es obligatorio.");
        this.protocolo = Objects.requireNonNull(protocolo, "El protocolo es obligatorio.");
    }

    @Override
    public synchronized void conectar(final DestinoServidor nuevoDestino) throws ClienteRedException {
        Objects.requireNonNull(nuevoDestino, "El destino es obligatorio.");
        try {
            final InetAddress nuevaDireccion = InetAddress.getByName(nuevoDestino.host());
            canal.abrir();
            final String respuesta =
                    canal.intercambiar(ProtocoloUdpMapper.CONECTAR, nuevaDireccion, nuevoDestino.puerto());
            protocolo.validarConexion(respuesta);
            conexion = new ConexionActiva(nuevaDireccion, nuevoDestino);
        } catch (final SocketTimeoutException excepcion) {
            canal.cerrar();
            throw new ClienteRedException("El servidor no respondió dentro del tiempo esperado.", excepcion);
        } catch (final UnknownHostException excepcion) {
            canal.cerrar();
            throw new ClienteRedException("No se pudo resolver el host " + nuevoDestino.host() + ".", excepcion);
        } catch (final IOException | RespuestaServidorException excepcion) {
            canal.cerrar();
            throw new ClienteRedException("No se pudo conectar con " + nuevoDestino.endpoint() + ".", excepcion);
        }
    }

    @Override
    public synchronized void desconectar() throws ClienteRedException {
        try {
            if (Objects.nonNull(conexion)) {
                final ConexionActiva actual = conexion;
                canal.enviar(ProtocoloUdpMapper.DESCONECTAR, actual.direccion(), actual.destino().puerto());
            }
        } catch (final IOException excepcion) {
            throw new ClienteRedException("No se pudo notificar la desconexión.", excepcion);
        } finally {
            conexion = null;
            canal.cerrar();
        }
    }

    @Override
    public synchronized ResultadoPorciones solicitarCalculo(final DatosReunion datos) throws ClienteRedException {
        if (Objects.isNull(conexion)) {
            throw new ClienteRedException("Debe conectarse antes de solicitar un cálculo.");
        }
        try {
            final ConexionActiva actual = conexion;
            final String respuesta = canal.intercambiar(
                    protocolo.serializarCalculo(datos), actual.direccion(), actual.destino().puerto());
            return protocolo.parsearCalculo(respuesta);
        } catch (final SocketTimeoutException excepcion) {
            throw new ClienteRedException("El servidor no respondió al cálculo.", excepcion);
        } catch (final RespuestaServidorException excepcion) {
            throw new ClienteRedException(excepcion.getMessage(), excepcion);
        } catch (final IOException excepcion) {
            throw new ClienteRedException("Falló la comunicación del cálculo.", excepcion);
        }
    }

    @Override
    public boolean estaConectado() {
        return Objects.nonNull(conexion) && canal.estaAbierto();
    }

    private record ConexionActiva(InetAddress direccion, DestinoServidor destino) {
        private ConexionActiva {
            Objects.requireNonNull(direccion, "La dirección es obligatoria.");
            Objects.requireNonNull(destino, "El destino es obligatorio.");
        }
    }
}
