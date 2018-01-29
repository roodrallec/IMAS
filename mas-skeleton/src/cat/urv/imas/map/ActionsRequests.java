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
public class ActionsRequests {
    private List<AID> ids = new ArrayList<>();
    private ArrayList<int[]> positions = new ArrayList<>();
    private int agentsInList = 0;

    public ActionsRequests() {
    }
    
    public int getNumberOfAgentsInList() {
        return agentsInList;
    }
    
    public void setNewAgent(int[] newPos, AID newId) {
        ids.add(newId);
        positions.add(newPos);
        agentsInList++;
    }
    
    public int[] getAgentById(AID agentId) {
        int[] agentPos; // = new int[2];
        int agentIndex = ids.indexOf(agentId);
        
        if (agentIndex == -1){
            //log("GUI loaded");
            //throw new Exception("There is no agent in cell");
            agentPos = new int[] {-1,-1};
        } else {
            agentPos = positions.get(agentIndex);
        }
       
        return agentPos;
    } 
    
    public int[] getAgentByName(String agentId) {
        int[] agentPos; // = new int[2];
        int agentIndex = ids.indexOf(agentId);
        
        if (agentIndex == -1){
            //log("GUI loaded");
            //throw new Exception("There is no agent in cell");
            agentPos = new int[] {-1,-1};
        } else {
            agentPos = positions.get(agentIndex);
        }
       
        return agentPos;
    }
    
    public int[] getAgentPosByIndex(int agentIndex) {            
        return positions.get(agentIndex);
    }
    public AID getAgentAIDByIndex(int agentIndex) {            
        return ids.get(agentIndex);
    }
    public List<AID> getAllAgentsAID() {            
        return ids;
    }
        
}
