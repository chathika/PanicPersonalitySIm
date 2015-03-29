package manualTracker.renderer;

import javax.media.Buffer;

public class PostAccessCodec extends PreAccessCodec 
{
	public PostAccessCodec (VideoFrameAccess videoFrameAccess)
	{
    	super(videoFrameAccess);
  	}
  	public String getName() 
  	{
    	return "Post-Access Codec";
  	}
 
  	void accessFrame(Buffer frame) 
  	{
    	if(videoFrameAccess.trackerVideoRenderer == null)
    	{
      		return;
    	}
    	
	System.err.println("Postaccess");    

  	}
}
