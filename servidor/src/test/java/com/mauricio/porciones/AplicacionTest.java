package com.mauricio.porciones;

import static com.mauricio.porciones.Verificador.assertEquals;
import static com.mauricio.porciones.Verificador.assertThrows;
import static com.mauricio.porciones.Verificador.assertTrue;

import com.mauricio.porciones.aplicacion.dto.CalcularPorcionesCommand;
import com.mauricio.porciones.aplicacion.dto.ProcesarPeticionUdpCommand;
import com.mauricio.porciones.aplicacion.dto.ResultadoPorcionesDto;
import com.mauricio.porciones.aplicacion.excepciones.ComandoInvalidoException;
import com.mauricio.porciones.aplicacion.excepciones.ServidorRedException;
import com.mauricio.porciones.aplicacion.mapper.EstimacionMapper;
import com.mauricio.porciones.aplicacion.mapper.PeticionMapper;
import com.mauricio.porciones.aplicacion.servicios.CalcularPorcionesService;
import com.mauricio.porciones.aplicacion.servicios.GestionarServidorService;
import com.mauricio.porciones.aplicacion.servicios.ProcesarPeticionUdpService;
import com.mauricio.porciones.dominio.enums.EstadoServidor;
import com.mauricio.porciones.dominio.enums.TipoRespuesta;
import com.mauricio.porciones.dominio.excepciones.AsistentesIncorrectosException;
import com.mauricio.porciones.dominio.excepciones.PuertoIncorrectoException;
import com.mauricio.porciones.dominio.modelos.EventoServidor;
import com.mauricio.porciones.dominio.modelos.RespuestaCliente;
import com.mauricio.porciones.dominio.puertos.salida.ControladorServidorRedPort;
import com.mauricio.porciones.dominio.puertos.salida.PuertoNotificacionEvento;
import com.mauricio.porciones.dominio.puertos.salida.PuertoSalidaRed;
import com.mauricio.porciones.dominio.vo.PuertoRed;
import java.util.ArrayList;
import java.util.List;

/**
 * Pruebas de los casos de uso del servidor. Los puertos de salida se reemplazan por dobles en
 * memoria, de modo que ninguna prueba abre un socket: esa es la ventaja de la arquitectura
 * hexagonal.
 */
public final class AplicacionTest {

    private AplicacionTest() {
    }

    public static void main(final String[] args) throws ServidorRedException {
        debeCalcularDesdeElCasoDeUso();
        debeRechazarComandosNulos();
        debeResponderAlSaludoDeConexion();
        debeResponderALaDesconexion();
        debeResponderElCalculo();
        debeAceptarElComandoEnMinusculas();
        debeRechazarElMensajeVacio();
        debeRechazarComandosDesconocidos();
        debeRechazarElFormatoIncorrecto();
        debeRechazarValoresNoNumericos();
        debeRechazarValoresFueraDelDominio();
        debeGestionarElCicloDeVidaDelServidor();
        debeRechazarUnPuertoInvalidoAlIniciar();
        System.out.println("Todas las pruebas de AplicacionTest pasaron.");
    }

    private static void debeCalcularDesdeElCasoDeUso() {
        final CalcularPorcionesService servicio = new CalcularPorcionesService(new EstimacionMapper());
        final ResultadoPorcionesDto dto = servicio.calcular(new CalcularPorcionesCommand(40, 3));
        assertEquals(120L, dto.total(), "El total del caso de uso no coincide.");
        assertEquals(132L, dto.totalConReserva(), "El total con reserva del caso de uso no coincide.");
        assertEquals("Reunión mediana", dto.clasificacion(), "La clasificación no coincide.");
        assertThrows(AsistentesIncorrectosException.class,
                () -> servicio.calcular(new CalcularPorcionesCommand(0, 3)),
                "El caso de uso debería propagar la validación del dominio.");
    }

    private static void debeRechazarComandosNulos() {
        final EstimacionMapper mapper = new EstimacionMapper();
        assertThrows(ComandoInvalidoException.class, () -> mapper.toDomain(null),
                "Un comando de cálculo nulo debería rechazarse.");
        assertThrows(ComandoInvalidoException.class, () -> mapper.toDto(null),
                "Un resultado nulo debería rechazarse.");
        assertThrows(ComandoInvalidoException.class, () -> new PeticionMapper().toDestinatario(null),
                "Un comando de petición nulo debería rechazarse.");
    }

    private static void debeResponderAlSaludoDeConexion() {
        final Escenario escenario = new Escenario();
        escenario.procesar("CONECTAR");
        final RespuestaCliente respuesta = escenario.unicaRespuesta();
        assertEquals(TipoRespuesta.CONECTADO, respuesta.getTipo(), "Se esperaba la confirmación de conexión.");
        assertEquals("127.0.0.1:5000", respuesta.getDestinatario().endpoint(),
                "La respuesta debe dirigirse al origen del datagrama.");
        assertEquals(1, escenario.salidaEventos.eventos.size(), "Debería registrarse un evento.");
    }

    private static void debeResponderALaDesconexion() {
        final Escenario escenario = new Escenario();
        escenario.procesar("DESCONECTAR");
        assertEquals(TipoRespuesta.DESCONECTADO, escenario.unicaRespuesta().getTipo(),
                "Se esperaba la confirmación de desconexión.");
    }

    private static void debeResponderElCalculo() {
        final Escenario escenario = new Escenario();
        escenario.procesar("CALCULAR;40;3");
        final RespuestaCliente respuesta = escenario.unicaRespuesta();
        assertEquals(TipoRespuesta.OK_CALCULO, respuesta.getTipo(), "Se esperaba un cálculo exitoso.");
        assertEquals(120L, respuesta.getResultado().getTotal(), "El total calculado no coincide.");
        assertEquals(132L, respuesta.getResultado().getTotalConReserva(), "La reserva no coincide.");
        final String descripcion = escenario.salidaEventos.eventos.get(0).getDescripcion();
        assertTrue(descripcion.contains("Personas: 40") && descripcion.contains("Total: 120"),
                "El evento debería describir lo recibido y lo enviado. Evento: " + descripcion);
    }

    private static void debeAceptarElComandoEnMinusculas() {
        final Escenario escenario = new Escenario();
        escenario.procesar("  calcular; 10 ; 2  ");
        assertEquals(20L, escenario.unicaRespuesta().getResultado().getTotal(),
                "El comando debería aceptarse sin importar mayúsculas ni espacios.");
    }

    private static void debeRechazarElMensajeVacio() {
        final Escenario escenario = new Escenario();
        escenario.procesar("   ");
        assertError(escenario, "Mensaje vacío");
    }

    private static void debeRechazarComandosDesconocidos() {
        final Escenario escenario = new Escenario();
        escenario.procesar("SUMAR;1;2");
        assertError(escenario, "Comando no reconocido");
    }

    private static void debeRechazarElFormatoIncorrecto() {
        final Escenario escenario = new Escenario();
        escenario.procesar("CALCULAR;40");
        assertError(escenario, "Formato inválido");

        final Escenario conCamposDeMas = new Escenario();
        conCamposDeMas.procesar("CALCULAR;40;3;9");
        assertError(conCamposDeMas, "Formato inválido");
    }

    private static void debeRechazarValoresNoNumericos() {
        final Escenario escenario = new Escenario();
        escenario.procesar("CALCULAR;cuarenta;3");
        assertError(escenario, "números enteros");

        final Escenario conDecimales = new Escenario();
        conDecimales.procesar("CALCULAR;40;2.5");
        assertError(conDecimales, "números enteros");
    }

    private static void debeRechazarValoresFueraDelDominio() {
        final Escenario sinPersonas = new Escenario();
        sinPersonas.procesar("CALCULAR;0;3");
        assertError(sinPersonas, "mayor que 0");

        final Escenario demasiadasPorciones = new Escenario();
        demasiadasPorciones.procesar("CALCULAR;10;101");
        assertError(demasiadasPorciones, "no pueden superar 100");
    }

    private static void debeGestionarElCicloDeVidaDelServidor() throws ServidorRedException {
        final ControladorFalso controlador = new ControladorFalso();
        final SalidaEventos eventos = new SalidaEventos();
        final GestionarServidorService servicio = new GestionarServidorService(controlador, eventos);

        assertTrue(!servicio.estaCorriendo(), "El servidor debería empezar detenido.");
        assertEquals(0, servicio.getPuertoActual(), "Sin servidor activo el puerto es 0.");

        servicio.iniciarServidor(9008);
        assertTrue(servicio.estaCorriendo(), "El servidor debería quedar en línea.");
        assertEquals(9008, servicio.getPuertoActual(), "El puerto activo no coincide.");
        assertEquals(EstadoServidor.EN_LINEA, eventos.estados.get(0), "Debería notificarse EN_LINEA.");

        servicio.detenerServidor();
        assertTrue(!servicio.estaCorriendo(), "El servidor debería quedar detenido.");
        assertEquals(EstadoServidor.DETENIDO, eventos.estados.get(1), "Debería notificarse DETENIDO.");
        assertEquals(2, eventos.eventos.size(), "Deberían registrarse el inicio y la parada.");
    }

    private static void debeRechazarUnPuertoInvalidoAlIniciar() {
        final ControladorFalso controlador = new ControladorFalso();
        final GestionarServidorService servicio = new GestionarServidorService(controlador, new SalidaEventos());
        assertThrows(PuertoIncorrectoException.class, () -> {
            try {
                servicio.iniciarServidor(80);
            } catch (final ServidorRedException excepcion) {
                throw new IllegalStateException(excepcion);
            }
        }, "Un puerto reservado debería rechazarse antes de tocar la red.");
        assertTrue(!controlador.isActivo(), "El controlador no debería haberse iniciado.");
    }

    private static void assertError(final Escenario escenario, final String fragmento) {
        final RespuestaCliente respuesta = escenario.unicaRespuesta();
        assertEquals(TipoRespuesta.ERROR, respuesta.getTipo(), "Se esperaba una respuesta de error.");
        assertTrue(respuesta.getMensaje().contains(fragmento),
                "El error debería mencionar \"" + fragmento + "\". Mensaje: " + respuesta.getMensaje());
    }

    /** Caso de uso de peticiones armado con dobles de los puertos de salida. */
    private static final class Escenario {
        private final SalidaRed salidaRed = new SalidaRed();
        private final SalidaEventos salidaEventos = new SalidaEventos();
        private final ProcesarPeticionUdpService servicio = new ProcesarPeticionUdpService(
                salidaRed, salidaEventos, new PeticionMapper(), new EstimacionMapper());

        void procesar(final String mensaje) {
            servicio.procesar(new ProcesarPeticionUdpCommand("127.0.0.1", 5000, mensaje));
        }

        RespuestaCliente unicaRespuesta() {
            assertEquals(1, salidaRed.respuestas.size(), "Cada petición debe producir una sola respuesta.");
            return salidaRed.respuestas.get(0);
        }
    }

    private static final class SalidaRed implements PuertoSalidaRed {
        private final List<RespuestaCliente> respuestas = new ArrayList<>();

        @Override
        public void enviarRespuesta(final RespuestaCliente respuesta) {
            respuestas.add(respuesta);
        }
    }

    private static final class SalidaEventos implements PuertoNotificacionEvento {
        private final List<EventoServidor> eventos = new ArrayList<>();
        private final List<EstadoServidor> estados = new ArrayList<>();

        @Override
        public void notificarEvento(final EventoServidor evento) {
            eventos.add(evento);
        }

        @Override
        public void notificarCambioEstado(final EstadoServidor nuevoEstado, final int puerto) {
            estados.add(nuevoEstado);
        }
    }

    private static final class ControladorFalso implements ControladorServidorRedPort {
        private PuertoRed puertoActual;

        @Override
        public void iniciar(final PuertoRed puerto) {
            puertoActual = puerto;
        }

        @Override
        public void detener() {
            puertoActual = null;
        }

        @Override
        public boolean isActivo() {
            return puertoActual != null;
        }

        @Override
        public PuertoRed getPuertoActual() {
            return puertoActual;
        }
    }
}
