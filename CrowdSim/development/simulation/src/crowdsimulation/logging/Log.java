/*
 * $RCSfile: Log.java,v $ $Date: 2008/10/16 17:45:25 $
 */
package crowdsimulation.logging;

import crowdsimulation.actioncontroller.*;	// Contains models of behavior for the entities. 
import crowdsimulation.entities.*;		// Contains definition of the crowd agents and features.
import java.io.*;
import java.util.*;
import sim.engine.*;
import sim.util.*;

/**
 * The class generates a generalized logging system to report things to an output file, and/or the command line.
 *
 * @author $Author: ganil $
 * @version $Revision: 1.2 $
 * $State: Exp $
 * $Date: 2008/10/16 17:45:25 $
 **/
public class Log
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	private static String consoleLogFileName;

	/** The writer used to write to the file. **/
	private PrintWriter writer;
	
	/** The level of logging to be used in the system. **/
	private int logLevel = 0;
	
	/** Should the log go to the console? **/
	private boolean consoleOutput = true;
	/** Should the log go to a file? **/
	private boolean fileOutput = true;
	
	/** The place holder for the instance of the logger. **/
	private static Log instance = null;
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Constructs a data log.
	 * 0 - No logging
	 * 1 - Critical Messages
	 * 2 - Serious Messages
	 * 3 - Error Reporting
	 * 4 - Warnings
	 * 5 - Default
	 * 6 - Detailed Message Reporting
	 * 7 - Entry & Exit
	 * 8 - Debug
	 * 9 - Verbose ( Everything )
	 *
	 * @param fileName The name of the file to store the collected data in. This should also have any directory info along with it.
	 **/
	public Log( String fileName )
	{
		try
		{
			File file = new File( fileName );
			file.getParentFile().mkdirs();
			writer = new PrintWriter( new FileOutputStream( fileName ) );
		}
		catch( IOException ioe )
		{
			ioe.printStackTrace();
		}
		
		if( instance == null )
		{
			instance = this;
		}
	}
	
	/**
	 * Write the information to the log. This will write the log information to 
	 *   possibly both the system console and a logfile.
	 *
	 * @param level The level of the logging that should be done.
	 * @param data The information to be written to the log.
	 **/
	public static void log( int level, String data )
	{
		instance.logData( level, data );
	}
	
	/**
	 * Write the stacktrace of an exception to the log. This will write the exception 
	 *    information to possibly both the system console and a logfile.
	 *
	 * @param level The level of the logging that should be done.
	 * @param data The exception information to be written to the log.
	 **/
	public static void log( int level, Exception data )
	{
		instance.logData( level, data );
	}
	
	/**
	 * Write the information to the log. This will write the log information to 
	 *   possibly both the system console and a logfile.
	 *
	 * @param level The level of the logging that should be done.
	 * @param data The information to be written to the log.
	 **/
	public void logData( int level, String data )
	{
		if( level <= logLevel )
		{
			if( consoleOutput )
			{
				System.out.println( data );
			}
			if( fileOutput )
			{
				writer.println( data );
				writer.flush();
			}
		}
	}
	
	/**
	 * Write the stacktrace of an exception to the log. This will write the exception 
	 *    information to possibly both the system console and a logfile.
	 *
	 * @param level The level of the logging that should be done.
	 * @param data The exception information to be written to the log.
	 **/
	public void logData( int level, Exception data )
	{
		if( level <= logLevel )
		{
			if( consoleOutput )
			{
				data.printStackTrace();
			}
			if( fileOutput )
			{
				data.printStackTrace( writer );
				writer.flush();
			}
		}
	}
	
	
	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Sets the level of logging.
	 * This allows the depth and detail of the logging to be changed.
	 *
	 * @param val The level to set the logging to.
	 **/
	public void setLogLevel( int val )
	{
		logLevel = val; 
	}
	
	/**
	 * Gets the level of logging.
	 * This allows the depth and detail of the logging to be changed.
	 *
	 * @return The level to set the logging to.
	 **/
	public int getLogLevel()
	{
		return logLevel;
	}
	
	/**
	 * Sets if the log should write to the console.
	 *
	 * @param val The boolean representing if the log should be written to the console.
	 **/
	public void setConsoleOutput( boolean val )
	{
		consoleOutput = val;
	}
	
	/**
	 * Gets if the log should write to the console.
	 *
	 * @return The boolean representing if the log should be written to the console.
	 **/
	public boolean getConsoleOutput()
	{
		return consoleOutput;
	}
	
	/**
	 * Sets if the log should write to a file.
	 *
	 * @param val The boolean representing if the log should be written to the a file.
	 **/
	public void setFileOutput( boolean val )
	{
		fileOutput = val;
	}
	
	/**
	 * Gets if the log should write to a file.
	 *
	 * @return The boolean representing if the log should be written to the a file.
	 **/
	public boolean getFileOutput()
	{
		return fileOutput;
	}

	/**
	 * Allows for anyone to be able to get a reference to this log.
	 * This is an implementation of the SINGLETON pattern.
	 *
	 * @return The instance of the log which individuals should be using.
	 **/
	public static Log getInstance()
	{
		return instance;
	}

	/** Allows setting of the default consoleLogFileName. Used only by outside functions.
	 */
	public static void setConsoleLogFileName(String fileName)
	{
		Log.consoleLogFileName = fileName;
	}

	public static String getConsoleLogFileName()
	{
		return Log.consoleLogFileName;
	}


}
