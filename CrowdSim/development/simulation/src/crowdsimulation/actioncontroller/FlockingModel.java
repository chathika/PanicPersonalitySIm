/*
 * $RCSfile: FlockingModel.java,v $ $Date: 2008/03/17 19:10:19 $
 */
package crowdsimulation.actioncontroller;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.strategy.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import ec.util.*;
import java.util.*;
import math.*;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;

/**
 * This model is based around the BOIDS flocking model created by Craig Reynolds.
 *
 *  Individuals move according to the vector sum of five contributing vectors:
 *     cohesion:    Tendency to move to the center of mass of the local group
 *     avoidance:   Tendency to not collide with others, very large compared to others
 *     consistency: Tendency to move in the same direction as the local group
 *     features:    Affects of repulsive and attractive features in the environment
 *     randomness:  Random variations applied to the above.
 *
 * @see crowdsimulation.actioncontroller.ActionController
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:19 $
 **/
public class FlockingModel extends SocialPotentialModel
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
	/** The names of all the forces. **/
	public static String
		forceNames = "cohesion.x,cohesion.y,"+
			         "avoidance.x,avoidance.y,"+
			         "consistency.x,consistency.y,"+
			         "randomness.x,randomness.y,"+
			         "features.x,features.y"; 
	/** Default values for all forces 0's. **/
	public static String
		forceValues = "0,0,0,0,0,0,0,0,0,0";
		
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs the BOIDS style model to be used for a set of individuals.
	 **/
	public FlockingModel() 
	{
		super();
	}


	/**
	 * Constructs the BOIDS style model to be used for a set of individuals.
	 *
	 * @param parameters The simulation state parameters and vaules to be used during the simulation.
	 **/
	public FlockingModel( Map parameters  ) 
	{
		super( parameters );
		
		setParameters( parameters );
	}

	
	/**
	 * Creates an instance of a FlockingStrategy.
	 * The strategy is populated with the same parameters that this model was constructed with.
	 *
	 * @return ActionStrategy A new instance of a FlockingStrategy
	 * @see crowdsimulation.actioncontroller.strategy.FlockingStrategy
	 **/
	public ActionStrategy getNewStrategy()
	{
		return new FlockingStrategy( params );
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

	/**
	 * Gets the names of all forces used for this model as a comma seperated list.
	 *
	 * @return String The string on the force names. 
	 **/
	public String getForceNames()
	{
		return forceNames;
	}
	
	/**
	 * This gets a string of zero's for the forces used in this model seperated by commas.
	 *
	 * @return String The string of zeros for each force.
	 **/
	public String getForceValues()
	{
		return forceValues;
	}    
	
}
