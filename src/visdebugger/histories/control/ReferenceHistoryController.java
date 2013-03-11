package visdebugger.histories.control;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import com.sun.jdi.ObjectReference;

import visdebugger.eclipseuiactions.InspectObjectReferenceAction;
import visdebugger.histories.model.AbstractBreakpointHistory;
import visdebugger.histories.model.AbstractBreakpointValue;
import visdebugger.histories.model.VariableHistory;
import visdebugger.histories.model.WatchPointValue;
import visdebugger.histories.view.BreakpointHistoryView;
import visdebugger.histories.view.ReferenceWatchpointHistoryView;

public class ReferenceHistoryController extends LineBreakpointHistoryController {

	public ReferenceHistoryController(Composite parent, final AbstractBreakpointHistory history, HistoryViewProperties properties) {
		super(parent, history, properties);
		
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		Menu menu = menuMgr.createContextMenu(view);
		view.setMenu(menu);
		menuMgr.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				
				if (selectedItem >= 0) {
					WatchPointValue wpv = getValue(selectedItem);

					Shell shell = view.getShell();
					manager.add(new InspectObjectReferenceAction(shell,
							history.getDebugTarget(),
							(ObjectReference)wpv.getValue()));

				}
				
			}
		});
		
	}

	@Override
	public int getItemY(int index, int level) {
		return view.getSize().y / 2;
	}

	@Override
	protected void draw(GC g, int from, int to) {
		super.draw(g, from, to);

	}
	
	protected BreakpointHistoryView createView(Composite parent, final VariableHistory history) {
		//return new ReferenceHistoryView(parent, 0, history);
		return super.createView(parent);
	}

	@Override
	protected List<? extends AbstractBreakpointValue> getActiveValues() {
		return properties.showReadAccesses ?
				history.getAllValues() : history.getModifications();	
	}
	
	@Override
	protected WatchPointValue getValue(int index) {
		return (WatchPointValue)super.getValue(index);
	}

	@Override
	protected int getLevelAt(int y) {
		return properties.level;
	}
}
