package sample.xml.editor.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;


public class CustomEditorSupport {
	
	private XMLEditor editor = null;
	
	public static final String START_MARK = "<!--auto-generate-code-start-->";
	public static final String END_MARK ="<!--auto-generate-code-end-->";

	public CustomEditorSupport(XMLEditor editor) {
		this.editor = editor;
	}
	
	/**
	 * Return starting offset and length of read-only section in currently opened XML document.
	 * @return
	 */
	private Position getReadOnlySection(){
		try {
			IDocument doc = editor.getDocument();
			int lineCount = doc.getNumberOfLines();
			int startOffset = -1, length = 0;
			/*It is assumed that there will be only one read-only block present (for simplicity)*/
			for(int i=0;i<lineCount; i++){
				int lineStartOffset = doc.getLineOffset(i);
				int lineLength = doc.getLineLength(i);
				String lineText = doc.get(lineStartOffset, lineLength);
				if(lineText!=null && lineText.startsWith(START_MARK)){
					startOffset = lineStartOffset;
				} else if (lineText!= null && lineText.startsWith(END_MARK)){
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
	
	/**
	 * Return true or false based on the parameter offset position.
	 * @param offset
	 * @param length
	 * @return
	 */
	public boolean isInsideReadOnlyBlock(int offset, int length){
		Position readOnlyBLock = getReadOnlySection();
		if(readOnlyBLock != null && readOnlyBLock.overlapsWith(offset, length)){
			return true;
		}
		return false;
	}
}
