/*
 * $RCSfile: CellularAutomata.java,v $ $Date: 2009/02/03 18:39:35 $
 */
package crowdsimulation.actioncontroller.cellularautomata;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.actioncontroller.strategy.*;
import crowdsimulation.actioncontroller.cellularautomata.strategy.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import ec.util.*;
import java.util.*;
import math.*;
import sim.engine.*;
import sim.field.grid.*;
import sim.field.continuous.*;
import sim.portrayal.grid.*;
import sim.util.*;

/**
 * Base class to controll entities with using Cellular Automata.
 * This class contains the attributes and methods which are common to Action Models
 * which will use ray traced vision techniques to view the environment.
 * This is object which is called at each step in the simulation.
 *
 * @author $Author: ajolly $
 * @version $Revision: 1.2 $
 * $State: Exp $
 * $Date: 2009/02/03 18:39:35 $
 **/
public abstract class CellularAutomata extends ActionController {
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
    protected double cellWidth = 0.4;
    protected double cellHeight = 0.4;
    protected SparseGrid2D worldGrid = null;
    protected SparseGrid2D terrainGrid = null;
    protected int numberOfCellsAcross = 0;
    protected int numberOfCellsDown = 0;
    protected double timeLastStepped = 0;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
    public CellularAutomata() {
        super();
        setTimeStep(0.3);
        numberOfCellsAcross = (int) (CrowdSimulation.getInstance().getWorld().getWidth() / cellWidth);
        numberOfCellsDown = (int) (CrowdSimulation.getInstance().getWorld().getHeight() / cellHeight);
        worldGrid = new SparseGrid2D(numberOfCellsAcross, numberOfCellsDown);

        SparseGridPortrayal2D worldPortrayal = new SparseGridPortrayal2D();
        CrowdSimulation.getInstance().addPortrayal(worldPortrayal, "CA: World Grid");

        SparseGridPortrayal2D terrainPortrayal = new SparseGridPortrayal2D();
        CrowdSimulation.getInstance().addPortrayal(terrainPortrayal, "CA: Terrain Grid");
    }

    /**
     * Sets the attributes for the Action Controller defined in this class.
     **/
    public CellularAutomata(Map parameters) {
        this();
        setParameters(parameters);
    }

    /**
     * Creates a new instance of the ActionStrategy which is to be used with this Model.
     *
     * @return This is a new ActionStrategy which has been uninitialized.
     **/
    public abstract ActionStrategy getNewStrategy();

    /**
     * This is called to calculate what needs to be done for a step.
     * 
     * @param state This is the current state of the simulation.
     **/
    public void preStep(CrowdSimulation state) {
        if (timeLastStepped == 0 || state.getSimTime() - timeLastStepped > getTimeStep()) {
            timeLastStepped = state.getSimTime();

            if (terrainGrid == null) {
                generateTerrainGrid();
            }
            generateWorldGrid();

            calculateMovementParameters(state);
        }
    }

    /**
     * Loops through the Individuals and calculates their movement parameters.
     *
     * @param simState This is the current state of the simulation.
     **/
    @Override
    public void calculateMovementParameters(CrowdSimulation simState) {
        // loop overal all individuals        
        for (int i = 0; i < individuals.numObjs; i++) {
            Individual ind = (Individual) individuals.get(i);
            Vector2D loc = new Vector2D(simState.getWorld().getObjectLocation(ind));

            ActionStrategy strategy = ind.getActionStrategy();

            Bag individuals = simState.getWorld().getObjectsWithinDistance(loc.getDouble2D(), strategy.getInteractionRadius(), true);
            ind.setInteractingIndividuals(individuals);
            Bag obstacles = simState.getTerrain().getAllObjects();
            ind.setInteractingObstacles(obstacles);

            // Calculate movement parameters and time steps for individuals. 
            strategy.calculateMovementParameters(simState);
        }
    }

    /**
     * Populates the worldGrid with the idividuals from the continuous map used in CrowdSimulation.
     * This creates a new SparseGrid2D to be used by the CA based movement models.
     **/
    public void generateWorldGrid() {
        worldGrid = new SparseGrid2D(numberOfCellsAcross, numberOfCellsDown);
        Bag individuals = CrowdSimulation.getInstance().getWorld().allObjects;

        SparseGridPortrayal2D worldPortrayal = (SparseGridPortrayal2D) CrowdSimulation.getInstance().getPortrayals().get("CA: World Grid");
        worldPortrayal.setField(worldGrid);

        for (int i = 0; i < individuals.size(); i++) {
            Individual ind = (Individual) individuals.get(i);
            Vector2D loc = ind.getLocation();

            worldGrid.setObjectLocation(ind, (int) (loc.x / cellWidth), (int) (loc.y / cellHeight));
        }
    }

    /**
     * Populates the terrainGrid with the obstacles from the continuous map used in CrowdSimulation.
     * This creates a new SparseGrid2D to be used by the CA based movement models.
     * This is an expensive operation and duplicates the obstacles when placing them
     * in the SparseGrid2D.
     * 
     * To see if an obstacle exists in the cell we check to see if the center of the cell
     * is in the obstacle then we check the other 9 points found by quartering the cel
     * in both directions. If any point is in the obstacle then the entire cell is considered 
     * to be occupied.
     **/
    public void generateTerrainGrid() {
        Continuous2D tempTerrain = new Continuous2D(100, CrowdSimulation.getInstance().getWorld().getWidth(), CrowdSimulation.getInstance().getWorld().getHeight());
        terrainGrid = new SparseGrid2D(numberOfCellsAcross, numberOfCellsDown);
        Object[] theObstacles = CrowdSimulation.getInstance().getTerrain().allObjects.toArray();
        for (int x = 0; x < terrainGrid.getWidth(); x++) {
            for (int y = 0; y < terrainGrid.getWidth(); y++) {
                for (int i = 0; i < theObstacles.length; i++) {
                    Obstacle obs = (Obstacle) theObstacles[i];

                    if (obs.isInside(new Vector2D(x * cellWidth + cellWidth / 2, y * cellHeight + cellHeight / 2))) {
                        terrainGrid.setObjectLocation(new CircularObstacle(tempTerrain, x, y, Math.min(cellHeight, cellWidth), obs.getParameters()), x, y);
                    } else if (obs.isInside(new Vector2D(x * cellWidth + cellWidth / 4, y * cellHeight + cellHeight / 2))) {
                        terrainGrid.setObjectLocation(new CircularObstacle(tempTerrain, x, y, Math.min(cellHeight, cellWidth), obs.getParameters()), x, y);
                    } else if (obs.isInside(new Vector2D(x * cellWidth + 3 * cellWidth / 4, y * cellHeight + cellHeight / 2))) {
                        terrainGrid.setObjectLocation(new CircularObstacle(tempTerrain, x, y, Math.min(cellHeight, cellWidth), obs.getParameters()), x, y);
                    } else if (obs.isInside(new Vector2D(x * cellWidth + cellWidth / 4, y * cellHeight + cellHeight / 4))) {
                        terrainGrid.setObjectLocation(new CircularObstacle(tempTerrain, x, y, Math.min(cellHeight, cellWidth), obs.getParameters()), x, y);
                    } else if (obs.isInside(new Vector2D(x * cellWidth + cellWidth / 2, y * cellHeight + cellHeight / 4))) {
                        terrainGrid.setObjectLocation(new CircularObstacle(tempTerrain, x, y, Math.min(cellHeight, cellWidth), obs.getParameters()), x, y);
                    } else if (obs.isInside(new Vector2D(x * cellWidth + 3 * cellWidth / 4, y * cellHeight + cellHeight / 4))) {
                        terrainGrid.setObjectLocation(new CircularObstacle(tempTerrain, x, y, Math.min(cellHeight, cellWidth), obs.getParameters()), x, y);
                    } else if (obs.isInside(new Vector2D(x * cellWidth + cellWidth / 4, y * cellHeight + 3 * cellHeight / 4))) {
                        terrainGrid.setObjectLocation(new CircularObstacle(tempTerrain, x, y, Math.min(cellHeight, cellWidth), obs.getParameters()), x, y);
                    } else if (obs.isInside(new Vector2D(x * cellWidth + cellWidth / 2, y * cellHeight + 3 * cellHeight / 4))) {
                        terrainGrid.setObjectLocation(new CircularObstacle(tempTerrain, x, y, Math.min(cellHeight, cellWidth), obs.getParameters()), x, y);
                    } else if (obs.isInside(new Vector2D(x * cellWidth + 3 * cellWidth / 4, y * cellHeight + 3 * cellHeight / 4))) {
                        terrainGrid.setObjectLocation(new CircularObstacle(tempTerrain, x, y, Math.min(cellHeight, cellWidth), obs.getParameters()), x, y);
                    }
                }
            }
        }
        SparseGridPortrayal2D terrainPortrayal = (SparseGridPortrayal2D) CrowdSimulation.getInstance().getPortrayals().get("CA: Terrain Grid");
        terrainPortrayal.setField(terrainGrid);

    }

    /**
     * Sets all the parameters from a key value pairs collection.
     *
     * @param parameters The parameters for the indiviudal.
     **/
    public void setParameters(Map parameters) {
        super.setParameters(parameters);
    }
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
    public SparseGrid2D getWorldGrid() {
        return worldGrid;
    }

    public void setWorldGrid(SparseGrid2D val) {
        worldGrid = val;
    }

    public SparseGrid2D getTerrainGrid() {
        return terrainGrid;
    }

    public void setTerrainGrid(SparseGrid2D val) {
        terrainGrid = val;
    }

    public double getCellWidth() {
        return cellWidth;
    }

    public void setCellWidth(double val) {
        cellWidth = val;
    }

    public double getCellHeight() {
        return cellHeight;
    }

    public void setCellHeight(double val) {
        cellHeight = val;
    }

    public double getTimeLastStepped() {
        return timeLastStepped;
    }

    public void setTimeLastStepped(int val) {
        timeLastStepped = val;
    }

    public int getNumCellsAcross() {
        return numberOfCellsAcross;
    }

    public void setNumCellsAcross(int val) {
        numberOfCellsAcross = val;
    }

    public int getNumCellsDown() {
        return numberOfCellsDown;
    }

    public void setNumCellsDown(int val) {
        numberOfCellsDown = val;
    }
}
