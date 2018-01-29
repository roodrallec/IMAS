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
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.ManufacturingCenterCell;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.onthology.*;
import jade.core.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.*;
import java.util.ArrayList;
import static java.lang.Math.abs;
import java.util.Arrays;
import java.util.List;


public class DiggerAgent extends ImasAgent {

    /*      ATTRIBUTES      */
    private AID diggerCoordinatorAgent;
    
    private GameSettings game;
    
    private int[] currentPosition; //This has to be initializaed (TODO Aleix)
    
    private boolean waitingMapFlag = true;
    
    private MetalFieldList currentMFL;
    
    private MetalField currentMF;
    
    private double [] parameters; //{gamma,beta,mu,psi}
    
    private int usedSlots;
    
    private String metaltype;
    
    private Boolean crash;
    
    private int[] previousMovement;
    
    
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
    
    public String getMetaltype() {
        return metaltype;
    }

    public void setMetaltype(String metaltype) {
        this.metaltype = metaltype;
    }

    public Boolean getCrash() {
        return crash;
    }

    public void setCrash(Boolean crash) {
        this.crash = crash;
    }

    public int[] getPreviousMovement() {
        return previousMovement;
    }

    public void setPreviousMovement(int[] previousMovement) {
        this.previousMovement = previousMovement;
    }
       
    
    // Method to compute bids
    public double[] computeBids(MetalFieldList metalFields){
        
        double[] bids = new double[metalFields.getMetalFields().size()+1];
        List mfl = this.getCurrentMFL().getMetalFields();
        double carryingbid = -this.getParameters()[2]*1.0*this.usedSlots;
        int EmptySlots = this.game.getDiggersCapacity()-this.usedSlots;
        if (EmptySlots == 0){ // If digger is full --> Go to manufacturing center.
            Arrays.fill(bids,-1.0);
            bids[bids.length-1] = this.game.getDiggersCapacity()-this.getUsedSlots();
            return bids;
        }
        //TODO: itera cada metalfield i per cada un computa la bid
        for (int i = 0; i < bids.length-1; i++ ){
            MetalField mf = (MetalField) mfl.get(i);
            if (!mf.getType().equals(this.metaltype) && this.metaltype!= "N"){
             bids[i] = -1.0;
            }
            else{
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
                if(distbid == 0){
                    bids[i] = 10000;
                }
                else{
                    bids[i] = 1.0/distbid + unitbid + carryingbid; //EXEMPLE, S'HA DE FER   
            }
            
            }
            
        }
        bids[bids.length-1] = this.game.getDiggersCapacity()-this.getUsedSlots();
        
        return bids;       
    }
    
    
    public int[] computeMovement(int[] distance){
        GameSettings game = this.getGame();
        int[] pos = this.getCurrentPosition();
        int[] movement = new int[]{0,0};
        int flag = 0;
        int [] previousmovement = (int[]) this.getPreviousMovement();
        if (this.crash){ //Check whether we have to take suboptimal routing
            
            if(distance[0] < 0 && previousmovement[0]!= 1){ //Check whether you can reduce the distance without going in the direction you last crashed.
                if(game.getMap()[pos[0]-1][pos[1]].getCellType() == CellType.PATH){
                    movement = new int[]{-1,0};
                    this.log("Moving up.");
                    flag = 1;
                    this.setCrash(false);
                }    
            }
            if(distance[0] > 0 && flag == 0 && previousmovement[0]!= -1){ 
                if(game.getMap()[pos[0]+1][pos[1]].getCellType() == CellType.PATH){
                    movement = new int[]{1,0};
                    this.log("Moving down.");
                    flag = 1;
                    this.setCrash(false);
                }  
            }
        
            if(distance[1] < 0 && flag == 0 && previousmovement[1]!= 1){ 
                if(game.getMap()[pos[0]][pos[1]-1].getCellType() == CellType.PATH){
                    movement = new int[]{0,-1};
                    this.log("Moving left.");
                    flag = 1;
                    this.setCrash(false);
                }    
            }
            if(distance[1] > 0 && flag == 0 && previousmovement[1]!= -1){ 
                if(game.getMap()[pos[0]][pos[1]+1].getCellType() == CellType.PATH){
                    movement = new int[]{0,1};
                    this.log("Moving right.");
                    flag = 1;
                    this.setCrash(false);
                }  
            }
            
            if (flag == 0) { //You can't reduce the distance --> Check whether you can  keep going back.
                if(game.getMap()[pos[0]+previousmovement[0]][pos[1] + previousmovement[1]].getCellType() == CellType.PATH){
                    movement[0] = previousmovement[0];
                    movement[1] = previousmovement[1];
                    this.log("Moving Backwards");
                }
                else if (abs(previousmovement[0])== 0){ //Change direction perpendicularly if you can't keep moving backwards.
                    if(game.getMap()[pos[0]+1][pos[1]].getCellType() == CellType.PATH){
                        movement[0] = 1;
                        movement[1] = 0;
                        this.log("Can't Move Backwards, Moving Up");
                    }
                    else if(game.getMap()[pos[0]-1][pos[1]].getCellType() == CellType.PATH){
                        movement[0] = -1;
                        movement[1] = 0;
                        this.log("Can't Move Backwards, Moving Down");
                    }
                }
                else if (abs(previousmovement[1])== 0){
                    if(game.getMap()[pos[0]][pos[1]+1].getCellType() == CellType.PATH){
                        movement[0] = 0;
                        movement[1] = 1;
                        this.log("Can't Move Backwards, Moving Right");
                    }
                    else if(game.getMap()[pos[0]][pos[1]-1].getCellType() == CellType.PATH){
                        movement[0] = 0;
                        movement[1] = -1;
                        this.log("Can't Move Backwards, Moving Left");
                    }
                }

                
            }
        }
        else{
        
            if(distance[0] < 0){ //&& comprovar que la casella de sobre sigui un pathcell)
                if(game.getMap()[pos[0]-1][pos[1]].getCellType() == CellType.PATH){
                    movement = new int[]{-1,0};
                    this.log("Moving up.");
                    flag = 1;
                }    
            }
            if(distance[0] > 0 && flag == 0){ //&& comprovar que la casella de sota sigui un pathcell)
                if(game.getMap()[pos[0]+1][pos[1]].getCellType() == CellType.PATH){
                    movement = new int[]{1,0};
                    this.log("Moving down.");
                    flag = 1;
                }  
            }
        
            if(distance[1] < 0 && flag == 0){ //&& comprovar que la casella de la esquerra sigui un pathcell)
                if(game.getMap()[pos[0]][pos[1]-1].getCellType() == CellType.PATH){
                    movement = new int[]{0,-1};
                    this.log("Moving left.");
                    flag = 1;
                }    
            }
            if(distance[1] > 0 && flag == 0){ //&& comprovar que la casella de la dreta sigui un pathcell)
                if(game.getMap()[pos[0]][pos[1]+1].getCellType() == CellType.PATH){
                    movement = new int[]{0,1};
                    this.log("Moving right.");
                    flag = 1;
                }  
            }
            
            if (flag == 0 && (previousmovement[0] != 0 || previousmovement[1]!=0) ) { // Go back if crashed.
                this.setCrash(true);
                if(game.getMap()[pos[0]+previousmovement[0]][pos[1]+previousmovement[1]].getCellType() == CellType.PATH){
                    movement = previousmovement;
                    this.log("Keep direction.");
                }
                else{
                    movement[0] = (int) (-1.0*this.getPreviousMovement()[0]);
                    movement[1] = (int) (-1.0*this.getPreviousMovement()[1]);
                    this.log("Moving Backwards");  
                }

            }
            else if (flag == 0 && previousmovement[0] == 0 && previousmovement[1]==0 ) { //Any movement.
                this.setCrash(true);
                if(game.getMap()[pos[0]][pos[1]-1].getCellType() == CellType.PATH){
                    movement = new int[]{0,-1};
                    this.log("Moving left.");
                }  
                else if(game.getMap()[pos[0]][pos[1]+1].getCellType() == CellType.PATH){
                    movement = new int[]{0,1};
                    this.log("Moving right.");
                }  
                else if(game.getMap()[pos[0]+1][pos[1]].getCellType() == CellType.PATH){
                    movement = new int[]{1,0};
                    this.log("Moving down.");
                }  
                else if(game.getMap()[pos[0]-1][pos[1]].getCellType() == CellType.PATH){
                    movement = new int[]{-1,0};
                    this.log("Moving up.");
                }    
            }
        }
        // Do it after confirmation from SA received -->this.setPreviousMovement(movement); 
        return movement;
    }
    
    public ManufacturingCenterCell chooseManufacturingCenter(){
        double eval = 0;
        ManufacturingCenterCell mcf = (ManufacturingCenterCell) this.game.getCellsOfType().get(CellType.MANUFACTURING_CENTER).get(0);
        
        List mancells = this.game.getCellsOfType().get(CellType.MANUFACTURING_CENTER);
        for (int i = 0; i < mancells.size(); i++){
            ManufacturingCenterCell mancell = (ManufacturingCenterCell) mancells.get(i);
            if(mancell.getMetal().getShortString() == this.metaltype){
                double distbid = 1.0*abs(this.currentPosition[0]-mancell.getRow()) + 1.0*abs(this.currentPosition[1]-mancell.getCol());
                if (distbid == 0){
                    
                    return mancell;  
                }
                double bid = 1.0/distbid + this.parameters[3]*mancell.getPrice();
                if(bid > eval){
                    eval = bid;
                    mcf = mancell;
                }   
            }
        }
        return mcf;
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
        this.setCrash(true);
        // PROVES! //
        this.currentPosition = new int[] {5,2};
        this.parameters = new double [] {0.5,0.5,0.5,0.1};
        this.usedSlots = 0;
        this.previousMovement = new int[]{0,0};
                
        
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
        
        // It triggers when the received message is a GET_MAP.
        MessageTemplate mt2 =MessageTemplate.MatchLanguage(MessageContent.GET_MAP);
        this.addBehaviour(new MapHandlingDA(this, mt2));
        
        // It triggers when the received message is a CHOOSE_ACTION.
        MessageTemplate mt3 =MessageTemplate.MatchLanguage(MessageContent.CHOOSE_ACTION);
        this.addBehaviour(new ChooseActionDA(this, mt3));
    }
    
    

}
