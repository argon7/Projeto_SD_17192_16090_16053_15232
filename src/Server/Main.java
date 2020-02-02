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

        // -------- RMI SETUP
        PlaceManagerInterface placeList = new PlaceManager();
        LocateRegistry.createRegistry(Integer.parseInt(args[0]));
        Registry r = LocateRegistry.getRegistry(Integer.parseInt(args[0]));
        r.rebind("placeList",placeList);
        // -------- RMI SETUP


        // from sdnos
        No no = new No();
        Time time = new Time();
        no.setPorto(Integer.parseInt(args[0]));

        //FOR TESTING
        // if (Integer.parseInt(args[0]) == 6003) no.setState(2);


        ElectionProcess electionProcess = new ElectionProcess();


        //from sdnos multicast
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
                        // no.PrintVistaDoNo();
                        // time.PrintVizinhos();
                    }

                    if (recebe_informacao.equals("ADD") && no.getState()!=2) {
                        String recebe_localidade = aux[3];
                        String recebe_postal = aux[4];
                        placeList.slavesADD(new Place(recebe_postal,recebe_localidade));

                    }

                    if (recebe_informacao.equals("DEL") && no.getState()!=2) {
                        String recebe_delete_index = aux[3];
                        System.out.print("========================================================= DEL ");
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
                //ENVIAR "HEARTBEAT" OR "ALIVE" OR "VICTORY"
                try {
                    No.sendMessage("230.1.1.1", 5000, no.getPorto() + "@" + no.getState() + "@" + "HEARTBEAT");
                    // System.out.println("////////////////////////" + no.getPorto() + "///////" + no.getState());
                } catch (IOException e) {
                    e.printStackTrace();
                }


                //LIMPAR A VISTA PERIODICAMENTE
                LimparVista(no, time);

                // Vista deste No
                no.PrintVistaDoNo();

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        sendsMulticastHeartbeat.start();

        //______________________________SDNOS__________________________

        Thread recebeUNICAST = new Thread(() -> {
            electionProcess.setFailure(true);
            while (true) {
                // Receive Message
                String receives = null;
                try {
                    receives = unicastReceives(Integer.parseInt(args[0]));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Parse Message
                assert receives != null;
                String[] aux = receives.split("@", 4);
                String receivesPort = aux[0];
                String receivesState = aux[1];
                String receivesInfo = aux[2];

                // What Action it does once after message analysis
                if (receivesInfo.equals("ELECTION")) {
                    //electionSyncTime.setWaitingForSyncElection(false); // election message forces the end of sync time
                    System.out.println("RECEIVED: " + receivesPort + " " + receivesState + " " + receivesInfo);
                    // Reply back "OK"
                    // we had +1000 so that both threads dont access the same port for sending at any time
                    // there is a window for 1000 nodes
                    unicastSends(Integer.parseInt(args[0]) + 1000, Integer.parseInt(receivesPort), args[0] + "@" + "1" + "@" + "ELECTION_OK");
                    //electionProcess.setFailure(true);
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
                    System.out.println("################ LEADER: " + receivesPort + "###################");


                    // set all states to 0 , received port to 2
                    no.acceptNewLeader(receivesPort, receivesState);

                    // if im leader change state
                    // no.ClaimLeadership(args[0]);

                    //System.out.println("### ALL HAIL THE NEW LEADER ###");
                    electionProcess.setWaitingForVictory(false); // must be last thing to do

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


                // DETETA FALHAS E INICIA ELEIÇOES

                if (electionProcess.isFailure()) {// Start election on failure detection if not waiting for election sync


                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++");
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++");
                    System.out.println("+++++++++++    ELECTION START    +++++++++++");
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++");
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++");


                    System.out.println("ELECTION PROCESS STARTED");
                    // Implement View sync here if needed
                    electionProcess.setWaitingForOK(true);
                    electionProcess.setWaitingForVictory(true); // maybe mistake here?
                    // Send "ELECTION" messages
                    for (int i = 0; i < 10; i++) {
                        Random rand = new Random();
                        int rand_int1 = rand.nextInt(1000);


                        for (Map.Entry<String, String> mapElement : no.getVista().entrySet()) {
                            String key = mapElement.getKey();

                            if (Integer.parseInt(args[0]) < Integer.parseInt(key)) { // send election to higher nodes
                                System.out.println("SENT TO: " + Integer.parseInt(key) + " : " + args[0] + " " + "1" + " " + "ELECTION");
                                unicastSends(Integer.parseInt(args[0]), Integer.parseInt(key), args[0] + "@" + "1" + "@" + "ELECTION");
                            }
                            if (!electionProcess.isWaitingForOK()) break;
                            try {
                                sleep(rand_int1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }


                    }


                    // Wait for OK
                    electionProcess.startOkwaitTime();
                    electionProcess.setPassedOKPhase(false);
                    electionProcess.setPassedVictoryPhase(false);

                    while (!electionProcess.checkIfOkWaitTimeHasPassed()) {
                        //System.out.println("Im wainting on checkIfOkWaitTimeHasPassed");
                        if (!electionProcess.isWaitingForOK()) { // ive received OK
                            System.out.println("I'VE RECEIVED OK AND MOVED ON");
                            electionProcess.setPassedOKPhase(true);
                            break;
                        }
                    }

                    if (!electionProcess.isPassedOKPhase()) { // if no ok then communicate victory
                        System.out.println("HAVENT RECEIVED OK, IM LEADER");
                        // claim sovereignty , implement a function here if needed
                        for (Map.Entry<String, String> mapElement : no.getVista().entrySet()) { // send victory to self
                            String key = mapElement.getKey();
                            unicastSends(Integer.parseInt(args[0]), Integer.parseInt(key), args[0] + "@" + "2" + "@" + "VICTORY");
                        }
                    }

                    // if it received ok then wait for reply from leader or from himself

                    // Wait for VICTORY
                    electionProcess.startVictorywaitTime();


                    // IF waiting time for "VICTORY" is too long Restarts election process
                    while (!electionProcess.checkIfVictoryWaitTimeHasPassed()) {
                        if (!electionProcess.isWaitingForVictory()) { // if i received "VICTORY" then victory phase success
                            System.out.println("I'VE RECEIVED VICTORY SO I COMPLY");
                            electionProcess.setPassedVictoryPhase(true);
                            break;
                        }
                    }


                    if (electionProcess.isPassedVictoryPhase()) { // Successful election
                        electionProcess.cleanUpFromElectionParty();
                        System.out.println("SENDER VERIDICT = HAIL INDEED");
                        System.out.println("%%%%%%%%%%% JUMPING THE IF %%%%%%%%%%%%%%");
                    } else {
                        electionProcess.cleanUpFromElectionParty();
                        electionProcess.setFailure(true); // There is a failure, restart election
                        System.out.println("SOMEONE FAILED MID ELECTION, RESTART");

                    }

                    System.out.println("--------------------------------------------");
                    System.out.println("--------------------------------------------");
                    System.out.println("-----------    ELECTION END    -------------");
                    System.out.println("--------------------------------------------");
                    System.out.println("--------------------------------------------");

                }

                // FAILURE DETECTION HERE


                electionProcess.setFailure(no.isThereAFailure(no.getVista()));

                //System.out.println(no.isThereAFailure(no.getVista()));
                //no.isThereAFailure();
                //electionProcess.setFailure(true);


                System.out.println("Not in election, waiting for ok =" + electionProcess.isWaitingForOK());

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        sendsTh.start();
    }

    public static String unicastReceives(Integer port) throws IOException { // Receive messages
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

    public static void unicastSends(Integer portThatSends, Integer portThatListens, String message) { // Send messages
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

    // from sdnos
    private static void LimparVista(No no, Time time) {
        int remove_this_node;
        remove_this_node = time.DevoRemoverDosVizinhos();
        if (remove_this_node != 0)
            System.out.println("$$$$$$$$$$$$$$$$$$$ Called " + remove_this_node + "$$$$$$$$$$$$$$$$$");
        while ((remove_this_node = time.DevoRemoverDosVizinhos()) != 0) {
            no.RemoverdaVista(String.valueOf(remove_this_node));
            time.RemoverVizinho(String.valueOf(remove_this_node));
        }
    }
    //_______________________from sdnos_____________________
}