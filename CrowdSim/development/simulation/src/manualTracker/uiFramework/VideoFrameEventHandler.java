package manualTracker.uiFramework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import manualTracker.data.DataContainer;
import manualTracker.data.FrameData;
import manualTracker.data.PointData;
import manualTracker.data.RegregressionPoint;
import manualTracker.regressor.LinearPointRegressor;
import manualTracker.regressor.Regressor;
import manualTracker.renderer.ProjectionScreen;
import manualTracker.renderer.TrackerVideoRenderer;
import manualTracker.renderer.VideoFrameAccess;
import manualTracker.regressor.LinearNPointInterpolator;

public class VideoFrameEventHandler {
  
  public static final int SELECT = -1;
  public static final int REMOVE = -2;
  public boolean ignoreEvents = false;
  
  ProjectionScreen displayComponent = null;
  VideoFrameAccess videoFrameAccess = null;
  TrackerVideoRenderer trackerVideoRenderer = null;
  Regressor regressor = new LinearPointRegressor();
  DataContainer dataContainer = null;
  UserObjects userObjects = new UserObjects();
  int curentFrameId = -1;
  int currentMarkerId = 0;
  PointIdComparator pointIdComparator = new PointIdComparator();
  int skipFramesRequested = 0;
  int skipCurentFrameId = -1;
  /**
   * 
   * @param videoFrameAccess
   */
  public void setVideoFrameAccess(VideoFrameAccess videoFrameAccess){
    this.videoFrameAccess = videoFrameAccess;
    trackerVideoRenderer = videoFrameAccess.getTrackerVideoRenderer();
    displayComponent = (ProjectionScreen)videoFrameAccess.getVisualComponent();
    userObjects.displayComponent = displayComponent;
    displayComponent.setUserObjects(userObjects); 
  }
  /**
   * 
   *
   */
  public void resetDragging(){
    userObjects.resetDragging();
  }
  /**
   * 
   * @param x
   * @param y
   */
  public void addObject(int x, int y){
    PointData pointData = new PointData();
    pointData.frameId = curentFrameId;
    pointData.pointId = dataContainer.maxPointId++;
    pointData.x = x;
    pointData.y = y;
    pointData.markerId = currentMarkerId;
    pointData.status = PointData.FIRST_ENTRY;
    userObjects.addNewObjectFromClick(x, y, pointData);
  }
  
  public void removeObject(int x, int y){
    TargetVisualObject object = userObjects.getObjectAt(x, y);
    if(object == null)
      return;
    userObjects.removeObject(object);
    for(int j=0; j < dataContainer.frames.size(); j++){
      FrameData frame = (FrameData)dataContainer.frames.get(j);
      for(int i = 0; i < frame.framePoints.size(); i++ ){
        PointData pointData = (PointData)frame.framePoints.get(i);
        if(pointData.pointId == object.tmpData.pointId)
          frame.framePoints.remove(i);
      }
    }    
  }
  public boolean isSingleData(int x, int y){
    if(curentFrameId < 0)
      return true;
    FrameData currentFrame = (FrameData)dataContainer.frames.get(curentFrameId);
    if(currentFrame == null)
      return true;
    TargetVisualObject object = userObjects.getObjectAt(x, y);
    // does not make sense, but removeObject has to check anyhow
    if(object == null)
      return true;
      
    for(int i = 0; i < currentFrame.framePoints.size(); i++ ){
      PointData pointData = (PointData)currentFrame.framePoints.get(i);
      if(pointData.pointId == object.tmpData.pointId)
        return false;
    }
      
    return false;
  }
  
  public void dragObject(int x, int y){
    userObjects.dragObject(x, y);
  }
  /**
   * 
   * @param x
   * @param y
   */
  public void endDragging(int x, int y){
    userObjects.endDragging(x,y);
  }
  /**
   * Interpolates points for skipped frames, based on manual entries
   * @param framesCount
   */
  public void skipRequested(int framesCount){
  	try{
	    System.gc();
	    if(skipFramesRequested != 0 &&
	        skipCurentFrameId != -1){
	      int framesToSkip = curentFrameId - skipCurentFrameId;
	      int start = skipCurentFrameId;
	      int end = curentFrameId;
	    //System.err.println("FRAME SKIP at "+curentFrameId);
	      if(framesToSkip < 0){
	        framesToSkip = - framesToSkip;
	        start = curentFrameId;
	        end = skipCurentFrameId;
	      }
	      FrameData frame = null;
	      if(curentFrameId != -1){
	        
	        try{
	        frame = (FrameData)dataContainer.frames.get(curentFrameId);
	        }catch(java.lang.IndexOutOfBoundsException ex){
	            //ex.printStackTrace();        
	        }
	        if(frame != null)
	        // copy changes that occured on the screen into datacontainer
	        for(int i=0; i < userObjects.targetVisualObjects.size(); i++){
	          TargetVisualObject visualObject = (TargetVisualObject)userObjects.targetVisualObjects.get(i);
	          if(visualObject.isMoved){
	            int pos = Collections.binarySearch(frame.framePoints, visualObject.tmpData, pointIdComparator);
	            if( pos >= 0){
	              PointData pointData = (PointData)frame.framePoints.get(pos);
	              pointData.x = visualObject.x;
	              pointData.y = visualObject.y;
	            }
	          }
	          //System.err.println("pass "+pointData.pointId+" to frame "+pointData.frameId+" "+pointData.status+ " x "+pointData.x+" "+pointData.y);        
	        }
	      }  
	      //
	      //  now interpolate between end points
	      //
	      FrameData startFrame = (FrameData)dataContainer.frames.get(start);
	      FrameData endFrame = (FrameData)dataContainer.frames.get(end);
	      //ArrayList<RegregressionPoint>
	      ArrayList pointHistory = new ArrayList();
	      //
	      //  loop through set of points in frame at lower time boundary
	      //
	//System.out.println("Start FRAME id "+startFrame.frameId+" End FRAME id "+endFrame.frameId);              
	      for(int i = 0; i < startFrame.framePoints.size(); i++ ){
	        pointHistory.clear();
	        PointData startPointData = (PointData)startFrame.framePoints.get(i);
	        int pos = Collections.binarySearch(endFrame.framePoints, startPointData, pointIdComparator);
	        if( pos >= 0){
	          PointData endPointData = (PointData)endFrame.framePoints.get(pos);
	//System.out.println("start id "+startPointData.pointId+" x "+startPointData.x+" y "+startPointData.y+" END x "+endPointData.x+" y "+endPointData.y);              
	          RegregressionPoint point = new RegregressionPoint();
	          point.time = startFrame.time;
	          point.x = startPointData.x;
	          point.y = startPointData.y;
	          pointHistory.add(point);
	          point = new RegregressionPoint(); 
	          point.time = endFrame.time;
	          point.x = endPointData.x;
	          point.y = endPointData.y;
	          pointHistory.add(point);
	          ArrayList pointInterpolation = 
	            LinearNPointInterpolator.getInterpolationPositions(pointHistory, framesToSkip); 
	          //
	          //  set intepolated points into frames
	          //  
	          for(int j = start+1; j < start+framesToSkip; j++ ){
	            frame = (FrameData)dataContainer.frames.get(j);
	//System.out.println("FRAME id "+frame.frameId);              
	            
	            int pos1 = Collections.binarySearch(frame.framePoints, startPointData, pointIdComparator);
	//System.out.println("START id "+startPointData.pointId+" pos in  "+j+" frame "+pos);              
	            if(pos1 >= 0){
	              PointData pointData = (PointData)frame.framePoints.get(pos1);
	              RegregressionPoint regregressionPoint = (RegregressionPoint)pointInterpolation.get(j-start-1);
	//System.out.println("id "+pointData.pointId+" status "+pointData.status+" x "+pointData.x+" y "+pointData.y);              
	              if(pointData.status.equals(PointData.REGRESSED) ||
	                 pointData.status.equals(PointData.INTERPOLATED)){
	                pointData.x = regregressionPoint.x;
	                pointData.y = regregressionPoint.y;
	                pointData.status = PointData.INTERPOLATED;
	// System.out.println("   "+pointData.pointId+" status "+pointData.status+" x "+pointData.x+" y "+pointData.y);              
	              }
	            }
	          }    
	          //System.err.println("Adding postfound "+pointData.pointId);
	        }
	      }
	    }
	    // retain current request for future reference
	    skipFramesRequested = framesCount;
	    skipCurentFrameId = curentFrameId;
  	}
  	catch( Exception e )
  	{
  		e.printStackTrace();
  		System.exit(-1);
  	}
  }
  /**
   * Handles changes that occur when frame has moved in any direction
   * @param frameId
   */
  public void newFrameEvent(int frameId){
    
    if(ignoreEvents){
      curentFrameId = frameId;
      return;
    }
    //System.err.println("NEW FRAME EVENT "+frameId);
    // save editing
    FrameData frame = null;
    if(curentFrameId != -1){
      try{
      frame = (FrameData)dataContainer.frames.get(curentFrameId);
      }catch(java.lang.IndexOutOfBoundsException ex){
          //ex.printStackTrace();        
      }
      if(frame != null)
        frame.framePoints.clear();
      else{
        frame = new FrameData();
        frame.frameId = curentFrameId;
        dataContainer.frames.add(frame);
      }
      // copy changes that occured on the screen
      for(int i=0; i < userObjects.targetVisualObjects.size(); i++){
        TargetVisualObject visualObject = (TargetVisualObject)userObjects.targetVisualObjects.get(i);
        PointData pointData = new PointData(); 
        pointData.x = visualObject.x;
        pointData.y = visualObject.y;
        pointData.markerId = visualObject.tmpData.markerId;
        pointData.pointId = visualObject.tmpData.pointId;
        pointData.frameId = curentFrameId;
        pointData.outOfBoundary = visualObject.outOfBoundary;
        pointData.status = visualObject.status;
        frame.framePoints.add(pointData);
        //System.err.println("pass "+pointData.pointId+" to frame "+pointData.frameId+" "+pointData.status+ " x "+pointData.x+" "+pointData.y);        
      }
    }
    userObjects.targetVisualObjects.clear();
    userObjects.frameHeight = trackerVideoRenderer.getInHeight();
    userObjects.frameWidth = trackerVideoRenderer.getInWidth();

    curentFrameId = frameId;
    frame = null;
    // create new set of data for the frame
    try{
    frame = (FrameData)dataContainer.frames.get(curentFrameId);
    }catch(java.lang.IndexOutOfBoundsException ex){
      //System.err.println("NOT FOUND frame "+curentFrameId);
    }
    System.err.println("HANDLING frame "+curentFrameId);
    
    if(frame == null){
      frame = new FrameData();
      //System.err.println("NEW frame "+curentFrameId);
      dataContainer.frames.add(frame);
      frame.frameId = curentFrameId;
      frame.time = videoFrameAccess.getMediaTime();
      // new frame try top make regression
      if(frameId > 0){
        // copy previous frame data
        int frameIdPrevious = frame.frameId -1;
        try{
          FrameData previousFrame = (FrameData)dataContainer.frames.get(frameIdPrevious);
          for(int i = 0; i < previousFrame.framePoints.size(); i++ ){
            PointData previousPointData = (PointData)previousFrame.framePoints.get(i);
            // skip copying out of frame data
            if(previousPointData.outOfBoundary)
              continue;
            PointData pointData = new PointData();
            copyData(pointData, previousPointData );
            pointData.frameId = frame.frameId;
            frame.framePoints.add(pointData);
            //System.err.println("reg point "+pointData.pointId+" "+pointData.x+" "+pointData.y );
          }
        }catch(Exception ex){
          
        }
      }
    }
    else{
      for(int i = 0; i < frame.framePoints.size(); i++ ){
        PointData ppointData = (PointData)frame.framePoints.get(i);
        //System.err.println("EXISTING frame "+curentFrameId+" pid "+ ppointData.frameId+" "+ppointData.status+" x "+ppointData.x+" y "+ppointData.y);
      }
      // check if some new marker has been added afterwards
      if(frameId > 0){
        int frameIdPrevious = frame.frameId -1;
        FrameData previousFrame = (FrameData)dataContainer.frames.get(frameIdPrevious);
        for(int i = 0; i < previousFrame.framePoints.size(); i++ ){
          PointData previousPointData = (PointData)previousFrame.framePoints.get(i);
          if(previousPointData.outOfBoundary)
            continue;
          if(Collections.binarySearch(frame.framePoints, previousPointData, pointIdComparator) < 0){
            PointData pointData = new PointData();
            copyData(pointData, previousPointData );
            pointData.frameId = frame.frameId;
            frame.framePoints.add(pointData);
            //System.err.println("Adding postfound "+pointData.pointId);
          }
        }
      }
    }
    getRegressionValues(frame);
    // create user objects for given frame
    for(int i=0; i < frame.framePoints.size(); i++){
        //PointData ppointData = (PointData)frame.framePoints.get(i);
        //System.err.println(" -------- pid "+ ppointData.pointId+" "+ppointData.status+" x "+ppointData.x+" y "+ppointData.y);
      userObjects.addObjectFromData((PointData)frame.framePoints.get(i)); 
    }
    resetDragging();
  }
  
  public void takeSnapshot(String path){
    displayComponent.exportImage(path);    
  }
  
  public int getCurentFrameId(){
    return curentFrameId;
  }
  
  void copyData(PointData pointData, PointData previousPointData ){
    pointData.markerId = previousPointData.markerId; 
    pointData.pointId = previousPointData.pointId;
    pointData.x = previousPointData.x;
    pointData.y = previousPointData.y;
    pointData.status = previousPointData.status;
    pointData.status = PointData.FIRST_ENTRY;
  }
  /**
   * Fills frame with regression points
   * @param frame
   */
  void getRegressionValues(FrameData frame){
    int frameIdPrevious = frame.frameId -1;
    if(frameIdPrevious < 0)
      return;
    //System.err.println("    getRegression current frame "+frame.frameId+" frameIdPrevious "+frameIdPrevious);      
    try{
      //System.err.println("current frame "+frame.frameId+" frameIdPrevious "+frameIdPrevious);      
      FrameData previousFrame = (FrameData)dataContainer.frames.get(frameIdPrevious);
      for(int i = 0; i < frame.framePoints.size(); i++ ){
        PointData currentPointData = (PointData)frame.framePoints.get(i);
        // recalculate everything as there might be manual corrections done in the previous frame
        //System.err.println("ENTRY point "+currentPointData.pointId+" "+currentPointData.x+" "+currentPointData.y+" "+currentPointData.status );
  //System.err.println("compare '"+currentPointData.status+"' - '"+PointData.INTERPOLATED+"' "+currentPointData.status.equals( PointData.INTERPOLATED));
          
        if(currentPointData.status.equals(PointData.MANUAL_CORRECTION) ||
           currentPointData.status.equals( PointData.INTERPOLATED))
          continue;
        int position = Collections.binarySearch(previousFrame.framePoints, currentPointData, pointIdComparator);
        if(position < 0)
          continue;
        PointData previousPointData = (PointData)previousFrame.framePoints.get(position);
        //System.err.println("==== previous frame "+previousPointData.frameId+" curr "+currentPointData.frameId);      
        
        RegregressionPoint point = regressor.getNextPosition(
            getRegressionArrayForPoint(previousPointData),frame.time);
        currentPointData.x = point.x;
        currentPointData.y = point.y;
        //System.err.println("reg point "+currentPointData.pointId+" "+currentPointData.x+" "+currentPointData.y+" "+currentPointData.status );
        currentPointData.status = PointData.REGRESSED;
  //System.err.println("setting "+ currentPointData.status+" -id- "+currentPointData.pointId);
      }
    }catch(Exception ex){
       // ex.printStackTrace();      
    }
    
  }
  /**
   * Creates regression for given point
   * @param point
   * @return ArrayList<RegregressionPoint>
   */
  ArrayList getRegressionArrayForPoint(PointData point){
    //System.err.println("--regression for "+point.pointId+" "+point.x+" "+point.y);
    // ArrayList<RegregressionPoint>
    ArrayList points = new ArrayList();
    // copy last three points for regression
    for(int i=point.frameId; i >= 0 && i >= point.frameId-2 ; i--){
      try{
        FrameData frame = (FrameData)dataContainer.frames.get(i);
        int position = Collections.binarySearch(frame.framePoints, point, pointIdComparator);
        if(position < 0){
          System.err.println("NOT FOUND in frame "+i);
          continue;
        }
        PointData pointData = (PointData)frame.framePoints.get(position);
        RegregressionPoint regregressionPoint = new RegregressionPoint();
        regregressionPoint.x = pointData.x; 
        regregressionPoint.y = pointData.y;
        regregressionPoint.time = frame.time;
        //System.err.println("--> "+i+" x "+pointData.x+" "+pointData.y+" "+pointData.status);
        points.add(regregressionPoint);
      }catch(java.lang.IndexOutOfBoundsException ex){
         ex.printStackTrace();
      }
    }
    return points;
  }
  
  class PointIdComparator implements Comparator{
    public int compare(Object o1, Object o2){
      PointData p1 = (PointData)o1; 
      PointData p2 = (PointData)o2; 
      if(p1.pointId == p2.pointId)
        return 0;
      return (p1.pointId > p2.pointId ? 1 : -1);
    }
  }
}
