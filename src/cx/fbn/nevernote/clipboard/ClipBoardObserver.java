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

package cx.fbn.nevernote.clipboard;

import com.trolltech.qt.gui.QApplication;

public class ClipBoardObserver {
	private String SourceGuid;
	private boolean internalFlg;	// アプリケーション内コピーかどうかのフラグ
	
	public ClipBoardObserver() {
		SourceGuid = new String("");
		internalFlg = false;
		QApplication.clipboard().dataChanged.connect(this, "clipboardDataChanged()");
	}
	
	public void setCopySourceGuid(String guid, String text) {		
		if (!text.equals("")) {
			SourceGuid = guid;
			internalFlg = true;
		}
	}
	
	public String getSourceGuid() {
		if (SourceGuid.equals("")) {
			return null;
		}
		return SourceGuid;
	}
	
	@SuppressWarnings("unused")
	private void clipboardDataChanged() {
		// 外部アプリケーションでコピー・カットが行われた時のための対処
		if (!internalFlg) {
			SourceGuid = "";
		}
		internalFlg = false;
	}
}
