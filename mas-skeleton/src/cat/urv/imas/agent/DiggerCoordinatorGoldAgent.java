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

import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.behaviour.diggercoordinatorgold.*;
import cat.urv.imas.onthology.InitialGameSettings;
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
public class DiggerCoordinatorGoldAgent extends ImasAgent {

    /*      ATTRIBUTES      */
    private AID diggerCoordinatorAgent;
    private List<AID> diggerAgents = new ArrayList<AID>();
    
    private GameSettings game;
    
    /**
     * Builds the coordinator agent.
     */
    public DiggerCoordinatorGoldAgent() {
        super(AgentType.GOLD_DIGGER_COORDINATOR);
    }

    public GameSettings getGame() {
        return game;
    }

    public void setGame(GameSettings game) {
        this.game = game;
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
        sd1.setType(AgentType.GOLD_DIGGER_COORDINATOR.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        
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
        
        // search DiggerCoordinatorAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.DIGGER_COORDINATOR.toString());
        this.diggerCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        
      
        // search DiggerAgents
        searchCriterion.setType(AgentType.DIGGER.toString());
        int numDiggers = InitialGameSettings.load("game.settings").getAgentList().get(AgentType.DIGGER).size();
        for (int i = 1; i <= numDiggers; i++ ){
            searchCriterion.setName("DiggerAgent"+i);
            this.diggerAgents.add(UtilsAgents.searchAgent(this, searchCriterion));
        }
        
        
        /*      BEHAVIOURS      */    
        // Waits for map from Coordinator and sends it to gold and silver subcoord and diggers.
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        this.addBehaviour(new MapHandling(this, mt));
        
        
    }
}
