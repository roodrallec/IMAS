/*
 * This onthology allow to register the variables of interest of the game.
 */
package cat.urv.imas.onthology;

import cat.urv.imas.map.FieldCell;
import cat.urv.imas.onthology.MetalType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author ALEIX
 */
public class GamePerformanceIndicators {
    private Double numberOfEntries = new Double(0.0);
    private Double benefits = new Double(0.0);  // amount of points received.
    private HashMap manufacturedMetal = new HashMap();  //: current amount of each metal already manufactured.
    private Double averageBenefitForUnitOfMetal = new Double(0.0); //: amount of points received per metal unit, regardless of the metal's type.
    
    private List<Double> turnsForDiscoveringMetal = new ArrayList();
    //private double averageTimeForDiscoveringMetal = 0; //: the amount of time spent from metal appearance until a prospector discovers it.
    
    private List<Double> turnsForDiggingMetal = new ArrayList();
    //private double averageTimeForDiggingMetal = 0; //: the amount of time spent from metal discoverage until the first digger gets to that point.
    
    private double discoveredMetalFields = 0;
    private double totalMetalFields = 0;
    //private double ratioOfDiscoveredMetal = 0; //: the ratio of the metal that is already discovered by prospectors from the total.
    private double collectedMetalFields = 0;
    //private double totalMetalUnits = 0;
    //private double ratioOfCollectedMetal = 0; //: the ratio of the metal that is already collected by diggers, including that in the digger and that already disposed in manufacturing centers.
      
    public GamePerformanceIndicators() {
        this.manufacturedMetal.put(MetalType.GOLD, new Double(0.0));
        this.manufacturedMetal.put(MetalType.SILVER, new Double(0.0));
    }  
    
    /**
     * Add new reward to the accumulated reward.
     */
    public void addBenefits(int newReward, MetalType metalType) {
        this.benefits = this.benefits + newReward;
        
        double balance = ((Double)manufacturedMetal.get(metalType)).doubleValue();
        this.manufacturedMetal.put(metalType, balance + 1.0);
        
        this.numberOfEntries++;
        
        this.averageBenefitForUnitOfMetal = this.benefits / this.numberOfEntries;
    }    
    /**
     * Gets the accumulated reward.
     */
    public Double getBenefits() {
        return this.benefits;
    }

    public HashMap getManufacturedMetal() {
        return manufacturedMetal;
    }

    public Double getAverageBenefitForUnitOfMetal() {
        return averageBenefitForUnitOfMetal;
    }
    
    public void addTurnsForDiscoveringMetal(double turns) {
        this.turnsForDiscoveringMetal.add(turns);
    }

    public double getAverageTimeForDiscoveringMetal() {
        double sum = 0.0;
        for (double turns: this.turnsForDiscoveringMetal) {
            sum = sum + turns;
        }
        double averageTimeForDiscoveringMetal = sum / (double) this.turnsForDiscoveringMetal.size();
        return averageTimeForDiscoveringMetal;
    }

    public void addTurnsForDiggingMetal(double turns) {
        this.turnsForDiggingMetal.add(turns);
    }
    
    public double getAverageTimeForDiggingMetal() {
        double sum = 0.0;
        for (double turns: this.turnsForDiggingMetal) {
            sum = sum + turns;
        }
        double averageTimeForDiggingMetal = sum / (double) this.turnsForDiggingMetal.size();
        return averageTimeForDiggingMetal;
    }

    public double getDiscoveredMetalFields() {
        return this.discoveredMetalFields;
    }

    public void addToDiscoveredMetalFieds(double newDiscoveredMetalFields) {
        this.discoveredMetalFields = this.discoveredMetalFields + newDiscoveredMetalFields;
    }

    public double getRatioOfDiscoveredMetal() {
        double ratioOfDiscoveredMetal = this.discoveredMetalFields / this.totalMetalFields;
        return ratioOfDiscoveredMetal;
    }
    
    public double getCollectedMetal() {
        return this.collectedMetalFields;
    }

    public void addCollectedMetalFields(double newCollectedMetalFields) {
        this.collectedMetalFields = this.collectedMetalFields + newCollectedMetalFields;
    }

    public double getRatioOfCollectedMetal() {
        double ratioOfCollectedMetal = this.collectedMetalFields / this.totalMetalFields;
        return ratioOfCollectedMetal;
    }

    public double getTotalMetalFieldsHist() {
        return totalMetalFields;
    }

    public void setTotalMetalFields(double newMetalFields) {
        this.totalMetalFields = newMetalFields;
    }

    public void addMetalFieldsToHist(double newMetalFields) {
        this.totalMetalFields = this.totalMetalFields + newMetalFields;
    }
}