package visdebugger.histories.model;

import org.eclipse.jdt.debug.core.IJavaDebugTarget;

import com.sun.jdi.ObjectReference;

/**
 * Interface for notifying listeners of access / modification events on a watchpoint
 * @author Bilal
 *
 */
public interface IBreakpointListener {
	
	/**
	 * Notifies the listener that a hit event has occurred to the watch point
	 * @param watchpoint the watch point
	 * @param object the object on which the watch point hit event has happened 
	 */
	void notifyBreakpointHit(AbstractBreakpoint watchpoint, ObjectReference object, IJavaDebugTarget target);

	
	/**
	 * Notifies the listener that a watchpoint has been installed in a target
	 * @param target 
	 * @param watchpoint 
	 */
	void notifyBreakpointInstalled(AbstractBreakpoint watchpoint, IJavaDebugTarget target);

	void notifyInstanceAdded(AbstractBreakpoint watchPoint, ObjectReference instance,
			IJavaDebugTarget target);

	/**
	 * Notifies the listener that debug-targets (i.e. JVM) have changed (currently, only when a new JVM is launched)
	 */
	void notifyDebugTargetAdded(IJavaDebugTarget newTarget);

}
