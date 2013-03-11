package visdebugger.eclipseuiactions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaReferenceType;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIType;
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

import visdebugger.arrays.control.ArraysMainController;
import visdebugger.arrays.model.ArrayExpression;

import com.sun.jdi.ArrayType;
import com.sun.jdi.IntegerType;
import com.sun.jdi.Type;

/**
 * This class acts as an {@link IObjectActionDelegate} to handle the "Visualize" Action invoked
 * on an {@link IJavaVariable} object in the "Variables" view in Eclipse 
 * or on an {@link IWatchExpression} object in the "Expressions" view in Eclipse 
 * @author Bilal
 *
 */
@SuppressWarnings("restriction")
public class ValueVisActionDelegate implements IObjectActionDelegate {

	public ValueVisActionDelegate() {
	}

	@Override
	public void run(IAction action) {	
		// selection must has 1 and only 1 element (as defined in the extension
		StructuredSelection selection = (StructuredSelection)((PluginAction)action).getSelection();
		String itemName = null;
		IJavaValue value = null;
		try {
			if (selection.getFirstElement() instanceof IJavaVariable) {
				IJavaVariable variable = (IJavaVariable)selection.getFirstElement();
				itemName = variable.getName();
				value = (IJavaValue) variable.getValue();
			}
			else if (selection.getFirstElement() instanceof IWatchExpression) {
				IWatchExpression expression = (IWatchExpression)selection.getFirstElement();
				itemName = expression.getExpressionText();
				value = (IJavaValue) expression.getValue();
			}
			
			if (value != null) {
				if (value instanceof IJavaArray) {
					/*IJavaArray arrValue = (IJavaArray)value;
					int arrLen = arrValue.getLength();
					IJavaType compType = ((IJavaArrayType)arrValue.getJavaType()).getComponentType();
					if (compType instanceof IJavaReferenceType) {
					}
					Type javaType = ((JDIType)compType).getUnderlyingType();
					if (javaType instanceof IntegerType) {
						
					}
					IJavaValue jvalue = arrValue.getValue(arrLen);
					if (jvalue instanceof IJavaObject) {
						((JDIObjectValue)jvalue).getField("", false).getValue();
					}*/
					ArraysMainController.addExpression(new ArrayExpression(itemName, (IJavaArray)value));
				}
				
				/*Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			    MessageBox mb = new MessageBox(shell, SWT.OK);
				// TODO open the suitable visualizer view 
			    mb.setText("Visualize Value:");
			    mb.setMessage(itemName + " of type: " + value.getJavaType()
			    			+ " has value " + value);
			    mb.open();*/		 
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
