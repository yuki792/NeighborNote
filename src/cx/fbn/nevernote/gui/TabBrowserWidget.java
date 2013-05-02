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

	public int insertNewTab(int index, QWidget widget, String title) {
		int trueIndex = this.insertTab(index, widget, new String());
		bar.addNewTab(trueIndex, title);
		this.setTabToolTip(trueIndex, title);
		return trueIndex;
	}
	
}
