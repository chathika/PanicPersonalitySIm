/*
 * $RCSfile: HMFVRayTracerModel.java,v $ $Date: 2008/03/17 19:10:19 $
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
 * ActionController based on the HMFV model and uses ray tracing to sample the environement.
 * The forces are the same as the standard HMFV model, but instead of using a single point
 * on each entity to calculate the forces the environment is sampled and the forces are calculated 
 * from all the intersection points which where found.
 *
 * @see crowdsimulation.actioncontroller.ActionController
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:19 $
 **/
public class HMFVRayTracerModel extends SocialPotentialWithRayTraceVisionModel
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
	
	/** precision at potential forces ( 0.0 < C_NS < 1.0 ) **/
	double cNS = 0.95;
	/** limit for the change of the relative velocity during one iteration **/
   	double velocityChangeLimit = 0.01;	

	/** The names of all the forces. **/
	public static String forceNames = 	"social.x,social.y,"+
										"tangential.x,tangential.y,"+
										"normal.x,normal.y,"+
										"pp_GroupForce.x,pp_GroupForce.y,"+
										"selfPropellingForce.x,selfPropellingForce.y,"+
										"randomForce.x,randomForce.y";
										
	/** Default values for all forces 0's. **/
	public static String forceValues = 	"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0";

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs a HMFVRayTracerModel to be used for a set of individuals.
	 **/		
	public HMFVRayTracerModel( ) 
	{
		super();
	}

	/**
	 * Creates an instance of an HMFVRayTracer strategy.
	 * The strategy is populated with the same parameters that this model was constructed with.
	 *
	 * @return ActionStrategy A new instance of a HMFVRayTracerStrategy
	 * @see crowdsimulation.actioncontroller.strategy.HMFVRayTracerStrategy
	 **/
	public ActionStrategy getNewStrategy()
	{
		return new HMFVRayTracerStrategy( params );
	}
	
	/**
	 * Adjust the timestep based on the force limits and the current time step.
	 *
	 * @param tstep The current time step of the simulaiton.
	 * @param f The current forces acting on the individual.
	 * @return The new timestep.
	 **/
	public double eulTStep( double tstep, double f )
	{
	 	/* adjusts the time step in a way that the force (fx,fy) doesn't
	     change the velocity of particle i by more than V_ChangeLimit */
	  
	  	while( f*(tstep) >= velocityChangeLimit ){ tstep *= cNS; }
	  	
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

			((HMFVRayTracerStrategy)ind.getActionStrategy()).calculateMovementParametersII( simState );
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
