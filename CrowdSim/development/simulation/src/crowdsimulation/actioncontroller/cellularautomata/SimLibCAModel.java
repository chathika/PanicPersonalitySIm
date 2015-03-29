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
import java.awt.*;
import java.io.*;

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
 * @version $Revision: 1.3 $
 * $State: Exp $
 * $Date: 2009/03/26 21:25:01 $
 *
 *
 **/
public class SimLibCAModel extends CellularAutomata {
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
    public boolean performMove = false;
    public HashMap staticMaps = new HashMap();
    public IntGrid2D moveMovementTracker;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////	
    public SimLibCAModel() {

        super();
        moveMovementTracker = new IntGrid2D(this.getNumCellsAcross(), this.getNumCellsDown(), 0);
    }

    /**
     * Sets the attributes for the Action Controller defined in this class.
     **/
    public SimLibCAModel(Map parameters) {
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
        return new SimLibCAStrategy();
    }

    /**
     * This is called to calculate what needs to be done for a step.
     * 
     * @param state This is the current state of the simulation.
     **/    //called by super ActionController class - controlled by simulation 
    @Override
    public void preStep(CrowdSimulation state) {
        //Log.log(1,"PreStep The Sim Time : " + state.getSimTime());
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

                        Waypoint wp = (Waypoint) path.waypoints.get(j);
                        if (!staticMaps.containsKey(wp)) {
                            System.out.println("Building Map for Path " + setArray[i] + " waypoint " + j);
                            Int2D exitLoc = new Int2D((int) (wp.getCenter().x / cellWidth), (int) (wp.getCenter().y / cellHeight));
                            System.out.println("ExitLoc " + exitLoc.x + " , " + exitLoc.y);
                            DijkstraMapSimLib aMap = new DijkstraMapSimLib(terrainGrid);
                            aMap.DijkstraAlg(exitLoc);
                            aMap.write2cvs("../logs/DijkstraMap" + i + "_" + j + ".csv");
                            staticMaps.put(wp, aMap);
                        }
                    }

                }
                System.out.println("Finished Building Maps");
            }
            /**
             * Populates the worldGrid with the idividuals from the continuous map used in CrowdSimulation.
             * This creates a new SparseGrid2D to be used by the CA based movement models.
             *
             * Called from extended CellularAutomata class
             **/
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
    public void step(SimState state) {


        // loop over all individuals 
        //Log.log(1,"Cellular Automata Step!");      
        for (int i = 0; i < individuals.numObjs; i++) {

            Individual ind = (Individual) individuals.get(i);
            if (!ind.isDead()) {
                ((CellularAutomataStrategy) ind.getActionStrategy()).move();
            }
        }
        // Need to regenerate the world grid so that the display is done as where everyone is
        // instead of the CA grid showing only where they were at the last sample.
        generateWorldGrid();

        CrowdSimulation crowdState = CrowdSimulation.getInstance();
        double current_time = crowdState.getSimTime();


        if ((crowdState.getDuration() - current_time) < 0.31) {
            PrintWriter writer1;
            PrintWriter writer;
            try {
                writer1 = new PrintWriter(new FileOutputStream("../logs/TrackingInfoObj.csv"));
                writer = new PrintWriter(new FileOutputStream("../logs/TrackingInfoAlan.csv"));
                for (int y = 0; y < moveMovementTracker.getHeight(); y++) {
                    StringBuffer data1 = new StringBuffer();
                    StringBuffer data = new StringBuffer();
                    for (int x = 0; x < moveMovementTracker.getWidth(); x++) {
                        data1.append(((SparseGrid2D) this.getTerrainGrid()).numObjectsAtLocation(x, y) + ",");
                        data.append(moveMovementTracker.get(x, y) + ",");
                    }
                    writer1.println(data1.toString());
                    writer1.flush();
                    writer.println(data.toString());
                    writer.flush();

                }
            } catch (FileNotFoundException fnfe) {
                File file1 = new File("../logs/TrackingInfoObj.csv");
                File file = new File("../logs/TrackingInfoAlan.csv");
                try {
                    writer1 = new PrintWriter(new FileOutputStream("../logs/TrackingInfoObj.csv"));
                    writer = new PrintWriter(new FileOutputStream("../logs/TrackingInfoAlan.csv"));
                    for (int y = 0; y < moveMovementTracker.getHeight(); y++) {
                        StringBuffer data1 = new StringBuffer();
                        StringBuffer data = new StringBuffer();
                        for (int x = 0; x < moveMovementTracker.getWidth(); x++) {
                            data1.append(((SparseGrid2D) this.getTerrainGrid()).numObjectsAtLocation(x, y) + ",");
                            data.append(moveMovementTracker.get(x, y) + " ,");
                        }
                        writer1.println(data1.toString());
                        writer1.flush();
                        writer.println(data.toString());
                        writer.flush();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    /**
     * Sets all the parameters from a key value pairs collection.
     *
     * @param parameters The parameters for the indiviudal.
     **/
    @Override
    public void setParameters(Map parameters) {
        super.setParameters(parameters);
    }
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////	
}
