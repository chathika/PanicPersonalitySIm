package manualTracker.renderer;

import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.VideoFormat;

public class PreAccessCodec implements Codec 
{
  /**
   * supported input formats
   */
	protected Format supportedIns[] = new Format []
	{
    	new VideoFormat(VideoFormat.CINEPAK),
      	new VideoFormat(VideoFormat.MPEG),
      	new VideoFormat(VideoFormat.RGB),
      	new VideoFormat(VideoFormat.SMC),
      	new VideoFormat(VideoFormat.YUV),
      	new VideoFormat(VideoFormat.INDEO32)
  	};

	// We'll advertize as supporting all video formats.

  	protected Format supportedOuts[] = new Format [] 
  	{
        new VideoFormat(VideoFormat.CINEPAK),
        new VideoFormat(VideoFormat.MPEG),
        new VideoFormat(VideoFormat.RGB),
        new VideoFormat(VideoFormat.SMC),
        new VideoFormat(VideoFormat.YUV),
        new VideoFormat(VideoFormat.INDEO32)
  	};

  	Format input = null, output = null;

  	VideoFrameAccess videoFrameAccess = null; 
  	ProjectionScreen projectionScreen = null;
  
  	public PreAccessCodec(VideoFrameAccess videoFrameAccess)
  	{
    	this.videoFrameAccess = videoFrameAccess;
 	}

  	public Format[] getSupportedInputFormats() 
  	{
    	return supportedIns;
  	}

	public Format[] getSupportedOutputFormats(Format inFormat)
	{
    	if (inFormat == null)
    	{
      		return supportedOuts;
    	}
    	else 
    	{
			// If an input format is given, we use that input format as the 
			//output since we are not modifying the bit stream at all.
			Format outs[] = new Format[1];
			outs[0] = inFormat;
			return outs;
    	}
  	}

  	public int process(Buffer inputFrame, Buffer outFrame)
  	{
    	
    	accessFrame(inputFrame); // This is the "Callback" to access individual frames.

    	
    	Object data = inputFrame.getData(); // Swap the manualTracker.data between the input & output.
    	inputFrame.setData(outFrame.getData());
    	outFrame.setData(data);

    	outFrame.setFormat(inputFrame.getFormat()); // Copy the input attributes to the output
    	outFrame.setLength(inputFrame.getLength());
    	outFrame.setOffset(inputFrame.getOffset());

    	return BUFFER_PROCESSED_OK;
  	}

  	public Format setInputFormat(Format inFormat)
  	{
    	input = inFormat;
    	return input;
  	}

  	public Format setOutputFormat(Format outFormat)
  	{
    	output = outFormat;
    	return output;
  	}

  	public void close() 
  	{
  	}

  	public String getName() 
  	{
    	return "Pre-Access Codec";
  	}

  	public void open() throws ResourceUnavailableException 
  	{
  	}

  	public void reset() 
  	{
  	}

  	public Object getControl(String arg0) 
  	{
    	return null;
  	}

  	public Object[] getControls()
  	{
    	return new Object[0];
  	}
	
	/**
	* Callback to access individual video frames.
	* 
	* @param frame
	*/
	   
  	void accessFrame(Buffer frame)
  	{
  	}

}
