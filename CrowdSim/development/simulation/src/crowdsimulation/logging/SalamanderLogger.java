/*
 * $RCSfile: SalamanderLogger.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.logging;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;	// Contains models of behavior for the entities. 
import crowdsimulation.actioncontroller.strategy.*;
import crowdsimulation.entities.*;		// Contains definition of the crowd agents and features.
import crowdsimulation.entities.individual.*;
import java.io.*;
import java.util.*;
import sim.engine.*;
import sim.util.*;

/**
 * The class prints out the data for a given timestep, for all salamanders.
 * This logger was written to try to closely reproduce the data that was collected
 * by Dr. Fauth on different salamanders placed along a stream bed.
 * Data captured is:
 *   1. real time
 *   2. sim time
 *   3. step nummber
 *   4. id of the salamander
 *   5. diameter of the salamander
 *   6. if the salamander was hiding
 *   7. what substrate the salamander is on
 *   8. The type of cover the salamander is under (only if it is hiding)
 *   9. y location
 *   10. x location
 *   11. velocity in y
 *   12. velocity in x      
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class SalamanderLogger extends Logger
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs the salamander logger given a file name.
	 * This constructs a Logger to collect data from the salamander experiment
	 * Trying to reproduce Dr. Fauths data as closely as possible.
	 *
	 * @param fileName The name of the file to store the collected data in. 
	 *   This should also have any directory info along with it.
	 **/
	public SalamanderLogger( String fileName )
	{
		super( fileName );
	}
		
	/**
	 * This initialized the timing information and construction the header (Field names).
	 **/
	public void init()
	{
		super.init();
		String[] headerSet = {"realTime","simTime","stepNum","individualID","diameter","hiding","substrate","cover","Y","X","velocityY","velocityX"};
		setHeader( headerSet );
		setHeaderWritten( false );
	}
		
	/**
	 * This is the area where the recording of the information actually occurs.
	 **/
	public void log( )
	{
		super.log();
		
		CrowdSimulation state = CrowdSimulation.getInstance();
		double simTime = state.getSimTime();
		
		Bag individuals = state.getWorld().getAllObjects();
				
		for( int i =0; i < individuals.numObjs; i++ )
		{
			Individual ind = (Individual)individuals.get( i );
			SalamanderStrategy strat = null;
			if( ind.getActionStrategy() instanceof SalamanderStrategy )
			{
				strat = (SalamanderStrategy)ind.getActionStrategy();
	
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
				data.append( strat.getIsHiding() );
				data.append( "," );
				data.append( getSubstrate( ind.getLocation().y ) );
				data.append( "," );
				data.append( strat.getCoverType() );
				data.append( "," );
				data.append( ind.getLocation().y );
				data.append( "," );
				data.append( ind.getLocation().x );
				data.append( "," );
				data.append( ind.getVelocityY() );
				data.append( "," );
				data.append( ind.getVelocityX() );
					
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
	}

	/**
	 * Determines what substrate was at a given location.
	 * In the experiment the substrates varied with the distance from the stream,
	 * which directly corresponds to the distance in the y direction.
	 *
	 * @param y The distance in the y direction.
	 * @return The string describing the substrate at a given distance.
	 **/
	private String getSubstrate( double y )
	{
		if( y < 6.1 )
		{
			return "cobbles";
		}
		if( y < 12.2 )
		{
			return "gravel";
		}
		if( y < 18.3 )
		{
			return "sand";
		}
		if( y < 2.44 )
		{
			return "bare soil";
		}
		return "leaf litter";
	}	
		
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

}
