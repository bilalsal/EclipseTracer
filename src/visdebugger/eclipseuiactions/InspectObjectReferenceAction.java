package visdebugger.eclipseuiactions;

import org.eclipse.debug.ui.DebugPopup;
import org.eclipse.debug.ui.InspectPopupDialog;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIValue;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;

import com.sun.jdi.ObjectReference;

@SuppressWarnings("restriction")
public class InspectObjectReferenceAction extends Action {
	
	ObjectReference instance;
	
	IJavaDebugTarget target;
	
	Shell shell;
	
	public InspectObjectReferenceAction(Shell shell, IJavaDebugTarget target, ObjectReference instance) {
		super("Inspect");
		this.instance = instance;
		this.target = target;
		this.shell = shell;
	}
	
	@Override
	public void run() {
		JavaInspectExpression expression = new JavaInspectExpression("name",
				new JDIValue((JDIDebugTarget)target, instance));	
		
        DebugPopup displayPopup = new InspectPopupDialog(shell, null, null, expression);
        displayPopup.open();

	}

}
