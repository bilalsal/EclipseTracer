package visdebugger.arrays.control;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdi.internal.FieldImpl;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.jdt.internal.debug.core.model.JDIPrimitiveValue;
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

import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;

import visdebugger.arrays.control.AbstractArrayController.ArrayViewProperties;
import visdebugger.arrays.model.ArrayExpression;
import visdebugger.arrays.view.ArrayValueView;
import visdebugger.histories.model.WatchPointValue;

public class BarChartArrayController extends AbstractArrayController {
	int [] barHeight;
	int minHeight=0,maxHeight,min,max,range;
	int binSize;
	public BarChartArrayController(Composite parent, ArrayExpression exp,
			FieldImpl field, ArrayViewProperties properties) {
		super(parent, exp, field, properties);
		updateBarChartData();
	}
	void updateBarChartData() {
		updateMinMax();
		calculateBarHeight();

		updateMinMaxBarCount();
	}

	private void calculateBarHeight() {
		properties.barCount = range+1;
		barHeight = new int[properties.barCount];
		for (int i=0;i<barHeight.length;i++) barHeight[i] = 0;
		binSize =1;
		int len;
		try {
			len = exp.getValue().getLength();
		} catch (DebugException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		if (binSize == 1) {
			for (int i = 0; i < len; i++) {
				barHeight[this.getArrayElement(i)-min] ++;
			}

		}

	}

	private int getBarHeight(int index) {
		return barHeight[index];
	}

	/**
	 */
	@Override
	protected int getItemY(int index) {
		int value = getBarHeight(index);
		double barRange = maxHeight- minHeight;
		int areaHeight =  view.getSize().y - properties.border *2;
		return (int)(areaHeight - ((value - minHeight) /barRange) * areaHeight) + properties.border; 

	}	

	protected int getBarCount() {
		return properties.barCount;
	}
	private void updateMinMaxBarCount(){
		
		maxHeight = Integer.MIN_VALUE;

		for (int i = 0; i < barHeight.length; i++) {
			int val = this.getBarHeight(i);
			if (val >= maxHeight) {
				maxHeight = val;
			}
					
		}

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
		range = max-min;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Draws the axis of the view and displays the minimum and maximum values on each axis
	 * @param g - the graphics context 
	 * @param to - the index of the last drawn point
	 */
	private void drawAxis(GC g,int from,int to) {
		g.setBackground(WHITE);
		Rectangle lineArea = new Rectangle(properties.border, properties.border,view.getSize().x - properties.border*2,view.getSize().y - properties.border*2);
		String strMaxY = "" + maxHeight;
		String strMinY = "" + minHeight;
		g.setForeground(new Color(view.getDisplay(), new RGB(0, 0, 0)));
		g.setLineStyle(SWT.LINE_SOLID);
		g.setLineWidth(1);
		//y-Axis
		g.drawLine(lineArea.x,lineArea.y,lineArea.x,lineArea.y + lineArea.height);
		g.drawString(strMaxY, lineArea.x - g.stringExtent(strMaxY).x -g.stringExtent(strMaxY).y/2, lineArea.y - g.stringExtent(strMaxY).y/2);
		g.drawString(strMinY, lineArea.x- g.stringExtent(strMinY).x -g.stringExtent(strMinY).y/2, lineArea.y+lineArea.height -g.stringExtent(strMinY).y/2);
		//x-Axis
		g.drawLine(lineArea.x,lineArea.y + lineArea.height,lineArea.x + this.getRequiredWidth() -  properties.border*2 ,lineArea.y + lineArea.height);
		//display bar chart class ranges
		String text;
		int value = min;
		for (int i=from;i<=to;i++) {

			text =  String.valueOf(value);
			g.drawString(text, getItemX(i) -  g.stringExtent(text).x/2 , lineArea.y +lineArea.height + g.stringExtent(text).y/2);
			value = min +  (i+1)*binSize;
		}
		if (to == getItemCount()-1) {
			text =  String.valueOf(value);
			g.drawString(text, this.getRequiredWidth() -  properties.border -  g.stringExtent(text).x/2, lineArea.y+lineArea.height + g.stringExtent(text).y/2);
		}
	}

	@Override
	protected void draw(GC g, int from, int to) {
		//check if view size to small
		if (view.getSize().y <= properties.border *2) {
			if (view.getSize().x > properties.border*2)
				g.drawLine(properties.border, view.getSize().y/2,view.getSize().x-properties.border,view.getSize().y/2);
			return;
		} 



		int x = getItemX(from);
		int y = getItemY(from);
		int binPixelSize =(int) ( properties.pixelPerValue * properties.zoomFactor);
		String binCount;
		for (int i = from; i <= to; i++) {
			x = getItemX(i);
			y = getItemY(i);
			g.setBackground(properties.barColor);
			//draw bar Rectangle 
			g.fillRectangle(x, y, binPixelSize,view.getSize().y-properties.border - y +1);
			g.setBackground(properties.pointColor);
			//draw Lines surrounding the bar
			g.drawLine(x,view.getSize().y-properties.border,x,y);
			g.drawLine(x, y, binPixelSize+x, y);
			g.drawLine(binPixelSize+x, y, binPixelSize+x, view.getSize().y-properties.border);
			//display value count for current bar
			g.setBackground(WHITE);
			binCount = String.valueOf( getBarHeight(i));
			g.drawString(binCount, x + binPixelSize/2 -  g.stringExtent(binCount).x/2, y - (int)(g.stringExtent(binCount).y*1.5) );

		}	

		drawAxis(g,from,to);
	}
	@Override
	protected int getItemCount() {
		return getBarCount();
	}




}
