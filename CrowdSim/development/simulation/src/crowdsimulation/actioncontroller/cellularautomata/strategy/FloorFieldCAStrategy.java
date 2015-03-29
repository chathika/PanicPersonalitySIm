package crowdsimulation.actioncontroller.cellularautomata.strategy;

import crowdsimulation.*;
import crowdsimulation.util.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.actioncontroller.cellularautomata.*;
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

public class FloorFieldCAStrategy extends CellularAutomataStrategy {
///////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
    DoubleGrid2D globalNeighDynamic2D = new DoubleGrid2D(3, 3, 0);
    DoubleGrid2D indNeighDynamic2D = new DoubleGrid2D(3, 3, 0);
    DoubleGrid2D staticNeigh2D = new DoubleGrid2D(3, 3, 0);
    DoubleGrid2D inertiaGrid2D = new DoubleGrid2D(3, 3, 1);
    IntGrid2D obsNeighGrid = new IntGrid2D(3, 3, 1);
    IntGrid2D indNeighGrid = new IntGrid2D(3, 3, 1);
    DoubleGrid2D static2D;
    DiffuserDoubleGrid2D indDynamic2D = null;
    private DoubleGrid2D indDynamic2DCalc;
    public double staticCoupling = 2.0; //coupling strength static floor fields
    public double dynamicCoupling = 1.0; //coupling strength dynamic floor fields
    public double inertiaConstant = 0.2; //tune the interia
    public double inverseTemp = 1; //inverse temperature discrete floor fields
    public double exitGradientScale = 1;
    public double exitGradientExp = 1.0;
    public Int2D lastLocation = null;
    protected Int2D moveToPoint = null;    public boolean performMove = false;
    public HashMap staticMaps = new HashMap();
    public IntGrid2D moveMovementTracker;

    boolean PrintOut;
    Int2D moveIndex = null;
    Int2D movePt = new Int2D();
    private boolean wayPointChange = true;
    private Int2D previousExitLocation = new Int2D();
    private Object oriColor;
    private boolean successfullMove = true;
    public int numCollisions = 0;
    public double distTravelled = 0;
    public double createTime = -99999;
    public double timeWalking = 0;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
    public FloorFieldCAStrategy() {
        super();
    }

    /**
     * Sets the attributes for the Action Controller defined in this class.
     **/
    public FloorFieldCAStrategy(Map parameters) {
        this();
        setParameters(parameters);
    }

    /**
     * Leave a trace in the world for other agents.  This will be used to assess the
     * "Dynamic" term in the transition probability computation.
     **/
    //called by this.move --- this.move called by super class ActionController
    public void leaveTrace(Int2D lastLocation) {
        FloorFieldCAModel caModel = (FloorFieldCAModel) ind.getActionController();
        caModel.globalBosonGrid.field[lastLocation.x][lastLocation.y] += 10;
        this.indDynamic2D.field[lastLocation.x][lastLocation.y] += 10;
    }

    //called by this.calculateMovementParameters
    public DoubleGrid2D setupWaypointGrid() {
        Int2D exitLoc = getWayPointLocation();
        
       
        //Alan Jolly
        //TODO: Why do I have to do this?
        if (previousExitLocation.x != exitLoc.x || previousExitLocation.y != exitLoc.y) {
            
            
            
            Path indPath = ind.getSelectedPath();
            // System.out.println("WayPoint: " + exitLoc.x + " , " + exitLoc.y);
            FloorFieldCAModel caModel = (FloorFieldCAModel) ind.getActionController();
            //int col = caModel.getWorldGrid().getWidth();
            //int row = caModel.getWorldGrid().getHeight();
            
            
            DoubleGrid2D wayPointGrid = (DoubleGrid2D)caModel.staticMaps.get(indPath.getActiveWaypoint(ind));
           
            // DoubleGrid2D wayPointGrid = new DoubleGrid2D(col, row, 0);


            // Populate the radial static gradient for the floor
            // double maxDistToExit = Math.sqrt( (exitLoc.x-gridWidth)*(exitLoc.x-gridWidth) +
            //			      (exitLoc.y-gridHeight)*(exitLoc.y-gridHeight) );
            //for (int x = 0; x < col; x++) {
            //    for (int y = 0; y < row; y++) {
            //        Bag entities = caModel.getTerrainGrid().getObjectsAtLocation(x, y);
            //        if (!(entities != null && entities.size() > 0)) {
            //            double dist = Math.sqrt((x - exitLoc.x) * (x - exitLoc.x) + (y - exitLoc.y) * (y - exitLoc.y));
                        // System.out.println("Distance : " + dist);
                        // double rescaled = exitGradientScale / (exitGradientScale + Math.pow(dist,exitGradientExp));
                        // double rescaled = exitGradientScale / Math.pow(dist,exitGradientExp);
             //           double rescaled = -exitGradientScale * dist + exitGradientScale * Math.max(col, row);
             //           wayPointGrid.field[x][y] = rescaled;
             //       }
             //   }
            //}
            previousExitLocation = exitLoc;

            return wayPointGrid;
        } else {
            return static2D;
        }
    }

    @Override
    public void calculateMovementParameters(CrowdSimulation state) {
        super.calculateMovementParameters(state);
        //PrintOut = true;
        //reset all grids
        if (oriColor == null) {
            oriColor = ind.getColor();
        }
        if (this.createTime == -99999){
            this.createTime = CrowdSimulation.getInstance().getSimTime();
        }
        globalNeighDynamic2D.setTo(0);
        indNeighDynamic2D.setTo(0);
        staticNeigh2D.setTo(0);
        obsNeighGrid.setTo(1);
        indNeighGrid.setTo(1);
        inertiaGrid2D.setTo(1);

        //set lower bounds
        globalNeighDynamic2D.lowerBound(0.0);
        indNeighDynamic2D.lowerBound(0);
        staticNeigh2D.lowerBound(0);
        obsNeighGrid.lowerBound(0);
        indNeighGrid.lowerBound(0);

        //set up static grid
        static2D = setupWaypointGrid();


        // Assess the position information of the agent in the world.
        FloorFieldCAModel caModel = (FloorFieldCAModel) ind.getActionController();

        // intialize indDynamic2D if = null
        if (indDynamic2D == null) {
            indDynamic2D = new DiffuserDoubleGrid2D(caModel.getNumCellsAcross(), caModel.getNumCellsDown(), 0);
        }

        // System.out.println("Individual ID:" + ind.getID());
        // populate the moore neighborhood grids
        globalNeighDynamic2D = processDouble(globalNeighDynamic2D, caModel.getbosonGrid());
        // System.out.println(caModel.getbosonGrid());
        //printDblGrid(globalNeighDynamic2D, "The Global Neighborhood Dynamic Grid");
        //Alan Jolly 3/24/08
        indDynamic2D.diffuse(0.9, 0.3);
        indDynamic2D.decay(0.3, 0.2);

        indNeighDynamic2D = processDouble(indNeighDynamic2D, indDynamic2D);
        // System.out.println(indDynamic2D);
        //printDblGrid(indNeighDynamic2D, "The Individual Neighborhood Dynamic Grid");
        // System.out.println("indNeighDynamic2D done");
        staticNeigh2D = processDouble(staticNeigh2D, static2D);
        // printDblGrid(staticNeigh2D, "The Static Grid");
        // System.out.println("staticNeigh2D done");
        obsNeighGrid = processInt(obsNeighGrid, caModel.getTerrainGrid());
        // printIntGrid(obsNeighGrid, "The Obstacle Grid");

        indNeighGrid = processInt(indNeighGrid, caModel.getWorldGrid());
        //set the center cell yourself to 1
        indNeighGrid.set(1, 1, 1);
        // printIntGrid(indNeighGrid, "The Neighborhood Grid");

        inertiaGrid2D = processInertia(inertiaGrid2D, lastLocation);
        // printDblGrid(inertiaGrid2D, "The Intertia Grid");
        // System.out.println("inertiaGrid2D done");

        indNeighDynamic2D.multiply(-1);

        globalNeighDynamic2D.add(indNeighDynamic2D);
        //printDblGrid(globalNeighDynamic2D, "The Global Dynamic Grid - Individual Dynamic Grid");

        // double centerDynVal = -1*globalNeighDynamic2D.get(1,1);
        // globalNeighDynamic2D.add(centerDynVal);

        double dynParam = this.getInverseTemp() * this.getDynamicCoupling();
        globalNeighDynamic2D.multiply(dynParam);
        //printDblGrid(globalNeighDynamic2D, "After the Global Dynamic Grid Processing (deltaD)");

        // double centerStaticVal = -1*staticNeigh2D.get(1,1);
        // staticNeigh2D.add(centerStaticVal);

        double staticParam = this.getInverseTemp() * this.getStaticCoupling();
        staticNeigh2D.multiply(staticParam);
        //printDblGrid(staticNeigh2D, "After the Static Grid Processing (deltaS)");

        // combine the grids check for obstacles and individuals
        DoubleGrid2D tempVal2D = new DoubleGrid2D(3, 3, 0);
        // do the mathmatical formula calculations for floor grids 
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                tempVal2D.set(x, y, Math.exp(globalNeighDynamic2D.get(x, y)) * Math.exp(staticNeigh2D.get(x, y)));
            // tempVal2D.set(x,y,Math.exp(staticNeigh2D.get(x,y)));
            }
        }

        // printDblGrid(tempVal2DDynamic, "The Dynamic grid for movement");
        // printDblGrid(tempVal2DStatic, "The Static grid for movement");
        // printDblGrid(tempVal2D, "The Determinisitic grid for movement");


        // cannot move into cells obstacles
        tempVal2D.multiply(obsNeighGrid);

        // cannot move into cells individuals
        tempVal2D.multiply(indNeighGrid);
        //apply intertia
        tempVal2D.multiply(inertiaGrid2D);
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

        if (totalVal == 0) {
            System.out.println("No totalvalue");
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


        //traverseKey(theCdfList.keySet()); 
        //traverseVal(theCdfList.values());
        // System.out.println("*******************************");


        double draw = state.random.nextDouble();
        // System.out.println("draw : " + draw);

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

        if (theMoves.size() > 1) {
            math.UniformDistributedGenerator uniformRNG = new math.UniformDistributedGenerator(0, theMoves.size());
            int theMove = (int) uniformRNG.nextValue();
            movePt = (Int2D) theMoves.get(theMove);

        } else {
            movePt = (Int2D) theMoves.get(0);
        }

        moveIndex = new Int2D(movePt.x - 1, movePt.y - 1);

    // System.out.println("moveIndex: x:" + moveIndex.getX() + " y:" + moveIndex.getY());
    }

    /**
     * This is where the individual is moved forward by a small timestep.
     * This is basically integrating the forces over the timestep to calculate where the individuals
     * moves to and moves them there.
     **/
    //called by super class ActionController
    public void move() {
        FloorFieldCAModel caModel = (FloorFieldCAModel) ind.getActionController();
        if (caModel.performMove == true) {
            this.timeWalking += caModel.getTimeStep();
            Int2D location = getLocation();
            // Change the position of the agent, if it decided to move.    
            if (moveIndex != null) {
                this.lastLocation = location;
                // make sure no-one else has moved there
                int numObjs = caModel.getWorldGrid().numObjectsAtLocation(location.x + moveIndex.x, location.y + moveIndex.y);
                if (numObjs == 0) {
                    setLocation(location.x + moveIndex.x, location.y + moveIndex.y);
                    // Leave a value in the dynmic gird for other pedestrians
                    leaveTrace(location);
                    this.setDistTravelled(this.getDistTravelled() + distance(moveIndex));
                    if (!successfullMove) {
                        ind.setColor((java.awt.Color) oriColor);
                        this.successfullMove = true;
                    }
                } else {
                    if ((moveIndex.x == 0) && (moveIndex.y == 0)) {
                        //Personal Choice Not Move
                    } else {
                        //Move blocked by other people
                        ind.setColor(java.awt.Color.YELLOW);
                        this.successfullMove = false;
                        this.numCollisions++;
                    }
                }
            }
        }
    }

    public double distance(Int2D moveIndex) {
        return Math.sqrt(moveIndex.x * moveIndex.x + moveIndex.y * moveIndex.y);
    }

    public void IntermediateStates() {
    }

    /**
     * Sets all the parameters from a key value pairs collection.
     *
     * @param parameters The parameters for the indiviudal.
     **/
    public void setParameters(Map parameters) {
        super.setParameters(parameters);

//		if( parameters.containsKey( "b1" ) )
//		{
//			setb1( Parameters.convertToDouble ( 
//				parameters.get( "b1" ), 
//				getb1(),
//			   "b1 for FloorFieldCAStrategy construction must be a Double or a string representing a Double." ) );
//		}
//		if( parameters.containsKey( "b2" ) )
//		{
//			setb2( Parameters.convertToDouble( 
//				parameters.get( "b2" ), 
//				getb2(),
//			   "b2 for FloorFieldCAStrategy construction must be a Double or a string representing a Double." ) );
//		}
//		if( parameters.containsKey( "g1" ) )
//		{
//			setg1( Parameters.convertToDouble ( 
//				parameters.get( "g1" ), 
//				getg1(),
//			   "g1 for FloorFieldCAStrategy construction must be a Double or a string representing a Double." ) );
//		}
//		if( parameters.containsKey( "g2" ) )
//		{
//			setg2( Parameters.convertToDouble( 
//				parameters.get( "g2" ), 
//				getg2(),
//			   "g2 for FloorFieldCAStrategy construction must be a Double or a string representing a Double." ) );
//		}
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
        if (parameters.containsKey("dynamicCoupling")) {
            setDynamicCoupling(Parameters.convertToDouble(
                    parameters.get("dynamicCoupling"),
                    getDynamicCoupling(),
                    "dynamicCoupling for FloorFieldCAStrategy construction must be a Double or a string representing a Double."));
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
        //System.out.println("Individual Location "+ individualLoc.x + "," + individualLoc.y);
        //DoubleBag theResult = new DoubleBag();
        //IntBag xPos = new IntBag();
        //IntBag yPos = new IntBag();
        //completeGrid.getNeighborsMaxDistance(individualLoc.x,individualLoc.y,1,false,theResult,xPos,yPos);
        //System.out.println("The resultbag size "+ theResult.numObjs);
        //double[] theArray = theResult.toArray();
        //int[] x = xPos.toArray();
        //int[] y = yPos.toArray();

        //for(int i = 0;i<theArray.length;i++)
        //{
        //	System.out.println("Result " + theArray[i]);
        ////	System.out.println("x " + x[i]);
        //	System.out.println("y " + y[i]);
        //}
        // and to simplify the for-loop, just include the cell itself
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
            double negativeIntertia = Math.exp(-1 * this.getInverseTemp() * this.getDynamicCoupling());
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
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        for (int row = 0; row < theHeight; row++) {
            for (int col = 0; col < theWidth; col++) {
                System.out.print(df.format(theGrid.get(col, row)) + " ");
            }
            System.out.println();
        }
        System.out.println("-----------------------------------------------------------");
    }

    public void printIntGrid(IntGrid2D theGrid, String theTitle) {
        int theWidth = theGrid.getWidth();
        int theHeight = theGrid.getHeight();
        System.out.println("-----------------------------------------------------------");
        System.out.println(theTitle);
        for (int x = 0; x < theWidth; x++) {
            for (int y = 0; y < theHeight; y++) {
                System.out.println("x " + x + " y " + y + " value " + theGrid.get(x, y));
            }
        }
        System.out.println("-----------------------------------------------------------");
    }

    static void traverseKey(Collection coll) {
        Iterator iter = coll.iterator();
        while (iter.hasNext()) {
            String elem = iter.next().toString();
            System.out.println(elem + " ");
        }
        System.out.println();
    }

    static void traverseVal(Collection coll) {
        Iterator iter = coll.iterator();
        while (iter.hasNext()) {
            ArrayList theList = (ArrayList) iter.next();
            for (int i = 0; i < theList.size(); i++) {
                System.out.print(theList.get(i).toString());
            }
            System.out.println();
        }
        System.out.println();
    }
    ////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////	
    /**
     * Sets first active floor field parameter
     *
     * @param val - first active floor field parameter
     **/
//	public void setb1( double val )
//	{
//		b1 = val;
//	}
//	/**
//	 * Gets first active floor field parameter
//	 *
//	 * @return first active floor field parameter
//	 **/
//    public double getb1()
//	{
//		return b1;
//	}
//	/**
//	 * Sets first active floor field parameter
//	 *
//	 * @param val - first active floor field parameter
//	 **/
//	public void setb2( double val )
//	{
//		b2 = val;
//	}
//	/**
//	 * Gets first active floor field parameter
//	 *
//	 * @return first active floor field parameter
//	 **/
//    public double getb2()
//	{
//		return b2;
//	}
//		/**
//	 * Sets first passive floor field parameter
//	 *
//	 * @param val - first passive floor field parameter
//	 **/
//	public void setg1( double val )
//	{
//		g1 = val;
//	}
//	/**
//	 * Gets first passive floor field parameter
//	 *
//	 * @return first passive floor field parameter
//	 **/
//    public double getg1()
//	{
//		return g1;
//	}
//	/**
//	 * Sets first passive floor field parameter
//	 *
//	 * @param val - first passive floor field parameter
//	 **/
//	public void setg2( double val )
//	{
//		g2 = val;
//	}
//	/**
//	 * Gets first passive floor field parameter
//	 *
//	 * @return first passive floor field parameter
//	 **/
//    public double getg2()
//	{
//		return g2;
//	}
//	/**
//	 * Sets exit scale gradient
//	 *
//	 * @param val - exit scale gradient
//	 **/
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
    GREEN * Sets setStaticCoupling - 
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
     * Sets setDynamicCoupling - 
     *
     * @param val - dynamicCoupling
     **/
    public void setDynamicCoupling(double val) {
        dynamicCoupling = val;
    }

    /**
     * Gets dynamicCoupling
     *
     * @return dynamicCoupling
     **/
    public double getDynamicCoupling() {
        return dynamicCoupling;
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
     * Sets setInverseTemp - 
     *
     * @param val - inverseTemp
     **/
    public void setDistTravelled(double val) {
        distTravelled = val;
    }

    /**
     * Gets inverseTemp
     *
     * @return inverseTemp
     **/
    public double getDistTravelled() {
        return distTravelled;
    }
}
