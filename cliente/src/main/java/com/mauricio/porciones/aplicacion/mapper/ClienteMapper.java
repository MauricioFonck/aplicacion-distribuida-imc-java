package com.mauricio.porciones.aplicacion.mapper;

import com.mauricio.porciones.aplicacion.dto.CalcularPorcionesCommand;
import com.mauricio.porciones.aplicacion.dto.ConectarCommand;
import com.mauricio.porciones.dominio.modelos.DatosReunion;
import com.mauricio.porciones.dominio.vo.Asistentes;
import com.mauricio.porciones.dominio.vo.DestinoServidor;
import com.mauricio.porciones.dominio.vo.PorcionesPorPersona;
import java.util.Objects;

/** Mapper de la capa de aplicación: convierte los comandos de la interfaz a objetos del dominio. */
public final class ClienteMapper {

    public DestinoServidor toDestino(final ConectarCommand comando) {
        Objects.requireNonNull(comando, "El comando de conexión es obligatorio.");
        return new DestinoServidor(comando.host(), comando.puerto());
    }

    public DatosReunion toDatos(final CalcularPorcionesCommand comando) {
        Objects.requireNonNull(comando, "El comando de cálculo es obligatorio.");
        return new DatosReunion(
                new Asistentes(comando.asistentes()), new PorcionesPorPersona(comando.porcionesPorPersona()));
    }
}
