/**
 * IMAS base code for the practical work. 
 * Copyright (C) 2014 DEIM - URV
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.gui;

import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.GamePerformanceIndicators;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MetalType;
import java.awt.*;
import javax.swing.*;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class of the graphical interface controlled by the System Agent. It
 * offers several methods to show the changes of the agents within the city, as
 * well as an area with the logs and an area with the main statistical results.
 */
public class GraphicInterface extends JFrame {

    /**
     * Internal gap from within the map.
     */
    private static final int INSET = 50;

    /**
     * Helps painting the game into a panel from the GUI.
     */
    private MapVisualizer jMapPanel;
    /**
     * Helps registering logs in the GUI.
     */
    private LogPanel jLogPanel;
    /**
     * Helps showing statistics in the GUI.
     */
    private StatisticsPanel jStatisticsPanel;

    /**
     * Tabbed pane to gather all GUI tabs.
     */
    private JTabbedPane jGameTabbedPane;
    /**
     * Panel to include the game GUI.
     */
    private JPanel jGamePanel = new JPanel();
    /**
     * DecimalFormat round to 2 decimals.
     */
    private DecimalFormat round2Decimals = new DecimalFormat("##.00");

    /**
     * Initializes GUI elements, including the game.
     *
     * @param game game settings to show.
     */
    public GraphicInterface(GameSettings game) {
        jbInit(game.getTitle());
        showGameMap(game.getMap());
    }

    /**
     * Initializes the GUI elements.
     *
     * @param title title to show in the GUI frame.
     * @throws Exception
     */
    private void jbInit(String title) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            //just in case
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(INSET, INSET, screenSize.width - INSET * 2, screenSize.height - INSET * 2);

        //Quit this app when the big window closes.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //System.exit(0);
                dispose();
            }
        });

        this.setForeground(Color.WHITE);
        this.setTitle(title);

        // initialize tabbed pane
        jGameTabbedPane = new JTabbedPane();
        jGameTabbedPane.setTabPlacement(JTabbedPane.TOP);
        jGameTabbedPane.setBackground(UtilsGUI.frameBgColor);
        jGameTabbedPane.setForeground(UtilsGUI.textFgColor);
        jGameTabbedPane.setFont(new java.awt.Font("Arial", Font.PLAIN, 10));
        jGameTabbedPane.setBorder(BorderFactory.createEtchedBorder());
        jGameTabbedPane.setMinimumSize(new Dimension(640, 480));
        jGameTabbedPane.setPreferredSize(new Dimension(640, 480));

        // game panel
        jGamePanel = new JPanel(new GridLayout(1, 1));
        jGamePanel.setBackground(UtilsGUI.frameBgColor);
        jGamePanel.setForeground(UtilsGUI.textFgColor);
        jGamePanel.setBorder(BorderFactory.createEtchedBorder());
        jGamePanel.setMinimumSize(new Dimension(640, 480));
        jGamePanel.setPreferredSize(new Dimension(640, 480));

        ImageIcon icon = new ImageIcon(UtilsGUI.gameIconPath);
        jGameTabbedPane.addTab("Partida", icon, jGamePanel);

        // Logs
        this.jLogPanel = new LogPanel();
        icon = new ImageIcon(UtilsGUI.logsIconPath);
        jGameTabbedPane.addTab("Logs", icon, this.jLogPanel);

        this.jLogPanel.log("Initializing components ....\n");

        // Statistics 
        this.jStatisticsPanel = new StatisticsPanel();
        jGameTabbedPane.addTab("Statistics", icon, this.jStatisticsPanel);

        this.jStatisticsPanel.showMessage("All tabs initialized successfully!");

        // Tabbed panel
        this.getContentPane().add(jGameTabbedPane);

    }

    /**
     * Repaint the whole game map.
     *
     * @param map game map.
     */
    public void showGameMap(Cell[][] map) {
        this.jMapPanel = new MapVisualizer(map);
        this.jGamePanel.add(jMapPanel);
        this.jGamePanel.repaint();
    }

    /**
     * Mostra una cadena en el panell destinat a logs
     *
     * @param msg String per mostrar
     */
    public void log(String msg) {
        this.jLogPanel.log(msg);
    }

    /**
     * Mostra una cadena en el panell destinat a stad�stiques
     *
     * @param msg String per mostrar
     */
    public void showStatistics(GamePerformanceIndicators gamePerformanceIndicators, int currentTurn, int totalTurns) {
        this.jStatisticsPanel.removeAllMessages();
        
        ArrayList<String> statisticsInfoStrings = new ArrayList<String>();
        statisticsInfoStrings.add("REMAINING TURNS: " + currentTurn + " OF " + totalTurns);
        statisticsInfoStrings.add("Current benefits:                   ");
        statisticsInfoStrings.add("Manufactured gold:                  " + this.round2Decimals.format(gamePerformanceIndicators.getManufacturedMetal().get(MetalType.GOLD)) + " units");
        statisticsInfoStrings.add("Manufactured silver:                " + this.round2Decimals.format(gamePerformanceIndicators.getManufacturedMetal().get(MetalType.SILVER)) + " units");
        statisticsInfoStrings.add("Average benefit for unit of metal:  ");
        statisticsInfoStrings.add("Average time for discovering metal: " + this.round2Decimals.format(gamePerformanceIndicators.getAverageTimeForDiscoveringMetal()) + " turns");
        statisticsInfoStrings.add("Average time for digging metal:     " + this.round2Decimals.format(gamePerformanceIndicators.getAverageTimeForDiggingMetal()) + " turns");
        statisticsInfoStrings.add("Ratio of discovered metal:          ");
        statisticsInfoStrings.add("Ratio of collected metal:           ");
        
        ArrayList<Double> statisticsInfoDoubles = new ArrayList<Double>();
        statisticsInfoDoubles.add(null);
        statisticsInfoDoubles.add(gamePerformanceIndicators.getBenefits());
        statisticsInfoDoubles.add(null);
        statisticsInfoDoubles.add(null);
        statisticsInfoDoubles.add(gamePerformanceIndicators.getAverageBenefitForUnitOfMetal());
        statisticsInfoDoubles.add(null);
        statisticsInfoDoubles.add(null);
        statisticsInfoDoubles.add(gamePerformanceIndicators.getRatioOfDiscoveredMetal());
        statisticsInfoDoubles.add(gamePerformanceIndicators.getRatioOfCollectedMetal());
        
        for (int counter = 0; counter < statisticsInfoStrings.size(); counter++) {
            //if (statisticsInfo.get(counter).toString() != "NaN"){
                try{
                    this.jStatisticsPanel.showMessage(statisticsInfoStrings.get(counter).toString() + this.round2Decimals.format(statisticsInfoDoubles.get(counter)));
                } catch (Exception ex) {
                    this.jStatisticsPanel.showMessage(statisticsInfoStrings.get(counter).toString());
                }
            //}
        }    
        
        this.jStatisticsPanel.addBlankLine();
        this.jStatisticsPanel.setAutoscrolls(true);
    }

    /**
     * Update the game GUI.
     */
    public void updateGame() {
        this.jGamePanel.repaint();
    }
    
}
