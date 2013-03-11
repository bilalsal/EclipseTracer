package visdebugger.eclipseuiactions;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.internal.debug.core.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.actions.OpenTypeAction;
import org.eclipse.jdt.internal.debug.ui.console.ConsoleMessages;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.sun.jdi.Location;

/**
 * This class provides a static method to highlight to a Java-code {@link Location} in Eclipse.
 * A location contains a {@link Location#sourcePath()} and {@link Location#lineNumber()}
 * that identify a line in a Java source file.
 * The code was adapted from JavaStackTraceHyperlink
 * @author Bilal
 *
 */
@SuppressWarnings("restriction")
public class SourceLocationNagivator {

	public static void gotoLocation(String sourcepath, int lineNumber, IJavaDebugTarget target) {
		if (lineNumber > 0) {
			lineNumber--;
		}
		startSourceSearch(target.getLaunch(), sourcepath, lineNumber);
	}
	
	/**
	 * Starts a search for the type with the given name. Reports back to 'searchCompleted(...)'.
	 * 
	 * @param typeName the type to search for
	 */
	protected static void startSourceSearch(final ILaunch launch, final String sourcepath, final int lineNumber) {
		Job search = new Job(ConsoleMessages.JavaStackTraceHyperlink_2) {
			protected IStatus run(IProgressMonitor monitor) {
				Object result = null;
				try {
					if (launch != null) {
						result = JavaDebugUtils.resolveSourceElement(sourcepath, launch);
					}
					if (result == null) {
						// search for the type in the workspace
						result = OpenTypeAction.findTypeInWorkspace(sourcepath);
					}
					searchCompleted(result, sourcepath, lineNumber, null);
				} catch (CoreException e) {
					searchCompleted(null, sourcepath, lineNumber, e.getStatus());
				}
				return Status.OK_STATUS;
			}
		
		};
		search.schedule();
	}
	
	protected static void searchCompleted(final Object source, final String typeName, final int lineNumber, final IStatus status) {
		UIJob job = new UIJob("link search complete") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (source == null) {
					if (status == null) {
						// did not find source
						MessageDialog.openInformation(JDIDebugUIPlugin.getActiveWorkbenchShell(), ConsoleMessages.JavaStackTraceHyperlink_Information_1,
								MessageFormat.format(ConsoleMessages.JavaStackTraceHyperlink_Source_not_found_for__0__2, new Object[] {typeName}));
					} else {
						JDIDebugUIPlugin.statusDialog(ConsoleMessages.JavaStackTraceHyperlink_3, status);
					}			
				} else {
					processSearchResult(source, typeName, lineNumber);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}
	
	/**
	 * The search succeeded with the given result
	 * 
	 * @param source resolved source object for the search
	 * @param typeName type name searched for
	 * @param lineNumber line number on link
	 */
	protected static void processSearchResult(Object source, String typeName, int lineNumber) {
		IDebugModelPresentation presentation = JDIDebugUIPlugin.getDefault().getModelPresentation();
		IEditorInput editorInput = presentation.getEditorInput(source);
		if (editorInput != null) {
			String editorId = presentation.getEditorId(editorInput, source);
			if (editorId != null) {
				try { 
					IEditorPart editorPart = JDIDebugUIPlugin.getActivePage().openEditor(editorInput, editorId);
					if (editorPart instanceof ITextEditor && lineNumber >= 0) {
						ITextEditor textEditor = (ITextEditor)editorPart;
						IDocumentProvider provider = textEditor.getDocumentProvider();
						provider.connect(editorInput);
						IDocument document = provider.getDocument(editorInput);
						try {
							IRegion line = document.getLineInformation(lineNumber);
							textEditor.selectAndReveal(line.getOffset(), line.getLength());
						} catch (BadLocationException e) {
                            MessageDialog.openInformation(JDIDebugUIPlugin.getActiveWorkbenchShell(), ConsoleMessages.JavaStackTraceHyperlink_0, 
                            		MessageFormat.format("{0}{1}{2}", new Object[] {(lineNumber+1)+"", ConsoleMessages.JavaStackTraceHyperlink_1, typeName}));                            
						}
						provider.disconnect(editorInput);
					}
				} catch (CoreException e) {
					JDIDebugUIPlugin.statusDialog(e.getStatus()); 
				}
			}
		}		
	}
	
}
