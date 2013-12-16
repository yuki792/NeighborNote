package cx.fbn.nevernote.gui;

import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QFocusEvent;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QStyle.PixelMetric;

public class SearchEdit extends QLineEdit {
	private final SearchClearButton clearButton;
	private final String inactiveColor;
	private final String activeColor;
	private final String defaultText;

	
	public SearchEdit(String iconPath) {
		inactiveColor = new String("QLineEdit {color: gray; font:italic;} ");
		activeColor = new String("QLineEdit {color: black; font:normal;} ");
		
		this.clearButton = new SearchClearButton(this, iconPath);
		this.clearButton.clicked.connect(this, "clear()");
		
		this.textChanged.connect(this, "updateClearButton(String)");
		int frameWidth = this.style().pixelMetric(PixelMetric.PM_DefaultFrameWidth);
		this.setStyleSheet("QLineEdit { padding-right: " + (clearButton.sizeHint().width() + frameWidth + 1) + "px; } ");
		defaultText = new String(tr("Search"));
		this.setText(defaultText);
		this.setStyleSheet(inactiveColor);
	}
	
	@Override
	protected void resizeEvent(QResizeEvent event) {
		QSize sz = clearButton.sizeHint();
		int frameWidth = this.style().pixelMetric(PixelMetric.PM_DefaultFrameWidth);
		clearButton.move(this.rect().right() - frameWidth - sz.width(), (this.rect().bottom() + 1 - sz.height()) / 2);
	}
	
	@SuppressWarnings("unused")
	private void updateClearButton(String text) {
		clearButton.setVisible(!text.isEmpty());
	}
	
	public void setdefaultText() {
		this.setText(defaultText);
		this.setStyleSheet(inactiveColor);
	}
	
	@Override
	protected void focusInEvent(QFocusEvent event) {
		super.focusInEvent(event);
		if (this.text().equals(defaultText)) {
			this.blockSignals(true);
			this.setText("");
			this.blockSignals(false);
		}
		this.setStyleSheet(activeColor);
	}
	
	@Override
	protected void focusOutEvent(QFocusEvent event) {
		super.focusOutEvent(event);
		if (this.text().trim().equals("")) {
			this.blockSignals(true);
			this.setText(defaultText);
			this.blockSignals(false);
			this.setStyleSheet(inactiveColor);
		}
	}
}
