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
package cat.urv.imas.agent;

import cat.urv.imas.onthology.*;
import cat.urv.imas.behaviour.coordinator.*;
import cat.urv.imas.onthology.MessageContent;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The main Coordinator agent. 
 * TODO: This coordinator agent should get the game settings from the System
 * agent every round and share the necessary information to other coordinators.
 */
public class CoordinatorAgent extends ImasAgent {

    /**
     * Game settings in use.
     */
    private GameSettings game;
    /**
     * System agent id.
     */
    private AID systemAgent;
    private AID diggerCoordinatorAgent;
    private AID prospectorCoordinatorAgent;
    
    private MetalFieldList currentMFL;
    private MetalFieldList completeMFL = new MetalFieldList(new ArrayList<MetalField>());
    private DiggingMessageList DMList;
    private MovingMessageList MMList = new MovingMessageList();
    private ManufacturingMessageList MFMList;
    private int flag;

    /**
     * Builds the coordinator agent.
     */
    public CoordinatorAgent() {
        super(AgentType.COORDINATOR);
    }

    public AID getSystemAgent() {
        return systemAgent;
    }

    public void setSystemAgent(AID systemAgent) {
        this.systemAgent = systemAgent;
    }

    public AID getDiggerCoordinatorAgent() {
        return diggerCoordinatorAgent;
    }

    public void setDiggerCoordinatorAgent(AID diggerCoordinatorAgent) {
        this.diggerCoordinatorAgent = diggerCoordinatorAgent;
    }

    public AID getProspectorCoordinatorAgent() {
        return prospectorCoordinatorAgent;
    }

    public void setProspectorCoordinatorAgent(AID prospectorCoordinatorAgent) {
        this.prospectorCoordinatorAgent = prospectorCoordinatorAgent;
    }

    public MetalFieldList getCurrentMFL() {
        return currentMFL;
    }

    public void setCurrentMFL(MetalFieldList currentMFL) {
        this.currentMFL = currentMFL;
    }

    public DiggingMessageList getDMList() {
        return DMList;
    }

    public void setDMList(DiggingMessageList DMList) {
        this.DMList = DMList;
    }

    public MovingMessageList getMMList() {
        return MMList;
    }

    public void setMMList(MovingMessageList MMList) {
        this.MMList = MMList;
    }

    public ManufacturingMessageList getMFMList() {
        return MFMList;
    }

    public void setMFMList(ManufacturingMessageList MFMList) {
        this.MFMList = MFMList;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public MetalFieldList getCompleteMFL() {
        return completeMFL;
    }

    public void setCompleteMFL(MetalFieldList completeMFL) {
        this.completeMFL = completeMFL;
    }
    
    
    
    

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.COORDINATOR.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        this.flag = 0;
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }
        
        
        /*      SEARCHS     */

        // search SystemAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SYSTEM.toString());
        this.systemAgent = UtilsAgents.searchAgent(this, searchCriterion);
        
        // search DiggerCoordinator
        searchCriterion.setType(AgentType.DIGGER_COORDINATOR.toString());
        this.diggerCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        
        // search DiggerCoordinator
        searchCriterion.setType(AgentType.PROSPECTOR_COORDINATOR.toString());
        this.prospectorCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        
        
        
        
        
        // searchAgent is a blocking method, so we will obtain always a correct AID

        /*      BEHAVIOURS      */
        
        // Request map to System

        ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
        initialRequest.clearAllReceiver();
        initialRequest.addReceiver(this.systemAgent);
        initialRequest.setProtocol(InteractionProtocol.FIPA_REQUEST);
        log("Request message to agent");
        try {
            initialRequest.setContent(MessageContent.GET_MAP);
            log("Request message content:" + initialRequest.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.addBehaviour(new RequesterBehaviour(this, initialRequest));
        
        MessageTemplate mt =MessageTemplate.MatchLanguage(MessageContent.DIG_ACTION);
        this.addBehaviour(new ActionHandlingCA(this, mt));

        
        MessageTemplate mt2 =MessageTemplate.MatchLanguage(MessageContent.NEW_MAP);
        this.addBehaviour(new MapHandlingCA(this, mt2));
        
        

        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
    }

    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setGame(GameSettings game) {
        this.game = game;
    }

    /**
     * Gets the current game settings.
     *
     * @return the current game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }
    
    public void combineMFL(MetalFieldList newMFL){
        
        List<MetalField> completeMFL = this.completeMFL.getMetalFields();
        List<MetalField> aux = newMFL.getMetalFields();

        for (MetalField mf : aux) {
            Boolean exists = false;
            for (MetalField mfc : completeMFL) {
                if ((mf.getPosition())[0] == (mfc.getPosition())[0] && (mf.getPosition())[1] == (mfc.getPosition())[1]){
                    exists = true;
                }                            
            }
            if (!(exists)) {
                completeMFL.add(mf);
            }
        }
        
        this.setCompleteMFL(new MetalFieldList(completeMFL));
        
    }
        

}
