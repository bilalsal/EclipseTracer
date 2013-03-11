package visdebugger.histories.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;

import visdebugger.histories.control.AbstractHistoryController;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerType;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

/**
 * 
 * @author Bilal
 * records modifcations / accesses on a variable
 */
public abstract class AbstractBreakpointHistory {

	private ObjectReference object;
	
	private IJavaDebugTarget target;
	
	/**
	 * The {@link ObjectReference} (instance) to whose field the {@link Value} of this {@link WatchPointValue} was assigned.
	 * @return the instance
	 */
	public ObjectReference getObject() {
		return object;
	}
	
	private AbstractHistoryController controller;
	
	public AbstractBreakpointHistory(IJavaDebugTarget target, AbstractBreakpoint watchpoint, ObjectReference object) {
		this.object = object;	
		this.target = target;	
	}
	
	/**
	 * 
	 * @return the history of accesses to the variable associated with this {@link AbstractBreakpointHistory}
	 */
	public abstract List<? extends AbstractBreakpointValue> getAllValues();
	

	public abstract List<? extends AbstractBreakpointValue> getModifications();

	
	/**
	 * 
	 * @return the {@link AbstractHistoryController} assigned for this history
	 */
	public AbstractHistoryController getController() {
		return controller;
	}
	
	/**
	 * 
	 * @return the {@link Watchpoint} this history is associated with
	 */
	public abstract AbstractBreakpoint getBreakpoint();
	
	/**
	 * 
	 * @return the debug target
	 */
	public IJavaDebugTarget getDebugTarget() {
		return target;
	}
	
	/**
	 * Sets the {@link AbstractHistoryController} for this history
	 * @param controller the controller
	 */
	public void setController(AbstractHistoryController controller) {
		if (this != controller.getHistory()) {
			throw new RuntimeException("Value History Controller set to the wrong Value History!");
		}
		this.controller = controller;
	}

	abstract public String getName() ;
}
