package manualTracker.data;

import java.util.ArrayList;

public class FrameData // Container of manualTracker.data that describe single frame from tracking point of the view
{
	public ArrayList framePoints = new ArrayList(); // set of all points present in current frame

  	public int frameId; // frame identifier, uniquely identifies frame withing tracker

  	public long time; // time since the beginning of the tracking

  	public double realTime; // time since the beginning of the tracking in double format
}
