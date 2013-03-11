package visdebugger.histories.model;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

/**
 * This class stores information about a watchpoint value
 * @author Bilal
 *
 */
public abstract class AbstractBreakpointValue {
	
//	private Location location;
	
	private ArrayList<String> sourceNames;
	
	private ArrayList<String> methodNames;
	
	private ArrayList<Integer> lineNumbers;
	
	private int timestamp;
	
	private String threadName;
	
	/**
	 * Contructs a nwe {@link AbstractBreakpointValue}
	 * @param value the value that the watchpoint took at the corresponding timestamp
	 * @param object the instance to which the value belong
	 * @param location the method, source file and line number of the caller
	 * @param timestamp the timestamp at which the watchpoint's variable took the corresponding value
	 * @param thread the thread which set the value
	 */
	public AbstractBreakpointValue(int timestamp, ThreadReference thread) {
		try {
			@SuppressWarnings("unchecked")
			List<StackFrame> frames = thread.frames();
			int frameSize = frames.size();
			sourceNames = new ArrayList<String>(frameSize);
			methodNames = new ArrayList<String>(frameSize);
			lineNumbers = new ArrayList<Integer>(frameSize);
			for (StackFrame frame : frames) {
				Location location = frame.location();
				Method method = location.method();
				sourceNames.add(location.sourcePath());
				methodNames.add(method.name() + method.signature());
				lineNumbers.add(location.lineNumber());
			}
		} catch (Exception e) {
		}
		this.timestamp = timestamp;
		this.threadName = thread.name();
	}
	
	public int getDepth() {
		return lineNumbers.size() - 1;
	}
	
	/**
	 * The line number from which this {@link AbstractBreakpointValue} has been assigned
	 * @return The line number.
	 */
	public int getLineNumber(int stackLevel) {
		if (stackLevel >= methodNames.size())
			stackLevel = methodNames.size() - 1;
		return lineNumbers.get(stackLevel);
	}
	
	/**
	 * The name of source file from which this {@link AbstractBreakpointValue} has been assigned
	 * @return The name of the source file. Null if information is unavaialble.
	 */
	public String getSourceName(int stackLevel) {
		if (stackLevel >= methodNames.size())
			stackLevel = methodNames.size() - 1;
		return sourceNames.get(stackLevel);
	}
	
	/**
	 * The method from which this {@link AbstractBreakpointValue} has been assigned.
	 * To make it unique, the method name is appended with the signature.
	 * @return The method name.
	 */
	public String getMethodName(int stackLevel) {
		if (stackLevel >= methodNames.size())
			stackLevel = methodNames.size() - 1;
		return methodNames.get(stackLevel);
	}

	/**
	 * the thread that the set this value
	 * @return the value
	 */
	public String getThreadName() {
		return threadName;
	}
	
	/**
	 * the timestamp at which that the watchpoint took this value 
	 * (measured in millisec. since program starts) 
	 * @return the timetsamp
	 */
	public int getTimestamp() {
		return timestamp;
	}

	public String getTooltipText(int level) {
		return getMethodName(level) + " at " + getTimestamp() + "ms";
	}
	

}
