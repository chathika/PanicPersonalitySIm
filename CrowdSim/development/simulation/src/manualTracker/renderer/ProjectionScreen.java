package manualTracker.renderer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import javax.imageio.ImageIO;
import manualTracker.uiFramework.GlobalSettings;
import manualTracker.uiFramework.UserObjects;

public class ProjectionScreen extends Canvas 
{ 
 	TrackerVideoRenderer trackerVideoRenderer = null;
  
  	static final long serialVersionUID = 0;
  
  	public boolean click = false;
  	
  	Image videoImage = null;
  	
  	BufferedImage background = null;
  	UserObjects userObjects = null;
  	GlobalSettings globalSettings = null;
  
  	public Dimension getPreferredSize() 
  	{
    	return new Dimension(trackerVideoRenderer.getInWidth(), trackerVideoRenderer.getInHeight());
  	}
  	
	/**
	* Sets video image to be displayed
	* @param videoImage
	*/
	
  	public void setImage(Image videoImage)
  	{
	    this.videoImage = videoImage;
	    prepareImage(videoImage, this);
	    background = new BufferedImage(videoImage.getWidth(this), videoImage.getHeight(this),BufferedImage.TYPE_INT_RGB);
	    background.getGraphics().drawImage(videoImage, 0, 0, this);
  	}
  	
  	public BufferedImage getBackgroundBufferedImage()
  	{
	    BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(),BufferedImage.TYPE_INT_RGB);
	    image.getGraphics().drawImage(videoImage, 0,0, this.getWidth(), this.getHeight(),this);
	    return image;
  	}
  	
  	public void setImage(MemoryImageSource memoryImageSource)
  	{
    	setImage(createImage(memoryImageSource));
  	}
  
  	public void exportImage(String path)
  	{
    	if(globalSettings == null)
    	{
      		globalSettings = GlobalSettings.getInstance();
    	}
    
    	try
    	{
      		File outputFile = new File(path+"."+globalSettings.imageExportFormat);
      		Graphics g = background.getGraphics();
      		paintGrid(g, background.getHeight(), background.getWidth());
    		ImageIO.write(background, globalSettings.imageExportFormat.toUpperCase(), outputFile);
    	}
    	catch(Exception ex)
    	{
      		ex.printStackTrace();
    	}
  	}
  
  	public void setUserObjects(UserObjects userObjects)
  	{
    	this.userObjects = userObjects;
  	}
  
  	public void update(Graphics g)
  	{
	}
  
  	public void repaint() 
  	{
    	Graphics g = getGraphics();
    	paint(g);
  	}
  	
	/**
	*  Skips painting background during the drag, only user objects are drawn as it is significalntly
	*  shorter operation
	*
	*/
   
  	public void paintMouseDragging() 
  	{
    	Graphics g = getGraphics();
    	userObjects.paint(g);
    	paintGrid(g);
  	}
  	
	/**
	* 
	* @param g
	*/
	
  	public void paintGrid(Graphics g, int height, int width)
  	{
    	if(globalSettings == null)
    	{
      		globalSettings = GlobalSettings.getInstance();
    	}
   		if(!globalSettings.gridOn)
   		{
     		return;
   		}
   
   		Color oldColor = g.getColor();
   		g.setColor(globalSettings.gridColor);
   
   		int h_count = globalSettings.gridHorizontalSize;
   		int v_count = globalSettings.gridVerticalSize;
   		int hShift = width / h_count;
   		int vShift = height / v_count;
   
   		for(int i = 1; i < h_count; i++)
   		{
    	 	g.drawLine(i*hShift, 0, i*hShift, this.getHeight());
   		}
   
   		for(int i = 1; i < v_count; i++)
   		{
     		g.drawLine(0, i*vShift, this.getWidth(), i*vShift);
   		}
   
   		g.setColor(oldColor);
   
  	}
  	
	/**
	* 
	* @param g
	*/
	
  	public void paintGrid(Graphics g)
  	{
    	paintGrid(g, this.getHeight(), this.getWidth());
  	}
  	
	/**
	* 
	* @param g
	*/
	
  	public void paint(Graphics g)
  	{
    	paintBackground(g);  
    	userObjects.paint(g);
    	paintGrid(g);
  	}
  	
	/**
	* 
	* @param g
	*/
	
	public void paintBackground(Graphics g)
  	{
    	if(background == null)
    	{
      		return; 
    	}
    	if (g != null) 
    	{
      		if (trackerVideoRenderer.reqBounds == null) 
      		{
	        	trackerVideoRenderer.bounds = getBounds();
	        	trackerVideoRenderer.bounds.x = 0;
	        	trackerVideoRenderer.bounds.y = 0;
      		} 
      		else
      		{
        		trackerVideoRenderer.bounds = trackerVideoRenderer.reqBounds;
      		}
      	
      		g.drawImage(background, trackerVideoRenderer.bounds.x, 
        
        	trackerVideoRenderer.bounds.y,  trackerVideoRenderer.bounds.width, trackerVideoRenderer.bounds.height, 0, 0, trackerVideoRenderer.getInWidth(), trackerVideoRenderer.getInHeight(), this);
    	}
  	}
}
