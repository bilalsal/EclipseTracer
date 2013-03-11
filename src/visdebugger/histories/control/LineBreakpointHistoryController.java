package visdebugger.histories.control;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import visdebugger.histories.model.AbstractBreakpointHistory;
import visdebugger.histories.model.AbstractBreakpointValue;
import visdebugger.histories.model.LineBreakpointValue;
import visdebugger.histories.model.VariableHistory;
import visdebugger.histories.view.BreakpointHistoryView;

public class LineBreakpointHistoryController extends AbstractHistoryController {

	private java.util.HashSet<String> legendSet;

	public LineBreakpointHistoryController(Composite parent,
			final AbstractBreakpointHistory history,
			HistoryViewProperties properties) {
		super(parent, history, properties);
		legendSet = new java.util.HashSet<String>();

	}

	@Override
	public int getItemY(int index, int level) {
		int startLevel = properties.keepLevels ? 0 : properties.level;
		return (level - startLevel + 1) * view.getSize().y
				/ (properties.level - startLevel + 2);
	}

	@Override
	protected int getLevelAt(int y) {
		int startLevel = properties.keepLevels ? 0 : properties.level;
		return y *  (properties.level - startLevel + 1) / view.getSize().y;
	}
	
	@Override
	protected void draw(GC g, int from, int to) {
		// check if view size to small
		if (view.getSize().y <= properties.border * 2) {
			if (view.getSize().x > properties.border * 2)
				g.drawLine(properties.border, view.getSize().y / 2,
						view.getSize().x - properties.border,
						view.getSize().y / 2);
			return;
		}

		drawLegend(from, to,g);
		int pointRad = properties.pointRad;
		int startLevel = properties.keepLevels ? 0 : properties.level;
		g.drawLine(getItemX(from), getItemY(from, startLevel),
					getItemX(to) , getItemY(to, startLevel));

		for (int i = from; i <= to; i++) {
			LineBreakpointValue wpv = getValue(i);
			int endLevel = Math.min(properties.level, wpv.getDepth());
			int itemX = getItemX(i);
			g.setLineStyle(SWT.LINE_DOT);
			g.drawLine(itemX, getItemY(i, startLevel), itemX, getItemY(i, endLevel));
			for (int level = startLevel; level <= endLevel; level++) {
				Color pointColor = getPointColor(wpv, level);
				g.setBackground(pointColor);
				int y = getItemY(i, level);

				// get the List of written WP Values an checks if the readAccess
				// Mode is active

				g.fillArc(getItemX(i) - pointRad, y - pointRad, 2 * pointRad,
						2 * pointRad, 0, 360);				
			}


		}
	}
	
	protected BreakpointHistoryView createView(Composite parent,
			final VariableHistory history) {
		// return new ReferenceHistoryView(parent, 0, history);
		return super.createView(parent);
	}

	@Override
	protected List<? extends AbstractBreakpointValue> getActiveValues() {
		return history.getAllValues();
	}

	@Override
	protected LineBreakpointValue getValue(int index) {
		return (LineBreakpointValue) super.getValue(index);
	}
	

	/**
	 * draws the legend 
	 * @param from
	 * @param to
	 * @param g
	 */
	private void drawLegend(int from,int to,GC g) {
		//Legend properties
		int posx = 10,rectSize = 15,space = 10,stringSpace = 5;	
		int height = view.getSize().y-20;
		//iterate  over instances 
		//for(int i = from; i <= to; i++) {	
		for (int i=0;i<getValuesCount();i++) {
			LineBreakpointValue wpv = getValue(i);
			//iterate over all stack levels from a single breakpoint event
			for (int level=properties.level;!(level > properties.level && (wpv.getLineNumber(level-1) == wpv.getLineNumber(level)));level++ ) {
				String colorId = "Default Color";
				if (properties.colorBy == HistoriesMainController.COLOR_BY_METHOD) {
					colorId = wpv.getMethodName(level);

				} else if (properties.colorBy == HistoriesMainController.COLOR_BY_THREAD){
					colorId = wpv.getThreadName();

				}
				//draw item only if not already in legend
				if (legendSet.contains(colorId)) ;
				else {
					//draw legend item
					g.setBackground(this.getPointColor(wpv,level));
					g.fillRectangle(posx, height, rectSize, rectSize);
					g.setBackground(WHITE);
					//draw method name
				    int stringSize =  g.stringExtent(colorId).x;
				    g.drawString(colorId,posx + rectSize + stringSpace, height);
				    posx = posx + rectSize + space + stringSpace + stringSize;
				    legendSet.add(colorId);
				}
			} 
			
		}
		legendSet.clear();				
	}
	
	@Override
	protected Color getPointColor(LineBreakpointValue wpv, int level) {
		if (properties.colorBy == HistoriesMainController.COLOR_BY_VALUE) {
			//its color to red if it matches 
			String b = String.valueOf(wpv);
			String c = b.substring(1, b.length()-1);
			if("null".equals(b)) {	
				return new Color(view.getDisplay(),new RGB(192,192,192));
			}
			if(b.contains(properties.searchParameter) && !properties.searchParameter.isEmpty()) {
				System.out.println(c);
				return new Color(view.getDisplay(),new RGB(255,0,0));
			}
		}
		return super.getPointColor(wpv, level);
	}
	
}
