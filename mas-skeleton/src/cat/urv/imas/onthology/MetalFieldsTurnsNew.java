/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.map.FieldCell;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ALEIX
 */
public class MetalFieldsTurnsNew  implements java.io.Serializable {
    private HashMap metalFieldsList = new HashMap();

    public MetalFieldsTurnsNew() {
    }    
    
    public void addNewMetalField(FieldCell fieldCell) {
        this.metalFieldsList.put(fieldCell, 0.0);
    }
    public HashMap getAllMetalFields() {
        return this.metalFieldsList;
    } 
    public void removeMetalField(FieldCell fieldCell) {
        this.metalFieldsList.remove(fieldCell);
    }
    public double getMetalField(FieldCell fieldCell) {
        double result = -1.0;
        try{
            result = (double) this.metalFieldsList.get(fieldCell);
        } catch (Exception ex) {
            Logger.getLogger(SystemAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    public void incrementTurn() {        
        for (Object key : this.metalFieldsList.keySet()) {
            double value = (double) this.metalFieldsList.get(key) + 1.0;
            this.metalFieldsList.replace(key, value);
        }
    }
    
}
