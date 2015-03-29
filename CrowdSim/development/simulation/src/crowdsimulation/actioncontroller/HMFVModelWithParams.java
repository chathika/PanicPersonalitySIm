/*
 * $RCSfile: HMFVModelWithParams.java,v $ $Date: 2008/03/17 19:10:19 $
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
 * ActionController based on the Helbing Molnar Farkas and Vicsek model of crowd movement but include parameters for age.
 * The forces are the same as the standard HMFV model, but the parameters of some of the forces are adjusted 
 * based on the age of the individual.
 *
 * @see crowdsimulation.actioncontroller.ActionController
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:19 $
 **/
public class HMFVModelWithParams extends HMFVModel
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
	
	/** The names of all the forces. **/
	public static String forceNames = 	"wallPsychRelationForce.x,wallPsychRelationForce.y,"+
										"wallYoungForce.x,wallYoungForce.y,"+
										"wallTangForce.x,wallTangForce.y,"+
										"pp_PsychForce.x,pp_PsychForce.y,"+
										"pp_YoungForce.x,pp_YoungForce.y,"+
										"pp_TangForce.x,pp_TangForce.y,"+
										"selfPropellingForce.x,selfPropellingForce.y,"+
										"randomForce.x,randomForce.y"; 
										
	/** Default values for all forces 0's. **/
	public static String forceValues = 	"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0"; 

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs the HMFV style model to be used for a set of individuals.
	 **/		
	public HMFVModelWithParams( ) 
	{
		super();
	}
	
	/**
	 * Creates an instance of an HMFVWithParamsStrategy.
	 * The strategy is populated with the same parameters that this model was constructed with.
	 *
	 * @return ActionStrategy A new instance of a HMFVWithParamsStrategy
	 * @see crowdsimulation.actioncontroller.strategy.HMFVWithParamsStrategy
	 **/
	public ActionStrategy getNewStrategy()
	{
		return new HMFVWithParamsStrategy( params );
	}

}
