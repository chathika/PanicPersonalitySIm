/*
 * $RCSfile: SimIndDataLogger.java,v $ $Date: 2009/05/14 14:38:58 $
 */
package crowdsimulation.logging;

import crowdsimulation.*;
import crowdsimulation.entities.individual.*;
import java.util.*;
import sim.util.*;
import crowdsimulation.actioncontroller.cellularautomata.strategy.*;
/**
 * The class prints out the data for a given timestep, for all individuals.
 * The data recorded is:
 *  1. realTime
 *  2. simTime
 *  3. stepNum
 *  4. individualID
 *  5. diameter
 *  6. X
 *  7. Y
 *  8. velocityX
 *  9. velocityY
 *  10. orientation
 *  11. pathID
 *  12. wayPointX
 *  13. wayPointY
 *  14. groupID
 *  15. initialVel
 *  16. color
 *  17. paths
 *  18. model
 *
 * @author $Author: ajolly $
 * @version $Revision: 1.2 $
 * $State: Exp $
 * $Date: 2009/05/14 14:38:58 $
 **/
public class SimIndDataLogger extends Logger
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The frequency (number of times per second) to write to the log. 
	 * One Sample every 1/smpleRate of a second. **/
	private double sampleRate = 0.3;
	/** The time when the last write to the log occured. **/
	private double lastSampleTime = 0;
	
	private double convert2hours = 3600;
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs the data logger given a file and the individuals to collect the data for.
	 *
	 * @param fileName The name of the file to store the collected data in. 
	 *   This should also have any directory info along with it.
	 **/
	public SimIndDataLogger( String fileName )
	{
		super( fileName );
	}
	
	
	/**
	 * This initialized the timing information and construction the header (Field names).
	 **/
	public void init()
	{
		super.init();
	
			String[] headerSet = {"realTime","simTime","stepNum","individualID","X","Y","pathID","numCollisions","R","G","B"};
	

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
		if( simTime >= CrowdSimulation.getInstance().getDuration() )
		{
			doLogging(simTime, state);
		}
                if( simTime-lastSampleTime >= sampleRate )
                {
		    doLogging(simTime, state);
		}
	}
	
	public void doLogging(double simTime, CrowdSimulation state)
	{
	        lastSampleTime = simTime;
			Bag individuals = state.getWorld().getAllObjects();
			
			for( int i =0; i < individuals.numObjs; i++ )
			{
                            Individual ind = (Individual)individuals.get( i );
				
				// The StringBuilder class can be replaced by the StringBuffer class if needed.
				StringBuffer data = new StringBuffer();
				Date time = new Date();
				data.append( System.currentTimeMillis() );
				data.append( "," );
				data.append( state.getSimTime() );
				data.append( "," );
				data.append( state.schedule.getSteps() );
				data.append( "," );
				data.append( ind.getID() );
				data.append( "," );
				data.append(( (FloorFieldCAStrategy)ind.getActionStrategy()).getLocation().x);
				data.append( "," );
				data.append(( (FloorFieldCAStrategy)ind.getActionStrategy()).getLocation().y);
				data.append( "," );
				if( ind.getSelectedPath()!= null )
				{
					data.append( ind.getSelectedPath().getID() );
				}
				data.append( "," );
				data.append( ((FloorFieldCAStrategy)ind.getActionStrategy()).numCollisions);
	      	    data.append( "," );
				data.append( ind.getColor().getRed() );
                data.append( "," );
				data.append( ind.getColor().getGreen() );
                data.append( "," );
				data.append( ind.getColor().getBlue() );
				write( data.toString() );
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
				Log.log( 1, " SampleRate for the loger must be a Double or a string representing a Double." );
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
