package manualTracker.regressor;

import java.io.*;
import java.util.ArrayList;
import manualTracker.data.RegregressionPoint;

public class LinearNPointInterpolator 
{
  /**
   * Function expects points in reverse order, i.e. last in time is first in array
   * 
   * @param  pointHistory ArrayList<RegregressionPoint>
   * @param frameSkipCount
   * @return ArrayList<RegregressionPoint>
   */
   
   //After correction has been maded in the manual tracker, the program jumps 
   //to this function and interpolates for the points in the middle
   
   	public static ArrayList getInterpolationPositions(ArrayList pointHistory, int frameSkipCount) throws IOException
  	{
	    ArrayList points = new ArrayList();
	    RegregressionPoint firstPoint = (RegregressionPoint)pointHistory.get(0); //gets first point on the history
	    RegregressionPoint lastPoint = (RegregressionPoint)pointHistory.get(1); //gets last point on the history
	    
	    long timeDifference = lastPoint.time - firstPoint.time; // calculates delta time
	    int xDifference = lastPoint.x - firstPoint.x; // calculates delta x
	    int yDifference = lastPoint.y - firstPoint.y; // calculates delta y
	    
	   	PrintWriter PointData = new PrintWriter (new FileOutputStream ("Point_Data_Last" + frameSkipCount + "Frame_sSkipped.txt")); // writes to a txt file for debugging purposes
		
		PointData.println("First Set : ( " + firstPoint.x + " , " + firstPoint.y + " )"); //writes first set of coordinates to .txt
	    PointData.println("Last  Set : ( " + lastPoint.x + " , " + lastPoint.y + " )");   //writes last set of coordinates to .txt
	    PointData.println(""); // separates coordinates in .txt
	   	PointData.println("Delta X: " + xDifference); // writes delta x to .txt
	    PointData.println("Delta Y: " + yDifference); // writes delta y to .txt
		
	    for(int i = 0; i < frameSkipCount; i++) // for-loop that interpolates
	    {
	    	RegregressionPoint newPoint = new RegregressionPoint(); 
	      	newPoint.x = firstPoint.x + (int)(i*xDifference/frameSkipCount); //calculates new x
	      	newPoint.y = firstPoint.y + (int)(i*yDifference/frameSkipCount); //calculates new y
	      	newPoint.time = firstPoint.time + (i*timeDifference/frameSkipCount); //calculates new time
	      	points.add(newPoint); //add points to history

	      	PointData.println("Coordinate set for Frame " + i + ": ( "+ newPoint.x + " , " + newPoint.y + " , " + newPoint.time + " )"); // writes coordinates to .txt
	      	PointData.println(""); // separates coordinates in .txt
	    }

		PointData.close(); // closes .txt

    return points;
    
  }
}
