/*
 * $RCSfile: FlockingStrategy.java,v $ $Date: 2009/06/11 20:36:55 $
 */
package crowdsimulation.actioncontroller.strategy;

import java.util.*;
import crowdsimulation.*;
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
 * This strategy for the model based around the BOIDS flocking model created by Craig Reynolds.
 *
 *  Individuals move according to the vector sum of five contributing vectors:
 *     cohesion:    Tendency to move to the center of mass of the local group
 *     avoidance:   Tendency to not collide with others, very large compared to others
 *     consistency: Tendency to move in the same direction as the local group
 *     features:    Affects of repulsive and attractive features in the environment
 *     randomness:  Random variations applied to the above.
 *
 * @see crowdsimulation.actioncontroller.strategy.ActionStrategy
 * @author $Author: ganil $
 * @version $Revision: 1.5 $
 * $State: Exp $
 * $Date: 2009/06/11 20:36:55 $
 **/
public class FlockingStrategy extends SocialPotentialStrategy
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The weight used for summing the cohesion of the individuals. 
	 *  The cohesion controlls how close together the individuals will become. **/
	private double cohesion = 0.125;
	/** The weight used for summing the avoidance of the individuals. 
	 *  The acoidance controlls how close far apart the individuals want to be. **/
    private double avoidance = 1.2;
    /** The weight used for summing the consistency of the individuals. 
     *  The consistency controls how strongly the individual will follow the other individuals. **/
	private double consistency = 0.5;
	/** The weight used for summing a randomness in for the individuals. **/
	private double randomness = 0.2;
	/** The weight used for summing the momentum of the individuals. 
	 *  The momentum controlls how difficult it is for the individual to change movement. **/
    private double momentum = 1.0;
    /** The weight used for summing the avoiding obstacle by the individuals. 
     *  The features controlls how the individuals will avoid obstacles. **/
    private double features = 1.1;
    /** The weight used for summing the waypoint attractions for the individuals. 
     *  The features controlls how the individuals will be attracted to waypoints. **/
    private double waypoints = 2.3;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * The default constructor for a Flocking Strategy.
	 **/
	public FlockingStrategy() 
	{
	}
	
	/**
	 * The constructor for a Flocking Strategy which also sets the parameters.
	 * 
	 * @param attributes The values to be used to set the parameters.
	 **/
	public FlockingStrategy( Map attributes ) 
	{
		this();
		this.setParameters( attributes );
	}
	
	/**
	 * Calculates the force on a given individual for the consistency component.
	 * 
	 * @param b The colloection of all individuals or obstacle needed to calculate this force.
	 * @param world The map on which this individual is interacting with the object in the bag b.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
    public Vector2D consistency( Bag b, Continuous2D world )
    {
        if( b==null || b.numObjs == 0 ) 
        {
        	return new Vector2D(0,0);
        }
        
		double x = 0; 
        double y = 0;
        int i =0;
        for( i=0; i<b.numObjs; i++ )
        {
        	Individual other = (Individual)(b.objs[i]);
            

            if( !other.isDead() && !ind.isDead() )
            {
            	double dx = world.tdx( ind.getLocation().x, other.getLocation().x );
                double dy = world.tdy( ind.getLocation().y, other.getLocation().y);
                double lensquared = dx*dx+dy*dy;
                if (lensquared <= CrowdSimulation.getInstance().getDiscretization() * CrowdSimulation.getInstance().getDiscretization() )
                {
                	Vector2D m = ((Individual)b.objs[i]).getMomentum();
                    x += m.x;
                    y += m.y;
                }
            }
        }
        
        x /= b.numObjs;
        y /= b.numObjs;
        
        return new Vector2D( consistency*x, consistency*y );
	}
    
	/**
	 * Calculates the force on a given individual for the cohesion component.
	 * 
	 * @param b The colloection of all individuals or obstacle needed to calculate this force.
	 * @param world The map on which this individual is interacting with the object in the bag b.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
    public Vector2D cohesion( Bag b, Continuous2D world )
    {
    	
        if( b==null || b.numObjs == 0 ) 
        {
        	return new Vector2D( 0,0 );
        }
        
        double x = 0; 
        double y= 0;        

        int i =0;
        for( i=0; i<b.numObjs; i++ )
        {
            Individual other = (Individual)(b.objs[i]);
            if( !other.isDead() && !ind.isDead() )
            {
                double dx = ind.getLocation().x - other.getLocation().x;
                double dy = ind.getLocation().y - other.getLocation().y;
                double lensquared = dx*dx+dy*dy;
                if( lensquared <= CrowdSimulation.getInstance().getDiscretization() * CrowdSimulation.getInstance().getDiscretization() )
                {
                    x -= dx;
                    y -= dy;
                }
            }
        }
        
        x /= b.numObjs;
        y /= b.numObjs;
        
        return new Vector2D( cohesion*x, cohesion*y );
    }
    
	/**
	 * Calculates the force on a given individual for the avoidance component.
	 * 
	 * @param b The colloection of all individuals or obstacle needed to calculate this force.
	 * @param world The map on which this individual is interacting with the object in the bag b.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
    public Vector2D avoidance( Bag b, Continuous2D world )
    {
        if( b==null || b.numObjs == 0 )
        {
        	return new Vector2D(0,0);
        }
        
        double x = 0;
        double y = 0;
        
        int i=0;
        int count = 0;

        for( i=0; i<b.numObjs; i++ )
        {
            Individual other = (Individual)(b.objs[i]);
            if( !other.isDead() && !other.equals(ind)  && !ind.isDead() )
            {
                count++;
                double dx = ind.getLocation().x - other.getLocation().x;
                double dy = ind.getLocation().y - other.getLocation().y;
                double lensquared = dx*dx+dy*dy;
                if( lensquared <= CrowdSimulation.getInstance().getDiscretization() * CrowdSimulation.getInstance().getDiscretization() )
                {
                    x += dx/lensquared;
                    y += dy/lensquared;
                }
            }
        }
        if( count>0 )
        {
            x /= count;
            y /= count;
        }
        
        return new Vector2D( avoidance*x, avoidance*y );
    }
    
    /**
	 * Calculates the force on a given individual for the waypoints component.
	 * 
	 * @param world The map on which this individual is interacting with the object in the bag b.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
    public Vector2D waypoint( Continuous2D world )
    {
 	    if( ind.isDead() )
        {
        	return new Vector2D(0,0);
        }

		if( ind.getInitialVelocity() < ind.getVelocity().magnitude() || ind.getSelectedPath() == null  )
		{
			selectPath();
		}

        double x = 0;
	    double y = 0;
    	
    	Bag paths = ind.getInteractingPaths();
    	if( paths.numObjs < 1 ) return new Vector2D(0,0);
    	
		Path path = ind.getSelectedPath();
		
		Waypoint wPoint = path.getActiveWaypoint( ind );
		if( wPoint != null)
		{
            double dx = ind.getLocation().x - wPoint.getTargetPoint(ind).x;
            double dy = ind.getLocation().y - wPoint.getTargetPoint(ind).y;
            double lensquared = dx*dx+dy*dy;
        
            x -= dx/(lensquared);
            y -= dy/(lensquared);
        }
        else
        {
        	ind.getActionController().removeIndividual(ind);
        	ind.kill();
        }
        
        return new Vector2D( waypoints*x, waypoints*y );
    }
        
	/**
	 * Creates a random force. This is a small force to keep the individuals mooving
	 * and to help from getting stuck in situations due to calculating forces at direct 
	 * 90 and 180 degree angles.
	 * 
	 * @param simState The object which contatins the current state information for the simualtion.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
    public Vector2D randomness( CrowdSimulation simState )
    {
        double x = simState.rand() * 2.0 - 1.0;
        double y = simState.rand() * 2.0 - 1.0;
        double l = Math.sqrt(x * x + y * y);
        x /= l;
        y /= l;
        
        return new Vector2D( randomness* x,randomness * y);
    }

	/**
	 * Calculates the force on a given individual for the feature component;
	 * 
	 * @param b The collection of all individuals or obstacle needed to calculate this force.
	 * @param terrain The map on which this individual is interacting with the object in the bag b.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
    public Vector2D features( Bag b, Continuous2D terrain )
    {
        if( b==null || b.numObjs == 0 || ind.isDead() )
        {
        	return new Vector2D(0,0);
        }
        
        double x = 0;
        double y = 0;
        
        int i=0;
        int count = 0;

        for( i=0; i<b.numObjs; i++ )
        {
			Obstacle feature = (Obstacle)(b.objs[i]);
            double force = feature.getForce();
			count++;
			
          	Vector2D origin = ind.getLocation();
          	
          	Vector2D rVec = ind.distanceTo( feature );
          	
            double dx = rVec.x;
            double dy = rVec.y;
            double lensquared = dx*dx+dy*dy;
           	x -= dx/(lensquared);
            y -= dy/(lensquared);
        }
        if( b.numObjs>0 )
        {
            x /= count;
            y /= count;
        }

        return new Vector2D( features*x, features*y );
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
	 * This function calculates all of the forces acting on the Individual.
	 *
	 * @param simState The object containing the state of the simulation.
	 **/
	public void calculateMovementParameters( CrowdSimulation simState )
	{
		super.calculateMovementParameters( simState );
        Bag individuals = ind.getNeighboringIndividuals();
	    Bag obstacles = ind.getNeighboringObstacles();
		
		Vector2D forceRandom = randomness( simState );
		Vector2D forceCohesion = cohesion( individuals, simState.getWorld() );
		Vector2D forceAvoidance = avoidance( individuals, simState.getWorld() );
		Vector2D forceConsistency = waypoint( simState.getWorld() );
		Vector2D forceMomentum = consistency( individuals, simState.getWorld() );
		Vector2D forceFeatures = features( obstacles, simState.getTerrain() );
		Vector2D forceWaypoints = waypoint( simState.getWorld() );

		double dx = forceRandom.x +
					forceCohesion.x +
					forceAvoidance.x +
					forceConsistency.x +
					forceMomentum.x +
					forceFeatures.x + 
					forceWaypoints.x;
		
		double dy = forceRandom.y +
					forceCohesion.y +
					forceAvoidance.y +
					forceConsistency.y +
					forceMomentum.y +
					forceFeatures.y + 
					forceWaypoints.y;
		
		ind.setForceValues( "" +forceRandom.x+","+forceRandom.y
							   +","+forceCohesion.x+","+forceCohesion.y
							   +","+forceAvoidance.x+","+forceAvoidance.y
							   +","+forceConsistency.x+","+forceConsistency.y
							   +","+forceMomentum.x+","+forceMomentum.y
							   +","+forceFeatures.x+","+forceFeatures.y
							   +","+forceWaypoints.x+","+forceWaypoints.y );
							   
		this.sumOfForces = new Vector2D( dx, dy );
		ind.setNextVelocity( this.sumOfForces );
	}

	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
		super.setParameters( parameters );
		
		if( parameters.containsKey( "cohesion" ) )
		{
			setCohesion( Parameters.convertToDouble( 
				parameters.get( "cohesion" ), 
				getCohesion(),
			   "cohesion for FlockingModel construction must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "avoidance" ) )
		{
			setAvoidance( Parameters.convertToDouble( 
				parameters.get( "avoidance" ), 
				getAvoidance(),
			   "avoidance for FlockingModel construction must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "consistency" ) )
		{
			setConsistency( Parameters.convertToDouble( 
				parameters.get( "consistency" ), 
				getConsistency(),
			   "consitency for FlockingModel construction must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "randomness" ) )
		{
			setRandomness( Parameters.convertToDouble( 
				parameters.get( "randomness" ), 
				getRandomness(),
			   "randomness for FlockingModel construction must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "momentum" ) )
		{
			setMomentum( Parameters.convertToDouble( 
				parameters.get( "momentum" ), 
				getMomentum(),
			   "momentum for FlockingModel construction must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "features" ) )
		{
			setFeatures( Parameters.convertToDouble( 
				parameters.get( "features" ), 
				getFeatures(),
			   "features for FlockingModel construction must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "waypoints" ) )
		{
			setWaypoints( Parameters.convertToDouble( 
				parameters.get( "waypoints" ), 
				getWaypoints(),
			   "waypoints for FlockingModel construction must be a Double or a string representing a Double." ) );
		}
	}
	
	

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the strength for the cohesion for the Model.
	 *
	 * @return The strength for the cohesion for the Model.
	 **/
    public double getCohesion()
	{
		return cohesion;
	}
	
	/**
	 * Sets the strength for the cohesion for the Model.
	 *
	 * @param val The strength for the cohesion for the Model.
	 **/
	public void setCohesion( double val )
	{
		cohesion = val;
	}
	
	/**
	 * Gets the valid interval for the strength for the cohesion for the Model.
	 *
	 * @return The interval for the cohesion for the Model.
	 **/
	public Object domCohesion() { return new Interval(0.0,1.0); }
	
	/**
	 * Gets the strength for the avoidance for the Model.
	 *
	 * @return The strength for the avoidance for the Model.
	 **/
	public double getAvoidance()
	{
		return avoidance;
	}
	
	/**
	 * Sets the strength for the cohesion for the Model.
	 *
	 * @param val The strength for the cohesion for the Model.
	 **/
	public void setAvoidance( double val )
	{
		avoidance = val;
	}
	
	/**
	 * Gets the valid interval for the strength for the avoidance for the Model.
	 *
	 * @return The interval for the avoidance for the Model.
	 **/
	public Object domAvoidance() { return new Interval(0.0,1.0); }
	
	/**
	 * Gets the strength for the consistency for the Model.
	 *
	 * @return double The strength for the consistency for the Model.
	 **/
	public double getConsistency()
	{
		return consistency;
	}
    
    /**
	 * Sets the strength for the consistency for the Model.
	 *
	 * @param val The strength for the consistency for the Model.
	 **/
    public void setConsistency( double val )
    {
    	consistency = val;
    }
    
    /**
	 * Gets the valid interval for the strength for the Consistency for the Model.
	 *
	 * @return The interval for the consistency for the Model.
	 **/
    public Object domConsistency() { return new Interval(0.0,1.0); }
    
    /**
	 * Gets the strength for the randomness for the Model.
	 *
	 * @return The strength for the cohesion for the Model.
	 **/
	public double getRandomness()
	{
		return randomness;
	}
	
	/**
	 * Sets the strength for the randomness for the Model.
	 *
	 * @param val The strength for the randomness for the Model.
	 **/
	public void setRandomness( double val )
	{
		randomness = val;
	}
	
	/**
	 * Gets the valid interval for the strength for the randomness for the Model.
	 *
	 * @return The interval for the randomness for the Model.
	 **/
	public Object domRandomness() { return new Interval(0.0,1.0); }
	
	/**
	 * Gets the strength for the momentum for the Model.
	 *
	 * @return The strength for the momentum for the Model.
	 **/
	public double getMomentum()
	{
		return momentum;
	}
	
	/**
	 * Sets the strength for the momentum for the Model.
	 *
	 * @param val The strength for the momentum for the Model.
	 **/
	public void setMomentum( double val )
	{
		momentum = val;
	}
	
	/**
	 * Gets the valid interval for the strength for the momentum for the Model.
	 *
	 * @return The interval for the momentum for the Model.
	 **/
	public Object domMomentum() { return new Interval(0.0,1.0); }
	
	/**
	 * Gets the strength for the feature for the Model.
	 *
	 * @return The strength for the feature for the Model.
	 **/
    public double getFeatures()
    {
    	return features;
    }
    
    /**
	 * Sets the strength for the feature for the Model.
	 *
	 * @param val The strength for the feature for the Model.
	 **/
    public void setFeatures( double val )
    {
    	features = val;
    }
    
    /**
	 * Gets the valid interval for the strength for the features for the Model.
	 *
	 * @return The interval for the features for the Model.
	 **/
    public Object domFeatures() { return new Interval(0.0,10.0); }
    
    /**
	 * Gets the valid interval for the size of the tStep for the Model.
	 *
	 * @return The interval of the tStep for the Model.
	 **/
    public Object domtStep() { return new Interval(0.0,1.0); }

	/**
	 * Gets the strength for the waypoints for the Model.
	 *
	 * @return The strength for the waypoints for the Model.
	 **/
    public double getWaypoints()
    {
    	return waypoints;
    }
    
    /**
	 * Sets the strength for the waypoints for the Model.
	 *
	 * @param val The strength for the waypoints for the Model.
	 **/
    public void setWaypoints( double val )
    {
    	waypoints = val;
    }
    
    /**
	 * Gets the valid interval for the strength for the waypoints for the Model.
	 *
	 * @return The interval for the waypoints for the Model.
	 **/
    public Object domWaypoints() { return new Interval(0.0,30.0); }

}
