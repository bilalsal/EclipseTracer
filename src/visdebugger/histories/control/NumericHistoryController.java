package visdebugger.histories.control;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import com.sun.jdi.IntegerValue;

import visdebugger.histories.model.AbstractBreakpointHistory;
import visdebugger.histories.model.AbstractBreakpointValue;
import visdebugger.histories.model.NumericVariableHistory;
import visdebugger.histories.model.VariableHistory;
import visdebugger.histories.model.WatchPointValue;

/**
 * Controller for the history of a numerical variable. It maps the numerical
 * variable to the y-value of the points representing the history values
 * 
 * @author Bilal
 * @author Peter
 * 
 */
public class NumericHistoryController extends AbstractHistoryController {

	public NumericHistoryController(Composite parent,
			final AbstractBreakpointHistory history,
			HistoryViewProperties properties) {
		super(parent, history, properties);
	}

	/**
	 * Draws the axis of the view and displays the minimum and maximum values on
	 * each axis
	 * 
	 * @param g
	 *            - the graphics context
	 * @param to
	 *            - the index of the last drawn point
	 */
	private void drawAxis(GC g, int to) {
		String strMaxX;
		String strMinX;
		if (properties.scaleByTime) {
			strMaxX = "" + getMaxTimeStamp();
			strMinX = "" + getMinTimeStamp();
		} else {
			strMaxX = "" + getValuesCount();
			strMinX = "" + 1;
		}
		Rectangle lineArea = new Rectangle(properties.border,
				properties.border, view.getSize().x - properties.border * 2,
				view.getSize().y - properties.border * 2);
		String strMaxY = ""
				+ ((IntegerValue) getHistory().getMaxValue().getValue())
						.value();
		String strMinY = ""
				+ ((IntegerValue) getHistory().getMinValue().getValue())
						.value();
		g.setForeground(new Color(view.getDisplay(), new RGB(0, 0, 0)));
		g.setLineStyle(SWT.LINE_SOLID);
		g.setLineWidth(1);
		g.drawLine(lineArea.x, lineArea.y, lineArea.x, lineArea.y
				+ lineArea.height);
		g.drawLine(lineArea.x, lineArea.y + lineArea.height, lineArea.x
				+ lineArea.width, lineArea.y + lineArea.height);
		g.drawString(
				strMaxY,
				lineArea.x - g.stringExtent(strMaxY).x
						- g.stringExtent(strMaxY).y / 2,
				lineArea.y - g.stringExtent(strMaxY).y / 2);
		g.drawString(
				strMinY,
				lineArea.x - g.stringExtent(strMinY).x
						- g.stringExtent(strMinY).y / 2, lineArea.y
						+ lineArea.height - g.stringExtent(strMinY).y / 2);
		g.drawString(strMinX, lineArea.x - g.stringExtent(strMinX).x / 2,
				lineArea.y + lineArea.height + g.stringExtent(strMinX).y / 2);
		g.drawString(strMaxX,
				getItemX(this.getValuesCount() - 1) - g.stringExtent(strMaxX).x
						/ 2,
				lineArea.y + lineArea.height + g.stringExtent(strMaxX).y / 2);

	}

	/**
	 * The history managed by this controller
	 * 
	 * @return
	 */
	public NumericVariableHistory getHistory() {
		return (NumericVariableHistory) super.getHistory();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getItemY(int index, int level) {
		WatchPointValue minVal = getHistory().getMinValue();
		WatchPointValue maxVal = getHistory().getMaxValue();
		// return the correct Y value
		int min = ((IntegerValue) minVal.getValue()).value();
		int max = ((IntegerValue) maxVal.getValue()).value();
		double range = max - min;
		int value = ((IntegerValue) getValue(index).getValue()).value();
		int height;
		if (min == max) {
			height = view.getSize().y / 2;
		} else {
			int areaHeight = view.getSize().y - properties.border * 2;
			height = new Double(areaHeight
					- (((value - min) / range) * (areaHeight))
					+ properties.border).intValue();
		}
		return height;
	}

	@Override
	protected void draw(PaintEvent e) {
		// draw the part of the axis inside e.width, e.x, e.y, e.height;

		// this call is necssary to trigger the draw(GC, from, to) method:
		super.draw(e);

		// Here you can draw things after the axis and the points are drawn.
	}

	/**
	 * {@inheritDoc}
	 */
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
		drawAxis(g, to);
		int pointRad = properties.pointRad;
		int prevRange = 0;
		List<? extends AbstractBreakpointValue> writerWPs = getModifications();
		for (int i = from; i <= to; i++) {
			// draw the step-chart.
			// Pay attention to the activation ranges of the watchpoint
			// (the user can stop the value-tracking and reactivate it -->
			// the visualization should depict the activation ranges by showing
			// discontinuity)
			// history.getWatchpoint().getTrackingActivationRangesCount();
			// history.getWatchpoint().getTrackingActivationRange(history.getDebugTarget(),
			// index);

			WatchPointValue wpv = getValue(i);
			for (int j = prevRange; j < history.getBreakpoint()
					.getTrackingActivationRangesCount(); j++) {
				int[] range = history
						.getBreakpoint()
						.getTrackingActivationRange(history.getDebugTarget(), j);
				if (wpv.getTimestamp() >= range[0]
						&& (wpv.getTimestamp() <= range[1] || range[1] == -1)) {
					if (prevRange == j && i > 0) {
						int[] line = { getItemX(i - 1), getItemY(i - 1, properties.level),
								getItemX(i), getItemY(i - 1, properties.level), getItemX(i),
								getItemY(i, properties.level) };
						g.drawPolyline(line);
					}
					prevRange = j;

					break;
				}
			}
			// get and use the correct color according to properties.colorBy
			// wpManager.getMethodColor(wpv.getMethodName());
			Color pointColor = getPointColor(wpv, properties.level);
			g.setBackground(pointColor);
			// get the List of written WP Values an checks if the readAccess
			// Mode is active
			if (properties.showReadAccesses) {
				// if the current watchpoint is in the list of written WPVs draw
				// it as circle else draw it as rectangle
				if (writerWPs.contains(wpv)) {
					g.fillArc(getItemX(i) - pointRad, getItemY(i, properties.level) - pointRad,
							2 * pointRad, 2 * pointRad, 0, 360);
				} else {
					g.fillRectangle(getItemX(i) - pointRad / 2, getItemY(i, properties.level)
							- pointRad / 2, pointRad, pointRad);
				}
				// draw all WPVs as circles
			} else {
				g.fillArc(getItemX(i) - pointRad, getItemY(i, properties.level) - pointRad,
						2 * pointRad, 2 * pointRad, 0, 360);
			}

		}
	}

	@Override
	protected List<? extends AbstractBreakpointValue> getActiveValues() {
		return properties.showReadAccesses ? history.getAllValues() : history
				.getModifications();
	}

	@Override
	protected WatchPointValue getValue(int index) {
		return (WatchPointValue) super.getValue(index);
	}

	@Override
	protected int getLevelAt(int y) {
		return properties.level;
	}
}
