/*
 * $RCSfile: DataAnalyzer.java,v $ $Date: 2008/04/22 23:54:09 $
 */
package crowdsimulation.dataanalysis;

import crowdsimulation.*;
import crowdsimulation.entities.*;
import java.io.*;
import math.*;
import sim.util.*;

/**
 * This is a base class for reads in a dataset from the simulation loggers.
 * This is helpfull when trying to summarize the data or condense the log to 
 * be run through other analyzer/statistical software/packages like the
 * outlier analysis. This also addes the features for writeing to data files also.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.3 $
 * $State: Exp $
 * $Date: 2008/04/22 23:54:09 $
 **/
public abstract class DataAnalyzer extends CSVAnalysis
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** Hold the header which is read off of the datafile. **/
	protected Object[] header;
	/** Data from the current line of the file being read from. **/
	protected Object[] currentLine;
	/** Collection of the data to average or summarize over. **/
	protected Bag dataToAverage = new Bag();
	/** The writer being used to write out to the new file. **/
	protected BufferedWriter writer;
	/** The stepnumber for the data which is in dataToAverage. **/
	public int stepNum=0;

	/** The filename of the file to output the data to. **/
	String outputFileName = "";
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/** 
	 * Constructs a data analyzer with an configuration filename, an input filename, and an output filename.
	 *
	 * @param configFilename The name of the configuration file which was used to generate the simualtion data being analyzed.
	 * @param dataFileName The name of the data file which is to be analyzed.
	 * @param outputFN The filename for the output of the analyzer.
	 **/
	public DataAnalyzer( String configFilename, String dataFileName, String outputFN )
	{
		super( dataFileName );

		new CrowdSimulation( false, System.currentTimeMillis(), configFilename );
		
		outputFileName = outputFN;

		initialize();
	}
	
	/**
	 * This sets up reading from the input file and writing to the output file.
	 * First it reads in the header then reads in the first line of the file. 
	 * Then it sets up the file writer for output.
	 **/
	public void initialize()
	{
		header = nextLine();
		
		currentLine = nextLine();
		
		try
		{
			FileWriter fw = new FileWriter( outputFileName );
			writer = new BufferedWriter( fw );
		}
		catch( Exception e )
		{
			System.out.println( "Couldn't load the file " + outputFileName + "!" );
			System.exit( 1 );
		}
	}
	
	
	/**
	 * This gathers all of the data for a given stepnumber and places it in data to average.
	 * 
	 * @param elementID This needs to tell which element ID out of a data row is the timestep.
	 **/
	public void getNextDataSet( int elementID )
	{
		dataToAverage.clear();
		
		stepNum = Integer.parseInt( (String)currentLine[elementID] );
					
		while( currentLine != null && 
		       Integer.parseInt( (String)currentLine[elementID] ) == stepNum )
		{
			dataToAverage.add( currentLine );
			currentLine = nextLine();
		}
	}
	
	
	/**
	 * This write a string to the output file.
	 *
	 * @param data The string to be written to the output file.
	 **/
	public void writeToFile( String data )
	{
		try
		{
			writer.write( data + " \n" );
			writer.flush();
		}
		catch( Exception e )
		{
			System.out.println( "Couldn't write to the file " + outputFileName + "!" );
			System.exit( 1 );
		}
	}
	
	/**
	 * This write an array of objects to the output file.
	 *
	 * @param data The array of objects to be written to the output file.
	 **/
	public void writeToFile( Object[] data )
	{
		writeToFile( writeElement( data ) );
	}

	/**
	 * This is where the actaul analysis of the file needs to take place.
	 * This must be implemented in the subclass.
	 **/
	public abstract void analyze();

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
	
}
