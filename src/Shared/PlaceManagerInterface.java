package Shared;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface PlaceManagerInterface extends Remote, Serializable {
    void addPlace(Place p) throws IOException;

    ArrayList<Place> allPlaces() throws RemoteException;

    boolean delPlace(int index) throws IOException;

    void slavesDEL(int index) throws IOException;

    void slavesADD(Place placetoadd) throws IOException;
}