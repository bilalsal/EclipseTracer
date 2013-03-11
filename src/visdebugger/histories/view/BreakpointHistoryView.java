package visdebugger.histories.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 * This is an abstract view for Value-History Views.
 * It implements common functionalities like a close button 
 * @author Bilal
 *
 */
public class BreakpointHistoryView extends Composite {
	
	
	Button btnClose; 
	
	public BreakpointHistoryView(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));
		btnClose = new Button(this, SWT.NONE | SWT.TRANSPARENT);
		btnClose.setText("X");		
		btnClose.getFont().getFontData()[0].setHeight(8);
		GridData btnCloseGD = new GridData(SWT.RIGHT, SWT.TOP, true, false);
		btnCloseGD.widthHint = 15;
		btnCloseGD.heightHint = 15;
		btnClose.setLayoutData(btnCloseGD);
	}
	public void draw(PaintEvent e) {
		
	}
	
	/**
	 * 
	 * @return the close button
	 */
	public Button getBtnClose() {
		return btnClose;
	}

}
