/*
 * $RCSfile: HMFVWithParamsStrategy.java,v $ $Date: 2008/03/17 19:10:19 $
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
 * This is a model used to simulate Human movement.
 *
 * @see crowdsimulation.actioncontroller.strategy.ActionStrategy
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:19 $
 **/
public class HMFVWithParamsStrategy extends HMFVStrategy 
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The default age of the individual **/
	private int age = 20;			

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Default onstructor for an HMFV Strategy with parameters.
	 **/
	public HMFVWithParamsStrategy() 
	{
		super();
	}

	
	/**
	 * Constructor for an HMFV Strategy with parameters where the data is set by attribute set passed in.
	 *
	 * @param attributes Map of attributes to be set in the strategy.
	 **/
	public HMFVWithParamsStrategy( Map attributes ) 
	{
		super( attributes );
		this.setParameters( attributes );
	}
	
	
	/**
	 * Function representing the fractional adjustment amount for the personal space based on age groups.
	 *
	 * @return The amount of personal space adjustment based on age.
	 **/
	public double personalSpace()
	{
		if( age > 3 && age < 8 )
		{
			return .25;
		}
		if( age > 70 )
		{
			return 2;
		}
		return 1;
	}

	
	/**
	 * Function representing the speed based on age groups.
	 *
	 * @return The amount of speed based on age.
	 **/
	public double getSpeed()
	{
		double speed = super.getSpeed(); 
		if( age > 3 && age < 8 )
		{
			speed = 2*speed;
		}
		if( age > 70 )
		{
			speed =  0.5*speed;
		}
		return speed;
	}

	
	/**
	 * Function representing the amount of randomness based on age groups.
	 *
	 * @return The amount of randomness adjustment based on age.
	 **/
	public double randomness()
	{
		if( age > 3 && age < 8 )
		{
			return 6;
		}
		if( age > 70 )
		{
			return .5;
		}
		return 1;
	}


	/**
	 * Function representing the amount of attraction to exit based on age groups.
	 *
	 * @return The amount of attraction to exit based on age.
	 **/
	public double exitAttraction()
	{
		if( age > 3 && age < 8 )
		{
			return .15;
		}
		return 1;
	}


	/**
	 * Force to keep away from the obstacle by a certain distance
	 * 
	 * @param obs The obstacle with which the interaction is occuring.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
	public Vector2D wallPsychForce( Obstacle obs )
	{
		Vector2D tempF = super.wallPsychForce( obs );
		return new Vector2D( personalSpace()*tempF.x, personalSpace()*tempF.y );
	}


	/**
	 * Force to keep away from another individual by a certain distance
	 * 
	 * @param ind2 The individual for which the force is being calculate.
	 * @return Vector2D A 2 Dimensional vector representing the force in the x and y directions.
	 **/
	public Vector2D pp_PsychForce( Individual ind2 )
	{
		Vector2D tempF = super.pp_PsychForce( ind2 );
		return new Vector2D( personalSpace()*tempF.x, personalSpace()*tempF.y );
	}

	
	/**
	 * Gives the force due to the current movement of the individual.
	 * 
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/	
	public Vector2D random()
	{
		Vector2D tempF = super.random();
		return new Vector2D( randomness()*tempF.x, randomness()*tempF.y );
 	}

 	
 	/**
	 * Gives the force due to the current movement of the individual.
	 * 
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/	
	public Vector2D selfPropellingForce( )
	{
		Vector2D tempF = super.selfPropellingForce( );
		return new Vector2D( exitAttraction()*tempF.x, exitAttraction()*tempF.y );
	}


	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
		super.setParameters( parameters );
		
		if( parameters.containsKey( "age" ) )
		{
			setAge( Parameters.convertToInt( 
				parameters.get( "age" ), 
				getAge(),
			   "age must be a Double or a string representing a Double." ) );
		}
	}
	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the age of the individuals for the simulation.
	 *
	 * @return The age of the individuals for the simulation.
	 **/
	public int getAge()
	{
		return age;
	}
	
	/**
	 * Sets the age of the individuals for the simulation.
	 *
	 * @param val The age of the individuals for the simulation.
	 **/
	public void setAge( int val )
	{
		this.age = val;
	}		
}
