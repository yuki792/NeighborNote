// ICHANGED
package cx.fbn.nevernote.gui;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QKeyEvent;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QMouseEvent;

public class NoteTableContextMenu extends QMenu {
	private final TableView parent;
	
	public NoteTableContextMenu(TableView tableView) {
		this.parent = tableView;
	}

	
	@Override
	protected void mousePressEvent(QMouseEvent event){
		super.mousePressEvent(event);

		int x = event.x();
		int y = event.y();

		if(x < 0 || this.width() < x){
			parent.restoreSelectedNoteInfo();
		}else if(y < 0 || this.height() < y){
			parent.restoreSelectedNoteInfo();
		}
	}
	
	// ノートテーブルでマウス右ボタンを押してコンテキストメニューを出し、そのままコンテキストメニュー上を通過してコンテキストメニュー外でボタンを離すと
	// コンテキストメニューが閉じてしまう問題への対処
	@Override
	protected void mouseReleaseEvent(QMouseEvent event){
		super.mouseReleaseEvent(event);

		int x = event.x();
		int y = event.y();

		if(x < 0 || this.width() < x){
			parent.restoreSelectedNoteInfo();
		}else if(y < 0 || this.height() < y){
			parent.restoreSelectedNoteInfo();
		}
	}
	
	@Override
	protected void keyPressEvent(QKeyEvent event){
		super.keyPressEvent(event);

		if(event.key() == Qt.Key.Key_Escape.value()){
			parent.restoreSelectedNoteInfo();
		}
	}
}
