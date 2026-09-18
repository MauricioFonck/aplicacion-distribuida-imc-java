package com.mauricio.porciones.dominio.enums;

/**
 * Estados de la sesión del cliente. En UDP la conexión es lógica: se considera establecida cuando
 * el servidor responde al saludo {@code CONECTAR}.
 */
public enum EstadoConexion {
    DESCONECTADO,
    CONECTANDO,
    CONECTADO
}
