package visdebugger.histories.model;

import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.Type;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.WatchpointEvent;

/**
 * This class holds information about an installed {@link IJavaWatchpoint}
 * such as the type of its variable and the {@link IJavaDebugTarget} it is installed in
 * @author Bilal
 *
 */

@SuppressWarnings("restriction")
public class Breakpoint extends AbstractBreakpoint {
	
	final BreakpointsManager wpMgr = BreakpointsManager.getInstance();

	private final IJavaLineBreakpoint javaBreakpoint;
	/**
	 * 
	 * @param javaBreakpoint the underlying {@link IJavaWatchpoint} for this watchpoint
	 * @param target the {@link IJavaDebugTarget} this watchpoint has been installed in
	 */
	public Breakpoint(final IJavaLineBreakpoint javaBreakpoint, boolean trackingActive) {
		super(trackingActive);
		this.javaBreakpoint = javaBreakpoint;
		
	}
	

	@Override
	public boolean handleEvent(Event event, JDIDebugTarget target,
			boolean suspendVote, EventSet eventSet) {
		if (event instanceof BreakpointEvent) {
			try {
				ObjectReference instance = ((BreakpointEvent) event).thread().frame(0).thisObject();
				wpMgr.processBreakpointEvent(instance, (BreakpointEvent)event, this, target);
			} catch (IncompatibleThreadStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}
		
	public IJavaLineBreakpoint getBreakpoint() {
		return javaBreakpoint;
	}
}
