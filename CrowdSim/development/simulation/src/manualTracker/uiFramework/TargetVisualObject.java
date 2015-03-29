package manualTracker.uiFramework;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import manualTracker.data.PointData;
import manualTracker.renderer.ProjectionScreen;

/**
* 
* User generated target visual data container, represents single target in the video 
*
*/
 
public class TargetVisualObject 
{
	
	/**
	* flag if currently an object is dragged
	*/
  	public boolean isDragged = false;
  	public boolean isMoved = false;
  	public String status = PointData.FIRST_ENTRY;

	/**
	* current position in the frame coordinates (editable value)
	*/
	
  	public int x;
  	public int y;
  
	/**
	* Flag set when point runs out of frame boundaries
	*/
	
  	public boolean outOfBoundary = false;

	/**
	* dragging state coordinates
	*/
	
  	public int x_drag_0;
  	public int y_drag_0;
  	public int x_drag_1;
  	public int y_drag_1;

	/**
	* current position in the screen coordinates
	*/
	
  	public int screen_x;
  	public int screen_y;
  
	/**
	* original position (if reverted). Also a position to be saved
	*/
	
  	public PointData tmpData;
  	public ArrayList previousPositions = null;
  	
  	BufferedImage previousBackground = null; 
  	BufferedImage dragBackground = null;
  	ProjectionScreen displayComponent = null;

	/**
	* 
	* @param graphics
	*/
	
  	public void paintDragged(Graphics graphics)
  	{
    	int markerWidth = GlobalSettings.getInstance().markerWidth;
    	int markerHeight = GlobalSettings.getInstance().markerHeight;

    	Graphics2D g = (Graphics2D)graphics;
    
    	int w = markerWidth+2;
    	int h = markerHeight+2;
    	int dx = w / 2;
    	int dy = h / 2;
    	int top_x = x_drag_0 - dx;
    
    	if(top_x < 0)
    	{
      		top_x = 0;
    	}
    	
    	int top_y = y_drag_0 - dy;
    	
    	if(top_y < 0)
    	{
      		top_y = 0;
    	}
    
    	int b_w = dragBackground.getWidth();
    	int b_h = dragBackground.getHeight();
    
    	if(top_x+w > b_w)
    	{
      		w = b_w - top_x;
    	}
    
    	if(top_y+h > b_h)
    	{
      		h = b_h - top_y;
    	}
    	
    	if(previousBackground != null)
    	{
      		g.drawImage(previousBackground, top_x,  top_y, previousBackground.getWidth(), previousBackground.getHeight(), displayComponent);
   	 	}  
    
    	// repeat calculation for future snapshot
    	w = markerWidth+2;
    	h = markerHeight+2;
    	dx = w / 2;
    	dy = h / 2;
    	top_x = x_drag_1 - dx;
    
    	if(top_x < 0)
    	{
      		top_x = 0;
    	}
    
    	top_y = y_drag_1 - dy;
    	
    	if(top_y < 0)
    	{
      		top_y = 0;
    	}
    
    	b_w = dragBackground.getWidth();
    	b_h = dragBackground.getHeight();
    
    	if(top_x+w > b_w)
    	{
      		w = b_w - top_x;
    	}
    	
    	if(top_y+h > b_h)
    	{
      		h = b_h - top_y;
    	}
    
    	try
    	{
    		previousBackground = dragBackground.getSubimage(top_x, top_y, w,  h); 
    	}
    	catch(Exception ex)
    	{
      		previousBackground = null;
    	}
    	
    	dragBackground = null; 
    	x_drag_0 = x_drag_1; 
    	y_drag_0 = y_drag_1;
	    paintMarker(g, x_drag_1, y_drag_1);
  	}

	/**
	* 
	* @param graphics
	*/
	
 	public void paint(Graphics graphics)
 	{
    	Graphics2D g = (Graphics2D)graphics;
    	if(isDragged)
    	{
      		isMoved = true;
      		paintDragged(g);
      		return;
    	}
	    paintMarker(g, screen_x, screen_y);
  	}
  
	/**
	* 
	* @param graphics
	* @param x
	* @param y
	*/
	
 	public void paintMarker(Graphics graphics, int x, int y)
 	{
    	Graphics2D g = (Graphics2D)graphics;
    	Color markerColor = (isMoved ? GlobalSettings.getInstance().markerChangedColor : GlobalSettings.getInstance().markerColor);
    	g.setColor(markerColor);
    
    	int markerWidth = GlobalSettings.getInstance().markerHeight; //imports settings for width
    	int markerHeight = GlobalSettings.getInstance().markerWidth; //imports settings for height
    	int dx = markerWidth / 2;
    	int dy = markerHeight / 2;
    	switch(this.tmpData.markerId) //marker selection options
    	{
    		case 1: //marker 1
      			g.drawRect(x-dx, y-dy,  markerWidth, markerHeight);
      			g.drawLine(x-dx, y, x+dx, y);
      			g.drawLine(x, y-dy, x, y+dy);
     			break;
    		
    		case 2: //marker 2
		      g.drawOval(x-dx, y-dy,  markerWidth, markerHeight);
		      g.drawLine(x-dx, y, x+dx, y);
		      g.drawLine(x, y-dy, x, y+dy);
		      break;
    
    		case 3: //marker 3
		      g.drawLine(x-dx, y-dy, x+dx, y+dy);
		      g.drawLine(x+dx, y-dy, x-dx, y+dy);
		      break;
    
    		case 4: //marker 4
		      g.drawLine(x-dx, y, x+dx, y);
		      g.drawLine(x, y-dy, x, y+dy);
		      break;
    
    		case 5: //marker 5
		      g.drawLine(x-dx, y, x+dx, y);
		      g.drawLine(x, y-dy, x, y+dy);
		      g.drawLine(x-dx, y+dy, x, y-dy);
		      g.drawLine(x+dx, y+dy, x, y-dy);
		      break;
    
    		case 6: //marker 6
		      g.drawLine(x-dx, y, x, y+dy);
		      g.drawLine(x+dx, y, x, y+dy);
		      g.drawLine(x-dx, y, x, y-dy);
		      g.drawLine(x+dx, y, x, y-dy);
		      break;
    
    		case 7: //marker 7
		      g.drawLine(x-dx, y, x+dx, y);
		      g.drawLine(x, y-dy, x, y+dy);
		      g.drawLine(x, y+dy, x-dx, y-dy);
		      g.drawLine(x, y+dy, x+dx, y-dy);
		      break;
		      
    		case 8: //marker 8
		      g.drawLine(x-dx, y, x+dx, y);
		      g.drawLine(x, y-dy, x, y+dy);
		      g.drawLine(x-dx, y-dy, x+dx, y+dy);
		      g.drawLine(x+dx, y-dy, x-dx, y+dy);
		      break;
    
    		default: 
		      g.drawLine(x-dx, y, x+dx, y);
		      g.drawLine(x, y-dy, x, y+dy);
		      break;
    	}
  	}
}
