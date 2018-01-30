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

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.onthology.*;
import cat.urv.imas.gui.GraphicInterface;
import cat.urv.imas.behaviour.system.*;
//import cat.urv.imas.map.Cell;
import cat.urv.imas.map.*;
//import jade.Boot;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
//
/**
 * System agent that controls the GUI and loads initial configuration settings.
 * TODO: You have to decide the onthology and protocol when interacting among
 * the Coordinator agent.
 */
public class SystemAgent extends ImasAgent {

    /**
     * GUI with the map, system agent log and statistics.
     */
    private GraphicInterface gui;
    /**
     * Game settings. At the very beginning, it will contain the loaded
     * initial configuration settings.
     */
    private InitialGameSettings game;
    /**
     * Current turn map.
     */
    private Cell[][] currentMap;// = (Cell[][]) this.game.getMap();
    public Cell[][] getCurrentMap() {
        return currentMap;
    }
    public void setCurrentMap(Cell[][] currentMap) {
        this.currentMap = currentMap;
    }
    /**
     * Requested map. The map that coordinator agent retrieve to System Agent. System Agent has to check if it is possible.
     */
    private Cell[][] requestedMap; // = (Cell[][]) this.game.getMap();
    public Cell[][] getRequestedMap() {
        return requestedMap;
    }
    public void setRequestedMap(Cell[][] requestedMap) {
        this.requestedMap = requestedMap;
    }
    /**
     * The Coordinator agent with which interacts sharing game settings every round.
     */
    private AID coordinatorAgent;
    
    /**
     * currentWorkingDiggers will contain the AID and position of all diggers that are currently digging, this will
     * be used in order to detect when a digger in this list request a new position and then System Agent will know
     * that it is needed to free the cell where the digger was working.
     */
    private ActionsRequests currentWorkingDiggers = new ActionsRequests();
    /**
     * diggingRequests will contain the requesting diggers AID and current positions of metal fields to dig.
     */
    private List<DiggingMessage> diggingRequests = new ArrayList<DiggingMessage>();

    public List<DiggingMessage> getDiggingRequests() {
        return diggingRequests;
    }

    public void setDiggingRequests(List<DiggingMessage> diggingRequests) {
        this.diggingRequests = diggingRequests;
    }

    /**
     * requestedAgentsPos will contain the requested positions of next turn for all mobile agents.
     */
    private List<MovingMessage> requestedAgentsPos = new ArrayList<MovingMessage>();

    public List<MovingMessage> getRequestedAgentsPos() {
        return requestedAgentsPos;
    }

    public void setRequestedAgentsPos(List<MovingMessage> requestedAgentsPos) {
        this.requestedAgentsPos = requestedAgentsPos;
    }

    /**
     * manufactureRequests will contain the requesting diggers AID and current positions of manufacturing centers to manufacture.
     */
    private List<ManufacturingMessage> manufactureRequests = new ArrayList<ManufacturingMessage>();

    public List<ManufacturingMessage> getManufactureRequests() {
        return manufactureRequests;
    }

    public void setManufactureRequests(List<ManufacturingMessage> manufactureRequests) {
        this.manufactureRequests = manufactureRequests;
    }

    private List<MetalField> metalFieldList = new ArrayList<MetalField>();

    public List<MetalField> getMetalFieldList() {
        return metalFieldList;
    }

    public void setMetalFieldList(List<MetalField> metalFieldList) {
        this.metalFieldList = metalFieldList;
    }
    /**
     * undiscoveredMetalField will contain the undiscovered metal fields until a prospector discovers them.
     */
    private MetalFieldsTurnsNew undiscoveredMetalField = new MetalFieldsTurnsNew();
    /**
     * discoveredMetalField will contain the discovered metal fields until a digger begin to dig them.
     */
    private MetalFieldsTurnsNew discoveredMetalField = new MetalFieldsTurnsNew(); 
    /**
     * Game settings. The game with the updated changes that the system agent
     * is constructing while checking that all changes are allowed.
     */
    private GameSettings nextTurnGame;
    /**
     * Path cell info.
     */
    private PathCell pathCell;
    /**
     * Game performance info.
     */
    private GamePerformanceIndicators gamePerformanceIndicators = new GamePerformanceIndicators();
    
    public AID getCoordinatorAgent() {
        return coordinatorAgent;
    }

    public void setCoordinatorAgent(AID coordinatorAgent) {
        this.coordinatorAgent = coordinatorAgent;
    }    
    
    /**
     * Builds the System agent.
     */
    public SystemAgent() {
        super(AgentType.SYSTEM);
    }

    /**
     * A message is shown in the log area of the GUI, as well as in the
     * stantard output.
     *
     * @param log String to show
     */
    @Override
    public void log(String log) {
        if (gui != null) {
            gui.log(getLocalName()+ ": " + log + "\n");
        }
        super.log(log);
    }

    /**
     * An error message is shown in the log area of the GUI, as well as in the
     * error output.
     *
     * @param error Error to show
     */
    @Override
    public void errorLog(String error) {
        if (gui != null) {
            gui.log("ERROR: " + getLocalName()+ ": " + error + "\n");
        }
        super.errorLog(error);
    }

    /**
     * Gets the game settings.
     *
     * @return game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }

    /**
     * Adds (if probability matches) new elements onto the map
     * for every simulation step.
     * This method is expected to be run from the corresponding Behaviour
     * to add new elements onto the map at each simulation step.
     */
    public MetalFieldsTurnsNew addElementsForThisSimulationStep(MetalFieldsTurnsNew undiscoveredMetalList) {
        return this.game.addElementsForThisSimulationStep(undiscoveredMetalList);
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {
        
        /* ** Very Important Line (VIL) ************************************* */
        this.setEnabledO2ACommunication(true, 1);

        // 1. Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.SYSTEM.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " failed registration to DF [ko]. Reason: " + e.getMessage());
            doDelete();
        }

        // 2. Load game settings.        
        this.game = InitialGameSettings.load("game.settings");
        //GenerateGameSettings.defineSettings(this.game);
        this.currentMap = this.game.getMap();
        log("Initial configuration settings loaded");

        // 3. Load GUI
        try {
            this.gui = new GraphicInterface(game);
            gui.setVisible(true);
            log("GUI loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }        
        
        // 4. Load all agents defined in game.settings
        
        jade.wrapper.AgentContainer container = this.getContainerController();
               
        ServiceDescription searchCriterion = new ServiceDescription(); 
                      
        int[][] initialMap = this.game.getInitialMap();
        //this.currentMap = this.game.getMap();                
                
        int diggersCount = 1;
        int prospectorsCount = 1;

        AID agentID = new AID();
        
        Map initialAgentsList = this.game.getAgentList();        
        
        /* Display content using Iterator*/
        Set outerSet = initialAgentsList.entrySet();
        Iterator outer = outerSet.iterator();
        
        InfoAgent infoAg = null;
        PathCell currentCell = null;
        String agentName = null;
        String agentPrefix = null;
        
        try {            
            while(outer.hasNext()) {
               Map.Entry agentsList = (Map.Entry)outer.next();           
               ArrayList innerSet = (ArrayList) agentsList.getValue();

                for (Object agCell : innerSet) {
                   currentCell = (PathCell) agCell;
                   infoAg = (InfoAgent) currentCell.getAgents().getFirst();

                   if (infoAg.getType() == AgentType.DIGGER){
                       agentPrefix = "DiggerAgent";
                       agentName = agentPrefix + diggersCount;
                       diggersCount++;
                   } else {
                       agentPrefix = "ProspectorAgent";
                       agentName = agentPrefix + prospectorsCount;
                       prospectorsCount++;
                   }

                   UtilsAgents.createAgent(container, agentName, "cat.urv.imas.agent." + agentPrefix , null);
                   //Search the new created agent AID
                   searchCriterion.setName(agentName);
                   agentID = UtilsAgents.searchAgent(this, searchCriterion);                                  

                   // Set the agent AID, without modifying agent type, after setting AID all agents will be able to know the position of all agents in the current map
                   infoAg.setAID(agentID);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(SystemAgent.class.getName()).log(Level.SEVERE, null, ex);
        }       
                
        // search CoordinatorAgent
        searchCriterion.setName(null);

        UtilsAgents.createAgent(container,"DiggerCoordinatorAgent","cat.urv.imas.agent.DiggerCoordinatorAgent" , null);
        UtilsAgents.createAgent(container,"ProspectorCoordinatorAgent","cat.urv.imas.agent.ProspectorCoordinatorAgent" , null);
        
        UtilsAgents.createAgent(container,"CoordinatorAgent","cat.urv.imas.agent.CoordinatorAgent" , null);
                
        // search CoordinatorAgent
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID

        // add behaviours
        // we wait for the initialization of the game
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        this.addBehaviour(new RequestResponseBehaviour(this, mt));
        
        // this behaviour will wait until coordinator agent send the requested actions
        MessageTemplate mt2 = MessageTemplate.MatchLanguage(MessageContent.CHOOSE_ACTION);
        this.addBehaviour(new ChooseActionSA(this,mt2));

        // Setup finished. When the last inform is received, the agent itself will add
        // a behaviour to send/receive actions
    }
    
    // Function to update game
    public void updateGUI() {
        this.gui.updateGame();
    }
    
    // Function to go one step ahead
    public void incrementStep() {

        // Update statistics window

        this.gui.showStatistics(this.gamePerformanceIndicators);

        
        this.undiscoveredMetalField.incrementTurn();
        this.discoveredMetalField.incrementTurn();
        // Substract one remaining turn
        this.game.setSimulationSteps(this.game.getSimulationSteps() - 1);
        
        this.game.setTitle("TURNS LEFT: " + String.valueOf(this.game.getSimulationSteps()));
        this.log(this.game.getTitle());
        // END GAME
        if (this.game.getSimulationSteps() == 0){
            this.log("GAME OVER");
            this.doDelete();
        }
        
        // ADD new metal fields
//        this.game.getMaxAmountOfNewMetal()
//        this.game.getMaxNumberFieldsWithNewMetal()
//        this.game.getNewMetalProbability()
        this.undiscoveredMetalField = this.addElementsForThisSimulationStep(this.undiscoveredMetalField);
        
        this.updateGUI();
    }
    
    public void checkTurnChanges() throws Exception {
        boolean result = false;
        AID agentID = new AID();
        int[] agentPos = null;
        PathCell currentCell = null;
        
        try {
            // Current turn map
            Cell[][] currentMap = this.currentMap;
            // Map where allowed changes will be reflected, at the end of this function, it will be the next turn map to pass to Coordinator Agent
            Cell[][] nextTurnMap = currentMap.clone();
            
            Map currentAgentList = this.game.getAgentList();
            
            //1. Set up diggers working
            while (this.diggingRequests.size() > 0){

                // get agent name from it's AID
                AID diggerID = this.diggingRequests.get(0).getDigger();
                int[] metalFieldPos = this.diggingRequests.get(0).getMetalfield().getPosition();
                int[] diggerPos = this.diggingRequests.get(0).getPosition();

                // Remove 1 metal unit from metal field
                FieldCell metalFieldCell = (FieldCell) nextTurnMap[metalFieldPos[0]][metalFieldPos[1]];
                double remainingMetalUnits = (int) metalFieldCell.getMetal().values().toArray()[0];
                
                if (remainingMetalUnits < 2.0){
                    this.gamePerformanceIndicators.addTurnsForDiggingMetal(this.discoveredMetalField.getMetalField(metalFieldCell));
                    this.discoveredMetalField.removeMetalField(metalFieldCell);
                }                
                
                metalFieldCell.removeMetal();
                this.gamePerformanceIndicators.addMetalUnits(1.0);

                // Set digger agent working in the path cell
                PathCell diggerCell = (PathCell) nextTurnMap[diggerPos[0]][diggerPos[1]];
                diggerCell.setDiggerAgentWorking();
                
                // Add digger to the current working diggers
                this.currentWorkingDiggers.setNewAgent(diggerPos, diggerID);
               
                this.diggingRequests.remove(0);
            }
            
            //2. Free cells where digger agents have finished working            
            for (int agentIndex = 0; agentIndex < this.requestedAgentsPos.size(); agentIndex++){
                agentID = this.requestedAgentsPos.get(agentIndex).getAgentID();                

                if (this.currentWorkingDiggers.getAllAgentsAID().contains(agentID)){
                    agentPos = this.currentWorkingDiggers.getAgentById(agentID);
                    currentCell = (PathCell) nextTurnMap[agentPos[0]][agentPos[1]];
                    currentCell.removeDiggerAgentWorking();   
                    this.currentWorkingDiggers.removeAgentById(agentID);
                }
            }
            
            //3. Movements checking  
            //List diggerlist = (ArrayList) currentAgentList.get(AgentType.DIGGER);
            //List prospectorlist = (ArrayList) currentAgentList.get(AgentType.PROSPECTOR);
            List<Cell> digglist = new ArrayList<Cell>();
            List<Cell> prosplist = new ArrayList<Cell>();
            
            
            for (int agentIndex = 0; agentIndex < this.requestedAgentsPos.size(); agentIndex++){

                int [] requestedAgentPos = new int[2];
                requestedAgentPos[0] = this.requestedAgentsPos.get(agentIndex).getPosition()[0] + this.requestedAgentsPos.get(agentIndex).getMovement()[0];
                requestedAgentPos[1] = this.requestedAgentsPos.get(agentIndex).getPosition()[1] + this.requestedAgentsPos.get(agentIndex).getMovement()[1];
                if (nextTurnMap[requestedAgentPos[0]][requestedAgentPos[1]].getCellType() == CellType.PATH){

                    currentCell = (PathCell) nextTurnMap[requestedAgentPos[0]][requestedAgentPos[1]];                
                    if (!currentCell.isThereADiggerAgentWorking()) {                    
                        // Movement allowed
                        // Obtain all necessary agent info
                        agentID = this.requestedAgentsPos.get(agentIndex).getAgentID();

                        AgentType agType = null;
                        if (agentID.getName().contains("Digger")){
                            agType = AgentType.DIGGER;
                            DiggerInfoAgent infoAg = new DiggerInfoAgent(agType ,agentID);
                            currentCell.addAgent(infoAg);
                            digglist.add((Cell)currentCell);
                            int [] currentAgentPos = this.requestedAgentsPos.get(agentIndex).getPosition();
                            currentCell = (PathCell) nextTurnMap[currentAgentPos[0]][currentAgentPos[1]];
                            Thread.yield();
                            currentCell.removeAgent(infoAg); 
                            
                        } else if (agentID.getName().contains("Prospector")) {
                            agType = AgentType.PROSPECTOR;
                            InfoAgent infoAg2 = new InfoAgent(agType ,agentID);
                            currentCell.addAgent(infoAg2);
                            prosplist.add((Cell)currentCell);
                            int [] currentAgentPos = this.requestedAgentsPos.get(agentIndex).getPosition();
                            currentCell = (PathCell) nextTurnMap[currentAgentPos[0]][currentAgentPos[1]];
                            Thread.yield();
                            if(currentCell.getAgents().size()==0){
                                int a = 0;
                            }
                            currentCell.removeAgent(infoAg2); 
                        }
                    }
                    else{
                        log("Not allowed movement request (requested movement to path cell where there is a working digger)");
                        if (this.requestedAgentsPos.get(agentIndex).getAgentID().getName().contains("Digger")){
                            digglist.add((Cell)currentCell);
                        }
                        else{
                            prosplist.add((Cell)currentCell);
                        }
                    }

                } else {
                    log("Not allowed movement request (requested movement to field cell)");
                }
            }
         
            Map<AgentType, List<Cell>> newlist = new HashMap<AgentType, List<Cell>>();
            newlist.put(AgentType.DIGGER, digglist);
            newlist.put(AgentType.PROSPECTOR, prosplist);
            
            this.game.setAgentList(newlist);

            //4. Set new metal fields detected to visible
            int[] metalPos = new int[2]; 
            for (MetalField mf : this.metalFieldList) {
                metalPos = mf.getPosition();
                FieldCell metalCell = (FieldCell) nextTurnMap[metalPos[0]][metalPos[1]];                
                metalCell.detectMetal();
                this.discoveredMetalField.addNewMetalField(metalCell);
                if (this.undiscoveredMetalField.getMetalField(metalCell) == -1.0){
                    int a;
                }
                this.gamePerformanceIndicators.addTurnsForDiscoveringMetal(this.undiscoveredMetalField.getMetalField(metalCell));
                this.undiscoveredMetalField.removeMetalField(metalCell);
            }

            //5. Update manufacturing centers (rewards)
            while (this.manufactureRequests.size() > 0){

                // Get the manufacturing reward
                ManufacturingCenterCell manufacturingCenterFieldCell = this.manufactureRequests.get(0).getMancell();
                // Add the new reward to the accumulated reward
                this.gamePerformanceIndicators.addBenefits(manufacturingCenterFieldCell.getPrice(), manufacturingCenterFieldCell.getMetal());

                this.manufactureRequests.remove(0);            
            } 

            //6. Substitute the old map with the new checked map
            currentMap = nextTurnMap.clone();            
            //Thread.sleep(10);
            result = true;
        
        } catch (Exception ex) {
            Logger.getLogger(SystemAgent.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

}