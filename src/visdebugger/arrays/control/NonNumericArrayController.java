package visdebugger.arrays.control;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.jdi.internal.FieldImpl;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.model.JDIObjectValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

import visdebugger.arrays.model.ArrayExpression;

public class NonNumericArrayController extends AbstractArrayController {

	public NonNumericArrayController(Composite parent, ArrayExpression exp,
			FieldImpl field, ArrayViewProperties properties) {
		super(parent, exp, field, properties);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void draw(GC g, int from, int to) {
		
		if (view.getSize().y <= properties.border *2) {
			if (view.getSize().x > properties.border*2)
			g.drawLine(properties.border, view.getSize().y/2,view.getSize().x-properties.border,view.getSize().y/2);
			return;
		} 
		
		drawAxis(g, to);
		
		int rad = properties.pointRad;
		for (int i = from; i <= to; i++) {
			Color pointColor =  properties.pointColor;
			String b = getArrayElement(i);
			if(b != null && !properties.searchParameter.isEmpty() &&
					b.toLowerCase().contains(properties.searchParameter)) {
				pointColor = new Color(view.getDisplay(),new RGB(255,0,0));
			}
			g.setBackground(pointColor);
			g.fillArc(getItemX(i) - rad, getItemY(i) - rad, 2 * rad, 2 * rad, 0, 360);
		}
		
	}
	
	@Override
	protected int getItemY(int i) {
		return view.getSize().y / 2;

	}
	
	@Override
	protected int getBarCount() {
		// TODO Auto-generated method stub
		return 10;
	}

	@Override
	protected String getArrayElement(int i) {
		try {
			ObjectReference val = ((JDIObjectValue)exp.getValue().getValue(i)).getUnderlyingObject();
			if (field == null) {
				return val.toString();
			}
			else {
				return val.getValue(field).toString();
			}
		} catch (DebugException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private void drawAxis(GC g,int to) {
		String maximumX = ""+(this.getItemCount()-1);
		String minimumX = "0";

		Rectangle lineArea = new Rectangle(properties.border, properties.border,view.getSize().x - properties.border*2,view.getSize().y - properties.border*2);
		g.setForeground(new Color(view.getDisplay(), new RGB(0, 0, 0)));
		g.setLineStyle(SWT.LINE_SOLID);
		g.setLineWidth(1);
		//g.drawLine(lineArea.x,lineArea.y,lineArea.x,lineArea.y + lineArea.height);
		g.drawLine(lineArea.x,lineArea.y + lineArea.height,lineArea.x + lineArea.width,lineArea.y + lineArea.height);
		g.drawString(minimumX, lineArea.x -  g.stringExtent(minimumX).x/2 , lineArea.y +lineArea.height + g.stringExtent(minimumX).y/2);
		g.drawString(maximumX, getItemX(this.getItemCount()-1) -  g.stringExtent(maximumX).x/2, lineArea.y+lineArea.height + g.stringExtent(maximumX).y/2);
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
