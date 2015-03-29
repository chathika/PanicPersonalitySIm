/*
 * $RCSfile: SalamanderIndividualLogger.java,v $ $Date: 2009/07/24 17:29:23 $
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
 * @author $Author: ajolly $
 * @version $Revision: 1.1 $
 * $State: Exp $
 * $Date: 2009/07/24 17:29:23 $
 **/


public class SalamanderIndividualLogger extends Logger
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The writer used to write individuals data to the file. **/
	private PrintWriter writer;
	/** The name of the file to write the data to. **/
	private String fileName;

	/** The header for the file. This should contain the column names.**/
	private String[] header;
	/** The number of columns of data which are to be written to the file. **/
	private int columns = 1;
	/** A holder for a collection of data which needs to be written.**/
	private Object[] data;
	/** A holder for a row of data which needs to be written to the file. **/
	private Object[] row;
	/** The delimeter to use when writting a row of data. **/
	private String delimeter = ",";
	/** Lets the object know if the header has been written the the file. This is
	 *   also a way of telling if the initialization of the logger has been done.
	 **/
	private boolean headerWritten;

    private boolean initialized = false;

    HashMap SalamanderLogs = new HashMap();


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
	public SalamanderIndividualLogger( String fileName )
	{
		super( fileName );
        this.fileName = fileName;
	}

    /**
	 * This initialized the timing information and construction the header (Field names).
	 **/
	public void init()
	{
		super.init();
	}
		
	/**
	 * This initialized the timing information and construction the header (Field names).
	 *
     **/
    public void init1() {


        CrowdSimulation state = CrowdSimulation.getInstance();

        Bag individuals = state.getWorld().getAllObjects();

        String headerSet = "simTime, stepNum, individualID, size, diameter, hiding, substrate, cover, Y, X, velocityY, velocityX, randomX, randomY, coverMemoryX, coverMemoryY, foodSourceX, foodSourceY, foodX, foodY, hidingX, hidingY, othersX, othersY, regionX, regionY, waterX, waterY";

        for (int i = 0; i < individuals.numObjs; i++) {
            Individual ind = (Individual) individuals.get(i);
            String aFilename = fileName + "_" + ind.getID() + ".csv";
            if (ind.getActionStrategy() instanceof SalamanderStrategy) {
                try {
                    File file = new File(aFilename);
                    file.getParentFile().mkdirs();
                    writer = new PrintWriter(new FileOutputStream(aFilename));
                    HashMap SalamanderInd = new HashMap();
                    SalamanderInd.put("headWritten", false);
                    SalamanderInd.put("header", headerSet);
                    SalamanderInd.put("writer", writer);
                    SalamanderLogs.put(ind.getID(), SalamanderInd);
                } catch (IOException ioe) {
                    System.err.println("Could not create log file " + aFilename);
                    ioe.printStackTrace();
                }
            }
        }


    }

	/**
	 * This is the area where the recording of the information actually occurs.
	 **/

	public void log( )
	{
       //check to see if initialized
        if(!initialized){
            init1();
            initialized = true;
        }

        CrowdSimulation state = CrowdSimulation.getInstance();
		double simTime = state.getSimTime();

		Bag individuals = state.getWorld().getAllObjects();

        //This writes all the headers
        for( int j =0; j < individuals.numObjs; j++ )
		{
            Individual ind = (Individual) individuals.get(j);
            if (ind.getActionStrategy() instanceof SalamanderStrategy) {
                HashMap aLog = (HashMap) SalamanderLogs.get(ind.getID());
                if (!(Boolean.valueOf(aLog.get("headWritten").toString()))) {
                    PrintWriter aWriter = (PrintWriter) aLog.get("writer");
                    aWriter.println(aLog.get("header").toString());
                    aWriter.flush();
                    aLog.put("headWritten", true);
                }
            }
        }


				
		for( int i =0; i < individuals.numObjs; i++ )
		{
			Individual ind = (Individual)individuals.get( i );
			SalamanderStrategy strat = null;
			if( ind.getActionStrategy() instanceof SalamanderStrategy )
			{
				strat = (SalamanderStrategy)ind.getActionStrategy();
	            HashMap aLog = (HashMap)SalamanderLogs.get(ind.getID());
                PrintWriter aWriter = (PrintWriter)aLog.get("writer");
				// The StringBuilder class can be replaced by the StringBuffer class if needed.
				StringBuffer data = new StringBuffer();
				data.append( state.getSimTime() );
				data.append( "," );
				data.append( state.schedule.getSteps() );
				data.append( "," );
				data.append( ind.getID() );
				data.append( "," );
                data.append( strat.getSize() );
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
				data.append( "," );
                data.append( ind.getForceValues() );

				aWriter.println( data.toString() );
                aWriter.flush();
			}
		}
    }
	/**
	 * This writes a string to the datafile. Each string is written as a complete line.
	 *
	 * @param data The string to be written to the datafile.
	 **/
	public void write( String data )
	{
		writer.println( data.toString() );
		writer.flush();
	}


	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the logger.
	 **/
	public void setParameters( Map parameters )
	{

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

	/**
	 * Gets the header for the logger.
	 *
	 * @return The array of string for the header of the datafile.
	 **/
	String[] getHeader()
	{
		return header;
	}

	/**
	 * Sets the header for the logger.
	 *
	 * @param val The array of string for the header of the datafile.
	 **/
	void setHeader( String[] val )
	{
		header = val;
	}

	/**
	 * Gets if the header has been written.
	 *
	 * @return The boolean representing if the header has been written.
	 **/
	boolean getHeaderWritten()
	{
		return headerWritten;
	}

	/**
	 * Gets if the header has been written.
	 *
	 * @param val The boolean representing if the header has been written.
	 **/
	void setHeaderWritten( boolean val )
	{
		headerWritten = val;
	}

}
