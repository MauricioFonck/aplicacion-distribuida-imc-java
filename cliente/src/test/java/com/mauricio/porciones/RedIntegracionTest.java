package com.mauricio.porciones;

import static com.mauricio.porciones.Verificador.assertEquals;
import static com.mauricio.porciones.Verificador.assertThrows;
import static com.mauricio.porciones.Verificador.assertTrue;

import com.mauricio.porciones.adaptadores.red.AdaptadorClienteUdp;
import com.mauricio.porciones.adaptadores.red.CanalUdp;
import com.mauricio.porciones.adaptadores.red.ProtocoloUdpMapper;
import com.mauricio.porciones.aplicacion.excepciones.ClienteRedException;
import com.mauricio.porciones.dominio.modelos.DatosReunion;
import com.mauricio.porciones.dominio.modelos.ResultadoPorciones;
import com.mauricio.porciones.dominio.vo.Asistentes;
import com.mauricio.porciones.dominio.vo.DestinoServidor;
import com.mauricio.porciones.dominio.vo.PorcionesPorPersona;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Prueba de integración del adaptador UDP del cliente contra un servidor de mentira que habla el
 * mismo protocolo por un socket real de la máquina local.
 */
public final class RedIntegracionTest {

    private static final int PUERTO = 19018;
    private static final int PUERTO_SIN_SERVIDOR = 19019;

    private RedIntegracionTest() {
    }

    public static void main(final String[] args) throws Exception {
        try (ServidorDePrueba servidor = new ServidorDePrueba(PUERTO)) {
            debeConectarCalcularYDesconectar(servidor);
            debeInformarElErrorDelServidor();
        }
        debeExigirLaConexionAntesDeCalcular();
        debeAgotarElTiempoSiNadieResponde();
        debeFallarSiElHostNoExiste();
        System.out.println("Todas las pruebas de RedIntegracionTest pasaron.");
    }

    private static void debeConectarCalcularYDesconectar(final ServidorDePrueba servidor) throws Exception {
        final AdaptadorClienteUdp cliente = nuevoCliente(2000);
        assertTrue(!cliente.estaConectado(), "El cliente debería empezar desconectado.");

        cliente.conectar(new DestinoServidor("localhost", PUERTO));
        assertTrue(cliente.estaConectado(), "El cliente debería quedar conectado tras el saludo.");

        final ResultadoPorciones resultado = cliente.solicitarCalculo(datos(40, 3));
        assertEquals(120L, resultado.total(), "El total recibido no coincide.");
        assertEquals(132L, resultado.totalConReserva(), "El total con reserva no coincide.");
        assertEquals("Reunión mediana", resultado.clasificacion(), "La clasificación no coincide.");

        // La misma sesión admite varios cálculos consecutivos.
        assertEquals(75L, cliente.solicitarCalculo(datos(25, 3)).total(), "El segundo total no coincide.");

        cliente.desconectar();
        assertTrue(!cliente.estaConectado(), "El cliente debería quedar desconectado.");
        servidor.esperarMensajes(4);
        assertEquals(List.of("CONECTAR", "CALCULAR;40;3", "CALCULAR;25;3", "DESCONECTAR"), servidor.recibidos,
                "El servidor debería recibir los datagramas del protocolo en orden.");
    }

    private static void debeInformarElErrorDelServidor() throws Exception {
        final AdaptadorClienteUdp cliente = nuevoCliente(2000);
        cliente.conectar(new DestinoServidor("127.0.0.1", PUERTO));
        // El servidor de prueba rechaza las reuniones de 13 personas.
        final ClienteRedException excepcion = assertThrows(ClienteRedException.class,
                () -> cliente.solicitarCalculo(datos(13, 1)),
                "Un ERROR del servidor debería convertirse en ClienteRedException.");
        assertTrue(excepcion.getMessage().contains("motivo de prueba"),
                "Debería conservarse el motivo enviado por el servidor. Mensaje: " + excepcion.getMessage());
        assertTrue(cliente.estaConectado(), "Un cálculo rechazado no debería cerrar la sesión.");
        cliente.desconectar();
    }

    private static void debeExigirLaConexionAntesDeCalcular() {
        final AdaptadorClienteUdp cliente = nuevoCliente(500);
        assertThrows(ClienteRedException.class, () -> cliente.solicitarCalculo(datos(40, 3)),
                "No debería poder calcularse sin haberse conectado.");
    }

    /** UDP no avisa si el destino no existe: lo único que ocurre es que la respuesta nunca llega. */
    private static void debeAgotarElTiempoSiNadieResponde() {
        final AdaptadorClienteUdp cliente = nuevoCliente(300);
        final ClienteRedException excepcion = assertThrows(ClienteRedException.class,
                () -> cliente.conectar(new DestinoServidor("127.0.0.1", PUERTO_SIN_SERVIDOR)),
                "Sin servidor, la conexión debería fallar.");
        assertTrue(excepcion.getMessage() != null && !excepcion.getMessage().isBlank(),
                "El fallo debería traer un mensaje para el usuario.");
        assertTrue(!cliente.estaConectado(), "El cliente no debería quedar conectado.");
    }

    private static void debeFallarSiElHostNoExiste() {
        final AdaptadorClienteUdp cliente = nuevoCliente(300);
        assertThrows(ClienteRedException.class,
                () -> cliente.conectar(new DestinoServidor("host-que-no-existe.invalid", PUERTO)),
                "Un host que no se resuelve debería fallar.");
    }

    private static AdaptadorClienteUdp nuevoCliente(final int timeoutMs) {
        return new AdaptadorClienteUdp(new CanalUdp(timeoutMs), new ProtocoloUdpMapper());
    }

    private static DatosReunion datos(final int asistentes, final int porciones) {
        return new DatosReunion(new Asistentes(asistentes), new PorcionesPorPersona(porciones));
    }

    /** Servidor UDP mínimo que contesta cada datagrama según el protocolo de porciones. */
    private static final class ServidorDePrueba implements AutoCloseable {
        private final DatagramSocket socket;
        private final List<String> recibidos = new CopyOnWriteArrayList<>();

        ServidorDePrueba(final int puerto) throws SocketException {
            socket = new DatagramSocket(puerto);
            final Thread hilo = new Thread(this::atender, "servidor-de-prueba");
            hilo.setDaemon(true);
            hilo.start();
        }

        private void atender() {
            final byte[] buffer = new byte[2048];
            while (!socket.isClosed()) {
                try {
                    final DatagramPacket peticion = new DatagramPacket(buffer, buffer.length);
                    socket.receive(peticion);
                    final String texto =
                            new String(peticion.getData(), 0, peticion.getLength(), StandardCharsets.UTF_8);
                    recibidos.add(texto);
                    final byte[] respuesta = responder(texto).getBytes(StandardCharsets.UTF_8);
                    socket.send(new DatagramPacket(
                            respuesta, respuesta.length, peticion.getAddress(), peticion.getPort()));
                } catch (final IOException excepcion) {
                    return;
                }
            }
        }

        private static String responder(final String texto) {
            if ("CONECTAR".equals(texto)) {
                return "CONECTADO_OK;Servidor de prueba";
            }
            if ("DESCONECTAR".equals(texto)) {
                return "DESCONECTADO_OK;Sesión finalizada";
            }
            final String[] partes = texto.split(";");
            final long asistentes = Long.parseLong(partes[1]);
            if (asistentes == 13L) {
                return "ERROR;motivo de prueba";
            }
            final long total = asistentes * Long.parseLong(partes[2]);
            final long conReserva = total + (total * 10L + 99L) / 100L;
            return "OK_CALCULO;" + total + ";" + conReserva + ";Reunión mediana;mensaje de prueba";
        }

        void esperarMensajes(final int cantidad) throws InterruptedException {
            final long limite = System.currentTimeMillis() + 2000L;
            while (recibidos.size() < cantidad && System.currentTimeMillis() < limite) {
                Thread.sleep(20L);
            }
        }

        @Override
        public void close() {
            socket.close();
        }
    }
}
