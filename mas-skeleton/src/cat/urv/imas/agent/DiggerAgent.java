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
import cat.urv.imas.behaviour.digger.*;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.*;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
import java.util.ArrayList;
import static java.lang.Math.abs;
import java.util.List;


public class DiggerAgent extends ImasAgent {

    /*      ATTRIBUTES      */
    private AID diggerCoordinatorAgent;
    
    private GameSettings game;
    
    private int[] currentPosition; //This has to be initializaed (TODO Aleix)
    
    private boolean waitingMapFlag = true;
    
    private MetalFieldList currentMFL;
    
    private MetalField currentMF;
    
    private double [] parameters; //{gamma,beta,mu}
    
    private int usedSlots;
    
    
    /*      METHODS     */
    public DiggerAgent() {
        super(AgentType.DIGGER);
    }

    public GameSettings getGame() {
        return game;
    }

    public void setGame(GameSettings game) {
        this.game = game;
    }

    public AID getDiggerCoordinatorAgent() {
        return diggerCoordinatorAgent;
    }

    public int[] getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int[] currentPosition) {
        this.currentPosition = currentPosition;
    }

    public boolean isWaitingMapFlag() {
        return waitingMapFlag;
    }

    public void setWaitingMapFlag(boolean waitingMapFlag) {
        this.waitingMapFlag = waitingMapFlag;
    }

    public MetalFieldList getCurrentMFL() {
        return currentMFL;
    }

    public void setCurrentMFL(MetalFieldList currentMFL) {
        this.currentMFL = currentMFL;
    }

    public MetalField getCurrentMF() {
        return currentMF;
    }

    public void setCurrentMF(MetalField currentMF) {
        this.currentMF = currentMF;
    }

    public int getUsedSlots() {
        return usedSlots;
    }

    public void setUsedSlots(int usedSlots) {
        this.usedSlots = usedSlots;
    }

    public double[] getParameters() {
        return parameters;
    }

    public void setParameters(double[] parameters) {
        this.parameters = parameters;
    }
       
  
    public double[] computeBids(MetalFieldList metalFields){
        
        double[] bids = new double[metalFields.getMetalFields().size()];
        List mfl = this.getCurrentMFL().getMetalFields();
        double carryingbid = -this.getParameters()[2]*1.0*this.usedSlots;
        int EmptySlots = this.game.getDiggersCapacity()-this.usedSlots;
        //TODO: itera cada metalfield i per cada un computa la bid
        for (int i = 0; i < bids.length; i++ ){
            MetalField mf = (MetalField) mfl.get(i);
            double distbid = 1.0*abs(this.currentPosition[0]-mf.getPosition()[0]) + 1.0*abs(this.currentPosition[1]-mf.getPosition()[1]);
            double unitbid = 0;
            if (EmptySlots > mf.getQuantity()){
                 unitbid = this.getParameters()[0]*1.0*mf.getQuantity()/EmptySlots;
            }
            else if (EmptySlots < mf.getQuantity()){
                 unitbid = this.getParameters()[1]*1.0*EmptySlots;
            }
            else{
                 unitbid = this.getParameters()[0]*1.0*mf.getQuantity()/EmptySlots + this.getParameters()[1]*1.0*EmptySlots;
            }
            
            bids[i] = 1.0/distbid + unitbid + carryingbid; //EXEMPLE, S'HA DE FER
        }
        
        return bids;       
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
        sd1.setType(AgentType.DIGGER.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        
        // PROVES! //
        this.currentPosition = new int[] {1,2};
        this.parameters = new double [] {0.5,0.5,0.5};
        this.usedSlots = 0;
                
        
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
        // search CoordinatorAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.DIGGER_COORDINATOR.toString());
        this.diggerCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);        
        
        
        /*      BEHAVIOURS        */
        
        // It triggers ONLY for the voting protocol (Selectivity)
        MessageTemplate mt1 = MessageTemplate.MatchLanguage(MessageContent.SELECTIVITY);
        this.addBehaviour(new SelectivityVotingDA(this, mt1));
        
        // It triggers when the received message is an INFORM.
        MessageTemplate mt2 =MessageTemplate.MatchLanguage(MessageContent.GET_MAP);
        this.addBehaviour(new MapHandlingDA(this, mt2));
    }
    
    

}
