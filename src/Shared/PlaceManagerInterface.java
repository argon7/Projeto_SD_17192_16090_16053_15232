package Shared;

import java.lang.reflect.Array;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface PlaceManagerInterface extends Remote {
    boolean addPlace(Place p) throws RemoteException;
    ArrayList<Place> allPlaces() throws RemoteException;
    boolean delPlace(int index) throws RemoteException;


}