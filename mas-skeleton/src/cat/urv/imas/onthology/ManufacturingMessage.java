/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import cat.urv.imas.map.ManufacturingCenterCell;
import jade.core.AID;
import java.io.Serializable;

/**
 *
 * @author usuario
 */
public class ManufacturingMessage implements Serializable{
    private AID digger;
    private ManufacturingCenterCell mancell;
    private int[] position;

    public ManufacturingMessage(AID digger, ManufacturingCenterCell mancell,int[] currentPosition) {
        this.digger = digger;
        this.mancell = mancell;
        this.position = currentPosition;
    }

    public AID getDigger() {
        return digger;
    }

    public void setDigger(AID digger) {
        this.digger = digger;
    }

    public ManufacturingCenterCell getMancell() {
        return mancell;
    }

    public void setMancell(ManufacturingCenterCell mancell) {
        this.mancell = mancell;
    }

    public int[] getPosition() {
        return position;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }
    
    

    
}