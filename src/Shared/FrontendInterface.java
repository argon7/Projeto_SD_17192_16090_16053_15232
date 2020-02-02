package Shared;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface FrontendInterface extends Remote, Serializable {
    boolean addPlace(String codigoPostal, String localidate) throws RemoteException, MalformedURLException,
            NotBoundException;

    HashMap<String, String> allPlaces() throws RemoteException, NotBoundException, MalformedURLException;

    void updatePlace() throws RemoteException;

    boolean delPlace(int index) throws IOException;
}