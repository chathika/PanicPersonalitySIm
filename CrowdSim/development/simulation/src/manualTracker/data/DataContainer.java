package manualTracker.data;

import java.util.ArrayList;

public class DataContainer 
{
	public String outputFilename; // variable used to store data out of the program
	 
	public String inputFilename; // variable used to bring in data stored in the computer
	
	public ArrayList frames = new ArrayList();
	
	public int maxPointId = 0;
	
	public int frameHSize = 0; //  varible for horizontal size of the video
	
	public int frameVSize = 0; // variable for vertical size of the video
}
