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
package cat.urv.imas.behaviour.prospector;

import cat.urv.imas.behaviour.diggercoordinator.*;
import cat.urv.imas.behaviour.coordinator.*;
import cat.urv.imas.behaviour.system.*;
import cat.urv.imas.agent.AgentType;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import cat.urv.imas.agent.ProspectorAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.GameMapUtility;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.InitialGameSettings;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.MetalFieldList;
import cat.urv.imas.onthology.MovingMessage;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This method handles the Map sent from above
 */
public class MapHandlingPA extends AchieveREResponder {

    /**
     * Sets up the template of messages to catch.
     *
     * @param agent The agent owning this behaviour
     * @param mt Template to receive future responses in this conversation
     */
    public MapHandlingPA(ProspectorAgent agent, MessageTemplate mt) {
        super(agent, mt);
        agent.log("Waiting for the updated map.");
    }
    
    // PARTE 1 DE LA RESPUESTA
    /**
     * Triggers when receives a message following the template
     *
     * @param msg message received.
     */
    @Override
    protected ACLMessage handleRequest(ACLMessage msg) {
        // Declares the current agent so you can use its getters and setters (and other methods)
        ProspectorAgent agent = (ProspectorAgent)this.getAgent();
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        
        try {
            // If the received message is a map.
            if(msg.getContentObject().getClass() == GameMapUtility.class){
                // sets the value of the agents map to the received map.
                GameSettings game = ((GameMapUtility)msg.getContentObject()).getGame();
                
                List cells = game.getAgentList().get(AgentType.PROSPECTOR);
                boolean found = false;
                for (Object cell : cells) {
                    List cellprospectors = ((PathCell)cell).getAgents().get(AgentType.PROSPECTOR);
                    for (Object prospector : cellprospectors){
                        if (agent.getAID().equals(((InfoAgent)prospector).getAID())){
                            agent.setCurrentPosition(new int[]{((PathCell)cell).getRow(),((PathCell)cell).getCol()});
                            agent.log(Arrays.toString(agent.getCurrentPosition())); 
                            int[] auxMov = new int[2];
                            auxMov[0] = agent.getLastPosition()[0] - agent.getCurrentPosition()[0];
                            auxMov[1] = agent.getLastPosition()[1] - agent.getCurrentPosition()[1];
                            agent.setLastMovementDir(auxMov);
                            agent.setLastPosition(agent.getCurrentPosition());
                            found = true;
                            break;
                        }   
                    }
                    if(found){
                        break;
                    }
                }
                
                Cell[][] map = ((GameMapUtility)msg.getContentObject()).getGame().getMap();
                agent.setMapView(map);
                agent.log("MAP Updated");  
                MetalFieldList currentMFL = agent.searchForMetal();
                agent.log("MetalSearched");
                Cell[][] map2 = ((GameMapUtility)msg.getContentObject()).getUtilitymap();
                agent.setMapView(map2);
                MovingMessage movobj = new MovingMessage(agent.getAID(),agent.move(),agent.getCurrentPosition());
                //agent.log("MetalSearched");
                reply.setContentObject(currentMFL);
                ACLMessage movemsg = new ACLMessage(ACLMessage.INFORM);
                movemsg.clearAllReceiver();
                movemsg.addReceiver(agent.getProspectorCoordinatorAgent());
                movemsg.setContentObject(movobj);
                agent.send(movemsg);
                agent.log("Movement Sent.");
                
            }           
            
        } catch (UnreadableException ex) {
            Logger.getLogger(MapHandlingPA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MapHandlingPA.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return reply;
    }
    
    @Override
    public void reset() {
    }

}