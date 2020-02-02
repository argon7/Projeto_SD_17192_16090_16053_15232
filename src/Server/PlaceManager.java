package Server;
import Shared.PlaceManagerInterface;
import Shared.Place;


import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class PlaceManager extends UnicastRemoteObject implements PlaceManagerInterface {
    private static final long serialVersionUID = 1L;
    private static ArrayList<Place> array = new ArrayList<>();
    public PlaceManager() throws RemoteException {
        super();
    }

    public boolean addPlace(Place p) {
        System.out.println("place:"+p.getLocality()+"adicionado");
        array.add(p);
        return true;
    }

    public ArrayList<Place> allPlaces() {
        return array;
    }

    public boolean delPlace(int index) {
        array.remove(index);
        return true;
    }


}