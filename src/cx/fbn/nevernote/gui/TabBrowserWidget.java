// ICHANGED
package cx.fbn.nevernote.gui;

import com.trolltech.qt.gui.QTabWidget;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.NeverNote;

public class TabBrowserWidget extends QTabWidget {
	private final TabBrowserBar bar;
	private final NeverNote parent;
	
	public TabBrowserWidget(NeverNote parent) {
		super(parent);
		this.parent = parent;
		bar = new TabBrowserBar();
		this.setTabBar(bar);
		bar.tabMoved.connect(parent, "tabIndexChanged(int, int)");
	}
	
	public int addNewTab(QWidget widget, String title){
		int index = this.addTab(widget, new String());
		bar.addNewTab(index, title);
		this.setTabToolTip(index, title);
		return index;
	}

	public void setTabTitle(int index, String title) {
		bar.setTabTitle(index, title);
		this.setTabToolTip(index, title);
	}
	
}
