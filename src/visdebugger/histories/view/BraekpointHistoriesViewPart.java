package visdebugger.histories.view;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import visdebugger.histories.control.HistoriesMainController;

/**
 * A {@link ViewPart} which shows modification to the variables of installed watch points as a table.
 * This View will be loaded automatically in Eclipse and can be opened either from
 * Window -> Show View -> Other -> Other or by clicking the orange button in the toolbar.
 * @author Bilal
 *
 */
public class BraekpointHistoriesViewPart extends ViewPart {

	public static final String ID = "VisDebugger.viewmain";
	
    HistoriesMainController controller;
    
    private ComboViewer comboDebugTarget;

    private Button cbxScaleByTime;
    
    private Button cbxShowReadAccesses;
    
    private ComboViewer comboColorBy;
    
    private Scale sliderZoom;
    
    private SashForm viewsArea;
    
    private ScrolledComposite compViews;
    
	private TreeViewer treeWatchedVariables;
	
	private Text txtInstantSearch;
	
	private Scale scaleLevel;
	
	private Button chkKeepLevels;
	
	private Composite viewsHeader;
    
	public BraekpointHistoriesViewPart() {
				
	}

	public void createPartControl(Composite parent) {
		
		SashForm form = new SashForm(parent, SWT.NONE);
				
		Composite dataComp = new Composite(form, SWT.NONE);
		
		dataComp.setLayout(new GridLayout(2, false));
		
		Label lblDebugTargets = new Label(dataComp, SWT.NONE);
		lblDebugTargets.setText("Targets:");
		
		comboDebugTarget = new ComboViewer(dataComp, SWT.READ_ONLY);
		comboDebugTarget.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		treeWatchedVariables = new TreeViewer(dataComp, SWT.BORDER);
		treeWatchedVariables.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		Composite viewsComp = new Composite(form, SWT.NONE);
		viewsComp.setLayout(new GridLayout(1, false));
		
	    viewsHeader = new Composite(viewsComp, SWT.NONE); 
		viewsHeader.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayout headerLayout = new GridLayout(10, false);
		
		headerLayout.marginTop = headerLayout.marginBottom = headerLayout.marginHeight = 0;
		viewsHeader.setLayout(headerLayout);

		cbxScaleByTime = new Button(viewsHeader, SWT.CHECK);
		cbxScaleByTime.setText("Scale by time");

		cbxShowReadAccesses = new Button(viewsHeader, SWT.CHECK);
		cbxShowReadAccesses.setText("Show read-accesses");
 
		Label lblColor = new Label(viewsHeader, SWT.NONE);
		lblColor.setText("Color by:");
		
		comboColorBy = new ComboViewer(viewsHeader, SWT.READ_ONLY);
		
		scaleLevel = new Scale(viewsHeader,SWT.VERTICAL);
		GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		scaleLevel.setLayoutData(data);
		scaleLevel.setMinimum(0);
		scaleLevel.setSelection(0);
		scaleLevel.setMaximum(3);
		scaleLevel.setPageIncrement(1);
		scaleLevel.setIncrement(1);
		scaleLevel.setToolTipText("set stack level");
		data.heightHint = 40;
		data.widthHint = 40;
		

		chkKeepLevels = new Button(viewsHeader, SWT.CHECK);
		chkKeepLevels.setText("Keep levels");


		
		//if (comboColorBy.getSelection())
		Label lblInstantSearch = new Label(viewsHeader, SWT.NONE);
		lblInstantSearch.setText("     Instant Search:");
		lblInstantSearch.setAlignment(SWT.FILL);
		
		txtInstantSearch = new Text(viewsHeader, SWT.SINGLE | SWT.BORDER);
		
		Label lblZoom = new Label(viewsHeader, SWT.NONE);
		lblZoom.setText("Zoom:");
		
		sliderZoom = new Scale(viewsHeader, SWT.NONE);
		sliderZoom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		sliderZoom.setMinimum(0);
		sliderZoom.setSelection(10);
		sliderZoom.setMaximum(20);
		
		compViews = new ScrolledComposite(viewsComp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		compViews.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));		
//		compViews.setLayout(new FormLayout());
		viewsArea = new SashForm(compViews, SWT.NONE);
		viewsArea.setOrientation(SWT.VERTICAL);
		compViews.setContent(viewsArea);
		form.setWeights(new int[]{1, 3});
		compViews.setExpandHorizontal(true);
		compViews.setExpandVertical(true);
		compViews.addControlListener( new ControlAdapter() {
		    @Override
		    public void controlResized( ControlEvent e ) {
		        ScrollBar sbX = compViews.getHorizontalBar();
		        if ( sbX != null ) {
		            sbX.setPageIncrement( sbX.getThumb() );
		            sbX.setIncrement( Math.max( 1, sbX.getThumb() / 5 ) );
		        }
		    }
		});

		controller = new HistoriesMainController(this);
	}
	
    public void setFocus() {

	}
	
    public SashForm getViewsArea() {
		return viewsArea;
	}    

	public TreeViewer getTreeWatchedVariables() {
		return treeWatchedVariables;
	}
	
	public ComboViewer getComboDebugTarget() {
		return comboDebugTarget;
	}
	
	public ComboViewer getComboColorBy() {
		return comboColorBy;
	}

	public Button getCbxScaleByTime() {
		return cbxScaleByTime;
	}
	
	public Button getCbxShowReadAccesses() {
		return cbxShowReadAccesses;
	}
	
	public Scale getSliderZoom() {
		return sliderZoom;
	}
	
	public ScrolledComposite getCompViews() {
		return compViews;
	}
	
	public Text getTextInstantSearch() {
		return txtInstantSearch;
	}
	
	public Scale getScaleLevel() {
		return scaleLevel;
	}
	public Composite getViewsHeader() {
		return viewsHeader;
	}
	
	public Button getChkKeepLevels() {
		return chkKeepLevels;
	}
	
	
}
