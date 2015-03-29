/*
 * $RCSfile: DataLogger.java,v $ $Date: 2009/06/11 20:36:56 $
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
 * @author $Author: ganil $
 * @version $Revision: 1.2 $
 * $State: Exp $
 * $Date: 2009/06/11 20:36:56 $
 **/
public class DataLogger extends Logger
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The frequency (number of times per second) to write to the log. 
	 * One Sample every 1/smpleRate of a second. **/
	private double sampleRate = 29;
	/** The time when the last write to the log occured. **/
	private double lastSampleTime = 0;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs the data logger given a file and the individuals to collect the data for.
	 *
	 * @param fileName The name of the file to store the collected data in. 
	 *   This should also have any directory info along with it.
	 **/
	public DataLogger( String fileName )
	{
		super( fileName );
	}
	
	
	/**
	 * This initialized the timing information and construction the header (Field names).
	 **/
	public void init()
	{
		super.init();
		String[] headerSet = {"realTime","simTime","stepNum","individualID","diameter","X","Y","velocityX","velocityY","orientation","pathID","wayPointX","wayPointY","groupID","initialVel","color","paths","model"};
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
				data.append( System.currentTimeMillis() );
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
				data.append( "," );
				data.append( ind.getOrientation() );
				data.append( "," );
				if( ind.getSelectedPath()!= null )
				{
					data.append( ind.getSelectedPath().getID() );
				}
				data.append( "," );
				Path selPath = ind.getSelectedPath();
				if( selPath != null && selPath.getActiveWaypoint(ind) != null )
				{
					data.append( selPath.getActiveWaypoint(ind).getCenter().x );
					data.append( "," );
					data.append( selPath.getActiveWaypoint(ind).getCenter().y );
					data.append( "," );
				}
				else
				{
					data.append( ",," );
				}
				if( ind.getGroupId() != 0 )
				{
					data.append( ind.getGroupId() );
				}
				data.append( "," );
				data.append( ind.getInitialVelocity() );
				data.append( "," );
				data.append( ind.getColor().getRed() + " " + ind.getColor().getGreen() + " " + ind.getColor().getBlue() );
				data.append( "," );
				for( int j=0; j < ind.getInteractingPaths().size(); j++ )
				{
					Path path = (Path)ind.getInteractingPaths().get(j);
					data.append( path.getID() );
					if( j != ind.getInteractingPaths().size()-1 )
					{
						data.append( " " );
					}
				}
				data.append( "," );
				data.append( ind.getActionController().getClass().getSimpleName());
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
