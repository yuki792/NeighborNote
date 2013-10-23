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
import com.trolltech.qt.gui.QTabBar;
import com.trolltech.qt.gui.QTabWidget;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.NeverNote;

public class TabBrowserWidget extends QTabWidget {
	private final TabBrowserBar bar;
	private final NeverNote parent;
	private QSize closeButtonSize;
	
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
		
		// タブが1個（多分これが発生するのはアプリ起動時だけ）なら閉じるボタンを隠す
		// タブが2個以上あったら、一番左のタブの閉じるボタンを復元する
		int tabCnt = this.count();
		if (tabCnt == 1) {
			hideTabCloseButton(0);
		} else if (tabCnt >= 2) {
			if (closeButtonSize != null && !bar.tabButton(0, QTabBar.ButtonPosition.RightSide).size().equals(closeButtonSize)) {
				showTabCloseButton(0);
			}
		}
		
		return index;
	}

	public void setTabTitle(int index, String title) {
		bar.setTabTitle(index, title);
		this.setTabToolTip(index, title);
	}
	
	// タブを閉じるボタンを隠す
	public void hideTabCloseButton(int index) {
		if (closeButtonSize == null) {
			closeButtonSize = bar.tabButton(index, QTabBar.ButtonPosition.RightSide).size();
		}
		bar.tabButton(index, QTabBar.ButtonPosition.RightSide).resize(0, 0);
	}
	
	// タブを閉じるボタンを復元する
	public void showTabCloseButton(int index) {
		if (closeButtonSize != null) {
			bar.tabButton(index, QTabBar.ButtonPosition.RightSide).resize(closeButtonSize);
		} else {
			bar.tabButton(index, QTabBar.ButtonPosition.RightSide).resize(16, 16);
		}
	}
}
