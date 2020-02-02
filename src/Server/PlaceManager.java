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

    public boolean addPlace(Place p) throws IOException {
        System.out.println("place:"+p.getLocality()+"adicionado");
        array.add(p);
        // ADD PROPAGATION
        String messageHead ="-1@-1@ADD";
        String messageBody = "@"+p.getLocality()+"@"+p.getPostalCode();
        String propagateThisData = messageHead+messageBody;
        sendMessage("230.1.1.1", 5000, propagateThisData);
        return true;
    }

    public ArrayList<Place> allPlaces() {
        return array;
    }

    public boolean delPlace(int index) throws IOException {
        array.remove(index-1);
        // DEL PROPAGATION
        String messageHead ="-1@-1@DEL";
        String messageBody = "@"+(index-1);
        String propagateThisData = messageHead+messageBody;
        sendMessage("230.1.1.1", 5000, propagateThisData);


        return true;

    }

    public static void sendMessage(String address, Integer port, String message) throws IOException {
        byte[] m = message.getBytes();

        DatagramSocket socket = new DatagramSocket();
        InetAddress host = InetAddress.getByName(address);

        DatagramPacket datagram = new DatagramPacket(m, m.length, host, port);

        socket.send(datagram);
        socket.close();
    }

    public boolean slavesDEL(int foo){
        array.remove(foo);
        return true;
    }

    public boolean slavesADD(Place placetoadd){
        array.add(placetoadd);
        return true;
    }


}