/*
 * $RCSfile: ModifiedHMFVModel.java,v $ $Date: 2008/03/17 19:10:19 $
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

//TODO: What is this file?
/**
 * ActionController based on the Helbing Molnar Farkas and Vicsek model of crowd movement.
 * This model contains modification which include age based parameters to the basic HMFV model.
 *
 * @see crowdsimulation.actioncontroller.ActionController
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:19 $
 **/
public class ModifiedHMFVModel extends HMFVModel
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
	public ModifiedHMFVModel( ) 
	{
		super();
	}

	/**
	 * Creates an instance of a Modified HMFV strategy.
	 * The strategy is populated with the same parameters that this model was constructed with.
	 *
	 * @return ActionStrategy A new instance of a ModifiedHMFVStrategy
	 * @see crowdsimulation.actioncontroller.strategy.ModifiedHMFVStrategy
	 **/	
	public ActionStrategy getNewStrategy()
	{
		return new ModifiedHMFVStrategy( getParameters() );
	}
	
}
