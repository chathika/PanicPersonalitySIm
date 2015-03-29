/*
 * $RCSfile: SocialPotentialWithRayTraceVisionModel.java,v $ $Date: 2008/03/17 19:10:19 $
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
 * Base class to controll entities with using ray trace vision.
 * This class contains the attributes and methods which are common to Action Models
 * which will use ray traced vision techniques to view the environment.
 * This is object which is called at each step in the simulation.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:19 $
 **/
public class SocialPotentialWithRayTraceVisionModel extends SocialPotentialModel
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The names of all the forces. **/
	public static String
		forceNames = "cohesion.x,cohesion.y,avoidance.x,avoidance.y,consistency.x,consistency.y,randomness.x,randomness.y,features.x,features.y"; 
	/** Default values for all forces 0's. **/
	public static String
		forceValues = "0,0,0,0,0,0,0,0,0,0";

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Default constructor for a social potential model using ray traced vision.
	 **/
	public SocialPotentialWithRayTraceVisionModel()
	{
		super();
	}


	/**
	 * Constructs a social potential model using ray traced vision ands ets the attributes for the Action Controller.
	 *
	 * @param parameters The parameters to use when setting the parameters in the Action Controller.
	 **/
	public SocialPotentialWithRayTraceVisionModel( Map parameters  ) 
	{
		this();
		setParameters( parameters );
	}


	/**
	 * Creates a new instance of the ActionStrategy which is to be used with this Model.
	 * This is an empty, default, Action Strategy.
	 *
	 * @return This is a new ActionStrategy which has been uninitialized.
	 **/
	public ActionStrategy getNewStrategy()
	{
		return new SocialPotentialWithRayTraceVisionStrategy();
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
