package visdebugger.histories.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.core.IJDIEventListener;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;

/**
 * This class holds information about an installed {@link IJavaWatchpoint}
 * such as the type of its variable and the {@link IJavaDebugTarget} it is installed in
 * @author Bilal
 *
 */

@SuppressWarnings("restriction")
public abstract class AbstractBreakpoint implements IJDIEventListener {
	
	private List<Long> trackingActivationChanges;
	
	/**
	 * 
	 * @param javaWatchpoint the underlying {@link IJavaWatchpoint} for this watchpoint
	 * @param target the {@link IJavaDebugTarget} this watchpoint has been installed in
	 */
	public AbstractBreakpoint(boolean trackingActive) {
		this.trackingActivationChanges = new ArrayList<Long>();
		if (trackingActive) {
			trackingActivationChanges.add(System.currentTimeMillis());
		}
	}

			@Override
			public void eventSetComplete(Event event, JDIDebugTarget target,
					boolean suspend, EventSet eventSet) {
//				((JavaWatchpoint)javaWatchpoint).eventSetComplete(event, target, suspend, eventSet);
			}		
	/**
	 * @param trackingActive
	 */
	public void setTrackingActive(boolean trackingActive) {
		if (isTrackingActive() != trackingActive) {
			trackingActivationChanges.add(System.currentTimeMillis());			
		}
	}

	/**
	 * @return true if the tracking of this watchpoint is active
	 */
	public boolean isTrackingActive() {
		return trackingActivationChanges.size() % 2 == 1;
	}
	
	/**
	 * 
	 * @return the number of tracking activation ranges
	 */
	public int getTrackingActivationRangesCount() {
		return (trackingActivationChanges.size() + 1) / 2;
	}
	
	
	/**
	 * Converts the given timestamp to the number of elapsed milliseconds since the launch
	 * of the given target
	 * @param timeStamp the timestamp
	 * @param target the {@link IJavaDebugTarget}
	 * @return the elapsed milliseconds
	 */
	public int convertToElapsedMilliSec(IJavaDebugTarget target, long timeStamp) {
		String timeStampStr = target.getLaunch().getAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP);
		return (int)(timeStamp - Long.parseLong(timeStampStr));
	}
	
	/**
	 * Gets the range of activation at the given index. During this range, the
	 * tracking was activated by the user. The range is an array of two values:
	 * [activation-start, activation-end].
	 * If index is the largest index (i.e. getTrackingActivationRangesCount() - 1),
	 * and the watchpoint is currently active, the range will be open (activation-end = -1)
	 * 
	 * @param index
	 * @return the time-range range of activation at the given index 
	 */
	public int[] getTrackingActivationRange(IJavaDebugTarget target, int index) {
		int[] result = new int[2];
		result[0] = convertToElapsedMilliSec(target, trackingActivationChanges.get(index * 2));
		int deactivationInd = index * 2 + 1;
		if (deactivationInd < trackingActivationChanges.size()) {
			result[1] = convertToElapsedMilliSec(target, trackingActivationChanges.get(deactivationInd));
		}
		else {
			result[1] = -1;
		}
		return result;
	}

}
