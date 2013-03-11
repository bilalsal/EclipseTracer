package visdebugger.histories.control;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

import visdebugger.eclipseuiactions.SourceLocationNagivator;
import visdebugger.histories.model.AbstractBreakpointHistory;
import visdebugger.histories.model.AbstractBreakpointValue;
import visdebugger.histories.model.BreakpointsManager;
import visdebugger.histories.model.LineBreakpointValue;
import visdebugger.histories.model.VariableHistory;
import visdebugger.histories.model.WatchPointValue;
import visdebugger.histories.view.BreakpointHistoryView;

/**
 * 
 * This is an abstract controller for Value-History Views. It implements common
 * functionalities
 * 
 * @author Bilal
 * 
 */

abstract public class AbstractHistoryController {

	protected AbstractBreakpointHistory history;
	protected final BreakpointsManager wpManager;
	protected int selectedItem;
	BreakpointHistoryView view;

	/**
	 * This class bundles some parameters (or properties) needed by history
	 * views
	 * 
	 * @author Bilal
	 */
	public static class HistoryViewProperties {

		protected String colorBy;
		protected boolean scaleByTime;
		protected boolean showReadAccesses;
		protected float zoomFactor;
		protected int level;
		protected boolean keepLevels;
		protected float millisecPerPixel;
		protected float pixelPerValue;
		protected long timeOrigin;

		protected String searchParameter;

		int border;
		int pointRad;
		protected Color pointColor;

		public HistoryViewProperties(Device device) {
			zoomFactor = 1;
			millisecPerPixel = 100;
			pixelPerValue = 20;
			border = 50;
			pointRad = 4;
			showReadAccesses = false;
			scaleByTime = false;
			timeOrigin = System.currentTimeMillis();
			colorBy = HistoriesMainController.COLOR_BY_NONE;
			pointColor = new Color(device, 50, 50, 50);
			searchParameter = "";
		}

	}

	/**
	 * The {@link HistoryViewProperties} this controller will use for the
	 * drawing its {@link VariableHistory}
	 */
	final protected HistoryViewProperties properties;

	public final Cursor HAND_CURSOR;
	public final Color WHITE;

	/**
	 * Create a new controller for the given {@link VariableHistory}.
	 * 
	 * @param parent
	 *            the parent Composite in which the view of this controller will
	 *            be created.
	 * @param history
	 *            the {@link VariableHistory}
	 * @param properties
	 *            the {@link HistoryViewProperties} to use
	 */
	protected AbstractHistoryController(Composite parent,
			final AbstractBreakpointHistory history,
			final HistoryViewProperties properties) {
		this.history = history;
		this.properties = properties;
		view = createView(parent);
		wpManager = BreakpointsManager.getInstance();
		HAND_CURSOR = new Cursor(view.getDisplay(), SWT.CURSOR_HAND);
		WHITE = new Color(view.getDisplay(), 255, 255, 255);
		getView().addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				draw(e);
			}
		});
		view.getBtnClose().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Composite parent = view.getParent();
				view.dispose();
				parent.layout();
			}
		});
		view.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				int ind = getItemIndAt(e.x, e.y);
				if (ind >= 0) {
					int level = getLevelAt(e.y);
					view.setCursor(HAND_CURSOR);
					AbstractBreakpointValue wpv = getValue(ind);
					view.setToolTipText(wpv.getTooltipText(level));
				} else {
					view.setCursor(null);
					view.setToolTipText(null);
				}
			}
		});
		view.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				selectedItem = getItemIndAt(e.x, e.y);
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				selectedItem = getItemIndAt(e.x, e.y);
				if (selectedItem >= 0) {
					AbstractBreakpointValue wpv = getValue(selectedItem);
					int level = getLevelAt(e.y);
					// navigate to the source code location
					SourceLocationNagivator.gotoLocation(
							wpv.getSourceName(level),
							wpv.getLineNumber(level),
							history.getDebugTarget());
				}
			}
		});
	}

	/* TRANSFORMATIONS FROM WATCHPOINT <-> SCREEN COORDINATES */

	/**
	 * 
	 * the screen-y value for the given {@link WatchPointValue}
	 * 
	 * @param that
	 *            index of the {@link WatchPointValue} in the values that need
	 *            to be shown.
	 * @return the screen-y value for the given index of a
	 *         {@link WatchPointValue}
	 */
	public int getItemX(int index) {
		int x;
		if (properties.scaleByTime) {
			long timestamp = getValue(index).getTimestamp();
			x = (int) (properties.zoomFactor
					* (timestamp - properties.timeOrigin) / properties.millisecPerPixel);
		} else {
			int count = getActiveValues().size();
			int totalItemLength = getRequiredWidth() - 2 * properties.border;
			x = totalItemLength * index / count;
		}
		return properties.border + x;
	}

	/**
	 * 
	 * the screen-y value for the given {@link WatchPointValue}
	 * 
	 * @param that
	 *            index of the {@link WatchPointValue} in the values that need
	 *            to be shown.
	 * @return the screen-y value for the given index of a
	 *         {@link WatchPointValue}
	 */
	public abstract int getItemY(int index, int level);

	/**
	 * Returns the index of the {@link WatchPointValue} drawn at the given
	 * screen point. If no point exists, -1 will be return. The
	 * 
	 * @param x
	 *            the x of the screen point
	 * @param y
	 *            the y of the screen point
	 * @return the index of the {@link WatchPointValue} drawn at the given
	 *         screen point.
	 */
	public int getItemIndAt(int x, int y) {
		if (x < properties.border || x > getRequiredWidth() + properties.border) {
			return -1;
		}
		int pointRad = properties.pointRad;
		int ind = getItemIndAtOrAfter(x - pointRad);
		
		if (ind >= getValuesCount())
			return -1;
		int x2 = getItemX(ind);
		int y2 = getItemY(ind, getLevelAt(y));
		return Math.abs(x - x2) <= pointRad && Math.abs(y - y2) <= pointRad ? ind
				: -1;
	}

	protected abstract int getLevelAt(int y);

	private int getItemIndAtOrAfter(int x) {
		int reqWidth = getRequiredWidth() - 2 * properties.border;
		if (properties.scaleByTime) {
			List<? extends AbstractBreakpointValue> values = getActiveValues();
			int minInd = 0;
			int maxInd = values.size() - 1;
			while (maxInd >= minInd) {
				int midInd = minInd + (maxInd - minInd) / 2;
				int itemX = getItemX(midInd);
				if (x == itemX) {
					minInd = maxInd = midInd;
					break;
				} else if (x > itemX) {
					minInd = midInd + 1;
				} else {
					maxInd = midInd - 1;
				}
			}
			return Math.min(minInd, values.size() - 1);
		} else {
			int count = getActiveValues().size();
			return (x - properties.border + 2 * properties.pointRad) * count
					/ reqWidth;
		}
	}

	public int getRequiredWidth() {
		int width;
		if (properties.scaleByTime) {
			width = (int) ((getMaxTimeStamp() - properties.timeOrigin)
					* properties.zoomFactor / properties.millisecPerPixel);
		} else {
			int count = getActiveValues().size();
			width = (int) (count * properties.pixelPerValue * properties.zoomFactor);
		}
		return width + 2 * properties.border;
	}

	/* VALUES-RELATED FUNCTIONS */

	/**
	 * Return the {@link WatchPointValue} at the given index from the points
	 * that needs to be shown from the history.
	 * 
	 * @param index
	 *            the index
	 * @return the {@link WatchPointValue}
	 */
	protected AbstractBreakpointValue getValue(int index) {
		return getActiveValues().get(index);
	}

	/**
	 * Returns the number of the points that need to be shown for this history.
	 * If {@link HistoryViewProperties#showReadAccesses} is true, this will
	 * return the number of all accesses (read / write) recorded for the
	 * variable. If {@link HistoryViewProperties#showReadAccesses} is false,
	 * this will return the number of write-accesses recorded for the variable.
	 * 
	 * @return the number of the points that need to be shown for this history.
	 * 
	 */
	protected int getValuesCount() {
		return getActiveValues().size();
	}

	protected abstract List<? extends AbstractBreakpointValue> getActiveValues();

	/**
	 * returns all WatchpointValues where the variable experienced a write
	 * access
	 * 
	 * @return List of modificated WatchpointValues
	 */
	protected List<? extends AbstractBreakpointValue> getModifications() {
		return history.getModifications();
	}

	/**
	 * Return the smallest timestamp in the history managed by this views
	 * 
	 * @return
	 */
	public long getMinTimeStamp() {
		long minTimeStamp = Long.MAX_VALUE;

		int size = getActiveValues().size();
		if (size > 0) {
			minTimeStamp = history.getAllValues().get(0).getTimestamp();
		}
		return minTimeStamp;
	}

	/**
	 * Return the lragest timestamp in the history managed by this views
	 * 
	 * @return
	 */
	public long getMaxTimeStamp() {
		List<? extends AbstractBreakpointValue> values = getActiveValues();
		int size = values.size();
		return (size > 0) ? values.get(size - 1).getTimestamp() : 0;
	}

	/* DRAWING FUNCTIONS */

	protected void draw(PaintEvent e) {
		e.gc.setBackground(WHITE);
		e.gc.fillRectangle(e.x, e.y, e.width, e.height);
		e.gc.drawText(String.valueOf(history.getName()), 5, 10);
		int count = getActiveValues().size();
		int from = Math.max(getItemIndAtOrAfter(e.x) - 1, 0);
		int to = Math.min(getItemIndAtOrAfter(e.x + e.width) + 1, count - 1);
		draw(e.gc, from, to);
	}

	/**
	 * Draws the values the lie in the range [from - to] on the view's canvas.
	 * 
	 * @param g
	 *            The {@link GC} (SWT graphics object)
	 * @param from
	 *            the index of the starting point of the part that needs
	 *            redrawing
	 * @param to
	 *            the index of the to point of the part that needs redrawing
	 */
	protected abstract void draw(GC g, int from, int to);

	/* VIEW-RELATED FUNCTIONS */

	/**
	 * @return the SWT view
	 */
	public BreakpointHistoryView getView() {
		return view;
	}

	/**
	 * updates the view (upon a modification / access to the instance being
	 * watched)
	 */
	public void refresh() {
		view.redraw();
		view.update();
	}

	/**
	 * creates a new {@link BreakpointHistoryView} in the given
	 * {@link Composite}
	 * 
	 * @param parent
	 *            the parent {@link Composite}
	 * @return the created {@link BreakpointHistoryView}
	 */
	protected BreakpointHistoryView createView(Composite parent) {
		return new BreakpointHistoryView(parent, SWT.NONE);

	}

	/**
	 * The history managed by this controller
	 * 
	 * @return
	 */
	public AbstractBreakpointHistory getHistory() {
		return history;
	}


	protected Color getPointColor(LineBreakpointValue wpv, int level) {
		if (properties.colorBy == HistoriesMainController.COLOR_BY_NONE) {
			return properties.pointColor;
		} else if (properties.colorBy == HistoriesMainController.COLOR_BY_METHOD) {
			return  new Color(view.getDisplay(),
					wpManager.getMethodColor(wpv
							.getMethodName(level)));
		} else if (properties.colorBy == HistoriesMainController.COLOR_BY_THREAD) {
			return new Color(view.getDisplay(),
					wpManager.getThreadColor(wpv.getThreadName()));
		}
		return properties.pointColor;
		
	}

}
