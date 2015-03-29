/*
 * $RCSfile: SocialPotentialStrategy.java,v $ $Date: 2008/04/18 17:55:09 $
 */
package crowdsimulation.actioncontroller.strategy;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import crowdsimulation.util.*;
import java.util.*;
import math.*;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;

/**
 * Base class for classes which control the movement of individuals or obstacles via Socail Potentia Techniques.
 * This class contains the attributes and methods which are common to Social potential action strategies.
 * This object is called at each step in the simulation.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.2 $
 * $State: Exp $
 * $Date: 2008/04/18 17:55:09 $
 **/
public abstract class SocialPotentialStrategy extends ActionStrategy 
{	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The sum of the forces acting on the individual. **/
	protected Vector2D sumOfForces = new Vector2D( 0.0, 0.0 );
	/** The distance at which the individual will follow the wall.**/
	double wallFollowCutOff = 0.5;
	Obstacle previousWallFollowObstacle = null;
	Vector2D previousWallFollowDirection = new Vector2D( -1 , -1 );
	Vector2D previousWallFollowLocation = new Vector2D( -1 , -1 );
	double previousWallFollowTime = 0;
		

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	
	/**
	 * This is where the individual is moved forward by a small timestep.
	 * This is basically integrating the forces over the timestep to calculate where the individuals
	 * moves to and moves them there.
	 * 
	 * @param deltaT The timestep to be used for moving the individuals.
	 **/
	public void move( double deltaT )
	{
		// Set the force acting on the individual.
		ind.setAcceleration( getSumOfForces() );
			
		double Xprev = ind.getLocation().x;
		double Yprev = ind.getLocation().y;
		Vector2D direction = ind.getForces();
		direction.normalize();

		double X = Xprev + direction.x* getSpeed() * deltaT;
		double Y = Yprev + direction.y* getSpeed() * deltaT;
		
		// Actually moves the individual.	
		ind.setMomentum( new Vector2D( (X-Xprev)/(Math.pow(deltaT, 3 )), (Y-Yprev)/(Math.pow(deltaT, 3 )) ) );
		ind.setLocation( CrowdSimulation.getInstance().confineToWorld( ind, X, Y ) );
		
		// Set the new velocity.
		ind.setVelocity( ind.getForces() );		
	}

	/**
	 * Returns the new force vectors if the individual should follow a wall.
	 * This takes in the current force vector of the individual, calculates to 
	 * see what portion of that pushes them into the wall then redirects that 
	 * portion of the force to go tangential to the wall.
	 *
	 * @param force The 2 dimensional vector of the forces acting on the indivudal.
	 * @return The resultant 2 dimensional vector making ht eindividual follow the wall.
	 **/
	 public Vector2D followWall( Vector2D force )
	 {
	 	Vector2D forceToFollow = new Vector2D( 0.0, 0.0 );
	 	Waypoint selectedPoint = ind.getSelectedPath().getActiveWaypoint( ind );
	 	if( selectedPoint == null )
	 	{
	 		return forceToFollow;
	 	}
	 	
	 	Bag obstacles = ind.getInteractingObstacles();
	 	if( obstacles.size() > 0 )
	 	{
		 	Obstacle obstacleOfInterest = (Obstacle)obstacles.get(0); 
		 	double obsDist = ind.distanceTo( obstacleOfInterest ).magnitude();
		 		
		 	for( int i=0; i < obstacles.size(); i++ )
		 	{
		 		Obstacle newObs = (Obstacle)obstacles.get(i); 
		 		double newObsDist = ind.distanceTo( newObs ).magnitude();
		 		Vector2D point = newObs.getClosestPoint( ind.getCenter(), force );
		 		if( newObsDist < obsDist && point.x > 0 && point.y > 0 )
		 		{
		 			obsDist = newObsDist;
		 			obstacleOfInterest = newObs;
		 		}
		 	}
		 	
		 	if( obsDist <= wallFollowCutOff )
		 	{
		 		// Don't allow the individual to turn completely around
				if( Math.abs( ind.getLocation().x - previousWallFollowLocation.x ) < 1 &&
					Math.abs( ind.getLocation().y - previousWallFollowLocation.y ) < 1 &&
					previousWallFollowDirection.getAngleBetween( ind.getVelocity() ) > Math.PI/4 &&
					obstacleOfInterest == previousWallFollowObstacle &&
					CrowdSimulation.getInstance().getSimTime() - previousWallFollowTime < 1
				  )
				{
					previousWallFollowLocation = ind.getLocation();
					previousWallFollowTime = CrowdSimulation.getInstance().getSimTime();
					previousWallFollowObstacle = obstacleOfInterest;
 					return previousWallFollowDirection;
				}
		 		
		 		Vector2D pointOfContact = obstacleOfInterest.getClosestPoint( ind.getCenter(), ind.getVelocity() );
				if( pointOfContact.x == -1 && pointOfContact.y == -1 )
				{
					previousWallFollowLocation = ind.getLocation();
					previousWallFollowTime = CrowdSimulation.getInstance().getSimTime();
 					previousWallFollowDirection = ind.getVelocity();
 					previousWallFollowObstacle = obstacleOfInterest;
					return ind.getVelocity();
				}

		 		Vector2D direction = new Vector2D( force.x, force.y);
				pointOfContact = obstacleOfInterest.getClosestPoint( ind.getCenter(), ind.distanceTo( obstacleOfInterest ) );
				
				Vector2D tang = obstacleOfInterest.getTangentAt( pointOfContact, direction );
				tang.normalize();
		
				double mag = ind.getInitialVelocity();
				
				forceToFollow = new Vector2D( tang.x*mag, tang.y*mag );
				
				previousWallFollowLocation = ind.getLocation();
				previousWallFollowTime = CrowdSimulation.getInstance().getSimTime();
				previousWallFollowObstacle = obstacleOfInterest;
				previousWallFollowDirection = forceToFollow;
		 	}				
	 	}
	 		 	
	 	return forceToFollow;
	 }
	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
	
    /**
     * Gets the vector containing the summ of all forces acting on the individual.
     *
     * @return The summ of all the forces acting on the individual.
	 **/
    public Vector2D getSumOfForces()
    {
    	return sumOfForces;
    }
    
    /**
     * Sets the vector containing the summ of all forces acting on the individual.
     *
     * @param val The summ of all the forces acting on the individual.
	 **/
    public void setSumOfForces( Vector2D val )
    {
    	sumOfForces = val;
    }
    
}
