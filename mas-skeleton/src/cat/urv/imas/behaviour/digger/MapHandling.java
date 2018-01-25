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
package cat.urv.imas.behaviour.digger;

import cat.urv.imas.behaviour.diggercoordinator.*;
import cat.urv.imas.behaviour.coordinator.*;
import cat.urv.imas.behaviour.system.*;
import cat.urv.imas.agent.AgentType;
import cat.urv.imas.onthology.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import cat.urv.imas.agent.DiggerAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
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
    public MapHandling(DiggerAgent agent, MessageTemplate mt) {
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
        DiggerAgent agent = (DiggerAgent)this.getAgent();
        try {
            // If receives a MAP from DiggerCoordinator.
            if(msg.getContentObject().getClass() == cat.urv.imas.onthology.InitialGameSettings.class){
                try { // Receive map from above level
                    agent.setGame((GameSettings) msg.getContentObject());
                    agent.log("MAP Updated");
                    ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                    reply.addReceiver(agent.getDiggerCoordinatorAgent());
                    reply.setContentObject(MessageContent.MAP_RECEIVED);
                    //agent.send(reply);
                } catch (Exception e) {
                    agent.log("ERROR while updating map.");
                    agent.errorLog(e.getMessage());
                    e.printStackTrace();
                }
            }
            
            
            // If receives the new MetalFields List from the DiggerCoordinator.
            if(msg.getContentObject().getClass().equals(cat.urv.imas.onthology.MetalFieldList.class)){
                MetalFieldList metalFields = (MetalFieldList)msg.getContentObject();
                agent.log("MetalFieldList received. Computing bids...");
                float[] bids = agent.computeBids(metalFields);
                ACLMessage bidmsg = new ACLMessage(ACLMessage.INFORM);
                bidmsg.addReceiver(agent.getDiggerCoordinatorAgent());
                bidmsg.setContentObject(bids);
                agent.send(bidmsg);
                agent.log("Bids sent to DiggerCoordinator.");
                
            }
            
            
        } catch (UnreadableException ex) {
            Logger.getLogger(MapHandling.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
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
    
    @Override
    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage response) { //Useless method

        // it is important to make the createReply in order to keep the same context of
        // the conversation
        DiggerAgent agent = (DiggerAgent)this.getAgent();
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);

        try {
            reply.setContentObject(agent.getGame());
        } catch (Exception e) {
            reply.setPerformative(ACLMessage.FAILURE);
            agent.errorLog(e.toString());
            e.printStackTrace();
        }
        //agent.log("Game settings sent");
        return null;

    }

    /**
     * No need for any specific action to reset this behaviour
     */
    @Override
    public void reset() {
    }
    


}
