/*
 * $RCSfile: Test.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.tests;

import crowdsimulation.logging.*;
import math.*;

/**
 * Base Class For Testing features of the simulation Framework.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class Test 
{

////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The allowable error when running the test. **/
	double ERROR = 0.00001;
	/** The name of the scenario being run. **/
	String scenarioName;
	/** The name of the method being run. **/
	String methodName;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Exectues the tests for the crowdSimulation System.
	 *
	 * @param args
	 */
	public static void main(String[] args) 
	{
		Test tests = new Test();
		
        Log systemLog = new Log( "../logs/SystemTest.dat" );

		tests.executeScenarios();
	}
	
	/**
	 * Constructs a test to be run on the simualtion. 
	 **/
	public Test()
	{
	}
	
	/**
	 * Runs a test on the simulation.
	 * This base method writes out the header and footer, where the footer executes the simualtion for the test.
	 **/
	public void runTest()
	{
		testHeader(  );
		testFooter( executeScenarios() );
	}
	
	
	/**
	 * The execution of the scenario runs a scenario and returns tru if the scenarios pass.
	 * This is a default scenario and doesn't do anything so it jsut returns true.
	 **/
	public boolean executeScenarios()
	{
		return true;
	}

	
	/** 
	 * A comparison between two vector to see if they are similar.
	 *
	 * @param a The first vector for the comparison.
	 * @param b The second vector for the comparison.
	 * @return The boolean representing if the two vectors are close enough to be considered the same.
	 **/
	public boolean approximatelyEqual( Vector2D a, Vector2D b )
	{
		return ( Math.abs(a.x - b.x) < ERROR && Math.abs(a.y - b.y) < ERROR );
	}
	
	
	/** 
	 * A comparison between two doubles to see if they are similar.
	 *
	 * @param a The first souble for the comparison.
	 * @param b The second double for the comparison.
	 * @return The boolean representing if the two doubles are close enough to be considered the same.
	 **/
	public boolean approximatelyEqual( double a, double b )
	{
		return Math.abs(a - b) < ERROR;
	}
	
	
	/** 
	 * A comparison between two floats to see if they are similar.
	 *
	 * @param a The first float for the comparison.
	 * @param b The second float for the comparison.
	 * @return The boolean representing if the two floats are close enough to be considered the same.
	 **/
	public boolean approximatelyEqual( float a, float b )
	{
		return Math.abs(a - b) < ERROR;
	}
	
	
	/** 
	 * A comparison between two ints to see if they are similar.
	 * This is not necessary, but was implemented to match the other primitive number types.
	 *
	 * @param a The first int for the comparison.
	 * @param b The second int for the comparison.
	 * @return The boolean representing if the two ints are close enough to be considered the same.
	 **/
	public boolean approximatelyEqual( int a, int b )
	{
		return Math.abs(a - b) < ERROR;
	}
	
	
	/**
	 * Converts a boolean represnetation of passing to a string to be displayed.
	 *
	 * @return The string containing if the boolean was representing a passing or not.
	 **/
	public String result( boolean pass )
	{
		if( pass )
		{
			return "PASS";
		}
		return "***FAIL***";
	}
	
	
	/**
	 * This writes a string out to through the logging mechanism, so it can be command line and/or a file.
	 * A shortcut to write to the log faster, calling Log.log(). 
	 * 
	 * @param data The data to be written out to the log.
	 **/
	public void log( String data )
	{
		Log.getInstance().log( 0, data );
	}
	
	
	/**
	 * Prints the header out to the log.
	 **/
	public void testHeader()
	{
		log( "*****************************************************************" );
		log( "TEST:"+getClass().getName() );
		log( "" );
	}
	
	
	/**
	 * Prints out the footer for a test.
	 *
	 * @param pass This is the boolean which shows if the test was passed or not.
	 **/
	public void testFooter( boolean pass )
	{
		log( "" );
		log( result(pass) + "::TEST:"+getClass().getName() );
		log( "*****************************************************************" );
		log( "" );
		log( "" );
	}
	
	
	/**
	 * This prints a scenario header to the log.
	 **/
	public void scenarioHeader()
	{
		log( "   ##############################################################" );
		log( "   SCENARIO:" + scenarioName );
	}
	
	
	/**
	 * This prints a scenario footer to the log.
	 *
	 * @param pass This is the boolean which shows if the test was passed or not.
	 **/
	public void scenarioFooter( boolean pass )
	{
		log( "   " + result( pass ) + "::SCENARIO:" + scenarioName );
		log( "   ##############################################################" );
		log( "" );
	}
	
	
	/**
	 * This prints a method header to the log.
	 **/
	public void methodHeader()
	{
		log( "      -----------------------------------------------------------" );
	}
	
	
	/**
	 * This prints a method footer to the log.
	 **/
	public void methodFooter()
	{
		log( "      -----------------------------------------------------------" );
	}
}
