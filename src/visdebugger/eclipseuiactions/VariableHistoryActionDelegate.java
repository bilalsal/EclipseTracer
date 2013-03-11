package visdebugger.eclipseuiactions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIFieldVariable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.PluginAction;

import visdebugger.histories.model.AbstractBreakpointHistory;
import visdebugger.histories.model.BreakpointsManager;
import visdebugger.histories.model.VariableHistory;
import visdebugger.histories.model.Watchpoint;

import com.sun.jdi.ObjectReference;

/**
 * This class acts as an {@link IObjectActionDelegate} to handle the "Show History" Action invoked
 * on an {@link IJavaVariable} object in the "Variables" view in Eclipse 
 * @author Bilal
 *
 */
@SuppressWarnings("restriction")
public class VariableHistoryActionDelegate implements IObjectActionDelegate {

	@Override
	public void run(IAction action) {	
		// selection must has 1 and only 1 element (as defined in the extension
		StructuredSelection selection = (StructuredSelection)((PluginAction)action).getSelection();
		try {
			if (selection.getFirstElement() instanceof JDIFieldVariable) {
				JDIFieldVariable variable = (JDIFieldVariable)selection.getFirstElement();
				ObjectReference instance = variable.getObjectReference();
				IJavaDebugTarget target = (IJavaDebugTarget)variable.getDebugTarget();
				BreakpointsManager wpMgr = BreakpointsManager.getInstance();
				Watchpoint watchpoint = wpMgr.getWatchpointByField(variable.getField());
				AbstractBreakpointHistory history;
				if (watchpoint != null && (history = wpMgr.getValuesHistory(watchpoint, instance, target)) != null) {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					// TODO open the suitable visualizer view 
				    MessageBox mb = new MessageBox(shell, SWT.OK);
				    mb.setText("Visualize Variable History:");
				    mb.setMessage(variable.getName() + ": " + history);
				    mb.open();	
				}
			}
		} catch (DebugException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		
	}

}
