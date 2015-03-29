/*
 * $RCSfile: CSVAnalysis.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.dataanalysis;

import sim.util.*;
import java.io.*;
import java.util.*;

/**
 * This is a base class for file analysis and reformating, and reads in a CSV file.
 * This is a class designed to assist with reading in CSV files and getting to
 * the data elements in each row easily.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class CSVAnalysis
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The name of the file to read in. **/
	private String fileName;
	/** The reader used to read the data from the file. **/
	private BufferedReader reader;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/** 
	 * Constructs a CSVAnalysis and prepares it to read data from the file with the filename passed in.
	 *
	 * @param filename The name of the file to read the data from.
	 **/
	public CSVAnalysis( String filename )
	{
		fileName = filename;
	
		reset();
	}

	
	/**
	 * Sets the reader back at the beginning of the file, and should clear any stored data in the class.
	 **/
	public void reset()
	{
		try
		{
			FileReader fr = new FileReader( fileName );
			reader = new BufferedReader( fr );
		}
		catch( Exception e )
		{
			System.out.println( "Couldn't load the file " + fileName + "!" );
			System.exit( 1 );
		}
	}

	
	/**
	 * Reads in the data from the next line of the file. 
	 * When a file is initialized the pointer into the file is set right before the first line.
	 *
	 * @return An array of objects containing the data from the line.
	 *  Each element in the array is what was between the commas on the line read in.
	 **/
	public Object[] nextLine()
	{
		try
		{
			String lineData = reader.readLine();
			if( lineData != null )
			{
				StringTokenizer st = new StringTokenizer( lineData, "," );
				String[] result = lineData.split( "," );
	     	
				return result;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	/**
	 * Writes out an array of objects into a string with a comma as a serperator between elements.
	 *
	 * @param elements The collection of elements to be printed to a string.
	 * @return The string containing all the elements seperated by a comma.
	 **/
	public String writeElement( Object[] elements )
	{
		return writeElement( elements, "," );
	}
	
	/**
	 * Writes out an array of objects into a string with a serperator between elements.
	 *
	 * @param elements The collection of elements to be printed to a string.
	 * @param seperator The seperate to use in between each of the elements in the string.
	 * @return The string containing all the elements seperated by the designated seperator.
	 **/
	public String writeElement( Object[] elements, String seperator )
	{
		String data = "";
		for( int i =0; i < elements.length; i++)
		{
			data += elements[i] + seperator;
		}
		return data;
	}


////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Sets the fileName to be used by the class.
	 *
	 * @param val The filename being used by the class.
	 **/
	public void setFileName( String val )
	{
		fileName = val;
	}
	
	/**
	 * Gets the fileName to be used by the class.
	 *
	 * @return The filename being used by the class.
	 **/
	public String getFileName()
	{
		return fileName;
	}

}