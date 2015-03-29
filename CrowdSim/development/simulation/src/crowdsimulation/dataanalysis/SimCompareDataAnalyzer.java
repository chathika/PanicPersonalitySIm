/*
 * $RCSfile: SimCompareDataAnalyzer.java,v $ $Date: 2009/06/11 20:36:56 $
 */
package crowdsimulation.dataanalysis;

import crowdsimulation.*;
import crowdsimulation.entities.*;
import java.util.*;
import math.*;
import sim.util.*;

/**
 * This class reads in the data file and generates the data that is being used for simulation comparisons.
 *
 * @author $Author: ganil $
 * @version $Revision: 1.5 $
 * $State: Exp $
 * $Date: 2009/06/11 20:36:56 $
 **/
public class SimCompareDataAnalyzer extends DataAnalyzerWithLocalDensity
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The waypoint of where the exit is. **/	
	private Waypoint exit = null;
	/** A hash of the locations of all individuals at the previous timestep. **/
	private HashMap previousLocation = new HashMap();
	/** A has of the paths at the previous timestep. **/
	private HashMap previousPath = new HashMap();
	/** What was the previous location of the exit. **/
	private HashMap previousExitLocation = new HashMap();
	/** A hash of what individuals have exited the room. **/
	private HashMap hasExited = new HashMap();
	/** The number of individuals in the room. **/
	private double numberOfIndiviudals = -1;
	/** The start time for the simulation. **/
	private double startTime = -1;
	/** The radius of influence for an individul. **/
	private double radiusOfInfluence = 3;
	
	/** A bag of all of the individuals which will have data to collect. **/
	private Bag individualsData = new Bag();
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/** 
	 * This is designed to executed this analysis class on the data passed in through the command line.
	 * The command line arguments should be either:
	 *    1. The directory where the files are.
	 *    2. The name of the configuration file the simulation was run with.
	 *    3. The name of the file to do the analysis on.
	 *    4. The datafile to output the results to.
	 *   OR
	 *    1. The name of the configuration file the simulation was run with, with the directory information.
	 *    2. The name of the file to do the analysis on, with the directory information.
	 *    3. The datafile to output the results to, with the directory information.
	 *
	 * @param args The String array which contains the command line arguments.
	 **/	
	public static void main( String[] args )
	{
//    	if( args.length < 3)
//    	{
//			System.out.println( "There must be atleast 3 inputs to this application." );
//    	}
    	
    	SimCompareDataAnalyzer da = null;

    	if( args.length == 4)
    	{
    		da = new SimCompareDataAnalyzer( args[0], args[1], args[2], args[3] );
    	}
    	if( args.length == 3)
    	{
    		da = new SimCompareDataAnalyzer( args[0], args[1], args[2] );
    	}

//    	String dir = "../logs/run0_0/";
//        da = new SimCompareDataAnalyzer( dir+"currentconfig.xml",
//                                            dir+"dataLog.csv",
//                                            dir+"compareData.csv"  );
//System.out.println( args[0] + " " + args[1] + " " + args[2]);
//System.out.println( args[0]+args[1] + " " + args[0]+args[2] + " " + args[0]+args[3]);
//		da = new SimCompareDataAnalyzer( args[0], args[1], args[2], args[3] );
//System.out.println( " was I able to create an object " );
        da.analyze();
	}


	/** 
	 * Constructs a SimCompareDataAnalyzer to summarize the data output form the simulation into a form usable by the oultier anayslis.
	 * The directory information must be included in the passed in data if using this 
	 * constructor. 
	 *
	 * @param configFilename The name of the configuration file along with the dirctory info to get to it.
	 * @param dataFilename The name of the data file along with the dirctory info to get to it.
	 * @param outputFilename The name of the output file along with the dirctory info to get to it.
	 **/
	public SimCompareDataAnalyzer( String configFilename, String dataFilename, String outputFilename )
	{
		super( configFilename, dataFilename, outputFilename );
		Path path =(Path)(CrowdSimulation.getInstance().getPaths().get( new Integer( 1 ) ));
		exit = (Waypoint)path.waypoints.get( 0 );
	}

	/** 
	 * Constructs a SimCompareDataAnalyzer to summarize the data output form the simulation into a form usable by the oultier anayslis.
	 * All the files must be in the same directory for this constructory.
	 * Basically the directory is added onto the filenames and that is what is used.
	 *
	 * @param dir The directory where all the files are, or will be placed.
	 * @param configFilename The name of the configuration file along with the dirctory info to get to it.
	 * @param dataFilename The name of the data file along with the dirctory info to get to it.
	 * @param outputFilename The name of the output file along with the dirctory info to get to it.
	 **/
	public SimCompareDataAnalyzer( String dir, String configFilename, String dataFilename, String outputFilename )
	{
		this( dir+configFilename, dir+dataFilename, dir+outputFilename );
	}
	
	/**
	 * This writes the header to the datafile then analyizes the data.
	 **/
	public void analyze()
	{
		writeToFile(
			"Real Time (s)," +
			"Simulation Time (s),"+
			"Step Number (num),"+
			"Average Density At Exits (num/m^2),"+
			"Number of individuals who crossed the doorway this time sample (num),"+
			"Average Orientation of all individuals (rad),"+
			"Average X Location of all individuals (m),"+
			"Average Y Location of all individuals (m),"+
			"Average X Velocity of all individuals (m/s),"+
			"Average Y Velocity of all individuals (m/s),"+
			"Average Denisty around each individuals (num/m^2),"+
			"number of individuals still being simulated (%),"+
			"number of individuals facing The Door (%),"+
			"number of individuals still in the room (%),"+
			"number of individuals having gotten out of the room (%)");

		while( currentLine != null && currentLine.length > 1 )
		{
			getNextDataSet( 2 );
			
			analyzeData();
		}
	}
	
	/**
	 * This analyzes each timestep worth of data.
	 **/
	public void analyzeData()
	{
		individualsData.clear();
		
		for( int i = 0; i < dataToAverage.size(); i++ )
		{
			Bag currentData = new Bag();
			Object[] dataSet = ((Object[])(dataToAverage.get(i)));
			double time = Double.parseDouble( (String)dataSet[0] );
			
			if( startTime < 0)
			{
				startTime = time;
			}
			
			currentData.add( new Double( (time - startTime)/1000 ) );
			currentData.add( dataSet[1] );
			currentData.add( dataSet[2] );

			Integer id = new Integer( (String)dataSet[3] );
			currentData.add( id );
			if( id.intValue()+1 > numberOfIndiviudals )
			{
				numberOfIndiviudals = id.intValue()+1;
			}
						
			double x = Double.parseDouble( (String)dataSet[5] );
			double y = Double.parseDouble( (String)dataSet[6] );
			
			double velX = Double.parseDouble( (String)dataSet[7] );
			double velY = Double.parseDouble( (String)dataSet[8] );

			double orientation = Double.parseDouble( (String)dataSet[9] );
			
			int pathId = 0;
			double wPointX = 0;
			double wPointY = 0;
			if( !dataSet[10].equals("") )
			{
				pathId = Integer.parseInt( (String)dataSet[10] );
				if( !dataSet[11].equals("") && !dataSet[12].equals("") )
				{
					wPointX = Double.parseDouble( (String)dataSet[11] );
					wPointY = Double.parseDouble( (String)dataSet[12] );
				}
			}
			Vector2D wPointLoc = new Vector2D( wPointX, wPointY );
			
			int hasCrossed = 0;
			
			if( !hasExited.containsKey( new Integer(id) ) && previousLocation.containsKey( id ) && previousPath.containsKey( id ) && previousExitLocation.containsKey( id ) )
			{
				int prevPathId = ((Integer)previousPath.get( id )).intValue();
				if( prevPathId == pathId )
				{
					Vector2D prevWPointLoc = (Vector2D)previousExitLocation.get( id );
					if( !prevWPointLoc.equals( wPointLoc ) )
					{
						hasCrossed = 1;
						hasExited.put( new Integer(id), new Boolean( true ) );
					}
				}
			}
			currentData.add( new Integer( hasCrossed ) );
			previousLocation.put( id, new Vector2D( x, y ) );
			previousPath.put( id, new Integer( pathId ) );
			previousExitLocation.put( id, new Vector2D( wPointX, wPointY ) );
			
			currentData.add( new Double( orientation ) );
			currentData.add( new Double( x ) );
			currentData.add( new Double( y ) );
			currentData.add( new Double( velX ) );
			currentData.add( new Double( velY ) );
			
			individualsData.add( currentData );
		}
		
		writeToFile( writeElement( averageData( individualsData ).objs, "," ) );
	}
	
	
	/**
	 * Checks to see if the individual being descibed is facing the doorway. 
	 * This checks to see if the individuals are within a given angle of facing the doorway.
	 * It check the orientation of the individual in comparison centerline from individaul to the exit.
	 * 
	 *
	 * @param x The x position of the individual.
	 * @param y The y position of the indiviudal.
	 * @param orientation The direction the individual is facing
	 * @param data The data fro the indiviudal being checked.
	 * @return A representation of if the indiviudal is facing the doorway: 0 no, 1 yes.
	 *   The reason for using an int instead of a boolean is to facilitate counting in the calling function.
	 **/
	public int facingDoorway( double x, double y, double orientation, Bag data )
	{
		int isFacingDoorway = 0;
		
		double theta = Math.atan2( exit.getCenter().x-x, exit.getCenter().y-y );
		double thetaDiff = theta - orientation;
		
		if( Math.abs( thetaDiff ) < Math.PI/4 )
		{
			isFacingDoorway = 1;
		}
		
		return isFacingDoorway;
	}
	
	
	/**
	 * This calcualtes the density at the exit.
	 *
	 * @param data The collection containing the data about the individulas
	 * @return The double which is the average density at the exit.
	 **/
	public double calculateAverageExitDensity( Bag data )
	{
		Object[] paths = CrowdSimulation.getInstance().getPaths().values().toArray();
		double density = 0;
		
		for( int i = 0; i < paths.length; i++)
		{
			Path path = (Path)paths[i];
			Waypoint point = (Waypoint)path.getWaypoints().get(0);
			//TODO: HACK: Make this proper for a line waypoint.
            density += calculateLocalDensity( point.getCenter().x, point.getCenter().y, radiusOfInfluence, data );
		}
		return density/(double)paths.length;
	}
	
	
	/**
	 * This gathers the averaged,or large scale, data for a single timestep in the simulation.
	 *
	 * @param data The data to average.
	 * @return The collection of data gathered from a time scale.
	 **/
	public Bag averageData( Bag data )
	{
		int numberOfIndividualsNow = data.size();
		
		Bag avgData = new Bag();
		
		int numCrossedDoor = 0;
		double orientation = 0.0;
		double locationX = 0.0;
		double locationY = 0.0;
		double velocityX = 0.0;
		double velocityY = 0.0;
		double density = 0.0;
		double numInRoom = 0;
		double numFacingDoorway = 0;
				
		for( int i = 0; i < numberOfIndividualsNow; i++ )
		{
			Bag individualsData = (Bag)data.get(i);
			numCrossedDoor += ((Integer)individualsData.get(4)).intValue();
			if( !hasExited.containsKey( (Integer)individualsData.get(3) ) )
			{
				double indOrientation = ((Double)individualsData.get(5)).doubleValue();
				orientation += indOrientation;
				double indLocX = ((Double)individualsData.get(6)).doubleValue();
				double indLocY = ((Double)individualsData.get(7)).doubleValue();
				locationX += indLocX;
				locationY += indLocY;
				velocityX += ((Double)individualsData.get(8)).doubleValue();
				velocityY += ((Double)individualsData.get(9)).doubleValue();
				density += calculateLocalDensity( indLocX, indLocY, radiusOfInfluence, data );
				numFacingDoorway += facingDoorway( indLocX, indLocY, indOrientation, data );
			}
		}

		numInRoom = numberOfIndiviudals - hasExited.size();
		//real Time
		avgData.add( ((Bag)data.get(0)).get(0) );
		//simulation Time
		avgData.add( ((Bag)data.get(0)).get(1) );
		//step Number
		avgData.add( ((Bag)data.get(0)).get(2) );
		//density At Exit
		avgData.add( new Double( calculateAverageExitDensity( data ) ) );
		//number of individuals who crossed the doorway this time sample
		avgData.add( new Integer( numCrossedDoor ) );
		//Average Orientation of all individuals
		if( numInRoom != 0 )
		{
			avgData.add( new Double( orientation/numInRoom ) );
		}
		else
		{
			avgData.add( new Double( 0.0 ) );
		}
		//Average X Location of all individuals
		if( numInRoom != 0 )
		{
			avgData.add( new Double( locationX/numInRoom ) );
		}
		else
		{
			avgData.add( new Double( 0.0 ) );
		}
		//Average Y Location of all individuals
		if( numInRoom != 0 )
		{
			avgData.add( new Double( locationY/numInRoom ) );
		}
		else
		{
			avgData.add( new Double( 0.0 ) );
		}
		//Average X Velocity of all individuals
		if( numInRoom != 0 )
		{
			avgData.add( new Double( velocityX/numInRoom ) );
		}
		else
		{
			avgData.add( new Double( 0.0 ) );
		}
		//Average Y Velocity of all individuals
		if( numInRoom != 0 )
		{
			avgData.add( new Double( velocityY/numInRoom ) );
		}
		else
		{
			avgData.add( new Double( 0.0 ) );
		}
		//Average Denisty around each individuals
		if( numInRoom != 0 )
		{
			avgData.add( new Double( density/numInRoom ) );
		}
		else
		{
			avgData.add( new Double( 0.0 ) );
		}
		//number of individuals still being simulated
		if( numberOfIndiviudals != 0 )
		{
			avgData.add( numberOfIndividualsNow/numberOfIndiviudals );
		}
		else
		{
			avgData.add( new Double( 0.0 ) );
		}
		//number of individuals facing The Door
		if( numInRoom != 0 )
		{
			avgData.add( new Double( numFacingDoorway/numInRoom ) );
		}
		else
		{
			avgData.add( new Double( 0.0 ) );
		}
		//number of individuals still in the room
		avgData.add( new Double( numInRoom/numberOfIndiviudals ) );
		//number of individuals having gotten out of the room
		avgData.add( hasExited.size()/numberOfIndiviudals );
		return avgData;
	}
	
}