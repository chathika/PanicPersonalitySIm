/*
 * $RCSfile: ObstacleStrategy.java,v $ $Date: 2008/03/17 19:10:19 $
 */
package crowdsimulation.actioncontroller.strategy;

import java.util.*;
import crowdsimulation.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*; 			// used for logging
import math.*;								// used for Vector2D
import ec.util.*;
import sim.field.continuous.*;
import sim.util.*;

/**
 * This is the strategy for the obstacle model. 
 * This is the default strategy for an obstacle which actually does nothing.
 *
 * @see crowdsimulation.actioncontroller.strategy.ActionStrategy
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:19 $
 **/
public class ObstacleStrategy extends ActionStrategy 
{		

////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * The default constructor for a Obstacle Strategy.
	 * Currently there is no movement associated with obstacles.
	 **/
	public ObstacleStrategy() 
	{
	}
	
	
	/**
	 * The constructor for a Obstacle Strategy with a set of parameters.
	 * Currently there is no movement associated with obstacles.
	 *
	 * @param attributes Map of attributes to be set in the strategy.
	 **/
	public ObstacleStrategy( Map attributes ) 
	{
		this();
		this.setParameters( attributes );
	}
	
	
	/**
	 * This function calculates all of the forces acting on the Salamander.
	 *
	 * @param simState The object containing the state of the simulation.
	 **/
	public void calculateMovementParameters( CrowdSimulation simState )
	{
	}


	/**
	 * Moves the individuals using the passed in time step.
	 *
	 * @param deltaT the timestep that has been set for use for this step.
	 **/
	public void move( double deltaT )
	{
	}

	
	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
		super.setParameters( parameters );
	}


////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

}
