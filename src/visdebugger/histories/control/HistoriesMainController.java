package visdebugger.histories.control;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

import visdebugger.eclipseuiactions.InspectObjectReferenceAction;
import visdebugger.histories.model.AbstractBreakpoint;
import visdebugger.histories.model.AbstractBreakpointHistory;
import visdebugger.histories.model.Breakpoint;
import visdebugger.histories.model.BreakpointsManager;
import visdebugger.histories.model.IBreakpointListener;
import visdebugger.histories.model.VariableHistory;
import visdebugger.histories.model.Watchpoint;
import visdebugger.histories.view.BraekpointHistoriesViewPart;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;

/**
 * The main controller of the variable-history {@link ViewPart}. It reacts to
 * changes from the front-end and from the backend.
 * 
 * @author Bilal
 * 
 */
public class HistoriesMainController implements IBreakpointListener {

	/* Coloring methods constants */

	public final static String COLOR_BY_NONE = "None";

	public final static String COLOR_BY_VALUE = "Value";

	public final static String COLOR_BY_METHOD = "Calling method";

	public final static String COLOR_BY_THREAD = "Calling thread";

	// The SWT view
	private BraekpointHistoriesViewPart view;

	private HistoryViewsManager viewsManager;

	private BreakpointsManager wpManager;

	/**
	 * Connsturcts the controller for the main view.
	 * 
	 * @param view
	 *            the main view
	 */
	public HistoriesMainController(final BraekpointHistoriesViewPart view) {
		this.view = view;
		wpManager = BreakpointsManager.getInstance();
		wpManager.addWatchPointListener(this);
		viewsManager = new HistoryViewsManager(view.getViewsArea(),
				view.getCompViews());

		initWatchedVariablesViewer();

		initDebugTargetsViewer();

		initColorByViewer();

		view.getCbxScaleByTime().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final boolean scaleByTime = view.getCbxScaleByTime()
						.getSelection();
				viewsManager.setScaleByTime(scaleByTime);
			}
		});

		view.getCbxShowReadAccesses().addSelectionListener(
				new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						viewsManager.setShowReadAccesses(view
								.getCbxShowReadAccesses().getSelection());
					}
				});

		view.getSliderZoom().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int val = view.getSliderZoom().getSelection();
				float zoomfactor = val > 10 ? val - 9 : 1f / (11 - val);
				viewsManager.setZoomFactor(zoomfactor);
			}
		});
		view.getScaleLevel().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int level = view.getScaleLevel().getSelection();
				viewsManager.setLevel(level);
			}
		});

		view.getScaleLevel().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean keepLevels = view.getChkKeepLevels().getSelection();
				viewsManager.setKeepLevels(keepLevels);
			}
		});

		view.getTextInstantSearch().addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				String searchParameter = view.getTextInstantSearch().getText();
				viewsManager.setFullTextSearch(searchParameter);
			}

		});
	}

	private void initWatchedVariablesViewer() {

		final TreeViewer watchedVariablesViewer = view
				.getTreeWatchedVariables();
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		Menu menu = menuMgr.createContextMenu(watchedVariablesViewer
				.getControl());
		watchedVariablesViewer.getControl().setMenu(menu);
		menuMgr.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				if (watchedVariablesViewer.getSelection().isEmpty()) {
					return;
				}

				if (watchedVariablesViewer.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) watchedVariablesViewer
							.getSelection();
					Object selectedItem = selection.getFirstElement();

					if (selectedItem instanceof ObjectReference) {
						Shell shell = watchedVariablesViewer.getControl()
								.getShell();
						manager.add(new InspectObjectReferenceAction(shell,
								getSelectedTarget(),
								(ObjectReference) selectedItem));
						manager.add(new Action("Show History") {
							@Override
							public void run() {
								openSelectedVariable();
							}
						});
					}
				}
			}
		});

		// sets the content provider (provides the watchpoints and instances for
		// the selected target)
		watchedVariablesViewer.setContentProvider(new ITreeContentProvider() {

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				IJavaDebugTarget target = getSelectedTarget();
				if (target != null) {
					return wpManager.getWatchpoints(target).toArray();
				} else {
					return Collections.EMPTY_LIST.toArray();
				}
			}

			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof AbstractBreakpoint) {
					IJavaDebugTarget target = getSelectedTarget();
					Set<ObjectReference> instances = wpManager.getInstances(
							(AbstractBreakpoint) element, target);
					return instances.size() > 0
							&& (instances.iterator().next() != null);
				}
				return false;
			}

			@Override
			public Object[] getChildren(Object element) {
				if (element instanceof AbstractBreakpoint) {
					return wpManager.getInstances((AbstractBreakpoint) element,
							getSelectedTarget()).toArray();
				}
				return Collections.EMPTY_LIST.toArray();
			}
		});
		// the input is the debug target (initially null)
		watchedVariablesViewer.setInput(null);

		// the label provider (to show the tree items in a readable format)
		watchedVariablesViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Watchpoint) {
					Watchpoint watchpoint = (Watchpoint) element;
					Field field = watchpoint.getField();
					String typeName = field.declaringType().name();
					int pos = typeName.lastIndexOf(".");
					if (pos > 0) {
						typeName = typeName.substring(pos + 1);
					}
					return typeName + "." + field.name();
				} else if (element instanceof Breakpoint) {
					IJavaLineBreakpoint breakpoint = ((Breakpoint) element)
							.getBreakpoint();
					try {
						return breakpoint.getTypeName() + "(line "
								+ breakpoint.getLineNumber() + ")";
					} catch (CoreException e) {
						return "<error>";
					}
				} else if (element instanceof ObjectReference) {
					String instanceName = element.toString();
					int startPos = instanceName.lastIndexOf('=') + 1;
					int endPos = instanceName.lastIndexOf(')');
					if (startPos >= 0 && endPos >= startPos) {
						instanceName = "id="
								+ instanceName.substring(startPos, endPos);
					}
					return instanceName;
				} else if (element == null) {
					return "<static>";
				} else {
					return super.getText(element);
				}
			}
		});
		// a listener to open the value-history view for the clicked instance
		watchedVariablesViewer.getTree().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				openSelectedVariable();
			}
		});
	}

	private void initDebugTargetsViewer() {
		ComboViewer debugTargetsViewer = view.getComboDebugTarget();
		debugTargetsViewer.setContentProvider(new IStructuredContentProvider() {

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {

			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return wpManager.getDebugTargets().toArray();
			}
		});
		// the label provider (to show the debut targest in a readable format)
		debugTargetsViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				try {
					IJavaDebugTarget target = (IJavaDebugTarget) element;
					String targetName = target.getName();
					int pos = targetName.lastIndexOf(" at ");
					if (pos >= 0) {
						String lName = target.getLaunch()
								.getLaunchConfiguration().getName();
						targetName = lName + targetName.substring(pos);
					}
					return targetName;
				} catch (Exception e) {
					return element.toString();
				}
			}
		});
		debugTargetsViewer.getCombo().addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				view.getTreeWatchedVariables().setInput(getSelectedTarget());
			}
		});
		debugTargetsViewer.setInput(debugTargetsViewer.getContentProvider());

	}

	private void initColorByViewer() {
		ComboViewer comboColorBy = view.getComboColorBy();
		comboColorBy.setContentProvider(new IStructuredContentProvider() {

			String[] colorByOptions = { COLOR_BY_NONE, COLOR_BY_METHOD, COLOR_BY_VALUE,
					COLOR_BY_THREAD };

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {

			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return colorByOptions;
			}
		});
		comboColorBy.setInput("");
		comboColorBy.getCombo().select(0);
		comboColorBy
				.addSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						StructuredSelection selection = (StructuredSelection) event
								.getSelection();
						viewsManager.setColorBy(selection.getFirstElement()
								.toString());
						view.getViewsHeader().redraw();

					}
				});

	}

	private IJavaDebugTarget getSelectedTarget() {
		ISelection selection = view.getComboDebugTarget().getSelection();
		IStructuredSelection structedSel = (IStructuredSelection) selection;
		if (structedSel.size() > 0) {
			return (IJavaDebugTarget) structedSel.getFirstElement();
		}
		return null;
	}

	private void openSelectedVariable() {
		ISelection sel = view.getTreeWatchedVariables().getSelection();
		if (sel != null) {
			ITreeSelection treeSel = (ITreeSelection) sel;
			if (treeSel.size() == 1) {
				Object element = treeSel.getFirstElement();
				AbstractBreakpoint wp = (AbstractBreakpoint) treeSel
						.getPathsFor(element)[0].getFirstSegment();
				ObjectReference obRef = null;
				if (element instanceof ObjectReference) {
					obRef = (ObjectReference) element;
				}
				showViewFor(getSelectedTarget(), wp, obRef);
			}
		}

	}

	// shows the value history view for the given parameters (it will be created
	// if it does not exist yet)
	public void showViewFor(IJavaDebugTarget target, AbstractBreakpoint wp,
			ObjectReference instance) {
		final AbstractBreakpointHistory history = wpManager.getValuesHistory(
				wp, instance, target);
		if (history != null) {
			AbstractHistoryController historyController = history
					.getController();
			if (historyController == null
					|| historyController.getView().isDisposed()) {
				historyController = viewsManager.createHistoryController(
						view.getViewsArea(), history);
				history.setController(historyController);
				view.getViewsArea().layout();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyBreakpointHit(AbstractBreakpoint watchpoint,
			ObjectReference object, IJavaDebugTarget target) {

		AbstractBreakpointHistory history = wpManager.getValuesHistory(
				watchpoint, object, target);
		final AbstractHistoryController valueHistoryController = history
				.getController();
		if (valueHistoryController != null
				&& !valueHistoryController.getView().isDisposed()) {
			final int reqWidth = valueHistoryController.getRequiredWidth();
			valueHistoryController.getView().getDisplay()
					.asyncExec(new Runnable() {

						@Override
						public void run() {
							if (reqWidth > view.getViewsArea().getSize().x) {
								int newWidth = reqWidth
										+ view.getCompViews().getSize().x / 2;
								view.getCompViews().setMinWidth(newWidth);
								view.getCompViews().getHorizontalBar()
										.setSelection(reqWidth);
								view.getCompViews().layout();
							}
							valueHistoryController.refresh();
						}
					});
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyBreakpointInstalled(AbstractBreakpoint watchpoint,
			IJavaDebugTarget target) {
		// views.get(target).put(watchpoint, new HashMap<ObjectReference,
		// AbstractValueHistoryController>());
		final TreeViewer viewer = view.getTreeWatchedVariables();
		if (!viewer.getControl().isDisposed()) {
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					viewer.refresh();
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyDebugTargetAdded(IJavaDebugTarget newTarget) {
		// views.put(newTarget, new HashMap<Watchpoint,
		// HashMap<ObjectReference,AbstractValueHistoryController>>());
		final ComboViewer viewer = view.getComboDebugTarget();
		if (!viewer.getControl().isDisposed()) {
			viewer.getControl().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					int selInd = viewer.getCombo().getSelectionIndex();
					viewer.refresh();
					if (selInd < 0 && viewer.getCombo().getItemCount() > 0) {
						viewer.getCombo().select(0);
					}
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyInstanceAdded(AbstractBreakpoint watchpoint,
			ObjectReference instance, IJavaDebugTarget target) {
		final TreeViewer viewer = view.getTreeWatchedVariables();
		if (!viewer.getControl().isDisposed()) {
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					viewer.refresh();
				}
			});
		}
	}

}
