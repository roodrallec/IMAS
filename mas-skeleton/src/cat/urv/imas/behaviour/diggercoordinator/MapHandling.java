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
 * A request-responder behaviour for System agent, answering to queries
 * from the Coordinator agent. The Coordinator Agent sends a REQUEST of the whole
 * game information and the System Agent sends an AGREE and then an INFORM
 * with the city information.
 */
public class MapHandling extends AchieveREResponder {

    /**
     * Sets up the System agent and the template of messages to catch.
     *
     * @param agent The agent owning this behaviour
     * @param mt Template to receive future responses in this conversation
     */
    public MapHandling(DiggerCoordinatorAgent agent, MessageTemplate mt) {
        super(agent, mt);
        agent.log("Waiting for the updated map.");
    }

    /**
     * When System Agent receives a REQUEST message, it agrees. Only if
     * message type is AGREE, method prepareResultNotification() will be invoked.
     *
     * @param msg message received.
     * @return AGREE message when all was ok, or FAILURE otherwise.
     */
    @Override
    protected ACLMessage handleRequest(ACLMessage msg) {
        try {
            DiggerCoordinatorAgent agent = (DiggerCoordinatorAgent)this.getAgent();
            
            // If receives a MAP
            if(msg.getContentObject().getClass().equals(cat.urv.imas.onthology.InitialGameSettings.class)){
                try { // Receive map from above level
                    agent.setGame((GameSettings) msg.getContentObject());
                    agent.log("MAP Updated");
                } catch (Exception e) {
                    agent.log("ERROR while updating map.");
                    agent.errorLog(e.getMessage());
                    e.printStackTrace();
                }
                try { // Send map to below level
                    ACLMessage mapmsg = new ACLMessage(ACLMessage.INFORM);
                    mapmsg.clearAllReceiver();
                    for (int i = 1; i <= agent.getNumDiggers(); i++ ){
                        mapmsg.addReceiver(agent.getDiggerAgents().get(i-1));
                    }
                    mapmsg.addReceiver(agent.getGoldDiggerCoordinatorAgent());
                    mapmsg.addReceiver(agent.getSilverDiggerCoordinatorAgent());
                    mapmsg.setContentObject(agent.getGame());
                    //agent.send(mapmsg);
                    agent.log("Map sent to underlying level");
                    return mapmsg;
                } catch (IOException ex) {
                    agent.log("Map could not be send.");
                    Logger.getLogger(MapHandling.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            // If receives a MetalField List
            else if(msg.getContentObject().getClass().equals(cat.urv.imas.onthology.MetalFieldList.class)){
                ACLMessage mapmsg = new ACLMessage(ACLMessage.INFORM);
                mapmsg.clearAllReceiver();
                for (int i = 1; i <= agent.getNumDiggers(); i++ ){
                    mapmsg.addReceiver(agent.getDiggerAgents().get(i-1));
                }
                try {
                    mapmsg.setContentObject(msg.getContentObject());
                    agent.send(mapmsg);
                    agent.log("New metal Fields sent to Diggers.");
                } catch (IOException ex) {
                    Logger.getLogger(MapHandling.class.getName()).log(Level.SEVERE, null, ex);
                    agent.log("Error sending new metal fields list.");
                }              
            }
            
            
            
            else{
                switch((String)msg.getContentObject()){
                    case MessageContent.MAP_RECEIVED:
                        int updatedmaps = agent.getUpdatedmaps() + 1;
                        if(updatedmaps < agent.getNumDiggers()){
                            agent.setUpdatedmaps(updatedmaps);
                        }
                        else{
                            agent.setUpdatedmaps(0);
                            agent.log("All diggers have map updated.");
                        }
                }
            }
            
        } catch (UnreadableException ex) {
            Logger.getLogger(MapHandling.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * After sending an AGREE message on prepareResponse(), this behaviour
     * sends an INFORM message with the whole game settings.
     *
     * NOTE: This method is called after the response has been sent and only when one
     * of the following two cases arise: the response was an agree message OR no
     * response message was sent.
     *
     * @param msg ACLMessage the received message
     * @param response ACLMessage the previously sent response message
     * @return ACLMessage to be sent as a result notification, of type INFORM
     * when all was ok, or FAILURE otherwise.
     */
    /*
    @Override
    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage response) { //Useless method
        return null;

    }
    */
    /**
     * No need for any specific action to reset this behaviour
     */
    @Override
    public void reset() {
    }

}
