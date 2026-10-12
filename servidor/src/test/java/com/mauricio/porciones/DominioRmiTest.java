package com.mauricio.porciones;
import com.mauricio.porciones.dominio.Estimacion;
import com.mauricio.porciones.remoto.PorcionesResponse;
public final class DominioRmiTest {
    public static void main(String[] args) {
        PorcionesResponse ok = Estimacion.calcular(40, 3);
        check(ok.isValid() && ok.getTotal() == 120 && ok.getTotalConReserva() == 132, "Cálculo normal incorrecto");
        check(!Estimacion.calcular(0, 3).isValid(), "Debe rechazar personas inválidas");
        check(Estimacion.calcular(100000, 100).getTotal() == 10000000L, "Debe usar long");
        System.out.println("La prueba de dominio RMI pasó.");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
