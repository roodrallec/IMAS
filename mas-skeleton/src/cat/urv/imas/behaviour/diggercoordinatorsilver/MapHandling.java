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
package cat.urv.imas.behaviour.diggercoordinatorsilver;

import cat.urv.imas.behaviour.diggercoordinator.*;
import cat.urv.imas.behaviour.coordinator.*;
import cat.urv.imas.behaviour.system.*;
import cat.urv.imas.agent.AgentType;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import cat.urv.imas.agent.DiggerCoordinatorSilverAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
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
    public MapHandling(DiggerCoordinatorSilverAgent agent, MessageTemplate mt) {
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
    protected ACLMessage prepareResponse(ACLMessage msg) {
        DiggerCoordinatorSilverAgent agent = (DiggerCoordinatorSilverAgent)this.getAgent(); 
        try {
            if(msg.getContentObject().getClass() == cat.urv.imas.onthology.InitialGameSettings.class){
                ACLMessage reply = msg.createReply(); //Trash
                reply.setPerformative(ACLMessage.UNKNOWN); //Trash
                try { // Receive map from above level
                    agent.setGame((GameSettings) msg.getContentObject());
                    agent.log("MAP Updated");
                } catch (Exception e) {
                    agent.log("ERROR while updating map.");
                    agent.errorLog(e.getMessage());
                    e.printStackTrace();
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
    @Override
    protected ACLMessage prepareResultNotification(ACLMessage msg, ACLMessage response) { //Useless method

        // it is important to make the createReply in order to keep the same context of
        // the conversation
        DiggerCoordinatorSilverAgent agent = (DiggerCoordinatorSilverAgent)this.getAgent();
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
