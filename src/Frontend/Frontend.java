package Frontend;
import Shared.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static java.lang.Thread.sleep;

// Implementing the remote interface
public class Frontend extends UnicastRemoteObject implements FrontendInterface {
    private static PlaceManagerInterface stub;
    FrontEndView frontEndView = new FrontEndView();
    public Frontend() throws RemoteException {

        new Thread(() -> {
            while (true) {
                try {
                    String recebe = listenMulticastMessage("230.1.1.1", 5000);
                    String[] aux = recebe.split("@", 4);
                    String recebe_porto = aux[0];
                    String recebe_estado = aux[1];
                    String recebe_informacao = aux[2];
                    if ((recebe_informacao.equals("HEARTBEAT"))) {
                        // UPDATE VIEW
                        Timestamp messageReceivedAt = new Timestamp(System.currentTimeMillis());
                        frontEndView.adicionarNaFrontEndView(recebe_porto,messageReceivedAt);
                        if(recebe_estado.equals("2")){ // Update stub
                            UpdateSTUB(recebe_porto);
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //Clean View Periodically
        new Thread(() -> {
            while (true) {
                try {
                    frontEndView.ifNoResponseFromNodeForTooLongRemoveFromView();
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();



    }

    public static void UpdateSTUB(String port) throws InterruptedException {
        if (stub == null) {
            while (stub == null) {
                try {
                    System.out.println("New lider"+port);
                    stub = (PlaceManagerInterface) Naming.lookup("rmi://localhost:"+port+"/placeList");
                } catch (NotBoundException | MalformedURLException | RemoteException e) {
                    System.err.println("Frontend not available, retrying in 5 seconds ");
                }
                sleep(5000);
            }
        } else {
            try {
                System.out.println("New lider"+port);
                stub = (PlaceManagerInterface) Naming.lookup("rmi://localhost:"+port+"/placeList");
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                System.out.println("Error connecting to new lider");
            }
        }




    }
    // Implementing the interface method
    public boolean addPlace(String codigoPostal,String localidate) throws RemoteException {
        System.out.println("AddPlace");
        Place p = new Place(codigoPostal,localidate);
        try{
            stub.addPlace(p);
            return true;
        }catch (NullPointerException a) {
            System.out.println("Stub nao definido: "+stub);
            return false;
        }
    }

    public boolean delPlace(int index) throws RemoteException {
        System.out.println("DelPlace");
        return stub.delPlace(index);
    }
    public HashMap<String,String> allPlaces() throws RemoteException, MalformedURLException, NotBoundException {

        System.out.println("Return all places");
        String calledReadOperationOnWhatNode = String.valueOf(frontEndView.fetchRandomNode());
        PlaceManagerInterface stub2= (PlaceManagerInterface) Naming.lookup("rmi://localhost:"+calledReadOperationOnWhatNode+"/placeList");
        System.out.println("NODE THAT RESPONDED TO READ REQUEST = "+calledReadOperationOnWhatNode);
        ArrayList<Place> arr = stub2.allPlaces();
        HashMap<String,String>ax = new HashMap<>();
        for (Place obj : arr) {
            ax.put(obj.getLocality(),obj.getPostalCode());
        }

        return ax;
    }

    public void updatePlace(String codigoPostal,String localidate){
        System.out.println("Update Place");
    }
    public static String listenMulticastMessage(String address, Integer port) throws IOException {
        MulticastSocket mSocket = new MulticastSocket(port);
        InetAddress mcAddress = InetAddress.getByName(address);
        mSocket.setReuseAddress(true);
        mSocket.joinGroup(mcAddress);

        byte[] buffer = new byte[1024];
        DatagramPacket response = new DatagramPacket(buffer, buffer.length);
        mSocket.receive(response);

        mSocket.close();
        return new String(response.getData(), 0, response.getLength());
    }

}