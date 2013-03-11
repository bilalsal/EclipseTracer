package visdebugger.histories.model;

import org.eclipse.jdt.debug.core.IJavaDebugTarget;

import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

/**
 * 
 * @author Bilal
 * records modifcations / accesses on a variable
 */
public class NumericVariableHistory extends VariableHistory {

	private int minvalue, maxValue;
	
	private WatchPointValue minWpValue, maxWpValue;
	
	
	public NumericVariableHistory(IJavaDebugTarget target, Watchpoint watchpoint, ObjectReference object) {
		super(target, watchpoint, object);
		minvalue = Integer.MAX_VALUE;		
		maxValue = Integer.MIN_VALUE;		
	}
	
	@Override
	public WatchPointValue addValue(Value value, long timestamp,
			ThreadReference thread, boolean modification) {
		WatchPointValue wpv = super.addValue(value, timestamp, thread, modification);
		PrimitiveValue nValue = (PrimitiveValue)value;
		if (nValue.intValue() < minvalue) {
			minvalue = nValue.intValue();
			minWpValue = wpv;
		}
		if (nValue.intValue() > maxValue) {
			maxValue = nValue.intValue();
			maxWpValue = wpv;
		}
		return wpv;
	}
	
	public WatchPointValue getMinValue() {
		return minWpValue;
	}
	
	public WatchPointValue getMaxValue() {
		return maxWpValue;
	}

}
