package visdebugger.histories.model;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

/**
 * This class stores information about a watchpoint value
 * @author Bilal
 *
 */
public class LineBreakpointValue extends AbstractBreakpointValue {
	
	/**
	 * Contructs a nwe {@link LineBreakpointValue}
	 * @param value the value that the watchpoint took at the corresponding timestamp
	 * @param object the instance to which the value belong
	 * @param location the method, source file and line number of the caller
	 * @param timestamp the timestamp at which the watchpoint's variable took the corresponding value
	 * @param thread the thread which set the value
	 */
	public LineBreakpointValue( int timestamp, ThreadReference thread) {
		super(timestamp, thread);
	}
	
	

}
