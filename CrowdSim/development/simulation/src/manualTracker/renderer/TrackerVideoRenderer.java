package manualTracker.renderer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.MemoryImageSource;
import javax.media.Buffer;
import javax.media.Control;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import com.sun.media.renderer.video.AWTRenderer;
import manualTracker.uiFramework.TrackerActions;

/**
 * 
 * Wraps actual video renderer i.e. produces image from the video buffer 
 * that is painted onto screen
 *
 */

public class TrackerVideoRenderer extends AWTRenderer 
{
	private static final String name = "TrackerVideoRenderer";

  	protected Buffer lastBuffer = null;
  	protected int inWidth = 0;
  	protected int inHeight = 0;
  	protected Rectangle reqBounds = null;
  	protected Rectangle bounds = new Rectangle();
  	protected MemoryImageSource sourceImage;
  	protected Image     destinationImage;
  	protected RGBFormat inputFormat;
  	protected RGBFormat supportedRGB;
  	protected Format [] supportedFormats;
  	
  	ProjectionScreen projectionScreen = null;
  	VideoImage videoImage = new VideoImage();
  	VideoFrameAccess videoFrameAccess = null; 
  	TrackerActions trackerActions = null;
  
  	public TrackerVideoRenderer()
  	{
    	// Prepare supported input formats and preferred format
    	int rMask = 0x000000FF;
    	int gMask = 0x0000FF00;
    	int bMask = 0x00FF0000;

    	supportedRGB = new RGBFormat(null,        	// size
               Format.NOT_SPECIFIED,  				// maxDataLength
               int[].class,     					// buffer type
               Format.NOT_SPECIFIED,  				// frame rate
               32,        							// bitsPerPixel
               rMask, gMask, bMask,   				// component masks
               1,         							// pixel stride
               Format.NOT_SPECIFIED,  				// line stride
               Format.FALSE,          				// flipped
               Format.NOT_SPECIFIED   				// endian
               );
    	
    	supportedFormats = new VideoFormat[1];
    	supportedFormats[0] = supportedRGB;
  	}
  	
	/**
	* Returns the region in the component where the video will be
	* rendered to. Returns null if the entire component is being used.
	*/
   
  	public Rectangle getBounds() 
  	{
    	return reqBounds;
  	}
  	public void setActionHandler(TrackerActions trackerActions)
  	{
    	this.trackerActions = trackerActions;
   	}

	/**
	* Returns an AWT component that it will render to. Returns null
	* if it is not rendering to an AWT component.
	*/
	
  	public Component getComponent() 
  	{
    	if (projectionScreen == null) 
    	{
      		projectionScreen = new ProjectionScreen();
      		((ProjectionScreen)projectionScreen).trackerVideoRenderer = this;

      		System.err.println("SCREEN CREATED");    
    	}
    	return projectionScreen;
  	}
  	
	/**
	* Sets the region in the component where the video is to be
	* rendered to. Video is to be scaled if necessary. If <code>rectangle</code>
	* is null, then the video occupies the entire component.
	* 
	* @param rectangle 
	*/
   
  	public void setBounds(Rectangle rectangle) 
  	{
    	reqBounds = rectangle;
  	}

	/**
	* Requests the manualTracker.renderer to draw into a specified AWT component.
	* Returns false if the manualTracker.renderer cannot draw into the specified
	* component.
	* 
	* @param component
	*/
	
  	public boolean setComponent(Component component) 
  	{
    	this.projectionScreen = (ProjectionScreen)component;
    	return true;
  	}

  	public Format[] getSupportedInputFormats() 
  	{
    	return supportedFormats;
  	}

  	public int process(Buffer buffer) 
  	{
    	if ( buffer.getLength() <= 0 )
    	{ 
      		return BUFFER_PROCESSED_OK;
    	}
    	if (projectionScreen == null)
    	{
      		return BUFFER_PROCESSED_FAILED;
    	}
    	
    	Format inf = buffer.getFormat();
    	
    	if (inf == null)
    	{
        	return BUFFER_PROCESSED_FAILED;
    	}
    	if (inf != inputFormat || !buffer.getFormat().equals(inputFormat)) 
    	{
        	if (setInputFormat(inf) != null)
        	{
        		return BUFFER_PROCESSED_FAILED;
        	}
   	 	}
    
    	Object data = buffer.getData();
    	if (!(data instanceof int[]))
    	{
      		return BUFFER_PROCESSED_FAILED;
    	}
    
    	if (lastBuffer != buffer) 
    	{
      		lastBuffer = buffer;
    	}
    
    	trackerActions.newFrameEvent(videoFrameAccess.getFrameId());
    	paintScreen(buffer);
    
    	return BUFFER_PROCESSED_OK;
  	}

  	public Format setInputFormat(Format format) 
  	{
    	if (format != null && format instanceof RGBFormat && format.matches(supportedRGB)) 
    	{
      		inputFormat = (RGBFormat) format;
      		Dimension size = inputFormat.getSize();
      		inWidth = size.width;
      		inHeight = size.height;
      
      		return format;
    	} 
    	else
    	{
      		return null;
    	}
  	}

  	public void start() 
  	{
	}

  	public void stop() 
  	{
  	}

  	public void close() 
  	{
  	}

  	public String getName() 
  	{
    	return name;
  	}

  	public void open() throws ResourceUnavailableException 
  	{
    	sourceImage = null;
    	destinationImage = null;
    	lastBuffer = null;
  	}

  	public void reset() 
  	{
  	}

	/**
	* Return the control based on a control type for the PlugIn.
	*/
  
  	public Object getControl(String controlType) 
  	{
     	try 
     	{
        	Class  cls = Class.forName(controlType);
        	Object cs[] = getControls();
        	for (int i = 0; i < cs.length; i++) 
        	{
	           	if (cls.isInstance(cs[i]))
	           	{
	              return cs[i];
	           	}
        	}
        	return null;
     	} 
     	catch (Exception e) 
     	{   // no such controlType or such control
       		return null;
     	}
  	}
  
  	public Object[] getControls() 
  	{
    	// No controls
    	return (Object[]) new Control[0];
  	}

  	public int getInWidth() 
  	{
    	return inWidth;
  	}

  	public int getInHeight() 
  	{
    	return inHeight;
  	}
  
  	void paintScreen(Buffer buffer)
  	{
    	if(projectionScreen == null)
    	{
      		projectionScreen = (ProjectionScreen)getComponent();
    	}
    	
    	Graphics g = projectionScreen.getGraphics();
    	
    	if (g == null)
    	{ 
      		return;
    	}

    	projectionScreen.setImage(videoImage.getVideoImageSource(buffer, inWidth, inHeight));
    	projectionScreen.paint(g);
    
  	}
  
}
