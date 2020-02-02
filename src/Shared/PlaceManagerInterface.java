package Shared;

import Server.PlaceManager;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface PlaceManagerInterface extends Remote, Serializable {
    boolean addPlace(Place p) throws IOException;
    ArrayList<Place> allPlaces() throws RemoteException;
    boolean delPlace(int index) throws IOException;
    boolean slavesDEL(int index) throws IOException;
    boolean slavesADD(Place placetoadd) throws IOException;
}