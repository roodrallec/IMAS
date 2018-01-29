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
public class DiggingMessage implements Serializable{
    private AID digger;
    private MetalField metalfield;
    private int[] position; 

    public DiggingMessage(AID digger, MetalField metalfield, int[] position) {
        this.digger = digger;
        this.metalfield = metalfield;
        this.position = position;
    }

    public AID getDigger() {
        return digger;
    }

    public void setDigger(AID digger) {
        this.digger = digger;
    }

    public MetalField getMetalfield() {
        return metalfield;
    }

    public void setMetalfield(MetalField metalfield) {
        this.metalfield = metalfield;
    }

    public int[] getPosition() {
        return position;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }
    
    
}
