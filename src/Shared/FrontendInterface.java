package Shared;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

// Creating Remote interface for our application 
public interface FrontendInterface extends Remote, Serializable {
    boolean addPlace(String codigoPostal, String localidate) throws RemoteException, MalformedURLException, NotBoundException;
    HashMap<String,String> allPlaces() throws RemoteException, NotBoundException, MalformedURLException;
    void updatePlace(String codigoPostal, String localidate) throws RemoteException;
    boolean delPlace(int index) throws RemoteException;
}