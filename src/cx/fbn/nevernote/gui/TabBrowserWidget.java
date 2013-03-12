// ICHANGED
package cx.fbn.nevernote.gui;

import com.trolltech.qt.gui.QTabWidget;
import com.trolltech.qt.gui.QWidget;

public class TabBrowserWidget extends QTabWidget {
	TabBrowserBar bar;
	
	public TabBrowserWidget(QWidget parent) {
		super(parent);
		bar = new TabBrowserBar();
		this.setTabBar(bar);
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
