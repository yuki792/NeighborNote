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
import java.util.List;

import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QActionGroup;
import com.trolltech.qt.gui.QCursor;
import com.trolltech.qt.gui.QFocusEvent;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QStyle.PixelMetric;
import com.trolltech.qt.gui.QToolButton;

import cx.fbn.nevernote.sql.DatabaseConnection;

public class SearchEdit extends QLineEdit {
	private final SearchClearButton clearButton;
	private final QToolButton targetButton;
	private final String inactiveColor;
	private final String activeColor;
	private String defaultText;
	private final QMenu targetMenu;
	private final QAction allNotesAction;
	private final QAction currentContextAction;
	private String targetStackName;
	private String targetNotebookGuid;
	private final List<String> targetTagGuids;
	private final DatabaseConnection conn;
	
	public enum SearchTarget {AllNotes, CurrentContext};

	
	public SearchEdit(String iconPath, DatabaseConnection conn) {
		this.conn = conn;
		
		inactiveColor = new String("QLineEdit {color: gray; font:italic;} ");
		activeColor = new String("QLineEdit {color: black; font:normal;} ");
		
		targetStackName = new String("");
		targetNotebookGuid = new String("");
		targetTagGuids = new ArrayList<String>();
		
		this.clearButton = new SearchClearButton(this, iconPath);
		this.clearButton.clicked.connect(this, "clear()");
		
		this.targetButton = new QToolButton(this);
		QPixmap targetIcon = new QPixmap(iconPath + "searchTarget.png");
		this.targetButton.setIcon(new QIcon(targetIcon));
		this.targetButton.setIconSize(new QSize(24, 16));
		this.targetButton.setCursor(new QCursor(Qt.CursorShape.ArrowCursor));
		this.targetButton.setStyleSheet("QToolButton { background-color: #ffffff; padding: 0px; }");
		this.targetButton.hide();
		this.targetButton.clicked.connect(this, "targetButtonClicked()");
		
		int frameWidth = this.style().pixelMetric(PixelMetric.PM_DefaultFrameWidth);
		this.setStyleSheet("QLineEdit { padding-right: " + (clearButton.sizeHint().width() + frameWidth + 1) + "px; } ");
		this.textChanged.connect(this, "updateClearButton(String)");
		
		// 検索対象切り替えメニュー
		targetMenu = new QMenu(this);
		QActionGroup targetGroup = new QActionGroup(this);
		targetGroup.setExclusive(true);
		allNotesAction = addContextAction(tr("Search All Notes"));
		targetGroup.addAction(allNotesAction);
		targetMenu.addAction(allNotesAction);
		currentContextAction = addContextAction(tr("Search Current Context"));
		targetGroup.addAction(currentContextAction);
		targetMenu.addAction(currentContextAction);
		targetGroup.triggered.connect(this, "toggleSearchTarget(QAction)");
		
		// 初期状態として「すべてのノートを検索」を選択
		allNotesAction.setChecked(true);
		toggleSearchTarget(allNotesAction);
	}
	
	private void toggleSearchTarget(QAction action) {
		if (action == allNotesAction) {
			defaultText = new String(tr("Search All Notes"));
		} else if (action == currentContextAction) {
			defaultText = new String(tr("Search Current Context"));
		}
    	setDefaultText();
    	this.clearFocus();
	}
	
	public SearchTarget currentSearchTarget() {
		if (allNotesAction.isChecked()) {
			return SearchTarget.AllNotes;
		} else if (currentContextAction.isChecked()) {
			return SearchTarget.CurrentContext;
		}
		
		return SearchTarget.AllNotes;	// デフォルト
	}
	
	@Override
	protected void resizeEvent(QResizeEvent event) {
		int frameWidth = style().pixelMetric(PixelMetric.PM_DefaultFrameWidth);
		
		QSize clearSize = clearButton.sizeHint();
		clearButton.move(rect().right() - frameWidth - clearSize.width(), (rect().bottom() + 1 - clearSize.height()) / 2);
		
		setTextMargins(targetButton.sizeHint().width(), 0, clearSize.width() + frameWidth, 0);
		targetButton.setFixedHeight(rect().bottom() + 1);
		targetButton.move(0,0);
		targetButton.setVisible(true);
	}
	
	@SuppressWarnings("unused")
	private void updateClearButton(String text) {
		clearButton.setVisible(!text.isEmpty());
	}
	
	public void setDefaultText() {
		if (text().trim().equals("")) {
			blockSignals(true);
			setText(defaultText);
			setStyleSheet(inactiveColor);
			blockSignals(false);
		}
	}
	
	@Override
	protected void focusInEvent(QFocusEvent event) {
		super.focusInEvent(event);
		if (text().equals(defaultText)) {
			blockSignals(true);
			setText("");
			blockSignals(false);
		}
		this.setStyleSheet(activeColor);
	}
	
	@Override
	protected void focusOutEvent(QFocusEvent event) {
		super.focusOutEvent(event);
		if (text().trim().equals("")) {
			blockSignals(true);
			setText(defaultText);
			blockSignals(false);
			setStyleSheet(inactiveColor);
		}
	}
	
	@SuppressWarnings("unused")
	private void targetButtonClicked() {
		QPoint globalPos = this.mapToGlobal(new QPoint(0, 0));
		targetMenu.popup(new QPoint(globalPos.x(), globalPos.y() + rect().bottom() + 1));
	}
	
    private QAction addContextAction(String name) {
    	QAction newAction = new QAction(this);
		newAction.setText(name);
		newAction.setCheckable(true);
		return newAction;
    }
    
    public String getSearchQuery() {
    	if (currentSearchTarget() == SearchTarget.AllNotes) {
    		return this.text();
    	} else if (currentSearchTarget() == SearchTarget.CurrentContext) {
    		return appendSearchQuery();
    	}
    	
    	return this.text();
    }
    
	private String appendSearchQuery() {
		StringBuilder query = new StringBuilder();
		
    	String stack = getTargetStack();
    	if (!stack.trim().equals("")) {
    		query.append("stack:" + stack.trim() + " ");
    	}
    	String notebook = getTargetNotebook();
    	if (!notebook.trim().equals("")) {
    		String notebookName = conn.getNotebookTable().getNotebook(notebook).getName();
    		query.append("notebook:" + notebookName + " ");
    	}
    	for (String tag: getTargetTags()) {
    		if (!tag.trim().equals("")) {
    			String tagName = conn.getTagTable().getTag(tag).getName();
    			query.append("tag:" + tagName + " ");
    		}
    	}
    	query.append(this.text());
    	
    	return new String(query);
    }

	public String getTargetStack() {
		return targetStackName;
	}
	
	public void setTargetStack(String targetStack) {
		this.targetStackName = new String(targetStack);
	}

	public String getTargetNotebook() {
		return targetNotebookGuid;
	}
	
	public void setTargetNotebook(String targetNotebook) {
		this.targetNotebookGuid = new String(targetNotebook);
	}

	public List<String> getTargetTags() {
		return targetTagGuids;
	}
	
	public void addTargetTag(String targetTag) {
		this.targetTagGuids.add(targetTag);
	}
}
