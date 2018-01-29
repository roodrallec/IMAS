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

import cat.urv.imas.behaviour.digger.*;
import cat.urv.imas.behaviour.coordinator.*;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import cat.urv.imas.agent.ProspectorAgent;
import cat.urv.imas.onthology.*;
import cat.urv.imas.onthology.MessageContent;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.proto.ContractNetResponder;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
public class ContractNetPA extends ContractNetResponder {

    public ContractNetPA(ProspectorAgent agent, MessageTemplate mt) {
        super(agent, mt);
        //agent.log("Started behaviour to wait for coalition petitions.");
    }
    
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        
        try {
            ProspectorAgent agent = (ProspectorAgent)this.getAgent();
            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            propose.setContentObject(agent.getCurrentPosition());
            //agent.log("Bids sent to " + cfp.getSender().getName());
            return propose;
        } catch (IOException ex) {
            Logger.getLogger(ContractNetPA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException{
        ProspectorAgent agent = (ProspectorAgent)this.getAgent();
        ACLMessage reply = accept.createReply();
        reply.setContent(MessageContent.OK);
        
        agent.log("Being followed by" + accept.getSender().getLocalName());
        
        return reply;
    }
    
    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject){
        
    }
    
    
    
    
    protected void handleRejectProposal(ACLMessage reject) {
        int a = 0;
    }
    
    protected void handleAcceptProposal(ACLMessage accept) {
        int a = 0;
    }
    
    

//    protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
//        System.out.println("Agent "+getLocalName()+": Proposal accepted");
//        if (performAction()) {
//                System.out.println("Agent "+getLocalName()+": Action successfully performed");
//                ACLMessage inform = accept.createReply();
//                inform.setPerformative(ACLMessage.INFORM);
//                return inform;
//        }
//        else {
//                System.out.println("Agent "+getLocalName()+": Action execution failed");
//                throw new FailureException("unexpected-error");
//        }	
//    }
//
//    protected void handleRejectProposal(ACLMessage reject) {
//            System.out.println("Agent "+getLocalName()+": Proposal rejected");
//    }
}
