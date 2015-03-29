package manualTracker.renderer;

import java.awt.Component;
import java.awt.Dimension;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Duration;
import javax.media.EndOfMediaEvent;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.Time;
import javax.media.control.FramePositioningControl;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;
import manualTracker.uiFramework.TrackerActions;

public class VideoFrameAccess implements ControllerListener 
{

  	Processor processor = null;
  	FramePositioningControl framePositioningControl = null;
  	MediaLocator mediaLocator = null;
  	TrackerVideoRenderer trackerVideoRenderer = null;
  	TrackerActions trackerActions = null;
  
  	boolean stateTransitionOK = true;
  	int[] waitSync = new int[0];
  	boolean stateTransOK = true;
  	int totalFrames = -1;
  	Dimension frameSize = null;

	/**
	* 
	* @return
	*/
	
  	public TrackerVideoRenderer getTrackerVideoRenderer()
  	{
    	return trackerVideoRenderer;
  	}
  	
	/**
	* 
	* @param mediaFilename
	* @return
	*/
	
  	public static VideoFrameAccess getVideoFrameAccess(String mediaFilename)
  	{
    	VideoFrameAccess instance = null;
    	try
    	{
      		instance = new VideoFrameAccess(mediaFilename);
      		if(!instance.init())
      		{
        		instance = null;
      		}
    	}
    	catch(Exception ex)
    	{
    	}
    	return instance;
  	}
  	
  	private VideoFrameAccess(String mediaFilename) throws Exception
  	{
    	String url = mediaFilename;
    	if ( !(url.startsWith("file:") || url.startsWith("http:"))) 
    	{
      		url = "file:" + url;
    	}
    	mediaLocator = new MediaLocator(url);
  	}

  	private boolean init() throws Exception
  	{
    	processor = Manager.createProcessor(mediaLocator);
    	processor.addControllerListener(this);

    	// Put the Processor into configured state.
    	processor.configure();
    	
    	if (!waitForState(Processor.Configured)) 
    	{
      		System.err.println("Failed to configure the processor.");
      		return false;
    	}
    	System.err.println("Configured the processor.");

    	// So I can use it as a player.
    	processor.setContentDescriptor(null);

    	// Obtain the track controls.
    	TrackControl tc[] = processor.getTrackControls();

    	if (tc == null) 
    	{
      		System.err.println("Failed to obtain track controls from the processor.");
      		return false;
    	}
    
    	// Search for the track control for the video track.
    	TrackControl videoTrack = null;

    	for (int i = 0; i < tc.length; i++) 
    	{
      		if (tc[i].getFormat() instanceof VideoFormat) 
      		{
        		videoTrack = tc[i];
        		frameSize = ((VideoFormat)videoTrack.getFormat()).getSize();
        		break;
      		}
    	}

    	if (videoTrack == null) 
    	{
      		System.err.println("The input media does not contain a video track.");
      		return false;
    	}
    	System.err.println("Video format: " + videoTrack.getFormat());

    	trackerVideoRenderer = new TrackerVideoRenderer();
    	trackerVideoRenderer.videoFrameAccess = this;
    	videoTrack.setRenderer(trackerVideoRenderer);

		// Realize the processor.
    	processor.realize();

    	if (!waitForState(Processor.Realized)) 
    	{
        	System.err.println("Failed to realize the player.");
        	return false;
    	}

    	// Try to retrieve a FramePositioningControl from the player.
    	framePositioningControl = (FramePositioningControl)processor.getControl("javax.media.control.FramePositioningControl");

    	if (framePositioningControl == null) 
    	{
        	System.err.println("The player does not support FramePositioningControl.");
        	return false;
    	}
    
    	Time duration = processor.getDuration();
    	if (duration != Duration.DURATION_UNKNOWN) 
    	{
   		    totalFrames = framePositioningControl.mapTimeToFrame(duration);
        	if (totalFrames != FramePositioningControl.FRAME_UNKNOWN);
    	}    
    
    	processor.prefetch();
    	
    	if (!waitForState(Processor.Prefetched)) 
    	{
      		System.err.println("Failed to realize the processor.");
      		return false;
    	}
    
    	processor.stop();
    	framePositioningControl.seek(1);
    
    	return true;
  	}
  	
	/**
	* 
	* @param trackerActions
	*/
	
	public void setActionHandler(TrackerActions trackerActions)
	{
    	this.trackerActions = trackerActions;
    	trackerVideoRenderer.setActionHandler(trackerActions);
   	}
  
	/**
	* 
	* @return visual compoent that can be displayed 
	*/
   
  	public Component getVisualComponent()
  	{
    	return processor.getVisualComponent();
  	}
  
	/**
	* Moves media forward or backward given number of frames
	* @param count of frames. Negative number moves back @seealso javax.media.control.FramePositioningControl
	*/
	
  	public void skip(int count)
  	{
    	processor.stop();
    	framePositioningControl.skip(count);
    	System.gc();
  	}
  
	/**
	* Positions media to beginning of the run
	*
	*/
	
  	public void rewind()
  	{
    	processor.stop();
    	framePositioningControl.seek(0);
    	processor.getVisualComponent().repaint();
    	System.gc();
  	}
  
	/**
	* Positions media to the end of the run
	*
	*/
  	
  	public void wind()
  	{
    	if (totalFrames != FramePositioningControl.FRAME_UNKNOWN)
    	{
      		processor.stop();
      		framePositioningControl.seek(totalFrames);
      		processor.getVisualComponent().repaint();
    	}
    	System.gc();
  	}
  
	/**
	* pauses play
	*
	*/
	
  	public void pause()
  	{
    	processor.stop();
  	}

	/**
	* starts / continues play
	*
	*/
	
  	public void start()
  	{
    	processor.start();
  	}

	/**
	* finishes play
	*
	*/
	
  	public void stop()
  	{
    	processor.stop();
    	processor.close();
  	}
  
	/**
	* 
	* @return  frameId: The frame returned is the nearest frame that has a media time less than or equal to the given media time
	*/
	
  	public int getFrameId()
  	{
    	int currentFrame = framePositioningControl.mapTimeToFrame(processor.getMediaTime());
    	if (currentFrame != FramePositioningControl.FRAME_UNKNOWN)
    	{
      		return currentFrame;
    	}
    	return 0;
  	}
  
	/**
	* 
	* @return media time in nanoseconds
	*/
	
  	public long getMediaTime()
  	{
    	return processor.getMediaNanoseconds();
  	}
  
	/**
	* @return media frame size
	*/
	
  	public Dimension getFrameSize()
  	{
    	return frameSize;
  	}

	/**
	* @param evt
	*/
	
  	public void controllerUpdate(ControllerEvent evt)
  	{
    	if (evt instanceof ConfigureCompleteEvent || evt instanceof RealizeCompleteEvent || evt instanceof PrefetchCompleteEvent) 
    	{
        	synchronized (waitSync) 
        	{
          		stateTransitionOK = true;
          		waitSync.notifyAll();
        	}
    	} 
    	else if (evt instanceof ResourceUnavailableEvent) 
    	{
        	synchronized (waitSync) 
        	{
      			stateTransitionOK = false;
      			waitSync.notifyAll();
        	}
    	} 
    	else if (evt instanceof EndOfMediaEvent) 
    	{
      		processor.stop();
      		trackerActions.endReached();
    	}
  	}

	/**
	* Block until the processor has transitioned to the given state.
	* Return false if the transition failed.
	*/
	
  	public boolean waitForState(int state) 
  	{
    	synchronized (waitSync) 
    	{
      		try 
      		{
        		while ( processor.getState() != state && stateTransOK ) 
        		{
          			waitSync.wait();
        		}
      		} 
      		catch (Exception ex) 
      		{
      		}
        
      		return stateTransOK;
    	}
  	}
}