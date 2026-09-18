package com.mauricio.porciones;

import static com.mauricio.porciones.Verificador.assertEquals;
import static com.mauricio.porciones.Verificador.assertThrows;
import static com.mauricio.porciones.Verificador.assertTrue;

import com.mauricio.porciones.adaptadores.notificacion.AdaptadorNotificacionCliente;
import com.mauricio.porciones.adaptadores.notificacion.ObservadorCliente;
import com.mauricio.porciones.aplicacion.dto.CalcularPorcionesCommand;
import com.mauricio.porciones.aplicacion.dto.ConectarCommand;
import com.mauricio.porciones.aplicacion.excepciones.ClienteRedException;
import com.mauricio.porciones.aplicacion.mapper.ClienteMapper;
import com.mauricio.porciones.aplicacion.servicios.ClientePorcionesService;
import com.mauricio.porciones.dominio.enums.EstadoConexion;
import com.mauricio.porciones.dominio.excepciones.DestinoIncorrectoException;
import com.mauricio.porciones.dominio.excepciones.PorcionesIncorrectasException;
import com.mauricio.porciones.dominio.modelos.DatosReunion;
import com.mauricio.porciones.dominio.modelos.EventoCliente;
import com.mauricio.porciones.dominio.modelos.ResultadoPorciones;
import com.mauricio.porciones.dominio.puertos.salida.ClienteUdpPort;
import com.mauricio.porciones.dominio.vo.DestinoServidor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

/**
 * Pruebas de los casos de uso del cliente. El puerto de red se reemplaza por un doble en memoria
 * y las tareas se ejecutan en el mismo hilo, así que ninguna prueba necesita un servidor.
 */
public final class ClienteServiceTest {

    private ClienteServiceTest() {
    }

    public static void main(final String[] args) {
        debeConectarYNotificarLosEstados();
        debeNotificarElFalloDeConexion();
        debeValidarElDestinoAntesDeUsarLaRed();
        debeCalcularYNotificarElResultado();
        debeNotificarElFalloDelCalculo();
        debeValidarLosDatosAntesDeUsarLaRed();
        debeDesconectar();
        System.out.println("Todas las pruebas de ClienteServiceTest pasaron.");
    }

    private static void debeConectarYNotificarLosEstados() {
        final Escenario escenario = new Escenario();
        escenario.servicio.conectar(new ConectarCommand("localhost", 9008)).join();
        assertTrue(escenario.servicio.estaConectado(), "El cliente debería quedar conectado.");
        assertEquals(List.of("CONECTANDO localhost:9008", "CONECTADO localhost:9008", "EVENTO CONEXIÓN"),
                escenario.avisos, "La secuencia de notificaciones no coincide.");
    }

    private static void debeNotificarElFalloDeConexion() {
        final Escenario escenario = new Escenario();
        escenario.red.fallo = new ClienteRedException("El servidor no respondió dentro del tiempo esperado.");
        final CompletionException excepcion = assertThrows(CompletionException.class,
                () -> escenario.servicio.conectar(new ConectarCommand("localhost", 9008)).join(),
                "El futuro debería completarse con error.");
        assertTrue(excepcion.getCause() instanceof ClienteRedException, "La causa debería ser el fallo de red.");
        assertEquals(List.of("CONECTANDO localhost:9008", "DESCONECTADO localhost:9008",
                        "ERROR El servidor no respondió dentro del tiempo esperado."),
                escenario.avisos, "El fallo debería devolver el estado a DESCONECTADO.");
    }

    private static void debeValidarElDestinoAntesDeUsarLaRed() {
        final Escenario escenario = new Escenario();
        assertThrows(DestinoIncorrectoException.class,
                () -> escenario.servicio.conectar(new ConectarCommand("localhost", 80)),
                "Un puerto inválido debería rechazarse de inmediato.");
        assertTrue(escenario.avisos.isEmpty(), "No debería notificarse nada si el destino es inválido.");
    }

    private static void debeCalcularYNotificarElResultado() {
        final Escenario escenario = new Escenario();
        escenario.servicio.conectar(new ConectarCommand("localhost", 9008)).join();
        escenario.avisos.clear();

        final ResultadoPorciones resultado =
                escenario.servicio.calcular(new CalcularPorcionesCommand(40, 3)).join();
        assertEquals(120L, resultado.total(), "El total devuelto no coincide.");
        assertEquals(40, escenario.red.ultimosDatos.asistentes().valor(),
                "El puerto de red debería recibir los datos del dominio.");
        assertEquals(List.of("RESULTADO 120"), escenario.avisos, "Debería notificarse el resultado.");
    }

    private static void debeNotificarElFalloDelCalculo() {
        final Escenario escenario = new Escenario();
        escenario.red.fallo = new ClienteRedException("El servidor no respondió al cálculo.");
        assertThrows(CompletionException.class,
                () -> escenario.servicio.calcular(new CalcularPorcionesCommand(40, 3)).join(),
                "El futuro debería completarse con error.");
        assertEquals(List.of("ERROR El servidor no respondió al cálculo."), escenario.avisos,
                "Debería notificarse el error.");
    }

    private static void debeValidarLosDatosAntesDeUsarLaRed() {
        final Escenario escenario = new Escenario();
        assertThrows(PorcionesIncorrectasException.class,
                () -> escenario.servicio.calcular(new CalcularPorcionesCommand(40, 0)),
                "Los datos inválidos no deberían llegar a la red.");
        assertTrue(escenario.red.ultimosDatos == null, "El puerto de red no debería haberse invocado.");
    }

    private static void debeDesconectar() {
        final Escenario escenario = new Escenario();
        escenario.servicio.conectar(new ConectarCommand("localhost", 9008)).join();
        escenario.avisos.clear();
        escenario.servicio.desconectar().join();
        assertTrue(!escenario.servicio.estaConectado(), "El cliente debería quedar desconectado.");
        assertEquals(List.of("DESCONECTADO ", "EVENTO CONEXIÓN"), escenario.avisos,
                "La desconexión debería notificarse.");
    }

    /** Servicio armado con un puerto de red falso y un observador que anota cada aviso. */
    private static final class Escenario implements ObservadorCliente {
        private final RedFalsa red = new RedFalsa();
        private final List<String> avisos = new ArrayList<>();
        private final ClientePorcionesService servicio;

        Escenario() {
            final AdaptadorNotificacionCliente notificador = new AdaptadorNotificacionCliente();
            notificador.registrar(this);
            servicio = new ClientePorcionesService(red, notificador, new ClienteMapper(), Runnable::run);
        }

        @Override
        public void onEstado(final EstadoConexion estado, final String endpoint) {
            avisos.add(estado + " " + endpoint);
        }

        @Override
        public void onEvento(final EventoCliente evento) {
            avisos.add("EVENTO " + evento.categoria());
        }

        @Override
        public void onResultado(final ResultadoPorciones resultado) {
            avisos.add("RESULTADO " + resultado.total());
        }

        @Override
        public void onError(final String mensaje) {
            avisos.add("ERROR " + mensaje);
        }
    }

    private static final class RedFalsa implements ClienteUdpPort {
        private ClienteRedException fallo;
        private boolean conectado;
        private DatosReunion ultimosDatos;

        @Override
        public void conectar(final DestinoServidor destino) throws ClienteRedException {
            lanzarSiHayFallo();
            conectado = true;
        }

        @Override
        public void desconectar() throws ClienteRedException {
            lanzarSiHayFallo();
            conectado = false;
        }

        @Override
        public ResultadoPorciones solicitarCalculo(final DatosReunion datos) throws ClienteRedException {
            lanzarSiHayFallo();
            ultimosDatos = datos;
            final long total = (long) datos.asistentes().valor() * datos.porcionesPorPersona().valor();
            return new ResultadoPorciones(total, total + 12L, "Reunión mediana", "mensaje");
        }

        @Override
        public boolean estaConectado() {
            return conectado;
        }

        private void lanzarSiHayFallo() throws ClienteRedException {
            if (fallo != null) {
                throw fallo;
            }
        }
    }
}
