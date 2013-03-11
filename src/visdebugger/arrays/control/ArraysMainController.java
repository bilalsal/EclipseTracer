package visdebugger.arrays.control;

import java.util.ArrayList;

import org.eclipse.jdi.internal.FieldImpl;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import visdebugger.arrays.model.ArrayExpression;
import visdebugger.arrays.view.ArraysViewPart;
import visdebugger.eclipseuiactions.InspectObjectReferenceAction;
import visdebugger.histories.model.VariableHistory;
import visdebugger.histories.model.Watchpoint;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;

/**
 * The main controller of the arrays {@link ViewPart}.
 * It reacts to changes from the front-end and from the backend. 
 * @author Bilal
 *
 */
public class ArraysMainController {

	private class ModeListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {

			
			if (view.getButtonHistogram().getSelection()) {
				viewsManager.setMode(MODE_HISTOGRAM);
			}
			else if (view.getButtonSeries().getSelection()) {
				viewsManager.setMode(MODE_SERIES);
			}
			else if (view.getButtonTable().getSelection()) {
				viewsManager.setMode(MODE_TABLE);
			}
		}

	}
	
	public static final int MODE_SERIES = 0;
	public static final int MODE_HISTOGRAM = 1;
	public static final int MODE_TABLE = 2;
	

	
	/* Coloring methods constants */
	// The SWT view
	private ArraysViewPart view;
	
	private ArrayViewsManager viewsManager;
	
	SelectionListener modeListener;
	
	private ArrayList<ArrayExpression> arrExpressions;
	
	private static ArraysMainController instance; 

	/**
	 * Connsturcts the controller for the main view.
	 * @param view the main view
	 */
	private ArraysMainController(final ArraysViewPart view) {
		this.view = view;
		arrExpressions = new ArrayList<ArrayExpression>();
		viewsManager = new ArrayViewsManager(view.getViewsArea(), view.getCompViews());

		initArrayExpressionsViewer();

		view.getSliderZoom().addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int val = view.getSliderZoom().getSelection();
				float zoomfactor = val > 10 ? val - 9 : 1f / (11 - val);
				viewsManager.setZoomFactor(zoomfactor);
			}
		});
		
		
		modeListener = new ModeListener();
		view.getButtonHistogram().addSelectionListener(modeListener);
		view.getButtonSeries().addSelectionListener(modeListener);
		view.getButtonTable().addSelectionListener(modeListener);
		
		view.getInstantSearch().addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				String searchParameter = view.getInstantSearch().getText();
				viewsManager.setInstantSearch(searchParameter);
			}
	    	
	    });
		
	}
	
		
	private void initArrayExpressionsViewer() {
		
		final TreeViewer watchedVariablesViewer = view.getTreeArrayExpressions();
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		Menu menu = menuMgr.createContextMenu(watchedVariablesViewer.getControl());
		watchedVariablesViewer.getControl().setMenu(menu);
		menuMgr.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				if (watchedVariablesViewer.getSelection().isEmpty()) {
					return;
				}

				if (watchedVariablesViewer.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) watchedVariablesViewer
							.getSelection();
					Object selectedItem = selection.getFirstElement();

					if (selectedItem instanceof ObjectReference) {
						Shell shell = watchedVariablesViewer.getControl().getShell();
						manager.add(new InspectObjectReferenceAction(shell,
								getSelectedTarget(),
								(ObjectReference)selectedItem));
						manager.add(new Action("Show History") {
							@Override
							public void run() {
								openSelectedVariable();
							}
						});
					}
				}
			}
		});
		
		// sets the content provider (provides the watchpoints and instances for the selected target)
		watchedVariablesViewer.setContentProvider(new ITreeContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
			@Override
			public void dispose() {
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				return arrExpressions.toArray();
			}
			
			@Override
			public boolean hasChildren(Object element) {
				return element instanceof ArrayExpression && ((ArrayExpression)element).hasChildren();
			}
			
			@Override
			public Object[] getChildren(Object element) {
				return ((ArrayExpression)element).getChildren();
			}
		});
		// the input is the debug target (initially null)
		watchedVariablesViewer.setInput("");
		
		// the label provider (to show the tree items in a readable format)
		watchedVariablesViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Watchpoint) {
					Watchpoint watchpoint = (Watchpoint)element;
					Field field = watchpoint.getField();
					String typeName = field.declaringType().name();
					int pos = typeName.lastIndexOf(".");
					if (pos > 0) {
						typeName = typeName.substring(pos + 1);
					}
					return typeName + "." + field.name();
				}
				else if (element instanceof ObjectReference) {
					String instanceName = element.toString();
					int startPos = instanceName.lastIndexOf('=') + 1;					
					int endPos = instanceName.lastIndexOf(')');	
					if (startPos >= 0 && endPos >= startPos) {
						instanceName = "id=" + instanceName.substring(startPos, endPos);
					}
					return  instanceName;
				}
				else {
					return super.getText(element);
				}
			}
		});		
		// a listener to open the value-history view for the clicked instance
		watchedVariablesViewer.getTree().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				openSelectedVariable();
			}
		});
	}
	

	public IJavaDebugTarget getSelectedTarget() {
		return null;
	}
	
	private void openSelectedVariable() {
		ISelection sel = view.getTreeArrayExpressions().getSelection();
		if (sel != null) {
			ITreeSelection treeSel = (ITreeSelection)sel;
			if (treeSel.size() == 1) {
				Object element = treeSel.getFirstElement();
				if (element instanceof ArrayExpression) {
					showViewFor((ArrayExpression)element, null);
				}
				else if (element instanceof FieldImpl){
					ArrayExpression expr = (ArrayExpression)treeSel.getPaths()[0].getSegment(0);
					showViewFor(expr, (FieldImpl)element);
				}
			}
		}
	}
	
	// shows the value history view for the given parameters (it will be created if it does not exist yet) 
	public static void addExpression(ArrayExpression exp) {
		ArraysViewPart view = null;
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			view = (ArraysViewPart)page.showView(ArraysViewPart.ID);
		} catch (PartInitException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (instance == null) {
				instance = new ArraysMainController(view);
		}
		instance.arrExpressions.add(exp);
		view.getTreeArrayExpressions().refresh();
		instance.showViewFor(exp, null);		
	}

	// shows the value history view for the given parameters (it will be created if it does not exist yet) 
	public void showViewFor(ArrayExpression exp, FieldImpl field) {
		AbstractArrayController arrayController = exp.getController(field);
		if (arrayController == null
				|| arrayController.getView().isDisposed()) {
			arrayController = viewsManager.createArrayController(
					view.getViewsArea(), exp, field);
			viewsManager.adjustWidth();
			exp.setController(field, arrayController);
			view.getViewsArea().layout();
		}

	}
	
	
}
