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
