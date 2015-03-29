/*
 * $RCSfile: SocialPotentialModel.java,v $ $Date: 2009/08/18 19:55:41 $
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
 * Base class for classes which control the movement of individuals or obstacles via Social Potential Techniques.
 * This class contains the attributes and methods which are common to all Social Potential Action Models.
 * This is object which is called at each step in the simulation.
 *
 * @author $Author: ajolly $
 * @version $Revision: 1.2 $
 * $State: Exp $
 * $Date: 2009/08/18 19:55:41 $
 **/
public abstract class SocialPotentialModel extends ActionController
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** A collection of all individuals controlled by the ActionController. **/
	//protected Bag individuals = new Bag();
	/** A collection of all obstacles controlled by the ActionController. **/
	//protected Bag obstacles = new Bag();
	
	/** The default time step for the model. **/
	double defaultDeltaT = 0.1;
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Default Constructor for generic SocialPotential Action Controllers.
	 * This is needed for some of the reflection techniques or 
	 * else this would be an abstract class.
	 **/	
	public SocialPotentialModel()
	{
	}

	
	/**
	 * Cosntructs a social potential model and sets the attributes for the SocialPotential Action Controller.
	 *
	 * @param parameters The parameters to use to initialize the Controller.
	 **/
	public SocialPotentialModel( Map parameters ) 
	{
		this();
		setParameters( parameters );
	}


	/**
	 * Loops through the Individuals and calculates their movement parameters.
	 *
	 * @param simState This is the current state of the simulation.
	 **/
	public void calculateMovementParameters( CrowdSimulation simState )
	{
		// loop overal all individuals        
        for( int i =0; i < individuals.numObjs; i++ )
        {
        	Individual ind = (Individual)individuals.get( i );
        	Vector2D loc = new Vector2D( simState.getWorld().getObjectLocation( ind ) );
        	
        	ActionStrategy strategy = ind.getActionStrategy();
        	
        	Bag individuals = simState.getWorld().getObjectsWithinDistance( loc.getDouble2D(), strategy.getInteractionRadius(), true );
        	ind.setInteractingIndividuals( individuals );
        	Bag obstacles = simState.getTerrain().getAllObjects();
			ind.setInteractingObstacles( obstacles );
			
           	// Calculate movement parameters and time steps for individuals.
       		strategy.calculateMovementParameters( simState );
        }		
	}
	
	
	/**
	 * Moves the individuals using the passed in time step.
	 * This tells all the individuals contained in this model to move acording to the timestep passed in.
	 * @Override overrides ActionController
	 * @param currentTimeStep The current timestep to be used for the simulations.
	 **/
	public void move( double currentTimeStep )
	{
		// loop overal all individuals        
        for( int i =0; i < individuals.numObjs; i++ )
        {
        	Individual ind = (Individual)individuals.get( i );
        	if( !ind.isDead() )
	        {
        		ind.getActionStrategy().move( currentTimeStep );
        	}	
		}	
	}

	/**
	 * This is where any cleanup or after move opertaions should take place.
	 * Currently there are none.
	 * @Override overrides ActionController
	 * @param state This is the current state of the simulation.
	 **/
	public void postStep( CrowdSimulation state )
	{

	}
	/**
	 * Squares the value passed in.
	 * This is a shortcut to Math.pow( x, 2 ).
	 *
	 * @param a1 the value to be squared.
	 * @return The square of the input value. return = a1*a1.
	 **/
	public double SQR( double a1 )
	{
		return Math.pow( a1, 2 );
	}

	
	/**
	 * Sets all the parameters from a key value pairs collection.
	 * @Override ActionController
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
		params = parameters;
		
		if( parameters.containsKey( "timeStep" ) )
		{
			setTimeStep( Parameters.convertToDouble( 
				parameters.get( "timeStep" ), 
				getTimeStep(),
				" The timestep for an Action Controller must be a Double or a string representing a Double." ) );
		}		
	}
	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the names of all forces used for this model as a comma seperated list.
	 *
	 * @return String The string on the force names. 
	 **/
	public abstract String getForceNames();
	
	/**
	 * This gets a string of zero's for the forces used in this model seperated by commas.
	 *
	 * @return String The string of zeros for each force.
	 **/
	public abstract String getForceValues();
    
    /**
     * This gets the default time step which is used by this model.
     * This is the timestep which is suggested as long as nothing done by the entities 
     * being contained in this model reduce it for some reason.
     *
     * @return The default time step which should be used for this model.
	 **/
    public double getDefaultDeltaT()
	{
		return defaultDeltaT;
	}
	
	/**
     * This sets the default time step which is used by this model.
     * This is the timestep which is suggested as long as nothing done by the entities 
     * being contained in this model reduce it for some reason.
     *
     * @param val The default time step which should be used for this model.
	 **/
	public void setDefaultDeltaT( double val )
	{
		defaultDeltaT = val;
	}
	
}
