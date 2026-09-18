package com.mauricio.porciones;

/** Comprobaciones mínimas para las pruebas, sin dependencias externas. */
final class Verificador {

    private Verificador() {
    }

    static void assertTrue(final boolean condicion, final String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    static void assertEquals(final Object esperado, final Object obtenido, final String mensaje) {
        assertTrue(esperado == null ? obtenido == null : esperado.equals(obtenido),
                mensaje + " Esperado: " + esperado + ", obtenido: " + obtenido);
    }

    /** Verifica que la acción lance una excepción del tipo indicado y la devuelve. */
    static <T extends Throwable> T assertThrows(
            final Class<T> tipo, final Accion accion, final String mensaje) {
        try {
            accion.ejecutar();
        } catch (final Throwable lanzada) {
            if (tipo.isInstance(lanzada)) {
                return tipo.cast(lanzada);
            }
            throw new AssertionError(mensaje + " Se lanzó " + lanzada.getClass().getSimpleName()
                    + " en lugar de " + tipo.getSimpleName() + ".", lanzada);
        }
        throw new AssertionError(mensaje + " No se lanzó " + tipo.getSimpleName() + ".");
    }

    /** Acción que puede lanzar excepciones comprobadas, como las de red. */
    @FunctionalInterface
    interface Accion {
        void ejecutar() throws Exception;
    }
}
