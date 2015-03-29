package crowdsimulation.actioncontroller.cellularautomata.strategy;

import crowdsimulation.*;
import crowdsimulation.util.*;
//import crowdsimulation.actioncontroller.*;
import crowdsimulation.actioncontroller.cellularautomata.*;
//import crowdsimulation.actioncontroller.strategy.*;
import crowdsimulation.entities.*;
//import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
//import crowdsimulation.maps.*;
//import ec.util.*;
import java.util.*;
import math.*;
//import sim.engine.*;
import sim.field.grid.*;
import sim.util.*;
import java.awt.Color;

public class SimLibCAStrategy extends CellularAutomataStrategy {
///////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
    DoubleGrid2D staticNeigh2D = new DoubleGrid2D(3, 3, 0);
    DoubleGrid2D inertiaGrid2D = new DoubleGrid2D(3, 3, 1);
    IntGrid2D obsNeighGrid = new IntGrid2D(3, 3, 1);
    DoubleGrid2D static2D;
    public double staticCoupling = 2.0; //coupling strength static floor fields
    public double inertiaConstant = 0.2; //tune the interia
    public double inverseTemp = 1; //inverse temperature discrete floor fields
    public double exitGradientScale = 1;
    public double exitGradientExp = 1.0;
    protected Int2D lastLocation = null;
    protected Int2D moveToPoint = null;
    boolean PrintOut;
    Int2D moveIndex = null;
    Int2D movePt = new Int2D();
    private boolean wayPointChange = true;
    private Int2D previousExitLocation = new Int2D();
    UniformDistributedGenerator randUniform = new UniformDistributedGenerator(0, 1);
    private boolean AttachedToStation = false;
    private boolean InQueue = false;
    private boolean IsWorking = false;
    public boolean IsFinished = false;
    //0 means no station
    private int TheStation = 0;
    public int taskNumber = -99999;
    public int jobNumber = -99999;
    private double taskStartTime = -99999;
    private double taskDurationTime = -99999;
    private double enterQueueTime = -99999;
    private double exitQueueTime = -99999;
    public double createTime = -99999;
    public double timeWalking = 0;
    public double timeInQueue = 0;
    public double timeWorking = 0;
    public double noMove = 0;
    private int wayPtCounter = 0;
    private Object oriColor;
    private boolean successfullMove = true;
    public int numCollisions = 0;
    public double distTravelled = 0;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
    public SimLibCAStrategy() {
        super();
    }

    /**
     * Sets the attributes for the Action Controller defined in this class.
     **/
    public SimLibCAStrategy(Map parameters) {
        this();
        setParameters(parameters);
    //System.out.println("In Strategy Past Set Parameters");

    }

    public void setJobType() {
        Color theColor;
        Path thePath;
        HashMap thePaths = CrowdSimulation.getInstance().getPaths();
        Bag paths = new Bag();
        double theDraw = randUniform.nextValue();
        if (theDraw <= 0.3) {
            thePath = (Path) ((Path) thePaths.get(0)).clone();
            theColor = new Color(225, 0, 0);
            jobNumber = 0;
        } else if (theDraw <= 0.8) {
            thePath = (Path) ((Path) thePaths.get(1)).clone();
            theColor = new Color(0, 225, 0);
            jobNumber = 1;
        } else {
            thePath = (Path) ((Path) thePaths.get(2)).clone();
            theColor = new Color(0, 0, 225);
            jobNumber = 2;
        }
        paths.add(thePath.clone());
        ind.setInteractingPaths(paths);
        ind.setColor(theColor);
        if (oriColor == null) {
            oriColor = ind.getColor();
        }

    }
    //called by this.calculateMovementParameters
    public DoubleGrid2D setupWaypointGrid() {
        Int2D exitLoc = getWayPointLocation();
        //Alan Jolly
        //TODO: Why do I have to do this?
        if (previousExitLocation.x != exitLoc.x || previousExitLocation.y != exitLoc.y) {
            // System.out.println("WayPoint: " + exitLoc.x + " , " + exitLoc.y);
            SimLibCAModel caModel = (SimLibCAModel) ind.getActionController();
            //int gridWidth = caModel.getWorldGrid().getWidth();
            //int gridHeight = caModel.getWorldGrid().getHeight();
            Path indPath = ind.getSelectedPath();

            DoubleGrid2D wayPointGrid = (DoubleGrid2D) caModel.staticMaps.get(indPath.getActiveWaypoint(ind));
            wayPtCounter++;
            // Log.log(2, ind.getID() + "," + wayPtCounter + "," + wayPointGrid.getWidth() + "," + wayPointGrid.getHeight() + "," + CrowdSimulation.getInstance().getSimTime());
            // Populate the radial static gradient for the floor
            // double maxDistToExit = Math.sqrt( (exitLoc.x-gridWidth)*(exitLoc.x-gridWidth) +
            //			      (exitLoc.y-gridHeight)*(exitLoc.y-gridHeight) );
            //for( int x=0; x<gridWidth; x++ )
            //{
            //		for( int y=0; y<gridHeight; y++ )
            //		{
            //			Bag entities = caModel.getTerrainGrid().getObjectsAtLocation(x,y);
            //			if( !( entities != null && entities.size() > 0 )) 
            //			{
            //				double dist = Math.sqrt((x-exitLoc.x)*(x-exitLoc.x) + (y-exitLoc.y)*(y-exitLoc.y));
            // System.out.println("Distance : " + dist);
            // double rescaled = exitGradientScale / (exitGradientScale + Math.pow(dist,exitGradientExp));
            // double rescaled = exitGradientScale / Math.pow(dist,exitGradientExp);
            //				double rescaled = -exitGradientScale*dist+exitGradientScale*Math.max(gridWidth,gridHeight);
            //				wayPointGrid.field[x][y] = rescaled;		
            //			}JobToServiceTimes
            //		}
            //	}
            previousExitLocation = exitLoc;

            return wayPointGrid;
        } else {
            return static2D;
        }
    }

    @Override
    public void calculateMovementParameters(CrowdSimulation state) {
        //Log.log(1,"SimLibStrategy CalcMovementParameters"); 



        if (this.AttachedToStation) {
            moveIndex = null;
            return;
        }

        if (ind.getSelectedPath() == null) {
            //this.createTime = state.getSimTime();
            //Log.log(1,"New Individual : ," + ind.getID() + ", created ," + createTime);	
            setJobType();
        }


        super.calculateMovementParameters(state);
        //PrintOut = true;
        //reset all grids
        staticNeigh2D.setTo(0);
        obsNeighGrid.setTo(1);
        //indNeighGrid.setTo(1);
        //inertiaGrid2D.setTo(1);

        //set lower bounds
        staticNeigh2D.lowerBound(0);
        obsNeighGrid.lowerBound(0);
        // indNeighGrid.lowerBound(0);

        //set up static grid
        static2D = setupWaypointGrid();


        // Assess the position information of the agent in the world.
        SimLibCAModel caModel = (SimLibCAModel) ind.getActionController();

        //System.out.println("Individual ID:" + ind.getID());
        // populate the moore neighborhood grids

        staticNeigh2D = processDouble(staticNeigh2D, static2D);
        //printDblGrid(staticNeigh2D, "The Static Grid");

        obsNeighGrid = processInt(obsNeighGrid, caModel.getTerrainGrid());
        //printIntGrid(obsNeighGrid, "The Obstacle Grid");

        //inertiaGrid2D = processInertia(inertiaGrid2D,lastLocation);
        // printDblGrid(inertiaGrid2D, "The Intertia Grid");


        double staticParam = this.getInverseTemp() * this.getStaticCoupling();
        staticNeigh2D.multiply(staticParam);
        //printDblGrid(staticNeigh2D, "After the Static Grid Processing (deltaS)");

        // combine the grids check for obstacles and individuals
        DoubleGrid2D tempVal2D = new DoubleGrid2D(3, 3, 0);
        // do the mathmatical formula calculations for floor grids 
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                tempVal2D.set(x, y, Math.exp(staticNeigh2D.get(x, y)));
            }
        }

        //printDblGrid(tempVal2DStatic, "The Static grid for movement");
        //printDblGrid(tempVal2D, "The Static grid for movement");

        // cannot move into cells obstacles
        tempVal2D.multiply(obsNeighGrid);
        //Log.log(1,"TheInd" + ind.getID());
        //printIntGrid(obsNeighGrid,"TheObsGrid");
        //apply intertia
        //tempVal2D.multiply(inertiaGrid2D);
        //printDblGrid(tempVal2D, "After the processing of obstacles, individuals, intertia");

        // calculate movement probabilites
        DoubleGrid2D probGrid = new DoubleGrid2D(3, 3, 0);
        double totalVal = 0;
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                probGrid.set(x, y, tempVal2D.get(x, y));
                totalVal = totalVal + probGrid.get(x, y);//[i][j];
            }
        }


        TreeMap theKeyList = new TreeMap();

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {

                double theKey = probGrid.get(x, y) / totalVal;
                Int2D thePoint = new Int2D(x, y);
                if (theKeyList.containsKey(theKey)) {
                    ArrayList thePoints = (ArrayList) theKeyList.get(theKey);
                    thePoints.add(thePoint);
                    theKeyList.put(theKey, thePoints);
                } else {
                    ArrayList thePoints = new ArrayList();
                    thePoints.add(thePoint);
                    theKeyList.put(theKey, thePoints);
                }
            }
        }


        Object[] theKeys = theKeyList.keySet().toArray();
        double theCummlativeVal = 0;
        TreeMap theCdfList = new TreeMap();

        for (int i = 0; i < theKeys.length; i++) {
            double theKeyVal = Double.valueOf(theKeys[i].toString()).doubleValue();
            ArrayList thePoints = (ArrayList) theKeyList.get(theKeyVal);
            theCummlativeVal = thePoints.size() * theKeyVal + theCummlativeVal;
            theKeyList.remove(theKeyVal);
            theCdfList.put(theCummlativeVal, thePoints);

        }

        //Log.log(1,"TheInd" + ind.getID()); 
        //traverseKey(theCdfList.keySet()); 
        //traverseVal(theCdfList.values());

        double draw = state.random.nextDouble();
        //Log.log(1,"draw : " + draw);

        ArrayList theMoves = new ArrayList();
        theKeys = theCdfList.keySet().toArray();
        int theIndex = -1;

        for (int i = 0; i < theKeys.length; i++) {
            double theCdfVal = Double.valueOf(theKeys[i].toString()).doubleValue();
            if (draw < theCdfVal) {
                theMoves = (ArrayList) theCdfList.get(theCdfVal);
                break;
            }
        }

        if (theMoves.size() > 0) {
            math.UniformDistributedGenerator uniformRNG = new math.UniformDistributedGenerator(0, theMoves.size());
            int theMove = (int) uniformRNG.nextValue();
            movePt = (Int2D) theMoves.get(theMove);
            moveIndex = new Int2D(movePt.x - 1, movePt.y - 1);
        //Log.log(1,"Size " + theMoves.size() + " Move Index " + moveIndex.x + " " + moveIndex.y);

        } else if (theMoves.size() != 0) {
            movePt = (Int2D) theMoves.get(0);
            moveIndex = new Int2D(movePt.x - 1, movePt.y - 1);
        //Log.log(1,"Size " + theMoves.size() + " Move Index " + moveIndex.x + " " + moveIndex.y);
        } else {

            //Log.log(1,"No Moves " + ind.getID());
            moveIndex = null;
        }
    }

    /**
     * This is where the individual is moved forward by a small timestep.
     * This is basically integrating the forces over the timestep to calculate where the individuals
     * moves to and moves them there.
     **/
    //called by super class ActionController
    public void move() {
        //Log.log(1,"SimLibModel Move!"); 
        SimLibCAModel caModel = (SimLibCAModel) ind.getActionController();
        if (this.AttachedToStation) {

            return;
        } 
        if (caModel.performMove == true) {
            this.timeWalking += caModel.getTimeStep();
            Int2D location = getLocation();
            // Change the position of the agent, if it decided to move.    
            if (moveIndex != null) {
                lastLocation = location;
                // make sure no-one else has moved there
                int numObjs = caModel.getWorldGrid().numObjectsAtLocation(location.x + moveIndex.x, location.y + moveIndex.y);
                if (numObjs == 0) {
                    //Log.log(1,"The ID " + ind.getID() + " Move Index " + moveIndex.x + "," + moveIndex.y); 
                    int xloc = location.x + moveIndex.x;
                    int yloc = location.y + moveIndex.y;
                    setLocation(xloc, yloc);
                    this.setDistTravelled(this.getDistTravelled() + distance(moveIndex));
                    int theNum = caModel.moveMovementTracker.get(xloc, yloc);
                    caModel.moveMovementTracker.set(xloc, yloc, theNum + 1);
                    if (!successfullMove) {
                        ind.setColor((java.awt.Color) oriColor);
                        this.successfullMove = true;
                    }
                } else {
                    if ((moveIndex.x == 0) && (moveIndex.y == 0)) {
                        //Personal Choice Not Move
                        this.noMove += caModel.getTimeStep();
                    } else {
                        //Move blocked by other people
                        ind.setColor(java.awt.Color.YELLOW);
                        this.successfullMove = false;
                        this.numCollisions++;
                    }

                }
            }

            Int2D newLocation = getLocation();
        }
        if (!AttachedToStation) {
            AttachtoWorkstation();
        }

    }

    public double distance(Int2D moveIndex) {
        return Math.sqrt(moveIndex.x * moveIndex.x + moveIndex.y * moveIndex.y);
    }

    public void AttachtoWorkstation() {

        Bag theObjs;
        StationObstacle theStation;
        SimLibCAModel caModel = (SimLibCAModel) ind.getActionController();
        Int2D individualLoc = this.getLocation();
        SparseGrid2D objGrid = caModel.getTerrainGrid();
        // and to simplify the for-loop, just include the cell itself

        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                try {
                    theObjs = objGrid.getObjectsAtLocation(individualLoc.x + dx, individualLoc.y + dy);
                } catch (Exception e) {
                    theObjs = null;
                }

                if (theObjs != null) {
                    for (int i = 0; i < theObjs.size(); i++) {
                        Object aObj = (Object) theObjs.get(i);
                        Object[] obstacles = CrowdSimulation.getInstance().getTerrain().allObjects.toArray();
                        for (int j = 0; j < obstacles.length; j++) {
                            Obstacle aObstacle = (Obstacle) obstacles[j];
                            Vector2D center = ((Entity) aObj).getCenter();
                            Vector2D loc = new Vector2D(center.x * caModel.getCellWidth(), center.y * caModel.getCellHeight());
                            if (aObstacle.isInside(loc)) {
                                if (aObstacle instanceof StationObstacle) {
                                    theStation = (StationObstacle) aObstacle;
                                    Vector2D stationCenter = theStation.getCenter();
                                    Path path = ind.getSelectedPath();
                                    CircularWaypoint wp = (CircularWaypoint)path.getActiveWaypoint(ind);
                                    if (wp == null) {
                                        return;
                                    }
                                    Vector2D wpCenter = wp.getCenter();
                                    if (stationCenter.approximatelyEqual(stationCenter, wpCenter)) {
                                        theStation.AttachIndividualToStation(ind);
                                    }
                                    return;
                                }
                            }
                        }
                    }
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
        if (parameters.containsKey("exitGradientScale")) {
            setExitGradientScale(Parameters.convertToDouble(
                    parameters.get("exitGradientScale"),
                    getExitGradientScale(),
                    "exitGradientScale for FloorFieldCAStrategy construction must be a Double or a string representing a Double."));
        }
        if (parameters.containsKey("exitGradientExp")) {
            setExitGradientExp(Parameters.convertToDouble(
                    parameters.get("exitGradientExp"),
                    getExitGradientExp(),
                    "exitGradientExp for FloorFieldCAStrategy construction must be a Double or a string representing a Double."));
        }
        if (parameters.containsKey("staticCoupling")) {
            setStaticCoupling(Parameters.convertToDouble(
                    parameters.get("staticCoupling"),
                    getStaticCoupling(),
                    "staticCoupling for FloorFieldCAStrategy construction must be a Double or a string representing a Double."));
        }
        if (parameters.containsKey("inverseTemp")) {
            setInverseTemp(Parameters.convertToDouble(
                    parameters.get("inverseTemp"),
                    getInverseTemp(),
                    "InverseTemp for FloorFieldCAStrategy construction must be a Double or a string representing a Double."));
        }
        if (parameters.containsKey("inertiaConstant")) {
            setInverseTemp(Parameters.convertToDouble(
                    parameters.get("inertiaConstant"),
                    getInertiaConstant(),
                    "InertiaConstant for FloorFieldCAStrategy construction must be a Double or a string representing a Double."));
        }
    }

    public DoubleGrid2D processDouble(DoubleGrid2D localNeighborhood, DoubleGrid2D completeGrid) {

        Int2D individualLoc = this.getLocation();
        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                try {
                    localNeighborhood.set(dx + 1, dy + 1, completeGrid.get(individualLoc.x + dx, individualLoc.y + dy));
                } catch (Exception e) {
                    localNeighborhood.set(dx + 1, dy + 1, 0);
                }
            }
        }
        //System.out.println("Past Local Neighborhood");
        return localNeighborhood;

    }

    public IntGrid2D processInt(IntGrid2D localNeighborhood, SparseGrid2D completeGrid) {
        int numObjs;
        Int2D individualLoc = this.getLocation();
        // and to simplify the for-loop, just include the cell itself
        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                try {
                    numObjs = completeGrid.numObjectsAtLocation(individualLoc.x + dx, individualLoc.y + dy);
                } catch (Exception e) {
                    numObjs = 0;
                }
                if (numObjs > 0) {
                    localNeighborhood.set(dx + 1, dy + 1, 0);
                // System.out.println("numObjs : " + numObjs);
                // PrintOut = true;
                } else {
                    localNeighborhood.set(dx + 1, dy + 1, 1);
                }
            }
        }
        return localNeighborhood;

    }

    public DoubleGrid2D processInertia(DoubleGrid2D localNeighborhood, Int2D lastLocation) {
        if (lastLocation != null) {
            Int2D individualLoc = this.getLocation();
            int dx = (lastLocation.x - individualLoc.x);
            int dy = (lastLocation.y - individualLoc.y);
            double negativeIntertia = Math.exp(-1 * this.getInverseTemp() * this.getStaticCoupling());
            double positiveIntertia = Math.exp(this.getInverseTemp() * this.getInertiaConstant());
            double tempNeg = negativeIntertia * localNeighborhood.get(1 + dx, 1 + dy);
            double tempPos = positiveIntertia * localNeighborhood.get(1 - dx, 1 - dy);
            localNeighborhood.set(1 + dx, 1 + dy, tempNeg);
            localNeighborhood.set(1 - dx, 1 - dy, tempPos);
        }
        return localNeighborhood;
    }

    public void printDblGrid(DoubleGrid2D theGrid, String theTitle) {
        int theWidth = theGrid.getWidth();
        int theHeight = theGrid.getHeight();
        System.out.println(theTitle);
        for (int x = 0; x < theWidth; x++) {
            for (int y = 0; y < theHeight; y++) {
                System.out.println("x " + x + " y " + y + " value " + theGrid.get(x, y));
            }
        }
        System.out.println("-----------------------------------------------------------");
    }

    public void printIntGrid(IntGrid2D theGrid, String theTitle) {
        int theWidth = theGrid.getWidth();
        int theHeight = theGrid.getHeight();
        Log.log(1, theTitle);
        for (int x = 0; x < theWidth; x++) {
            for (int y = 0; y < theHeight; y++) {
                Log.log(1, "x " + x + " y " + y + " value " + theGrid.get(x, y));
            }
        }
    }

    static void traverseKey(Collection coll) {
        Log.log(1, "KEY VALUES");
        Iterator iter = coll.iterator();
        while (iter.hasNext()) {
            String elem = iter.next().toString();
            Log.log(1, elem + " ");
        }

    }

    static void traverseVal(Collection coll) {
        Log.log(1, "HASH VALUES");
        Iterator iter = coll.iterator();
        while (iter.hasNext()) {
            ArrayList theList = (ArrayList) iter.next();
            for (int i = 0; i < theList.size(); i++) {
                Log.log(1, theList.get(i).toString());
            }
        }
    }
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////	
    /**
     * Sets first active floor field parameter
     *
     * @param val - first active floor field parameter
     **/
    public void setExitGradientScale(double val) {
        exitGradientScale = val;
    }

    /**
     * Gets exit scale gradient
     *
     * @return uexit scale gradient
     **/
    public double getExitGradientScale() {
        return exitGradientScale;
    }

    /**
     * Sets exitGradientExp - 
     *
     * @param val - exitGradientExp
     **/
    public void setExitGradientExp(double val) {
        exitGradientExp = val;
    }

    /**
     * Gets exitGradientExp 
     *
     * @return exitGradientExp
     **/
    public double getExitGradientExp() {
        return exitGradientExp;
    }

    /**
     * Sets setStaticCoupling - 
     *
     * @param val - staticCoupling
     **/
    public void setStaticCoupling(double val) {
        staticCoupling = val;
    }

    /**
     * Gets staticCoupling
     *
     * @return staticCoupling
     **/
    public double getStaticCoupling() {
        return staticCoupling;
    }

    /**
     * Gets InertiaConstant
     *
     * @return InertiaConstant
     **/
    public double getInertiaConstant() {
        return inertiaConstant;
    }

    /**
     * Sets InertiaConstant - 
     *
     * @param val - InertiaConstant
     **/
    public void setInertiaConstant(double val) {
        inertiaConstant = val;
    }

    /**
     * Sets setInverseTemp - 
     *
     * @param val - inverseTemp
     **/
    public void setInverseTemp(double val) {
        inverseTemp = val;
    }

    /**
     * Gets inverseTemp
     *
     * @return inverseTemp
     **/
    public double getInverseTemp() {
        return inverseTemp;
    }

    /**
     * Sets setIsWorking - 
     *
     * @param val - IsWorking
     **/
    public void setIsWorking(boolean val) {
        this.IsWorking = val;
    }

    /**
     * Gets IsWorking
     *
     * @return IsWorking
     **/
    public boolean getIsWorking() {
        return this.IsWorking;
    }

    /**
     * Sets setInverseTemp - 
     *
     * @param val - inverseTemp
     **/
    public void setAttachToStation(boolean val) {
        this.AttachedToStation = val;
    }

    /**
     * Gets inverseTemp
     *
     * @return inverseTemp
     **/
    public boolean getAttachToStation() {
        return this.AttachedToStation;
    }

    public void setInQueue(boolean val) {
        this.InQueue = val;
    }

    /**
     * Gets inverseTemp
     *
     * @return inverseTemp
     **/
    public boolean getInQueue() {
        return this.InQueue;
    }

    public void setTheStation(int val) {
        this.TheStation = val;
    }

    /**
     * Gets inverseTemp
     *
     * @return inverseTemp
     **/
    public int getTheStation() {
        return this.TheStation;
    }

    public void setEnterQueueTime(double val) {
        this.enterQueueTime = val;
    }

    public double getEnterQueueTime() {
        return this.enterQueueTime;
    }

    public void setExitQueueTime(double val) {
        this.exitQueueTime = val;
    }

    public double getExitQueueTime() {
        return this.exitQueueTime;
    }

    public void setTaskStartTime(double val) {
        this.taskStartTime = val;
    }

    public double getTaskStartTime() {
        return this.taskStartTime;
    }

    public void setTaskDurationTime(double val) {
        this.taskDurationTime = val;
    }

    public double getTaskDurationTime() {
        return this.taskDurationTime;
    }

    public int getJobNumber() {
        return this.jobNumber;
    }

    public void settaskNumber(int val) {
        this.taskNumber = val;
    }

    public double gettaskNumber() {
        return this.taskNumber;
    }

    public void setDistTravelled(double val) {
        distTravelled = val;
    }

    public double getDistTravelled() {
        return distTravelled;
    }
}
