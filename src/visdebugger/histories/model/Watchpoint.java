package visdebugger.histories.model;

import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.Type;
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
public class Watchpoint extends AbstractBreakpoint {
	
	private Field field;
	
	private Type type;
	
	final BreakpointsManager wpMgr = BreakpointsManager.getInstance();

	/**
	 * 
	 * @param javaWatchpoint the underlying {@link IJavaWatchpoint} for this watchpoint
	 * @param target the {@link IJavaDebugTarget} this watchpoint has been installed in
	 */
	public Watchpoint(final IJavaWatchpoint javaWatchpoint, boolean trackingActive) {
		super(trackingActive);
		
	}
	
	/**
	 * 
	 * @param type the type of the field on which this watchpoint is registered 
	 */
	public void setField(Field field) {
		try {
			this.field = field;
			this.type = field.type();
		} catch (ClassNotLoadedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return the field
	 */
	public Field getField() {
		return field;
	}

	/**
	 * 
	 * @return the {@link Type} of the field on which this watchpoint is registered
	 */
	public Type getJavaType() {
		return type;
	}
	
	/***
	 * 
	 * @return true if the {@link Type} returned by {@link #getJavaType()} is a {@link PrimitiveType}
	 */
	public boolean isPrimitiveType() {
		return getJavaType() instanceof PrimitiveType;
	}
	

	@Override
	public boolean handleEvent(Event event, JDIDebugTarget target,
			boolean suspendVote, EventSet eventSet) {
		if (event instanceof WatchpointEvent) {
			wpMgr.processWatchEvent((WatchpointEvent)event, this, target);
		}
		return true;
	}
	
}
