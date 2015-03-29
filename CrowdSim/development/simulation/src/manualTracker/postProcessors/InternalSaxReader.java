package manualTracker.postProcessors;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import manualTracker.data.DataContainer;
import manualTracker.data.FrameData;
import manualTracker.data.PointData;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

public class InternalSaxReader extends DefaultHandler
{
	String currentValue;
	DataContainer dataContainer = null;
	FrameData frame = null;
	PointData point = null;
  
  	public static void readFile(File filename, DataContainer dataContainer) throws Exception
  	{
	    XMLReader xr = XMLReaderFactory.createXMLReader();
	    InternalSaxReader handler = new InternalSaxReader();
	    xr.setContentHandler(handler);
	    xr.setErrorHandler(handler);
	    handler.dataContainer = dataContainer;
  
      	// Parse each file provided on the command line.
      	FileReader r = new FileReader(filename);
      	xr.parse(new InputSource(r));
  	}
  
  	public void startDocument ()
  	{
    	dataContainer.frames = new ArrayList();
  	}
  
  	public void endDocument ()
  	{
    	System.out.println("End document frames = "+dataContainer.frames.size());
  	}
  
  	public void startElement (String uri, String name, String qName, Attributes atts)
  	{
    	if(name.equals(InternalIO.FRAME))
    	{
      		frame = new FrameData();
    	} 
    	else if(name.equals(InternalIO.POINT))
    	{
      		point = new PointData();
      		point.frameId = frame.frameId;
    	}
  	}
	
	public void endElement (String uri, String name, String qName) 
  	{
    	if(name.equals(InternalIO.MAX_POINT_ID))
    	{
      		dataContainer.maxPointId = Integer.parseInt(currentValue);
    	}
    	if(name.equals(InternalIO.FRAME_H_SIZE))
    	{
      		dataContainer.frameHSize = Integer.parseInt(currentValue);
    	}
    	else if(name.equals(InternalIO.FRAME_V_SIZE))
    	{
      		dataContainer.frameVSize = Integer.parseInt(currentValue);
    	}
    	else if(name.equals(InternalIO.SOURCE_FILE))
    	{
      		dataContainer.inputFilename = currentValue;
    	}
    	if(name.equals(InternalIO.FRAME))
    	{
      		dataContainer.frames.add(frame);
      		frame = null;
    	}
    	else if(name.equals(InternalIO.FRAME_ID))
    	{
      		frame.frameId = Integer.parseInt(currentValue);
    	}
    	else if(name.equals(InternalIO.TIME))
    	{
      		frame.time = Long.parseLong(currentValue);
    	}
    	else if(name.equals(InternalIO.POINT))
    	{
      		frame.framePoints.add(point);
		    point = null;
    	}
    	else if(name.equals(InternalIO.POINT_ID))
    	{
      		point.pointId = Integer.parseInt(currentValue);
    	}
    	else if(name.equals(InternalIO.POINT_X))
    	{
      		point.x = Integer.parseInt(currentValue);
    	}
    	else if(name.equals(InternalIO.POINT_Y))
    	{
      		point.y = Integer.parseInt(currentValue);
    	}
    	else if(name.equals(InternalIO.MARKER_ID))
    	{
      		point.markerId = Integer.parseInt(currentValue);
    	}
    	else if(name.equals(InternalIO.OUT_OF_BOUNDARY))
    	{
      		point.outOfBoundary = Boolean.valueOf(currentValue).booleanValue();
    	}
    	else if(name.equals(InternalIO.STATUS))
    	{
      		point.status = currentValue;
    	}
  	}
  
  	public void characters (char ch[], int start, int length)
  	{
    	currentValue = new String(ch, start, length);
  	}
}
