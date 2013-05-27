/*
 * This file is part of NixNote/NeighborNote 
 * Copyright 2009 Randy Baumgarte
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

package cx.fbn.nevernote.dialog;

//**********************************************
//**********************************************
//* Dialog box to notify that a new release
//* is available.
//**********************************************
//**********************************************

import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QDesktopServices;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSpacerItem;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.webkit.QWebView;

import cx.fbn.nevernote.Global;

public class UpgradeAvailableDialog extends QDialog {

	private boolean 	yesPressed;
	private final QPushButton yesButton;
	private final QPushButton noButton;
	private final QCheckBox doNotRemindeMe;
	private final QLabel downloadLabel;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	// Constructor
	public UpgradeAvailableDialog() {
		yesPressed = false;
		setWindowTitle(tr("Upgrade Available"));
		setWindowIcon(new QIcon(iconPath+"nevernote.png"));
		QVBoxLayout grid = new QVBoxLayout();
		QGridLayout input = new QGridLayout();
		QHBoxLayout button = new QHBoxLayout();
		setLayout(grid);		
			
		QWebView page = new QWebView(this);
		page.setUrl(new QUrl(Global.getUpdateAnnounceUrl()));
		
		doNotRemindeMe = new QCheckBox();
		doNotRemindeMe.setText(tr("Automatically check for updates at startup"));
		input.addWidget(page,1,1);
		doNotRemindeMe.setChecked(true);
		input.addWidget(doNotRemindeMe,2,1);
		
		QHBoxLayout labelLayout = new QHBoxLayout();
		downloadLabel = new QLabel(tr("Do you want to download now?"));
		labelLayout.addStretch();
		labelLayout.addWidget(downloadLabel);
		labelLayout.addStretch();
		input.addItem(new QSpacerItem(0, 50), 3, 1);
		input.addLayout(labelLayout, 4, 1);
		
		grid.addLayout(input);
		
		yesButton = new QPushButton(tr("Yes"));
		yesButton.clicked.connect(this, "yesButtonPressed()");
		noButton = new QPushButton(tr("No"));
		noButton.clicked.connect(this, "noButtonPressed()");

		button.addStretch();
		button.addWidget(yesButton);
		button.addWidget(noButton);
		button.addStretch();
		grid.addLayout(button);		
	}

	// The Yes button was pressed
	@SuppressWarnings("unused")
	private void yesButtonPressed() {
		yesPressed = true;
		QDesktopServices.openUrl(new QUrl(Global.getUpdateDownloadUrl()));
		close();
	}
	// The No button was pressed
	@SuppressWarnings("unused")
	private void noButtonPressed() {
		yesPressed = false;
		close();
	}
	// Check if the Yes button was pressed
	public boolean yesPressed() {
		return yesPressed;
	}
	
	public boolean remindMe() {
		return doNotRemindeMe.isChecked();
	}
}
