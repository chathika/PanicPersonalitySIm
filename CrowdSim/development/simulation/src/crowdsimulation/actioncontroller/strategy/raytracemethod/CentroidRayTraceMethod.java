/*
 * $RCSfile: CentroidRayTraceMethod.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.actioncontroller.strategy.raytracemethod;

import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import java.util.*;
import sim.util.*;
import math.*;

/**
 * This tests only the line formed between the center of the two interacting entities.
 * A center line is drawn from the center of the sampling entity and then to each entity
 * that might be sampled. The point of intersection is then taken form that.
 *
 * @author $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class CentroidRayTraceMethod extends RayTraceMethod
{	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * The default constructor of a Centroidal Ray Tracing method.
	 **/
	public CentroidRayTraceMethod()
	{
	}

	
	/**
	 * Constructing a Centroidal Ray Trace method with a given set of parameters.
	 *
	 * @param attributes The attributes used to set the parameters of the Ray Tracer.
	 **/
	public CentroidRayTraceMethod( Map attributes )
	{
		setParameters( attributes );
	}


	/**
	 * This is where the environment is sampled to find all points of interest for movement.
	 * The environment is sampled by drawing lines from the individual sampling the environment 
	 * to the entities which might be sampled. Then the point of interest is found
	 * at the intersection of that line and the entity.
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
			
			Vector2D centerDist = new Vector2D( entity.getCenter().x-ind.getX(), entity.getCenter().y-ind.getY() );
			poi = rayTrace( ind, entities, ind.getCenter(), centerDist );

			if( poi!=null )
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
