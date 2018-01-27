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

    public ManufacturingMessage(AID digger, ManufacturingCenterCell mancell) {
        this.digger = digger;
        this.mancell = mancell;
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
    
    

    
    

    
}