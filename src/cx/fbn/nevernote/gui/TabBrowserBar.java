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
