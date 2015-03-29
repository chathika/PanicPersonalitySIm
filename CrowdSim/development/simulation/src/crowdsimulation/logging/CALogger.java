/*
 * $RCSfile: CALogger.java,v $ $Date: 2008/10/27 16:27:46 $
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
 * The class prints out the HMFV Data for a given timestep, for all individuals.
 * This logger is based on the logger Helbing used in his c code.
 * The data recorded is :
 *  1. Step Number
 *  2. Simulation Time
 *  3. Number of people in the room
 *  4. The mean value of the system Efficiency
 *  5. The timestep used (Delta t)
 *
 * @author $Author: ajolly $
 * @version $Revision: 1.1 $
 * $State: Exp $
 * $Date: 2008/10/27 16:27:46 $
 **/
public class CALogger extends Logger
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The frequency (number of times per second) to write to the log. 
	 * One Sample every 1/smpleRate of a second. **/
	private double sampleRate = 29;
	/** The time when the last write to the log occured. **/
	private double lastSampleTime = 0;
	/** A local variable to tell how many people have exited the room.
	 * This is sepcifically designed for measures of individuals exiting a room. **/
	private double numberInRoom = 0;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs the data logger given a file and the individuals to collect the data for.
	 *
	 * @param fileName The name of the file to store the collected data in. 
	 *   This should also have any directory info along with it.
	 **/
	public CALogger( String fileName )
	{
		super( fileName );
	}
	
	
	/**
	 * This initialized the timing information and construction the header (Field names).
	 **/
	public void init()
	{
		String[] headerSet = {"stepNumber", "simTime", "numberInRoom", "<E>", "deltaT"};
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
				
				data.append( (state.getStepNum()-1) );
				data.append( "," );
				data.append( state.getSimTime() );
				data.append( "," );
				data.append( numberInRoom );
				data.append( "," );
				data.append( eMean( "", 0, 0 ) );
				data.append( "," );
				data.append( state.getDeltaT() );
					
				write( data.toString() );
			}
		}
	}
	
	//TODO: Need to implement this. This still is the code from the original helbing code, and is just commented out.
	/**
	 * calculates the mean value of the efficiency of the system for the last
	 *    few update steps -- NOTE: use this function only when UpdNum > 0 
	 *
	 *    if unfreq != 0, the average will be calculated for the last unfreq
	 *    updates (the present one included)
	 *    if unfreq == 0, the average will be calculated for the shortest
	 *    possible time interval exceeding stfreq
	 **/
	public double eMean( String sw, int unfreq, float stfreq ) 
	{
	  /*  */
/*	
		int i, start;
	  	float e_mean, f;
	
	
	  	if(strcmp(sw,"un")==0) 
	  	{ start = UpdNum - unfreq; }
	  	else // i.e. if(strcmp(sw,"st")==0)  
	  	{ 
	  		start = Mb; // start from beginning of present time window 
		  	f = floor( SimTime[UpdNum] / stfreq );
		  	while( f - floor( SimTime[start] / stfreq ) > 1.0 ) { start++; }
		  	if( start==UpdNum ) { start--; }
	  	}
	  	e_mean = 0.0;
	  	for(i=start+1; i<=UpdNum; i++) 
	  	{
	    	e_mean += E[i] * ( SimTime[i] - SimTime[i-1] );
	  	}	  
	  	e_mean /= SimTime[UpdNum] - SimTime[start];
	
	
	  	e_mean /= V0; 
	  	return e_mean;
*/
		return 0;
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
