/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import jade.core.AID;
import java.io.Serializable;

/**
 *
 * @author usuario
 */
public class MovingMessage implements Serializable{
    private AID agent;
    private int[] movement;

    public MovingMessage(AID agentID, int[] movement) {
        this.agent = agentID;
        this.movement = movement;
    }

    public AID getAgentID() {
        return agent;
    }

    public void setAgentID(AID agentID) {
        this.agent = agentID;
    }

    public int[] getMovement() {
        return movement;
    }

    public void setMovement(int[] movement) {
        this.movement = movement;
    }
    
    

    
}