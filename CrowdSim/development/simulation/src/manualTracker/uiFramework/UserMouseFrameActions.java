package manualTracker.uiFramework;

import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import javax.swing.event.MouseInputListener;

public class UserMouseFrameActions implements MouseInputListener 
{
	VideoFrameEventHandler videoFrameEventHandler = null;
  	TrackerFrame trackerFrame = null;
  	boolean isDragging = false;

  	public void mouseClicked(MouseEvent event) 
  	{
    	if(videoFrameEventHandler.currentMarkerId == VideoFrameEventHandler.REMOVE)
    	{
      		if(! videoFrameEventHandler.isSingleData(event.getX(), event.getY()))
      		{
        		if(JOptionPane.showConfirmDialog(/*trackerFrame*/null, "Selected marker has several frames.\n Remove it anyway?", "WARNING !", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != 0)
        		{
          			return;
        		}
      		}
			videoFrameEventHandler.removeObject(event.getX(), event.getY());
      		return;
    	}
      
    	if(videoFrameEventHandler.currentMarkerId != VideoFrameEventHandler.SELECT)
    	{
      		videoFrameEventHandler.addObject(event.getX(), event.getY());
    	}
  	}

  	public void mouseEntered(MouseEvent event) 
  	{
  	}

  	public void mouseExited(MouseEvent event) 
  	{
  	}

  	public void mousePressed(MouseEvent event) 
  	{
    	isDragging = false;
    	videoFrameEventHandler.resetDragging();
  	}

  	public void mouseReleased(MouseEvent event) 
  	{
    	if(isDragging)
    	{
      		videoFrameEventHandler.endDragging(event.getX(), event.getY());
    	}
    
    	isDragging = false;
  	}

  	public void mouseDragged(MouseEvent event) 
  	{
    	isDragging = true;
    	videoFrameEventHandler.dragObject(event.getX(), event.getY());
  	}

  	public void mouseMoved(MouseEvent event) 
  	{
	}
}
