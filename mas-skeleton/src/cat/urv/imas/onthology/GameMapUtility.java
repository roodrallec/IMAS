/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.onthology;

import cat.urv.imas.map.Cell;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Josep Famadas
 */
public class GameMapUtility implements Serializable{
    
    private GameSettings game;
    private Cell[][] utilitymap;

    public GameMapUtility(GameSettings game, Cell[][] utilitymap) {
        this.game = game;
        this.utilitymap = utilitymap;
    }

    public GameSettings getGame() {
        return game;
    }

    public void setGame(GameSettings game) {
        this.game = game;
    }

    public Cell[][] getUtilitymap() {
        return utilitymap;
    }

    public void setUtilitymap(Cell[][] utilitymap) {
        this.utilitymap = utilitymap;
    }

   

    
    

    
    
}
