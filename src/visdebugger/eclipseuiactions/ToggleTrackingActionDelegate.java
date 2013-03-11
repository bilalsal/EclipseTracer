package visdebugger.eclipseuiactions;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.RulerBreakpointAction;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaWatchpoint;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

import visdebugger.histories.model.AbstractBreakpoint;
import visdebugger.histories.model.BreakpointsManager;
import visdebugger.histories.model.Watchpoint;

/**
 * This {@link IActionDelegate} reacts to clicking on the "Track Values" entry in the edtior's breakpoint context-menu.
 * The class switches the enablement of value-tracking for the clicked breakpoint (in case it is a {@link IJavaWatchpoint}
 * @author Bilal
 *
 */
public class ToggleTrackingActionDelegate extends AbstractRulerActionDelegate {

	static class ToggleBreakpointSilentAction extends RulerBreakpointAction implements IUpdate {
		
		private IBreakpoint fBreakpoint;
		
		private final static BreakpointsManager wpMgr = BreakpointsManager.getInstance();
		
		ToggleBreakpointSilentAction(ITextEditor editor,
				IVerticalRulerInfo rulerInfo) {
			super(editor, rulerInfo);
		}
		
		@Override
		public void run() {
			IBreakpoint breakpoint = getBreakpoint();
			if (fBreakpoint != null) {
				AbstractBreakpoint watchPoint = wpMgr.getBreakpoint(breakpoint);			
				if (watchPoint != null) {
					watchPoint.setTrackingActive(!watchPoint.isTrackingActive());
				}
				else if (fBreakpoint instanceof IJavaWatchpoint){
					wpMgr.createWatchpoint((IJavaWatchpoint)fBreakpoint, true);
				}
				else if (fBreakpoint instanceof IJavaLineBreakpoint){
					wpMgr.createBreakpoint((IJavaLineBreakpoint)fBreakpoint, true);
				}
			}
		}
		

		/* (non-Javadoc)
		 * @see org.eclipse.ui.texteditor.IUpdate#update()
		 */
		public void update() {
			fBreakpoint = getBreakpoint();
			if (fBreakpoint != null) {
				setEnabled(true);
				AbstractBreakpoint watchPoint = wpMgr.getBreakpoint(fBreakpoint);
				if (watchPoint != null && watchPoint.isTrackingActive()) {
					setText("Stop Value-Tracking");
				}
				else {
					setText("Track Values");
				}
			}
			else {
				setText("Track Values");
				setEnabled(false);
			}
		}
		
	};

	@Override
	protected IAction createAction(ITextEditor editor,
			IVerticalRulerInfo rulerInfo) {
		return new ToggleBreakpointSilentAction(editor, rulerInfo);
	}

}
