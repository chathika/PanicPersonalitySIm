/*
 * $RCSfile: DataAnalyzerWithLocalDensity.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.dataanalysis;

import crowdsimulation.*;
import crowdsimulation.entities.*;
import java.io.*;
import math.*;
import sim.util.*;

/**
 * This is a base class for reads in a dataset from the simulation loggers and needing local density calculations.
 * This is helpfull when trying to summarize the data or condense the log to 
 * be run through other analyzer/statistical software/packages like the
 * outlier analysis. This also addes the features for writeing to data files also.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public abstract class DataAnalyzerWithLocalDensity extends DataAnalyzer
{

////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

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
	public DataAnalyzerWithLocalDensity ( String configFilename, String dataFileName, String outputFN )
	{
		super( configFilename, dataFileName, outputFN );
	}
	
	/**
	 * Gets the ids of all indiviudals within a certain distance of a given point.
	 * The individuals a pulled out of a collection and then tested to see if the 
	 * are within a given distance of a defined point and then all individuals 
	 * which met this criteria are returned in a collection.
	 *
	 * @param x The x element of the point araound which to check.
	 * @param y The y element of the point araound which to check.
	 * @param dist The distance from which the individuals must be inside of.
	 * @param data The bag containing the data for all of the individauls to be checked.
	 * @return The bag containig the ids of all the individuals which fell inside the circle defined by the inmputs.
	 **/
	public Bag getIdsOfLocalIndividuals( double x, double y, double dist, Bag data )
	{
		return getIdsOfLocalIndividuals( new Vector2D( x, y ), dist, data );
	}
	
	/**
	 * Gets the ids of all indiviudals within a certain distance of a given point.
	 * The individuals a pulled out of a collection and then tested to see if the 
	 * are within a given distance of a defined point and then all individuals 
	 * which met this criteria are returned in a collection.
	 *
	 * @param pointOfInterest The location of the point araound which to check.
	 * @param dist The distance from which the individuals must be inside of.
	 * @param data The bag containing the data for all of the individauls to be checked.
	 * @return The bag containig the ids of all the individuals which fell inside the circle defined by the inmputs.
	 **/
	public Bag getIdsOfLocalIndividuals( Vector2D pointOfInterest, double dist, Bag data )
	{
		Bag individuals = new Bag();
		
		for( int i=0; i < data.size(); i++ )
		{
			Bag indsData = (Bag)data.get( i );
			double indLocX = ((Double)indsData.get(6)).doubleValue();
			double indLocY = ((Double)indsData.get(7)).doubleValue();
			double r = pointOfInterest.distance( indLocX, indLocY );
			if( r <= dist )
			{
				if( pointOfInterest.x != indLocX && pointOfInterest.y != indLocY )
				{
					individuals.add( indsData.get(3) );
				}
			}
		}
		
		return individuals;
	}
	
	
	/**
	 * Gets the number of indiviudals within a certain distance of a given point.
	 * The individuals are pulled out of a collection and then tested to see if the 
	 * are within a given distance of a defined point and then all individuals 
	 * which met this criteria counted and the number meeting the criteria return.
	 *
	 * @param x The x element of the point around which to check.
	 * @param y The y element of the point around which to check.
	 * @param radius The distance from which the individuals must be inside of.
	 * @param data The bag containing the data for all of the individauls to be checked.
	 * @return The number of individuals which fell inside the circle defined by the inmputs.
	 **/
	public int calculateLocalDensityCount( double x, double y, double radius, Bag data )
	{
		return getIdsOfLocalIndividuals( x, y, radius, data ).size();
	}
	
	
	/**
	 * Gets the number of indiviudals within a certain distance of a given point.
	 * The individuals are pulled out of a collection and then tested to see if the 
	 * are within a given distance of a defined point and then all individuals 
	 * which met this criteria counted and the number meeting the criteria return.
	 *
	 * @param pointOfInterest The location of the point araound which to check.
	 * @param radius The distance from which the individuals must be inside of.
	 * @param data The bag containing the data for all of the individauls to be checked.
	 * @return The number of individuals which fell inside the circle defined by the inmputs.
	 **/
	public int calculateLocalDensityCount( Vector2D pointOfInterest, double radius, Bag data )
	{
		return getIdsOfLocalIndividuals( pointOfInterest, radius, data ).size();
	}
	
	/**
	 * Gets the density of indiviudals within a certain distance of a given point.
	 * The individuals are pulled out of a collection and then tested to see if the 
	 * are within a given distance of a defined point and then all individuals 
	 * which met this criteria counted and the number then divided by the area which was measured.
	 *
	 * @param x The x element of the point around which to check.
	 * @param y The y element of the point around which to check.
	 * @param radius The distance from which the individuals must be inside of.
	 * @param data The bag containing the data for all of the individauls to be checked.
	 * @return The number of individuals which fell inside the circle defined by the inmputs.
	 **/
	public double calculateLocalDensity( double x, double y, double radius, Bag data )
	{
		return calculateLocalDensityCount( x, y, radius, data )/(radius*radius*Math.PI);
	}
	
	/**
	 * Gets the density of indiviudals within a certain distance of a given point.
	 * The individuals are pulled out of a collection and then tested to see if the 
	 * are within a given distance of a defined point and then all individuals 
	 * which met this criteria counted and the number then divided by the area which was measured.
	 *
	 * @param pointOfInterest The location of the point araound which to check.
	 * @param radius The distance from which the individuals must be inside of.
	 * @param data The bag containing the data for all of the individauls to be checked.
	 * @return The number of individuals which fell inside the circle defined by the inmputs.
	 **/
	public double calculateLocalDensity( Vector2D pointOfInterest, double radius, Bag data )
	{
		return calculateLocalDensityCount( pointOfInterest, radius, data )/(radius*radius*Math.PI);
	}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
	
}

