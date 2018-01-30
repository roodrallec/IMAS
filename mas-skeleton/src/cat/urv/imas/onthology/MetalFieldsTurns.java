/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import cat.urv.imas.map.FieldCell;
import java.util.HashMap;

/**
 *
 * @author ALEIX
 */
public class MetalFieldsTurns {
    //private List<FieldCell> metalFieldsList = new ArrayList<>();
    private HashMap metalFieldsList = new HashMap();

    public MetalFieldsTurns() {
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
        return (double) this.metalFieldsList.get(fieldCell);
    }
    public void incrementTurn() {        
        for (Object key : this.metalFieldsList.keySet()) {
            double currentValue = (double) this.metalFieldsList.get(key);
            this.metalFieldsList.replace(key, currentValue++);
        }
    }
    
}
