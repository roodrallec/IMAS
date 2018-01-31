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
import cat.urv.imas.behaviour.prospectorcoordinator.*;
import cat.urv.imas.map.Agents;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.map.ActionsRequests;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.InitialGameSettings;
import cat.urv.imas.onthology.MetalField;
import cat.urv.imas.onthology.MetalFieldList;
import cat.urv.imas.onthology.MovingMessage;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ProspectorCoordinatorAgent extends ImasAgent {

    /*      ATTRIBUTES      */
    private AID coordinatorAgent;

    public AID getCoordinatorAgent() {
        return coordinatorAgent;
    }

    public void setCoordinatorAgent(AID coordinatorAgent) {
        this.coordinatorAgent = coordinatorAgent;
    }
    private List<AID> prospectorAgents = new ArrayList<AID>();
    
    //private ActionsRequests cellsWithAgents = new ActionsRequests();;
    
    private GameSettings game;
    
    private int numProspectors = InitialGameSettings.load("game.settings").getAgentList().get(AgentType.PROSPECTOR).size();
    
    private Cell[][] utilityMap;
    
    private boolean utilityMapInitialized = false;
    
    private int msgreceived;
    
    private int movereceived;
    
    private List<MetalFieldList> MFLreceived = new ArrayList<MetalFieldList>();
    
    private List <MovingMessage> MMreceived = new ArrayList<MovingMessage>();

    public List<MetalFieldList> getMFLreceived() {
        return MFLreceived;
    }

    public void setMFLreceived(List<MetalFieldList> MFLreceived) {
        this.MFLreceived = MFLreceived;
    }

    public int getMsgreceived() {
        return msgreceived;
    }

    public void setMsgreceived(int msgreceived) {
        this.msgreceived = msgreceived;
    }

    public int getMovereceived() {
        return movereceived;
    }

    public void setMovereceived(int movereceived) {
        this.movereceived = movereceived;
    }

    public List<MovingMessage> getMMreceived() {
        return MMreceived;
    }

    public void setMMreceived(List<MovingMessage> MMreceived) {
        this.MMreceived = MMreceived;
    }
    
    //public 
    
    /*      METHODS     */  
    public ProspectorCoordinatorAgent() {
        super(AgentType.PROSPECTOR_COORDINATOR);
    }
    
    @SuppressWarnings("empty-statement")
    public Cell[][] applyUtility(Cell[][] newMap) {  
        try{
            if (!this.utilityMapInitialized) {
                this.utilityMap = newMap.clone();
                this.utilityMapInitialized = true;
            }
            //this.cellsWithAgents.clear();
            Agents cellAgents = null;
            boolean auxBol = false;

            // 1. FieldCells utility update            
            for(int row = 0; row < newMap.length; row++) {          
                for(int col = 0; col < newMap[0].length; col++) { 
                    if (newMap[row][col] instanceof FieldCell) {
                        FieldCell indFieldCell = (FieldCell) newMap[row][col];
                        auxBol = indFieldCell.isEmpty();
                        if (auxBol) {
                            ((FieldCell) this.utilityMap[row][col]).incUtilityUnit();
                        }
                    }
                }           
            }

            // 2. Locate all agents positions and reset fieldCells inside visual field utility
            int[] cellPos = new int[] {0,0};
            FieldCell indFieldCell = new FieldCell(0,0);
            for(int row = 0; row < newMap.length; row++) {          
                for(int col = 0; col < newMap[0].length; col++) {                     
                    if (newMap[row][col] instanceof PathCell) { 
                        Cell indCell = (Cell) newMap[row][col];
                        cellAgents = (Agents) ((PathCell) indCell).getAgents();
                        if (cellAgents.get(AgentType.PROSPECTOR).size() > 0) {                        
                            ((PathCell) indCell).resetUtility();
                            cellPos[0] = row; cellPos[1] = col;
                            // add that in this cell there is at least one prospector agent
                            //this.cellsWithAgents.setNewAgent(cellPos, cellAgents.get(AgentType.PROSPECTOR).get(0).getAID());
                            // reset fieldCells inside visual field utility
                            for(int x = -1; x < 2; x++) {          
                                for(int y = -1; y < 2; y++) {
                                    if (newMap[row + x][col + y] instanceof FieldCell) {
                                        indFieldCell = (FieldCell) this.utilityMap[row + x][col + y];
                                        indFieldCell.resetUtility();
                                    }
                                }
                            }
                        } 
//                        if (cellAgents.get(AgentType.DIGGER).size() > 0) {
//                            // add that in this cell there is at least one digger agent
//                            this.cellsWithAgents.setNewAgent(cellPos, cellAgents.get(AgentType.DIGGER).get(0).getAID());
//                        }                   
                    }
                }           
            }

            // 3. PathCells simple utility set
            double utilitySum = 0;
            for(int row = 0; row < newMap.length; row++) {          
                for(int col = 0; col < newMap[0].length; col++) { 
                    if (newMap[row][col] instanceof PathCell) {
                        utilitySum = 0;
                        PathCell indCell = (PathCell) this.utilityMap[row][col];
                        if (!indCell.isThereADiggerAgentWorking()) {
                            for(int x = -1; x < 2; x++) {          
                                for(int y = -1; y < 2; y++) {
                                    if (newMap[row + x][col + y] instanceof FieldCell) {
                                        indFieldCell = (FieldCell) this.utilityMap[row + x][col + y];
                                        utilitySum = utilitySum + indFieldCell.getUtility();
                                    }
                                }
                            }
                            indCell.setUtility(utilitySum);
                        }
                    }
                }           
            }
            
            // 4. Penalize cells whith prospector agents (it is important to do this step before propagation)
            for(int row = 0; row < this.utilityMap.length; row++) {          
                for(int col = 0; col < this.utilityMap[0].length; col++) { 
                    if (this.utilityMap[row][col] instanceof PathCell){
                        Cell indCell = (Cell) newMap[row][col];
                        cellAgents = (Agents) ((PathCell) indCell).getAgents();
                        if (cellAgents.get(AgentType.PROSPECTOR).size() > 0){
                            ((PathCell) this.utilityMap[row][col]).setUtility(-1);
                        }
                    }
                }           
            }
            
            // 5. PathCells propagation utility update
            int pathCellsNum = 0;
            int [][] weightWindow = new int[3][3];
            weightWindow[0][0] = 1; weightWindow[0][1] = 1; weightWindow[0][2] = 1;
            weightWindow[1][0] = 1; weightWindow[1][1] = 2; weightWindow[1][2] = 1;
            weightWindow[2][0] = 1; weightWindow[2][1] = 1; weightWindow[2][2] = 1;
            for(int row = 0; row < newMap.length; row++) {          
                for(int col = 0; col < newMap[0].length; col++) { 
                    if (newMap[row][col] instanceof PathCell) { 
                        PathCell indCell = (PathCell) this.utilityMap[row][col];
                        //if (((PathCell) indCell).getUtility() == 0) {
                            utilitySum = 0;
                            pathCellsNum = 0;
                            for(int x = -1; x < 2; x++) {          
                                for(int y = -1; y < 2; y++) {
                                    if (newMap[row + x][col + y] instanceof PathCell) {
                                        pathCellsNum = pathCellsNum + weightWindow[row + x][col + y];
                                        PathCell surroundingPathCell = (PathCell) this.utilityMap[row + x][col + y];
                                        utilitySum = utilitySum + surroundingPathCell.getUtility() * weightWindow[row + x][col + y];
                                    }
                                }
                            }
                            ((PathCell) indCell).setUtility(utilitySum / pathCellsNum);
                        //}
                    }
                }           
            }
            
            // 6. Set FieldCells and digger agent working cells with negative utility
            for(int row = 0; row < this.utilityMap.length; row++) {          
                for(int col = 0; col < this.utilityMap[0].length; col++) { 
//                    if (this.utilityMap[row][col] instanceof FieldCell) {
//                        ((FieldCell) this.utilityMap[row][col]).setUtility(-1);
//                    }
                    if (this.utilityMap[row][col] instanceof PathCell){
                        if (((PathCell)newMap[row][col]).isThereADiggerAgentWorking()){
                            ((PathCell) this.utilityMap[row][col]).setUtility(-1);
                        }
                    }
                }           
            }
            
        } catch (Exception ex) {
            Logger.getLogger(SystemAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this.utilityMap;
    }
            
    public GameSettings getGame() {
        return game;
    }

    public void setGame(GameSettings game) {
        this.game = game;
    }

    public int getNumProspectors() {
        return numProspectors;
    }

    public List<AID> getProspectorAgents() {
        return prospectorAgents;
    }
    
    public List<MetalField> cleanDuplicatedMFL(){
        int a = 0;
        List<MetalFieldList> listoflist = this.MFLreceived;
        List<MetalField> aux = new ArrayList<MetalField>();
        //for(int i ; iterarenlista)
        for (MetalFieldList listoflist1 : listoflist) {
            List<MetalField> fromone = listoflist1.getMetalFields();
            for (MetalField fromone1 : fromone) {
                Boolean exists = false;
                for (MetalField aux1 : aux) {
                    if ((aux1.getPosition())[0] == (fromone1.getPosition())[0] && (aux1.getPosition())[1] == (fromone1.getPosition())[1]){
                        exists = true;
                    }                            
                }
                if (!(exists)) {
                    aux.add(fromone1);
                }
            }               
        }
            //listoflist.get(i).getMetalFields()
            //for(each de i (iterar en MF)
                //aux.add(each)
        return aux;
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
        sd1.setType(AgentType.PROSPECTOR_COORDINATOR.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);
        this.msgreceived = 0;
        this.movereceived = 0;
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
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);

        
        // search ProspectorAgents
        searchCriterion.setType(AgentType.PROSPECTOR.toString());
        for (int i = 1; i <= this.numProspectors; i++ ){
            searchCriterion.setName("ProspectorAgent"+i);
            this.prospectorAgents.add(UtilsAgents.searchAgent(this, searchCriterion));
        }
        
        
        
        /*      BEHAVIOURS      */
               
        // It triggers when the received message is an INFORM.
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        this.addBehaviour(new MapHandlingPC(this, mt));
        
        
        
    }
}
