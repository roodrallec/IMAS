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
public class MovingMessageList implements Serializable{
    
    private List<MovingMessage> movingMessages = new ArrayList<MovingMessage>();

    public MovingMessageList(List<MovingMessage> movingMessages) {
        this.movingMessages = movingMessages;
    }
    public MovingMessageList() {
       this.movingMessages = new ArrayList<MovingMessage>();
    }
    public List<MovingMessage> getMovingMessages() {
        return movingMessages;
    }

    public void setMovingMessages(List<MovingMessage> movingMessages) {
        this.movingMessages = movingMessages;
    }
    
    

    
    
}
