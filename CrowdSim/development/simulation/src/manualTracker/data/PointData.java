package manualTracker.data;

public class PointData // Container of point manualTracker.data that describe marked point within the given frame
{
	public static final String FIRST_ENTRY = "FIRST_ENTRY";
	public static final String REGRESSED = "REGRESSED"; 
	public static final String MANUAL_CORRECTION = "MANUAL_CORRECTION"; 
	public static final String INTERPOLATED = "INTERPOLATED"; 

  	public int x; // The X coordinate

  	public int y; // The Y coordinate

  	public int markerId = -1; // Marker associated with point

  	public int pointId = -1; // point identifier, uniquely determines point in entire tracking manualTracker.data

  	public int frameId = -1; // The frame where current point manualTracker.data are recorded

  	public boolean outOfBoundary = false; // Flag set when point runs out of frame boundaries

  	public String status = FIRST_ENTRY; // Status of this entry
}
