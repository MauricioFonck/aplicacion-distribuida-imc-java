package com.mauricio.porciones.remoto;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** Contrato remoto del cálculo de porciones. */
public interface PorcionesRemote extends Remote {
    String conectar(String cliente) throws RemoteException;
    PorcionesResponse calcular(int personas, int porcionesPorPersona) throws RemoteException;
    String desconectar(String cliente) throws RemoteException;
}
