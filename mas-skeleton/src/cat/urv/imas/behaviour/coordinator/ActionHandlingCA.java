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
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.behaviour.prospector.*;
import cat.urv.imas.behaviour.diggercoordinator.*;
import cat.urv.imas.behaviour.coordinator.*;
import cat.urv.imas.behaviour.system.*;
import cat.urv.imas.agent.AgentType;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.CompleteMessage;
import cat.urv.imas.onthology.DiggingMessageList;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.ManufacturingMessageList;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MetalField;
import cat.urv.imas.onthology.MetalFieldList;
import cat.urv.imas.onthology.MovingMessage;
import cat.urv.imas.onthology.MovingMessageList;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This method handles the Map sent from above
 */
public class ActionHandlingCA extends AchieveREResponder {

    /**
     * Sets up the template of messages to catch.
     *
     * @param agent The agent owning this behaviour
     * @param mt Template to receive future responses in this conversation
     */
    public ActionHandlingCA(CoordinatorAgent agent, MessageTemplate mt) {
        super(agent, mt);
        agent.log("Waiting for digger and prospector actions.");
    }
    
    // PARTE 1 DE LA RESPUESTA
    /**
     * Triggers when receives a message following the template
     *
     * @param msg message received.
     */
    @Override
    protected ACLMessage handleRequest(ACLMessage msg) {
        try {
            // Declares the current agent so you can use its getters and setters (and other methods)
            CoordinatorAgent agent = (CoordinatorAgent)this.getAgent();
            if(msg.getContentObject().getClass() == cat.urv.imas.onthology.MetalFieldList.class){
                agent.log("Received Metal Field List from Prospector Coordinator.");
                ACLMessage MFLmsg = new ACLMessage(ACLMessage.INFORM);
                MFLmsg.clearAllReceiver();
                MFLmsg.addReceiver(agent.getDiggerCoordinatorAgent());
                //MFLmsg.setContentObject((MetalFieldList) msg.getContentObject());
                MFLmsg.setLanguage(MessageContent.GET_MAP);
                //agent.getGame().getCellsOfType()
                
                agent.setCurrentMFL((MetalFieldList) msg.getContentObject());
                //List<MetalField> comls = agent.getCompleteMFL().getMetalFields();
                //comls.addAll(agent.getCurrentMFL().getMetalFields());
                //agent.setCompleteMFL(new MetalFieldList(comls));
                agent.combineMFL(agent.getCurrentMFL());
                
                MFLmsg.setContentObject((MetalFieldList)agent.getCompleteMFL());
                
                
                
                
                
                return MFLmsg;
            }
            else if(msg.getContentObject().getClass() == cat.urv.imas.onthology.DiggingMessageList.class){
                agent.setDMList((DiggingMessageList) msg.getContentObject());
                agent.log("Diggig Messages List Received.");
                agent.setFlag(agent.getFlag() + 1);
            }
            else if(msg.getContentObject().getClass() == cat.urv.imas.onthology.ManufacturingMessageList.class){
                agent.setMFMList((ManufacturingMessageList) msg.getContentObject());
                agent.log("Manufacturing Messages List Received.");
                agent.setFlag(agent.getFlag() + 1);
            }
            else if(msg.getContentObject().getClass() == cat.urv.imas.onthology.MovingMessageList.class){
                List<MovingMessage>MML = agent.getMMList().getMovingMessages();
                MML.addAll(((MovingMessageList)msg.getContentObject()).getMovingMessages());
                MovingMessageList aux = new MovingMessageList(MML);
                agent.setMMList(aux);
                agent.setFlag(agent.getFlag() + 1);
                agent.log("Moving Messages List Received.");
            }
            if(agent.getFlag() == 4){
                agent.setFlag(0);
                CompleteMessage commsg = new CompleteMessage(agent.getDMList(),agent.getMMList(), agent.getMFMList(), agent.getCurrentMFL());
                ACLMessage completemsg = new ACLMessage(ACLMessage.INFORM);
                completemsg.clearAllReceiver();
                completemsg.addReceiver(agent.getSystemAgent());
                completemsg.setLanguage(MessageContent.CHOOSE_ACTION);
                completemsg.setContentObject(commsg);
                agent.setMMList(new MovingMessageList());
                agent.log("Turn information sent.");
                return completemsg;
            }
        } catch (UnreadableException ex) {
            Logger.getLogger(ActionHandlingCA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ActionHandlingCA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage response) {
        return null;
    }  
    
    @Override
    public void reset() {
    }

}
