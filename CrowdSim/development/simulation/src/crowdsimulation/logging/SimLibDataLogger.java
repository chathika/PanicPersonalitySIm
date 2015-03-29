/*
 * $RCSfile: SimLibDataLogger.java,v $ $Date: 2008/10/27 16:27:46 $
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
import crowdsimulation.actioncontroller.cellularautomata.*;
import crowdsimulation.actioncontroller.cellularautomata.strategy.*;
import crowdsimulation.entities.obstacle.*;

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
 * @version $Revision: 1.1 $
 * $State: Exp $
 * $Date: 2008/10/27 16:27:46 $
 **/
public class SimLibDataLogger extends Logger
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The frequency (number of times per second) to write to the log. 
	 * One Sample every 1/smpleRate of a second. **/
	private double sampleRate = 0.000277777777777778;
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
	public SimLibDataLogger( String fileName )
	{
		super( fileName );
	}
	
	
	/**
	 * This initialized the timing information and construction the header (Field names).
	 **/
	public void init()
	{
		super.init();
	
//		String[] headerSet = {"simTime","stepNum","individualID","X","Y","PathID","inQueue","AttachedtoStation","StationID","wayPointX"};
//	
//
//		setHeader( headerSet );
//		setHeaderWritten( false );
	}
	
	public void createHeader(SimLog theLog)
	{
		Bag headerStuff = new Bag();
		headerStuff.add("RealTime");
		headerStuff.add("SimTime");
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 5; j++)
			{
				StringBuffer data = new StringBuffer();
				data.append("AveMST_"+i+"_"+j + ",NumSamplesMST_"+i+"_"+j);
				headerStuff.add(data.toString());
			}
		}
		for(int i = 0; i < 3; i++)
		{
			StringBuffer data = new StringBuffer();
			data.append("AveDelayJob_"+i+",NumSamplesDelayJob_"+i);
			headerStuff.add(data.toString());
		}
		
		for(int i = 0; i < 5; i++)
		{
			StringBuffer data = new StringBuffer();
			data.append("AveNumQueueWS_"+i+",TotNumQueueWS_"+i);
			headerStuff.add(data.toString());
		}
		
		for(int i = 0; i < 5; i++)
		{
			StringBuffer data = new StringBuffer();
			data.append("AveDelayWS_"+i+",NumSamplesDelayWS_"+i);
			headerStuff.add(data.toString());
		}
		
		String[] headerSet = new String[headerStuff.size()];
		for(int i = 0; i < headerStuff.size(); i++)
		{
			headerSet[i] = headerStuff.get(i).toString();
		}
		
		write(headerSet);
		setHeaderWritten(true);
	}
	/**
	 * This is the area where the recording of the information actually occurs.
	 **/
	public void log()
	{
		
		CrowdSimulation state = CrowdSimulation.getInstance();
		double simTime = state.getSimTime();
		
		//Log.log(1,"simTime-lastSampleTime" + (simTime-lastSampleTime) + " check " + (1.0/sampleRate));
        if( (simTime-lastSampleTime > 1.0/sampleRate) || ((state.getDuration() - simTime )  <1) )
        {
	    	System.gc();
	    	lastSampleTime = simTime;
			SimLog theLog = state.getUpdateStats();
			if( !getHeaderWritten() )
			{
				createHeader(theLog);
			}
		
		
			// The StringBuilder class can be replaced by the StringBuffer class if needed.
			StringBuffer data = new StringBuffer();
			data.append( System.currentTimeMillis() );
			data.append( "," );
			data.append( simTime );
			data.append( "," );
			for(int i = 0; i < 3; i++)
			{
				for(int j = 0; j < 5; j++)
				{
					try
					{
						data.append(theLog.getTotalServiceTime_Job_Ws(i,j)/(theLog.numSampleServiceTime_job_ws[i][j]*convert2hours) + ",");
						data.append(theLog.numSampleServiceTime_job_ws[i][j] + ",");
					}
					catch( Exception e )
					{
						data.append("Spacer,");
						data.append("Num Sample Service Time Samples: " + theLog.numSampleServiceTime_job_ws[i][j] + ",");
					}
				}
			}
			
			for(int i = 0; i < 3; i++)
			{
				try
				{
					data.append(theLog.getTotalQueueDelayJob(i)/(theLog.numSampleDelayJob[i]*convert2hours) + ",");
					data.append(theLog.numSampleDelayJob[i] + ",");
				}
				catch( Exception e )
				{
					data.append("Spacer,");
					data.append("Num Sample Delay Time Samples: " + theLog.numSampleDelayJob[i] + ",");
				}
			}
			
			for(int i = 0; i < 5; i++)
			{
				try
				{
					data.append(theLog.getTotalNumInWsQueue(i)/simTime + ",");
					data.append(theLog.getTotalNumInWsQueue(i) + ",");
				}
				catch( Exception e )
				{
					data.append("Spacer,");
				}
			}
			
			for(int i = 0; i < 5; i++)
			{
				try
				{
					data.append(theLog.getTotalQueueDelayWS(i)/(theLog.numSampleDelayWS[i]*convert2hours) + ",");
					data.append(theLog.numSampleDelayWS[i] + ",");
				}
				catch( Exception e )
				{
					data.append("Spacer,");
					data.append("Num in Delay WS Samples: " + theLog.numSampleDelayWS[i] + ",");
				}
			}
			
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
	public Bag GetWorkstations(CrowdSimulation state)
	{
  		Bag theStations = new Bag();
  		Object[] obstacles = state.getTerrain().getAllObjects().toArray();         
		for(int j = 0;j < obstacles.length;j++)
		{
			Obstacle aObstacle = (Obstacle)obstacles[j];
			if(aObstacle instanceof StationObstacle)
			{
					StationObstacle theStation = (StationObstacle)aObstacle;
					theStations.add(theStation);
			
			}
		}
		return theStations;
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
