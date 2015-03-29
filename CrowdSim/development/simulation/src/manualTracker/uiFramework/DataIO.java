package manualTracker.uiFramework;

import java.io.FileWriter;
import manualTracker.data.DataContainer;
import manualTracker.data.FrameData;
import manualTracker.data.PointData;

public class DataIO 
{
  	public void writeData(FileWriter fileWriter, DataContainer dataContainer) throws Exception
  	{
    	int actionCounter = 0;
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("<ActionDataSet xmlns=\"http://netmoc.cpe.ucf.edu/ActionDataSet.xsd\">");
    	fileWriter.write(buffer.toString(), 0, buffer.length());
    	for(int i=0; i < dataContainer.frames.size(); i++ )
    	{
      		FrameData frameData = (FrameData)dataContainer.frames.get(i);
      		for(int j=0; j < frameData.framePoints.size(); j++ )
      		{
        		PointData pointData = (PointData)frameData.framePoints.get(j);
		        buffer = new StringBuffer();
		        buffer.append("<ActionTable>");
		        buffer.append("<Time>");
		        buffer.append(frameData.time);
		        buffer.append("</Time>");
		        buffer.append("<ActionId>");
		        buffer.append(actionCounter);
		        buffer.append("</ActionId>");
		        buffer.append("<ActionTypeId/>");
		        buffer.append("<AgentId>");
		        buffer.append(pointData.pointId);
		        buffer.append("</AgentId>");
		        buffer.append("<ActionParameter>");
		        buffer.append(frameData.frameId);
		        buffer.append("</ActionParameter>");
		        buffer.append("</ActionTable>");
		        buffer.append("<LocationTable>");
		        buffer.append("<ActionId>");
		        buffer.append(actionCounter);
		        buffer.append("</ActionId>");
		        buffer.append("<X>");
		        buffer.append(pointData.x);
		        buffer.append("</X>");
		        buffer.append("<Y>");
		        buffer.append(pointData.y);
		        buffer.append("</Y>");
		        buffer.append("<Z>");
		        buffer.append(0);
		        buffer.append("</Z>");
		        buffer.append("</LocationTable>");
		        fileWriter.write(buffer.toString(), 0, buffer.length());
		        actionCounter++;
      		}
    	}
	    buffer = new StringBuffer();
	    buffer.append("</ActionDataSet>");
	    fileWriter.write(buffer.toString(), 0, buffer.length());
	    fileWriter.flush();
	    fileWriter.close();
  	}
}
