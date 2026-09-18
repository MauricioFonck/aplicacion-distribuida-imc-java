package com.mauricio.porciones.aplicacion.servicios;

import com.mauricio.porciones.aplicacion.dto.CalcularPorcionesCommand;
import com.mauricio.porciones.aplicacion.dto.ConectarCommand;
import com.mauricio.porciones.aplicacion.excepciones.ClienteRedException;
import com.mauricio.porciones.aplicacion.mapper.ClienteMapper;
import com.mauricio.porciones.aplicacion.puertos.entrada.CalcularPorcionesInputPort;
import com.mauricio.porciones.aplicacion.puertos.entrada.GestionarConexionInputPort;
import com.mauricio.porciones.dominio.enums.EstadoConexion;
import com.mauricio.porciones.dominio.modelos.DatosReunion;
import com.mauricio.porciones.dominio.modelos.EventoCliente;
import com.mauricio.porciones.dominio.modelos.ResultadoPorciones;
import com.mauricio.porciones.dominio.puertos.salida.ClienteUdpPort;
import com.mauricio.porciones.dominio.puertos.salida.PuertoNotificacionCliente;
import com.mauricio.porciones.dominio.vo.DestinoServidor;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/**
 * Servicio de aplicación del cliente. Implementa los dos casos de uso (gestionar la conexión y
 * calcular las porciones) coordinando el puerto de red y el puerto de notificación.
 *
 * <p>Las operaciones de red se ejecutan en el {@link Executor} recibido para no bloquear el hilo
 * de la interfaz gráfica mientras se espera el datagrama de respuesta.</p>
 */
public final class ClientePorcionesService implements GestionarConexionInputPort, CalcularPorcionesInputPort {

    private static final System.Logger LOG = System.getLogger(ClientePorcionesService.class.getName());
    private static final String LOG_ERROR_OPERACION = "Falló la operación UDP {0}";

    private final ClienteUdpPort clienteUdp;
    private final PuertoNotificacionCliente notificador;
    private final ClienteMapper mapper;
    private final Executor executor;

    public ClientePorcionesService(
            final ClienteUdpPort clienteUdp,
            final PuertoNotificacionCliente notificador,
            final ClienteMapper mapper,
            final Executor executor) {
        this.clienteUdp = Objects.requireNonNull(clienteUdp, "El puerto UDP es obligatorio.");
        this.notificador = Objects.requireNonNull(notificador, "El notificador es obligatorio.");
        this.mapper = Objects.requireNonNull(mapper, "El mapper es obligatorio.");
        this.executor = Objects.requireNonNull(executor, "El executor es obligatorio.");
    }

    @Override
    public CompletableFuture<Void> conectar(final ConectarCommand comando) {
        final DestinoServidor destino = mapper.toDestino(comando);
        notificador.notificarEstado(EstadoConexion.CONECTANDO, destino.endpoint());
        return CompletableFuture.runAsync(() -> {
            try {
                clienteUdp.conectar(destino);
                notificador.notificarEstado(EstadoConexion.CONECTADO, destino.endpoint());
                notificador.notificarEvento(new EventoCliente("CONEXIÓN", "Conectado a " + destino.endpoint()));
            } catch (final ClienteRedException excepcion) {
                LOG.log(System.Logger.Level.DEBUG, LOG_ERROR_OPERACION, "conectar", excepcion);
                notificador.notificarEstado(EstadoConexion.DESCONECTADO, destino.endpoint());
                notificador.notificarError(excepcion.getMessage());
                throw new CompletionException(excepcion);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> desconectar() {
        return CompletableFuture.runAsync(() -> {
            try {
                clienteUdp.desconectar();
                notificador.notificarEstado(EstadoConexion.DESCONECTADO, "");
                notificador.notificarEvento(new EventoCliente("CONEXIÓN", "Cliente desconectado."));
            } catch (final ClienteRedException excepcion) {
                LOG.log(System.Logger.Level.DEBUG, LOG_ERROR_OPERACION, "desconectar", excepcion);
                notificador.notificarEstado(EstadoConexion.DESCONECTADO, "");
                notificador.notificarError(excepcion.getMessage());
                throw new CompletionException(excepcion);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<ResultadoPorciones> calcular(final CalcularPorcionesCommand comando) {
        final DatosReunion datos = mapper.toDatos(comando);
        return CompletableFuture.supplyAsync(() -> {
            try {
                final ResultadoPorciones resultado = clienteUdp.solicitarCalculo(datos);
                notificador.notificarResultado(resultado);
                return resultado;
            } catch (final ClienteRedException excepcion) {
                LOG.log(System.Logger.Level.DEBUG, LOG_ERROR_OPERACION, "calcular", excepcion);
                notificador.notificarError(excepcion.getMessage());
                throw new CompletionException(excepcion);
            }
        }, executor);
    }

    @Override
    public boolean estaConectado() {
        return clienteUdp.estaConectado();
    }
}
