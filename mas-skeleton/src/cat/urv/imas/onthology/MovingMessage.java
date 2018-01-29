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
    private AID agentID;
    private int[] movement;
    private int[] position;

    public MovingMessage(AID agentID, int[] movement, int[] position) {
        this.agentID = agentID;
        this.movement = movement;
        this.position = position;
    }

    public AID getAgentID() {
        return agentID;
    }

    public void setAgentID(AID agentID) {
        this.agentID = agentID;
    }

    public int[] getMovement() {
        return movement;
    }

    public void setMovement(int[] movement) {
        this.movement = movement;
    }

    public int[] getPosition() {
        return position;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }
    
    

    
}