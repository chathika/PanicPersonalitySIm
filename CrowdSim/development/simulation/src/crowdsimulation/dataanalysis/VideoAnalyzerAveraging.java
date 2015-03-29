/*
 * $RCSfile: VideoAnalyzerAveraging.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.dataanalysis;

import crowdsimulation.*;
import crowdsimulation.entities.*;
import java.util.*;
import math.*;
import sim.util.*;

/**
 * This class reads in a dataset from the optical flow analysis and averages over the grids to produce the flux measures.
 * This is designed to read in the stuff from the LK optical flow analyzer which we have written in C++ usng 
 * the openCV libraries.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class VideoAnalyzerAveraging extends DataAnalyzer
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The width of the cells which should be used for averaging. **/
	double cellWidth=80;
	/** The height of the cells which should be used for averaging. **/
	double cellHeight=80;
	/** The width of world that was simulated. **/
	double width=640;
	/** The height of world being simulated. **/
	double height=480;
	/** The number of cells horizontally. **/
	int horizontalCells = 0;
	/** The number of cells vertically. **/
	int verticalCells = 0;
	/** The first frame the analysis was run on. This is the frame from the video. **/
	int startFrame = 0;
	/** The last frame the analysis was run on. This is the frame from the video. **/
	int endFrame = 350;
	/** The number of frame which were skipped to get to the next frame. 
	 *  This is the frame from the video. ie:
	 *      first frame was 0, then the next frame is 0+frameSkip.**/
	int frameSkip = 10;
	/** Multidimensional array of data for the cells. Used in averageing the data. **/
	double[][][] cellData;
	/** Multidimensional array of the output data for the cells. **/
	double[][][] outputCellData;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * This allows the execution of the VideoAnalyzerAveraging to generate the grouping information.
	 * The data to run the analysis is passed in through the command line.
	 * The command line arguments should be either:
	 *    1. The directory where the files are.
	 *    2. The name of the configuration file the simulation was run with.
	 *    3. The name of the file to do the analysis on.
	 *   OR
	 *    1. The name of the configuration file the simulation was run with, with the directory information.
	 *    2. The name of the file to do the analysis on, with the directory information.
	 *
	 * @param args The String array which contains the command line arguments.
	 **/
	public static void main( String[] args )
	{
//    	if( args.length < 2)
//    	{
//			System.out.println( "There must be atleast 2 inputs to this application." );
//    	}
    	
    	VideoAnalyzerAveraging da = null;
/*    	
    	if( args.length == 3)
    	{
    		da = new VideoAnalyzerAveraging( args[0], args[1], args[2] );
    	}
    	if( args.length == 2)
    	{
    		da = new VideoAnalyzerAveraging( args[0], args[1] );
    	}
*/  	
		da = new VideoAnalyzerAveraging( "C:/home/roleson/projects/ucf/crowdSimulation/development/videoAnalysis/opticalFlow/data/0-5.txt", "C:/home/roleson/projects/ucf/crowdSimulation/development/videoAnalysis/opticalFlow/data/0-350_3x4.csv" );

        da.analyze();
        System.exit(0);
	}

	
	public VideoAnalyzerAveraging( String dataFileName, String outFileName )
	{
		super( null, dataFileName, outFileName );
		horizontalCells = (int)Math.ceil(((double)width)/cellWidth);
		verticalCells = (int)Math.ceil(((double)height)/cellHeight);
		cellData = new double[horizontalCells][verticalCells][3];
		outputCellData = new double[horizontalCells/2][verticalCells/2][4];
	}


	public VideoAnalyzerAveraging( String dir, String dataFilename, String outFN )
	{
		this( dir+dataFilename, dir+outFN );
	}
	
	
	/**
	 * sets the reader back at the beginning of the file, and should clear any stored data in the class.
	 **/
	public void reset()
	{
		super.reset();

		cellData = new double[horizontalCells][verticalCells][3];
		outputCellData = new double[horizontalCells/2][verticalCells/2][4];
	}


	/**
	 * This gathers the appropriate data out of the input file and gathers the required data.
	 **/	
	public void analyze()
	{
		writeToFile( "frame,x,y,North,South,East,West" );
		for( int frame = startFrame; frame < endFrame; frame += frameSkip )
		{
			setFileName("C:/home/roleson/projects/ucf/crowdSimulation/development/videoAnalysis/opticalFlow/data/"+frame+"-"+(frame+frameSkip)+".txt");
			reset();
	/*
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
	*/	
	
			int maxWidth = 0;
			int maxHeight = 0;
			int cell_y = 0;
			int cell_x = 0;
			Object[] line;
			
			do
			{
				line = nextLine();
				if( line != null && line.length > 1 )
				{
					int x = 0;
					int y = 0;
					double u = 0;
					double v = 0;
					
					if( line.length == 4 ) 
					{
						x = Integer.parseInt( (String)line[0] );
						y = Integer.parseInt( (String)line[1] );
						u = Double.parseDouble( (String)line[2] );
						v = Double.parseDouble( (String)line[3] );
					}
					else if( line.length == 5 ) 
					{
						x = Integer.parseInt( (String)line[1] );
						y = Integer.parseInt( (String)line[2] );
						u = Double.parseDouble( (String)line[3] );
						v = Double.parseDouble( (String)line[4] );
					}
					else if( line.length == 6 ) 
					{
						x = Integer.parseInt( (String)line[2] );
						y = Integer.parseInt( (String)line[3] );
						u = Double.parseDouble( (String)line[4] );
						v = Double.parseDouble( (String)line[5] );
					}
					
					if( x > maxWidth )
					{
						maxWidth = x;
					}
					
					if( y > maxHeight )
					{
						maxHeight = y;
					}
					
					cell_x = (int)Math.ceil(((double)x)/cellWidth)-1;
					cell_y = (int)Math.ceil(((double)y)/cellHeight)-1;

					cellData[cell_x][cell_y][0]=cellData[cell_x][cell_y][0]+u;
					cellData[cell_x][cell_y][1]=cellData[cell_x][cell_y][1]+v;
					cellData[cell_x][cell_y][2]=cellData[cell_x][cell_y][2]+1;
				}
			}while( line != null && line.length > 0 );
			
			for( int y=0; y < verticalCells/2; y++ )		
			{
				for( int x=0; x < horizontalCells/2; x++ )
				{
					String north = (x*2) +","+ y*2+"and " + (x*2+1) + "," + y*2;
					String south = x*2 + ","+(y*2+1)+" and " + (x*2+1) + "," + (y*2+1);
					String east = (x*2+1)+","+y*2 +" and " +(x*2+1)+","+ (y*2+1);
					String west = (x*2)+","+y*2 +" and " + (x*2)+","+(y*2+1);
				
					outputCellData[x][y][0]=(cellData[x*2][y*2][1] + cellData[x*2+1][y*2][1] - cellData[x*2][y*2+1][1] - cellData[x*2+1][y*2+1][1] )/(cellData[x*2][y*2][2] + cellData[x*2+1][y*2][2] + cellData[x*2][y*2+1][2] + cellData[x*2+1][y*2+1][2]);
					outputCellData[x][y][1]=-1*((cellData[x*2][y*2+1][1] + cellData[x*2+1][y*2+1][1] - cellData[x*2][y*2][1] - cellData[x*2+1][y*2][1])/(cellData[x*2][y*2+1][2] + cellData[x*2+1][y*2+1][2] + cellData[x*2][y*2][2] + cellData[x*2+1][y*2][2] ));
					outputCellData[x][y][2]=(cellData[x*2+1][y*2][0] + cellData[x*2+1][y*2+1][0] - cellData[x*2][y*2][0] - cellData[x*2][y*2+1][0])/(cellData[x*2+1][y*2][2] + cellData[x*2+1][y*2+1][2] + cellData[x*2][y*2][2] + cellData[x*2][y*2+1][2]);
					outputCellData[x][y][3]=-1*((cellData[x*2][y*2][0] + cellData[x*2][y*2+1][0] - cellData[x*2+1][y*2][0] - cellData[x*2+1][y*2+1][0])/(cellData[x*2][y*2][2] + cellData[x*2][y*2+1][2] + cellData[x*2+1][y*2][2] + cellData[x*2+1][y*2+1][2]));
					
					writeToFile( frame+","+(x*(cellWidth*2)+(cellWidth*2))+","+(y*(cellHeight*2)+(cellHeight*2))+","+outputCellData[x][y][0]+","+outputCellData[x][y][1]+","+outputCellData[x][y][2]+","+outputCellData[x][y][3] );
				}
			}
		}
	}

}