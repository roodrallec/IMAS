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
//import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.map.*;
import cat.urv.imas.behaviour.prospector.*;
//import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.*;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
import java.util.Map;



public class ProspectorAgent extends ImasAgent {

    /*      ATTRIBUTES      */
    private AID prospectorCoordinatorAgent;
    
    private Cell[] mapView = new Cell[8];
        
    private int[] currentPosition; //This has to be initializaed (TODO Aleix)
    
    private ArrayList<MetalField> currentMetalFields = new ArrayList<MetalField>();
 
    /*      METHODS     */
    public ProspectorAgent() {
        super(AgentType.PROSPECTOR);
    }
/* Takes a whole map and stores just the agents view */
    public MetalFieldList setMapView(Cell[][] map) {         
        this.currentPosition = new int[] {1,5};
        int row = this.currentPosition[0];
        int column = this.currentPosition[1];      
        int idx = 0;
        //MetalField current = new MetalField();
        //Cell submapita;
        //int a;
        for(int r=row-1; r <= row+1; r++) {
            for(int c=column-1; c <= column+1; c++) {
                if (!(r == row && c == column)){                  
                    mapView[idx] = map[r][c];
                    //After all, change to correct values -> r and c
                    if (mapView[idx] instanceof SettableFieldCell){
                        if ((((SettableFieldCell) (map[r][c])).detectMetal()).size() == 1){
                            //Size
                            int quantity = (int) ((((SettableFieldCell) (map[r][c])).detectMetal().values().toArray())[0]);
                            //MetalType
                            String metal = ((((SettableFieldCell) (map[r][c])).detectMetal().keySet().toArray())[0]).toString();
                            //Location R and C
                            int[] metalLocation = {r, c};
                            //MetalField
                            MetalField currentMetal = new MetalField(metalLocation, metal, quantity);
                            currentMetalFields.add(currentMetal);
                        }
                       
                    }  
                    idx++;
                }
            }
        } 
        MetalFieldList currentMFL = new MetalFieldList(currentMetalFields);
        return currentMFL;
    }

    public void searchForMetal() {
        for(Cell c: this.mapView) {
            if (c instanceof SettableFieldCell) {
                ((SettableFieldCell) c).detectMetal();
            }            
        }
    }   
    
    public Cell[] getMapView() {
        return mapView;
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
        
        
        
        /*      BEHAVIOURS        */        
        // It triggers when the received message is an INFORM.
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        this.addBehaviour(new MapHandling(this, mt));
    }
}
