/*
 * This onthology allow to register the variables of interest of the game.
 */
package cat.urv.imas.onthology;

import cat.urv.imas.map.FieldCell;
import cat.urv.imas.onthology.MetalType;
import java.util.HashMap;

/**
 *
 * @author ALEIX
 */
public class GamePerformanceIndicators {
    private Double numberOfEntries = new Double(0.0);
    private Double benefits = new Double(0.0);  // amount of points received.
    private HashMap manufacturedMetal = new HashMap();  //: current amount of each metal already manufactured.
    private Double averageBenefitForUnitOfMetal = new Double(0.0); //: amount of points received per metal unit, regardless of the metal's type.
    private long averageTimeForDiscoveringMetal = 0; //: the amount of time spent from metal appearance until a prospector discovers it.
    private long averageTimeForDiggingMetal = 0; //: the amount of time spent from metal discoverage until the first digger gets to that point.
    private int ratioOfDiscoveredMetal = 0; //: the ratio of the metal that is already discovered by prospectors from the total.
    private int ratioOfCollectedMetal = 0; //: the ratio of the metal that is already collected by diggers, including that in the digger and that already disposed in manufacturing centers.

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

    public long getAverageTimeForDiscoveringMetal() {
        return averageTimeForDiscoveringMetal;
    }

    public void setAverageTimeForDiscoveringMetal(long averageTimeForDiscoveringMetal) {
        this.averageTimeForDiscoveringMetal = averageTimeForDiscoveringMetal;
    }

    public long getAverageTimeForDiggingMetal() {
        return averageTimeForDiggingMetal;
    }

    public void setAverageTimeForDiggingMetal(long averageTimeForDiggingMetal) {
        this.averageTimeForDiggingMetal = averageTimeForDiggingMetal;
    }

    public int getRatioOfDiscoveredMetal() {
        return ratioOfDiscoveredMetal;
    }

    public void setRatioOfDiscoveredMetal(int ratioOfDiscoveredMetal) {
        this.ratioOfDiscoveredMetal = ratioOfDiscoveredMetal;
    }

    public int getRatioOfCollectedMetal() {
        return ratioOfCollectedMetal;
    }

    public void setRatioOfCollectedMetal(int ratioOfCollectedMetal) {
        this.ratioOfCollectedMetal = ratioOfCollectedMetal;
    }


    

}
