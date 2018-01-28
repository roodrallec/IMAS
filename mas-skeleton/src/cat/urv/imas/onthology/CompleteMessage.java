/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Josep Famadas
 */
public class CompleteMessage implements Serializable{
    
    private DiggingMessageList DML;
    private MovingMessageList MML;
    private ManufacturingMessageList MFML;

    public CompleteMessage(DiggingMessageList DML, MovingMessageList MML, ManufacturingMessageList MFML) {
        this.DML = DML;
        this.MML = MML;
        this.MFML = MFML;
    }

    public DiggingMessageList getDML() {
        return DML;
    }

    public void setDML(DiggingMessageList DML) {
        this.DML = DML;
    }

    public MovingMessageList getMML() {
        return MML;
    }

    public void setMML(MovingMessageList MML) {
        this.MML = MML;
    }

    public ManufacturingMessageList getMFML() {
        return MFML;
    }

    public void setMFML(ManufacturingMessageList MFML) {
        this.MFML = MFML;
    }


    
    

    
    
}
