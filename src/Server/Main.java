package Server;

import Shared.No;
import Shared.Place;
import Shared.PlaceManagerInterface;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Random;

import static java.lang.Thread.sleep;

public class Main {
    public static void main(String[] args) throws RemoteException {
        PlaceManagerInterface placeList = new PlaceManager();
        LocateRegistry.createRegistry(Integer.parseInt(args[0]));
        Registry r = LocateRegistry.getRegistry(Integer.parseInt(args[0]));
        r.rebind("placeList", placeList);
        No no = new No();
        Time time = new Time();
        no.setPorto(Integer.parseInt(args[0]));
        ElectionProcess electionProcess = new ElectionProcess();
        Thread RecebeMensagensMulticast = new Thread(() -> {
            while (true) {
                try {
                    String recebe = No.listenMulticastMessage("230.1.1.1", 5000);
                    String[] aux = recebe.split("@", 5);
                    String recebe_porto = aux[0];
                    String recebe_estado = aux[1];
                    String recebe_informacao = aux[2];
                    if (recebe_informacao.equals("HEARTBEAT")) {
                        no.AdicionarNaVistaSeNecessario(recebe_porto, recebe_estado, no.getVista());
                        time.AdicionarVizinho(recebe_porto);
                    }
                    if (recebe_informacao.equals("ADD") && no.getState() != 2) {
                        String recebe_localidade = aux[3];
                        String recebe_postal = aux[4];
                        placeList.slavesADD(new Place(recebe_postal, recebe_localidade));
                    }
                    if (recebe_informacao.equals("DEL") && no.getState() != 2) {
                        String recebe_delete_index = aux[3];
                        System.out.println(recebe_delete_index);
                        placeList.slavesDEL(Integer.parseInt(recebe_delete_index));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        RecebeMensagensMulticast.start();

        Thread sendsMulticastHeartbeat = new Thread(() -> {
            while (true) {

                try {
                    No.sendMessage("230.1.1.1", 5000, no.getPorto() + "@" + no.getState() + "@"
                            + "HEARTBEAT");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LimparVista(no, time);
                no.PrintVistaDoNo();
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        sendsMulticastHeartbeat.start();

        Thread recebeUNICAST = new Thread(() -> {
            electionProcess.setFailure(true);
            while (true) {
                String receives = null;
                try {
                    receives = unicastReceives(Integer.parseInt(args[0]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert receives != null;
                String[] aux = receives.split("@", 4);
                String receivesPort = aux[0];
                String receivesState = aux[1];
                String receivesInfo = aux[2];
                if (receivesInfo.equals("ELECTION")) {
                    System.out.println("RECEIVED: " + receivesPort + " " + receivesState + " " + receivesInfo);
                    unicastSends(Integer.parseInt(args[0]) + 1000, Integer.parseInt(receivesPort),
                            args[0] + "@" + "1" + "@" + "ELECTION_OK");
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (receivesInfo.equals("ELECTION_OK")) {
                    electionProcess.setWaitingForOK(false);
                    electionProcess.setFailure(true);
                    System.out.println("RECEIVED OK: " + receivesPort + " " + receivesState + " " + receivesInfo);
                }
                if (receivesInfo.equals("VICTORY")) {
                    System.out.println("################ LEADER: " + receivesPort + " ###################");
                    no.acceptNewLeader(receivesPort, receivesState);
                    electionProcess.setWaitingForVictory(false);
                }
            }
        });
        recebeUNICAST.start();

        Thread sendsTh = new Thread(() -> {
            try {
                sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            System.out.println("&&&&&&&      SYNC UP TIME OVER     &&&&&&&&&");
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            while (true) {
                if (electionProcess.isFailure()) {
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++");
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++");
                    System.out.println("+++++++++++    ELECTION START    +++++++++++");
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++");
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++");
                    System.out.println("ELECTION PROCESS STARTED");
                    electionProcess.setWaitingForOK(true);
                    electionProcess.setWaitingForVictory(true);
                    for (int i = 0; i < 10; i++) {
                        Random rand = new Random();
                        int rand_int1 = rand.nextInt(1000);
                        for (Map.Entry<String, String> mapElement : no.getVista().entrySet()) {
                            String key = mapElement.getKey();
                            if (Integer.parseInt(args[0]) < Integer.parseInt(key)) {
                                System.out.println("SENT TO: " + Integer.parseInt(key) + " : " + args[0] + " " + "1"
                                        + " " + "ELECTION");
                                unicastSends(Integer.parseInt(args[0]), Integer.parseInt(key), args[0] + "@"
                                        + "1" + "@" + "ELECTION");
                            }
                            if (electionProcess.isWaitingForOK()) break;
                            try {
                                sleep(rand_int1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    electionProcess.startOkwaitTime();
                    electionProcess.setPassedOKPhase(false);
                    electionProcess.setPassedVictoryPhase(false);
                    while (!electionProcess.checkIfOkWaitTimeHasPassed()) {
                        if (electionProcess.isWaitingForOK()) {
                            System.out.println("I'VE RECEIVED OK AND MOVED ON");
                            electionProcess.setPassedOKPhase(true);
                            break;
                        }
                    }
                    if (!electionProcess.isPassedOKPhase()) {
                        System.out.println("HAVENT RECEIVED OK, IM LEADER");
                        for (Map.Entry<String, String> mapElement : no.getVista().entrySet()) {
                            String key = mapElement.getKey();
                            unicastSends(Integer.parseInt(args[0]), Integer.parseInt(key), args[0] + "@" + "2"
                                    + "@" + "VICTORY");
                        }
                    }
                    electionProcess.startVictorywaitTime();
                    while (!electionProcess.checkIfVictoryWaitTimeHasPassed()) {
                        if (!electionProcess.isWaitingForVictory()) {
                            electionProcess.setPassedVictoryPhase(true);
                            break;
                        }
                    }
                    if (electionProcess.isPassedVictoryPhase()) {
                        electionProcess.cleanUpFromElectionParty();
                    } else {
                        electionProcess.cleanUpFromElectionParty();
                        electionProcess.setFailure(true);
                        System.out.println("SOMEONE FAILED MID ELECTION, RESTART");
                    }
                    System.out.println("--------------------------------------------");
                    System.out.println("--------------------------------------------");
                    System.out.println("-----------    ELECTION END    -------------");
                    System.out.println("--------------------------------------------");
                    System.out.println("--------------------------------------------");
                }
                electionProcess.setFailure(no.isThereAFailure(no.getVista()));
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        sendsTh.start();
    }

    public static String unicastReceives(Integer port) throws IOException {
        try (DatagramSocket clientSocket = new DatagramSocket(port)) {
            byte[] buffer = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, 0, buffer.length);
            clientSocket.receive(datagramPacket);
            clientSocket.close();
            return new String(datagramPacket.getData(), 0, datagramPacket.getLength());
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void unicastSends(Integer portThatSends, Integer portThatListens, String message) {
        try (DatagramSocket serverSocket = new DatagramSocket(50000 + portThatSends)) {
            DatagramPacket datagramPacket = new DatagramPacket(
                    message.getBytes(),
                    message.length(),
                    InetAddress.getLocalHost(),
                    portThatListens);
            serverSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void LimparVista(No no, Time time) {
        int remove_this_node;
        remove_this_node = time.DevoRemoverDosVizinhos();
        if (remove_this_node != 0)
            System.out.println("$$$$$$$$$$$$$$$$$$$ REMOVED " + remove_this_node + "$$$$$$$$$$$$$$$$$");
        while ((remove_this_node = time.DevoRemoverDosVizinhos()) != 0) {
            no.RemoverdaVista(String.valueOf(remove_this_node));
            time.RemoverVizinho(String.valueOf(remove_this_node));
        }
    }
}