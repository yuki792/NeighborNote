package cx.fbn.nevernote.neighbornote;

import com.trolltech.qt.gui.QApplication;

public class ClipBoardObserver {
	private String SourceGuid;
	private boolean internalFlg;	// アプリケーション内コピーかどうかのフラグ
	
	public ClipBoardObserver() {
		SourceGuid = new String("");
		internalFlg = false;
		QApplication.clipboard().dataChanged.connect(this, "clipboardDataChanged()");
	}
	
	public void setCopySourceGuid(String guid, String text) {		
		if (!text.equals("")) {
			SourceGuid = guid;
			internalFlg = true;
		}
	}
	
	public String getSourceGuid() {
		if (SourceGuid.equals("")) {
			return null;
		}
		return SourceGuid;
	}
	
	@SuppressWarnings("unused")
	private void clipboardDataChanged() {
		// 外部アプリケーションでコピー・カットが行われた時のための対処
		if (!internalFlg) {
			SourceGuid = "";
		}
		internalFlg = false;
	}
}
