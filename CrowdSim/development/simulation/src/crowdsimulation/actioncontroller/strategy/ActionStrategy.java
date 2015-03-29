/*
 * $RCSfile: ActionStrategy.java,v $ $Date: 2010/12/31 20:39:56 $
 */
package crowdsimulation.actioncontroller.strategy;

import crowdsimulation.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
//import crowdsimulation.logging.*;
import crowdsimulation.util.*;
import java.util.*;
import math.*;
import sim.engine.*;
//import sim.field.continuous.*;
import sim.util.*;

/**
 * Base class for classes which control the movement of individuals or obstacles.
 * This class contains the attributes and methods which are common to a specific action model.
 * This object is called at each step in the simulation.
 *
 * @author $Author: dkaup $
 * @version $Revision: 1.6 $
 * $State: Exp $
 * $Date: 2010/12/31 20:39:56 $
 **/
public abstract class ActionStrategy 
{	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The timestep being used for the simulation. **/
	protected double timeStep;
	/** The individual being controlled by this action strategy. **/
	protected Individual ind;
	/** The sum of the forces acting on the individual. **/
	protected Vector2D sumOfForces = new Vector2D( 0.0, 0.0 );
	/** The distance at which an individual will no longer feel an influence. **/
	protected double interactionRadius = 0;
	/** The speed of an indiviudal. **/
	protected double speed = 1;
	/** A minor offset used to correct for division by zero type errors. **/
	protected double eps = 0.3;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * This calculates the points that are needed to calculate the movement parameters.
	 *
	 * @param state The current State of the simulation.
	 **/
	public void calculateMovementParameters( CrowdSimulation state )
	{
        if( ind.isDead() )
        { 
        	return;
       	}
       	
        Bag individuals = ind.getNeighboringIndividuals();
        Bag obstacles = ind.getNeighboringObstacles();
	}

	
	/**
	 * This is called by the scheduler and redirects the call to calculateMovementParameters.
	 *
	 * @see #calculateMovementParameters( CrowdSimulation state  )
	 * @param state The state of the simulation.
	 **/
	public void preStep( SimState state )
	{
		calculateMovementParameters( (CrowdSimulation)state );
	}

	
	/**
	 * Selects a path for the individual to follow.
	 * The path is selected selected as the path with the largest force and calculated as
	 *   path.getWeight() * 1/(density*r*r).<BR>
	 * The density is the density at the path, and r is the distance to the path.
	 **/
	public void selectPath()
	{
		double totalWeight = 0;
		double pdf = 0;
		CrowdSimulation simState = CrowdSimulation.getInstance();
		double tempForce = 0;
		Path tempPath = null;

		for( int i =0; i < ind.getInteractingPaths().size(); i++ )
		{
	  		Path path = (Path)ind.getInteractingPaths().get(i);

			Waypoint wPoint = path.getActiveWaypoint( ind );
			if( wPoint == null )
			{
				ind.kill();
				return;
			}
            else {
                Bag individuals = simState.getIndividualsWithinDistance( wPoint.getTargetPoint(ind), ind.getRadiusOfConcern() );
                int density = individuals.numObjs;

                Vector2D contact = ind.distanceTo( wPoint.getTargetPoint(ind) );

                double r = contact.magnitude();

                double force = path.getWeight() * 1/((density+0.01)*Math.pow(r,4));

                if( tempForce < force )
                {
    				tempForce = force;
    				tempPath = path;
    			}
            }
		}

		ind.setSelectedPath( (Path) tempPath );
		return;
	}


	/**
	 * This is the method called at each step of the simulation.
	 * Based on the forces which have been calculated the individual is moved acording to the 
	 * calculated movement parameters and the current time step.
	 *
	 * @param state The object representing the current state of the simulation.
	 **/
	public void step( SimState state )
	{
		move( getTimeStep() );
	}

	
	/**
	 * The actions to be taken care of after the individuals have moved.
	 * 
	 * @param state The current State of the simulation.
	 **/
	public void postStep(  SimState state  )
	{
	}

	
	/**
	 * This is where the individual is moved forward by a small timestep.
	 * This is basically integrating the forces over the timestep to calculate where the individuals
	 * moves to and moves them there.
	 * 
	 * @param deltaT The timestep to be used for moving the individuals.
	 **/
	public void move( double deltaT )
	{
	}


	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
		if( parameters.containsKey( "speed" ) )
		{
			setSpeed( Parameters.convertToDouble( 
				parameters.get( "speed" ), 
				getSpeed(),
			   "Speed for the strategy must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "interactionRadius" ) )
		{
			setInteractionRadius( Parameters.convertToDouble(
				parameters.get( "interactionRadius" ),
				getInteractionRadius(),
				"interactionRadius for SalamanderStrategy construction must be a Double or a string representing a Double." ));
		}
    }

	
	/**
	 * Squares the value passed in.
     *
	 * @param a1 the value to be squared.
	 * @return The square of the input value. return = a1*a1.
	 **/
	public double SQR( double a1 )
	{
		return Math.pow( a1, 2 );
	}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the time step for the Strategy.
	 *
	 * @return The timestep used by this Action Strategy.
	 **/
	public double getTimeStep()
	{
       	return ind.getActionController().getTimeStep();
	}
	
	/**
	 * Gets the individual assocaited with this action Strategy.
	 *
	 * @return The individual.
	 **/
	public Individual getIndividual()
	{
		return this.ind;	
	}
	
	/**
	 * Sets the individual assocaited with this action Strategy.
	 *
	 * @param val The individual.
	 **/
	public void setIndividual(Individual val)
	{
		this.ind = val;	
	}

    /**
     * Gets the summ of the forces which dictate the movement of the individual.
     *
     * @return The summ of the forces.
	 **/
    public Vector2D getSumOfForces()
    {
    	return sumOfForces;
    }
    
    /**
     * Sets the summ of the forces which dictate the movement of the individual.
     *
     * @param val The summ of the forces.
	 **/
    public void setSumOfForces( Vector2D val )
    {
    	sumOfForces = val;
    }
    
    /**
     * Gets the speed that the strategy says the individual should be travelling at.
     *
     * @return The speed of the individual.
	 **/
    public double getSpeed()
    {
    	return speed;
    }
    
    /**
     * Sets the speed that the strategy says the individual should be travelling at.
     *
     * @param val The speed of the individual.
	 **/
    public void setSpeed( double val )
    {
    	speed = val;
    }
    
	/**
     * Gets the interaction radius of the strategy.
     *
     * @return The interaction radius of the strategy.
	 **/
    public double getInteractionRadius()
	{
		return interactionRadius;
	}

	/**
     * Sets the interaction radius of the strategy.
     *
     * @param val The interaction radius of the strategy.
	 **/
	public void setInteractionRadius( double val )
	{
		interactionRadius = val;
	}

}
