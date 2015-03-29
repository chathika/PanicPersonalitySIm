package manualTracker.postProcessors;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import manualTracker.data.DataContainer;
import manualTracker.data.FrameData;
import manualTracker.data.PointData;
import manualTracker.uiFramework.GlobalSettings;

public class HeadCount_0 
{
  	static final int NORTH = 0;
  	static final int SOUTH = 1;
  	//static final int EAST = 2;
  	//static final int WEST = 3;
  	// On Mario's request
  	static final int EAST = 3;
  	static final int WEST = 2;
  
  	int[] gridLimit_H_Low;
	int[] gridLimit_H_High;
	int[] gridLimit_V_Low;
	int[] gridLimit_V_High;
  	
  	ArrayList[][] squareCount = null;
  	ArrayList[][] previousSquareCount = null;
  
  	int[] flux = null;
  	int[][][] moveOverFlux = null;
  	int[][][] squareFlux = null;
  	
  	ArrayList[] outNorth = null;
  	ArrayList[] outSouth = null;
  	ArrayList[] outWest = null;
  	ArrayList[] outEast = null;

  	DataContainer dataContainer;

	/**
	*
	*/  
		
  	public void writeData(FileWriter fileWriter, DataContainer dataContainer ) throws Exception
  	{
    	this.dataContainer = dataContainer;
    	parseData(fileWriter);
    
    	StringBuffer buffer = new StringBuffer();
  	} 

	/**
	*
	*/  

  	void parseData(FileWriter fileWriter) throws Exception
  	{
    	setUpGridBoundaries(); 
    	StringBuffer buffer = new StringBuffer();
    
    	buffer.append("Frame ID, Square ID, Count, Flux North, Flux South, Flux East, Flux West, Sq. Hor. ID, Sq. Vert. ID");
    	buffer.append(System.getProperty("line.separator"));
    	
    	fileWriter.write(buffer.toString(), 0, buffer.length());
    	buffer = new StringBuffer();
    	int counter = GlobalSettings.getInstance().frameSkipOnOutput;
    	moveOverFlux = new int[gridLimit_H_Low.length][gridLimit_H_Low.length][4];
    	
    	for(int i=0; i < dataContainer.frames.size(); i++ )
    	{
      		buffer = new StringBuffer();
      		FrameData frameData = (FrameData)dataContainer.frames.get(i);
      		squareCount = new ArrayList[gridLimit_H_Low.length][gridLimit_V_Low.length];
      		fillInSquares(frameData);
      		fillFlux();
      		appendFlux();
      		
      		if(counter < GlobalSettings.getInstance().frameSkipOnOutput)
      		{
        		counter++;
      		}
      		else
      		{
        		dumpSquares(fileWriter, frameData.frameId);
        		counter = 1;
      		}
      		previousSquareCount = squareCount;
    	}
    	fileWriter.flush();
    	fileWriter.close();
  	}
  
	/**
	*
	*/  
		
  	void dumpSquares(FileWriter fileWriter, int frameId) throws Exception
  	{
    	int squareId = 0;
    	
    	for(int j=0; j < gridLimit_V_Low.length; j++)
    	{
      		for(int i=0; i < gridLimit_H_Low.length; i++)
      		{
        		squareId = gridLimit_H_Low.length * j + i+1;
        		StringBuffer buffer = new StringBuffer();
        		buffer.append(frameId+",");
        		buffer.append(squareId+",");
        		buffer.append(squareCount[i][j].size()+",");
        		flux = moveOverFlux[i][j];
  
        		for(int k = 0; k < 4; k++)
        		{
          			buffer.append(flux[k]+",");
          			// reset flux data
        		}
        		buffer.append(i+","+j);
        		buffer.append(System.getProperty("line.separator"));
        		fileWriter.write(buffer.toString(), 0, buffer.length());
      		}
    	}
    	for(int i=0; i < gridLimit_H_Low.length; i++)
    	{
      		for(int j=0; j < gridLimit_V_Low.length; j++)
      		{
        		for(int k = 0; k < 4; k++)
        		{
          			moveOverFlux[i][j][k] = 0;
        		}
      		}
    	}
  	}
  
	/**
	*
	*/  
		
  	void appendFlux()
  	{
    	if(squareFlux == null)
    	{
      		return;
    	}
    	for(int i=0; i < gridLimit_H_Low.length; i++)
    	{
      		for(int j=0; j < gridLimit_V_Low.length; j++)
      		{
        		for(int k = 0; k < 4; k++)
        		{
          			moveOverFlux[i][j][k] += squareFlux[i][j][k];
        		}
      		}
    	}
    	for(int i=0; i < gridLimit_H_Low.length; i++)
    	{
      		moveOverFlux[i][0][NORTH] -= outNorth[i].size();
      		moveOverFlux[i][gridLimit_V_Low.length-1][SOUTH] -= outSouth[i].size();
    	}
    	for(int j=0; j < gridLimit_V_Low.length; j++)
    	{
      		moveOverFlux[0][j][EAST] -= outEast[j].size();
      		moveOverFlux[gridLimit_H_Low.length-1][j][WEST] -= outWest[j].size();
    	}
  	}
  
	/**
	*
	*/  
		
  	void fillInSquares(FrameData frameData)
  	{
   		for(int i=0; i < gridLimit_H_Low.length; i++)
   		{
      		for(int j=0; j < gridLimit_V_Low.length; j++)
      		{
        		squareCount[i][j] = new ArrayList();
      		}
    	}
    	outNorth = new ArrayList[gridLimit_H_Low.length];
    	outSouth = new ArrayList[gridLimit_H_Low.length];
    	outEast = new ArrayList[gridLimit_V_Low.length];
    	outWest = new ArrayList[gridLimit_V_Low.length];
    
    	for(int i=0; i < gridLimit_H_Low.length; i++)
    	{
      		outNorth[i] = new ArrayList();
      		outSouth[i] = new ArrayList();
    	}
    	for(int i=0; i < gridLimit_V_Low.length; i++)
    	{
      		outWest[i] = new ArrayList();
      		outEast[i] = new ArrayList();
    	}

    	// find locations of the points
    	for(int i=0; i < frameData.framePoints.size(); i++ )
    	{
      		PointData pointData = (PointData)frameData.framePoints.get(i);
        	int x = check(pointData.x, gridLimit_H_Low, gridLimit_H_High);
	        int y = check(pointData.y, gridLimit_V_Low, gridLimit_V_High);
        	if(! checkOutOfFrame(pointData, x, y))
        	{
          		squareCount[x][y].add(new Integer(pointData.pointId));
        	}
    	}
  	}
  
  	void fillFlux()
  	{
    	for(int i=0; i < gridLimit_H_Low.length; i++)
    	{
      		for(int j=0; j < gridLimit_V_Low.length; j++)
      		{
        		Collections.sort(squareCount[i][j]);
      		}
    	}

    	if(previousSquareCount == null)
    	{
      		return;
    	}
      
    	squareFlux = new int[gridLimit_H_Low.length][gridLimit_H_Low.length][4];
      
	    // check flux from previous position:
	    //
	    //  previous  current 
	    //      i1   <   i   --> E
	    //      i1   >   i   --> W
	    //      j1   <   j   --> S
	    //      j1   >   j   --> N
	    //
	    //  reset the data
	    
    	for(int i=0; i < gridLimit_H_Low.length; i++)
    	{
      		for(int j=0; j < gridLimit_V_Low.length; j++)
      		{
        		for(int l=0; l < 4; l++)
        		{
          			squareFlux[i][j][l] =0;
        		}
      		}
    	}
    	for(int i=0; i < gridLimit_H_Low.length; i++)
    	{
      		for(int j=0; j < gridLimit_V_Low.length; j++)
      		{
        		flux = squareFlux[i][j];
          
        		int h_low = (i-1 >= 0 ? i-1 : i);
        		int h_high = (i+1 <= gridLimit_H_Low.length-1 ? i+1 : gridLimit_H_Low.length-1);
        		int v_low = (j-1 >=0 ? j-1 : j);
        		int v_high = (j+1 <= gridLimit_V_Low.length-1 ? j+1 : gridLimit_V_Low.length-1);
          
        		for(int k = 0; k < squareCount[i][j].size(); k++)
        		{
          			Integer id = (Integer)squareCount[i][j].get(k);
          			// boundaries of neighbourhood
          
          			// fill flux for neighbouring squares
          			for(int i1 = h_low; i1 <= h_high; i1++)
          			{
            			for(int j1 = v_low; j1 <= v_high; j1++)
            			{
              				// do not examine the same position
              				if(i == i1 && j == j1)     
              				{
                				continue;
              				}
              				ArrayList list = previousSquareCount[i1][j1];
              
             	 			// has point crossed the boundaries?
              				if(Collections.binarySearch(list, id) > -1)
              				{
                				if(j1 == j)
                				{
                  					if(i < i1)
                  					{
                    					flux[WEST] +=1;
                    					squareFlux[i1][j1][EAST] -= 1;
                  					}
                  					else
                  					{
                    					flux[EAST] +=1;
                    					squareFlux[i1][j1][WEST] -= 1;
                  					}
                				}
                				else if(i1 == i)
                				{
                  					if(j < j1)
                  					{
                    					flux[SOUTH] +=1;
                    					squareFlux[i1][j1][NORTH] -= 1;
                  					}
                  					else
                  					{
                    					flux[NORTH] +=1;
                    					squareFlux[i1][j1][SOUTH] -= 1;
                  					}
                				}
                				else
                				{
                  					if(j < j1)
                  					{
                    					flux[SOUTH] +=2;
                    					squareFlux[i1][j1][NORTH] -= 2;
                  					}
                  					else
                  					{
                    					flux[NORTH] +=2;
                    					squareFlux[i1][j1][SOUTH] -= 2;
                  					}
                  					if(i < i1)
                  					{
                    					flux[WEST] +=2;
                    					squareFlux[i1][j1][EAST] -= 2;
                  					}
                  					else
                  					{
                    					flux[EAST] +=2;
                    					squareFlux[i1][j1][WEST] -= 2;
                  					}
                				}
              				}
            			}
          			} // end of neigbourhood
        		} // end of points in square in previous frame
      		}
    	}
  	}
  
	/**
	*
	*/
	  
  	void setUpGridBoundaries()
  	{
    	int gVSize = GlobalSettings.getInstance().headCountGridVerticalSize;
    	int gHSize = GlobalSettings.getInstance().headCountGridHorizontalSize;
    	       
    	gridLimit_H_Low = new int[gHSize];
    	gridLimit_H_High = new int[gHSize];
    	gridLimit_V_Low = new int[gVSize];
    	gridLimit_V_High = new int[gVSize];
    	
    	int h_size = dataContainer.frameHSize / gHSize;
    	int v_size = dataContainer.frameVSize / gVSize;

    	for(int i=0; i < gridLimit_H_Low.length; i++)
    	{
      		if(i == 0)
      		{
        		gridLimit_H_Low[i] = 0;
        		gridLimit_H_High[i] = h_size;
      		}
      		else
      		{
        		gridLimit_H_Low[i] = gridLimit_H_Low[i-1] +  h_size;
        		gridLimit_H_High[i] = gridLimit_H_High[i-1] +  h_size;
      		}
    	}
    	for(int i=0; i < gridLimit_V_Low.length; i++)
    	{
      		if(i == 0)
      		{
        		gridLimit_V_Low[i] = 0;
        		gridLimit_V_High[i] = v_size;
      		}
      		else
      		{
        		gridLimit_V_Low[i] = gridLimit_V_Low[i-1] +  v_size;
        		gridLimit_V_High[i] = gridLimit_V_High[i-1] +  v_size;
      		}
    	}
    
    	// collect rounding errors in last grid pane
    	gridLimit_H_High[gHSize-1] = dataContainer.frameHSize;
    	gridLimit_V_High[gVSize-1] = dataContainer.frameVSize;
  	}
  
	/**
	*
	*/  
  
  	int check(int position, int[] gridLimit_Low, int[] gridLimit_High)
  	{
		int length = gridLimit_Low.length;      
    	int index = length / 2;
    	int deltaLeft;
    	int deltaRight;
    	int base = 0;
    
    	// there are some problems on the boundaries when point moves out of the frame ...
    	if(position > gridLimit_High[gridLimit_High.length -1])
    	{
      		return gridLimit_High.length + 1;
    	}

    	if(position < gridLimit_Low[0])
    	{
      		return -1;
    	}
              
    	while(true)
    	{
      		deltaLeft = length - (index - base);
      		deltaRight = length - deltaLeft;
            
	      	if(position < gridLimit_Low[index])
	      	{
	        	base = index - deltaLeft;
	        	if(deltaLeft == 1)
	        	{
	          		index--;
	        	}
	        	else
	        	{
	          		index -= deltaLeft/2;
	        	}
	        	length = deltaLeft;
	        
	        	continue;
	      	}
	      
	      	if(position > gridLimit_High[index])
	      	{
	        	base = index;
	        	if(deltaRight == 1)
	        	{
	          		index++;
	        	}
	        	else
	        	{
	          		index += deltaRight/2;
	        	}
	        	length = deltaRight;
	
	        	continue;
	      	}
	      
			return index;
    	}
 	}
  
  	boolean checkOutOfFrame(PointData pointData, int x, int y)
  	{
    
    	int h_length = gridLimit_H_Low.length;      
    	int v_length = gridLimit_V_Low.length;      
    	if(x >= 0 && y >= 0 && x < h_length && y < v_length)
    	{
      		return false;
    	}
    
	    if(x < 0)
	    {
	      	if(y < 0)
	      	{
	        	outEast[0].add(new Integer(pointData.pointId));
	        	outNorth[0].add(new Integer(pointData.pointId));
	        }       
	      	else
	      	{
	        	if(y >= v_length)
	        	{
	          		outEast[v_length-1].add(new Integer(pointData.pointId));
	          		outSouth[0].add(new Integer(pointData.pointId));
	        	}
	        	else
	        	{
	        		outEast[y].add(new Integer(pointData.pointId));
	        	}
	      	}
	    }
	    else if(x >= h_length)
	    {
	      	if(y < 0)
	      	{
	        	outWest[0].add(new Integer(pointData.pointId));
	        	outNorth[h_length-1].add(new Integer(pointData.pointId));
	      	}
	      	else
	      	{
	        	if(y >= v_length)
	        	{
	          		outWest[v_length-1].add(new Integer(pointData.pointId));
	          		outSouth[h_length-1].add(new Integer(pointData.pointId));
	        	}
	        	else
	        	{
	        		outWest[y].add(new Integer(pointData.pointId));
	        	}
	      	}
	    }
	    else
	    {
	 		if(y >= v_length)
	 		{
	        	outSouth[x].add(new Integer(pointData.pointId));
	      	}  
	      	else
	      	{
	        	outNorth[x].add(new Integer(pointData.pointId));
	      	}
	    }
	    return true;
	}

}
