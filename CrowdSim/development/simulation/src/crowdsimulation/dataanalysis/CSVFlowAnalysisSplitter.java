/*
 * $RCSfile: CSVFlowAnalysisSplitter.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.dataanalysis;

import sim.util.*;
import java.io.*;
import java.util.*;

//TODO: This class still needs to be implemented
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
public class CSVFlowAnalysisSplitter extends CSVAnalysis
{

////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The number of grids to divide the field into when averaging to find the fluxes. **/
	private int numberOfGrids;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/** 
	 * This is designed to executed this analysis class on the data passed in through the command line.
	 * The command line arguments should be:
	 *  1. The name of the file to do the analysis on. This should contain any directory information needed.
	 *  2. The number of grid cells to divide the field into.
	 *
	 * @param args The String array which contains the command line arguments.
	 **/
	public static void main( String[] args ) 
	{
		if( args.length < 2 )
		{
			System.out.println( "The CVSFlowAnalysisSplitter needs the filname then the number of grids" );
			System.exit(1);
		}
		
		CSVFlowAnalysisSplitter splitter = new CSVFlowAnalysisSplitter( args[0], Integer.parseInt(args[1]) );
		
		System.out.println( splitter.writeElement( splitter.nextLine() ) );
		System.out.println( splitter.writeElement( splitter.nextLine() ) );
		System.out.println( splitter.writeElement( splitter.nextLine() ) );
		System.out.println( splitter.writeElement( splitter.nextLine() ) );
		System.out.println( splitter.writeElement( splitter.nextLine() ) );
		
		splitter.reset();
		
		System.out.println( splitter.writeElement( splitter.nextLine() ) );
	}
	
	/**
	 * This constructs a CSVFlowAnalysisSplitter to analyze the data from the optical flow code.
	 *
	 * @param filename The filename of the file to analyze.
	 * @param grids The number of grid cells to divide the field into.
	 **/
	public CSVFlowAnalysisSplitter( String filename, int grids )
	{
		super( filename );
		numberOfGrids = grids;
	}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

}
