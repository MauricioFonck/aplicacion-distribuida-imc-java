package com.mauricio.porciones;

import static com.mauricio.porciones.Verificador.assertEquals;
import static com.mauricio.porciones.Verificador.assertThrows;
import static com.mauricio.porciones.Verificador.assertTrue;

import com.mauricio.porciones.adaptadores.red.AdaptadorControlServidorRed;
import com.mauricio.porciones.adaptadores.red.AdaptadorNotificacionEvento;
import com.mauricio.porciones.adaptadores.red.AdaptadorSalidaUdp;
import com.mauricio.porciones.adaptadores.red.CanalUdp;
import com.mauricio.porciones.adaptadores.red.mapper.UdpNetworkMapper;
import com.mauricio.porciones.aplicacion.excepciones.ServidorRedException;
import com.mauricio.porciones.aplicacion.mapper.EstimacionMapper;
import com.mauricio.porciones.aplicacion.mapper.PeticionMapper;
import com.mauricio.porciones.aplicacion.servicios.GestionarServidorService;
import com.mauricio.porciones.aplicacion.servicios.ProcesarPeticionUdpService;
import com.mauricio.porciones.entrypoint.udp.ReceptorPeticionesUdp;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * Prueba de integración: arma el servidor completo (sin interfaz gráfica), lo pone a escuchar en
 * un puerto UDP real de la máquina local y le envía datagramas como lo haría un cliente.
 */
public final class RedUdpIntegracionTest {

    private static final int PUERTO = 19008;
    private static final int TIMEOUT_MS = 3000;

    private RedUdpIntegracionTest() {
    }

    public static void main(final String[] args) throws IOException, ServidorRedException {
        final CanalUdp canalUdp = new CanalUdp();
        final AdaptadorNotificacionEvento notificador = new AdaptadorNotificacionEvento();
        final ProcesarPeticionUdpService procesarPeticion = new ProcesarPeticionUdpService(
                new AdaptadorSalidaUdp(canalUdp, new UdpNetworkMapper(), notificador),
                notificador, new PeticionMapper(), new EstimacionMapper());
        final ReceptorPeticionesUdp receptor = new ReceptorPeticionesUdp(canalUdp, procesarPeticion, notificador);
        final GestionarServidorService servidor = new GestionarServidorService(
                new AdaptadorControlServidorRed(canalUdp, receptor::iniciar, receptor::detener), notificador);

        servidor.iniciarServidor(PUERTO);
        try (DatagramSocket cliente = new DatagramSocket()) {
            cliente.setSoTimeout(TIMEOUT_MS);
            assertTrue(servidor.estaCorriendo(), "El servidor debería estar en línea.");
            assertEquals(PUERTO, servidor.getPuertoActual(), "El puerto activo no coincide.");

            assertTrue(intercambiar(cliente, "CONECTAR").startsWith("CONECTADO_OK;"),
                    "El saludo debería confirmarse.");

            final String[] calculo = intercambiar(cliente, "CALCULAR;40;3").split(";", -1);
            assertEquals("OK_CALCULO", calculo[0], "Se esperaba un cálculo exitoso.");
            assertEquals("120", calculo[1], "El total de porciones no coincide.");
            assertEquals("132", calculo[2], "El total con reserva no coincide.");
            assertEquals("Reunión mediana", calculo[3], "La clasificación no coincide.");

            // Un mismo socket cliente puede enviar varias peticiones: cada datagrama es independiente.
            assertTrue(intercambiar(cliente, "CALCULAR;100000;100").startsWith("OK_CALCULO;10000000;11000000;"),
                    "El caso máximo debería calcularse sin desbordamiento.");

            assertTrue(intercambiar(cliente, "CALCULAR;0;3").startsWith("ERROR;"),
                    "Cero personas debería rechazarse.");
            assertTrue(intercambiar(cliente, "CALCULAR;abc;3").startsWith("ERROR;"),
                    "Un valor no numérico debería rechazarse.");
            assertTrue(intercambiar(cliente, "HOLA").startsWith("ERROR;"),
                    "Un comando desconocido debería rechazarse.");

            // Una petición rechazada no afecta a las siguientes.
            assertTrue(intercambiar(cliente, "CALCULAR;25;3").startsWith("OK_CALCULO;75;83;"),
                    "El servidor debería seguir atendiendo después de un error.");

            assertTrue(intercambiar(cliente, "DESCONECTAR").startsWith("DESCONECTADO_OK;"),
                    "La desconexión debería confirmarse.");

            debeAtenderAVariosClientes();
        } finally {
            servidor.detenerServidor();
        }

        assertTrue(!servidor.estaCorriendo(), "El servidor debería quedar detenido.");
        assertTrue(!receptor.isEscuchando(), "El receptor debería dejar de escuchar.");
        debeFallarSiElPuertoEstaOcupado();
        System.out.println("Todas las pruebas de RedUdpIntegracionTest pasaron.");
    }

    /** Dos sockets distintos reciben cada uno su propia respuesta por el mismo socket del servidor. */
    private static void debeAtenderAVariosClientes() throws IOException {
        try (DatagramSocket primero = new DatagramSocket(); DatagramSocket segundo = new DatagramSocket()) {
            primero.setSoTimeout(TIMEOUT_MS);
            segundo.setSoTimeout(TIMEOUT_MS);
            enviar(primero, "CALCULAR;10;2");
            enviar(segundo, "CALCULAR;300;1");
            assertTrue(recibir(segundo).startsWith("OK_CALCULO;300;330;Evento masivo;"),
                    "El segundo cliente debería recibir su propio resultado.");
            assertTrue(recibir(primero).startsWith("OK_CALCULO;20;22;Reunión pequeña;"),
                    "El primer cliente debería recibir su propio resultado.");
        }
    }

    private static void debeFallarSiElPuertoEstaOcupado() throws IOException {
        try (DatagramSocket ocupado = new DatagramSocket(PUERTO)) {
            final CanalUdp canalUdp = new CanalUdp();
            final GestionarServidorService servidor = new GestionarServidorService(
                    new AdaptadorControlServidorRed(canalUdp, () -> { }, () -> { }),
                    new AdaptadorNotificacionEvento());
            assertThrows(IllegalStateException.class, () -> {
                try {
                    servidor.iniciarServidor(ocupado.getLocalPort());
                } catch (final ServidorRedException excepcion) {
                    throw new IllegalStateException(excepcion);
                }
            }, "Iniciar en un puerto ocupado debería fallar con ServidorRedException.");
            assertTrue(!servidor.estaCorriendo(), "El servidor no debería quedar en línea.");
        }
    }

    private static String intercambiar(final DatagramSocket cliente, final String mensaje) throws IOException {
        enviar(cliente, mensaje);
        return recibir(cliente);
    }

    private static void enviar(final DatagramSocket cliente, final String mensaje) throws IOException {
        final byte[] datos = mensaje.getBytes(StandardCharsets.UTF_8);
        cliente.send(new DatagramPacket(datos, datos.length, InetAddress.getLoopbackAddress(), PUERTO));
    }

    private static String recibir(final DatagramSocket cliente) throws IOException {
        final DatagramPacket respuesta = new DatagramPacket(new byte[2048], 2048);
        cliente.receive(respuesta);
        return new String(respuesta.getData(), 0, respuesta.getLength(), StandardCharsets.UTF_8);
    }
}
