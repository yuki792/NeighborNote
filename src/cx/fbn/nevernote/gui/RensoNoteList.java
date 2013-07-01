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

// ICHANGED
package cx.fbn.nevernote.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.evernote.edam.type.Note;
import com.trolltech.qt.QThread;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt.MouseButton;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QContextMenuEvent;
import com.trolltech.qt.gui.QListWidget;
import com.trolltech.qt.gui.QListWidgetItem;
import com.trolltech.qt.gui.QMenu;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.NeverNote;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.threads.ENRelatedNotesRunner;
import cx.fbn.nevernote.threads.SyncRunner;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.utilities.Pair;

public class RensoNoteList extends QListWidget {
	private final DatabaseConnection conn;
	private final ApplicationLogger logger;
	private final HashMap<QListWidgetItem, String> rensoNoteListItems;
	private final List<RensoNoteListItem> rensoNoteListTrueItems;
	private String rensoNotePressedItemGuid;
	private final QAction openNewTabAction;
	private final QAction starAction;
	private final QAction unstarAction;
	private final QAction excludeNoteAction;
	private final NeverNote parent;
	private final QMenu menu;
	private HashMap<String, Integer> mergedHistory;				// マージされた操作履歴
	private final SyncRunner syncRunner;
	private final ENRelatedNotesRunner enRelatedNotesRunner;
	private final QThread enRelatedNotesThread;
	private final HashMap<String, List<String>> enRelatedNotesCache;	// Evernote関連ノートのキャッシュ<guid, 関連ノートリスト>
	private String guid;
	private int allPointSum;

	public RensoNoteList(DatabaseConnection c, NeverNote p, SyncRunner syncRunner) {
		logger = new ApplicationLogger("rensoNoteList.log");
		logger.log(logger.HIGH, "Setting up rensoNoteList");
		allPointSum = 0;

		this.conn = c;
		this.parent = p;
		this.syncRunner = syncRunner;
		
		this.guid = new String();
		mergedHistory = new HashMap<String, Integer>();
		enRelatedNotesCache = new HashMap<String, List<String>>();
		this.enRelatedNotesRunner = new ENRelatedNotesRunner(this.syncRunner);
		this.enRelatedNotesRunner.enRelatedNotesSignal.getENRelatedNotesFinished.connect(this, "enRelatedNotesComplete()");
		this.enRelatedNotesThread = new QThread(enRelatedNotesRunner, "ENRelatedNotes Thread");
		this.enRelatedNotesThread.start();
		
		rensoNoteListItems = new HashMap<QListWidgetItem, String>();
		rensoNoteListTrueItems = new ArrayList<RensoNoteListItem>();
		
		this.itemPressed.connect(this, "rensoNoteItemPressed(QListWidgetItem)");
		
		// コンテキストメニュー作成
		menu = new QMenu(this);
		// 新しいタブで開くアクション生成
		openNewTabAction = new QAction(tr("Open in New Tab"), this);
		openNewTabAction.setToolTip(tr("Open this note in new tab"));
		openNewTabAction.triggered.connect(parent, "openNewTabFromRNL()");
		// スターをつけるアクション生成
		starAction = new QAction(tr("Add Star"), this);
		starAction.setToolTip(tr("Add Star to this item"));
		starAction.triggered.connect(parent, "starNote()");
		// スターを外すアクション生成
		unstarAction = new QAction(tr("Remove Star"), this);
		unstarAction.setToolTip(tr("Remove Star from this item"));
		unstarAction.triggered.connect(parent, "unstarNote()");
		// このノートを除外するアクション生成
		excludeNoteAction = new QAction(tr("Exclude"), this);
		excludeNoteAction.setToolTip(tr("Exclude this note from RensoNoteList"));
		excludeNoteAction.triggered.connect(parent, "excludeNote()");
		// コンテキストメニューに登録
		menu.addAction(openNewTabAction);
		menu.addAction(excludeNoteAction);
		menu.aboutToHide.connect(this, "contextMenuHidden()");
		
		logger.log(logger.HIGH, "rensoNoteList setup complete");
	}

	// 連想ノートリストをリフレッシュ
	public void refreshRensoNoteList(String guid) {
		logger.log(logger.HIGH, "Entering RensoNoteList.refreshRensoNoteList");

		this.clear();
		rensoNoteListItems.clear();
		rensoNoteListTrueItems.clear();
		mergedHistory = new HashMap<String, Integer>();

		if (!this.isEnabled()) {
			return;
		}
		if (guid == null || guid.equals("")) {
			return;
		}
		
		this.guid = guid;
		// すでにEvernote関連ノートがキャッシュされているか確認
		boolean isCached;
		isCached = enRelatedNotesCache.containsKey(guid);
		if (!isCached) {	// キャッシュ無し
			// Evernoteの関連ノートを別スレッドで取得させる
			enRelatedNotesRunner.addGuid(guid);
		} else {			// キャッシュ有り
			List<String> relatedNoteGuids = enRelatedNotesCache.get(guid);
			addENRelatedNotes(relatedNoteGuids);
		}
		
		calculateHistory(guid);
		repaintRensoNoteList(false);

		logger.log(logger.HIGH, "Leaving RensoNoteList.refreshRensoNoteList");
	}
	
	// 操作履歴をデータベースから取得してノートごとの関連度を算出、その後mergedHistoryに追加
	private void calculateHistory(String guid) {
		// browseHistory<guid, 回数（ポイント）>
		HashMap<String, Integer> browseHistory = conn.getHistoryTable().getBehaviorHistory("browse", guid);
		addWeight(browseHistory, Global.getBrowseWeight());
		mergedHistory = mergeHistory(browseHistory, mergedHistory);
		
		// copy&pasteHistory<guid, 回数（ポイント）>
		HashMap<String, Integer> copyAndPasteHistory = conn.getHistoryTable().getBehaviorHistory("copy & paste", guid);
		addWeight(copyAndPasteHistory, Global.getCopyPasteWeight());
		mergedHistory = mergeHistory(copyAndPasteHistory, mergedHistory);
		
		// addNewNoteHistory<guid, 回数（ポイント）>
		HashMap<String, Integer> addNewNoteHistory = conn.getHistoryTable().getBehaviorHistory("addNewNote", guid);
		addWeight(addNewNoteHistory, Global.getAddNewNoteWeight());
		mergedHistory = mergeHistory(addNewNoteHistory, mergedHistory);
		
		// rensoItemClickHistory<guid, 回数（ポイント）>
		HashMap<String, Integer> rensoItemClickHistory = conn.getHistoryTable().getBehaviorHistory("rensoItemClick", guid);
		addWeight(rensoItemClickHistory, Global.getRensoItemClickWeight());
		mergedHistory = mergeHistory(rensoItemClickHistory, mergedHistory);
		
		// sameTagHistory<guid, 回数（ポイント）>
		HashMap<String, Integer> sameTagHistory = conn.getHistoryTable().getBehaviorHistory("sameTag", guid);
		addWeight(sameTagHistory, Global.getSameTagWeight());
		mergedHistory = mergeHistory(sameTagHistory, mergedHistory);
		
		// sameNotebookNoteHistory<guid, 回数（ポイント）>
		HashMap<String, Integer> sameNotebookHistory = conn.getHistoryTable().getBehaviorHistory("sameNotebook", guid);
		addWeight(sameNotebookHistory, Global.getSameNotebookWeight());
		mergedHistory = mergeHistory(sameNotebookHistory, mergedHistory);
	}
	
	// 操作回数に重み付けする
	private void addWeight(HashMap<String, Integer> history, int weight){
		Set<String> keySet = history.keySet();
		Iterator<String> hist_iterator = keySet.iterator();
		while(hist_iterator.hasNext()){
			String key = hist_iterator.next();
			history.put(key, history.get(key) * weight);
		}
	}
	
	// 連想ノートリストを再描画
	private void repaintRensoNoteList(boolean needClear) {
		if (needClear) {
			this.clear();
			rensoNoteListItems.clear();
			rensoNoteListTrueItems.clear();
		}
		
		if (!this.isEnabled()) {
			return;
		}
		
		// すべての関連ポイントの合計を取得（関連度のパーセント算出に利用）
		allPointSum = 0;
		for (int p : mergedHistory.values()) {
			allPointSum += p;
		}
		
		addRensoNoteList(mergedHistory);
	}
	
	// 引数1と引数2をマージしたハッシュマップを返す
	private HashMap<String, Integer> mergeHistory(HashMap<String, Integer> History1, HashMap<String, Integer> History2){
		HashMap<String, Integer> mergedHistory = new HashMap<String, Integer>();
		
		mergedHistory.putAll(History1);
		
		Set<String> keySet = History2.keySet();
		Iterator<String> hist2_iterator = keySet.iterator();
		while(hist2_iterator.hasNext()){
			String key = hist2_iterator.next();
			if(mergedHistory.containsKey(key)){
				mergedHistory.put(key, mergedHistory.get(key) + History2.get(key));
			}else {
				mergedHistory.put(key, History2.get(key));
			}
		}

		return mergedHistory;
	}
	
	// 連想ノートリストにハッシュマップのデータを追加
	private void addRensoNoteList(HashMap<String, Integer> History){
		String currentNoteGuid = new String(parent.getCurrentNoteGuid());
		
		// スター付きノートとスター無しノートを分ける
		HashMap<String, Integer> staredNotes = new HashMap<String, Integer>();	// スター付きノートのマップ
		HashMap<String, Integer> normalNotes = new HashMap<String, Integer>();	// スター無しノートのマップ
		for (String nextGuid: History.keySet()) {
			int relationPoint = History.get(nextGuid);
			boolean isStared = conn.getStaredTable().existNote(currentNoteGuid, nextGuid);
			if (isStared) {
				staredNotes.put(nextGuid, relationPoint);
			} else {
				normalNotes.put(nextGuid, relationPoint);
			}
		}
		
		// 連想ノートリストアイテムの最大表示数まで繰り返す
		for (int i = 0; i < Global.getRensoListItemMaximum(); i++) {
			// スター付きノートがあれば先に処理する
			HashMap<String, Integer> tmpMap = new HashMap<String, Integer>();
			if (!staredNotes.isEmpty()) {
				tmpMap = staredNotes;
			}else if (!normalNotes.isEmpty()) {
				tmpMap = normalNotes;
			}
			
			// 操作回数が多い順に取り出して連想ノートリストに追加する
			if (!tmpMap.isEmpty()) {
				int maxNum = -1;
				String maxGuid = new String();
				
				for (String nextGuid: tmpMap.keySet()) {
					int relationPoint = tmpMap.get(nextGuid);
					
					// 最大ノート探索する
					if (relationPoint > maxNum) {
						maxNum = relationPoint;
						maxGuid = nextGuid;
					}
				}
				
				// 次の最大値探索で邪魔なので最大値をHashMapから削除
				tmpMap.remove(maxGuid);
	
				// 関連度最大のノートがアクティブか確認
				Note maxNote = conn.getNoteTable().getNote(maxGuid, true, false, false, false, true);
				boolean isNoteActive = false;
				if(maxNote != null) {
					isNoteActive = maxNote.isActive();
				}
				
				// 存在していて、かつ関連度0でなければノート情報を取得して連想ノートリストに追加
				if (isNoteActive && maxNum > 0) {
					// スター付きか確認
					boolean isStared;
					isStared = conn.getStaredTable().existNote(currentNoteGuid, maxGuid);
					
					QListWidgetItem item = new QListWidgetItem();
					RensoNoteListItem myItem = new RensoNoteListItem(maxNote, maxNum, isStared, allPointSum, conn, this);
					item.setSizeHint(new QSize(0, 90));
					this.addItem(item);
					this.setItemWidget(item, myItem);
					rensoNoteListItems.put(item, maxGuid);
					rensoNoteListTrueItems.add(myItem);
				} else {
					break;
				}
			}
		}
	}

	// リストのアイテムから対象ノートのguidを取得
	public String getNoteGuid(QListWidgetItem item) {
		return rensoNoteListItems.get(item);
	}
	
	// 関連ノートリストの右クリックメニュー
	@Override
	public void contextMenuEvent(QContextMenuEvent event){
		if (rensoNotePressedItemGuid == null || rensoNotePressedItemGuid.equals("")) {
			return;
		}
		
		// STAR, UNSTARがあれば、一度消す
		List<QAction> menuActions = new ArrayList<QAction>(menu.actions());
		if (menuActions.contains(starAction)) {
			menu.removeAction(starAction);
		}
		if (menuActions.contains(unstarAction)) {
			menu.removeAction(unstarAction);
		}
		
		// 対象アイテムがスター付きなら「UNSTAR」、スター無しなら「STAR」を追加
		String currentNoteGuid = parent.getCurrentNoteGuid();
		boolean isExist = conn.getStaredTable().existNote(currentNoteGuid, rensoNotePressedItemGuid);
		if (isExist) {
			menu.insertAction(excludeNoteAction, unstarAction);
		} else {
			menu.insertAction(excludeNoteAction, starAction);
		}
		
		// コンテキストメニューを表示
		menu.exec(event.globalPos());
		
		rensoNotePressedItemGuid = null;
	}
	
	// コンテキストメニューが表示されているかどうか
	public boolean isContextMenuVisible() {
		return menu.isVisible();
	}
	
	// コンテキストメニューが閉じられた時
	@SuppressWarnings("unused")
	private void contextMenuHidden() {
		for (int i = 0; i < rensoNoteListTrueItems.size(); i++) {
			RensoNoteListItem item = rensoNoteListTrueItems.get(i);
			item.setDefaultBackground();
		}
	}
	
	// ユーザが連想ノートリストのアイテムを選択した時の処理
	@SuppressWarnings("unused")
	private void rensoNoteItemPressed(QListWidgetItem current) {
		rensoNotePressedItemGuid = null;
		// 右クリックだったときの処理
		if (QApplication.mouseButtons().isSet(MouseButton.RightButton)) {
			rensoNotePressedItemGuid = getNoteGuid(current);
		}
	}
	
	// Evernoteの関連ノートの取得が完了
	@SuppressWarnings("unused")
	private void enRelatedNotesComplete() {
		Pair<String, List<String>> enRelatedNoteGuidPair = enRelatedNotesRunner.getENRelatedNoteGuids();	// <元ノートguid, 関連ノートguidリスト>
		
		if (enRelatedNoteGuidPair == null) {
			return;
		}
		
		String sourceGuid = enRelatedNoteGuidPair.getFirst();
		List<String> enRelatedNoteGuids = enRelatedNoteGuidPair.getSecond();
		
		
		if (sourceGuid != null && !sourceGuid.equals("") && enRelatedNoteGuids != null) {	// Evernote関連ノートがnullでなければ
			// まずキャッシュに追加
			enRelatedNotesCache.put(sourceGuid, enRelatedNoteGuids);
			
			if (!enRelatedNoteGuids.isEmpty()) {	// Evernote関連ノートが存在していて
				if (sourceGuid.equals(this.guid)) {	// 取得したデータが今開いているノートの関連ノートなら
					// mergedHistoryにEvernote関連ノートを追加してから再描画
					addENRelatedNotes(enRelatedNoteGuids);
					repaintRensoNoteList(true);
				}
			}
		}
	}
	
	// Evernote関連ノートの関連度情報をmergedHistoryに追加
	private void addENRelatedNotes(List<String> relatedNoteGuids) {
		// Evernote関連ノート<guid, 関連ポイント>
		HashMap<String, Integer> enRelatedNotes = new HashMap<String, Integer>();
		
		for (String relatedGuid : relatedNoteGuids) {
			// TODO 重みをとりあえず10で固定。あとで設定できるようにする。
			enRelatedNotes.put(relatedGuid, 10);
		}
		
		mergedHistory = mergeHistory(enRelatedNotes, mergedHistory);
	}
	
	// Evernoteの関連ノート取得スレッドを終了させる
	public boolean stopThread() {
		if (enRelatedNotesRunner.addStop()) {
			return true;
		}
		return false;
	}
}
