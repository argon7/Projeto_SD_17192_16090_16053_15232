package Server;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ElectionProcess {
    private boolean failure = false;
    private boolean waitingForOK = true;
    private boolean waitingForVictory = true;
    private boolean passedOKPhase = false;
    private boolean passedVictoryPhase = false;
    private ConcurrentHashMap<String, Timestamp> electoralProcessStates = new ConcurrentHashMap<>();
    private Timestamp waitTimeForOK = new Timestamp(30000);
    private Timestamp waitTimeForVictory = new Timestamp(60000);


    public boolean isFailure() {
        return failure;
    }

    public void setFailure(boolean failure) {
        this.failure = failure;
    }

    public boolean isWaitingForOK() {
        return waitingForOK;
    }

    public void setWaitingForOK(boolean waitingForOK) {
        this.waitingForOK = waitingForOK;
    }

    public boolean isWaitingForVictory() {
        return waitingForVictory;
    }

    public void setWaitingForVictory(boolean waitingForVictory) {
        this.waitingForVictory = waitingForVictory;
    }

    public boolean isPassedOKPhase() {
        return passedOKPhase;
    }

    public void setPassedOKPhase(boolean passedOKPhase) {
        this.passedOKPhase = passedOKPhase;
    }

    public boolean isPassedVictoryPhase() {
        return passedVictoryPhase;
    }

    public void setPassedVictoryPhase(boolean passedVictoryPhase) {
        this.passedVictoryPhase = passedVictoryPhase;
    }


    public void startOkwaitTime() {
        Timestamp Testingtimestamp = new Timestamp(System.currentTimeMillis());
        electoralProcessStates.put("waitingForOK", Testingtimestamp);
    }

    public void startVictorywaitTime() {
        Timestamp Testingtimestamp = new Timestamp(System.currentTimeMillis());
        electoralProcessStates.put("waitingForVictory", Testingtimestamp);
    }

    public boolean checkIfOkWaitTimeHasPassed() {
        for (Map.Entry<String, Timestamp> mapElement : electoralProcessStates.entrySet()) {
            String key = mapElement.getKey();
            Timestamp Value = mapElement.getValue();
            Timestamp Testingtimestamp = new Timestamp(System.currentTimeMillis());
            // newTimestamp.setTime(System.currentTimeMillis());
            if (key.equals("waitingForOK"))
                // System.out.println(String.valueOf(newTimestamp.getTime() - Value.getTime() > waitTimeForOK.getTime()));
                return (Testingtimestamp.getTime() - Value.getTime() > waitTimeForOK.getTime()); // bool value
        }
        return false;
    }

    public boolean checkIfVictoryWaitTimeHasPassed() {
        for (Map.Entry<String, Timestamp> mapElement : electoralProcessStates.entrySet()) {
            String key = mapElement.getKey();
            Timestamp Value = mapElement.getValue();
            Timestamp TestingVictorytimestamp = new Timestamp(System.currentTimeMillis());
            // newTimestamp.setTime(System.currentTimeMillis());
            if (key.equals("waitingForVictory"))
                return TestingVictorytimestamp.getTime() - Value.getTime() > waitTimeForVictory.getTime(); // bool value
        }
        return false;
    }

    public void cleanUpFromElectionParty(){
        failure = false;
        waitingForOK = true;
        waitingForVictory = true;
        passedOKPhase = false;
        passedVictoryPhase = false;
    }

}