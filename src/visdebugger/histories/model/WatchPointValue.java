package visdebugger.histories.model;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

/**
 * This class stores information about a watchpoint value
 * @author Bilal
 *
 */
public class WatchPointValue extends LineBreakpointValue {
	
	private Value value;
		
	/**
	 * Contructs a nwe {@link WatchPointValue}
	 * @param value the value that the watchpoint took at the corresponding timestamp
	 * @param object the instance to which the value belong
	 * @param location the method, source file and line number of the caller
	 * @param timestamp the timestamp at which the watchpoint's variable took the corresponding value
	 * @param thread the thread which set the value
	 */
	public WatchPointValue(Value value,  int timestamp, ThreadReference thread) {
		super(timestamp, thread);
		this.value = value;
	}
	
	/**
	/**
	 * the value that the watchpoint took at the corresponding timestamp
	 * @return the value
	 */
	public Value getValue() {
		return value;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return value + "";
	}
	
	@Override
	public String getTooltipText(int level) {
			return value + " at " + getTimestamp() + "ms";
	}

}
