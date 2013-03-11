package visdebugger.arrays.control;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdi.internal.FieldImpl;
import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

import visdebugger.arrays.control.AbstractArrayController.ArrayViewProperties;
import visdebugger.arrays.model.ArrayExpression;
import visdebugger.histories.control.AbstractHistoryController;
import visdebugger.histories.control.AbstractHistoryController.HistoryViewProperties;
import visdebugger.histories.model.VariableHistory;
import visdebugger.histories.model.WatchPointValue;
import visdebugger.histories.view.BreakpointHistoryView;

import com.sun.jdi.ArrayType;
import com.sun.jdi.BooleanType;
import com.sun.jdi.CharType;
import com.sun.jdi.IntegerType;
import com.sun.jdi.PrimitiveType;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;

/**
 * This class manages the {@link AbstractHistoryController}s of {@link BreakpointHistoryView}s.
 * It offers methods for creating a new view, and handles its closing event.
 * It also offers methods for setting the parameters of the views, and performs
 * automatic adjustment of the views (like setting the widths and time origin) based on the these parameters
 * @author Bilal
 */
public class ArrayViewsManager {

	private ArrayList<AbstractArrayController> historyControllers;

	private SashForm viewsArea;
    
    private ScrolledComposite compViews;
    
    private ArrayViewProperties properties;

	/**
	 * Constructs the {@link ArrayViewsManager} for the given viewsArea and compViews.
	 * The viewArea is a {@link SashForm} that layouts multiple views in a column with sashes for resizing.
	 * The compViews is a {@link ScrolledComposite} provides the scrollbars when necessary. 
	 * @param viewsArea the SashForm. 
	 * @param compViews the ScrolledComposite.
	 */
	public ArrayViewsManager(SashForm viewsArea, ScrolledComposite compViews) {
		historyControllers = new ArrayList<AbstractArrayController>();
		this.viewsArea = viewsArea;
		this.compViews = compViews;
		properties = new ArrayViewProperties(viewsArea.getDisplay());
	}
	int mode;
	/**
	 * Factory method
	 * 
	 * @param parent
	 *            the parent SWT composite in which the view of this controller
	 *            should be created
	 * @param history
	 *            the actual history
	 * @return created {@link AbstractHistoryController}
	 */
	public AbstractArrayController createArrayController(
			final Composite parent,
			final ArrayExpression exp, final FieldImpl field) {
		final AbstractArrayController newController;
		if (exp.getType(field) instanceof PrimitiveType) {
			switch (mode) {
			case ArraysMainController.MODE_SERIES:
				newController = new NumericArrayController(parent, exp, field, properties);
				break;
			case ArraysMainController.MODE_HISTOGRAM:
				newController = new BarChartArrayController(parent, exp, field, properties);
				break;
			default:
				newController = null;					
			}
		}
		else {
			newController = new NonNumericArrayController(parent, exp, field, properties);
		}
		if (newController != null) {
			historyControllers.add(newController);			
			newController.getView().getBtnClose().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {					
					historyControllers.remove(newController);
					adjustWidth();
				}
			});
		}
		return newController;
	}

	/**
	 * Sets the zoom factor for the history views in this manager.
	 * @param value the zoom factor (must be > 0):
	 * 		0 < value < 1 --> zoom out
	 * 		1 = value --> default size
	 * 		1 < value < inifity --> zoom in
	 */
	public void setZoomFactor(float value) {
		properties.zoomFactor = value;
		for (AbstractArrayController hc : historyControllers) {
			hc.getView().redraw();
		}
		adjustWidth();
		
	}
	
	/**
	 * Compute the required widths of the views and adjust the minimum-width of the scrollbar
	 * if necessary
	 */
	public void adjustWidth() {
		int maxWidth = 0;
		for (AbstractArrayController hc : historyControllers) {
			int width = hc.getRequiredWidth();
			if (width > maxWidth) {
				maxWidth  = width;
			}
		}
		compViews.setMinWidth(maxWidth);
		
		/*for (AbstractArrayController hc : historyControllers) {
			hc.refresh();
		}	*/	
	}
	
	public void setInstantSearch(String searchParameter) {
		properties.searchParameter = searchParameter.toLowerCase();
		for (AbstractArrayController hc : historyControllers) {
			hc.getView().redraw();
		}	
	}

	public void setMode(int mode) {
		if (this.mode != mode) {
			this.mode = mode;
			ArrayList<AbstractArrayController> controllers = new ArrayList<AbstractArrayController>(historyControllers);
			for (AbstractArrayController controller : controllers) {
				controller.getView().dispose();
			}
			historyControllers.clear();
			for (AbstractArrayController controller : controllers) {				
				createArrayController(viewsArea, controller.exp, controller.field);
			}
			viewsArea.layout();
		}
	}
	
}
