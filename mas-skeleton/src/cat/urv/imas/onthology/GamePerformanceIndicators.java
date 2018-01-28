/*
 * This onthology allow to register the variables of interest of the game.
 */
package cat.urv.imas.onthology;

/**
 *
 * @author ALEIX
 */
public class GamePerformanceIndicators {
    public int accumulatedReward = 0;
    public int currentStep = 0;
    
    /**
     * Add new reward to the accumulated reward.
     */
    public void addNewReward(int newReward) {
        this.accumulatedReward = this.accumulatedReward + newReward;
    }
    
    /**
     * Gets the accumulated reward.
     */
    public int getAccumulatedReward() {
        return this.accumulatedReward;
    }
    
    /**
     * Add new reward to the accumulated reward.
     */
    public void incrementGameStep() {
        this.currentStep++;
    }

    /**
     * Gets the current step.
     */
    public int getCurrentStep() {
        return this.getCurrentStep();
    }      
}
