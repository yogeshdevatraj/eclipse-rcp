package sample.xml.editor.editors;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dnd.IDragAndDropService;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

public class XMLEditor extends TextEditor {

	private ColorManager colorManager;
	public CustomEditorSupport editorSupport = null;

	private static final String[] RESTRICTED_ACTION = {
		ITextEditorActionConstants.CUT_LINE,
		ITextEditorActionConstants.CUT_LINE_TO_BEGINNING,
		ITextEditorActionConstants.CUT_LINE_TO_END,
		ITextEditorActionConstants.CUT_EXT,
		ITextEditorActionConstants.DELETE_LINE,
		ITextEditorActionConstants.DELETE_LINE_TO_BEGINNING,
		ITextEditorActionConstants.DELETE_LINE_TO_END,
		ITextEditorActionConstants.CONTENT_ASSIST,
		ITextEditorActionConstants.MOVE_LINE_DOWN,
		ITextEditorActionConstants.MOVE_LINE_UP,
		ITextEditorActionConstants.SHIFT_LEFT,
		ITextEditorActionConstants.SHIFT_RIGHT,
		ITextEditorActionConstants.SHIFT_RIGHT_TAB,
};
	public XMLEditor() {
		super();
		editorSupport = new CustomEditorSupport(this);
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new XMLConfiguration(colorManager));
		setDocumentProvider(new XMLDocumentProvider());
	}
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}
	
	public IDocument getDocument() {
		IDocumentProvider documentProvider = getDocumentProvider();
		if (documentProvider != null) {
			return documentProvider.getDocument(getEditorInput());
		}
		return null;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		/* Install Keyboard event consumer to disable any editing  read only
		 * code section */
		getSourceViewer().setEventConsumer(new IEventConsumer() {
			@Override
			public void processEvent(VerifyEvent event) {
				int offset = event.start;
				if(editorSupport.isInsideReadOnlyBlock(offset, 0)){
					// swallow the event.
					event.doit = false;
					return;
				}
			}
		});
		
		LineBackgroundListener lineBackgroundListener = new LineBackgroundListener(){
			@Override
			public void lineGetBackground(LineBackgroundEvent event) {
				int offset = event.lineOffset;
				if(editorSupport.isInsideReadOnlyBlock(offset, 0)){
					event.lineBackground = colorManager.getColor(new RGB(245,245,245));
				}
			}
		};
		//Install line background color listener on source viewer 
		getSourceViewer().getTextWidget().addLineBackgroundListener(lineBackgroundListener);
		
		/*Provide Cursor listen on Source Viewer to handle TExt Editor action*/
		getSourceViewer().getTextWidget().addCaretListener(new CaretListener() {
			
			@Override
			public void caretMoved(CaretEvent event) {
				ITextSelection sel= (ITextSelection) getSourceViewer().getSelectionProvider().getSelection();
				if(sel.getLength()>1){
					/*Convert widget offset to model offset*/
					int selOffset = ((SourceViewer)getSourceViewer()).modelOffset2WidgetOffset(sel.getOffset());
					if(editorSupport.isInsideReadOnlyBlock(selOffset, 0)){
						/*If cureent cursor is in read-only code section*/
						enableTextEditorAction(false);
					} else {
						/*If current cursor is in editable code section*/
						enableTextEditorAction(true);
					}
				} else {
					/*Convert widget offset to model offset*/
					int selOffest = ((SourceViewer)getSourceViewer()).modelOffset2WidgetOffset(event.caretOffset);
					if(editorSupport.isInsideReadOnlyBlock(selOffest, 0)){
						/*If cureent cursor is in read-only code section*/
						enableTextEditorAction(false);
					} else {
						/*If current cursor is in editable code section*/
						enableTextEditorAction(true);
					}
				}
				
			}
		});
		
		restrictDragAndDrop(getSourceViewer());
	}
	
	
	/**
	 * Enable/Disable text editor actions
	 * @param flag <code>true/false</code>
	 */
	private void enableTextEditorAction(boolean flag){
		for(String actionID : RESTRICTED_ACTION){
				IAction action = this.getAction(actionID);
				if(action!=null)
				action.setEnabled(flag);
		}
	}
	
	/**
	 * Installs text drag and drop on the given source viewer.
	 *
	 */
	protected void restrictDragAndDrop(final ISourceViewer viewer) {
		if (viewer == null)
			return;
		/**
		 * Get Drag and drop service from editor.
		 */
		final IDragAndDropService dndService= (IDragAndDropService)this.getSite().getService(IDragAndDropService.class);
		if (dndService == null)
			return;

		final StyledText st= viewer.getTextWidget();

		// Install drag target
		DropTargetListener dropTargetListener= new DropTargetAdapter() {

			private Point fSelection;

			public void dragEnter(DropTargetEvent event) {
				//fTextDragAndDropToken= null;
				fSelection= st.getSelection();
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_MOVE) != 0) {
						event.detail= DND.DROP_MOVE;
					} else if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail= DND.DROP_COPY;
					} else {
						event.detail= DND.DROP_NONE;
					}
				}
			}

			public void dragOperationChanged(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_MOVE) != 0) {
						event.detail= DND.DROP_MOVE;
					} else if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail= DND.DROP_COPY;
					} else {
						event.detail= DND.DROP_NONE;
					}
				}
			}

			public void dragOver(DropTargetEvent event) {
				event.feedback |= DND.FEEDBACK_SCROLL;
			}

			public void drop(DropTargetEvent event) {
				try {
					/*If drop offset falls under read-only section then ignore it*/
					Point newSelection= st.getSelection();
					int modelOffset= ((SourceViewer)viewer).widgetOffset2ModelOffset(newSelection.x);
					if(editorSupport.isInsideReadOnlyBlock(modelOffset,0)){
						event.detail= DND.DROP_NONE;
						return;
					} 
					
					if (/*fTextDragAndDropToken != null &&*/ event.detail == DND.DROP_MOVE) {
						// Move in same editor
						int caretOffset= st.getCaretOffset();
						if (fSelection.x <= caretOffset && caretOffset <= fSelection.y) {
							event.detail= DND.DROP_NONE;
							return;
						}

						// Start compound change
						IRewriteTarget target= (IRewriteTarget)XMLEditor.this.getAdapter(IRewriteTarget.class);
						if (target != null)
							target.beginCompoundChange();
					}

					if (!((TextEditor)XMLEditor.this).validateEditorInputState()) {
						event.detail= DND.DROP_NONE;
						return;
					}

					String text= (String)event.data;
					if (((TextEditor)XMLEditor.this).isBlockSelectionModeEnabled()) {
						// FIXME fix block selection and DND
//						if (fTextDNDColumnSelection != null && fTextDragAndDropToken != null && event.detail == DND.DROP_MOVE) {
//							// DND_MOVE within same editor - remove origin before inserting
//							Rectangle newSelection= st.getColumnSelection();
//							st.replaceColumnSelection(fTextDNDColumnSelection, ""); //$NON-NLS-1$
//							st.replaceColumnSelection(newSelection, text);
//							st.setColumnSelection(newSelection.x, newSelection.y, newSelection.x + fTextDNDColumnSelection.width - fTextDNDColumnSelection.x, newSelection.y + fTextDNDColumnSelection.height - fTextDNDColumnSelection.y);
//						} else {
//							Point newSelection= st.getSelection();
//							st.insert(text);
//							IDocument document= getDocumentProvider().getDocument(getEditorInput());
//							int startLine= st.getLineAtOffset(newSelection.x);
//							int startColumn= newSelection.x - st.getOffsetAtLine(startLine);
//							int endLine= startLine + document.computeNumberOfLines(text);
//							int endColumn= startColumn + TextUtilities.indexOf(document.getLegalLineDelimiters(), text, 0)[0];
//							st.setColumnSelection(startColumn, startLine, endColumn, endLine);
//						}
					} else {
						//Point newSelection= st.getSelection();
						try {
							modelOffset= ((SourceViewer)viewer).widgetOffset2ModelOffset(newSelection.x);
							viewer.getDocument().replace(modelOffset, 0, text);
						} catch (BadLocationException e) {
							return;
						}
						st.setSelectionRange(newSelection.x, text.length());
					}
				} finally {
				}
			}
		};
		dndService.addMergedDropTarget(st, DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] {TextTransfer.getInstance()}, dropTargetListener);
	}
	

}
