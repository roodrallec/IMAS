/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.behaviour.diggercoordinator;

import cat.urv.imas.behaviour.digger.*;
import cat.urv.imas.behaviour.diggercoordinator.*;
import cat.urv.imas.behaviour.coordinator.*;
import cat.urv.imas.behaviour.system.*;
import cat.urv.imas.agent.AgentType;
import cat.urv.imas.onthology.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.map.*;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This method collects movements, manufacturing and digging actions computed by diggers.
 */
public class ChooseActionDCA extends AchieveREResponder {

    /**
     * Sets up the template of messages to catch.
     *
     * @param agent The agent owning this behaviour
     * @param mt Template to receive future responses in this conversation
     */
    public ChooseActionDCA(DiggerCoordinatorAgent agent, MessageTemplate mt) {
        super(agent, mt);
        agent.log("Waiting for the diggers to decide action.");
    }

    /**
     * Triggers when the DCA receives a digging, movement or manufacturing order..
     *
     * @param msg message received.
     * 
     */
    @Override
    protected ACLMessage handleRequest(ACLMessage msg) {
        try {
            // Declares the current agent so you can use its getters and setters (and other methods)
            DiggerCoordinatorAgent agent = (DiggerCoordinatorAgent)this.getAgent();
            if(msg.getContentObject().getClass().equals(MetalField.class)){
                List<DiggingMessage> aux = agent.getCurrentDML();
                DiggingMessage digmes = new DiggingMessage(msg.getSender(),(MetalField)msg.getContentObject());
                aux.add(digmes);
                agent.setCurrentDML(aux);
                agent.log("Received digging petition.");

            }
            else if(msg.getContentObject().getClass().equals(int[].class)){
                List<MovingMessage> aux = agent.getCurrentMML();
                MovingMessage movmes = new MovingMessage(msg.getSender(),(int[])msg.getContentObject());
                aux.add(movmes);
                agent.setCurrentMML(aux);
                agent.log("Received movement petition.");
                
            }
            // Need to be done the same with manufacturing messages
            
            
            int count = agent.getReceivedActions()+1;
            if (count == agent.getNumDiggers()){
                DiggingMessageList dml = new DiggingMessageList(agent.getCurrentDML());
                ACLMessage dmlmsg = new ACLMessage(ACLMessage.INFORM);
                dmlmsg.clearAllReceiver();
                dmlmsg.addReceiver(agent.getCoordinatorAgent());
                dmlmsg.setLanguage(MessageContent.DIG_ACTION);
                dmlmsg.setContentObject(dml);
            }
            else{
                agent.setReceivedActions(count);
            }
            
            
        } catch (UnreadableException ex) {
            Logger.getLogger(ChooseActionDCA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ChooseActionDCA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    

    /*
     * @param msg ACLMessage the received message
     * @param response ACLMessage the previously sent response message
     * @return ACLMessage to be sent as a result notification, of type INFORM
     * when all was ok, or FAILURE otherwise.
     */
    @Override
    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage response) {
        return null;
    }

    @Override
    public void reset() {
    }
    


}
