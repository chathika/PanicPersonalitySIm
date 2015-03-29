/*
 * $RCSfile: RayTraceMethod.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.actioncontroller.strategy.raytracemethod;

import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import java.util.*;
import sim.util.*;
import math.*;

/**
 * This is the base class for the rayTrace methods that collect pointsOfInterest.
 *
 * @author $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public abstract class RayTraceMethod
{	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The randome number generator used to sample the environment. **/
	RandomGenerators randomGenerator = new NormalDistributedGenerator();

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * This is where the environment is sampled to find all points of interest for movement.
	 * This is where the logic of the sampling will be added in subclasses.
	 * 
	 * @return The bag of all the PointsOfInterest found.
	 **/
	public abstract Bag getPointsOfInterest( Individual ind, Bag allEntities );
	
	/**
	 * This samples the environment to see if a given ray intersect any entities.
	 *
	 * @param ind The Individual that is doing the sampling
	 * @param allEntities All entities which might be sampled.
	 * @param origin The Starting point of the ray to test.
	 * @param angle The direction of the ray in radians.
	 * @return The closest intersection found, returned as a PointOfInterest.
	 * This will return a value of NULL if no intersections are found.
	 **/
	PointOfInterest rayTrace( Individual ind, Bag allEntities, Vector2D origin, double angle )
	{
		Vector2D direction = new Vector2D( Math.cos( angle ), Math.sin( angle ) );
		
		return rayTrace( ind, allEntities, origin, direction );
	}
	
	
	/**
	 * This samples the environment to see if a given ray intersect any entities.
	 *
	 * @param ind The Individual that is doing the sampling
	 * @param allEntities All entities which might be sampled.
	 * @param origin The Starting point of the ray to test.
	 * @param direction The direction of the ray as a vector.
	 * @return The closest intersection found, returned as a PointOfInterest.
	 * This will return a value of NULL if no intersections are found.
	 **/
	PointOfInterest rayTrace( Individual ind, Bag allEntities, Vector2D origin, Vector2D direction )
	{
		PointOfInterest poi = null;
		
		double dist = Double.MAX_VALUE;
		
		for( int i = 0; i < allEntities.size(); i++  )
		{
			Entity entity = (Entity)allEntities.get( i );
			Vector2D point = entity.getClosestPoint( origin, direction.normalize() );

			Vector2D distToPoint = ind.distanceTo( entity );
			double distToEntity = distToPoint.magnitude();

			if( (point != null && point.x!=-1 && point.y!=-1) )
			{
				poi = new PointOfInterest( ind, point, entity, distToPoint, distToEntity );
			}
			
		}
		
		return poi;
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
