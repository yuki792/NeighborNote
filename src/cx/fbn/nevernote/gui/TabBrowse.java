// ICHANGED
package cx.fbn.nevernote.gui;

import java.awt.Desktop;

import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.gui.QDesktopServices;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QPrintDialog;
import com.trolltech.qt.gui.QPrinter;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.dialog.FindDialog;
import cx.fbn.nevernote.neighbornote.ClipBoardObserver;
import cx.fbn.nevernote.sql.DatabaseConnection;

public class TabBrowse extends QWidget {
	private final DatabaseConnection conn;
	private final BrowserWindow browser;
	public Signal4<String, String, Boolean, BrowserWindow> contentsChanged;
	private boolean noteDirty;
	String saveTitle;
	private final FindDialog find; // Text search in note dialog
	private final TabBrowserWidget parent;
	private final ClipBoardObserver cbObserver;

	// コンストラクタ
	public TabBrowse(DatabaseConnection c, TabBrowserWidget p, ClipBoardObserver cbObserver) {
		conn = c;
		parent = p;
		this.cbObserver = cbObserver;
		contentsChanged = new Signal4<String, String, Boolean, BrowserWindow>();
		browser = new BrowserWindow(conn, this.cbObserver);
		QVBoxLayout v = new QVBoxLayout();
		v.addWidget(browser);
		setLayout(v);
		noteDirty = false;
		browser.titleLabel.textChanged.connect(this, "titleChanged(String)");
		browser.getBrowser().page().contentsChanged.connect(this,
				"contentChanged()");
		find = new FindDialog();
		find.getOkButton().clicked.connect(this, "doFindText()");
	}

	@SuppressWarnings("unused")
	private void contentChanged() {
		noteDirty = true;
		contentsChanged.emit(getBrowserWindow().getNote().getGuid(),
				getBrowserWindow().getContent(), false, getBrowserWindow());
	}

	public BrowserWindow getBrowserWindow() {
		return browser;
	}

	
	@SuppressWarnings("unused")
	private void titleChanged(String value) {
		int index = parent.indexOf(this);
		if(index >= 0){
			parent.setTabTitle(index, value);
		}
	}
	
	/*
	@SuppressWarnings("unused")
	private void updateTitle(String guid, String title) {
		if (guid.equals(getBrowserWindow().getNote().getGuid())
				&& (saveTitle != null && !title.equals(saveTitle) || saveTitle == null)) {
			saveTitle = title;
			getBrowserWindow().loadingData(true);
			getBrowserWindow().setTitle(title);
			getBrowserWindow().getNote().setTitle(title);
			getBrowserWindow().loadingData(false);
		}
	}
	*/

	/*
	@SuppressWarnings("unused")
	private void updateNotebook(String guid, String notebook) {
		if (guid.equals(getBrowserWindow().getNote().getGuid())) {
			getBrowserWindow().loadingData(true);
			getBrowserWindow().setNotebook(notebook);
			getBrowserWindow().loadingData(false);
		}
	}
	*/

	/*
	@SuppressWarnings("unused")
	private void updateTags(String guid, List<String> tags) {
		if (guid.equals(getBrowserWindow().getNote().getGuid())) {
			StringBuffer tagLine = new StringBuffer(100);
			for (int i = 0; i < tags.size(); i++) {
				if (i > 0)
					tagLine.append(Global.tagDelimeter + " ");
				tagLine.append(tags.get(i));

			}
			getBrowserWindow().loadingData(true);
			getBrowserWindow().getTagLine().setText(tagLine.toString());
			getBrowserWindow().loadingData(false);
		}
	}
	*/

	@SuppressWarnings("unused")
	private void findText() {
		find.show();
		find.setFocusOnTextField();
	}

	@SuppressWarnings("unused")
	private void doFindText() {
		browser.getBrowser().page().findText(find.getText(), find.getFlags());
		find.setFocus();
	}

	@SuppressWarnings("unused")
	private void printNote() {

		QPrintDialog dialog = new QPrintDialog();
		if (dialog.exec() == QDialog.DialogCode.Accepted.value()) {
			QPrinter printer = dialog.printer();
			browser.getBrowser().print(printer);
		}
	}

	// Listener triggered when the email button is pressed
	@SuppressWarnings("unused")
	private void emailNote() {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();

			String text2 = browser.getContentsToEmail();
			QUrl url = new QUrl("mailto:");
			url.addQueryItem("subject", browser.getTitle());
			url.addQueryItem("body", text2);
			QDesktopServices.openUrl(url);
		}
	}

	// noteDirtyの値を返す
	public boolean getNoteDirty() {
		return noteDirty;
	}
}
