/*
 * $RCSfile: CrowdSimulation.java,v $ $Date: 2011/06/03 17:40:06 $
 */
package crowdsimulation;

import crowdsimulation.actioncontroller.*;	// Contains models of behavior for the entities. 
import crowdsimulation.entities.*;		// Contains definition of the crowd agents and features.
import crowdsimulation.entities.individual.*;
import crowdsimulation.logging.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.media.*;
import javax.swing.*;
import math.*;
import sim.display.*;
import sim.engine.*;
import sim.util.*;
import sim.util.gui.*;
import sim.util.media.*;
import sim.field.continuous.*;
import sim.portrayal.*;
//Alan Jolly 3/10/08
import sim.field.grid.*;

/** 
 * The execution class which contains all simulation state information.
 * This class sets up the simulations and is responsible for constructing all
 * obstacles, and individuals in the simulations. 
 *
 * @author $Author: dkaup $
 * @version $Revision: 1.16 $
 * $State: Exp $
 * $Date: 2011/06/03 17:40:06 $
 **/
public class CrowdSimulation extends SimState implements Steppable
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** A random number generator to be used for randomizing aspects of the simulation. **/
    private RandomGenerators randomGenerator;

	/** The number of steps which have been executed. **/
	private int stepNum = 0;
	/** The current time for the simulation. **/ 
	private double simTime = 0;
	/** The time step currently being used. **/
	private double deltaT = 0;
	/** The time of at which the simulation started (Real World). **/
	private Date startTime;
	/** Contains collection of all individuals. **/
	private Continuous2D world;
	/** Contains collection of all obstacles. **/
        private Continuous2D terrain;
        /** Contains collection of entities that are noninteractive. **/
	private Continuous2D worldInfo;
	/** The width of the world. **/
	public double worldWidth = 0;
	/** The height of the world. **/
	public double worldHeight = 0;
	
	/** Number of elements to include in discretization of the world. 10 times this for the terrain. **/
	private double discretization = 100;
	/** The amount of time which the simulation should run. **/
	private double duration = 0;

	/** If the simulation should terminate when all individuals are gone. **/
	private boolean terminate = false;

	/** The app config variable. This is not used directly. Used only to check if appConfig has been created or not. **/
	private AppConfig appConfig = null;
	private String configFileName;
	
        /*Movie Generation*/
	/** Should a movie be captured. **/
	private boolean captureMovie = false;
	/** The MovieMaker to be used to generate the movie. **/
	private MovieEncoder movie = null;
	/** The type of movie to be generated. **/
	private String movieType = "";
	/** The name to be given to the generated movie. **/ 
	private String movieName = "";
	/** The file to be used to the store the movie. **/ 
	private File movieFile = null;
	/** The frame rate to be used in captureing frames for the movie. **/
	private int frameRate = 30;
	/** A collection of all the paths. **/
	private HashMap paths = new HashMap();
	
	/* Logging */
	/** Action model used to move most individuals. **/
	private ActionController primaryActionController;
	/** The loggers to use to save the data from the simulations. **/
	private Bag loggers = new Bag();
	
	/** Class based holder for the single instance of a CrowdSimulation object. **/
	private static CrowdSimulation instance;
	/** The default scale to be used for the graphical display. **/
	private double visualScale = 1;
	/** The collection of all items being simulated. **/
	private Bag items = new Bag();
	/** The UI class which started this simualtion.**/
	private CrowdSimulationWithUI ui = null;
	
	/** Records the last time that a sample of the data was taken. **/
	private double lastCapTime = 0;
	/** The frame used to record the movie. **/
	public JFrame otherframe;
	/** The panel used to record the movie. **/
	public JPanel panel;
	/** The portrayls which are used for hte individuals in the simulation. **/
	private HashMap portrayals = new HashMap();
	
	//Alan Jolly
	private DoubleGrid2D theGlobalBosonGrid;
	
	////////////////////////////////////////////////////////////////////////////////
	// JobShop StatCounter
	////////////////////////////////////////////////////////////////////////////////	

	private SimLog updateStats = new SimLog();

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * This is the execution thread to run the simulation.
	 *
	 * @param args These are the command line parameters to adjust the execution 
	 *	of the simulation.
	 **/
	public static void main( String args[] )
	{
    	String configFile=null;
    	if( args.length > 0)
    	{
    		configFile = args[0];
    	}
    	
		Log.setConsoleLogFileName(  "../logs/system.log"  ); // This filename may get replaced in the next line

//		CrowdSimulation sim = new CrowdSimulation( false, System.currentTimeMillis(), configFile );
		CrowdSimulation sim = new CrowdSimulation( false, 0, configFile );
		
		new Log(Log.getConsoleLogFileName());
		Log.log( 0, "Starting CrowdSimulation..." );
		
		doLoop( sim, args);
        System.exit(0);
	}

	/**
	 * Constructor which takes in a random number generator.
	 * 
	 * @param ui Designates if the ui is active or not. Not currently implemented, or used.
	 * @param seed This is the seed to be used to generate random numbers.
	 * @param configFileName The name of the configuration file to be used to configure the simulation.
	 **/
	public CrowdSimulation( boolean ui, long seed, String configFileName )
	{
    	super( new ec.util.MersenneTwisterFast( seed ), new Schedule(  ) );
    	this.randomGenerator = new UniformDistributedGenerator( 0, 1 );
    	instance = this;

		this.configFileName = configFileName;

		System.out.println("In common Init");
		// Initialize the world, terrain, obstacles, and individuals base on a config file
		// cant do this now.
		if(!ui) {
			System.out.println("In unique CL section 1");
			appConfig = new AppConfig( configFileName, this );
			appConfig.initializeCrowdSimulation();
		}
	}
	
	/**
	 * Constructor which takes in a random number generator, and a config file.
	 * Constructs a CrowdSimualtion object with the given seed and loads the configuration file
	 * with the name which is passed in.
	 * 
	 * @param seed This is the seed to be used to generate random numbers.
	 * @param configFileName The name of the configuration file to be used to configure the simulation.
	 **/
	public CrowdSimulation( long seed, String configFileName )
	{
    	this( false, seed, configFileName );
	}
	
	/**
	 * Constructor which takes in a random number generator. 
	 * Generates the CrowdSimulation object with a given seed and the default cofig file.
	 * 
	 * @param seed This is the seed to be used to generate random numbers.
	 **/
	public CrowdSimulation( long seed )
	{
    	this( seed, (String)null );
	}
	
	/**
	 * Constructor which takes in a random number generator.
	 * This is the constructor which is used for the ui and no confgiguration file given.
	 * The constructs the Crowdsim and sets the UI component.
	 * 
	 * @param seed This is the seed to be used to generate random numbers.
	 * @param ui This is the UI which wil be displayed.
	 **/
	public CrowdSimulation( long seed, CrowdSimulationWithUI ui )
	{
    	super( new ec.util.MersenneTwisterFast( seed ), new Schedule(  ) );
    	instance = this;
    	

		// Initialize the world, terrain, obstacles, and individuals base on a config file
		appConfig = new AppConfig( null, this );
		appConfig.initializeCrowdSimulation();
		
	}
		
	/**
	 * Generates a random number.
	 *
	 * @return A number generated from the random generator in the ActionController.
	 **/
	public double rand()
	{
		return randomGenerator.nextValue();
	}


    /** 
     * This is the primary execution loop for the simulation.
     * This Calls doLoop(MakesSimState,args), passing in a MakesSimState which creates
     * SimStates of the provided Class c, using the constructor new SimState(Random seed).
     *
     * @param simState The SimulationState which should be used to loop over.
     * @param args Any arguments which chould be used during the looping.
     **/
    public static void doLoop( final CrowdSimulation simState, String[] args )
    {
	    doLoop( new MakesSimState()
	    {
	    	public SimState newInstance( long seed, String[] args )
	        {
	            try
	            {
	                return (SimState)simState;
	            }
	            catch( Exception e )
	            {
	            	throw new RuntimeException("Exception occurred while trying to construct the simulation: " + e);
	            }
	        }
	        
	        public Class simulationClass() { return simState.getClass(); }
	    }, args );
    }


	/**
	 * This is the method which set up the environment and starts executing the simulation.
	 **/
    public void start()
    {
        super.start();
        startTime = new Date();
        
    	this.schedule( EntityFactory.getInstance() );
    	
		for( int i=0; i < loggers.size(); i++ )
		{
			((Logger)loggers.get( i )).init();
		}
    	
        schedule.scheduleRepeating( this );
        
    }

	/**
	 * This causes the simulation to pause. This will only work if the UI is being used.
	 * It occurs in the same manor as pressing the pause button on the UI.
	 **/
	public void pause()
	{
		if( this.ui != null )
		{
			((sim.display.Console)this.ui.controller).pressPause();
		}
	}
    
	/**
	 * This causes the simulation to start. This will only work if the UI is being used.
	 * It occurs in the same manor as pressing the pause button on the UI.
	 **/
	public void go()
	{
		if( this.ui != null )
		{
			((sim.display.Console)this.ui.controller).pressPlay();
		}
	}
    
    /**
     * The method executed at every step of the simulation.
     * The execution is as follows:
     *  1. call prestep on all ActionControllers in the items bag.
     *  2. call step on all Steppable elements in the items bag.
     *  3. call poststep on all ActionControllers in the items bag.
     *  4. update simtime.
     *  5. update step number.
     *  6. excute all registered loggers.
     *  7. If recording movie and time since last framecapture is greater then the 
     *       1/frameRate then take a new framecapture.
     *  8. If duration is set and the value is grater then the time passed, then end the simulation.
     *  9. otherwise repeat the process.
     * 
     * @param state The SimState object which contains the simulations current state.
     **/
    public void step( SimState state )
    {
        deltaT = Double.MAX_VALUE;
    	
    	// Execute PreStep on all ActionControllers.
    	// This should give us what timestep is needed to be used.
    	for( int i = 0; i < items.size(); i++ )
    	{
    		Steppable stepItem = (Steppable)items.get( i );
    		if( stepItem instanceof ActionController )
    		{
    			((ActionController)stepItem).preStep( this );
    			double tStep = ((ActionController)stepItem).getTimeStep();
    			if( tStep < deltaT )
    			{
    				deltaT = tStep;
    			}
    		}
    	}
    	
    	// Execute Step on all steppable items.
    	for( int i = 0; i < items.size(); i++ )
    	{
    		Steppable stepItem = (Steppable)items.get( i );
   			stepItem.step( this );
    	}
    	
    	// Execute PostStep on all ActionControllers.
    	for( int i = 0; i < items.size(); i++ )
    	{
    		Steppable stepItem = (Steppable)items.get( i );
    		if( stepItem instanceof ActionController )
    		{
    			((ActionController)stepItem).postStep( this );
    		}
    	}
    	
    	simTime += deltaT;
    	stepNum++;
    	
    	// Record data in each of the logs.
		for( int i=0; i < loggers.size(); i++ )
		{
			((Logger)loggers.get( i )).log();
		}

		//System.out.println("MovieName = " + movieName);
		//System.out.println("ui = " + ui);
		//System.out.println("simtime = " + simTime + ", lastcaptime = "+lastCapTime +", framerate = "+frameRate);
		// Record the frame if the appropriate amount of time has passed.
        if( ui != null && captureMovie && simTime-lastCapTime > 1.0/frameRate )
        {

	        if( captureMovie && ui != null && movie == null )
	        {
	        	Graphics g = panel.getGraphics();
	            final BufferedImage i = ((ui.getDisplay()).insideDisplay).paint( g, true, false );
	            g.dispose();  // because we got it with getGraphics(), we're responsible for it
	            
	            Format formats[] = MovieEncoder.getEncodingFormats( frameRate, i );
	            
				for(Format f : formats) System.out.println("Format = " + f);

				movieFile = new File("../logs/"+movieName);
	        	movie = new MovieEncoder( (float)frameRate, movieFile, i, formats[0] );
	        }
    		
    		Graphics g = panel.getGraphics();
        	final BufferedImage i = ((ui.getDisplay()).insideDisplay).paint( g, true, false );
        
    		movie.add( i );    
        
        	g.drawImage( i, 0, 0, null );
        	g.dispose();  // because we got it with getGraphics(), we're responsible for it
        	panel.repaint();
        	
            lastCapTime = simTime;
        }

        // If the duration time has been reached then end the simulation.
        if( duration != 0 && simTime >= duration )
        {
        	
        	this.kill();
        	System.exit(0);
        }
        else if( terminate )
        {
        	int numberOfIndividuals = 0;
        	
        	for( int i = 0; i < items.size(); i++ )
        	{
        		Steppable stepItem = (Steppable)items.get( i );
        		
        		if( stepItem instanceof ActionController )
        		{
        			numberOfIndividuals += ((ActionController)stepItem).getIndividuals().size();
        		}
        	}
			//if( ((int)simTime) % 10 == 0)
				//System.out.println(simTime + " I"+numberOfIndividuals);
        	if( numberOfIndividuals == 0 )
        	{
        		this.kill();
        		System.exit(0);
        	}
        }
    }
    
    /**
     * This schedules a steppable item to be called durring each execution of step.
     * Items should be added in the order of which they are to be executed.
     * First item in will be the first item to be called at each timestep.
     *
     * @param item The Steppable entity which should have step called on it during each step of the simulation.
     **/
    public void schedule( Steppable item )
    {	
    	items.add( item );
    }
    
    /**
     * This stops the simulation and stops recording the movie.
     **/
    public void kill()
    {
    	super.kill();
        if( movie != null ) movie.stop();
        System.exit(0);
    }

//	/**
//     * Note by DJK on 27Mar2011: This method was originally misprogrammed.
//     *
//	 * This method will confine an indiviudal to stay on the bounds of the simulated world.
//	 *
//	 * @param ind The individual to confine to the world.
//	 * @param x The new X location for the individual.
//	 * @param y The new Y location for the individual.
//	 * @return The Vector2D representing the new location of the individual.
//	 **/
//	public Vector2D confineToWorld( Individual ind, double x, double y )
//	{
//		double newX = 0;
//		double newY = 0;
//        double xc = ind.getX();
//        double yc = ind.getY();
//        Vector2D norm = new Vector2D(x-xc, y-yc); //length and direction of movement.
//        double dist = norm.magnitude();    // length of motion needed.
//
//		if( x < 0 ) newX = ind.getRadius();  // /changed Diameter to Radius.
//		if( y < 0 ) newY = ind.getRadius();
//        // the following equation originally contained: 1)  max(0,x) and 2) the
//        // getRadius part was missing. Whence the first two lines never did anything
//        // and an ind's center could be "exactly on" the wall.
//		newX = Math.min( Math.max( 0, newX ), world.getWidth() - ind.getRadius());
//		newY = Math.min( Math.max( 0, newY ), world.getHeight() - ind.getRadius());
//
//		return new Vector2D( newX, newY );
//        // This will always keep the individual inside the box, but could leave him
//        // totally stationary.  For a salamanderm, he must be able to move a distance
//        // along the wall, the same that he would have if the wall was not there.
//	}
//

	/**
     * Note by DJK on 14Apr2011:  We are redesigning this method to wrap the path
     * around the world, whenever the new position is outside the world.  The direction
     * to do the wrap is in the direction of the component of the change in the position
     * which is parallel to the world wall he is next to.  We assume that he shall never
     * have move farther than the smallest world dimension.
     * .
     * Note by DJK on 27Mar2011: This method was originally misprogrammed.
     *
	 * This method will confine an indiviudal to stay on the bounds of the simulated world.
	 *
	 * @param ind The individual to confine to the world.
	 * @param x The new X location for the individual.
	 * @param y The new Y location for the individual.
	 * @return The Vector2D representing the new location of the individual.
	 **/
	public Vector2D confineToWorld( Individual ind, double x, double y )
	{
		double newX = 0;
		double newY = 0;
        int corner = 0;   // 0 = none,  1 = NW, 2 = NE, 3 = SE, 4 = SW
        int side = 0;     // 0 = none,  1 = W, 2 = N, 3 = E, 4 = S
        double xc = ind.getX();
        double yc = ind.getY();
        double rad = ind.getRadius();
        Vector2D dir = new Vector2D(x-xc, y-yc); //length and direction of change in position.
        double dist = dir.magnitude();    // length of motion needed.
        Vector2D edge = new Vector2D(0,0);
        Vector2D pnm1 = new Vector2D(0,0);
        double dirXEdge = 0.0;
        double alpha = 0.0;
        double beta = 0.0;
        double dL=0.0;

        if( x <= rad )
        {
            if( y <= rad )
            {
                corner = 1;           // It is at the NW corner.
                pnm1 = new Vector2D( rad, rad ); // This is the location.
                edge = new Vector2D( -1.0, -1.0 );  // This is the orientation of the corner.
            }
            else
            {
                if( y >= world.getHeight()- rad )
                {
                    corner = 4;  // it is at the SW corner.
                    pnm1 = new Vector2D( rad, world.getHeight()- rad );
                    edge = new Vector2D( - 1.0, 1.0 );
                }
                else
                {   side = 1;      // It is on the W side.
                    edge = new Vector2D( 0.0, - world.getHeight() + 2*rad);
                    pnm1 = new Vector2D( rad, - edge.y + rad );
                }
            }
        }
        else
        {
            if( x >= world.getWidth()-rad )
            {
                if( y <= rad )
                {
                    corner = 2;  // It is at the NE corner.
                    pnm1 = new Vector2D( world.getWidth() - rad, rad );
                    edge = new Vector2D( 1.0, - 1.0 );
                }
                else
                {
                    if( y >= world.getHeight() - rad ) 
                    {
                        corner = 3;  // It is at the SE corner.
                        pnm1 = new Vector2D( world.getWidth() - rad, world.getHeight() - rad );
                        edge = new Vector2D( 1.0, 1.0 );
                    }
                    else
                    { 
                        side = 3;      // It is on the E side.
                        edge = new Vector2D( 0.0, world.getHeight() - 2*rad);
                        pnm1 = new Vector2D( world.getWidth() - rad, rad );
                    }
                }
            }
            else   // Now we have a legal value for x.
            {
                if( y <= rad )
                {
                    side = 2;   // It is on the N side.
                    edge = new Vector2D( world.getWidth()- 2*rad, 0.0 );
                    pnm1 = new Vector2D( rad, rad );
                }
                if( y >= world.getHeight() - rad )
                {
                    side = 4;     // It is on the S side.
                    edge = new Vector2D( - world.getWidth() + 2*rad, 0.0 );
                    pnm1 = new Vector2D( world.getWidth() - rad, world.getHeight() - rad );
                }
            }
        }
        if( side == 0 && corner == 0 )  return new Vector2D( x, y );
//
        if( side != 0) // Then we are on a side.
        {
            double magEdge = edge.magnitude();
            dirXEdge = dir.x*edge.y - dir.y*edge.x;  // As long as (xc,yc) is inside the world and (x,y) is outside, dirXEdge can never vanish.
            Vector2D Q = new Vector2D( pnm1.x-xc, pnm1.y - yc );  // Vector from (xc,yc) to the n-1 corner.
            double dirXQ = dir.x*Q.y- dir.y*Q.x;
            double edgeXQ = edge.x*Q.y- edge.y*Q.x;
            alpha = - edgeXQ / dirXEdge;   // This is the fractional part of dist, along dir, from (xc,yc) to the wall.
            beta = - dirXQ / dirXEdge;    // This is the fractional part of edge, along edge from pnm1 to the the intersection with dir.
            dL = ( 1.0 - alpha )*dist;  // This is the remaining distance to travel.
            double xx = (dir.x*edge.x + dir.y*edge.y) / dist / Math.pow( magEdge, 2 );
            // The following takes the direction to be according to the component of dir parallel to edge.
            Vector2D newdir = new Vector2D( dL*xx*edge.x, dL*xx*edge.y );  // This is the new direction and distance remaining to go.
            // If xx > 0, then he is headed for corner #n, if otherwise, he is headed for corner #(n-1).
            if( xx > 0 )
            {  // Here he is headed toward P_n.
                if( 1.0 - beta - dL/magEdge > 0.0 )
                {  // We move him the additional distance.
                    newX = pnm1.x + beta*edge.x + dL*edge.x / magEdge;
                    newY = pnm1.y + beta*edge.y + dL*edge.y / magEdge;
                }
                else
                { // We move him as far as we can, along that side.
                    newX = pnm1.x + edge.x / magEdge;
                    newY = pnm1.y + edge.y / magEdge;
                }
                return new Vector2D( newX, newY );
            }
            else  // Here he is headed to P_{n-1}.
            {
                if( beta - dL/magEdge > 0.0 )
                {  // We move him the additional distance.
                    newX = pnm1.x + beta*edge.x - dL*edge.x / magEdge;
                    newY = pnm1.y + beta*edge.y - dL*edge.y / magEdge;
                }
                else
                { // We move him as far as we can, along that side.
                    newX = pnm1.x;
                    newY = pnm1.y;
                }
                return new Vector2D( newX, newY );
            }
 	    }
        else
        {      // So he must be on a corner. We have the position of the corner and its orientation.
            if( dir.x*edge.x > dir.y*edge.y )
            // Now he needs to run in the y-direction.
            {
                newX = pnm1.x;
                newY = - dist*edge.y;
            }
            else
               // Now he needs to run in the x-direction.
            {
                newY = pnm1.y;
                newX = -dist*edge.x;
            }
            return new Vector2D( newX, newY );
        }
    }

	/**
	 * Get all individuals within a certain distance of a point.
	 *
	 * @param point The Vector2D represntation of a location.
	 * @param distance The distance of which the returned individuals are within.
	 * @return Bag The collection of indiviudals within the requested distance.
	 **/	
	public Bag getIndividualsWithinDistance( Vector2D point, double distance )
	{
		Bag selectedInds = new Bag();
		Bag individuals = CrowdSimulation.getInstance().getWorld().getAllObjects();
		for( int i = 0; i < individuals.size(); i++ )
		{
			Individual ind = (Individual)individuals.get(i);
			if( !ind.equals(this) )
			{

				double r = point.distance( ind.getLocation() );

				if( r-ind.getRadius() < distance )
				{
					selectedInds.add(ind);
				}
			}
		}
		
		return selectedInds;
	}
	

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Way to always get the current instance of the CrowdSimulation object.
	 * Only one instance of this class should ever be created. 
	 * Implementation on the SINGLETON pattern.
	 * 
	 * @return The CrowdSimulation object for the current simulation.
	 **/
	public static CrowdSimulation getInstance()
	{
		return instance;
	}
	
	/**
	 * Gets the primary action controller used for moving all other individuals.
	 *
	 * @return The primary action controller used for moving individuals.
	 **/
	public ActionController getPrimaryActionController()
	{
		return primaryActionController;
	}
	
	/**
	 * Sets the primary action controller used for moving all other individuals.
	 *
	 * @param val The primary action controller used for moving individuals.
	 **/
	public void setPrimaryActionController( ActionController val )
	{
		primaryActionController = val;
	}

	/**
	 * Gets the current time in the simulation.
	 *
	 * @return The current time in the simulation.
	 **/
	public double getSimTime()
	{
		return simTime;
	}
	
	/**
	 * Sets the current time in the simulation.
	 *
	 * @param val The current time in the simulation.
	 **/
	public void setSimTime( double val )
	{
		simTime = val;
	}

	/**
	 * Gets the current time step in the simulation.
	 *
	 * @return The current time step in the simulation.
	 **/
	public double getDeltaT()
	{
		return deltaT;
	}
	
	/**
	 * Sets the current time step in the simulation.
	 *
	 * @param val The current time step  in the simulation.
	 **/
	public void setDeltaT( double val )
	{
		deltaT = val;
	}
	
	/**
	 * Gets the start time for the simulation.
	 *
	 * @return The start time for the simulation.
	 **/
	public Date getStartTime()
	{
		return startTime;
	}
	
	/**
	 * Sets the start time for the simulation.
	 *
	 * @param val The start time for the simulation.
	 **/
	public void setStartTime( Date val )
	{
		startTime = val;
	}
	
	/**
	 * Gets the world for the simulation.
	 *
	 * @return The world for the simulation.
	 **/
	public Continuous2D getWorld()
	{
		return world;
	}
	
	/**
	 * Sets the world for the simulation.
	 *
	 * @param val The world for the simulation.
	 **/
	public void setWorld( Continuous2D val )
	{
		world = val;
	}
	
	/**
	 * Gets the terrain for the simulation.
	 *
	 * @return The terrain for the simulation.
	 **/
	public Continuous2D getTerrain()
	{
		return terrain;
	}
	
	/**
	 * Sets the terrain for the simulation.
	 *
	 * @param val The terrain for the simulation.
	 **/
	public void setTerrain( Continuous2D val )
	{
		terrain = val;
	}
	
	/**
	 * Gets the worldInfo for the simulation.
	 *
	 * @return The worldInfo for the simulation.
	 **/
	public Continuous2D getWorldInfo()
	{
		return worldInfo;
	}
	
	/**
	 * Sets the worldInfo for the simulation.
	 *
	 * @param val The worldInfo for the simulation.
	 **/
	public void setWorldInfo( Continuous2D val )
	{
		worldInfo = val;
	}
	
	/**
	 * Gets the discretization of the world for the simulation.
	 *
	 * @return The discretization of the world for the simulation.
	 **/
	public double getDiscretization()
	{
		return discretization;
	}
	
	/**
	 * Sets the discretization of the world for the simulation.
	 *
	 * @param val The discretization of the world for the simulation.
	 **/
	public void setDiscretization( double val )
	{
		discretization = val;
	}
	
	
	/**
	 * Gets the visualScale for the simulation.
	 *
	 * @return The visualScale of the simulation.
	 **/
	public double getVisualScale()
	{
		return visualScale;
	}
	
	/**
	 * Sets the visualScale for the simulation.
	 *
	 * @param val The visualScale the simulation.
	 **/
	public void setVisualScale( double val )
	{
		visualScale = val;
	}
	
	/**
	 * Gets the UI component for the simulation.
	 *
	 * @return The UI component of the simulation.
	 **/
	public CrowdSimulationWithUI getUI()
	{
		return ui;
	}
	
	/**
	 * Sets the UI component for the simulation.
	 *
	 * @param val The UI component of the simulation.
	 **/
	public void setUI( CrowdSimulationWithUI val )
	{
		this.ui = val;
		if(appConfig == null && configFileName != null) {
			appConfig = new AppConfig( configFileName, this );
			appConfig.initializeCrowdSimulation();
		}
	}
	
	/**
	 * Gets if the movie should be captured for the simulation.
	 *
	 * @return The if the movie should be captured.
	 **/
	public boolean getCaptureMovie()
	{
		return captureMovie;
	}
	
	/**
	 * Sets if the movie should be captured for the simulation.
	 *
	 * @param val The value representing if the movie should be captured.
	 **/
	public void setCaptureMovie( boolean val )
	{
		this.captureMovie = val;
	}

	/**
	 * Gets the frameRate for the movie of the simulation.
	 *
	 * @return The frameRate for the movie of the simulation.
	 **/
	public int getFrameRate()
	{
		return frameRate;
	}
	
	/**
	 * Sets frameRate for the movie of the simulation.
	 *
	 * @param val The frameRate for the movie of the simulation.
	 **/
	public void setFrameRate( int val )
	{
		this.frameRate = val;
	}
	
	/**
	 * Gets the Type of Movie for the movie of the simulation.
	 *
	 * @return The Type of Movie for the movie of the simulation.
	 **/
	public String getMovieType()
	{
		return movieType;
	}
	
	/**
	 * Sets the Type of Movie for the movie of the simulation.
	 *
	 * @param val The Type of Movie for the movie of the simulation.
	 **/
	public void setMovieType( String val )
	{
		this.movieType = val;
	}
	
	/**
	 * Gets the Name of Movie for the movie of the simulation.
	 *
	 * @return The Name of Movie for the movie of the simulation.
	 **/
	public String getMovieName()
	{
		return movieName;
	}
	
	/**
	 * Sets the Name of Movie for the movie of the simulation.
	 *
	 * @param val The Name of Movie for the movie of the simulation.
	 **/
	public void setMovieName( String val )
	{
		this.movieName = val;
	}
	
	/**
	 * Gets the duration of the simulation.
	 *
	 * @return The duration of the simulation.
	 **/
	public double getDuration()
	{
		return duration;
	}
	
	/**
	 * Sets the Duration of the simulation.
	 *
	 * @param val The duration of the simulation.
	 **/
	public void setDuration( double val )
	{
		this.duration = val;
	}
	
	/**
	 * Gets the termination state of the simulation.
	 *
	 * @return The termination state of the simulation.
	 **/
	public boolean getTerminate()
	{
		return terminate;
	}
	
	/**
	 * Sets the termination state of the simulation.
	 *
	 * @param val The termination state of the simulation.
	 **/
	public void setTerminate( boolean val )
	{
		this.terminate = val;
	}

	/**
	 * Gets the current Number of Steps executed for the simulation.
	 *
	 * @return The number of steps which have been executed.
	 **/
	public int getStepNum()
	{
		return stepNum;
	}
	
	/**
	 * Sets the current Number of Steps executed for the simulation.
	 *
	 * @param val The number of steps which have been executed.
	 **/
	public void setStepNum( int val )
	{
		this.stepNum = val;
	}
	
	/**
	 * Gets the collection of loggers used in the simulation.
	 *
	 * @return The loggers used for the simulation.
	 **/
	public Bag getLoggers()
	{
		return loggers;
	}
	
	/**
	 * Adds a loggers to the collection used in the simulation.
	 *
	 * @param val loggers used for the simulation.
	 **/
	public void addLogger( Logger val )
	{
		loggers.add( val );
	}
	
	/**
	 * Sets the collection of paths used in the simulation.
	 *
	 * @param val The paths used for the simulation.
	 **/
	public void setPaths( HashMap val )
	{
		paths = val;
	}

	/**
	 * Gets the collection of paths used in the simulation.
	 *
	 * @return The paths used for the simulation.
	 **/
	public HashMap getPaths()
	{
		return paths;
	}
	
	/**
	 * Gets the collection of portrayals used in the simulation.
	 *
	 * @return The portrayals used for the simulation.
	 **/
	public HashMap getPortrayals()
	{
		return portrayals;
	}
	
	/**
	 * Sets the collection of portrayals used in the simulation.
	 *
	 * @param val The portrayals used for the simulation.
	 **/
	public void setPortrayals( HashMap val )
	{
		portrayals = val;
	}
	
	/**
	 * Adds a portrayals to be used in the simulation.
	 *
	 * @param portrayal The portrayal to be added in the simulation.
	 * @param name The name associated with the portrayal.
	 **/
	public void addPortrayal( FieldPortrayal2D portrayal, String name )
	{
		portrayals.put( name, portrayal );
	}
	
	//ALAN JOLLY Simlog
	public SimLog getUpdateStats()
	{
		return this.updateStats;
	}
}
