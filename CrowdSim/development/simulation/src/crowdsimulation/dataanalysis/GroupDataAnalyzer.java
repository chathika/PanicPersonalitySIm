/*
 * $RCSfile: GroupDataAnalyzer.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.dataanalysis;

import crowdsimulation.*;
import crowdsimulation.entities.*;
import java.util.*;
import java.awt.*;
import java.io.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.imageio.*;
import math.*;
import sim.util.*;

/**
 * This class looks at the data in a file and tracks any whole which form inside groups of indiviudals.
 * This generates that type of information by creating a series of images to load into matlab to 
 * do connected graph analysis on.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class GroupDataAnalyzer extends DataAnalyzerWithLocalDensity
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The data for a group. **/
	private Bag groupData = new Bag();
	/** The data for all of the groups. **/
	private Bag groups = new Bag();
	/** The number of individuals which are in a group. **/
	private double numberOfIndiviudals = -1;
	/** The start time for the whole simulation. **/
	private double startTime = -1;
	/** The minimum number of individuals for something to be considered a group. **/
	private int minimumNumberForGroup = 3;
	/** The timestep which was used to move to the next step of the simulation. **/
	private double timeStep = -1;
	/** A map of all the individuals indexed my the individual's IDs. **/
	private HashMap individuals;
	/** The length of the world. **/
	double worldSizeX = -1;
	/** The height of the world. **/
	double worldSizeY = -1;
	/** The scale used for the world. **/
	double discretization = 10;
	/** The largest of the groups. **/
	Group primaryGroup;
	/** The distance to consider someone a member of the the same group. **/
	static double groupDist = 2;
	/** The directory where the data is and where the output file should go. **/
	String dir = "";

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
		
	/**
	 * This allows the execution of the GroupDataAnalyzer to generate the grouping information.
	 * The data to run the analysis is passed in through the command line.
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
/*
    	if( args.length < 3)
    	{
			System.out.println( "There must be atleast 3 inputs to this application." );
    	}
*/    	
    	
    	GroupDataAnalyzer ga = null;
/*    	
    	if( args.length == 4)
    	{
    		ga = new GroupDataAnalyzer( args[0], args[1], args[2], args[3] );
    	}
    	if( args.length == 3)
    	{
    		ga = new GroupDataAnalyzer( args[0], args[1], args[2] );
    	}
*/    	
        ga = new GroupDataAnalyzer( "../logs/test/", "currentconfig.xml",
                                            "individualsData.csv",
                                            "groupData.csv"  );
		//SimCompareDataAnalyzer da = new GroupDataAnalyzer( args[0], args[1], args[2] );
        
        ga.analyze();
	}
	
	/**
	 * Constructs a GroupDataAnalyzer from a configfile, data file, and an output file.
	 * The directory information must be included in the passed in data if using this 
	 * constructor. 
	 *
	 * @param configFilename The name of the configuration file along with the dirctory info to get to it.
	 * @param dataFilename The name of the data file along with the dirctory info to get to it.
	 * @param outputFilename The name of the output file along with the dirctory info to get to it.
	 **/
	public GroupDataAnalyzer( String configFilename, String dataFilename, String outputFilename )
	{
		super( configFilename, dataFilename, outputFilename );
		
		Path path =(Path)(CrowdSimulation.getInstance().getPaths().get(new Integer(1)));
	}

	/**
	 * Constructs a GroupDataAnalyzer from a directory, configfile, data file, and an output file.
	 * All the files must be in the same directory for this constructory.
	 * Basically the directory is added onto the filenames and that is what is used.
	 *
	 * @param dir The directory where all the files are, or will be placed.
	 * @param configFilename The name of the configuration file.
	 * @param dataFilename The name of the data file.
	 * @param outputFilename The name of the output file.
	 **/
	public GroupDataAnalyzer( String dir, String configFilename, String dataFilename, String outputFilename )
	{
		this( dir+configFilename, dir+dataFilename, dir+outputFilename );
	}
	
	/**
	 * This gathers the appropriate data out of the input file and hands it off to groupData to run the analysis.
	 *
	 * @see #groupData()
	 **/
	public void analyze()
	{
		writeToFile("");

		while( currentLine != null && currentLine.length > 1 )
		{
			getNextDataSet( 2 );
			
			if( numberOfIndiviudals < 0)
			{
				numberOfIndiviudals = dataToAverage.size();
			}
			if( worldSizeX < 0 || worldSizeY < 0 )
			{
				worldSizeX = CrowdSimulation.getInstance().worldWidth * discretization;
				worldSizeY = CrowdSimulation.getInstance().worldHeight * discretization;
			}
			
			groupData();
		}
	}
	
	/** 
	 * This actaully runs the analysis on the data to determine what grouping if any are in the data.
	 **/
	public void groupData()
	{
		groupData.clear();
		
		individuals = new HashMap();
		Bag data = new Bag();
		primaryGroup = new Group( (int)worldSizeX, (int)worldSizeY );
		primaryGroup.id = 0;
		
		for( int i = 0; i < dataToAverage.size(); i++ )
		{
			Bag individualsData = new Bag();
			
			Object[] dataSet = ((Object[])(dataToAverage.get(i)));
			
			individualsData.add( (String)dataSet[0] );
			individualsData.add( new Double( (String)dataSet[1] ) );
			individualsData.add( new Integer( (String)dataSet[2] ) );
			
			Integer id = new Integer( (String)dataSet[3] );
			individualsData.add( id );
			individualsData.add( new Double( (String)dataSet[4] ) );
			individualsData.add( new Double( (String)dataSet[5] ) );
			individualsData.add( new Double( (String)dataSet[6] ) );
			individualsData.add( new Double( (String)dataSet[7] ) );
			individualsData.add( new Double( (String)dataSet[8] ) );

			individuals.put( id, individualsData );
			data.add( individualsData );
			primaryGroup.addIndividual( id, individualsData );
		}

		for( int j = 0; j < individuals.size(); j++ )
		{
			Bag individualsData = new Bag();
			Object[] keys = individuals.keySet().toArray();
			
			Integer key = (Integer)keys[j];

			individualsData = ((Bag)(individuals.get( key )));

			//individualsData = (Bag)individuals.get( new Integer( (String)dataSet[3] ) );
			
			double x = ((Double)individualsData.get( 5 )).doubleValue();
			double y = ((Double)individualsData.get( 6 )).doubleValue();
			
			if( !elementOfAGroup( key ) )
			{
				Bag influencingIndsIds = getIdsOfLocalIndividuals( x, y, groupDist, data );

				if( influencingIndsIds.size() >= minimumNumberForGroup )
				{
					HashMap influencingInds = new HashMap();
					for( int i=0; i < influencingIndsIds.size(); i++ )
					{
						Integer id = (Integer)influencingIndsIds.get( i );
						Bag indData = (Bag)individuals.get( id );
						influencingInds.put( id, indData );
					}
					Group group = new Group( (int)worldSizeX, (int)worldSizeY );
					group = buildGroup( group, key, influencingInds, data );
					groups.add( group );
				}
			}
			
		}
		
		writeGroupData();
//		writeToFile( writeElement( averageData( groupData ).objs, "," ) );
	}
	
	/**
	 * This actually associates individuals to a given group.
	 * The existing group is passed in and the individuals in the bag of data are checked to
	 * see if they belong to this group.
	 *
	 * @param group The start of a group to see if any other individuals in the data are a member of.
	 * @param rootID This is the id of the original group this group was associated with, if one exists.
	 * @param elementsLeft A Hash of element which still need to be checked.
	 * @param data The data of all individuals to check to see if they are member of the group.
	 * @return The group, and its assocaited data, if one existed.
	 **/
	public Group buildGroup( Group group, Integer rootID, HashMap elementsLeft, Bag data )
	{
		group.id = groups.size();
		
		Bag dataSet = (Bag)individuals.get( rootID );
		group.addIndividual( rootID, dataSet );
		elementsLeft.remove( rootID );

		double x = ((Double)dataSet.get( 5 )).doubleValue();
		double y = ((Double)dataSet.get( 6 )).doubleValue();
		double radius = ((Double)dataSet.get( 4 )).doubleValue();
		
		Bag influencingIndsIds = getIdsOfLocalIndividuals( x, y, groupDist, data );
		
		if( influencingIndsIds.size() >= minimumNumberForGroup )
		{
			for( int i=0; i < influencingIndsIds.size(); i++ )
			{
				Integer id = (Integer)influencingIndsIds.get( i );
				if( !group.alreadyAdded( id ) )
				{
					Bag indData = (Bag)individuals.get( id );
					elementsLeft.put( id, indData );
				}
			}
		}
		
		for( int i = 0; i < elementsLeft.size(); i++ )
		{
			Object[] ids = elementsLeft.keySet().toArray();
			Integer id = (Integer)ids[i];

			if( id != null && !group.alreadyAdded( id ) )
			{
				Integer childID = id;
				buildGroup( group, childID, elementsLeft, data );
			}
		}
			
		return group;
	}
	
	/**
	 * This write an image of the group to a file in the directory.
	 * Each group gets a different file representing the area taken up the 
	 * the individuals in the group.
	 **/
	public void writeGroupData()
	{
		try 
		{
			// Save as JPEG
		    File file = new File( dir+"primaryGroupImage_"+stepNum+".bmp" );
		    ImageIO.write( primaryGroup.image, "bmp", file );
		} 
		catch( IOException e )
		{
			e.printStackTrace();
		}

		for( int i = 0; i < groups.size(); i++)
		{
			Group grp = (Group)groups.get( i );
			System.out.println( "This is for group ID " + grp.id );
			
			Object[] keys = grp.individualsData.keySet().toArray();
			Double time = new Double( 0 );
			
			for( int j = 0; j < keys.length; j++)
			{
				Bag data = (Bag)grp.individualsData.get( keys[j] );
				time = (Double)data.get( 1 );
			}
			
			try 
			{
				// Save as JPEG
			    File file = new File( dir+"groupImage_"+stepNum+"_"+grp.id+".bmp" );
			    ImageIO.write( grp.image, "bmp", file );
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Checks to see if the id is the id of an individual which has already been added to a group.
	 *
	 * @param id The id of the individual to check for.
	 * @return The boolean representing if the id is has already been added to a group. 
	 **/
	public boolean elementOfAGroup( Integer id )
	{
		for( int i = 0; i < groups.size(); i ++ )
		{
			Group grp = (Group)groups.get(i);
			if( grp.alreadyAdded(id) )
			{
				return true;
			}
		}
		return false;
	}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
	
}

/** 
 * This is an internal class which defines a group of individuals.
 * This holder for the data and individuals which are part of a group.
 **/
class Group
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
	
	/** The image representing the area taken up by the individuals in the group. **/
	public BufferedImage image;
	/** The unique identifier for the group. **/
	public int id;
	/** The collection of individuals in the group. **/
	public HashMap individualsData = new HashMap();
	/** The graphics object which is used to create the image for the group. **/
	Graphics2D g2D;
	/** The scale the image should be drawn to. **/
	double scale = 10;
	/** The height of the world. **/
	double width;
	/** The width of the world. **/
	double height;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs a group object for a given width and height.
	 *
	 * @param wVal The width of the world the group exists in.
	 * @param hVal The height of the world the group exists in.
	 **/
	Group( double wVal, double hVal )
	{
		width = wVal;
		height = hVal;
		image = new BufferedImage( (int)(width), (int)(height), BufferedImage.TYPE_BYTE_BINARY );
		g2D = image.createGraphics();
		g2D.setColor( Color.WHITE );
		Rectangle2D.Double rect = new Rectangle2D.Double(0,0,width*scale,height*scale); 
		g2D.fill(rect);
	}
	
	/**
	 * Checks to see if the individual with the given id has already been added to this group.
	 *
	 * @param id The identifier of the individual to check to see if they are in this group.
	 **/
	boolean alreadyAdded( Integer id )
	{
		return individualsData.containsKey( id );
	}
	
	/**
	 * This adds and individual to this group.
	 *
	 * @param id The id of the individual to be added to the group.
	 * @param dataSet The data for the individual being added to the group.
	 **/
	public void addIndividual( Integer id, Bag dataSet )
	{
		individualsData.put( id, dataSet );
		g2D.setColor(Color.BLACK);
		g2D.fill( createCircle( ((Double)dataSet.get(5)).doubleValue(), ((Double)dataSet.get(6)).doubleValue(), GroupDataAnalyzer.groupDist) );
	}
	
	/**
	 * This draws a circle on the image at the given x,y coordinates with a given radius.
	 *
	 * @param x The x component of the center of the circle to be drawn.
	 * @param y The y component of the center of the circle to be drawn.
	 * @param radius The radius of the circle to be drawn.
	 **/
	Shape createCircle( double x, double y, double radius )
	{
		Shape shape = new Ellipse2D.Double( 
		(x-radius)*scale, 
		(y-radius)*scale, 
		(radius)*scale,
		(radius)*scale );
		
		return shape;
	}
	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

}