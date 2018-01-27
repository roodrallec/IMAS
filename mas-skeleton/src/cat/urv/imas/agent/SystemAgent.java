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

import cat.urv.imas.onthology.InitialGameSettings;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.GamePerformanceIndicators;
import cat.urv.imas.gui.GraphicInterface;
import cat.urv.imas.behaviour.system.RequestResponseBehaviour;
//import cat.urv.imas.map.Cell;
import cat.urv.imas.map.*;
//import jade.Boot;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
//import jade.wrapper.AgentController;
//import jade.wrapper.StaleProxyException;
//import java.util.List;
//import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;

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
     * The Coordinator agent with which interacts sharing game settings every
     * round.
     */
    private AID coordinatorAgent;
    /**
     * agentsPos will contain the current positions of all mobile agents.
     */
    private AgentsPositions agentsPos = new AgentsPositions();
    private AgentsPositions getAgentsPositions() {
        return agentsPos;
    }
    private void setAgentsPositions(AgentsPositions agentsPositions) {
        this.agentsPos = agentsPositions;
    }
    /**
     * requestedDiggersToWork will contain AID of diggers with the x,y location of the metal field that they want to dig.
     */
    private AgentsIdAssociatedWithFC requestedDiggersToWork = new AgentsIdAssociatedWithFC();
    /**
     * requestedDiggersToWork will contain AID of diggers with the x,y location of the manufacturing center where they would like to manufacture metal.
     */
    private AgentsIdAssociatedWithFC requestedDiggersToManufacture = new AgentsIdAssociatedWithFC();
    /**
     * diggersFinishDigging will contain AID of diggers that have finished digging in a metal field. It will be used to free the path cell.
     */
    private AgentsIdAssociatedWithFC diggersFinishDigging = new AgentsIdAssociatedWithFC();

    
    /**
     * requestedAgentsPos will contain the requested positions of next turn for
     * all mobile agents.
     */
    private AgentsPositions requestedAgentsPos = new AgentsPositions();

    public AgentsPositions getRequestedAgentsPos() {
        return requestedAgentsPos;
    }
    public void setRequestedAgentsPos(AgentsPositions requestedAgentsPos) {
        this.requestedAgentsPos = requestedAgentsPos;
    }       
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
     * Path cell info.
     */
    private GamePerformanceIndicators gamePerformanceIndicators;
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
    public void addElementsForThisSimulationStep() {
        this.game.addElementsForThisSimulationStep();
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
        log("Initial configuration settings loaded");

        // 3. Load GUI
        try {
            this.gui = new GraphicInterface(game);
            gui.setVisible(true);
            log("GUI loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        ---------------
        MY CODE - START
        ---------------
         */ 
        
        // 4. Load all agents defined in game.settings
        int numDiggers = this.game.getAgentList().get(AgentType.DIGGER).size();
        int numProspectors = this.game.getAgentList().get(AgentType.PROSPECTOR).size();
        
        jade.wrapper.AgentContainer container = this.getContainerController();
        
        UtilsAgents.createAgent(container,"DiggerCoordinatorAgent","cat.urv.imas.agent.DiggerCoordinatorAgent" , null);
        UtilsAgents.createAgent(container,"ProspectorCoordinatorAgent","cat.urv.imas.agent.ProspectorCoordinatorAgent" , null);
        
        ServiceDescription searchCriterion = new ServiceDescription(); 
        
        int[][] initialMap = this.game.getInitialMap();
        int diggersCount = 1;
        int prospectorsCount = 1;
        int[] agentPos = new int[2];
        AID agentID = null;
        
        for (int i = 0; i < initialMap.length; i++){       
            for (int j = 0; j < initialMap.length; j++){                              
                if (initialMap[i][j] == -1){
                    agentPos = new int[] {i,j};
                    UtilsAgents.createAgent(container,"DiggerAgent"+diggersCount,"cat.urv.imas.agent.DiggerAgent" , null);
                    //Search the new created agent AID
                    searchCriterion.setName("DiggerAgent"+i);
                    agentID = UtilsAgents.searchAgent(this, searchCriterion);
                    //Add the new agent to agents positions list
                    this.agentsPos.setNewAgent(agentPos, agentID);
                    diggersCount++;
                    
                } else if (initialMap[i][j] == -2) {
                    agentPos = new int[] {i,j};
                    UtilsAgents.createAgent(container,"ProspectorAgent"+prospectorsCount,"cat.urv.imas.agent.ProspectorAgent" , null);
                    //Search the new created agent AID
                    searchCriterion.setName("ProspectorAgent"+i);
                    agentID = UtilsAgents.searchAgent(this, searchCriterion);  
                    //Add the new agent to agents positions list
                    this.agentsPos.setNewAgent(agentPos, agentID);
                    prospectorsCount++;
                }
            }
        }

        /*
        ---------------
        MY CODE - END
        ---------------
         */
        

        // search CoordinatorAgent
        //ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID

        // add behaviours
        // we wait for the initialization of the game
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        this.addBehaviour(new RequestResponseBehaviour(this, mt));

        // Setup finished. When the last inform is received, the agent itself will add
        // a behaviour to send/receive actions
    }

    public void updateGUI() {
        this.gui.updateGame();
    }
    
    public void checkTurnChanges() throws Exception {
        // Current turn map
        Cell[][] currentMap = this.game.getMap();
        // Map where allowed changes will be reflected, at the end of this function, it will be the next turn map to pass to Coordinator Agent
        Cell[][] nextTurnMap = currentMap.clone();
        
        //ServiceDescription searchCriterion = new ServiceDescription();
        
        //1. Set up diggers working
        while (this.requestedDiggersToWork.getNumberOfAgentsInList() > 0){
            // get agent name from it's AID
            AID diggerID = this.requestedDiggersToWork.getAgentIDByIndex(0);
            int[] metalFieldPos = this.requestedDiggersToWork.getFieldPosByIndex(0);
            int[] diggerPos = this.agentsPos.getAgentPosByIndex(0);
            
            // Remove 1 metal unit from metal field
            FieldCell metalFieldCell = (FieldCell) nextTurnMap[metalFieldPos[0]][metalFieldPos[1]];
            metalFieldCell.removeMetal();
            
            // Set digger agent working in the path cell
            PathCell diggerCell = (PathCell) nextTurnMap[diggerPos[0]][diggerPos[1]];
            diggerCell.setDiggerAgentWorking();
            
            this.requestedDiggersToWork.removeAgentByIndex(0);
        }
        
        //2. Movements checking        
        for (int agentIndex = 0; agentIndex < this.requestedAgentsPos.getNumberOfAgentsInList(); agentIndex++){
            
            int [] requestedAgentPos = this.requestedAgentsPos.getAgentPosByIndex(agentIndex);
            if (nextTurnMap[requestedAgentPos[0]][requestedAgentPos[1]].getCellType() == CellType.PATH){
                
                PathCell currentCell = (PathCell) nextTurnMap[requestedAgentPos[0]][requestedAgentPos[1]];                
                if (!currentCell.isThereADiggerAgentWorking()) {                    
                    // Movement allowed
                    // Obtain all necessary agent info
                    AID agentID = this.requestedAgentsPos.getAgentAIDByIndex(agentIndex);
                    
                    AgentType agType = null;
                    if (agentID.getName().contains("Digger")){
                        agType = AgentType.DIGGER;
                    } else if (agentID.getName().contains("Prospector")) {
                        agType = AgentType.PROSPECTOR;
                    }
                            
                    InfoAgent infoAg = new InfoAgent(agType ,agentID);
                    // Add agent to it's new cell
                    currentCell.addAgent(infoAg);
                    // Remove agent from it's old cell
                    int [] currentAgentPos = this.agentsPos.getAgentPosByIndex(agentIndex);
                    currentCell = (PathCell) nextTurnMap[currentAgentPos[0]][currentAgentPos[1]];
                    currentCell.removeAgent(infoAg);                    
                }
                
            }
        }
        
        //3. Free cells where digger have finished working
        while (this.diggersFinishDigging.getNumberOfAgentsInList() > 0){
            // get agent name from it's AID
            AID diggerID = this.requestedDiggersToWork.getAgentIDByIndex(0);
            int[] metalFieldPos = this.requestedDiggersToWork.getFieldPosByIndex(0);
            int[] diggerPos = this.agentsPos.getAgentPosByIndex(0);
            
            // Remove 1 metal unit from metal field
            FieldCell metalFieldCell = (FieldCell) nextTurnMap[metalFieldPos[0]][metalFieldPos[1]];
            metalFieldCell.removeMetal();
            
            // Set digger agent working in the path cell
            PathCell diggerCell = (PathCell) nextTurnMap[diggerPos[0]][diggerPos[1]];
            diggerCell.setDiggerAgentWorking();
            
            this.requestedDiggersToWork.removeAgentByIndex(0);
        }
        
        //4. Set new metal fields detected to visible and update metal fields capacity
        
        //5. Update manufacturing centers (rewards)
        while (this.requestedDiggersToManufacture.getNumberOfAgentsInList() > 0){
            // get agent name from it's AID
            AID diggerID = this.requestedDiggersToManufacture.getAgentIDByIndex(0);
            int[] manufacturingCenterPos = this.requestedDiggersToManufacture.getFieldPosByIndex(0);
            
            // Get the manufacturing reward
            ManufacturingCenterCell manufacturingCenterFieldCell = (ManufacturingCenterCell) nextTurnMap[manufacturingCenterPos[0]][manufacturingCenterPos[1]];
            // Add the new reward to the accumulated reward
            gamePerformanceIndicators.addNewReward(manufacturingCenterFieldCell.getPrice());
            
            this.requestedDiggersToManufacture.removeAgentByIndex(0);
        }
        
        
    }

}
