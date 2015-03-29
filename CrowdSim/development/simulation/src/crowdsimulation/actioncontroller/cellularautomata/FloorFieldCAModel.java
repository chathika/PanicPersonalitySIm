package crowdsimulation.actioncontroller.cellularautomata;

import crowdsimulation.*;
import crowdsimulation.util.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.actioncontroller.cellularautomata.strategy.*;
import crowdsimulation.actioncontroller.strategy.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import crowdsimulation.maps.*;
import ec.util.*;
import java.util.*;
import math.*;
import sim.engine.*;
import sim.field.grid.*;
import sim.util.*;

/**
 *
 * @author Alan Jolly - adaptation of Dr. Paul Wiegand'a code
 * This model is based on work of:
 * 
 * A. Schadschneider. "Cellular automaton approach to pedestrian dynamics
 * - theory". In M. Schreckenberg and S.D. Sharma, editors, Pedestrian and
 * Evacuation Dynamics, Berlin, 2002. Springer.
 *
 * and
 *
 * C. Burstedde, K. Klauck, A. Schadschneider, and J. Zittartz. "Simulation
 * of pedestrian dynamics using a 2-dimensional cellular automaton", 
 * In M. Schreckenberg and S.D. Sharma, editors, Pedestrian and
 * Evacuation Dynamics, Berlin, 2002. Springer.
 *
 * @version $Revision: 1.4 $
 * $State: Exp $
 * $Date: 2009/03/26 00:47:25 $
 *
 *
 **/
public class FloorFieldCAModel extends CellularAutomata {
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
    /** The constant used for evaporationRate for heat diffusion equation, bosons decay with this probablility**/
    public double evaporationRate = 0.005;
    /** The constant used for diffusionRate for heat diffusion equation, bosons diffuse at this rate**/
    public double diffusionRate = 0.9;
    /** Choose the movement model p_ij Matrix is probablistic =1 and p_ij Matrix is deterministic = 0 **/
    private int useStochasticMoveSelection = 0;
    /** Value each individual leaves as trace - higher values make route more attractive generally set to one**/
    public double bosonTraceValue = 1;
    /**model strategy**/
    public int modelStrategy = 1; //1 = discrete floor field and 0 = continuous floor field;
    /**boson grid**/
    public DiffuserDoubleGrid2D globalBosonGrid;
    /**copy of boson grid where calculations are done**/
    private DoubleGrid2D globalBosonGridCalc;
    public boolean performMove = false;
    public boolean conflictsResolved;
    public HashMap staticMaps = new HashMap();
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////	
    public FloorFieldCAModel() {
        super();
        //construct the grids to match world
        if (modelStrategy == 0 || modelStrategy == 1) {
            globalBosonGrid = new DiffuserDoubleGrid2D(this.numberOfCellsAcross, this.numberOfCellsDown, 0);
            
        } else {
            System.out.println("Model Strategy Not Known");
        }

    }

    /**
     * Sets the attributes for the Action Controller defined in this class.
     **/
    public FloorFieldCAModel(Map parameters) {
        this();
        //gets parameters defined in *.xml under model type.
        setParameters(parameters);
    }

    /**
     * Creates a new instance of the ActionStrategy which is to be used with this Model.
     *
     * @return This is a new ActionStrategy which has been uninitialized.
     **/
    public ActionStrategy getNewStrategy() {
        return new FloorFieldCAStrategy();
    }

    /**
     * This is called to calculate what needs to be done for a step.
     * 
     * @param state This is the current state of the simulation.
     **/    //called by super ActionController class - controlled by simulation 
    @Override
    public void preStep(CrowdSimulation state) {
        if (timeLastStepped == 0 || state.getSimTime() - timeLastStepped > getTimeStep()) {
            timeLastStepped = state.getSimTime();

            if (terrainGrid == null) {
                // called from extended CellularAutomata class
                generateTerrainGrid();

                Set theKeySet = CrowdSimulation.getInstance().getPaths().keySet();
                Object[] setArray = theKeySet.toArray();

                for (int i = 0; i < setArray.length; i++) {
                    Path path = (Path) CrowdSimulation.getInstance().getPaths().get(setArray[i]);
                    // create map for each waypoint
                    for (int j = 0; j < path.waypoints.size(); j++) {
                         System.out.println("Building Map for Path " + setArray[i] + " waypoint " + j);
                        Waypoint wp = (Waypoint) path.waypoints.get(j);
                        Int2D exitLoc = new Int2D((int) (wp.getCenter().x / cellWidth), (int) (wp.getCenter().y / cellHeight));
                        DijkstraMap aMap = new DijkstraMap(terrainGrid);
                        aMap.DijkstraAlg(exitLoc);
                        aMap.write2cvs("../logs/DijkstraMap" + i + "_" + j + ".csv");
                        staticMaps.put(wp, aMap);
                    }
                }
            }
            /**
             * Populates the worldGrid with the idividuals from the continuous map used in CrowdSimulation.
             * This creates a new SparseGrid2D to be used by the CA based movement models.
             *
             * Called from extended CellularAutomata class
             **/
            
            globalBosonGrid.diffuse(diffusionRate, 0.3);
            globalBosonGrid.decay(0.3,0.2);
            this.conflictsResolved = false;
            generateWorldGrid();
            //calls calculate movement parameters from CellularAutomata.java which passes 
            //the current simulation state to the calculateMovementParamters method in the actionstrategy
            // which calculates each individuals next movement
            calculateMovementParameters(state);
            performMove = true;
        } else {
            performMove = false;
        }
    }
    /**
     * Moves the individuals using the passed in time step.
     **/
    @Override
    public void move(double currentTimeStep) {
        //generateWorldGrid();
        // loop over all individuals        
        for (int i = 0; i < individuals.numObjs; i++) {
            Individual ind = (Individual) individuals.get(i);
            if (!ind.isDead()) {
                  ((FloorFieldCAStrategy) ind.getActionStrategy()).move();
            }
        }
        // Need to regenerate the world grid so that the display is done as where everyone is
        // instead of the CA grid showing only where they were at the last sample.
        generateWorldGrid();
    }

    /**
     * Sets all the parameters from a key value pairs collection.
     *
     * @param parameters The parameters for the indiviudal.
     **/
    @Override
    public void setParameters(Map parameters) {
        super.setParameters(parameters);

        if (parameters.containsKey("evaporationRate")) {
            setEvaporationRate(Parameters.convertToDouble(
                    parameters.get("evaporationRate"),
                    getEvaporationRate(),
                    "evaporationRate for FloorFieldCAModel construction must be a Double or a string representing a Double."));
        }
        if (parameters.containsKey("diffusionRate")) {
            setDiffusionRate(Parameters.convertToDouble(
                    parameters.get("diffusionRate"),
                    getDiffusionRate(),
                    "diffusionRate for FloorFieldCAModel construction must be a Double or a string representing a Double."));
        }
        if (parameters.containsKey("useStochasticMoveSelection")) {
            setUseStochasticMoveSelection(Parameters.convertToInt(
                    parameters.get("useStochasticMoveSelection"),
                    getUseStochasticMoveSelection(),
                    "useStochasticMoveSelection for FloorFieldCAModel construction must be a integer or a string representing a Integer."));
        }
        if (parameters.containsKey("modelStrategy")) {
            setModelStrategy(Parameters.convertToInt(
                    parameters.get("modelStrategy"),
                    getModelStrategy(),
                    "modelStrategy for FloorFieldCAStrategy construction must be a integer or a string representing a Double."));
        }
    }
    ////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////	
    /**
     * Sets evaporationRate for heat diffusion equation
     *
     * @param val - evaporationRate for heat diffusion equation
     **/
    public void setEvaporationRate(double val) {
        evaporationRate = val;
    }

    /**
     * Gets the evaporationRate for heat diffusion equation
     *
     * @return evaporationRate for heat diffusion equation
     **/
    public double getEvaporationRate() {
        return evaporationRate;
    }

    /**
     * Sets diffusionRate for heat diffusion equation
     *
     * @param val - diffusionRate for heat diffusion equation
     **/
    public void setDiffusionRate(double val) {
        diffusionRate = val;
    }

    /**
     * Gets the diffusionRate for heat diffusion equation
     *
     * @return diffusionRate for heat diffusion equation
     **/
    public double getDiffusionRate() {
        return diffusionRate;
    }

    /**
     * Sets useStochasticMoveSelection - Detemines whether of 
     * not the deterministic or probablistic version of
     * calculateMovementParameters is used.
     *
     * @param val - useStochasticMoveSelection int 0 or 1.
     **/
    public void setUseStochasticMoveSelection(int val) {
        useStochasticMoveSelection = val;
    }

    /**
     * Gets useStochasticMoveSelection - Detemines whether of 
     * not the deterministic or probablistic version of
     * calculateMovementParameters is used.
     *
     * @return useStochasticMoveSelection int 0 or 1.
     **/
    public int getUseStochasticMoveSelection() {
        return useStochasticMoveSelection;
    }

    /**
     * Sets bosonTraceValue - detemines attractiveness of
     * route for individual usually a global value set to 1
     *
     * @param val - the strength of boson attractiveness bosonTraceValue .
     **/
    public void setBosonTraceValue(double val) {
        bosonTraceValue = val;
    }

    /**
     * Gets bosonTraceValue - detemines attractiveness of
     * route for individual usually a global value set to 1
     *
     * @return bosonTraceValue - strength of boson attractiveness.
     **/
    public double getBosonTraceValue() {
        return bosonTraceValue;
    }

    /**
     * Gets gets the model strategy
     *
     * @return integer representing model strategy
     **/
    public int getModelStrategy() {
        return modelStrategy;
    }

    /**
     * Sets the model strategy
     *
     * @param val - returns the model strategy
     **/
    public void setModelStrategy(int val) {
        modelStrategy = val;
    }

    /**
     * Gets bosonGrid - this is the dynamic grid
     *
     * @return bosonGrid - this is the dynamic grid
     **/
    public DoubleGrid2D getbosonGrid() {
        return globalBosonGrid;
    }
}
