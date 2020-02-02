package Shared;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class No {
    private final ConcurrentHashMap<String, String> Vista = new ConcurrentHashMap<>();
    private int State = 0;
    private int Porto = 0;
    private int viewprint = 0;

    public int getState() {
        return State;
    }

    public void setState(int state) {
        State = state;
    }

    public ConcurrentHashMap<String, String> getVista() {
        return Vista;
    }

    public int getPorto() {
        return this.Porto;
    }

    public void setPorto(int porto) {
        this.Porto = porto;
    }

    public void AdicionarNaVistaSeNecessario(String porto, String estado, ConcurrentHashMap<String, String> Test) {
        Test.put(porto, estado);
    }

    public void RemoverdaVista(String porto) {
        Vista.remove(porto);
    }

    public synchronized void PrintVistaDoNo() {
        System.out.print("Vista = ");
        for (Map.Entry<String, String> mapElement : Vista.entrySet()) {
            String key = mapElement.getKey();
            String Value = mapElement.getValue();
            System.out.print("[" + key + "] ");
            System.out.print("[" + Value + "] ");
        }
        System.out.print(" #" + ++viewprint);
        System.out.println();
    }

    public boolean isThereAFailure(ConcurrentHashMap<String, String> Test) {
        int LeaderCount = 0;
        for (Map.Entry<String, String> mapElement : Test.entrySet()) {
            String Value = mapElement.getValue();
            if (Value.equals("2")) {
                LeaderCount++;
            }
        }
        return LeaderCount != 1;
    }

    public void acceptNewLeader(String leader, String leadState) {
        for (Map.Entry<String, String> mapElement : Vista.entrySet()) {
            String key = mapElement.getKey();
            Vista.put(key, "0");
        }
        Vista.put(leader, leadState);
        if (leader.equals(String.valueOf(getPorto()))) {
            this.setState(2);
        }
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

    public static void sendMessage(String address, Integer port, String message) throws IOException {
        byte[] m = message.getBytes();
        DatagramSocket socket = new DatagramSocket();
        InetAddress host = InetAddress.getByName(address);
        DatagramPacket datagram = new DatagramPacket(m, m.length, host, port);
        socket.send(datagram);
        socket.close();
    }
}