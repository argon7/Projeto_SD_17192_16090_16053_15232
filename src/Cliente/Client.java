package Cliente;
import Shared.FrontendInterface;


import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {
    private Client() {
    }

    public static void main(String[] args) throws  InterruptedException {

        FrontendInterface stub = null;
        while (stub == null) {
            try {
                stub = (FrontendInterface) Naming.lookup("rmi://localhost:2023/FrontendInterface");
                menu(stub);
            } catch (Exception e) {
                System.err.println("Frontend not available, retrying in 5 seconds ");
                e.printStackTrace();
            }
            Thread.sleep(5000);
        }
    }

    public static void menu(FrontendInterface stub) throws IOException, NotBoundException {
        Scanner myObj = new Scanner(System.in);
        String escolha;
        while (true) {
            System.out.println("+----------------------------------+");
            System.out.println("|             ESCOLHA              |");
            System.out.println("+----------------------------------+");
            System.out.println("|    1 :   Create Places           |");
            System.out.println("|    2 :   Read Places             |");
            System.out.println("|    3 :   Update Places           |");
            System.out.println("|    4 :   Delete Places           |");
            System.out.println("|    0 :       Leave               |");
            System.out.println("+----------------------------------+");
            System.out.print("-> ");
            escolha = myObj.nextLine();  // Read user input
            switch (escolha) {
                case "1":
                    System.out.println(" ::: Create Places ::: ");
                    addPlace(stub);
                    break;
                case "2":
                    System.out.println(" ::: Read Places ::: ");
                    readPlaces(stub);
                    break;
                case "3":
                    System.out.println(" ::: Update Places ::: ");
                    updatePlace(stub);
                    break;
                case "4":
                    System.out.println(" ::: Delete Places ::: ");
                    deletePlace(stub);
                    break;
                case "0":
                    myObj.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("A opção que escolheu não é válida : " + escolha);
            }
        }
    }

    //Adds new place
    public static void addPlace(FrontendInterface stub) throws RemoteException, MalformedURLException, NotBoundException {
        Scanner myObj = new Scanner(System.in);
        System.out.println("Codigo Postal:");
        System.out.print("-> ");
        String codigoPostal = myObj.nextLine();
        System.out.println("localidade:");
        System.out.print("-> ");
        String localidade = myObj.nextLine();

        stub.addPlace(codigoPostal, localidade);
    }

    //Return places array
    public static void readPlaces(FrontendInterface stub) throws RemoteException, MalformedURLException, NotBoundException {
        HashMap<String, String> ax = stub.allPlaces();
        int i = 0;
        System.out.println("+----------------------------------+");
        System.out.println("|             Places               |");
        System.out.println("+----------------------------------+");
        for (HashMap.Entry<String, String> val : ax.entrySet()) {
            i++;
            System.out.println("|  " + i + " -> Local: " + val.getKey() + " C.Postal: " + val.getValue());
        }
    }

    //Update place -- Not implemented
    public static void updatePlace(FrontendInterface stub) throws RemoteException, MalformedURLException, NotBoundException {
        HashMap<String, String> ax;
        ax = stub.allPlaces();
        if (ax.isEmpty()) {
            System.out.println("No places to update");
            return;
        }

        String key = "";
        String value;
        String localidade = "99999";

        int i = 0;
        System.out.println("+----------------------------------+");
        System.out.println("|             Places               |");
        System.out.println("+----------------------------------+");
        for (HashMap.Entry<String, String> val : ax.entrySet()) {
            i++;
            System.out.println("|  " + i + " -> Local: " + val.getKey() + " C.Postal: " + val.getValue());
        }
        System.out.println("+----------------------------------+");
        System.out.println("|       0 -> Cancel Operation      |");
        System.out.println("+----------------------------------+");

        Scanner myObj = new Scanner(System.in);


        while ((Integer.parseInt(localidade) > i || Integer.parseInt(localidade) < 0 )) {
            System.out.println("What place do you wish to update?");
            System.out.print("-> ");
            localidade = myObj.nextLine();
            if (Integer.parseInt(localidade) == 0) {
                System.out.println("Update operation canceled");
                return;
            }
        }

        i = 0;
        for (HashMap.Entry<String, String> val : ax.entrySet()) {
            i++;
            if (i == Integer.parseInt(localidade)) {
                key = val.getKey();
                break;
            }
        }
        System.out.println("Novo código postal:");
        System.out.print("->");

        value = myObj.nextLine();
        boolean answer = stub.addPlace(value, key);
        if (answer) {
            System.out.println("Place updated com sucesso");
        } else {
            System.out.println("Erro ao dar update place selecionado");
        }


        stub.updatePlace("teste", "teste");
    }

    //Delete place
    public static void deletePlace(FrontendInterface stub) throws IOException, NotBoundException {
        HashMap<String, String> ax = stub.allPlaces();
        if (ax.isEmpty()) {
            System.out.println("No places to delete");
            return;
        }

        int i = 0;
        System.out.println("+----------------------------------+");
        System.out.println("|             Places               |");
        System.out.println("+----------------------------------+");
        for (HashMap.Entry<String, String> val : ax.entrySet()) {
            System.out.println("|  " + ++i + " -> Local: " + val.getKey() + " C.Postal: " + val.getValue());
        }
        System.out.println("+----------------------------------+");
        System.out.println("|       0 -> Cancel Operation      |");
        System.out.println("+----------------------------------+");
        Scanner myObj = new Scanner(System.in);
        String teste = String.valueOf(ax.size()+1);


        while ((Integer.parseInt(teste)>ax.size()) || (Integer.parseInt(teste)<1)){
            System.out.println("What place do you wish to delete?");
            System.out.print("-> ");
            teste = myObj.nextLine();
            if(Integer.parseInt(teste)==0) return;
        }

        boolean answer = stub.delPlace(Integer.parseInt(teste));
        if (answer) {
            System.out.println("Place apagado com sucesso");
        } else {
            System.out.println("Erro ao apagar place selecionado");
        }
    }
}