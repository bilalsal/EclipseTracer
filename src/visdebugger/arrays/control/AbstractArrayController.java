package visdebugger.arrays.control;

import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdi.internal.FieldImpl;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIValue;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import com.sun.jdi.IntegerValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

import visdebugger.arrays.model.ArrayExpression;
import visdebugger.arrays.view.ArrayValueView;
import visdebugger.histories.model.NumericVariableHistory;
import visdebugger.histories.model.VariableHistory;
import visdebugger.histories.model.WatchPointValue;

/**
 * Controller for the history of a numerical variable.
 * It maps the numerical variable to the y-value of the points representing the history values 
 * @author Bilal
 * @author Peter
 *
 */
public abstract class AbstractArrayController {

	protected final ArrayExpression exp;
	
	protected final ArrayViewProperties properties;
	ArrayValueView view;
	protected final FieldImpl field;
	Color WHITE;
	public final Cursor HAND_CURSOR;

	public static class ArrayViewProperties {
		
		protected float zoomFactor;
		protected long timeOrigin;
		int barCount;
		protected String searchParameter;
		int border;
		int pointRad;
		protected float pixelPerValue;

		protected Color pointColor;
		protected Color barColor;
		public ArrayViewProperties(Device device) {
			zoomFactor = 1;
			border = 50;
			barCount = 10;
			pointRad = 4;
			pixelPerValue = 20;			
			timeOrigin = System.currentTimeMillis();
			pointColor = new Color(device, 50, 50, 50);
			barColor = new Color(device, 153,204,255);
			searchParameter = "";
		}
		
	}
	
	public AbstractArrayController(Composite parent, final ArrayExpression exp, FieldImpl field, ArrayViewProperties properties) {
		this.exp = exp;
		this.field = field;
		this.properties = properties;
		view = new ArrayValueView(parent, SWT.NONE);
		view.getBtnClose().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Composite parent = view.getParent();
				view.dispose();
				parent.layout();
			}
		});
		view.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {		
				draw(e);
			}
		});	
		view.addMouseMoveListener(new MouseMoveListener() {
			
			@Override
			public void mouseMove(MouseEvent e) {
				int ind = getItemIndAt(e.x, e.y);
				if (ind >= 0) {
					view.setCursor(HAND_CURSOR);
					view.setToolTipText(ind + ": " + getArrayElement(ind));
				}
				else {
					view.setCursor(null);
					view.setToolTipText(null);
				}
			}
		});		
		WHITE = new Color(view.getDisplay(), 255, 255, 255);
		HAND_CURSOR = new Cursor(view.getDisplay(), SWT.CURSOR_HAND);
		
	}	

	/**
	 * Returns the index of the {@link WatchPointValue} drawn at the given screen point.
	 * If no point exists, -1 will be return.
	 * The 
	 * @param x the x of the screen point
	 * @param y the y of the screen point
	 * @return the index of the {@link WatchPointValue} drawn at the given screen point.
	 */
	public int getItemIndAt(int x, int y) {
		if (x < properties.border || x > getRequiredWidth() + properties.border) {
			return -1;
		}
		int pointRad = properties.pointRad;
		int ind = getItemIndAtOrAfter(x - pointRad);
		ind = Math.max(Math.min(ind, getItemCount() - 1), 0);
		int x2 = getItemX(ind);
		int y2 = getItemY(ind);
		return Math.abs(x - x2) <= pointRad && Math.abs(y - y2) <= pointRad ? ind : -1;
	}
	
	protected void draw(PaintEvent e) {
		e.gc.setBackground(WHITE);
		e.gc.fillRectangle(e.x, e.y, e.width, e.height);
		String name = exp.getName();
		if (field != null)
			name += "." + field.name();
		e.gc.drawText(name, 5, 10);
		int count = getItemCount();
		int from = Math.max(getItemIndAtOrAfter(e.x) - 1, 0);
		int to = Math.min(getItemIndAtOrAfter(e.x + e.width) + 1, count - 1);
		draw(e.gc, from, to);
	}
	
	int getItemIndAtOrAfter(int x) {
		int reqWidth = getRequiredWidth() - 2 * properties.border;			
		return (x - properties.border + 2 * properties.pointRad) * getItemCount() / reqWidth;
	}
	
	public int getItemX(int index) {
		int x;
			int totalItemLength = getRequiredWidth() - 2 * properties.border;
			x = totalItemLength * index / getItemCount();
		return properties.border + x;
	}
	
	protected abstract int getItemY(int index);
	
	protected abstract int getItemCount();
	
	public int getRequiredWidth() {
		int width;
		width = (int)(getItemCount() * properties.pixelPerValue * properties.zoomFactor);
		return width + 2 * properties.border;
	}


	protected abstract Object getArrayElement(int i);
	
	protected abstract int getBarCount();
	
	/**
	 */
	protected abstract void draw(GC g, int from, int to);
	
	public ArrayValueView getView() {
		return view;
	}
	
	
	
}
