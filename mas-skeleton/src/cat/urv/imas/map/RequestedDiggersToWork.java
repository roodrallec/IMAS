/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.map;

import jade.core.AID;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ALEIX
 */
public class RequestedDiggersToWork {
    //AID of the digger requesting to work
    private List<AID> ids = new ArrayList<AID>();
    //position x,y of the field where the digger want to start working
    private ArrayList<int[]> fieldPositions = new ArrayList<int[]>();
    private int agentsInList = 0;

    public RequestedDiggersToWork() {
    }
    
    public int getNumberOfAgentsInList() {
        return agentsInList;
    }
    
    public void setNewDiggerWorkingRequest(int[] newPos, AID newId) {
        ids.add(newId);
        fieldPositions.add(newPos);
        agentsInList++;
    }
    
    public AID getAgentIDByIndex(int agentIndex) {              
        return ids.get(agentIndex);
    }
    
    public int[] getFieldPosByIndex(int agentIndex) {              
        return fieldPositions.get(agentIndex);
    }
    
    public void removeAgentByIndex(int agentIndex) {              
        fieldPositions.remove(agentIndex);
        ids.remove(agentIndex);
    }
    
//    public void removeAgentById(AID agentId) {              
//        fieldPositions.remove(agentIndex);
//        ids.remove(agentIndex);
//    }
        
}
