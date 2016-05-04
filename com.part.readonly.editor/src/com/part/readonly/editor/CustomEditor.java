package com.part.readonly.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class CustomEditor extends TextEditor {
	CustomEditorSupport editorSupport = null;
	
	public CustomEditor(){
		editorSupport = new CustomEditorSupport(this);
	}
	
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		setTitle(input.getName());
	}
	
	/**
	 */
	public IDocument getDocument() {
		IDocumentProvider documentProvider = getDocumentProvider();
		if (documentProvider != null) {
			return documentProvider.getDocument(getEditorInput());
		}
		return null;
	}
	
	@Override
	protected ISourceViewer createSourceViewer(Composite parent,
			IVerticalRuler ruler, int styles) {
		return super.createSourceViewer(parent, ruler, styles);
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
					event.doit = false;
					return;
				}
			}
		});
	}
}
