package visdebugger.histories.control;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.debug.core.IJavaArrayType;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

import visdebugger.histories.control.AbstractHistoryController.HistoryViewProperties;
import visdebugger.histories.model.AbstractBreakpointHistory;
import visdebugger.histories.model.AbstractBreakpointValue;
import visdebugger.histories.model.NumericVariableHistory;
import visdebugger.histories.model.VariableHistory;
import visdebugger.histories.model.WatchPointValue;
import visdebugger.histories.view.BreakpointHistoryView;

import com.sun.jdi.ArrayType;
import com.sun.jdi.BooleanType;
import com.sun.jdi.CharType;
import com.sun.jdi.IntegerType;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;

/**
 * This class manages the {@link AbstractHistoryController}s of
 * {@link BreakpointHistoryView}s. It offers methods for creating a new view,
 * and handles its closing event. It also offers methods for setting the
 * parameters of the views, and performs automatic adjustment of the views (like
 * setting the widths and time origin) based on the these parameters
 * 
 * @author Bilal
 */
public class HistoryViewsManager {

	private ArrayList<AbstractHistoryController> historyControllers;

	private SashForm viewsArea;

	private ScrolledComposite compViews;

	private HistoryViewProperties properties;

	/**
	 * Constructs the {@link HistoryViewsManager} for the given viewsArea and
	 * compViews. The viewArea is a {@link SashForm} that layouts multiple views
	 * in a column with sashes for resizing. The compViews is a
	 * {@link ScrolledComposite} provides the scrollbars when necessary.
	 * 
	 * @param viewsArea
	 *            the SashForm.
	 * @param compViews
	 *            the ScrolledComposite.
	 */
	public HistoryViewsManager(SashForm viewsArea, ScrolledComposite compViews) {
		historyControllers = new ArrayList<AbstractHistoryController>();
		this.viewsArea = viewsArea;
		this.compViews = compViews;
		properties = new HistoryViewProperties(viewsArea.getDisplay());
	}

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
	public AbstractHistoryController createHistoryController(
			final Composite parent, final AbstractBreakpointHistory history) {
		final AbstractHistoryController newController;
		if (history instanceof NumericVariableHistory) {
			newController = new NumericHistoryController(parent, history,
					properties);
		} else if (history instanceof VariableHistory) {
			newController = new ReferenceHistoryController(parent, history,
					properties);
		} else {
			newController = new LineBreakpointHistoryController(parent,
					history, properties);

		}
		historyControllers.add(newController);
		adjustViewsOrigin();
		newController.getView().getBtnClose()
				.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						historyControllers.remove(newController);
						adjustViewsOrigin();
					}
				});
		return newController;
	}

	/**
	 * Sets the stack level for the coloring by method in the history views in
	 * this manager
	 * 
	 * @param colorBy
	 *            the coloring method
	 */
	public void setLevel(int level) {
		properties.level = level;
		for (AbstractHistoryController hc : historyControllers) {
			hc.refresh();
		}
	}

	/**
	 * Sets the stack level for the coloring by method in the history views in
	 * this manager
	 * 
	 * @param colorBy
	 *            the coloring method
	 */
	public void setKeepLevels(boolean keepLevels) {
		properties.keepLevels = keepLevels;
		for (AbstractHistoryController hc : historyControllers) {
			hc.refresh();
		}
	}

	/**
	 * Sets the method for coloring the points in the history views in this
	 * manager
	 * 
	 * @param colorBy
	 *            the coloring method
	 */
	public void setColorBy(String colorBy) {
		properties.colorBy = colorBy;
		for (AbstractHistoryController hc : historyControllers) {
			hc.refresh();
		}
	}

	/**
	 * Sets whether separate points for read accesses should be shown in the
	 * history views or not.
	 * 
	 * @param value
	 *            true iff the read accesses should be shown
	 */
	public void setShowReadAccesses(boolean value) {
		properties.showReadAccesses = value;
		adjustWidthAndRepaint();
	}

	public void setFullTextSearch(String searchParameter) {
		properties.searchParameter = searchParameter;
		adjustWidthAndRepaint();
		viewsArea.getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				for (AbstractHistoryController hc : historyControllers) {
					hc.getView().update();
				}
			}
		});
	}

	/**
	 * Sets the zoom factor for the history views in this manager.
	 * 
	 * @param value
	 *            the zoom factor (must be > 0): 0 < value < 1 --> zoom out 1 =
	 *            value --> default size 1 < value < inifity --> zoom in
	 */
	public void setZoomFactor(float value) {
		properties.zoomFactor = value;
		adjustWidthAndRepaint();
	}

	/**
	 * Sets whether the points the represent the history should be positioned
	 * along the x-axis by their timestamps or not.
	 * 
	 * @param value
	 *            true: the x-axis represents the time (starting by
	 *            {@link HistoryViewProperties#timeOrigin}) false: the x-axis
	 *            represent the ordered index of the point in the history
	 *            (starting by 0)
	 */
	public void setScaleByTime(boolean value) {
		properties.scaleByTime = value;
		final long minTimestamp = getMinTimeStamp();
		if (minTimestamp == Long.MAX_VALUE) {
			return;
		}
		viewsArea.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				if (properties.scaleByTime) {
					long maxTimeStamp = getMaxTimeStamp();
					// the term (- 4) compansate for the sashform borders
					float milliSecPerPxl = (maxTimeStamp - minTimestamp)
							/ (float) (compViews.getSize().x - 2
									* properties.border - 4);
					properties.millisecPerPixel = milliSecPerPxl;
				}
				for (AbstractHistoryController hc : historyControllers) {
					if (properties.scaleByTime) {
						hc.refresh();
					}
				}
				adjustWidthAndRepaint();
			}
		});
	}

	/**
	 * Sets the time origin for the views. It will be used as origin for the
	 * x-axis when {@link HistoryViewProperties#scaleByTime} is true
	 */
	private void adjustViewsOrigin() {
		properties.timeOrigin = getMinTimeStamp();
		adjustWidthAndRepaint();
	}

	/**
	 * Compute the required widths of the views and adjust the minimum-width of
	 * the scrollbar if necessary
	 */
	private void adjustWidthAndRepaint() {
		int maxWidth = 0;
		for (AbstractHistoryController hc : historyControllers) {
			int width = hc.getRequiredWidth();
			if (width > maxWidth) {
				maxWidth = width;
			}
		}
		compViews.setMinWidth(maxWidth);
		for (AbstractHistoryController hc : historyControllers) {
			hc.refresh();
		}
	}

	/**
	 * 
	 * @return the smallest timestamp in all views (to be used as origin)
	 */
	private long getMinTimeStamp() {
		long minTimeStamp = Long.MAX_VALUE;
		for (AbstractHistoryController hc : historyControllers) {
			long timestamp = hc.getMinTimeStamp();
			if (timestamp < minTimeStamp) {
				minTimeStamp = timestamp;
			}
		}
		return minTimeStamp;
	}

	/**
	 * 
	 * @return the largest timestamp in all views (to be used as origin)
	 */
	private long getMaxTimeStamp() {
		long maxTimeStamp = Long.MIN_VALUE;
		for (AbstractHistoryController hc : historyControllers) {
			long timestamp = hc.getMaxTimeStamp();
			if (timestamp > maxTimeStamp) {
				maxTimeStamp = timestamp;
			}
		}
		return maxTimeStamp;
	}

}
