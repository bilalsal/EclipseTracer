package visdebugger.arrays.view;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.part.ViewPart;

/**
 * A {@link ViewPart} which shows modification to the variables of installed watch points as a table.
 * This View will be loaded automatically in Eclipse and can be opened either from
 * Window -> Show View -> Other -> Other or by clicking the orange button in the toolbar.
 * @author Bilal
 *
 */
public class ArraysViewPart extends ViewPart {

	public static final String ID = "VisDebugger.viewarray";
	
    private Scale sliderZoom;
    
    private SashForm viewsArea;
    
    private Button buttonSeries;

    private Button buttonHistogram;

    private Button buttonTable;
    
    private ScrolledComposite compViews;
    
	private TreeViewer treeWatchedVariables;
	
	private Text txtFullTextSearch;
    
	public ArraysViewPart() {
				
	}

	public void createPartControl(Composite parent) {
		
		SashForm form = new SashForm(parent, SWT.NONE);
				
		Composite dataComp = new Composite(form, SWT.NONE);
		
		dataComp.setLayout(new GridLayout(1, false));
		
		Label lblDebugTargets = new Label(dataComp, SWT.NONE);
		lblDebugTargets.setText("Array Expressions:");

		treeWatchedVariables = new TreeViewer(dataComp, SWT.BORDER);
		treeWatchedVariables.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite viewsComp = new Composite(form, SWT.NONE);
		viewsComp.setLayout(new GridLayout(1, false));
		
		Composite viewsHeader = new Composite(viewsComp, SWT.NONE); 
		viewsHeader.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayout headerLayout = new GridLayout(8, false);
		headerLayout.marginTop = headerLayout.marginBottom = headerLayout.marginHeight = 0;
		viewsHeader.setLayout(headerLayout);

		Label lblShowAs = new Label(viewsHeader, SWT.NONE);
		lblShowAs.setText("View as:");
		
		buttonSeries = new Button(viewsHeader, SWT.RADIO | SWT.SELECTED);
		buttonSeries.setText("Series");
		buttonSeries.setSelection(true);
		
		buttonHistogram = new Button(viewsHeader, SWT.RADIO);
		buttonHistogram.setText("Histogram");
		
		buttonTable = new Button(viewsHeader, SWT.RADIO);
		buttonTable.setText("Table");
		
		
		Label lblFullTextSearch = new Label(viewsHeader, SWT.NONE);
		lblFullTextSearch.setText("Instant Text Search:");
		lblFullTextSearch.setAlignment(SWT.FILL);
		
		txtFullTextSearch = new Text(viewsHeader, SWT.SINGLE);
		
		Label lblZoom = new Label(viewsHeader, SWT.NONE);
		lblZoom.setText("Zoom:");
		
		sliderZoom = new Scale(viewsHeader, SWT.NONE);
		sliderZoom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		sliderZoom.setMinimum(0);
		sliderZoom.setSelection(10);
		sliderZoom.setMaximum(20);
		
		compViews = new ScrolledComposite(viewsComp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		compViews.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));		
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
	}
	
    public void setFocus() {

	}
	
    public SashForm getViewsArea() {
		return viewsArea;
	}    

	public TreeViewer getTreeArrayExpressions() {
		return treeWatchedVariables;
	}
	

	public Scale getSliderZoom() {
		return sliderZoom;
	}
	
	public ScrolledComposite getCompViews() {
		return compViews;
	}
	
	public Text getInstantSearch() {
		return txtFullTextSearch;
	}
	
	public Button getButtonSeries() {
		return buttonSeries;
	}
	
	public Button getButtonHistogram() {
		return buttonHistogram;
	}

	public Button getButtonTable() {
		return buttonTable;
	}
	
}
