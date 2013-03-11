package visdebugger.eclipseuiactions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.PluginAction;

@SuppressWarnings("restriction")
public class ObjectReferenceVisActionDelegate implements IObjectActionDelegate {

	public ObjectReferenceVisActionDelegate() {
	}

	@Override
	public void run(IAction action) {	
		// selection must has 1 and only 1 element (as defined in the extension)
		StructuredSelection selection = (StructuredSelection)((PluginAction)action).getSelection();
		System.out.println("selection: " + selection);
		// TODO open the suitable visualizer view 
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		
	}


	

}
