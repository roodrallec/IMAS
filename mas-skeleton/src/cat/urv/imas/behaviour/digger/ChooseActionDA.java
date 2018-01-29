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
import cat.urv.imas.map.*;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import jade.core.AID;
import jade.domain.FIPANames;
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
public class ChooseActionDA extends AchieveREResponder {

    /**
     * Sets up the template of messages to catch.
     *
     * @param agent The agent owning this behaviour
     * @param mt Template to receive future responses in this conversation
     */
    public ChooseActionDA(DiggerAgent agent, MessageTemplate mt) {
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
                    if (agent.getUsedSlots() > 0){
                        //Aplicar el metode per anar al manufacture
                        
                        ManufacturingCenterCell mancencell = agent.chooseManufacturingCenter();
                        int[] manCenDist = new int[]{agent.getCurrentPosition()[0]-mancencell.getRow(),agent.getCurrentPosition()[1]-mancencell.getCol()};
                        
                        if(abs(manCenDist[0]) <= 1 && abs(manCenDist[1]) <= 1){
                            agent.log("No metal assigned. Manufacture.");
                            ACLMessage mfmsg = new ACLMessage(ACLMessage.INFORM);
                            ManufacturingMessage MFMsg = new ManufacturingMessage(agent.getAID(),mancencell,agent.getCurrentPosition());
                            mfmsg.clearAllReceiver();
                            mfmsg.addReceiver(agent.getDiggerCoordinatorAgent());
                            mfmsg.setContentObject(MFMsg);
                            mfmsg.setLanguage(MessageContent.CHOOSE_ACTION);
                        }
                        else{
                            agent.log("No metal assigned. Moving towards manufacturing center.");
                            int[] movement = agent.computeMovement(manCenDist);
                            MovingMessage MMsg = new MovingMessage(agent.getAID(),movement,agent.getCurrentPosition());
                            ACLMessage movemsg = new ACLMessage(ACLMessage.INFORM);
                            movemsg.clearAllReceiver();
                            movemsg.addReceiver(agent.getDiggerCoordinatorAgent());
                            movemsg.setContentObject(MMsg);
                            movemsg.setLanguage(MessageContent.CHOOSE_ACTION);
                            return movemsg;   
                        }
                    }
                    else{
                        agent.log("No metal assigned. Follow Prospector.");
                        agent.log("Current Position:" + Arrays.toString(agent.getCurrentPosition()));
                        
                        List<AID> candidates = agent.getProspectorAgents();
                        ACLMessage cnmsg = new ACLMessage(ACLMessage.CFP);
                        for (int i = 0; i < candidates.size(); i++){
                            cnmsg.addReceiver(candidates.get(i));
                        }
                        cnmsg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                        cnmsg.setContent(MessageContent.COALITION);
                        
                        agent.addBehaviour(new ContractNetDA(agent,cnmsg));
                                // contract net;
                    }
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
                        ACLMessage digmsg = new ACLMessage(ACLMessage.INFORM);
                        DiggingMessage DMsg = new DiggingMessage(agent.getAID(),agent.getCurrentMF(),agent.getCurrentPosition());
                        digmsg.clearAllReceiver();
                        digmsg.addReceiver(agent.getDiggerCoordinatorAgent());
                        digmsg.setContentObject(DMsg);
                        digmsg.setLanguage(MessageContent.CHOOSE_ACTION);
                        return digmsg;
                    }
                    else{
                        agent.log("Moving toward metal field.");
                        int[] movement = agent.computeMovement(distance);
                        MovingMessage MMsg = new MovingMessage(agent.getAID(),movement,agent.getCurrentPosition());
                        ACLMessage movemsg = new ACLMessage(ACLMessage.INFORM);
                        movemsg.clearAllReceiver();
                        movemsg.addReceiver(agent.getDiggerCoordinatorAgent());
                        movemsg.setContentObject(MMsg);
                        movemsg.setLanguage(MessageContent.CHOOSE_ACTION);
                        return movemsg;
                    }
                    
                    
                    
                }
            }
            
            
        } catch (UnreadableException ex) {
            Logger.getLogger(ChooseActionDA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ChooseActionDA.class.getName()).log(Level.SEVERE, null, ex);
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
