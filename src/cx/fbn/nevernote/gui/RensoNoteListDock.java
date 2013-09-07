/*
 * This file is part of NeighborNote
 * Copyright 2013 Yuki Takahashi
 * 
 * This file may be licensed under the terms of of the
 * GNU General Public License Version 2 (the ``GPL'').
 *
 * Software distributed under the License is distributed
 * on an ``AS IS'' basis, WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the GPL for the specific language
 * governing rights and limitations.
 *
 * You should have received a copy of the GPL along with this
 * program. If not, go to http://www.gnu.org/licenses/gpl.html
 * or write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
*/

package cx.fbn.nevernote.gui;

import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QDockWidget;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QToolBar;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.NeverNote;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.threads.SyncRunner;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class RensoNoteListDock extends QDockWidget {
	private final DatabaseConnection	conn;
	private final NeverNote				parent;
	private final SyncRunner			syncRunner;
	private final ApplicationLogger		logger;
	private final QPushButton			haltLogButton;			// 操作ログの取得を停止ボタン
	private final RensoNoteList			rensoNoteList;			// 連想ノートリスト
	private final QToolBar				rensoToolBar;			// 連想ノートリストドックのツールバー
	
	private final String 				iconPath;
	
	public RensoNoteListDock(DatabaseConnection conn, NeverNote parent, SyncRunner syncRunner, String iconPath, String title) {
		super(title);
		
		this.logger = new ApplicationLogger("rensoNoteList.log");
		this.logger.log(this.logger.HIGH, "Setting up rensoNoteListDock");
		
		this.conn = conn;
		this.parent = parent;
		this.iconPath = iconPath;
		this.syncRunner = syncRunner;
		
		QVBoxLayout vLayout = new QVBoxLayout();
		rensoToolBar = new QToolBar();
		vLayout.addWidget(rensoToolBar);
		
		haltLogButton = new QPushButton();
		QIcon haltLogIcon = new QIcon(this.iconPath + "haltLog.png");
		haltLogButton.setIcon(haltLogIcon);
		haltLogButton.setIconSize(new QSize(24, 24));
		haltLogButton.setToolTip(tr("Halt Collectiong Operation Log"));
		haltLogButton.setCheckable(true);
		haltLogButton.setChecked(Global.isHaltLogButton());
		haltLogButton.toggled.connect(this, "haltLogToggled(boolean)");
		
		// ログ取得停止ボタンを右寄せするためのスペーサ
		QWidget spacer = new QWidget();
		spacer.setSizePolicy(QSizePolicy.Policy.Expanding, QSizePolicy.Policy.Expanding);
		rensoToolBar.addWidget(spacer);
		rensoToolBar.addWidget(haltLogButton);
		
		rensoNoteList = new RensoNoteList(this.conn, this.parent, this.syncRunner, this.logger);
		getRensoNoteList().itemPressed.connect(this.parent, "rensoNoteItemPressed(QListWidgetItem)");
		vLayout.addWidget(getRensoNoteList());

		QWidget w = new QWidget();
		w.setLayout(vLayout);
		this.setWidget(w);
	}
	
	@SuppressWarnings("unused")
	private void haltLogToggled(boolean checked) {
		logger.log(logger.EXTREME, "RensoNoteListDock.haltLogToggled");
		
		if (checked) {
			QMessageBox.information(this, tr("Halt Collectiong Operation Log"), tr("Collecting operation log halted.\nYou can resume, if you press this button again."));
		}
		Global.saveHaltLogButton(checked);
	}

	public RensoNoteList getRensoNoteList() {
		return rensoNoteList;
	}
}
