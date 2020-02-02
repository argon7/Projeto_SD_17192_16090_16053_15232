package Server;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Time {
    private ConcurrentHashMap<String, Timestamp> Vizinhos = new ConcurrentHashMap<>();
    private Timestamp newtimestamp = new Timestamp(System.currentTimeMillis());
    private Timestamp Threshold = new Timestamp(20000);

    public void AdicionarVizinho(String porto) {
        newtimestamp = new Timestamp(System.currentTimeMillis());
        Vizinhos.put(porto, newtimestamp);
    }

    public void RemoverVizinho(String porto) {
        this.Vizinhos.remove(porto);
    }

    public synchronized int DevoRemoverDosVizinhos() {
        for (Map.Entry<String, Timestamp> mapElement : Vizinhos.entrySet()) {
            String key = mapElement.getKey();
            Timestamp Value = mapElement.getValue();
            newtimestamp.setTime(System.currentTimeMillis());
            if (newtimestamp.getTime() - Value.getTime() > Threshold.getTime()) {
                return Integer.parseInt(key);
            }
        }
        return 0;
    }
}