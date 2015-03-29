/*
 * $RCSfile: SocialPotentialWithRayTraceVisionStrategy.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.actioncontroller.strategy;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.strategy.raytracemethod.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.logging.*;
import crowdsimulation.util.*;
import java.util.*;
import sim.util.*;
import math.*;

/**
 * This is the base class for the SocialPotential Strategies that use Ray Tracers to "see" the environment.
 *
 * @author $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class SocialPotentialWithRayTraceVisionStrategy extends SocialPotentialStrategy
{	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** 
	 * The Ray Tracer which will be used to sample the environment.
	 * There are many different subclasses and can be set via the 
	 * name of the appropriate Ray Tracer to use in the config file.
	 **/
	RayTraceMethod rayTracer;
	/** The collection of all Points of Interest found from sampling the environment. **/
	Bag pointsOfInterest;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * The Default Constructor for the Social Potential Strategy With Ray Tracing.
	 **/
	public SocialPotentialWithRayTraceVisionStrategy()
	{
		super();
		rayTracer = new AxialRayTraceMethod();
	}
	
	
	/**
	 * Constructor for a Social Potential Strategy With Ray Tracing where the data is set by attribute set passed in.
	 *
	 * @param attributes Map of attributes to be set in the strategy.
	 **/
	public SocialPotentialWithRayTraceVisionStrategy( Map attributes )
	{
		rayTracer = new AxialRayTraceMethod( attributes );
	}
	
	
	/**
	 * This calculates the points that are needed to calculate the movement parameters.
	 *
	 * @param simState The current State of the simulation.
	 **/
	public void calculateMovementParameters( CrowdSimulation simState )
	{
		super.calculateMovementParameters( simState );
		
		pointsOfInterest = getPointsOfInterest();
	}


	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
		super.setParameters( parameters );
		
		if( parameters.containsKey( "rayTraceMethod" ) )
		{
			Object value = parameters.get( "rayTraceMethod" );
			
			setRayTracer( (RayTraceMethod)Parameters.convertToObject( 
				parameters.get( "rayTraceMethod" ), 
				new CentroidWithAxialRayTraceMethod( parameters ), 
				"rayTraceMethod for the strategy must be a the name of a Ray Tace Method." ) );
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * This is where the environment is sampled to find all points of interest for movement.
	 * 
	 * @return The bag of all the PointsOfInterest found.
	 **/
	public Bag getPointsOfInterest()
	{
		return rayTracer.getPointsOfInterest( ind, ind.getInteractingEntities() );
	}

	/**
	 * Gets the rayTracer used by the class.
	 *
	 * @return The rayTracer used by the class.
	 **/
	public RayTraceMethod getRayTracer()
	{
		return rayTracer;
	}
	
	/**
	 * Sets the rayTracer to be used by the class.
	 *
	 * @param val The rayTracer to be used by the class.
	 **/
	public void setRayTracer( RayTraceMethod val )
	{
		rayTracer = val;
	}
}
