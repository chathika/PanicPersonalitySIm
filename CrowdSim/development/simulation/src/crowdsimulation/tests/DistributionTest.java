/*
 * $RCSfile: DistributionTest.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.tests;

import crowdsimulation.logging.*;
import math.*;

/**
 * Class for testing the different distributions available in the math package.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class DistributionTest extends Test
{

////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
	
	/** The normal distribution used for testing some fo the generators. **/
	NormalDistributedGenerator generator;
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Exectues the Distributions tests for the crowdSimulation System.
	 *
	 * @param args
	 */
	public static void main(String[] args) 
	{
		DistributionTest tests = new DistributionTest();
        Log systemLog = new Log( "../logs/SystemTest.dat" );
        systemLog.setFileOutput( true );
        systemLog.setLogLevel(0);
        
        tests.runTest();
	}
	
	
	/**
	 * Default constructor for the DistributionTest, and sets up the generator for the tests.
	 **/
	public DistributionTest()
	{
		generator = new NormalDistributedGenerator();
	}
	
	
	/**
	 * This executes the different scenarios for the test.
	 * The tests being run are:
	 *   1. gaussRandTests()
	 **/
	public boolean executeScenarios()
	{
		boolean pass = true;
		
		pass = pass && gaussRandTests();
		
		return pass;		
	}


	/**
	 * Executes a series of tests for the Gaussian distributions. 
	 **/
	public boolean gaussRandTests()
	{
		scenarioName = "RandTests";
		boolean pass = true;
		double randMax = 100;
		double mean = 50;
		double stdDev = 9;
		double sigmaSq = 4;
		double cutmult = 3;
		int times = 10000;
		double probability = 0.65;
		int trials = 10;
		int eventNumber = 4;
		double min = 2;
		double max = 10;
		

//scenario parameters
		scenarioHeader();
		log( "" );
		
////// Method testing segment		
		pass = pass && gaussRand( times, mean, sigmaSq, cutmult );
		pass = pass && gasdev( times, mean, stdDev );
		pass = pass && binomial( times, probability, trials );
		pass = pass && exponential( times );
		pass = pass && gamma( times, eventNumber );
		pass = pass && poisson( times, mean );
		pass = pass && uniform( times, min, max );
		
		
//end Method Testing section
		scenarioFooter( pass );
		return pass;
	}


	/**
	 * Prints out data from a gaussian Random Number test.
	 * This generates a bunch of test data and prints it to the screen.
	 * This will never fail since it doesn not automatically check the test.
	 * The idea is to run the output of this into a stats package and have it tell you 
	 * what the distribution looks like. 
	 *
	 * @param times The number of test values to generate.
	 * @param gmean The mean for the test values.
	 * @param gtheta The theta for the distribution.
	 * @param gcutmult The multiplier to be used for the distribtuion.
	 * @return If the test is passes, this will always be true for thise test.
	 **/
	public boolean gaussRand( int times, double gmean, double gtheta, double gcutmult )
	{
		methodHeader();
		log( "      gaussRand(  " + gmean + ", " + gtheta + ", " + gcutmult + " ) " );
		log( "         times = " + times );
		log( "" );
		
		for( int i = 0; i < times; i++ )
		{
			log( ""+generator.gaussRand( gmean, gtheta, gcutmult ) );
		}
		
		log( "" );
		methodFooter();
		
		return true;
	}
	
	
	/**
	 * Prints out data from a normal Random Number test.
	 * This generates a bunch of test data and prints it to the screen.
	 * This will never fail since it doesn not automatically check the test.
	 * The idea is to run the output of this into a stats package and have it tell you 
	 * what the distribution looks like. 
	 *
	 * @param times The number of test values to generate.
	 * @param mean The mean for the test values.
	 * @param stdDev The stdard deviation for the distribution.
	 * @return If the test is passes, this will always be true for thise test.
	 **/
	public boolean gasdev( int times, double mean, double stdDev )
	{
		methodHeader();
		log( "      gasdev() " );
		log( "         times = " + times );
		log( "" );

		for( int i = 0; i < times; i++ )
		{
			log( ""+generator.gasdev( mean, stdDev ) );
		}

		log( "" );
		methodFooter();
		
		return true;
	}

	/**
	 * Prints out data from a binomail Random Number test.
	 * This generates a bunch of test data and prints it to the screen.
	 * This will never fail since it doesn not automatically check the test.
	 * The idea is to run the output of this into a stats package and have it tell you 
	 * what the distribution looks like. 
	 *
	 * @param times The number of test values to generate.
	 * @param probability The probility for the test values.
	 * @param trials The number of trials for the distribution.
	 * @return If the test is passes, this will always be true for thise test.
	 **/
	public boolean binomial( int times, double probability, int trials )
	{
		RandomGenerators generator = new BinomialDistributedGenerator( probability, trials );
		
		methodHeader();
		log( "      Binomial Distribution " );
		log( "         times = " + times );
		log( "         probability = " + probability );
		log( "         trials = " + trials );
		log( "" );

		for( int i = 0; i < times; i++ )
		{
			log( "" + generator.nextValue() );
		}

		log( "" );
		methodFooter();
		
		return true;
	}
	
	/**
	 * Prints out data from a exponential Random Number test.
	 * This generates a bunch of test data and prints it to the screen.
	 * This will never fail since it doesn not automatically check the test.
	 * The idea is to run the output of this into a stats package and have it tell you 
	 * what the distribution looks like. 
	 *
	 * @param times The number of test values to generate.
	 * @return If the test is passes, this will always be true for thise test.
	 **/
	public boolean exponential( int times )
	{
		RandomGenerators generator = new ExponentialDistributedGenerator();
		
		methodHeader();
		log( "      Exponential Distribution " );
		log( "         times = " + times );
		log( "" );

		for( int i = 0; i < times; i++ )
		{
			log( "" + generator.nextValue() );
		}

		log( "" );
		methodFooter();
		
		return true;
	}

	/**
	 * Prints out data from a gamma Random Number test.
	 * This generates a bunch of test data and prints it to the screen.
	 * This will never fail since it doesn not automatically check the test.
	 * The idea is to run the output of this into a stats package and have it tell you 
	 * what the distribution looks like. 
	 *
	 * @param times The number of test values to generate.
	 * @param eventNumber The event number to be used for the distribution.
	 * @return If the test is passes, this will always be true for thise test.
	 **/
	public boolean gamma( int times, int eventNumber )
	{
		RandomGenerators generator = new GammaDistributedGenerator( eventNumber );
		
		methodHeader();
		log( "      Gamma Distribution " );
		log( "         times = " + times );
		log( "         event = " + eventNumber );
		log( "" );

		for( int i = 0; i < times; i++ )
		{
			log( "" + generator.nextValue() );
		}

		log( "" );
		methodFooter();
		
		return true;
	}

	/**
	 * Prints out data from a poisson Random Number test.
	 * This generates a bunch of test data and prints it to the screen.
	 * This will never fail since it doesn not automatically check the test.
	 * The idea is to run the output of this into a stats package and have it tell you 
	 * what the distribution looks like. 
	 *
	 * @param times The number of test values to generate.
	 * @param mean The mean to be used for the distribution.
	 * @return If the test is passes, this will always be true for thise test.
	 **/
	public boolean poisson( int times, double mean )
	{
		RandomGenerators generator = new PoissonDistributedGenerator( mean );
		
		methodHeader();
		log( "      Poisson Distribution " );
		log( "         times = " + times );
		log( "         mean = " + mean );
		log( "" );

		for( int i = 0; i < times; i++ )
		{
			log( "" + generator.nextValue() );
		}

		log( "" );
		methodFooter();
		
		return true;
	}

	/**
	 * Prints out data from a uniform Random Number test.
	 * This generates a bunch of test data and prints it to the screen.
	 * This will never fail since it doesn not automatically check the test.
	 * The idea is to run the output of this into a stats package and have it tell you 
	 * what the distribution looks like. 
	 *
	 * @param times The number of test values to generate.
	 * @param min The minimum to be used for the distribution.
	 * @param max The maximum to be used for the distribution.
	 * @return If the test is passes, this will always be true for thise test.
	 **/
	public boolean uniform( int times, double min, double max )
	{
		RandomGenerators generator = new UniformDistributedGenerator( min, max );
		
		methodHeader();
		log( "      Uniform Distribution " );
		log( "         times = " + times );
		log( "         min = " + min );
		log( "         max = " + max );
		log( "" );

		for( int i = 0; i < times; i++ )
		{
			log( "" + generator.nextValue() );
		}

		log( "" );
		methodFooter();
		
		return true;
	}
	
}
