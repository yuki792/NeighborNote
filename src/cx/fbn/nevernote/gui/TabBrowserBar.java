// ICHANGED
package cx.fbn.nevernote.gui;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QTabBar;

public class TabBrowserBar extends QTabBar {

	public TabBrowserBar(){
		super();
	}

	public void addNewTab(int index, String title){
		QLabel label = new QLabel(title);
		label.setAlignment(Qt.AlignmentFlag.AlignLeft);
		int tabWidth = this.tabSizeHint(index).width();
		label.setFixedWidth(tabWidth - 35);
		this.setTabButton(index, QTabBar.ButtonPosition.LeftSide, label);
	}

	public void setTabTitle(int index, String title) {
		QLabel label = (QLabel)this.tabButton(index, QTabBar.ButtonPosition.LeftSide);
		label.setText(title);
	}
	
}
