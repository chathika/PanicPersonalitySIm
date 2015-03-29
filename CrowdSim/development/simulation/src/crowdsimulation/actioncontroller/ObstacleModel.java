/*
 * $RCSfile: ObstacleModel.java,v $ $Date: 2008/03/17 19:10:19 $
 */
package crowdsimulation.actioncontroller;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.strategy.*;
import crowdsimulation.entities.*;
import ec.util.*;
import java.util.*;
import math.*;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;


/**
 * This model is the default model for an obstacle which actually does nothing.
 *
 * @see crowdsimulation.actioncontroller.ActionController
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:19 $
 **/
public class ObstacleModel extends ActionController
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs the Obstacle Model to be used for a set of obstacle.
	 **/
	public ObstacleModel() 
	{
		super();
	}

	/**
	 * Creates an instance of an obstacle strategy.
	 *
	 * @return ActionStrategy A new instance of a ObstacleStrategy
	 * @see crowdsimulation.actioncontroller.strategy.ObstacleStrategy
	 **/	
	public ActionStrategy getNewStrategy()
	{
		return new ObstacleStrategy();
	}

	/**
	 * This is the method called at each step of the simulation.
	 * Movement is calculated for each individual associated with this ActionController
	 *   and the 5 forces are calculated, summed then integrated to find the velocities
	 *   of each individual. Then each individual is moved acording to the calculated movement 
	 *   and there jump value.
	 *
	 * @param state The object representing the current state of the simulation.
	 **/
    public void step( SimState state )
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
