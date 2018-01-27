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
public class ManufacturingMessageList implements Serializable{
    
    private List<ManufacturingMessage> manufacturingMessage = new ArrayList<ManufacturingMessage>();

    public ManufacturingMessageList(List<ManufacturingMessage> manufacturingMessage) {
        this.manufacturingMessage = manufacturingMessage;
    }

    public List<ManufacturingMessage> getManufacturingMessage() {
        return manufacturingMessage;
    }

    public void setManufacturingMessage(List<ManufacturingMessage> manufacturingMessage) {
        this.manufacturingMessage = manufacturingMessage;
    }


    
    

    
    
}
