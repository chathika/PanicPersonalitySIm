/*
 * $RCSfile: HMFVModel.java,v $ $Date: 2008/03/17 19:10:19 $
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
 * ActionController based on the Helbing Molnar Farkas and Vicsek (HMFV) model of crowd movement.
 * This is a model where there are 3 primary forces
 *  1. Person to person/obstacle social forces
 *  2. Person to person/obstacle tangential (frictional) forces
 *  3. Person to person/obstacle normal (pushing) forces
 * There is still a random force and a self propelling force.
 * The self propelling force makes an individual move towards the exit. 
 *
 * @see crowdsimulation.actioncontroller.ActionController
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:19 $
 **/
public class HMFVModel extends SocialPotentialModel
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The precision of potential forces ( 0.0 < C_NS < 1.0 ) **/
	double cNS = 0.95;
	/** Limit for the change of the relative velocity during one iteration **/
   	double velocityChangeLimit = 0.01;	

	/** The names of all the forces. **/
	public static String forceNames = 	"wallPsychRelationForce.x,wallPsychRelationForce.y,"+
										"wallYoungForce.x,wallYoungForce.y,"+
										"wallTangForce.x,wallTangForce.y,"+
										"pp_PsychForce.x,pp_PsychForce.y,"+
										"pp_YoungForce.x,pp_YoungForce.y,"+
										"pp_TangForce.x,pp_TangForce.y,"+
										"pp_GroupForce.x,pp_GroupForce.y,"+
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
										"0,0,"+
										"0,0,"+
										"0,0";

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Default constructor for the HMFV model.
	 **/		
	public HMFVModel( ) 
	{
		super();
	}


	/**
	 * Creates an instance of an HMFV strategy.
	 * The strategy is populated with the same parameters that this model was constructed with.
	 *
	 * @return ActionStrategy A new instance of a HMFVStrategy
	 * @see crowdsimulation.actioncontroller.strategy.HMFVStrategy
	 **/
	public ActionStrategy getNewStrategy()
	{
		return new HMFVStrategy( params );
	}

	
	/**
	 * This adjusts the timestep based on the strength of the forces passed in.
	 *
	 * @param f The strength of the forces.
	 * @param tstep The timestep which is currently being suggested to use.
	 * @return The new timestep.
	 **/
	public double eulTStep( double tstep, double f )
	{
	 	/* adjusts the time step in a way that the force (fx,fy) doesn't
	     change the velocity of particle i by more than V_ChangeLimit */
	  
	  	while( f*(tstep) >= velocityChangeLimit )
	  	{ 
	  		tstep *= cNS; 
	  	}
	  	
	  	return tstep;
	}


	/**
	 * This is where movement parameters are calculated, so the strength of the forces and new timestep should be calcualted.
	 * This is the first of the three phases which happen at every step of the simulation.
	 *
	 * @param state The object containing the simulation state information.
	 **/
    public void preStep( CrowdSimulation state )
	{
	  	setTimeStep( defaultDeltaT );

        final CrowdSimulation simState = (CrowdSimulation)state;

        for( int i =0; i < individuals.numObjs; i++ )
        {
        	Individual ind = (Individual)individuals.get( i );
        	ind.getActionStrategy().calculateMovementParameters( simState );
		}

  		for( int i = 0 ; i < individuals.numObjs; i++ ) 
  		{ 
		    Individual ind = (Individual)individuals.get( i );

			((HMFVStrategy)ind.getActionStrategy()).calculateMovementParametersII( simState );
		}
	}


	/**
	 * This is where the model should tell each of the individuals to move.
	 * This is the second of the three phases which happen at every step of the simulation.
	 * Each individual is moved acording to the calculated movement and there speed.
	 * This is done using Euler Integration and an adaptive step size.
	 *
	 * @param state The object containing the simulation state information.
	 **/
    public void step( SimState state )
	{
        final CrowdSimulation simState = (CrowdSimulation)state;
		
		for( int i = 0 ; i < individuals.numObjs; i++ ) 
  		{ 
  			Individual ind = (Individual)individuals.get(i);
  			
			ind.getActionStrategy().move( getTimeStep() );
		}
    }

	
	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
		super.setParameters( parameters );
			
		if( parameters.containsKey( "velocityChangeLimit" ) )
		{
			setVelocityChangeLimit( Parameters.convertToDouble( 
				parameters.get( "velocityChangeLimit" ), 
				getVelocityChangeLimit(),
				" VelocityChangeLimit for HMFVModel construction must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "cNS" ) )
		{
			setCNS( Parameters.convertToDouble( 
				parameters.get( "cNS" ), 
				getCNS(),
				" CNS for HMFVModel construction must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "defaultDeltaT" ) )
		{
			setDefaultDeltaT( Parameters.convertToDouble( 
				parameters.get( "defaultDeltaT" ), 
				getDefaultDeltaT(),
				" DefaultDeltaT for HMFVModel construction must be a Double or a string representing a Double." ) );
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

	/**
	 * Gets the allowed limit that the velocity is allowed to change during a single timestep.
	 *
	 * @return The allowed limit.
	 **/
    public double getVelocityChangeLimit()
	{
		return velocityChangeLimit;
	}
	
	/**
	 * Sets the allowed limit that the velocity is allowed to change during a single timestep.
	 *
	 * @param val The allowed limit.
	 **/
	public void setVelocityChangeLimit( double val )
	{
		velocityChangeLimit = val;
	}

	/**
	 * Gets the presision of the social forces (cNS).
	 *
	 * @return The cNS value for the model.
	 **/
    public double getCNS()
	{
		return cNS;
	}
	
	/**
	 * Sets the presision of the social forces (cNS).
	 *
	 * @param val The cNS value for the model.
	 **/
	public void setCNS( double val )
	{
		cNS = val;
	}

}
