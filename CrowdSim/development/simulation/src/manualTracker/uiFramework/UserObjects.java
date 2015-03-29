package manualTracker.uiFramework;

import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import manualTracker.data.PointData;
import manualTracker.renderer.ProjectionScreen;

/**
* 
* A container for user generated visual objects. It also handles all conversions from
* frame coordinates to screen coordinates and vice versa.
* 
* NOTE: video frame data are integral values and screen data are integral values. Yet difference in range of the data
* may be significant, up to order of magnitude. It results in large rounding error represented as shift between  
* mouse motion and position of marker when mouse released. Some test show that if ratio of dimensions screen/frame
* is five, undesired shift of position may be six pixels.
*
*/

public class UserObjects 
{
	ArrayList targetVisualObjects = new ArrayList();
  	BufferedImage background = null;
  	BufferedImage dragBackground = null;
  	ProjectionScreen displayComponent = null;

	/**
	* flag if currently an object is dragged
	*/
	   
  	public boolean isDragging = false;
  	public boolean startDragging = false;
  	public int frameHeight;
  	public int frameWidth;
  
	/**
	* Selection radius around the mouse center
	*/
  
  	TargetVisualObject selectedObject = null;
  
  	public void selectCurrentObject(int x, int y)
  	{
    	TargetVisualObject object = getObjectAt(x,y);
    	if(object != null)
    	{
        	selectedObject = object;
    	}
  	}
  
  	public TargetVisualObject getObjectAt(int x, int y)
  	{
    	int selectionRadius = GlobalSettings.getInstance().selectionRadius;
    	
    	for(int i=0; i < targetVisualObjects.size(); i++)
    	{
      		TargetVisualObject object = (TargetVisualObject)targetVisualObjects.get(i);
      		
      		if((object.screen_x <= x + selectionRadius) && (object.screen_x >= x - selectionRadius) && (object.screen_y <= y + selectionRadius) && (object.screen_y >= y - selectionRadius))
      		{
        		return object;
      		}
    	}
    	return null;
  	}
  
	/**
	* Resets dragging environment
	*
	*/
	
  	public void resetDragging()
  	{
    	isDragging = false;
    	startDragging = false;
    	selectedObject = null;
  	}

	/**
	* 
	* @param x
	* @param y
	*/
	   
  	public void dragObject(int x, int y)
  	{
    	if(! isDragging && ! startDragging )
    	{
      		if(selectedObject == null)
      		{
        		selectCurrentObject(x, y);
        		isDragging = true;
        		startDragging = true;
      		}
    	}
    	if(selectedObject == null)
    	{
      		isDragging = false;
      		startDragging = false;
      		return;
    	}
    	
    	selectedObject.isDragged = true;
    	
    	if(startDragging)
    	{
      		selectedObject.x_drag_0 = selectedObject.screen_x;
      		selectedObject.y_drag_0 = selectedObject.screen_y;
	    }
    
    	selectedObject.x_drag_1 = x;
    	selectedObject.y_drag_1 = y;
    
    	if(x < 0 || y < 0 || x > displayComponent.getWidth() || y > displayComponent.getHeight())
    	{
      		selectedObject.outOfBoundary = true;
    	}
    	else
    	{
      		selectedObject.outOfBoundary = false;
    	}
    
    	displayComponent.paintMouseDragging();
    	startDragging = false;
  	}
  	
	/**
	* 
	* @param x
	* @param y
	*/
	
  	public void endDragging(int x, int y)
  	{
    	if(!isDragging)
    	{
      		return;
    	}
    	isDragging = false;
    	selectedObject.isDragged = false;
    	calculateFromScreenData(selectedObject, x, y);
    	selectedObject.status = PointData.MANUAL_CORRECTION;
    	dragBackground = null;
    	displayComponent.repaint();
    	selectedObject = null;
  	}

	/**
	* 
	* @param x
	* @param y
	* @param pointData
	*/
	
  	public void addNewObjectFromClick(int x, int y, PointData pointData)
  	{
    	TargetVisualObject object = new TargetVisualObject();
    	calculateFromScreenData(object, x, y);
    	object.tmpData = pointData;
    	object.displayComponent = displayComponent; 
    	targetVisualObjects.add(object);
    	displayComponent.repaint();
  	}
  	
	/**
	* 
	* @param object
	* @param x
	* @param y
	*/
	
  	private void calculateFromScreenData(TargetVisualObject object, int x, int y)
  	{
    	Rectangle screen = displayComponent.getBounds();
    	// editable value in frame coordinates 
    	object.x = (screen.x + x) * frameWidth /screen.width;
   	 	object.y = (screen.y + y) * frameHeight /screen.height;
    
    	object.screen_x = x;
    	object.screen_y = y;
    
    	if(x < 0 || y < 0 || x > displayComponent.getWidth() || y > displayComponent.getHeight())
    	{
      		object.outOfBoundary = true;
    	}
    	else
    	{
      		object.outOfBoundary = false;
    	}
    }
  
	/**
	* 
	* @param pointData
	*/
	
  	public void addObjectFromData(PointData pointData)
  	{
    	// do not copy objects that are already marked out of frame
    	if(pointData.outOfBoundary)
    	{
      		return;
    	}
    	// there are points that are estimated but not confirmed to be out of frame
    	if(pointData.x < 0 || pointData.y < 0 || pointData.x > frameWidth || pointData.y > frameHeight)
    	{
      		pointData.outOfBoundary = true;
    	}
    	else
    	{
      		pointData.outOfBoundary = false;
    	}
    
    	TargetVisualObject object = new TargetVisualObject();
    	object.x = pointData.x;
    	object.y = pointData.y;
    	object.outOfBoundary = pointData.outOfBoundary;
    	object.status = pointData.status;
    	object.tmpData = pointData;
   		object.displayComponent = displayComponent; 
    	targetVisualObjects.add(object);
	}
  
  	public void removeObject(TargetVisualObject object)
  	{  
    	targetVisualObjects.remove(object);
    	displayComponent.repaint();
  	}
  
	/**
	* Paints 
	* @param graphics
	*/
	
  	public void paint(Graphics graphics)
  	{
    	if(isDragging)
    	{
      		paintDragging(graphics);
      		return;
    	}
    	Rectangle screen = displayComponent.getBounds();
    	for(int i = 0; i < targetVisualObjects.size(); i++)
    	{
      		TargetVisualObject object =  (TargetVisualObject)targetVisualObjects.get(i);
      
      		// here is a significant problem: if screen.width/frameWidth is large, rounding error
      		// reaches values of even 10 pixels
      		
      		object.screen_x = object.x * screen.width/frameWidth - screen.x;
      		object.screen_y = object.y * screen.height/frameHeight - screen.y;
      
      		if(object.screen_x < 0)
      		{
        		object.screen_x = 1;
      		}
      		if(object.screen_x > screen.width)
      		{
        		object.screen_x = screen.width;
      		}
      		if(object.screen_y < 0)
      		{
        		object.screen_y = 1;
      		}
      		if(object.screen_y > screen.height)
      		{
        		object.screen_y = screen.height;
      		}
      		object.paint(graphics);
    	}
  	}

	/**
	* Dragging paint draws whole set of objects only first time
	* After that it draws only dragged object
	* 
	* @param graphics
	*/
	
  	void paintDragging(Graphics graphics)
  	{
    	if(startDragging)
    	{
      		startDragging = false;
      		dragBackground = displayComponent.getBackgroundBufferedImage();
      
      		for(int i = 0; i < targetVisualObjects.size(); i++)
      		{
        		TargetVisualObject object =  (TargetVisualObject)targetVisualObjects.get(i);
        		if(!object.isDragged)
        		{
          			object.paint(graphics);
        		}
        		else // clean up previous snapshot
        		{
          			object.previousBackground = null;
        		}
      		}
    	}
    	
    	// draw dragged marker
    	for(int i = 0; i < targetVisualObjects.size(); i++)
    	{
      		TargetVisualObject object =  (TargetVisualObject)targetVisualObjects.get(i);
      		if(object.isDragged)
      		{
        		object.dragBackground = dragBackground; 
        		object.paint(graphics);
      		}
    	}
  	}
}
