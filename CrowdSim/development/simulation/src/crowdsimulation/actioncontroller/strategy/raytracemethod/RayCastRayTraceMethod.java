/*
 * $RCSfile: RayCastRayTraceMethod.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.actioncontroller.strategy.raytracemethod;

import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.logging.*;
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
public class RayCastRayTraceMethod extends RayTraceMethod
{	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The angle to either side of the orientation that the environment will be sampled. **/
	private double viewAngle = 90;
	/** The number of samples to take across the environment. **/
	private int numberOfSamples = 30;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * The default constructor of the ray trace method which samples some portion of the environment.
	 **/
	public RayCastRayTraceMethod()
	{
	}

	
	/**
	 * The constructor which sets the attributs of the RayCastRayTraceMethod by the given set of attributes.
	 *
	 * @param attributes The parameter values which should be used to initialize this object.
	 **/
	public RayCastRayTraceMethod( Map attributes )
	{
		setParameters( attributes );
	}


	/**
	 * This is where the environment is sampled to find all points of interest for movement.
	 * 
	 * @return The bag of all the PointsOfInterest found.
	 **/
	public Bag getPointsOfInterest( Individual ind, Bag allEntities )
	{
		Bag pointsOfInterest = new Bag();
		
		for( int i = 0; i < numberOfSamples; i++ )
		{
			double angle = (randomGenerator.nextValue()*2*viewAngle)-viewAngle;
			
			PointOfInterest poi= rayTrace( ind, allEntities, ind.getCenter(), angle );
			
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
		super.setParameters( parameters );
		
		if( parameters.containsKey( "viewAngle" ) )			
		{
			Object value = parameters.get( "viewAngle" );
			double val = 0;
			
			if( value instanceof String )
			{
				val = Double.parseDouble( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Double)value).doubleValue();
			}
			else if( value instanceof RandomGenerators )
			{
				val = ((RandomGenerators)value).nextValue();
			}
			else
			{
				Log.log( 1, " ViewAngle for the strategy must be a Double or a string representing a Double.");
				Log.log( 1, " Random Generators are also valid.");
			}
			setViewAngle( val );
		}
		if( parameters.containsKey( "numberOfSamples" ) )
		{
			Object value = parameters.get( "numberOfSamples" );
			int val = 0;
			
			if( value instanceof String )
			{
				val = Integer.parseInt( (String)value );
			}
			else if( value instanceof Integer )
			{
				val = ((Double)value).intValue();
			}
			else if( value instanceof RandomGenerators )
			{
				val = (int)((RandomGenerators)value).nextValue();
			}
			else
			{
				Log.log( 1, " NumberOfSamples for the strategy must be a Double or a string representing a Double.");
				Log.log( 1, " Random Generators are also valid.");
			}
			setNumberOfSamples( val );
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the view angle from the object.
	 *
	 * @return The angle represnting the amount of the environemnt to be sampled from the orientation vector.
	 **/
	public double getViewAngle()
	{
		return viewAngle;
	}
	
	/**
	 * Sets the view angle from the object.
	 *
	 * @param val The angle represnting the amount of the environemnt to be sampled from the orientation vector.
	 **/
	public void setViewAngle( double val )
	{
		viewAngle = val;
	}
	
	/**
	 * Gets the number of samples to be taken from teh environment.
	 *
	 * @return The number of samples which will be taken by this ray tracer.
	 **/
	public int getNumberOfSamples()
	{
		return numberOfSamples;
	}
	
	/**
	 * Sets the number of samples to be taken from teh environment.
	 *
	 * @param val The number of samples which will be taken by this ray tracer.
	 **/
	public void setNumberOfSamples( int val )
	{
		numberOfSamples = val;
	}
	
}
