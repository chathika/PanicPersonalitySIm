/*
 * $RCSfile: Logger.java,v $ $Date: 2008/10/16 17:45:25 $
 */
package crowdsimulation.logging;

import java.io.*;
import java.util.*;

/**
 * Base Class For Different Loggers. This contains the structures
 * to open a file and write to it.
 *
 * @author $Author: ganil $
 * @version $Revision: 1.2 $
 * $State: Exp $
 * $Date: 2008/10/16 17:45:25 $
 **/
public class Logger
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
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Constructs the logger given a file and the individuals to collect the data for.
	 *
	 * @param file The name of the file to store the collected data in. 
	 *  This should also have any directory info along with it.
	 **/
	public Logger( String file )
	{
		fileName = file; 
		init();
	}
	
	
	/**
	 * This initialized the timing information and construction the header (Field names)along with the file reference.
	 **/
	public void init()
	{
		headerWritten = false;
		header = new String[columns];
		data = new Object[columns];
		row = new Object[columns];
		setHeaderWritten( false );
			
		try
		{
			File file = new File( fileName );
			file.getParentFile().mkdirs();
			writer = new PrintWriter( new FileOutputStream( fileName ) );
		}
		catch( IOException ioe )
		{
			System.err.println("Could not create log file " + fileName);
			ioe.printStackTrace();
		}
	}
	
	
	/**
	 * This is the area where the recording of the information actually occurs.
	 **/
	public void log()
	{
		if( !getHeaderWritten() )
		{
			write( header );
			setHeaderWritten( true );
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
	 * This writes a array of objects to the datafile. 
	 * The array is written as a complete line.
	 * 
	 * @param data The Object array to be written to the datafile.
	 **/
	public void write( Object[] data )
	{   
		row = data;

		writeRow();
	}
	
	/**
	 * This writes the data currently stored in the object attribute row to the datafile. 
	 **/
	public void writeRow()
	{
		writer.print( row[0].toString() );
		for( int i = 1; i < row.length; i++ )
		{
			writer.print( delimeter+row[i].toString() );
		}
		writer.println( "" );
		writer.flush();
	}

	/**
	 * Method whic allows for setting parameters on the logger by a map of parameters.
	 * This method is implemented in the subclasses, but is not really necessary for a
	 * logger.
	 *
	 * @param parameters The map of parameters to set the attributes in this object.
	 **/	
	public void setParameters( Map parameters )
	{
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
