/*
 * $RCSfile: EnvDataLogger.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.logging;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;	// Contains models of behavior for the entities. 
import crowdsimulation.entities.*;		// Contains definition of the crowd agents and features.
import crowdsimulation.entities.individual.*;
import java.io.*;
import java.util.*;
import sim.engine.*;
import sim.util.*;

/**
 * The class prints out the data for a given timestep, for all individuals.
 * The data recorded is:
 *  1. realTime
 *  2. simTime
 *  3. exitDensity
 *  4. fluxAcrossDoorway
 *  5. averageOrientation
 *  6. averageVelocityX
 *  7. averageVelocityY
 *  8. averageVelocity
 *  9. numberOfIndividualsInRoom
 *  10. numberOfIndividualsMovingAwayFromDoor
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class EnvDataLogger extends Logger
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The frequency (number of times per second) to write to the log. 
	 * One Sample every 1/smpleRate of a second. **/
	private double sampleRate = 29;
	/** The time when the last write to the log occured. **/
	private double lastSampleTime = 0;
	/** The exit of the room as recorded by a given waypoint. **/
	private Waypoint exit = null;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs the data logger given a file and the individuals to collect the data for.
	 *
	 * @param fileName The name of the file to store the collected data in. This should also have any directory info along with it.
	 **/
	public EnvDataLogger( String fileName )
	{
		super( fileName );
	}
	
	
	/**
	 * This initialized the timing information and construction the header (Field names).
	 **/
	public void init()
	{
		String[] headerSet = {"realTime","simTime","exitDensity","fluxAcrossDoorway","averageOrientation","averageVelocityX","averageVelocityY","averageVelocity","numberOfIndividualsInRoom","numberOfIndividualsMovingAwayFromDoor"};
		setHeader( headerSet );
		setHeaderWritten( false );
	}
	
	
	/**
	 * This is the area where the recording of the information actually occurs.
	 **/
	public void log()
	{
		super.log();
		
		CrowdSimulation state = CrowdSimulation.getInstance();
		double simTime = state.getSimTime();
		
        if( simTime-lastSampleTime > 1.0/sampleRate )
        {
			lastSampleTime = simTime;
			Bag individuals = state.getWorld().getAllObjects();

			for( int i =0; i < individuals.numObjs; i++ )
			{
				Individual ind = (Individual)individuals.get( i );

				// The StringBuilder class can be replaced by the StringBuffer class if needed.
				StringBuffer data = new StringBuffer();
				Date time = new Date();
				data.append( time.getHours()+"."+time.getMinutes()+"."+time.getSeconds() );
				data.append( "," );
				data.append( state.getSimTime() );
				data.append( "," );
				data.append( state.schedule.getSteps() );
				data.append( "," );
				data.append( ind.getID() );
				data.append( "," );
				data.append( ind.getDiameter() );
				data.append( "," );
				data.append( ind.getLocation().x );
				data.append( "," );
				data.append( ind.getLocation().y );
				data.append( "," );
				data.append( ind.getVelocityX() );
				data.append( "," );
				data.append( ind.getVelocityY() );

				write( data.toString() );
			}
		}
	}

	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the logger.
	 **/
	public void setParameters( Map parameters )
	{
		super.setParameters( parameters );

		if( parameters.containsKey( "sampleRate" ) )
		{
			Object value = parameters.get( "sampleRate" );
			double val = 0;
				
			if( value instanceof String )
			{
				val =  Double.parseDouble( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Double)value).doubleValue();
			}
			else
			{
				Log.log( 1, " SampleRate for the loger must be a Double or a string representing a Double.");
			}
			setSampleRate( val );
		}

	}
	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/** 
	 * Gets the sample rate for this logger.
	 *
	 * @return The sample rate for this logger.
	 **/
	public double getSampleRate()
	{
		return sampleRate;
	}
	
	/** 
	 * Sets the sample rate for this logger.
	 *
	 * @param val The sample rate for this logger.
	 **/
	public void setSampleRate( double val )
	{
		sampleRate = val;
	}

}
