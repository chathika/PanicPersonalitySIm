/*
 * $RCSfile: GroupingModel.java,v $ $Date: 2008/03/17 19:10:19 $
 */
package crowdsimulation.actioncontroller;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.strategy.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.logging.*;
import crowdsimulation.util.*;
import ec.util.*;
import java.util.*;
import math.*;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;

/**
 * This model is based around the Allegience grouping model created by Kaup and Oleson.
 *
 *  Individuals move according to the vector sum of five contributing vectors:
 *     cohesion:    		Tendency to move to the center of mass of the local group
 *     avoidance:   		Tendency to not collide with others, very large compared to others
 *     consistency: 		Tendency to move in the same direction as the local group
 *     groupingStrength:    Tendancy of individuals to group together passed on a personality parameter
 *     randomness:  		Random variations applied to the above.
 *
 * @see crowdsimulation.actioncontroller.ActionController
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:19 $
 **/
public class GroupingModel extends SocialPotentialModel
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
 
 	/** The names of all the forces. **/
    private static String forceNames = "Cohesion.X,Cohesion.Y,"+
    								   "Avoidance.X,Avoidance.Y,"+
    								   "Consistency.X,Consistency.Y,"+
    								   "Random.X,Random.Y,"+
    								   "Momentum.X,Momentum.Y,"+
    								   "Features.X,Features.Y,"+
    								   "Waypoints.X,Waypoints.Y,"+
    								   "Grouping.X,Grouping.Y";
    /** Default values for all forces 0's. **/
	private static String forceValues = "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs the Alegience Grouping style model to be used for a set of individuals.
	 **/
	public GroupingModel( ) 
	{
		super( );
	}

	/**
	 * Creates an instance of a GroupingStrategy.
	 * The strategy is populated with the same parameters that this model was constructed with.
	 *
	 * @return ActionStrategy A new instance of a GroupingStrategy
	 * @see crowdsimulation.actioncontroller.strategy.GroupingStrategy
	 **/
	public ActionStrategy getNewStrategy()
	{
		return new GroupingStrategy( params );
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
