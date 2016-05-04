package com.part.readonly.editor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;


public class CustomEditorSupport {
	
	private CustomEditor editor = null;
	
	public static final String START_MARK = "<auto-generate-code-start>";
	public static final String END_MARK ="<auto-generate-code-end>";

	public CustomEditorSupport(CustomEditor editor) {
		this.editor = editor;
	}
	
	private Position getReadOnlySection(){
		try {
			IDocument doc = editor.getDocument();
			int lineCount = doc.getNumberOfLines();
			int startOffset = -1, length = 0;
			/*It is asumed that there will be only one read-only block present (for simplicity)*/
			for(int i=0;i<lineCount; i++){
				int lineStartOffset = doc.getLineOffset(i);
				int lineLength = doc.getLineLength(i);
				String lineText = doc.get(lineStartOffset, lineLength);
				if(START_MARK.equals(lineText)){
					startOffset = lineStartOffset;
				} else if (END_MARK.equals(lineText)){
					length = (lineStartOffset + lineLength) - startOffset;
					break;
				}
			}
			if(startOffset != -1 && length >0){
				return new Position(startOffset,length);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean isInsideReadOnlyBlock(int offset, int length){
		Position readOnlyBLock = getReadOnlySection();
		if(readOnlyBLock.overlapsWith(offset, length)){
			return true;
		}
		return false;
	}
}
