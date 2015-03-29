package manualTracker.postProcessors;

import java.io.File;
import java.io.FileWriter;
import manualTracker.data.DataContainer;
import manualTracker.data.FrameData;
import manualTracker.data.PointData;

public class InternalIO //This is all the data that is written into the XML file
{
	public static final String INTERNAL_DATA = "InternalData"; //each of this strings represent the data that is being written in to the XML file
	public static final String SOURCE_FILE = "SOURCE_FILE";
	public static final String MAX_POINT_ID = "MAX_POINT_ID";
	public static final String FRAME_H_SIZE = "FRAME_H_SIZE";
	public static final String FRAME_V_SIZE = "FRAME_V_SIZE";
	public static final String FRAME_SET = "FRAME_SET";
	public static final String FRAME = "FRAME";
	public static final String FRAME_ID = "F_ID";
	public static final String TIME = "TIME";
	public static final String POINT_SET = "POINT_SET";
	public static final String POINT = "POINT";
	public static final String POINT_X = "X";
	public static final String POINT_Y = "Y";
	public static final String POINT_ID = "P_ID";
	public static final String MARKER_ID = "M_ID";
	public static final String STATUS = "STATUS";
	public static final String OUT_OF_BOUNDARY = "OUT";
  
  	public void writeData(FileWriter fileWriter, DataContainer dataContainer ) throws Exception
  	{
	    StringBuffer buffer = new StringBuffer();
	    buffer.append(startTag(INTERNAL_DATA)+"\n");
	    buffer.append(marked(MAX_POINT_ID, dataContainer.maxPointId)+"\n");
	    buffer.append(marked(FRAME_H_SIZE, dataContainer.frameHSize)+"\n");
	    buffer.append(marked(FRAME_V_SIZE, dataContainer.frameVSize)+"\n");
	    buffer.append(marked(SOURCE_FILE, dataContainer.inputFilename)+"\n");
	    buffer.append(startTag(FRAME_SET)+"\n");
    
    	fileWriter.write(buffer.toString(), 0, buffer.length());
    
    	for(int i=0; i < dataContainer.frames.size(); i++ )
    	{
			buffer = new StringBuffer();
			FrameData frameData = (FrameData)dataContainer.frames.get(i);
			buffer.append(startTag(FRAME)+"\n");
			buffer.append(marked(FRAME_ID, frameData.frameId)+"\n");
			buffer.append(marked(TIME, frameData.time)+"\n");
      
      		for(int j=0; j < frameData.framePoints.size(); j++ )
      		{
		        PointData pointData = (PointData)frameData.framePoints.get(j);
		        buffer.append(startTag(POINT));
		        buffer.append(marked(POINT_ID, pointData.pointId));
		        buffer.append(marked(POINT_X, pointData.x));
		        buffer.append(marked(POINT_Y, pointData.y));
		        buffer.append(marked(MARKER_ID, pointData.markerId));
		        buffer.append(marked(OUT_OF_BOUNDARY, ""+pointData.outOfBoundary));
		        buffer.append(marked(STATUS, ""+pointData.status));
		        buffer.append(endTag(POINT)+"\n");
      		}
			buffer.append(endTag(FRAME)+"\n");
			fileWriter.write(buffer.toString(), 0, buffer.length());
    	}
	    buffer = new StringBuffer();
	    buffer.append(endTag(FRAME_SET)+"\n");
	    buffer.append(endTag(INTERNAL_DATA)+"\n");
	    fileWriter.write(buffer.toString(), 0, buffer.length());
	    fileWriter.flush();
	    fileWriter.close();
  	}
	String startTag(String tag)
	{
		return "<"+tag+">";
	}
	String endTag(String tag)
	{
		return "</"+tag+">";
	}
	String attributed(String tag, String value)
	{
		return " "+tag+"=\""+value+"\"";
	}
	String attributed(String tag, int value)
	{
		return " "+tag+"=\""+value+"\"";
	}
	String attributed(String tag, long value)
	{
		return " "+tag+"=\""+value+"\"";
	}
	String marked(String tag, String value)
	{
		return startTag(tag)+value+endTag(tag);
	}
	String marked(String tag, int value)
	{
		return startTag(tag)+value+endTag(tag);
	}
	String marked(String tag, long value)
	{
		return startTag(tag)+value+endTag(tag);
	}
  
  	public static void readFile(File filename, DataContainer dataContainer)
  	{
    	try
    	{
    		InternalSaxReader.readFile(filename, dataContainer);
    	}
    	catch(Exception ex)
    	{
      		ex.printStackTrace();
    	}
  	}
}
