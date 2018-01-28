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
    private AID digger;
    private int[] movement;

    public MovingMessage(AID digger, int[] movement) {
        this.digger = digger;
        this.movement = movement;
    }

    public AID getDigger() {
        return digger;
    }

    public void setDigger(AID digger) {
        this.digger = digger;
    }

    public int[] getMovement() {
        return movement;
    }

    public void setMovement(int[] movement) {
        this.movement = movement;
    }
    
    

    
}