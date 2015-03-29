/*
 * $RCSfile: ActionController.java,v $ $Date: 2009/08/18 19:55:41 $
 */
package crowdsimulation.actioncontroller;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.strategy.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import crowdsimulation.util.*;
import ec.util.*;
import java.util.*;
import math.*;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;

/**
 * Base class for classes which control the movement of individuals or obstacles.
 * This class contains the attributes and methods which are common to all Action Models.
 * This is object which is called at each step in the simulation.
 *
 * @author $Author: ajolly $
 * @version $Revision: 1.2 $
 * $State: Exp $
 * $Date: 2009/08/18 19:55:41 $
 **/
public abstract class ActionController implements Steppable
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** A collection of all individuals controlled by the ActionController. **/
	protected Bag individuals = new Bag();
	/** A collection of all obstacles controlled by the ActionController. **/
	protected Bag obstacles = new Bag();
	/** The set of parameters used to initialize the model.**/
	protected Map params = new HashMap();
	/** The time step for the model. **/
	private double timeStep = 0.1;


////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Default Constructor for generic action Controllers.
	 * This is needed for some of the reflection techniques or 
	 * else this would be an abstract class.
	 **/	
	public ActionController()
	{
	}
	
	
	/**
	 * Sets the attributes for the Action Controller defined in this class.
	 *
	 * @param parameters The parameters to use to initialize the Controller.
	 **/
	public ActionController( Map parameters ) 
	{
		this();
		setParameters( parameters );
	}


	/**
	 * Creates a new instance of the ActionStrategy which is to be used with this Model.
	 * This should be overided by any subclass of this, unless a strategy is not needed.
	 *
	 * @return This is a new ActionStrategy which has been uninitialized.
	 **/
	public abstract ActionStrategy getNewStrategy();


	/**
	 * This is called to calculate what needs to be done for a step.
	 * 
	 * @param state This is the current state of the simulation.
	 **/	
	public void preStep( CrowdSimulation state )
	{
		calculateMovementParameters( state );
	}

	/**
	 * This is the method called at each step of the simulation.
	 * Movement is calculated for each individual(Salamander) associated with this ActionController
	 *   and the 6 forces are calculated, summed then integrat/usr/share/doc/HTML/index.htmled to find the velocities
	 *   of each individual. Then each individual is moved acording to the calculated movement 
	 *   and the current time step.
	 **/
	public void step( SimState state )
	{
		move( getTimeStep() );
	}

	/**
	 * This is where any cleanup or after move opertaions should take place.
	 * Currently there are none.
	 *
	 * @param state This is the current state of the simulation.
	 **/
	public void postStep( CrowdSimulation state )
	{
	}
	
	/**
	 * Loops through the Individuals and calculates their movement parameters.
	 *
	 * @param simState This is the current state of the simulation.
	 **/
	public void calculateMovementParameters( CrowdSimulation simState )
	{
	}
	
	/**
	 * Moves the individuals using the passed in time step.
	 *
	 * @param currentTimeStep The current timestep to be used for hte simulations.
	 **/
	public void move( double currentTimeStep )
	{
	}

	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
		params = parameters;
	}
	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the map of parameters used to initialize the controller.
	 * 
	 * @return The map of the parameters used to initialize this ActionController.
	 **/
	public Map getParameters()
	{
		return params;
	}

	/**
	 * Adds an individual to the action model.
	 * This places the individual in the collection which denotes which individuals are controlled by this action model.
	 *
	 * @param ind The individual to be controlled by this ActionController.
	 **/
	public void addIndividual( Individual ind )
	{
		individuals.add( ind );
	}

	/**
	 * Removes an individual to the action model.
	 * This removes the individual from the collection which denotes which individuals are controlled by this action model.
	 *
	 * @param ind The individual to be controlled by this ActionController.
	 **/
	public void removeIndividual( Individual ind )
	{
		individuals.remove( ind );
		CrowdSimulation.getInstance().getWorld().remove( ind );
	}
	
	/**
	 * Gets the collection of individuals for the actionController.
	 *
	 * @return The individuals controlled by this ActionController.
	 **/
	public Bag getIndividuals()
	{
		return individuals;
	}

	/**
	 * Adds an obstacle to the action model.
	 * This places the obstacle in the collection which denotes which individuals are controlle dby this action model.
	 *
	 * @param obstacle The obstacle to be controlled by this ActionController.
	 **/
    public void addObstacle( Obstacle obstacle )
    {
		obstacles.add( obstacle );
    }

	/**
	 * Gets the collection of obstacles controlled by this actionController.
	 *
	 * @return The individuals controlled by this ActionController.
	 **/
    public Bag getObstacles( )
    {
		return obstacles;
    }


	/**
	 * Adds an entity to the action model.
	 * This places the entity in the collection which denotes which individuals are controlle dby this action model.
	 *
	 * @param entity The obstacle to be controlled by this ActionController.
	 **/
    public void addEntity( Entity entity )
    {
    	if( entity instanceof Individual )
    	{
    		addIndividual( (Individual)entity );
    	}
    	else if( entity instanceof Obstacle )
    	{
    		addObstacle( (Obstacle)entity );
    	}
    }

	/**
	 * Gets the collection of entities controlled by this actionController.
	 *
	 * @return The individuals controlled by this ActionController.
	 **/
    public Bag getEntities( )
    {
    	Bag entities = new Bag();
    	entities.addAll( individuals );
    	entities.addAll( obstacles );
		return entities;
    }
    
    /**
     * Gets the timestep being used for the simulation.
     *
     * @return The timestep that the Action Controller is going to use to move the individuals.
	 **/
    public double getTimeStep()
	{
		return timeStep;
	}
	
	/**
     * Sets the timestep being used for the simulation.
     *
     * @param val The timestep that the Action Controller is going to use to move the individuals.
	 **/
	public void setTimeStep( double val )
	{
		timeStep = val;
	}
	
}
