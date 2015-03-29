/*
 * $RCSfile: ModifiedHMFVStrategy.java,v $ $Date: 2010/10/25 16:19:39 $
 */
package crowdsimulation.actioncontroller.strategy;

import java.util.*;
import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*; 			// used for logging
import math.*;								// used for Vector2D
import ec.util.*;
import sim.field.continuous.*;
import sim.util.*;

/**
 * This is a model used to simulate HMFV style movement.
 *
 * @see crowdsimulation.actioncontroller.strategy.ActionStrategy
 * @author $Author: dkaup $
 * @version $Revision: 1.2 $
 * $State: Exp $
 * $Date: 2010/10/25 16:19:39 $
 **/
public class ModifiedHMFVStrategy extends HMFVStrategy
{

////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Default constructor for a Modified HMFV Strategy.
	 **/
	public ModifiedHMFVStrategy() 
	{
		super();
	}

	
	/**
	 * Constructor for a Modified HMFV Strategy where the data is set by attribute set passed in.
	 *
	 * @param attributes Map of attributes to be set in the strategy.
	 **/
	public ModifiedHMFVStrategy( Map attributes ) 
	{
		super( attributes );
	}


	/**
	 * Calculates the tangential forces on the individual from an entity.
	 * The force is : Kappa*(radius_i - dist) *velocity
	 *
	 * @param e the entity which is causing the tangential force.
	 * @return Vector2D The resultant force vector.
	 **/
	public Vector2D tangentialForces( Entity e )
	{
		Vector2D pointOfInterest = ind.surfacePointOn( e );

		Vector2D r = ind.distanceTo( pointOfInterest );

        // DJK: I do not see that "r" is ever used herein.
        
		Vector2D velocity = new Vector2D( ind.getVelocity().x-e.getVelocity().x, ind.getVelocity().y-e.getVelocity().y );
		Vector2D tangent = ind.getTangentOf( e );
		tangent = new Vector2D( -tangent.x, -tangent.y);
		
		double rx = ind.getCenter().x-pointOfInterest.x;
		double ry = ind.getCenter().y-pointOfInterest.y;
		double scal_prod_over_rsqr = (ry*ind.getVelocity().x - rx*ind.getVelocity().y) / (SQR(r.magnitude()) + SQR(eps)); 
		
		double tempF = -1 * kappa * (ind.getRadius() - r.magnitude()) * scal_prod_over_rsqr;
		
		return new Vector2D( ry * tempF, -1 * rx * tempF );
	}	
		
}
