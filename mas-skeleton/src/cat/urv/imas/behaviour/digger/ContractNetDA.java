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

import cat.urv.imas.behaviour.coordinator.*;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import cat.urv.imas.agent.DiggerAgent;
import cat.urv.imas.onthology.*;
import cat.urv.imas.onthology.MessageContent;
import jade.domain.FIPANames;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import java.io.IOException;
import java.io.Serializable;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Behaviour for the Coordinator agent to deal with AGREE messages. 
 * The Coordinator Agent sends a REQUEST for the
 * information of the game settings. The System Agent sends an AGREE and 
 * then it informs of this information which is stored by the Coordinator Agent. 
 * 
 * NOTE: The game is processed by another behaviour that we add after the 
 * INFORM has been processed.
 */
public class ContractNetDA extends ContractNetInitiator {

    public ContractNetDA(DiggerAgent agent, ACLMessage requestMsg) {
        super(agent, requestMsg);
        agent.log("Started behaviour to deal with Prospectors responses.");
    }
    
    
    
    
    

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        try {
            DiggerAgent agent = (DiggerAgent)this.getAgent();
            agent.log("all responses received");
            AID bestprospector = new AID();
            int bestdist = 10000000;
            int[] bestdistvec = new int[2];
            for (Object response : responses) {

                int[] prospectorPos = (int[])((ACLMessage)response).getContentObject();
                int dist = (int) (1.0*abs(prospectorPos[0]-agent.getCurrentPosition()[0]) + 1.0*abs(prospectorPos[1]-agent.getCurrentPosition()[1]));
                if (dist < bestdist){
                    bestdist = dist;
                    bestdistvec = new int[]{prospectorPos[0]-agent.getCurrentPosition()[0], prospectorPos[1]-agent.getCurrentPosition()[1]};
                    bestprospector = ((ACLMessage)response).getSender();
                }
                    

            }
            
            // Sends the move message to the digger coordinator
            int[] movement = agent.computeMovement(bestdistvec);
            ACLMessage movemsg = new ACLMessage(ACLMessage.INFORM);
            MovingMessage MMsg = new MovingMessage(agent.getAID(),movement,agent.getCurrentPosition());
            movemsg.clearAllReceiver();
            movemsg.addReceiver(agent.getDiggerCoordinatorAgent());
            movemsg.setContentObject(MMsg);
            movemsg.setLanguage(MessageContent.CHOOSE_ACTION);
            agent.send(movemsg);
            
//            for (Object response : responses) {
//                
//                AID prospector = ((ACLMessage)response).getSender();
//                
//                if (prospector.equals(bestprospector)){
//                    ACLMessage acc = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
//                    acc.addReceiver(prospector);
//                    acceptances.addElement(acc);
//                }
//                else{
//                    ACLMessage acc = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
//                    acc.addReceiver(prospector);
//                    acceptances.addElement(acc); 
//                }
            Enumeration e = responses.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                ACLMessage reply = msg.createReply();
                if (msg.getSender().equals(bestprospector)){
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    acceptances.addElement(reply);
                }
                else{
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acceptances.addElement(reply);
                    
                }
            }           
        } catch (IOException ex) {
            Logger.getLogger(ContractNetDA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnreadableException ex) {
            Logger.getLogger(ContractNetDA.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Agent "+refuse.getSender().getName()+" refused");
    }
    
    protected void handleInform(ACLMessage inform){
        
    }

//    protected void handleFailure(ACLMessage failure) {
//            if (failure.getSender().equals(myAgent.getAMS())) {
//                    // FAILURE notification from the JADE runtime: the receiver
//                    // does not exist
//                    System.out.println("Responder does not exist");
//            }
//            else {
//                    System.out.println("Agent "+failure.getSender().getName()+" failed");
//            }
//            // Immediate failure --> we will not receive a response from this agent
//            nResponders--;
//    }
//
//    protected void handleAllResponses(Vector responses, Vector acceptances) {
//            if (responses.size() < nResponders) {
//                    // Some responder didn't reply within the specified timeout
//                    System.out.println("Timeout expired: missing "+(nResponders - responses.size())+" responses");
//            }
//            // Evaluate proposals.
//            int bestProposal = -1;
//            AID bestProposer = null;
//            ACLMessage accept = null;
//            Enumeration e = responses.elements();
//            while (e.hasMoreElements()) {
//                    ACLMessage msg = (ACLMessage) e.nextElement();
//                    if (msg.getPerformative() == ACLMessage.PROPOSE) {
//                            ACLMessage reply = msg.createReply();
//                            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
//                            acceptances.addElement(reply);
//                            int proposal = Integer.parseInt(msg.getContent());
//                            if (proposal > bestProposal) {
//                                    bestProposal = proposal;
//                                    bestProposer = msg.getSender();
//                                    accept = reply;
//                            }
//                    }
//            }
//            // Accept the proposal of the best proposer
//            if (accept != null) {
//                    System.out.println("Accepting proposal "+bestProposal+" from responder "+bestProposer.getName());
//                    accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
//            }						
//    }
//
//    protected void handleInform(ACLMessage inform) {
//            System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
//    }


}
