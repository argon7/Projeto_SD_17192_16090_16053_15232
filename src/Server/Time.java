package Server;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Time {
    private ConcurrentHashMap<String, Timestamp> Vizinhos = new ConcurrentHashMap<>(); // regista o tempo da ultima comunicaçao do no
    private Timestamp newtimestamp = new Timestamp(System.currentTimeMillis());
    private Timestamp Threshold = new Timestamp(20000);

    public void AdicionarVizinho(String porto) { // Vizinhos guarda a ultima vez algum no comunicou
        newtimestamp = new Timestamp(System.currentTimeMillis());
        // System.out.println("ESTOU A A ADICIONAR "+porto+" | tempo "+newtimestamp.getTime());
        Vizinhos.put(porto, newtimestamp);
    }

    public void RemoverVizinho(String porto) {
        this.Vizinhos.remove(porto);
    }

    public synchronized int DevoRemoverDosVizinhos() { //Devolve o primeiro vizinho encontrado que não comunica ha muito
        for (Map.Entry<String, Timestamp> mapElement : Vizinhos.entrySet()) {
            String key = mapElement.getKey();
            Timestamp Value = mapElement.getValue();
            newtimestamp.setTime(System.currentTimeMillis());
            if (newtimestamp.getTime() - Value.getTime() > Threshold.getTime()) { //demasiado tempo sem ouvir dele
                return Integer.parseInt(key);
            }
        }
        return 0; //Se todos na lista estiverem apenas com comunicaçoes recentes envia 0
    }

    public synchronized void PrintVizinhos() { //Devolve o primeiro vizinho encontrado que não comunica ha muito
        for (Map.Entry<String, Timestamp> mapElement : Vizinhos.entrySet()) {
            String key = mapElement.getKey();
            Timestamp Value = mapElement.getValue();
            System.out.println("KEY = " + key + " | VALUE = " + Value.getTime() + "| CURRENT TIME = " + newtimestamp.getTime());
        }
    }
}
