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
import java.util.ArrayList;
import cat.urv.imas.map.*;
import cat.urv.imas.behaviour.prospector.*;
import cat.urv.imas.onthology.*;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import java.util.List;
import java.util.Random;

public class ProspectorAgent extends ImasAgent {

    /*      ATTRIBUTES      */
    private AID prospectorCoordinatorAgent;
    
    private Cell[] mapView = new Cell[9];
        
    private int[] currentPosition = new int[]{1,1}; 
    
    private ArrayList<MetalField> currentMetalFields = new ArrayList<MetalField>();
 
    private List<AID> diggerAgents = new ArrayList<AID>();
    
    private int numDiggers = InitialGameSettings.load("game.settings").getAgentList().get(AgentType.DIGGER).size();
    
    
    
    /*      METHODS     */
    public ProspectorAgent() {
        super(AgentType.PROSPECTOR);
    }
/* Takes a whole map and stores just the agents view */
    public void setMapView(Cell[][] map) {       
        int row = this.currentPosition[0];
        int column = this.currentPosition[1];    
        int idx = 0;
        for(int r=row-1; r <= row+1; r++) {
            for(int c=column-1; c <= column+1; c++) {                               
                mapView[idx] = map[r][c];                
                idx++;
            }
        }         
    }
    
    public Cell[] getMapView() {
        return this.mapView;
    }            

    public MetalFieldList searchForMetal() {
        int[] metalLocation = {};
        int quantity = 0;
        String metal = "";
        for(Cell c: this.mapView) {
            if (c instanceof SettableFieldCell){
                SettableFieldCell fc = (SettableFieldCell)(c);
                if (fc.detectMetal().size() == 1) {                    
                    quantity = (int) (fc.detectMetal().values().toArray()[0]);                    
                    metal = (fc.detectMetal().keySet().toArray())[0].toString();
                    metalLocation[0] = c.getRow();                   
                    metalLocation[1] = c.getCol();
                    MetalField currentMetal = new MetalField(metalLocation, metal, quantity);
                    currentMetalFields.add(currentMetal);
                }
            }         
        }        
        return new MetalFieldList(currentMetalFields);
    }   
    
    public void shuffleView() {
        int n = this.mapView.length;
        Random random = new Random();
        random.nextInt();
        for (int i = 0; i < n; i++) {
            int change = i + random.nextInt(n - i);
            Cell helper = this.mapView[i];
            this.mapView[i] = this.mapView[change];
            this.mapView[change] = helper;
        }
    }
    
    public int[] move() {                  
        this.shuffleView(); // Randomizes movement when there's equal utility
        int maxCellUtility = -1;
        int[] movement = new int[2];
        for(Cell c: this.mapView) {
            if (c instanceof PathCell) {
                PathCell pc = (PathCell)(c);                            
                if (pc.getUtility() > maxCellUtility) {
                    maxCellUtility = pc.getUtility();
                    movement[0] = c.getRow()- this.currentPosition[0];
                    movement[1] = c.getCol()- this.currentPosition[1];
                }
            }
        }
        return movement;
    }
    
    public AID getProspectorCoordinatorAgent() {
        return prospectorCoordinatorAgent;
    }

    public void setProspectorCoordinatorAgent(AID prospectorCoordinatorAgent) {
        this.prospectorCoordinatorAgent = prospectorCoordinatorAgent;
    }

    public int[] getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int[] currentPosition) {
        this.currentPosition = currentPosition;
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
        sd1.setType(AgentType.PROSPECTOR.toString());
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
        // search ProspectorCoordinatorAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.PROSPECTOR_COORDINATOR.toString());
        this.prospectorCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        
        // search DiggerAgent
        searchCriterion.setType(AgentType.DIGGER.toString());
        for (int i = 1; i <= this.numDiggers; i++ ){
            searchCriterion.setName("DiggerAgent"+i);
            this.diggerAgents.add(UtilsAgents.searchAgent(this, searchCriterion));
        }
        
        
        
        
        /*      BEHAVIOURS        */        
        // It triggers when the received message is an INFORM.
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        this.addBehaviour(new MapHandling(this, mt));
        
        for (int i = 0; i < diggerAgents.size(); i++){
        MessageTemplate mt2 = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP)),MessageTemplate.MatchSender(diggerAgents.get(i)));
        
        this.addBehaviour(new ContractNetPA(this,mt2));
        }
    }
}
