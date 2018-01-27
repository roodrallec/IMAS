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
public class DiggingMessageList implements Serializable{
    
    private List<DiggingMessage> diggingMessages = new ArrayList<DiggingMessage>();

    public DiggingMessageList(List<DiggingMessage> diggingMessages) {
        this.diggingMessages = diggingMessages;
        
    }
    public DiggingMessageList() {
        
    }
    

    public List<DiggingMessage> getDiggingMessages() {
        return diggingMessages;
    }

    public void setDiggingMessages(List<DiggingMessage> diggingMessages) {
        this.diggingMessages = diggingMessages;
    }
    
    
    
    
    
    
}
