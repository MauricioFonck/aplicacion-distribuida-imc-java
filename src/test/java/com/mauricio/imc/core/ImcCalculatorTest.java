package com.mauricio.imc.core;

/**
 * Pruebas ejecutables sin dependencias externas.
 * Se ejecutan con: java -cp ... com.mauricio.imc.core.ImcCalculatorTest
 */
public final class ImcCalculatorTest {
    private ImcCalculatorTest() {
    }

    public static void main(String[] args) {
        shouldCalculateHealthyBmi();
        shouldRejectNonPositiveValues();
        shouldRejectNonFiniteValues();
        shouldFormatWithTwoDecimals();
        System.out.println("Todas las pruebas de ImcCalculator pasaron.");
    }

    private static void shouldCalculateHealthyBmi() {
        ImcResult result = ImcCalculator.calculate(70.0f, 1.75f);
        assertTrue(result.isValid(), "El resultado debería ser válido.");
        assertBetween(22.85f, result.getValue(), 22.87f, "El IMC saludable no coincide.");
        assertTrue(result.getMessage().contains("saludable"), "La clasificación debería ser saludable.");
    }

    private static void shouldRejectNonPositiveValues() {
        ImcResult result = ImcCalculator.calculate(0.0f, 1.75f);
        assertTrue(!result.isValid(), "El peso cero debería rechazarse.");
        assertTrue(result.getMessage().contains("mayores que 0"), "Falta el mensaje de validación.");
    }

    private static void shouldRejectNonFiniteValues() {
        ImcResult result = ImcCalculator.calculate(Float.NaN, 1.75f);
        assertTrue(!result.isValid(), "NaN debería rechazarse.");
    }

    private static void shouldFormatWithTwoDecimals() {
        assertTrue("22.86".equals(ImcCalculator.format(22.85714f)),
                "El formato debería usar dos decimales.");
    }

    private static void assertBetween(float minimum, float actual, float maximum, String message) {
        assertTrue(actual >= minimum && actual <= maximum, message + " Valor: " + actual);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
