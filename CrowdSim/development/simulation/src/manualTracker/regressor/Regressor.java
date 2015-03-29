package manualTracker.regressor;

import java.util.ArrayList;
import manualTracker.data.RegregressionPoint;

/**
 * 
 * Regressor of point manualTracker.data. Based on history, manualTracker.regressor predicts next position where the given point is expected in given
 * time slot / frame. 
 * Regressor is deemed to facilitate as much as possible the manual manualTracker.data entry, though, it is not deemed to make accurate
 * prediction for entire tracking.
 *
 */

public interface Regressor 
{
	/**
	* 
	* @param pointHistory history of point positions (ArrayList<RegregressionPoint>)
	* @param nextTime time slot/ frame for which position is sought
	* @return estimated position
	*/
  	RegregressionPoint getNextPosition(ArrayList pointHistory, long nextTime);
}
