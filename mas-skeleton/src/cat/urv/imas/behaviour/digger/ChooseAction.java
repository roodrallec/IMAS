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
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This method waits for the voting to end
 */
public class ChooseAction extends AchieveREResponder {

    /**
     * Sets up the template of messages to catch.
     *
     * @param agent The agent owning this behaviour
     * @param mt Template to receive future responses in this conversation
     */
    public ChooseAction(DiggerAgent agent, MessageTemplate mt) {
        super(agent, mt);
        agent.log("Waiting for the voting to finish.");
    }

    /**
     * Triggers when the Digger receives its assignation or NO assignation.
     *
     * @param msg message received.
     * 
     */
    @Override
    protected ACLMessage handleRequest(ACLMessage msg) {
        // Declares the current agent so you can use its getters and setters (and other methods)
        DiggerAgent agent = (DiggerAgent)this.getAgent();
        try {
            // If the received messag is an integer.
            if(msg.getContentObject().getClass().equals(Integer.class)){
                int metalF = (int) msg.getContentObject();
                if (metalF == -1){
                    agent.log("No metal assigned. Follow Prospector.");
                    // Contract Net
                }
                else{
                    List mf = agent.getCurrentMFL().getMetalFields();
                    agent.setCurrentMF((MetalField) mf.get(metalF));
                    agent.log("Metal Field Assigned [x,y]: " + Arrays.toString(agent.getCurrentMF().getPosition()));
                    
                    int[] distance = new int[2]; 
                    distance[0] = ((MetalField)mf.get(metalF)).getPosition()[0] - agent.getCurrentPosition()[0];
                    distance[1] = ((MetalField)mf.get(metalF)).getPosition()[1] - agent.getCurrentPosition()[1];
                    
                    if(abs(distance[0]) <= 1 && abs(distance[1]) <= 1){
                        agent.log("Mine.");
                    }
                    else{
                        agent.log("Moving toward metal field.");
                        int[] movement = agent.computeMovement(distance);
                    }
                    
                    
                    
                }
            }
            
            
        } catch (UnreadableException ex) {
            Logger.getLogger(ChooseAction.class.getName()).log(Level.SEVERE, null, ex);
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
