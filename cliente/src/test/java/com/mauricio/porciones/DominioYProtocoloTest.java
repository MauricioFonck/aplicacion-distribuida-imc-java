package com.mauricio.porciones;

import static com.mauricio.porciones.Verificador.assertEquals;
import static com.mauricio.porciones.Verificador.assertThrows;
import static com.mauricio.porciones.Verificador.assertTrue;

import com.mauricio.porciones.adaptadores.red.ProtocoloUdpMapper;
import com.mauricio.porciones.aplicacion.dto.CalcularPorcionesCommand;
import com.mauricio.porciones.aplicacion.dto.ConectarCommand;
import com.mauricio.porciones.aplicacion.mapper.ClienteMapper;
import com.mauricio.porciones.dominio.excepciones.AsistentesIncorrectosException;
import com.mauricio.porciones.dominio.excepciones.DestinoIncorrectoException;
import com.mauricio.porciones.dominio.excepciones.PorcionesIncorrectasException;
import com.mauricio.porciones.dominio.excepciones.RespuestaServidorException;
import com.mauricio.porciones.dominio.modelos.DatosReunion;
import com.mauricio.porciones.dominio.modelos.ResultadoPorciones;
import com.mauricio.porciones.dominio.vo.Asistentes;
import com.mauricio.porciones.dominio.vo.DestinoServidor;
import com.mauricio.porciones.dominio.vo.PorcionesPorPersona;

/**
 * Pruebas del dominio del cliente y de la traducción del protocolo UDP.
 * Se ejecutan con: java -cp ... com.mauricio.porciones.DominioYProtocoloTest
 */
public final class DominioYProtocoloTest {

    private DominioYProtocoloTest() {
    }

    public static void main(final String[] args) {
        debeValidarLosDatosDeLaReunion();
        debeValidarElDestino();
        debeConvertirLosComandos();
        debeSerializarLaPeticion();
        debeValidarElSaludo();
        debeInterpretarElCalculo();
        debeConservarUnMensajeConSeparadores();
        debeInformarElErrorDelServidor();
        debeRechazarRespuestasMalFormadas();
        System.out.println("Todas las pruebas de DominioYProtocoloTest pasaron.");
    }

    private static void debeValidarLosDatosDeLaReunion() {
        assertThrows(AsistentesIncorrectosException.class, () -> new Asistentes(0),
                "Cero personas debería rechazarse.");
        assertThrows(AsistentesIncorrectosException.class, () -> new Asistentes(Asistentes.MAXIMO + 1),
                "Superar el máximo de personas debería rechazarse.");
        assertThrows(PorcionesIncorrectasException.class, () -> new PorcionesPorPersona(0),
                "Cero porciones debería rechazarse.");
        assertThrows(PorcionesIncorrectasException.class,
                () -> new PorcionesPorPersona(PorcionesPorPersona.MAXIMO + 1),
                "Superar el máximo de porciones debería rechazarse.");
        assertThrows(NullPointerException.class, () -> new DatosReunion(null, new PorcionesPorPersona(1)),
                "Los datos de la reunión exigen la cantidad de personas.");
    }

    private static void debeValidarElDestino() {
        assertEquals("localhost:9008", new DestinoServidor(" localhost ", 9008).endpoint(),
                "El endpoint debería quedar sin espacios.");
        assertThrows(DestinoIncorrectoException.class, () -> new DestinoServidor(" ", 9008),
                "Un host vacío debería rechazarse.");
        assertThrows(DestinoIncorrectoException.class, () -> new DestinoServidor("localhost", 80),
                "Un puerto reservado debería rechazarse.");
        assertThrows(DestinoIncorrectoException.class, () -> new DestinoServidor("localhost", 70000),
                "Un puerto fuera de rango debería rechazarse.");
    }

    private static void debeConvertirLosComandos() {
        final ClienteMapper mapper = new ClienteMapper();
        assertEquals(new DestinoServidor("localhost", 9008), mapper.toDestino(new ConectarCommand("localhost", 9008)),
                "El destino convertido no coincide.");
        final DatosReunion datos = mapper.toDatos(new CalcularPorcionesCommand(40, 3));
        assertEquals(40, datos.asistentes().valor(), "La cantidad de personas no coincide.");
        assertEquals(3, datos.porcionesPorPersona().valor(), "Las porciones por persona no coinciden.");
        assertThrows(AsistentesIncorrectosException.class,
                () -> mapper.toDatos(new CalcularPorcionesCommand(-1, 3)),
                "El mapper debería propagar la validación del dominio.");
    }

    private static void debeSerializarLaPeticion() {
        final DatosReunion datos = new DatosReunion(new Asistentes(100_000), new PorcionesPorPersona(100));
        assertEquals("CALCULAR;100000;100", new ProtocoloUdpMapper().serializarCalculo(datos),
                "Los números deben viajar sin separadores de miles.");
    }

    private static void debeValidarElSaludo() {
        final ProtocoloUdpMapper protocolo = new ProtocoloUdpMapper();
        protocolo.validarConexion("CONECTADO_OK;Servidor listo");
        assertThrows(RespuestaServidorException.class, () -> protocolo.validarConexion("ERROR;ocupado"),
                "Una respuesta distinta de CONECTADO_OK debería rechazarse.");
        assertThrows(RespuestaServidorException.class, () -> protocolo.validarConexion(null),
                "Una respuesta nula debería rechazarse.");
    }

    private static void debeInterpretarElCalculo() {
        final ResultadoPorciones resultado = new ProtocoloUdpMapper()
                .parsearCalculo("OK_CALCULO;120;132;Reunión mediana;120 porciones para 40 personas.");
        assertEquals(120L, resultado.total(), "El total no coincide.");
        assertEquals(132L, resultado.totalConReserva(), "El total con reserva no coincide.");
        assertEquals("Reunión mediana", resultado.clasificacion(), "La clasificación no coincide.");
        assertEquals("120 porciones para 40 personas.", resultado.mensaje(), "El mensaje no coincide.");
    }

    private static void debeConservarUnMensajeConSeparadores() {
        final ResultadoPorciones resultado =
                new ProtocoloUdpMapper().parsearCalculo("OK_CALCULO;20;22;Reunión pequeña;uno; dos");
        assertEquals("uno; dos", resultado.mensaje(), "El mensaje final no debería partirse.");
    }

    private static void debeInformarElErrorDelServidor() {
        final RespuestaServidorException excepcion = assertThrows(RespuestaServidorException.class,
                () -> new ProtocoloUdpMapper().parsearCalculo("ERROR;Cantidad de personas incorrecta"),
                "Un ERROR del servidor debería convertirse en excepción.");
        assertTrue(excepcion.getMessage().contains("Cantidad de personas incorrecta"),
                "La excepción debería conservar el motivo enviado por el servidor.");
    }

    private static void debeRechazarRespuestasMalFormadas() {
        final ProtocoloUdpMapper protocolo = new ProtocoloUdpMapper();
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo(" "),
                "Una respuesta vacía debería rechazarse.");
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo("OK_CALCULO;120;132"),
                "Una respuesta con campos de menos debería rechazarse.");
        assertThrows(RespuestaServidorException.class,
                () -> protocolo.parsearCalculo("OTRA_COSA;120;132;Reunión mediana;mensaje"),
                "Un tipo de respuesta desconocido debería rechazarse.");
        assertThrows(RespuestaServidorException.class,
                () -> protocolo.parsearCalculo("OK_CALCULO;muchas;132;Reunión mediana;mensaje"),
                "Un total no numérico debería rechazarse.");
        assertThrows(RespuestaServidorException.class,
                () -> protocolo.parsearCalculo("OK_CALCULO;120;100;Reunión mediana;mensaje"),
                "Una reserva menor que el total debería rechazarse.");
    }
}
