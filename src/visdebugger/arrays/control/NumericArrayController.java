package visdebugger.arrays.control;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdi.internal.FieldImpl;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIPrimitiveValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import com.sun.jdi.IntegerValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;

import visdebugger.arrays.model.ArrayExpression;
import visdebugger.histories.model.WatchPointValue;

public class NumericArrayController extends AbstractArrayController {

	
	int min, max;
	
	public NumericArrayController(Composite parent, ArrayExpression exp, FieldImpl field, ArrayViewProperties properties) {
		super(parent, exp, field, properties);
		updateMinMax();
	}
	
	private void updateMinMax() {
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		try {
			for (int i = 0; i < exp.getValue().getLength(); i++) {
				int val = getArrayElement(i);
				if (val >= max) {
					max = val;
				}
				if (val <= min) {
					min = val;
				}				
			}
		} catch (DebugException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 */
	@Override
	protected int getItemY(int index) {
		double range = max - min;
		int value = getArrayElement(index);
		int height;
		if (min == max) {
			height = view.getSize().y / 2;
		} else {
			int areaHeight =  view.getSize().y - properties.border *2;
			height = (int)(areaHeight - ((value - min) / range) * areaHeight) + properties.border; 
		}
		return height;
	}	
	
	
	
	/**
	 * Draws the axis of the view and displays the minimum and maximum values on each axis
	 * @param g - the graphics context 
	 * @param to - the index of the last drawn point
	 */
	private void drawAxis(GC g,int to) {
		String strMaxX;
		String strMinX;
		strMaxX = "" + (this.getItemCount()-1);
		strMinX = "" + 0;
		
		Rectangle lineArea = new Rectangle(properties.border, properties.border,view.getSize().x - properties.border*2,view.getSize().y - properties.border*2);
		String strMaxY = "" + max;
		String strMinY = "" + min;
		g.setForeground(new Color(view.getDisplay(), new RGB(0, 0, 0)));
		g.setLineStyle(SWT.LINE_SOLID);
		g.setLineWidth(1);
		g.drawLine(lineArea.x,lineArea.y,lineArea.x,lineArea.y + lineArea.height);
		g.drawLine(lineArea.x,lineArea.y + lineArea.height,lineArea.x + lineArea.width,lineArea.y + lineArea.height);
		g.drawString(strMaxY, lineArea.x - g.stringExtent(strMaxY).x -g.stringExtent(strMaxY).y/2, lineArea.y - g.stringExtent(strMaxY).y/2);
		g.drawString(strMinY, lineArea.x- g.stringExtent(strMinY).x -g.stringExtent(strMinY).y/2, lineArea.y+lineArea.height -g.stringExtent(strMinY).y/2);
		g.drawString(strMinX, lineArea.x -  g.stringExtent(strMinX).x/2 , lineArea.y +lineArea.height + g.stringExtent(strMinX).y/2);
		g.drawString(strMaxX, getItemX(this.getItemCount()-1) -  g.stringExtent(strMaxX).x/2, lineArea.y+lineArea.height + g.stringExtent(strMaxX).y/2);

	}

	@Override
	protected void draw(GC g, int from, int to) {
		//check if view size to small
		if (view.getSize().y <= properties.border *2) {
			if (view.getSize().x > properties.border*2)
			g.drawLine(properties.border, view.getSize().y/2,view.getSize().x-properties.border,view.getSize().y/2);
			return;
		} 
		drawAxis(g, to);
		int rad = properties.pointRad;
		g.setBackground(properties.pointColor);
		int x = getItemX(from);
		int y = getItemY(from);
		int newX = 0, newY = 0;
		for (int i = from; i <= to; i++) {
			if (i < to) {
				newX = getItemX(i + 1);
				newY = getItemY(i + 1);
				g.drawLine(x, y, newX, newY);
			}
			g.fillArc(x - rad, getItemY(i) - rad,
					2 * rad, 2 * rad, 0, 360);
			x = newX;
			y = newY;
		}		
	}

	@Override
	protected int getBarCount() {
		return properties.barCount;
	}

	@Override
	protected Integer getArrayElement(int i) {
		try {
			IJavaValue val = exp.getValue().getValue(i);
			if (field == null) {
				return ((JDIPrimitiveValue)val).getIntValue();
			}
			else {
				Value fieldVal = ((JDIObjectValue)val).getUnderlyingObject().getValue(field);
				return ((PrimitiveValue)fieldVal).intValue();
			}
		} catch (DebugException e) {
			System.out.println("index: ");
			e.printStackTrace();
			return null;
		}
	}


	@Override
	protected int getItemCount() {
		try {
			return exp.getValue().getLength();
		} catch (DebugException e) {
			e.printStackTrace();
			return 0;
		}
	}

}
