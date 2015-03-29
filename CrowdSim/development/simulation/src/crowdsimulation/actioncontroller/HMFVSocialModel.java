/*
 * $RCSfile: HMFVSocialModel.java,v $ $Date: 2008/03/17 19:10:19 $
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
 * This model is based around on the Social elements of the HMFV model.
 * This only has the long distance forces and does not deal with the physical 
 * contact forces in from the HMFV model.
 *
 * @see crowdsimulation.actioncontroller.ActionController
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:19 $
 **/
public class HMFVSocialModel extends SocialPotentialModel
{

////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The names of all the forces. **/
	public static String  					
		forceNames = "avoidInds.x,avoidInds.y,prefVel.x,prefVel.y,avoidObstacle.x,avoidObstacle.y,attractors.x,attractors.y,randomness.x,randomness.y,momentum.x,momementum.y";
	/** Default values for all forces 0's. **/
	public static String  					
		forceValues = "0,0,0,0,0,0,0,0";
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs the HMFVSocialModel to be used for a set of individuals.
	 **/
	public HMFVSocialModel() 
	{
		super();
	}

	/**
	 * Creates an instance of an HMFVSocialPotentialStrategy.
	 * The strategy is populated with the same parameters that this model was constructed with.
	 *
	 * @return ActionStrategy A new instance of an HMFVSocialPotentialStrategy
	 * @see crowdsimulation.actioncontroller.strategy.SocialPotentialStrategy
	 **/
	public ActionStrategy getNewStrategy()
	{
		return new HMFVSocialStrategy( params );
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
