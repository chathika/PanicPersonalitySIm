/*
 * $RCSfile: HMFVSocialStrategy.java,v $ $Date: 2009/06/11 20:36:55 $
 */
package crowdsimulation.actioncontroller.strategy;

import java.util.*;
import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import crowdsimulation.util.*;
import math.*;								// used for Vector2D
import ec.util.*;
import sim.field.continuous.*;
import sim.util.*;

/**
 * This is the strategy for the model based around on the Social elements of the HMFV model.
 * This only has the long distance forces and does not deal with the physical 
 * contact forces in from the HMFV model.
 *
 * @see crowdsimulation.actioncontroller.strategy.ActionStrategy
 * @author $Author: ganil $
 * @version $Revision: 1.3 $
 * $State: Exp $
 * $Date: 2009/06/11 20:36:55 $
 **/
public class HMFVSocialStrategy extends SocialPotentialStrategy 
{		

////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The weight used for summing the cohesion of the individuals. 
	 *  The cohesion controlls how close together the individuals will become. **/
	public double avoidInds = 25.0;
	/** The velocity that the individual wishes to travel at. **/
	public double preferedVelocity = 1.0;
	/** The weight used for summing the avoidance of the individuals. 
	 *  The avoidObstacle controlls how close far an individual want to be from an obstacle. **/
	public double avoidObstacle = 25.0;
	/** The weight used for summing the avoidance of the individuals. 
	 *  The attractors controlls how strongly they are attracted to attractors placed in the field. **/
	public double attractors = 10.0;
	/** The weight used for summing a randomness in for the individuals. **/
    public double randomness = 4.0;
    /** The weight used for summing the momentum of the individuals. 
     *  The momentum controlls how difficult it is for the individual to change movement. **/
    public double momentum = 1.0;
    
	/** Parameter to adjust exponential the social repulsion forces. **/
	public double b = 0.08;
	/** The time constant to create a drop off for the self propelling force.**/
	double tau = 0.5;
	/** gauss theta **/
	double gaTh = 1.0;
	/** gauss mean **/
	double gaMe = 0;
	/** gauss cutoff multiplier **/
	double gaCM = 3.0;
	/** The maximum random value. **/
	double RAND_MAX = 0;
	
	/** A random number generator with a normal distribution. **/
	NormalDistributedGenerator randomGen = new NormalDistributedGenerator();
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * The Default Constructor for the HMFVSocialStrategy.
	 **/
	public HMFVSocialStrategy() 
	{
	}
	
	
	/**
	 * Constructor for a Social Potential Strategy where the data is set by attribute set passed in.
	 *
	 * @param attributes Map of attributes to be set in the strategy.
	 **/
	public HMFVSocialStrategy( Map attributes ) 
	{
		this();
		this.setParameters( attributes );
	}


	/**
	 * Calculates the social forces on the individual from an entity.
	 * The force is : A*exp[(radius_i-dist)/B]
	 *
	 * @param e the entity which is causing the social force.
	 * @return Vector2D The resultant force vector.
	 **/
	public Vector2D socialForces( Entity e )
	{
		Vector2D pointOfInterest = ind.surfacePointOn( e );
		Vector2D dist = ind.distanceTo( pointOfInterest );
		Vector2D normal = ind.getNormalOf( e );
		
		double tempF = Math.exp( (ind.getRadius() - dist.magnitude())/b );
		
		return new Vector2D( normal.x * tempF, normal.y * tempF );
	}

	
	/**
	 * Calculates the force on a given individual for the avoidingIndividuals component;
	 * 
	 * @param bag The colloection of all individuals or obstacle needed to calculate this force.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
   	public Vector2D avoidInd( Bag bag )
    {
    	Vector2D force = new Vector2D( 0, 0);
    	double forceX = 0;
    	double forceY = 0;
    	
        for( int i=0; i<bag.numObjs; i++ )
        {
            Individual other = (Individual)(bag.objs[i]);
            
			if( !ind.equals( other ) )
			{
				Vector2D rVec = ind.distanceTo( other );
		
				Vector2D tempForce =  socialForces( other );
				forceX += tempForce.x;
				forceY += tempForce.y;
			}
        }

        force.x = avoidInds*forceX;
        force.y = avoidInds*forceY;

        return force;
	}
    
    /**
	 * Calculates the force on a given individual for the preferedVelocity component;
	 * 
	 * @param b The collection of all individuals or obstacle needed to calculate this force.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
   	public Vector2D preferedVelocity( Bag b )
    {
    	Vector2D force = new Vector2D( 0, 0);
    	double forceX = 0;
    	double forceY = 0;
    	
		forceX = (ind.getInitialVelocity()-ind.getVelocity().x)/ind.getReactionTime();
		forceY = (ind.getInitialVelocity()-ind.getVelocity().y)/ind.getReactionTime();
        
        force = new Vector2D( preferedVelocity*forceX, preferedVelocity*forceY);
        
        return force;
	}

	/**
	 * Calculates the force on a given individual for the avoidObs component;
	 * 
	 * @param bag The collection of all individuals or obstacle needed to calculate this force.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
   	public Vector2D avoidObs( Bag bag )
    {
        if( bag==null || bag.numObjs == 0 )
        {
        	return new Vector2D(0,0);
        }
        
        double x = 0;
        double y = 0;
        
        int i=0;
        double force = 0;

       	for( i=0; i<bag.numObjs; i++ )
       	{
        	Obstacle feature = (Obstacle)(bag.objs[i]);
			
			Vector2D tempForce = socialForces( feature );
					
			x += avoidObstacle*tempForce.x;
			y += avoidObstacle*tempForce.y;
		}

       	return new Vector2D(x,y);
	}

	/**
	 * Calculates the force on a given individual because of attractors component;
	 * 
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
   	public Vector2D attractors( )
    {
		Vector2D force = new Vector2D( 0, 0 );
		if( ind.getSelectedPath() == null || ind.getVelocity().magnitude() < ind.getInitialVelocity()/2 )
		{
			selectPath();
			if( ind.isDead() ) return( new Vector2D( 0, 0 ) );
		}
		
    	// self-propelling 
    	Vector2D initialVelocity = ind.getVelocity();
    	Vector2D vel = (Vector2D)initialVelocity.clone();
    	
    	vel.normalize();

  		Path path = ind.getSelectedPath();
  		Waypoint wPoint = path.getActiveWaypoint( ind );
	  			  		
  		if( wPoint == null )
  		{
  			((HMFVModel)ind.getActionController()).removeIndividual( ind );
  			return( new Vector2D( 0, 0 ) );
  		}
	  		
  		Vector2D contact = ind.distanceTo( wPoint.getTargetPoint(ind) );
  		double r = contact.magnitude();
  		double phi = Math.atan2( contact.y, contact.x );
	  		
    	force.x += path.getWeight() * 1/tau * (ind.getInitialVelocity()*Math.cos(phi) - ind.getVelocity().x);
	  	force.y += path.getWeight() * 1/tau * (ind.getInitialVelocity()*Math.sin(phi) - ind.getVelocity().y);
	  	
	  	return force;
	}

	/**
	 * Creates a random force.
	 * 
	 * @param r The random number generator used to calculate the random force.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/   
    public Vector2D randomness( MersenneTwisterFast r )
    {
		double ksi = 0;
		double eta = 0;
		
		Vector2D force = new Vector2D( 0, 0 );
		
		double sqrt_fact = Math.sqrt( getTimeStep()/((SocialPotentialModel)ind.getActionController()).getDefaultDeltaT() );

		if(gaTh!=0.0)
	  	{ 
	    	ksi = randomGen.gaussRand( gaMe, gaTh, gaCM );
		  	eta = 2.0*Math.PI * r.nextDouble() / (RAND_MAX+1.0);
	  	}
	  	else
	  	{ 
	  		ksi=0.0; 
	  		eta=0.0; 
	  	}
	  	
	  	force.x = sqrt_fact * ksi * Math.cos(eta);
	  	force.y = sqrt_fact * ksi * Math.sin(eta);
	  	return force;
    }


	/**
	 * This function calculates all of the forces acting on the individual.
	 *
	 * @param simState The object containing the state of the simulation.
	 **/
	public void calculateMovementParameters( CrowdSimulation simState )
	{
        Vector2D loc = new Vector2D( simState.getWorld().getObjectLocation( ind ) );
        
        if( ind.isDead() )
        { 
        	return;
       	}
        
        Bag individuals = simState.getWorld().getObjectsWithinDistance( loc.getDouble2D(), simState.getDiscretization(), true );
        ind.setInteractingIndividuals( individuals );
        Bag obstacles = simState.getTerrain().getAllObjects();
		ind.setInteractingObstacles( obstacles );
		
        Vector2D avoidInd = new Vector2D(0,0);//avoidInd( individuals );
        Vector2D prefVel = new Vector2D(0,0);//preferedVelocity( individuals );
        Vector2D avoidObs = avoidObs( obstacles );
        Vector2D attract = attractors( );
        Vector2D rand = new Vector2D(0,0);//randomness( simState.random );
        Vector2D mome = new Vector2D(0,0);//ind.getMomentum();

        ind.setForceValues( ""+avoidInd.x+","+avoidInd.y+","+
        					 prefVel.x+","+prefVel.y+","+
        					 avoidObs.x+","+avoidObs.y+","+
        					 attract.x+","+attract.y+","+
        					 rand.x+","+rand.y+","+
        					 mome.x+","+mome.y );

        double fx = avoidInd.x + 
        			prefVel.x + 
        			avoidObs.x + 
        			attract.x +
        			rand.x + 
        			mome.x;
        double fy = avoidInd.y + 
        			prefVel.y + 
        			avoidObs.y + 
        			attract.y +
        			rand.y + 
        			mome.y;
        			
        ind.setForces( fx, fy );
	}

	/**
	 * Moves the individuals using the passed in time step.
	 *
	 * @param deltaT the timestep that has been set for use for this step.
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
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
		super.setParameters( parameters );
		
		if( parameters.containsKey( "avoidInd" ) )
		{
			setAvoidInd( Parameters.convertToDouble(	
				parameters.get( "avoidInd" ),
				getAvoidInd(),
				"avoidInd must be a Double or a string representing a Double."));
		}
		if( parameters.containsKey( "preferedVelocity" ) )
		{
			setPreferedVelocity( Parameters.convertToDouble(
				parameters.get( "preferedVelocity" ),
				getPreferedVelocity(),
				"preferedVelocity must be a Double or a string representing a Double."));
		}
		if( parameters.containsKey( "avoidObstacle" ) )
		{
			setAvoidObstacle( Parameters.convertToDouble(
				parameters.get( "avoidObstacle" ),
				getAvoidObstacle(),
				"avoidObstacle must be a Double or a string representing a Double."));
		}
		if( parameters.containsKey( "attractors" ) )
		{
			setAttractors( Parameters.convertToDouble(
				parameters.get( "attractors" ),
				getAttractors(),
				"attractors must be a Double or a string representing a Double."));
		}
		if( parameters.containsKey( "randomness" ) )
		{
			setRandomness( Parameters.convertToDouble(
				parameters.get( "randomness" ),
				getRandomness(),
				"randomness must be a Double or a string representing a Double."));
		}
		if( parameters.containsKey( "momentum" ) )
		{
			setMomentum( Parameters.convertToDouble(
				parameters.get( "momentum" ),
				getMomentum(),
				"momentum must be a Double or a string representing a Double."));
		}
		if( parameters.containsKey( "b" ) )
		{
			setB( Parameters.convertToDouble(
				parameters.get( "b" ),
				getB(),
				"b must be a Double or a string representing a Double."));
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Gets the coefficient of the force for avoiding all other individuals.
	 *
	 * @return The coefficient of the force for avoiding individuals.
	 **/
	public double getAvoidInd()
	{
		return avoidInds;
	}
	
	/**
	 * Sets the coefficient of the force for avoiding all other individuals.
	 *
	 * @param val The coefficient of the force for avoiding individuals.
	 **/
	public void setAvoidInd( double val )
	{
		avoidInds = val;
	}
	
	/**
	 * Gets the coefficient of the preffered velocity of the individual.
	 *
	 * @return The coefficient for the prefered velocity of the individual.
	 **/
	public double getPreferedVelocity()
	{
		return preferedVelocity;
	}
	
	/**
	 * Sets the coefficient of the preffered velocity of the individual.
	 *
	 * @param val The coefficient for the prefered velocity of the individual.
	 **/
	public void setPreferedVelocity( double val )
	{
		preferedVelocity = val;
	}
	
	/**
	 * Gets the coefficient for avoiding obstacles.
	 *
	 * @return The coefficient for avoiding obstacles.
	 **/
	public double getAvoidObstacle()
	{
		return avoidObstacle;
	}
	
	/**
	 * Sets the coefficient for avoiding obstacles.
	 *
	 * @param val The coefficient for avoiding obstacles.
	 **/
	public void setAvoidObstacle( double val )
	{
		avoidObstacle = val;
	}
	
	/**
	 * Gets the coefficient for attractors.
	 *
	 * @return The coefficient for attractors.
	 **/
	public double getAttractors()
	{
		return attractors;
	}
	
	/**
	 * Sets the coefficient for attractors.
	 *
	 * @param val The coefficient for attractors.
	 **/
	public void setAttractors( double val )
	{
		attractors = val;
	}
	
	/**
	 * Gets the coefficient for randomness.
	 *
	 * @return The coefficient for randomness.
	 **/
	public double getRandomness()
	{
		return randomness;
	}
	
	/**
	 * Sets the coefficient for randomness.
	 *
	 * @param val The coefficient for randomness.
	 **/
	public void setRandomness( double val )
	{
		randomness = val;
	}
	
	/**
	 * Gets the coefficient for momentum.
	 *
	 * @return The coefficient for momentum.
	 **/
    public double getMomentum()
    {
    	return momentum;
    }
    
    /**
	 * Sets the coefficient for momentum.
	 *
	 * @param val The coefficient for momentum.
	 **/
    public void setMomentum( double val )
    {
    	momentum = val;
    }
 
	/**
	 * Gets the value for attribute b.
	 *
	 * @return The value of attribute b.
	 **/
 	public double getB()
 	{
 		return b;
 	}
 	
 	/**
	 * Sets the value for attribute b.
	 *
	 * @param val The value of attribute b.
	 **/
 	public void setB( double val )
 	{
 		b = val;
 	}
 	
}
