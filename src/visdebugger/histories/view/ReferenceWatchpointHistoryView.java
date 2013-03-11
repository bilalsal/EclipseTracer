package visdebugger.histories.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import visdebugger.histories.model.VariableHistory;
import visdebugger.histories.model.WatchPointValue;

public class ReferenceWatchpointHistoryView extends BreakpointHistoryView {

	Text txtSearch;
	Button btnFind;
	Composite parent;
	ArrayList<Integer> markedPoints;
	
	public ReferenceWatchpointHistoryView(Composite parent, int style, final VariableHistory history) {
		super(parent, style);

		GridData searchTextGD = new GridData(SWT.LEFT, SWT.TOP, false, false);
		searchTextGD.widthHint = 90;
		searchTextGD.heightHint = 15;
		markedPoints = new ArrayList<Integer>();
		txtSearch = new Text(parent, SWT.NONE | SWT.TRANSPARENT);
		txtSearch.setLayoutData(searchTextGD);

		btnFind = new Button(parent, SWT.NONE | SWT.TRANSPARENT);
		btnFind.setText("Find");
		btnFind.setLayoutData(searchTextGD);
		
		new Listener() {
		      public void handleEvent(Event event) {
		        if (event.widget == btnFind) {
		          if(txtSearch.getText() != null) {
		        	  String txtContent = txtSearch.getText();
		        	  List<WatchPointValue> vl = history.getAllValues();
		        	  int i = 0;
		        	  for(WatchPointValue a : vl) {
		        		  String valueString = a.getValue().toString();
		        		  if(valueString.regionMatches(true, 0, txtContent, 0, valueString.length())) {
		        			 markedPoints.add(i);
		        		  }
		        		  i++;
		        	  }
		          }
		        } 
		      }
		};
	}
}
