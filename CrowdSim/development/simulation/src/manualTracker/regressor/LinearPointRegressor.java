package manualTracker.regressor;

import java.util.ArrayList;
import manualTracker.data.RegregressionPoint;
import java.io.*;

//This function calculates the projected point in the manual tracker
public class LinearPointRegressor implements Regressor 
{
  /**
   * Function expects points in reverse order, i.e. last in time is first in array
   * 
   * @param pointHistory ArrayList<RegregressionPoint>
   * @param nextTime
   * 
   * @return RegregressionPoint
   */
	public RegregressionPoint getNextPosition(ArrayList pointHistory, long nextTime) 
  	{
		RegregressionPoint lastPoint = (RegregressionPoint)pointHistory.get(0); // get last point from history
		RegregressionPoint beforeLastPoint = (RegregressionPoint)pointHistory.get(1); //get before last point from history
		RegregressionPoint newPoint = new RegregressionPoint();
		newPoint.time = nextTime;
		
		/*START SUMMER 2007 DEBUGGIN SOLUTION FOR POINTER MULTIPLICATION*/
  	
    	if(pointHistory.size() == 0) // if no points in history, do nothing
    	{
      		return newPoint;
    	}
   
    	if(pointHistory.size() == 1) // if one point in history, add 1 to last point
    	{
      		newPoint.x = lastPoint.x + 1;
      		newPoint.y = lastPoint.y + 1;
    	}
    	
    	else // if more than 1 point in history
    	{
      		if((lastPoint.x - beforeLastPoint.x) > 0) //conditions determine where the projection is going to move
      		{
      			newPoint.x = lastPoint.x + 1;
      			if((lastPoint.y - beforeLastPoint.y > 0))
      			{
      				newPoint.y = lastPoint.y + 1;
      			}
      			else
      			{
      				newPoint.y = lastPoint.y - 1;
      			}
      		}
      		else
      		{
      			newPoint.x = lastPoint.x - 1;
      			if((lastPoint.y - beforeLastPoint.y) > 0)
      			{
      				newPoint.y = lastPoint.y + 1;
      			}
      			else
      			{
      				newPoint.y = lastPoint.y - 1;
      			}
      		}
      		/*END SUMMER 2007 DEBUGGIN SOLUTION FOR POINTER MULTIPLICATION*/
      		
      		//START OF WHAT USED TO BE THE ORIGINAL SOLUTION
      		
      		//newPoint.x = lastPoint.x+(lastPoint.x - beforeLastPoint.x)*(int)(nextTime-lastPoint.time)/(int)(lastPoint.time - beforeLastPoint.time);
      		//newPoint.y = lastPoint.y+(lastPoint.y - beforeLastPoint.y)*(int)(nextTime-lastPoint.time)/(int)(lastPoint.time - beforeLastPoint.time);
      		
      		//END OF WHAT USED TO BE THE ORIGINAL SOLUTION
    	}
    	try // try-catch method used for debugging purposes only
		{
			PrintWriter Projection = new PrintWriter (new FileOutputStream ("Projection.txt")); // writes projection to a .txt file
			
			Projection.println("Projection: " + newPoint.x + " , " + newPoint.y);
			Projection.close();
		}
		catch(IOException ex)
		{
		}
		
    return newPoint;
  }

}
