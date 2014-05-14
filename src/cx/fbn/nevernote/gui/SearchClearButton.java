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

import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QCursor;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QToolButton;
import com.trolltech.qt.gui.QWidget;

public class SearchClearButton extends QToolButton {
	private final QPixmap clearIcon;
	private final QPixmap clearActiveIcon;
	private final QPixmap clearPressedIcon;
	private final String iconPath;
	
	public SearchClearButton(QWidget parent, String iconPath) {
		super(parent);
		this.iconPath = iconPath;
		
		clearIcon = new QPixmap(this.iconPath + "clear.png");
		clearActiveIcon = new QPixmap(this.iconPath + "clearActive.png");
		clearPressedIcon = new QPixmap(this.iconPath + "clearPressed.png");
		
		this.setIcon(new QIcon(clearIcon));
		this.setIconSize(new QSize(16, 16));
		this.setCursor(new QCursor(Qt.CursorShape.ArrowCursor));
		this.setStyleSheet("QToolButton { border: none; padding: 0px; }");
		this.hide();
	}
	
	@Override
	protected void enterEvent(QEvent e) {
		super.enterEvent(e);
		this.setIcon(new QIcon(clearActiveIcon));
	}
	
	@Override
	protected void leaveEvent(QEvent e) {
		super.leaveEvent(e);
		this.setIcon(new QIcon(clearIcon));
	}
	
	@Override
	protected void mousePressEvent(QMouseEvent e) {
		super.mousePressEvent(e);
		this.setIcon(new QIcon(clearPressedIcon));
	}
	
	@Override
	protected void mouseReleaseEvent(QMouseEvent e) {
		super.mouseReleaseEvent(e);
		this.setIcon(new QIcon(clearIcon));
	}
}
