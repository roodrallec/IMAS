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
import cat.urv.imas.behaviour.diggercoordinator.*;
import cat.urv.imas.onthology.InitialGameSettings;
import cat.urv.imas.onthology.*;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The main Coordinator agent. 
 * TODO: This coordinator agent should get the game settings from the System
 * agent every round and share the necessary information to other coordinators.
 */
public class DiggerCoordinatorAgent extends ImasAgent {

    
    
    /*      ATTRIBUTES      */
    private AID coordinatorAgent;
    private List<AID> diggerAgents = new ArrayList<AID>();
   
    private GameSettings game;
    
    private int receivedBids = 0;
    
    private int numDiggers;
    
    private MetalFieldList currentMFL;
    
    private List<double[]> bids ;
    
    
    /*      METHODS     */
    public DiggerCoordinatorAgent() {
        super(AgentType.DIGGER_COORDINATOR);
    }
    
    public void setGame(GameSettings game) {
        this.game = game;
    }

    public GameSettings getGame() {
        return game;
    }

    public int getNumDiggers() {
        return numDiggers;
    }

    public List<AID> getDiggerAgents() {
        return diggerAgents;
    }

    public AID getCoordinatorAgent() {
        return coordinatorAgent;
    }

    public int getReceivedBids() {
        return receivedBids;
    }

    public void setReceivedBids(int receivedBids) {
        this.receivedBids = receivedBids;
    }

    public MetalFieldList getCurrentMFL() {
        return currentMFL;
    }

    public void setCurrentMFL(MetalFieldList currentMFL) {
        this.currentMFL = currentMFL;
    }

    public List<double[]> getBids() {
        return bids;
    }

    public void setBids(List<double[]> bids) {
        this.bids = bids;
    }
    
    
    public int[] metalFieldAssignation(){
        List mfl = this.currentMFL.getMetalFields();
        List bidslist = new ArrayList<double[]>();
        int[] matching = new int[this.getNumDiggers()];
        Arrays.fill(matching,-1);
        bidslist = this.bids;
        double[] onesarray = new double[mfl.size()];
        Arrays.fill(onesarray,-1.0);
        int k = 0;
        while (!mfl.isEmpty()|| k < this.numDiggers){

            k = k+1;
            double maxbid = -1.0;
            int digger = -1;
            int metal = -1;
            for (int i = 0; i < this.numDiggers; i++ ){
                for (int j = 0; j < this.currentMFL.getMetalFields().size(); j++){
                    double[] array = (double[]) bidslist.get(i);
                    if (maxbid < array[j]){
                        maxbid = array[j];
                        digger = i;
                        metal = j;
                    }
                }
            } 
            bidslist.set(digger,onesarray);
            for (int i = 0; i < this.numDiggers; i++ ){
                double [] aux = (double []) bidslist.get(i);
                aux[metal] = -1.0;
                bidslist.set(i,aux);
            }
            if(mfl.size() == 1){
                matching[digger] = metal;
                break;
            }
            mfl.remove(0);
            matching[digger] = metal;
        }
    return matching;  
    }         
    
    
    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {
        this.numDiggers = InitialGameSettings.load("game.settings").getAgentList().get(AgentType.DIGGER).size();
        this.bids= new ArrayList<double[]>();
        

        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.DIGGER_COORDINATOR.toString());
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
        
        /*        SEARCH      */
        
        // search CoordinatorAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        
        // search DiggerAgents
        searchCriterion.setType(AgentType.DIGGER.toString());
        
        for (int i = 1; i <= this.numDiggers; i++ ){
            searchCriterion.setName("DiggerAgent"+i);
            this.diggerAgents.add(UtilsAgents.searchAgent(this, searchCriterion));
            double[] val = {};
            this.bids.add(val);
        }
        // searchAgent is a blocking method, so we will obtain always a correct AID
        
        
        /*      BEHAVIOURS        */
        
        
        // It triggers ONLY for the voting protocol (Selectivity)
        MessageTemplate mt1 = MessageTemplate.MatchLanguage(MessageContent.SELECTIVITY);
        this.addBehaviour(new SelectivityVotingDCA(this, mt1));
        
        // It triggers when the received message is an INFORM.
        MessageTemplate mt2 = MessageTemplate.MatchLanguage(MessageContent.GET_MAP);
        this.addBehaviour(new MapHandlingDCA(this, mt2));

        
        
        
        
    }
}
