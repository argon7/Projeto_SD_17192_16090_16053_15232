package Frontend;

import Shared.FrontendInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
    public static void main(String[] args) throws RemoteException {
        Registry r;
        try {
            LocateRegistry.createRegistry(2023);
        } catch (RemoteException var4) {
            var4.printStackTrace();
        }
        r = LocateRegistry.getRegistry(2023);

        FrontendInterface fe = new Frontend();
        // fe = (FrontendInterface) UnicastRemoteObject.exportObject(fe, 2023);
        r.rebind("FrontendInterface", fe);
        System.out.println("Server FrontendInterface ready");
    }

}
