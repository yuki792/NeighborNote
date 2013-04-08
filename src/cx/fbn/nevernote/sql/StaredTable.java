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

package cx.fbn.nevernote.sql;

import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class StaredTable {
	private final ApplicationLogger logger;
	private final DatabaseConnection db;

	// コンストラクタ
	public StaredTable(ApplicationLogger l, DatabaseConnection d) {
		logger = l;
		db = d;
	}

	// テーブル作成
	public void createTable() {
		NSqlQuery query = new NSqlQuery(db.getBehaviorConnection());
		logger.log(logger.HIGH, "StaredNotesテーブルを作成しています...");
		if (!query.exec("Create table StaredNotes (id integer primary key auto_increment, masterGuid varchar, staredGuid varchar)"))
			logger.log(logger.HIGH, "StaredNotesテーブル作成失敗!!!");
	}

	// テーブルをドロップ
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getBehaviorConnection());
		query.exec("Drop table StaredNotes");
	}

	// StaredNotesテーブルにアイテムを1つ追加
	public void addStaredItem(String masterGuid, String staredGuid) {
		NSqlQuery query = new NSqlQuery(db.getBehaviorConnection());
		query.prepare("Insert Into StaredNotes (masterGuid, staredGuid) Values(:masterGuid, :staredGuid)");
		query.bindValue(":masterGuid", masterGuid);
		query.bindValue(":staredGuid", staredGuid);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "StaredNotesテーブルへのアイテム追加に失敗");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	
	// StaredNotesテーブルからアイテムを1つ削除
	public void removeStaredItem(String masterGuid, String staredGuid) {
		NSqlQuery query = new NSqlQuery(db.getBehaviorConnection());
		query.prepare("Delete from StaredNotes where (masterGuid=:masterGuid and staredGuid=:staredGuid)");
		query.bindValue(":masterGuid", masterGuid);
		query.bindValue(":staredGuid", staredGuid);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "StaredNotesテーブルからのアイテム削除に失敗");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	
	// guidを含む列をStaredNotesテーブルから削除
	public void expungeStaredNote(String guid) {
		NSqlQuery query = new NSqlQuery(db.getBehaviorConnection());
		boolean check;
		
		query.prepare("Delete from StaredNotes where masterGuid=:masterGuid or staredGuid=:staredGuid");
		query.bindValue(":masterGuid", guid);
		query.bindValue(":staredGuid", guid);
		
		check = query.exec();
		if(!check){
			logger.log(logger.MEDIUM, "StaredNotesテーブルからguid=" + guid + "のデータ削除に失敗");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	
	// masterGuidとchildGuidをマージ
	public void mergeHistoryGuid(String masterGuid, String childGuid) {
		NSqlQuery staredNotesQuery = new NSqlQuery(db.getBehaviorConnection());
		boolean check = false;
		
		// マージ後に重複してしまうデータを先に削除
		staredNotesQuery.prepare("Delete from StaredNotes where (masterGuid=:masterGuid1 and staredGuid=:staredGuid1) or (masterGuid=:masterGuid2 and staredGuid=:staredGuid2)");
		staredNotesQuery.bindValue(":masterGuid1", masterGuid);
		staredNotesQuery.bindValue(":childGuid1", childGuid);
		staredNotesQuery.bindValue(":masterGuid2", childGuid);
		staredNotesQuery.bindValue(":staredGuid2", masterGuid);
		check = staredNotesQuery.exec();
		if(!check){
			logger.log(logger.MEDIUM, "staredNotesテーブルの重複削除で失敗");
			logger.log(logger.MEDIUM, staredNotesQuery.lastError());
		}
		
		updateStaredNoteGuid(masterGuid, childGuid);
	}
	
	// StaredNotesテーブルのGuidを更新
	public void updateStaredNoteGuid(String newGuid, String oldGuid){
		NSqlQuery staredNotesQuery = new NSqlQuery(db.getBehaviorConnection());
		boolean check = false;
		
		staredNotesQuery.prepare("Update StaredNotes set masterGuid=:newGuid where masterGuid=:oldGuid");
		staredNotesQuery.bindValue(":newGuid", newGuid);
		staredNotesQuery.bindValue(":oldGuid", oldGuid);
		check = staredNotesQuery.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "StaredNotesテーブルのmasterGuidのところでguid更新失敗");
			logger.log(logger.MEDIUM, staredNotesQuery.lastError());
		}
		staredNotesQuery.prepare("Update StaredNotes set staredGuid=:newGuid where staredGuid=:oldGuid");
		staredNotesQuery.bindValue(":newGuid", newGuid);
		staredNotesQuery.bindValue(":oldGuid", oldGuid);
		check = staredNotesQuery.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "StaredNotesテーブルのstaredGuidのところでguid更新失敗");
			logger.log(logger.MEDIUM, staredNotesQuery.lastError());
		}
	}

	// StaredNotesテーブルに引数guidのノートが存在するか
	public boolean existNote(String masterGuid, String staredGuid) {
		NSqlQuery staredNotesQuery = new NSqlQuery(db.getBehaviorConnection());

		// 2つの引数guidを含むアイテムの存在確認
		staredNotesQuery.prepare("Select * from StaredNotes where Exists(Select * from StaredNotes where (masterGuid=:masterGuid1 and staredGuid=:staredGuid1) or (masterGuid=:masterGuid2 and staredGuid=:staredGuid2))");
		staredNotesQuery.bindValue(":masterGuid1", masterGuid);
		staredNotesQuery.bindValue(":staredGuid1", staredGuid);
		staredNotesQuery.bindValue(":masterGuid2", masterGuid);
		staredNotesQuery.bindValue(":staredGuid2", staredGuid);
		
		if (!staredNotesQuery.exec()) {
			logger.log(logger.MEDIUM, "StaredNotesテーブルからmasterGuid=" + masterGuid + "かつstaredGuid=" + staredGuid + "（またはその逆）のアイテムの存在確認失敗");
			logger.log(logger.MEDIUM, staredNotesQuery.lastError());
		}
		
		if (staredNotesQuery.next()) {
			return true;
		}
		
		return false;
	}
	
	// oldGuidのノートの除外ノートをnewGuidのノートの除外ノートとして複製
	public void duplicateStaredNotes(String newGuid, String oldGuid) {
		NSqlQuery staredNotesQuery = new NSqlQuery(db.getBehaviorConnection());

		// masterGuid = oldGuidのスター付きノートを取得
		staredNotesQuery.prepare("Select staredGuid from StaredNotes where masterGuid=:oldGuid");
		staredNotesQuery.bindValue(":oldGuid", oldGuid);
		if(!staredNotesQuery.exec()){
			logger.log(logger.MEDIUM, "StaredNotesテーブルからmasterGuid=" + oldGuid + "のアイテム取得失敗");
			logger.log(logger.MEDIUM, staredNotesQuery.lastError());
		}
		// masterGuid = newGuidのスター付きノートとして複製
		while(staredNotesQuery.next()){
			String staredGuid = staredNotesQuery.valueString(0);
			
			addStaredItem(newGuid, staredGuid);
		}
		
		// staredGuid = oldGuidの除外ノートを取得
		staredNotesQuery.prepare("Select masterGuid from StaredNotes where staredGuid=:oldGuid");
		staredNotesQuery.bindValue(":oldGuid", oldGuid);
		if(!staredNotesQuery.exec()){
			logger.log(logger.MEDIUM, "StaredNotesテーブルからstaredGuid=" + oldGuid + "のアイテム取得失敗");
			logger.log(logger.MEDIUM,  staredNotesQuery.lastError());
		}
		// staredGuid = newGuidの除外ノートとして複製
		while(staredNotesQuery.next()){
			String masterGuid = staredNotesQuery.valueString(0);
			
			addStaredItem(masterGuid, newGuid);
		}
	}
}
