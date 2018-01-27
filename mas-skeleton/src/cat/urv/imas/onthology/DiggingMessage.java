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

    public DiggingMessage(AID digger, MetalField metalfield) {
        this.digger = digger;
        this.metalfield = metalfield;
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
    
    
}
