package visdebugger.histories;

import java.util.HashMap;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.jdi.internal.FieldImpl;
import org.eclipse.jdi.internal.LocationImpl;
import org.eclipse.jdi.internal.VirtualMachineImpl;
import org.eclipse.jdi.internal.request.AccessWatchpointRequestImpl;
import org.eclipse.jdi.internal.request.BreakpointRequestImpl;
import org.eclipse.jdi.internal.request.ModificationWatchpointRequestImpl;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jdt.internal.debug.core.IJDIEventListener;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaBreakpoint;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;

import visdebugger.histories.model.AbstractBreakpoint;
import visdebugger.histories.model.Breakpoint;
import visdebugger.histories.model.BreakpointsManager;
import visdebugger.histories.model.Watchpoint;

import com.sun.jdi.request.EventRequest;

/**
 * This listener implements the eclipse extension point
 * "org.eclipse.jdt.debug.breakpointListeners". By that it gets notified when
 * breakpoints are added, removed, installed, hit, etc..
 * 
 * @author Bilal
 * 
 */
@SuppressWarnings("restriction")
public class JavaBreakpointListener implements IJavaBreakpointListener {

	final BreakpointsManager wpMgr = BreakpointsManager.getInstance();
	private HashMap<AbstractBreakpoint, HashMap<IJavaDebugTarget, EventRequest[]>> requests;

	public JavaBreakpointListener() {
		requests = new HashMap<AbstractBreakpoint, HashMap<IJavaDebugTarget, EventRequest[]>>();
	}

	@Override
	public void addingBreakpoint(IJavaDebugTarget target,
			IJavaBreakpoint breakpoint) {
	}

	@Override
	public int installingBreakpoint(IJavaDebugTarget target,
			IJavaBreakpoint breakpoint, IJavaType type) {
		return 0;
	}

	/**
	 * {@inheritDoc} If the breakpoint is a {@link IJavaWatchpoint}, two
	 * additional {@link IJDIEventListener} listeners are added to the target to
	 * get notified when the breakpoint's field is accessed or notified.
	 */
	@Override
	public void breakpointInstalled(final IJavaDebugTarget target,
			final IJavaBreakpoint breakpoint) {

		if (!wpMgr.getDebugTargets().contains(target)) {
			wpMgr.notifyDebugTargetLaunch(target);
		}
		JDIDebugTarget jdiTarget = ((JDIDebugTarget) target);
		VirtualMachineImpl vm = (VirtualMachineImpl) jdiTarget.getVM();
		AbstractBreakpoint absBreakpoint = null;
		EventRequest[] bpRequests = null;  
		if (breakpoint instanceof IJavaWatchpoint) {
			AccessWatchpointRequestImpl evRequest = (AccessWatchpointRequestImpl) findRequest(
					breakpoint, jdiTarget.getEventRequestManager().accessWatchpointRequests());
			/*
			 * from the found event request we can infer the Field which is
			 * necessary for creating the listeners.
			 */
			FieldImpl field = (FieldImpl) evRequest.field();

			Watchpoint watchPoint = (Watchpoint)wpMgr.getBreakpoint(breakpoint);
			if (watchPoint == null) {
				watchPoint = wpMgr.createWatchpoint((IJavaWatchpoint) breakpoint, false);
			}
			if (watchPoint.getJavaType() == null) {
				watchPoint.setField(field);
			}

			ModificationWatchpointRequestImpl writeReq = new ModificationWatchpointRequestImpl(
					vm);
			writeReq.addFieldFilter(field);
			writeReq.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			writeReq.setEnabled(true);
			jdiTarget.addJDIEventListener(watchPoint, writeReq);
			AccessWatchpointRequestImpl accessReq = new AccessWatchpointRequestImpl(
					vm);
			accessReq.addFieldFilter(field);
			accessReq.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			accessReq.setEnabled(true);
			jdiTarget.addJDIEventListener(watchPoint, accessReq);
			bpRequests = new EventRequest[] { accessReq, writeReq };
			absBreakpoint = watchPoint;
		}
		else if (breakpoint instanceof ILineBreakpoint) {
			
			Breakpoint watchPoint = (Breakpoint)wpMgr.getBreakpoint(breakpoint);
			if (watchPoint == null) {
				watchPoint = wpMgr.createBreakpoint((IJavaLineBreakpoint) breakpoint, false);
			}
			
			BreakpointRequestImpl evRequest = (BreakpointRequestImpl) findRequest(
					breakpoint, jdiTarget.getEventRequestManager().breakpointRequests());
			BreakpointRequestImpl bpReq = new BreakpointRequestImpl(vm);
			bpReq.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			
			bpReq.addLocationFilter((LocationImpl)evRequest.location());
			bpReq.setEnabled(true);
			jdiTarget.addJDIEventListener(watchPoint, bpReq);
			absBreakpoint = watchPoint;
			bpRequests = new EventRequest[] { bpReq };
			
		}
		if (absBreakpoint != null) {
			if (!requests.containsKey(absBreakpoint)) {
				requests.put(absBreakpoint,
						new HashMap<IJavaDebugTarget, EventRequest[]>());
			}
			requests.get(absBreakpoint).put(target, bpRequests);
			wpMgr.installWatchPoint(absBreakpoint, target);
		}

	}

	/**
	 * Notification that the given breakpoint has been hit in the specified
	 * thread. This listener votes by
	 * <code DONT_SUSPEND> to prevent actual suspension of thread
	 * and a switch to the IDE.
	 * Instead, the values are watched by the listeners defined in 
	 * {@link IJavaBreakpointListener#breakpointInstalled(IJavaDebugTarget, IJavaBreakpoint)}.
	 * and accumulated to be presented visually.
	 */
	@Override
	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		return DONT_CARE;
	}

	/**
	 * Deinstall the watchpoint from the target
	 */
	@Override
	public void breakpointRemoved(IJavaDebugTarget target,
			IJavaBreakpoint breakpoint) {
		// if breakpoint is not instanceof Watchpoint, this will return nulll
		AbstractBreakpoint watchPoint = wpMgr.getBreakpoint(breakpoint);
		if (watchPoint != null) {
			JDIDebugTarget jdiTarget = (JDIDebugTarget) target;
			for(EventRequest request : this.requests.get(watchPoint).get(target)) {
				jdiTarget.removeJDIEventListener(watchPoint, request);
			}
		}
	}

	@Override
	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint,
			DebugException exception) {
	}

	@Override
	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint,
			Message[] errors) {
	}

	private EventRequest findRequest(IJavaBreakpoint breakpoint, List requests) {
		for (Object obj : requests) {
			EventRequest evRequest = (EventRequest) obj;
			if (breakpoint == evRequest
					.getProperty(JavaBreakpoint.JAVA_BREAKPOINT_PROPERTY)) {
				return evRequest;
			}
		}
		return null;
	}
}
