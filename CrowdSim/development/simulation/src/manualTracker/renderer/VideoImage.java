package manualTracker.renderer;

import java.awt.Image;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import javax.media.Buffer;
import javax.media.format.RGBFormat;
import javax.media.util.BufferToImage;

public class VideoImage 
{

  	BufferToImage bufferToImage = null;

	/**
	* Uses DirectColorModel to create image
	* @param buffer
	* @param inWidth
	* @param inHeight
	* @return
	*/
	
  	MemoryImageSource getVideoImageSource(Buffer buffer, int inWidth, int inHeight)
  	{
    	MemoryImageSource sourceImage;
    	Object data = buffer.getData();
      
    	RGBFormat format = (RGBFormat) buffer.getFormat();
    
    	DirectColorModel dcm = new DirectColorModel(format.getBitsPerPixel(), format.getRedMask(), format.getGreenMask(), format.getBlueMask());
  
    	sourceImage = new MemoryImageSource(format.getLineStride(), format.getSize().height, dcm, (int[])data, 0, format.getLineStride());
    	sourceImage.setAnimated(true);
    	sourceImage.setFullBufferUpdates(true);
    	sourceImage.newPixels(0, 0, inWidth, inHeight);
    	return sourceImage; 
  	}    

	/**
	* Uses javax.media.util.BufferToImage to convert buffer content to image
	* @param buffer
	* @param inWidth
	* @param inHeight
	* @return
	*/
	
  	Image getVideoImage(Buffer buffer, int inWidth, int inHeight)
  	{
    	RGBFormat format = (RGBFormat) buffer.getFormat();
    	if(bufferToImage == null)
    	{
      		bufferToImage = new BufferToImage(format);
    	}
    	return bufferToImage.createImage(buffer);
  	}
}
