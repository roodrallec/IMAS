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

import cat.urv.imas.behaviour.coordinator.*;
import cat.urv.imas.behaviour.system.*;
import cat.urv.imas.agent.AgentType;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.*;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
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
public class MapHandlingDCA extends AchieveREResponder {

    /**
     * Sets up the template of messages to catch.
     *
     * @param agent The agent owning this behaviour
     * @param mt Template to receive future responses in this conversation
     */
    public MapHandlingDCA(DiggerCoordinatorAgent agent, MessageTemplate mt) {
        super(agent, mt);
        agent.log("Waiting for the updated map.");
    }

    /**
     * Triggers when receives a message following the template
     *
     * @param msg message received.
     */
    @Override
    protected ACLMessage handleRequest(ACLMessage msg) {
        // Declares the current agent so you can use its getters and setters (and other methods)
        DiggerCoordinatorAgent agent = (DiggerCoordinatorAgent)this.getAgent();
        try {         
            // If the received message is a map.
            if(msg.getContentObject().getClass().equals(cat.urv.imas.onthology.InitialGameSettings.class)){
                // sets the value of the agents map to the received map.
                agent.setGame((GameSettings) msg.getContentObject());
                agent.log("MAP Updated");
                // Send map to below level
                ACLMessage mapmsg = new ACLMessage(ACLMessage.INFORM);
                mapmsg.clearAllReceiver();
                for (int i = 1; i <= agent.getNumDiggers(); i++ ){
                    mapmsg.addReceiver(agent.getDiggerAgents().get(i-1));
                }
                mapmsg.setContentObject(agent.getGame());
                mapmsg.setLanguage(MessageContent.GET_MAP);
                agent.log("Map sent to underlying level");
                return mapmsg;
            }
            // If the received message is a MetalField List
            else if(msg.getContentObject().getClass().equals(cat.urv.imas.onthology.MetalFieldList.class)){
                // Sets the value of the current MFL to the received one.
                agent.setCurrentMFL((MetalFieldList)msg.getContentObject());
                // Sends the MetalField List to the diggers.
                ACLMessage mflmsg = new ACLMessage(ACLMessage.INFORM);
                mflmsg.setLanguage(MessageContent.SELECTIVITY);
                mflmsg.clearAllReceiver();
                for (int i = 1; i <= agent.getNumDiggers(); i++ ){
                    mflmsg.addReceiver(agent.getDiggerAgents().get(i-1));
                }
                mflmsg.setContentObject(msg.getContentObject());
                agent.log("New metal Fields sent to Diggers.");    
                return mflmsg;
            }
            
            // If the received message is a String
            else{
                switch((String)msg.getContentObject()){
                    // If it is a MAP_RECEIVED
                    case MessageContent.MAP_RECEIVED:
                        return null;     
                }
            }     
        } catch (UnreadableException ex) {
            Logger.getLogger(MapHandlingDCA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MapHandlingDCA.class.getName()).log(Level.SEVERE, null, ex);
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
    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage response) { //Useless method
        return null;
    }

    @Override
    public void reset() {
    }

}
