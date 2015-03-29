/*
 * $RCSfile: AxialRayTraceMethod.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.actioncontroller.strategy.raytracemethod;

import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import java.util.*;
import sim.util.*;
import math.*;

/**
 * This tests in all four Axial Directions to check for entities ot interact with.
 *
 * @author $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class AxialRayTraceMethod extends RayTraceMethod
{	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * The default constructor of an Axial Ray Tracing method.
	 **/
	public AxialRayTraceMethod()
	{
	}
	
	/**
	 * Constructing an Axial Ray Trace method with a given set of parameters.
	 *
	 * @param attributes The attributes used to set the parameters of the Ray Tracer.
	 **/
	public AxialRayTraceMethod( Map attributes )
	{
		setParameters( attributes );
	}


	/**
	 * This is where the environment is sampled to find all points of interest for movement.
	 * The environment is sampled in the positive x, negative x, positive y, 
	 * and negative y directions.
	 * 
	 * @param ind The individual which the sampling is being done for.
	 * @param allEntities The entities which might be hit when sampling.
	 * @return The bag of all the PointsOfInterest found.
	 **/
	public Bag getPointsOfInterest( Individual ind, Bag allEntities )
	{
		Bag pointsOfInterest = new Bag();

		PointOfInterest poi = null;
		for( int i = 0; i < allEntities.size(); i++ )
		{
			Entity entity = (Entity)allEntities.get(i);

			Bag entities = new Bag();
			entities.add( entity );
			
			poi = rayTrace( ind, entities, ind.getCenter(), Entity.xPlusVector );

			PointOfInterest tempPOI = rayTrace( ind, entities, ind.getCenter(), Entity.xMinusVector );
			if( poi==null || (tempPOI!=null && poi.getRadialDistance() > tempPOI.getRadialDistance()))
			{
				poi = tempPOI;
			}
			
			tempPOI = rayTrace( ind, entities, ind.getCenter(), Entity.yPlusVector );
			if( poi==null || (tempPOI!=null && poi.getRadialDistance() > tempPOI.getRadialDistance()))
			{
				poi = tempPOI;
			}

			tempPOI = rayTrace( ind, entities, ind.getCenter(), Entity.yMinusVector );
			if( poi==null || (tempPOI!=null && poi.getRadialDistance() > tempPOI.getRadialDistance()))
			{
				poi = tempPOI;
			}
			
			if( poi != null )
			{
				pointsOfInterest.add( poi );
			}
		}

		return pointsOfInterest;
	}

	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
	}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
}
