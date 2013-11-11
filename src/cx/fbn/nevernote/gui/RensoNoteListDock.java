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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDockWidget;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
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
	private final QComboBox				weightingModeSelect;	// 連想ノートリストの重み付けプリセットモード
	private final RensoNoteList			rensoNoteList;			// 連想ノートリスト
	private final QToolBar				rensoToolBar;			// 連想ノートリストドックのツールバー
	private enum PresetMode {Standard, ConcurrentBrowse, OperationToOrganize, ContentSimilarity, Custom};	// プリセットモード一覧
	
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
		
		QLabel modeLabel = new QLabel(tr("Mode: "));
		modeLabel.setToolTip(tr("Preset Weighting Mode"));
		rensoToolBar.addWidget(modeLabel);
		
		weightingModeSelect = new QComboBox();
		weightingModeSelect.setMaximumWidth(150);
		weightingModeSelect.setToolTip(tr("Preset Weighting Mode"));
		weightingModeSelect.insertItem(PresetMode.Standard.ordinal(), tr("Standard"));
		weightingModeSelect.insertItem(PresetMode.ConcurrentBrowse.ordinal(), tr("Concurrent Browse"));
		weightingModeSelect.insertItem(PresetMode.OperationToOrganize.ordinal(), tr("Operation to Organize"));
		weightingModeSelect.insertItem(PresetMode.ContentSimilarity.ordinal(), tr("Content Similarity"));
		weightingModeSelect.insertItem(PresetMode.Custom.ordinal(), tr("Custom"));
		int savedModeIndex = 0;
		try {
			savedModeIndex = PresetMode.valueOf(Global.rensoWeightingSelect()).ordinal();
		} catch (Exception e) {
			this.logger.log(this.logger.EXTREME, "Exception in weightingModeSelect = " + e);
		}
		if (savedModeIndex >= 0) {
			weightingModeSelect.setCurrentIndex(savedModeIndex);
		} else {
			weightingModeSelect.setCurrentIndex(0);
		}
		weightingModeSelect.currentIndexChanged.connect(this, "weightingModeChanged(int)");
		rensoToolBar.addWidget(weightingModeSelect);
		
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
	private void weightingModeChanged(int modeIndex) {
		logger.log(logger.EXTREME, "RensoNoteListDock.weightingModeChanged modeIndex = " + modeIndex);
		
		PresetMode mode = Global.fromOrdinal(PresetMode.class, modeIndex);
		
		// プリセットの重み付けマップを用意
		// 値は同時閲覧、コピペ、新規ノート追加、連想ノートクリック、同じタグ、同じノートブック、Evernote関連ノートの順
		Map<PresetMode, List<Integer>> presetMap = new EnumMap<PresetMode, List<Integer>>(PresetMode.class);
		presetMap.put(PresetMode.Standard, Arrays.asList(1, 3, 3, 10, 2, 2, 5));
		presetMap.put(PresetMode.ConcurrentBrowse, Arrays.asList(5, 2, 3, 10, 1, 1, 2));
		presetMap.put(PresetMode.OperationToOrganize, Arrays.asList(1, 2, 3, 10, 5, 5, 2));
		presetMap.put(PresetMode.ContentSimilarity, Arrays.asList(1, 2, 2, 10, 1, 1, 10));
		presetMap.put(PresetMode.Custom, Arrays.asList(Global.customBrowseWeight(), Global.customCopyPasteWeight(), 
				Global.customAddNewNoteWeight(), Global.customRensoItemClickWeight(), Global.customSameTagWeight(), 
				Global.customSameNotebookWeight(), Global.customENRelatedNotesWeight()));
		
		List<Integer> weightList = new ArrayList<Integer>(presetMap.get(mode));
		Global.setBrowseWeight(weightList.get(0));
		Global.setCopyPasteWeight(weightList.get(1));
		Global.setAddNewNoteWeight(weightList.get(2));
		Global.setRensoItemClickWeight(weightList.get(3));
		Global.setSameTagWeight(weightList.get(4));
		Global.setSameNotebookWeight(weightList.get(5));
		Global.setENRelatedNotesWeight(weightList.get(6));
		
		Global.saveRensoWeightingSelect(mode.name());
		
		// 連想ノートリストをリフレッシュ
		if (rensoNoteList != null && rensoNoteList.getGuid() != null ) {
			rensoNoteList.refreshRensoNoteList();
			
			// カスタムモードかつアプリ起動時以外なら、メッセージ表示
			if (mode == PresetMode.Custom) {
				QMessageBox.information(this, tr("Information"), tr("Custom mode was selected.\nYou can customize weighting manually in the Edit/Preferences/Renso Note List."));
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void haltLogToggled(boolean checked) {
		logger.log(logger.EXTREME, "RensoNoteListDock.haltLogToggled");
		
		if (checked) {
			QMessageBox.information(this, tr("Information"), tr("Collecting operation log halted.\nYou can resume, if you press this button again."));
		}
		Global.saveHaltLogButton(checked);
	}

	public RensoNoteList getRensoNoteList() {
		return rensoNoteList;
	}
	
}
