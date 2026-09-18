package com.mauricio.porciones;

import static com.mauricio.porciones.Verificador.assertEquals;
import static com.mauricio.porciones.Verificador.assertThrows;
import static com.mauricio.porciones.Verificador.assertTrue;

import com.mauricio.porciones.dominio.enums.ClasificacionReunion;
import com.mauricio.porciones.dominio.enums.TipoRespuesta;
import com.mauricio.porciones.dominio.excepciones.AsistentesIncorrectosException;
import com.mauricio.porciones.dominio.excepciones.DestinatarioIncorrectoException;
import com.mauricio.porciones.dominio.excepciones.EstimacionInvalidaException;
import com.mauricio.porciones.dominio.excepciones.PorcionesIncorrectasException;
import com.mauricio.porciones.dominio.excepciones.PuertoIncorrectoException;
import com.mauricio.porciones.dominio.excepciones.RespuestaInvalidaException;
import com.mauricio.porciones.dominio.excepciones.ResultadoInvalidoException;
import com.mauricio.porciones.dominio.modelos.Estimacion;
import com.mauricio.porciones.dominio.modelos.RespuestaCliente;
import com.mauricio.porciones.dominio.modelos.Resultado;
import com.mauricio.porciones.dominio.vo.Asistentes;
import com.mauricio.porciones.dominio.vo.Destinatario;
import com.mauricio.porciones.dominio.vo.PorcionesPorPersona;
import com.mauricio.porciones.dominio.vo.PuertoRed;

/**
 * Pruebas del dominio del servidor: objetos de valor, regla de cálculo y respuestas.
 * Se ejecutan con: java -cp ... com.mauricio.porciones.DominioTest
 */
public final class DominioTest {

    private DominioTest() {
    }

    public static void main(final String[] args) {
        debeMultiplicarPersonasPorPorciones();
        debeUsarElSingularConUnaSolaPorcion();
        debeRechazarValoresNoPositivos();
        debeRechazarValoresSobreElLimite();
        noDebeDesbordarseConLaEntradaMaxima();
        debeAgregarElMargenDeReserva();
        noDebeRedondearCuandoLaReservaEsExacta();
        debeClasificarLaReunion();
        debeExigirLosDatosDeLaEstimacion();
        debeValidarElResultado();
        debeValidarElDestinatarioYElPuerto();
        debeConstruirLasRespuestasAlCliente();
        System.out.println("Todas las pruebas de DominioTest pasaron.");
    }

    private static void debeMultiplicarPersonasPorPorciones() {
        final Resultado resultado = estimar(25, 3).calcular();
        assertEquals(75L, resultado.getTotal(), "El total de porciones no coincide.");
        assertEquals(83L, resultado.getTotalConReserva(), "El total con reserva no coincide.");
        assertEquals(ClasificacionReunion.MEDIANA, resultado.getClasificacion(),
                "Veinticinco personas es una reunión mediana.");
        assertTrue(resultado.getMensaje().contains("75 porciones para 25 personas"),
                "El mensaje no describe el cálculo. Mensaje: " + resultado.getMensaje());
        assertTrue(!resultado.getMensaje().contains(";"),
                "El mensaje no puede contener el separador del protocolo.");
    }

    private static void debeUsarElSingularConUnaSolaPorcion() {
        final Resultado resultado = estimar(1, 1).calcular();
        assertEquals(1L, resultado.getTotal(), "El total para una persona no coincide.");
        assertTrue(resultado.getMensaje().contains("1 porción para 1 persona,"),
                "El mensaje debería usar el singular. Mensaje: " + resultado.getMensaje());
    }

    private static void debeRechazarValoresNoPositivos() {
        final AsistentesIncorrectosException sinPersonas = assertThrows(
                AsistentesIncorrectosException.class, () -> new Asistentes(0),
                "Cero personas debería rechazarse.");
        assertTrue(sinPersonas.getMessage().contains("mayor que 0"), "Falta el motivo del rechazo.");
        assertThrows(PorcionesIncorrectasException.class, () -> new PorcionesPorPersona(-2),
                "Un valor negativo de porciones debería rechazarse.");
    }

    private static void debeRechazarValoresSobreElLimite() {
        assertThrows(AsistentesIncorrectosException.class, () -> new Asistentes(Asistentes.MAXIMO + 1),
                "Superar el máximo de personas debería rechazarse.");
        assertThrows(PorcionesIncorrectasException.class,
                () -> new PorcionesPorPersona(PorcionesPorPersona.MAXIMO + 1),
                "Superar el máximo de porciones por persona debería rechazarse.");
    }

    private static void noDebeDesbordarseConLaEntradaMaxima() {
        final Resultado resultado = estimar(Asistentes.MAXIMO, PorcionesPorPersona.MAXIMO).calcular();
        assertEquals(10_000_000L, resultado.getTotal(),
                "El total máximo debería calcularse sin desbordamiento.");
        assertEquals(11_000_000L, resultado.getTotalConReserva(),
                "La reserva del caso máximo debería ser exacta.");
        assertEquals(ClasificacionReunion.MASIVA, resultado.getClasificacion(),
                "Cien mil personas es un evento masivo.");
    }

    private static void debeAgregarElMargenDeReserva() {
        assertEquals(83L, Estimacion.conReserva(75L), "La reserva debería redondear hacia arriba.");
        assertEquals(11L, Estimacion.conReserva(10L), "La reserva de 10 porciones no coincide.");
        assertEquals(132L, Estimacion.conReserva(120L), "La reserva del ejemplo del README no coincide.");
        assertEquals(0L, Estimacion.conReserva(0L), "Sin porciones no debería haber reserva.");
    }

    /** Con coma flotante, Math.ceil(200 * 1.1) devuelve 221 porque el producto es 220.00000000000003. */
    private static void noDebeRedondearCuandoLaReservaEsExacta() {
        assertEquals(220L, Estimacion.conReserva(200L),
                "Una reserva exacta no debería redondear hacia arriba.");
        assertEquals(1_100L, Estimacion.conReserva(1_000L),
                "La reserva de 1.000 porciones debería ser exacta.");
    }

    private static void debeClasificarLaReunion() {
        assertEquals(ClasificacionReunion.PEQUENA, ClasificacionReunion.deAsistentes(10),
                "Diez personas es una reunión pequeña.");
        assertEquals(ClasificacionReunion.MEDIANA, ClasificacionReunion.deAsistentes(11),
                "Once personas es una reunión mediana.");
        assertEquals(ClasificacionReunion.GRANDE, ClasificacionReunion.deAsistentes(200),
                "Doscientas personas es una reunión grande.");
        assertEquals(ClasificacionReunion.MASIVA, ClasificacionReunion.deAsistentes(201),
                "Más de doscientas personas es un evento masivo.");
        assertEquals("Evento masivo", ClasificacionReunion.MASIVA.getDescripcion(),
                "La descripción de la clasificación no coincide.");
    }

    private static void debeExigirLosDatosDeLaEstimacion() {
        assertThrows(EstimacionInvalidaException.class,
                () -> new Estimacion(null, new PorcionesPorPersona(2)),
                "Una estimación sin personas debería rechazarse.");
        assertThrows(EstimacionInvalidaException.class,
                () -> new Estimacion(new Asistentes(2), null),
                "Una estimación sin porciones debería rechazarse.");
    }

    private static void debeValidarElResultado() {
        assertThrows(ResultadoInvalidoException.class,
                () -> new Resultado(0L, 0L, ClasificacionReunion.PEQUENA, "mensaje"),
                "Un total de cero no es un resultado válido.");
        assertThrows(ResultadoInvalidoException.class,
                () -> new Resultado(10L, 9L, ClasificacionReunion.PEQUENA, "mensaje"),
                "El total con reserva no puede ser menor que el total.");
        assertThrows(ResultadoInvalidoException.class,
                () -> new Resultado(10L, 11L, null, "mensaje"),
                "La clasificación es obligatoria.");
        assertThrows(ResultadoInvalidoException.class,
                () -> new Resultado(10L, 11L, ClasificacionReunion.PEQUENA, " "),
                "El mensaje es obligatorio.");
        assertEquals("75", Resultado.formatear(75L), "Dos cifras no llevan separador de miles.");
        assertEquals(6, Resultado.formatear(10_000L).length(),
                "Cinco cifras deberían incluir un separador de miles.");
    }

    private static void debeValidarElDestinatarioYElPuerto() {
        final Destinatario destinatario = new Destinatario(" 127.0.0.1 ", 5000);
        assertEquals("127.0.0.1:5000", destinatario.endpoint(), "El endpoint debería quedar sin espacios.");
        assertThrows(DestinatarioIncorrectoException.class, () -> new Destinatario(" ", 5000),
                "Una IP vacía debería rechazarse.");
        assertThrows(PuertoIncorrectoException.class, () -> new Destinatario("127.0.0.1", 0),
                "El puerto 0 del destinatario debería rechazarse.");
        assertThrows(PuertoIncorrectoException.class, () -> new PuertoRed(1023),
                "Un puerto reservado debería rechazarse.");
        assertThrows(PuertoIncorrectoException.class, () -> new PuertoRed(65536),
                "Un puerto fuera de rango debería rechazarse.");
        assertEquals(9008, new PuertoRed(PuertoRed.POR_DEFECTO).valor(), "El puerto por defecto es 9008.");
    }

    private static void debeConstruirLasRespuestasAlCliente() {
        final Destinatario destinatario = new Destinatario("127.0.0.1", 5000);
        final Resultado resultado = estimar(40, 3).calcular();

        final RespuestaCliente exito = RespuestaCliente.calculoExitoso(destinatario, resultado);
        assertEquals(TipoRespuesta.OK_CALCULO, exito.getTipo(), "El tipo de la respuesta no coincide.");
        assertEquals(120L, exito.getResultado().getTotal(), "La respuesta debería llevar el resultado.");

        assertEquals(TipoRespuesta.ERROR, RespuestaCliente.error(destinatario, "motivo").getTipo(),
                "El tipo de la respuesta de error no coincide.");
        assertThrows(RespuestaInvalidaException.class,
                () -> RespuestaCliente.calculoExitoso(destinatario, null),
                "Una respuesta de cálculo sin resultado debería rechazarse.");
        assertThrows(RespuestaInvalidaException.class, () -> RespuestaCliente.error(null, "motivo"),
                "Una respuesta sin destinatario debería rechazarse.");
    }

    private static Estimacion estimar(final int asistentes, final int porcionesPorPersona) {
        return new Estimacion(new Asistentes(asistentes), new PorcionesPorPersona(porcionesPorPersona));
    }
}
