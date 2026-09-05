package com.mauricio.porciones.core;

/**
 * Pruebas del cálculo de porciones, ejecutables sin dependencias externas.
 * Se ejecutan con: java -cp ... com.mauricio.porciones.core.PorcionesCalculatorTest
 */
public final class PorcionesCalculatorTest {
    private PorcionesCalculatorTest() {
    }

    public static void main(String[] args) {
        shouldMultiplyAttendeesByPortions();
        shouldHandleASinglePerson();
        shouldRejectNonPositiveValues();
        shouldRejectValuesOverTheLimit();
        shouldNotOverflowWithTheLargestInput();
        shouldAddTheReserveMargin();
        shouldClassifyTheMeeting();
        shouldFormatIntegerValues();
        System.out.println("Todas las pruebas de PorcionesCalculator pasaron.");
    }

    private static void shouldMultiplyAttendeesByPortions() {
        PorcionesResult result = PorcionesCalculator.calculate(25, 3);
        assertTrue(result.isValid(), "El resultado debería ser válido.");
        assertEquals(75L, result.getValue(), "El total de porciones no coincide.");
        assertTrue(result.getMessage().contains("mediana"),
                "La clasificación debería ser de reunión mediana.");
    }

    private static void shouldHandleASinglePerson() {
        PorcionesResult result = PorcionesCalculator.calculate(1, 1);
        assertTrue(result.isValid(), "Una persona con una porción es una entrada válida.");
        assertEquals(1L, result.getValue(), "El total para una persona no coincide.");
        assertTrue(result.getMessage().contains("1 porción"),
                "El mensaje debería usar el singular. Mensaje: " + result.getMessage());
    }

    private static void shouldRejectNonPositiveValues() {
        PorcionesResult sinPersonas = PorcionesCalculator.calculate(0, 3);
        assertTrue(!sinPersonas.isValid(), "Cero personas debería rechazarse.");
        assertTrue(sinPersonas.getMessage().contains("mayores que 0"),
                "Falta el mensaje de validación.");

        PorcionesResult sinPorciones = PorcionesCalculator.calculate(10, -2);
        assertTrue(!sinPorciones.isValid(), "Un valor negativo debería rechazarse.");
        assertEquals(0L, sinPorciones.getValue(), "Un resultado inválido no debería traer total.");
    }

    private static void shouldRejectValuesOverTheLimit() {
        PorcionesResult demasiadasPersonas =
                PorcionesCalculator.calculate(PorcionesCalculator.MAX_ASISTENTES + 1, 1);
        assertTrue(!demasiadasPersonas.isValid(),
                "Superar el máximo de personas debería rechazarse.");

        PorcionesResult demasiadasPorciones =
                PorcionesCalculator.calculate(10, PorcionesCalculator.MAX_PORCIONES_POR_PERSONA + 1);
        assertTrue(!demasiadasPorciones.isValid(),
                "Superar el máximo de porciones por persona debería rechazarse.");
    }

    private static void shouldNotOverflowWithTheLargestInput() {
        PorcionesResult result = PorcionesCalculator.calculate(
                PorcionesCalculator.MAX_ASISTENTES, PorcionesCalculator.MAX_PORCIONES_POR_PERSONA);
        assertTrue(result.isValid(), "La entrada máxima debería ser válida.");
        assertEquals(10_000_000L, result.getValue(),
                "El total máximo debería calcularse sin desbordamiento.");
    }

    private static void shouldAddTheReserveMargin() {
        assertEquals(83L, PorcionesCalculator.withReserve(75L),
                "La reserva debería redondear hacia arriba.");
        assertEquals(11L, PorcionesCalculator.withReserve(10L),
                "La reserva de 10 porciones no coincide.");
        assertEquals(0L, PorcionesCalculator.withReserve(0L),
                "Sin porciones no debería haber reserva.");
    }

    private static void shouldClassifyTheMeeting() {
        assertTrue("Reunión pequeña".equals(PorcionesCalculator.classify(10)),
                "Diez personas es una reunión pequeña.");
        assertTrue("Reunión mediana".equals(PorcionesCalculator.classify(11)),
                "Once personas es una reunión mediana.");
        assertTrue("Reunión grande".equals(PorcionesCalculator.classify(200)),
                "Doscientas personas es una reunión grande.");
        assertTrue("Evento masivo".equals(PorcionesCalculator.classify(201)),
                "Más de doscientas personas es un evento masivo.");
    }

    private static void shouldFormatIntegerValues() {
        assertTrue("75".equals(PorcionesCalculator.format(75L)),
                "Un valor de dos cifras no debería llevar separador.");
        String milValores = PorcionesCalculator.format(10_000L);
        assertTrue(milValores.length() == 6,
                "Un valor de cinco cifras debería incluir un separador de miles. Valor: " + milValores);
    }

    private static void assertEquals(long expected, long actual, String message) {
        assertTrue(expected == actual,
                message + " Esperado: " + expected + ", obtenido: " + actual);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
