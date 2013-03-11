package visdebugger.histories.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;

import visdebugger.histories.control.AbstractHistoryController;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

/**
 * 
 * @author Bilal
 * records modifcations / accesses on a variable
 */
public class LineBreakpointHistory extends AbstractBreakpointHistory {

	private AbstractHistoryController controller;
	
	private Breakpoint watchpoint;
	
	private List<LineBreakpointValue> allValues;
	
	String name;
	
	public LineBreakpointHistory(IJavaDebugTarget target, Breakpoint breakpoint, ObjectReference object) {
		super(target, breakpoint, object);
		allValues = new ArrayList<LineBreakpointValue>();
		this.watchpoint = breakpoint;
		try {
			int lineNum = breakpoint.getBreakpoint().getLineNumber();
			name = "[line - " + lineNum  + "]";
			if (object != null) {
				String instanceName = object.toString();
				int startPos = instanceName.lastIndexOf('=') + 1;
				int endPos = instanceName.lastIndexOf(')');
				if (startPos >= 0 && endPos >= startPos) {
					name += " id="
							+ instanceName.substring(startPos, endPos);
				}
				
			}
		} catch (CoreException e) {
			name = "<error>";
		}
	}
	
	/**
	 * 
	 * @return the history of accesses to the variable associated with this {@link LineBreakpointHistory}
	 */
	public List<LineBreakpointValue> getAllValues() {
		return allValues;
	}
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
	public Breakpoint getBreakpoint() {
		return watchpoint;
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

	@Override
	public List<? extends AbstractBreakpointValue> getModifications() {
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * Adds the given {@link Value} to the history
	 * @param value the {@link Value} the history's variable has taken / took 
	 * @param timestamp the timestamp at which this value was assigned / accessed 
	 * @param thread the {@link Thread} which assigned / accessed this value
	 * @param modification true if the value has been assigned by a modification access
	 */
	protected LineBreakpointValue addValue(long timestamp,
			ThreadReference thread) {
		int milliSec = getBreakpoint().convertToElapsedMilliSec(getDebugTarget(), timestamp);
		LineBreakpointValue wpv  = new LineBreakpointValue(milliSec, thread);
		allValues.add(wpv);
		return wpv;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
}
