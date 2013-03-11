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
public class VariableHistory extends AbstractBreakpointHistory {
	
	private List<WatchPointValue> modifications;
	private List<WatchPointValue> allValues;
	private Watchpoint watchpoint;
	private AbstractHistoryController controller;
	
	public VariableHistory(IJavaDebugTarget target, Watchpoint watchpoint, ObjectReference object) {
		super(target, watchpoint, object);
		modifications = new ArrayList<WatchPointValue>();
		allValues = new ArrayList<WatchPointValue>();
		this.watchpoint = watchpoint;
	}
	
	/**
	 * 
	 * @return the history of modifications to the variable associated with this {@link VariableHistory}
	 */
	public List<WatchPointValue> getModifications() {
		return modifications;
	}
	
	/**
	 * Adds the given {@link Value} to the history
	 * @param value the {@link Value} the history's variable has taken / took 
	 * @param timestamp the timestamp at which this value was assigned / accessed 
	 * @param thread the {@link Thread} which assigned / accessed this value
	 * @param modification true if the value has been assigned by a modification access
	 */
	protected WatchPointValue addValue(Value value, long timestamp,
			ThreadReference thread, boolean modification) {
		int milliSec = getBreakpoint().convertToElapsedMilliSec(getDebugTarget(), timestamp);
		WatchPointValue wpv  = new WatchPointValue(value, milliSec, thread);
		allValues.add(wpv);
		if (modification) {
			modifications.add(wpv);
		}
		return wpv;
	}
	
	/**
	 * 
	 * @return the history of accesses to the variable associated with this {@link VariableHistory}
	 */
	public List<WatchPointValue> getAllValues() {
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
	 * Sets the {@link AbstractHistoryController} for this history
	 * @param controller the controller
	 */
	public void setController(AbstractHistoryController controller) {
		if (this != controller.getHistory()) {
			throw new RuntimeException("Value History Controller set to the wrong Value History!");
		}
		this.controller = controller;
	}

	public static AbstractBreakpointHistory createHistory(IJavaDebugTarget target,
			AbstractBreakpoint absBPoint, ObjectReference instance) {
		if (absBPoint instanceof Breakpoint) {
			return new LineBreakpointHistory(target, (Breakpoint)absBPoint, instance);
		}
		Watchpoint watchPoint = (Watchpoint)absBPoint;
		if (watchPoint.getJavaType() instanceof IntegerType) {
			return new NumericVariableHistory(target, watchPoint, instance);
		}
		else {
			return new VariableHistory(target, watchPoint, instance);
		}
	}

	@Override
	public Watchpoint getBreakpoint() {
		return watchpoint;
	}

	@Override
	public String getName() {
		String name = watchpoint.getField().name();
		if (getObject() != null) {
			String instanceName = getObject().toString();
			int startPos = instanceName.lastIndexOf('=') + 1;					
			int endPos = instanceName.lastIndexOf(')');	
			if (startPos >= 0 && endPos >= startPos) {
				name += "(for id=" + instanceName.substring(startPos, endPos) + ")";
			}
		}
		return name;
	}

}
