// ICHANGED
package cx.fbn.nevernote.sql;

import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class ExcludedTable {
	private final ApplicationLogger logger;
	private final DatabaseConnection db;

	// コンストラクタ
	public ExcludedTable(ApplicationLogger l, DatabaseConnection d) {
		logger = l;
		db = d;
	}

	// テーブル作成
	public void createTable() {
		NSqlQuery query = new NSqlQuery(db.getBehaviorConnection());
		logger.log(logger.HIGH, "ExcludedNotesテーブルを作成しています...");
		if (!query.exec("Create table ExcludedNotes (id integer primary key auto_increment, guid1 varchar, guid2 varchar)"))
			logger.log(logger.HIGH, "ExcludedNotesテーブル作成失敗!!!");
	}

	// テーブルをドロップ
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getBehaviorConnection());
		query.exec("Drop table ExcludedNotes");
	}

	// ExcludedNotesテーブルにアイテムを1つ追加
	public void addExclusion(String guid1, String guid2) {
		NSqlQuery query = new NSqlQuery(db.getBehaviorConnection());
		query.prepare("Insert Into ExcludedNotes (guid1, guid2) Values(:guid1, :guid2)");
		query.bindValue(":guid1", guid1);
		query.bindValue(":guid2", guid2);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "ExcludedNotesテーブルへのアイテム追加に失敗");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	
	// masterGuidとchildGuidをマージ
	public void mergeHistoryGuid(String masterGuid, String childGuid) {
		NSqlQuery excludedNotesQuery = new NSqlQuery(db.getBehaviorConnection());
		boolean check = false;
		
		// マージ後に重複してしまうデータを先に削除
		excludedNotesQuery.prepare("Delete from ExcludedNotes where (guid1=:oldGuid1 and guid2=:newGuid1) or (guid1=:newGuid2 and guid2=:oldGuid2)");
		excludedNotesQuery.bindValue(":oldGuid1", masterGuid);
		excludedNotesQuery.bindValue(":newGuid1", childGuid);
		excludedNotesQuery.bindValue(":oldGuid2", masterGuid);
		excludedNotesQuery.bindValue(":newGuid2", childGuid);
		check = excludedNotesQuery.exec();
		if(!check){
			logger.log(logger.MEDIUM, "excludedNotesテーブルの重複削除で失敗");
			logger.log(logger.MEDIUM, excludedNotesQuery.lastError());
		}
		
		updateExcludedNoteGuid(masterGuid, childGuid);
	}
	
	// ExcludedNotesテーブルのGuidを更新
	public void updateExcludedNoteGuid(String newGuid, String oldGuid){
		NSqlQuery excludedNotesQuery = new NSqlQuery(db.getBehaviorConnection());
		boolean check = false;
		
		excludedNotesQuery.prepare("Update ExcludedNotes set guid1=:newGuid where guid1=:oldGuid");
		excludedNotesQuery.bindValue(":newGuid", newGuid);
		excludedNotesQuery.bindValue(":oldGuid", oldGuid);
		check = excludedNotesQuery.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "ExcludedNotesテーブルのguid1のところでguid更新失敗");
			logger.log(logger.MEDIUM, excludedNotesQuery.lastError());
		}
		excludedNotesQuery.prepare("Update ExcludedNotes set guid2=:newGuid where guid2=:oldGuid");
		excludedNotesQuery.bindValue(":newGuid", newGuid);
		excludedNotesQuery.bindValue(":oldGuid", oldGuid);
		check = excludedNotesQuery.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "ExcludedNotesテーブルのguid2のところでguid更新失敗");
			logger.log(logger.MEDIUM, excludedNotesQuery.lastError());
		}
	}

	// ExcludedNotesテーブルに引数guidのノートが存在するか
	public boolean existNote(String guid1, String guid2) {
		NSqlQuery excludedNotesQuery = new NSqlQuery(db.getBehaviorConnection());

		// 2つの引数guidを含むアイテムの存在確認
		excludedNotesQuery.prepare("Select * from ExcludedNotes where Exists(Select * from ExcludedNotes where (guid1=:guid1_1 and guid2=:guid2_1) or (guid1=:guid2_2 and guid2=:guid1_2))");
		excludedNotesQuery.bindValue(":guid1_1", guid1);
		excludedNotesQuery.bindValue(":guid2_1", guid2);
		excludedNotesQuery.bindValue(":guid1_2", guid1);
		excludedNotesQuery.bindValue(":guid2_2", guid2);
		
		if (!excludedNotesQuery.exec()) {
			logger.log(logger.MEDIUM, "ExcludedNotesテーブルからguid1=" + guid1 + "かつguid2=" + guid2 + "（またはその逆）のアイテムの存在確認失敗");
			logger.log(logger.MEDIUM, excludedNotesQuery.lastError());
		}
		
		if (excludedNotesQuery.next()) {
			return true;
		}
		
		return false;
	}
	
	// oldGuidのノートの除外ノートをnewGuidのノートの除外ノートとして複製
	public void duplicateExcludedNotes(String newGuid, String oldGuid) {
		NSqlQuery excludedNotesQuery = new NSqlQuery(db.getBehaviorConnection());

		// guid1 = oldGuidの除外ノートを取得
		excludedNotesQuery.prepare("Select guid2 from ExcludedNotes where guid1=:oldGuid");
		excludedNotesQuery.bindValue(":oldGuid", oldGuid);
		if(!excludedNotesQuery.exec()){
			logger.log(logger.MEDIUM, "ExcludedNotesテーブルからguid1=" + oldGuid + "のアイテム取得失敗");
			logger.log(logger.MEDIUM, excludedNotesQuery.lastError());
		}
		// guid1 = newGuidの除外ノートとして複製
		while(excludedNotesQuery.next()){
			String guid2 = excludedNotesQuery.valueString(0);
			
			addExclusion(newGuid, guid2);
		}
		
		// guid2 = oldGuidの除外ノートを取得
		excludedNotesQuery.prepare("Select guid1 from ExcludedNotes where guid2=:oldGuid");
		excludedNotesQuery.bindValue(":oldGuid", oldGuid);
		if(!excludedNotesQuery.exec()){
			logger.log(logger.MEDIUM, "ExcludedNotesテーブルからguid2=" + oldGuid + "のアイテム取得失敗");
			logger.log(logger.MEDIUM,  excludedNotesQuery.lastError());
		}
		// guid2 = newGuidの除外ノートとして複製
		while(excludedNotesQuery.next()){
			String guid1 = excludedNotesQuery.valueString(0);
			
			addExclusion(guid1, newGuid);
		}
	}

	// guidを含む列をExcludedNotesテーブルから削除
	public void expungeExcludedNote(String guid) {
		NSqlQuery query = new NSqlQuery(db.getBehaviorConnection());
		boolean check;
		
		query.prepare("Delete from ExcludedNotes where guid1=:guid1 or guid2=:guid2");
		query.bindValue(":guid1", guid);
		query.bindValue(":guid2", guid);
		
		check = query.exec();
		if(!check){
			logger.log(logger.MEDIUM, "ExcludedNotesテーブルからguid=" + guid + "のデータ削除に失敗");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
}
