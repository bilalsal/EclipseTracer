package visdebugger.histories.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.swt.graphics.RGB;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.WatchpointEvent;

/**
 * This class manages all watch points for which visual debugging has been
 * activated. It stores for each watch point the value history and the caller
 * information. This class is a Singleton.
 * 
 * @author Bilal
 * 
 */
public class BreakpointsManager {

	private static BreakpointsManager instance = new BreakpointsManager();

	private List<IBreakpointListener> listeners;

	private HashMap<IJavaDebugTarget, HashMap<AbstractBreakpoint, HashMap<ObjectReference, AbstractBreakpointHistory>>> history;

	private HashMap<IJavaBreakpoint, AbstractBreakpoint> breakpoints;

	private HashMap<String, RGB> threadToColor;

	private HashMap<String, RGB> methodToColor;

	/**
	 * Singleton constructor
	 */
	private BreakpointsManager() {
		listeners = new ArrayList<IBreakpointListener>();
		history = new HashMap<IJavaDebugTarget, HashMap<AbstractBreakpoint, HashMap<ObjectReference, AbstractBreakpointHistory>>>();
		breakpoints = new HashMap<IJavaBreakpoint, AbstractBreakpoint>();
		threadToColor = new HashMap<String, RGB>();
		methodToColor = new HashMap<String, RGB>();

	}

	/**
	 * Gets the singleton instance of WatchPointsManager.
	 * 
	 * @return
	 */
	public static BreakpointsManager getInstance() {
		return instance;
	}

	/**
	 * Add a WatchPoint listener. This listener will be notified when watch
	 * point access / modification events occur.
	 * 
	 * @param listener
	 */
	public void addWatchPointListener(IBreakpointListener listener) {
		listeners.add(listener);
	}

	/**
	 * Installs the given watchpoint in the given target and notifies the
	 * registered {@link IBreakpointListener}s that a new watchpoint has been
	 * installed
	 * 
	 * @param watchpoint
	 *            the watchpoint
	 * @param target
	 *            the target
	 */
	public void installWatchPoint(AbstractBreakpoint watchpoint, IJavaDebugTarget target) {
		history.get(target).put(watchpoint,
				new HashMap<ObjectReference, AbstractBreakpointHistory>());
		for (IBreakpointListener listener : listeners) {
			listener.notifyBreakpointInstalled(watchpoint, target);
		}

	}

	/**
	 * Adda the given {@link IJavaDebugTarget} to the manager, and notifies the
	 * registered {@link IBreakpointListener} of this change.
	 * 
	 * @param debugTarget
	 */
	public void notifyDebugTargetLaunch(IJavaDebugTarget debugTarget) {
		history.put(
				debugTarget,
				new HashMap<AbstractBreakpoint, HashMap<ObjectReference, AbstractBreakpointHistory>>());
		debugTarget.getLaunch().getAttribute(DebugPlugin.ATTR_LAUNCH_TIMESTAMP);
		for (IBreakpointListener listener : listeners) {
			listener.notifyDebugTargetAdded(debugTarget);
		}
	}

	/**
	 * Processes modification event for the given watch point by storing the new
	 * values and caller information, and then by registered listeners
	 * 
	 * @param event
	 *            the modification event. It provides information about the
	 *            current / new values and the affected instance, as well as the
	 *            caller (thread, method, etc.).
	 * @param watchPoint
	 *            the watch point to which the event belongs.
	 * @param target
	 *            the debug target in which the event occurred
	 */
	public void processWatchEvent(WatchpointEvent event, AbstractBreakpoint watchPoint,
			IJavaDebugTarget target) {
		if (!watchPoint.isTrackingActive()) {
			return;
		}
		long currTime = System.currentTimeMillis();
		ObjectReference instance = event.object();
		VariableHistory history = (VariableHistory)getHistory(instance, watchPoint, target);		
		boolean notifyInstanceAdded = history.getAllValues().size() == 0;
		boolean eventModificationEvent = event instanceof ModificationWatchpointEvent;
		Value value = eventModificationEvent ? ((ModificationWatchpointEvent) event)
				.valueToBe() : ((WatchpointEvent)event).valueCurrent();
		history.addValue(value, currTime, event.thread(),
				eventModificationEvent);
		for (IBreakpointListener listener : listeners) {
			if (notifyInstanceAdded) {
				listener.notifyInstanceAdded(watchPoint, instance, target);
			}
			listener.notifyBreakpointHit(watchPoint, instance, target);
		}
		
	}
	
	
	private AbstractBreakpointHistory getHistory(ObjectReference instance, AbstractBreakpoint watchPoint,
			IJavaDebugTarget target) {
		HashMap<ObjectReference, AbstractBreakpointHistory> objectMap = history.get(
				target).get(watchPoint);
		if (objectMap == null) {
			history.get(target)
					.put(watchPoint,
							objectMap = new HashMap<ObjectReference, AbstractBreakpointHistory>());
		}
		AbstractBreakpointHistory history = objectMap.get(instance);
		if (history == null) {
			objectMap.put(
					instance,
					history = VariableHistory.createHistory(target, watchPoint,
							instance));
		}
		return history;
	}

	/**
	 * 
	 * @param threadName
	 *            the thread name
	 * @return the color
	 */
	public RGB getThreadColor(String threadName) {
		if (!threadToColor.containsKey(threadName)) {
			// return a unique color. Use threadToColor.keySet() to access
			// previous colors
			RGB rgb = getUniqueColor(threadToColor);
			threadToColor.put(threadName, rgb);
			return rgb;
		} else {
			return threadToColor.get(threadName);
		}
	}

	/**
	 * Returns a unique color for the specified color map
	 * 
	 * @param colorMap
	 *            the colorMap
	 * @return the unique color (RGB)
	 */
	private RGB getUniqueColor(HashMap<String, RGB> colorMap) {
		Set<String> prevColor = colorMap.keySet();
		Random random = new Random(System.currentTimeMillis());
		int r = 0, g = 0, b = 0;
		for (int i = 0; i < 1000; i++) {
			r = random.nextInt(256);
			g = random.nextInt(256);
			b = random.nextInt(256);
			Iterator<String> itr = prevColor.iterator();
			boolean hasSameColor = false;
			while (itr.hasNext()) {
				RGB rgb = colorMap.get(itr.next());
				if (rgb.red == r && rgb.green == g && rgb.blue == b) {
					hasSameColor = true;
					break;
				}
			}
			if (hasSameColor == false) {
				break;
			}
		}
		return new RGB(r, g, b);

	}

	/**
	 * 
	 * @param methodName
	 *            the method name
	 * @return the color
	 */
	public RGB getMethodColor(String methodName) {
		if (!methodToColor.containsKey(methodName)) {
			// return a unique color. Use methodToColor.keySet() to access
			// previous colors
			RGB rgb = getUniqueColor(methodToColor);
			methodToColor.put(methodName, rgb);
			return rgb;
		} else {
			return methodToColor.get(methodName);
		}

	}

	/**
	 * return for the field associated with the given watchPoint the values
	 * history in the given instance (object) in the given virtual machine
	 * 
	 * @param watchPoint
	 *            the watchPoint
	 * @param object
	 *            the object
	 * @return
	 */
	public AbstractBreakpointHistory getValuesHistory(AbstractBreakpoint watchPoint,
			ObjectReference object, IJavaDebugTarget target) {
		return history.get(target).get(watchPoint).get(object);
	}

	/**
	 * Gets all instances of the class that contains the watchpoint that are
	 * being tracked in the given target for changes in the watchpoint's field.
	 * 
	 * @param watchPoint
	 * @param target
	 *            the debug target
	 * @return
	 */
	public Set<ObjectReference> getInstances(AbstractBreakpoint watchPoint,
			IJavaDebugTarget target) {
		return history.get(target).get(watchPoint).keySet();
	}

	/**
	 * Returns all watchpoints installed in the given debug target.
	 * 
	 * @param target
	 *            the debug target
	 * @return the watchpoints
	 */
	public Set<AbstractBreakpoint> getWatchpoints(IJavaDebugTarget target) {
		return history.get(target).keySet();
	}

	/**
	 * Returns all installed debug targets
	 * 
	 * @return all installed debug targets
	 */
	public Set<IJavaDebugTarget> getDebugTargets() {
		return history.keySet();
	}

	/**
	 * Returns the {@link Watchpoint} instance that decorates the given
	 * breakpoint. If the given breakpoint is not a {@link IJavaWatchpoint},
	 * null will be returned.
	 * 
	 * @param breakpoint
	 * @return
	 */
	public AbstractBreakpoint getBreakpoint(IBreakpoint breakpoint) {
		return breakpoints.get(breakpoint);
	}

	/**
	 * Creates a new {@link Watchpoint} that decorates the given
	 * {@link IJavaWatchpoint}, and stores it in the {@link BreakpointsManager}
	 * 
	 * @param point
	 *            the
	 * @param trackingActive
	 *            set true if the created watchpoint should be tracked for value
	 *            changes of its field (in all instances in all targets)
	 * @return the created {@link Watchpoint}
	 */
	public Watchpoint createWatchpoint(IJavaWatchpoint watchpoint,
			boolean trackingActive) {
		Watchpoint watchpnt = new Watchpoint(watchpoint, trackingActive);
		breakpoints.put(watchpoint, watchpnt);
		return watchpnt;
	}

	/**
	 * Creates a new {@link Watchpoint} that decorates the given
	 * {@link IJavaWatchpoint}, and stores it in the {@link BreakpointsManager}
	 * 
	 * @param point
	 *            the
	 * @param trackingActive
	 *            set true if the created watchpoint should be tracked for value
	 *            changes of its field (in all instances in all targets)
	 * @return the created {@link Watchpoint}
	 */
	public Breakpoint createBreakpoint(IJavaLineBreakpoint watchpoint,
			boolean trackingActive) {
		Breakpoint watchpnt = new Breakpoint(watchpoint, trackingActive);
		breakpoints.put(watchpoint, watchpnt);
		return watchpnt;
	}

	/**
	 * Gets a registered {@link Watchpoint} for the given {@link Field}. If no
	 * watchpoint is registered, null will be returned.
	 * 
	 * @param field
	 *            the {@link Field}
	 * @return the {@link Watchpoint} installed on field
	 */
	public Watchpoint getWatchpointByField(Field field) {
		for (AbstractBreakpoint breakpoint : breakpoints.values()) {
			if (breakpoint instanceof Watchpoint) {
				Watchpoint watchpoint = (Watchpoint)breakpoint;
				Field field2 = watchpoint.getField();
				if (field.name().equals(field2.name())
						&& field.declaringType().name()
								.equals(field2.declaringType().name())) {
					return watchpoint;
				}
			}
		}
		return null;
	}

	public void processBreakpointEvent(ObjectReference instance, BreakpointEvent event,
			Breakpoint breakpoint, JDIDebugTarget target) {
		if (!breakpoint.isTrackingActive()) {
			return;
		}
		long currTime = System.currentTimeMillis();
		LineBreakpointHistory history = (LineBreakpointHistory)getHistory(instance, breakpoint, target);		
		boolean notifyInstanceAdded = history.getAllValues().size() == 0;
		boolean eventModificationEvent = event instanceof ModificationWatchpointEvent;
		history.addValue(currTime, event.thread());
		for (IBreakpointListener listener : listeners) {
			if (notifyInstanceAdded) {
				listener.notifyInstanceAdded(breakpoint, instance, target);
			}
			listener.notifyBreakpointHit(breakpoint, instance, target);
		}
	}

}
