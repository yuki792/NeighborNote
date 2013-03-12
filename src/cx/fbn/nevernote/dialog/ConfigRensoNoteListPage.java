// ICHANGED
package cx.fbn.nevernote.dialog;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QFormLayout;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QSlider;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.Global;

public class ConfigRensoNoteListPage extends QWidget {
	private final QSlider browseSlider;
	private final QSlider copyPasteSlider;
	private final QSlider addNewNoteSlider;
	private final QSlider rensoItemClickSlider;
	private final QSlider sameTagSlider;
	private final QSlider sameNotebookSlider;
	
	private final QSpinBox browseSpinner;
	private final QSpinBox copyPasteSpinner;
	private final QSpinBox addNewNoteSpinner;
	private final QSpinBox rensoItemClickSpinner;
	private final QSpinBox sameTagSpinner;
	private final QSpinBox sameNotebookSpinner;
	private final QSpinBox rensoListItemMaximumSpinner;
	
	private final QCheckBox mergeCheck;
	private final QCheckBox duplicateCheck;
	private final QCheckBox verifyExclude;
	
	public ConfigRensoNoteListPage(QWidget parent) {
		// 操作履歴への重み付け
		// browseHistory
		browseSlider = new QSlider();
		browseSlider.setOrientation(Qt.Orientation.Horizontal);
		browseSlider.setRange(0, 10);
		browseSlider.setSingleStep(1);
		browseSlider.setTickPosition(QSlider.TickPosition.TicksAbove);
		browseSlider.setTickInterval(1);
		browseSlider.setFocusPolicy(Qt.FocusPolicy.StrongFocus);

		browseSpinner = new QSpinBox();
		browseSpinner.setRange(0,10);
		browseSpinner.setSingleStep(1);
		
		browseSlider.valueChanged.connect(browseSpinner, "setValue(int)");
		browseSpinner.valueChanged.connect(browseSlider, "setValue(int)");
		browseSlider.setValue(Global.getBrowseWeight());
		
		QHBoxLayout browseLayout = new QHBoxLayout();
		browseLayout.addWidget(browseSlider);
		browseLayout.addWidget(browseSpinner);
		
		
		// copyPasteHistory
		copyPasteSlider = new QSlider();
		copyPasteSlider.setOrientation(Qt.Orientation.Horizontal);
		copyPasteSlider.setRange(0, 10);
		copyPasteSlider.setSingleStep(1);
		copyPasteSlider.setTickPosition(QSlider.TickPosition.TicksAbove);
		copyPasteSlider.setTickInterval(1);
		copyPasteSlider.setFocusPolicy(Qt.FocusPolicy.StrongFocus);
		
		copyPasteSpinner = new QSpinBox();
		copyPasteSpinner.setRange(0,10);
		copyPasteSpinner.setSingleStep(1);
		
		copyPasteSlider.valueChanged.connect(copyPasteSpinner, "setValue(int)");
		copyPasteSpinner.valueChanged.connect(copyPasteSlider, "setValue(int)");
		copyPasteSlider.setValue(Global.getCopyPasteWeight());

		
		QHBoxLayout copyPasteLayout = new QHBoxLayout();
		copyPasteLayout.addWidget(copyPasteSlider);
		copyPasteLayout.addWidget(copyPasteSpinner);
		
		
		// addNewNoteHistory
		addNewNoteSlider = new QSlider();
		addNewNoteSlider.setOrientation(Qt.Orientation.Horizontal);
		addNewNoteSlider.setRange(0, 10);
		addNewNoteSlider.setSingleStep(1);
		addNewNoteSlider.setTickPosition(QSlider.TickPosition.TicksAbove);
		addNewNoteSlider.setTickInterval(1);
		addNewNoteSlider.setFocusPolicy(Qt.FocusPolicy.StrongFocus);

		addNewNoteSpinner = new QSpinBox();
		addNewNoteSpinner.setRange(0,10);
		addNewNoteSpinner.setSingleStep(1);
		
		addNewNoteSlider.valueChanged.connect(addNewNoteSpinner, "setValue(int)");
		addNewNoteSpinner.valueChanged.connect(addNewNoteSlider, "setValue(int)");
		addNewNoteSlider.setValue(Global.getAddNewNoteWeight());
		
		QHBoxLayout addNewNoteLayout = new QHBoxLayout();
		addNewNoteLayout.addWidget(addNewNoteSlider);
		addNewNoteLayout.addWidget(addNewNoteSpinner);
		
		
		// rensoItemClickHistory
		rensoItemClickSlider = new QSlider();
		rensoItemClickSlider.setOrientation(Qt.Orientation.Horizontal);
		rensoItemClickSlider.setRange(0, 10);
		rensoItemClickSlider.setSingleStep(1);
		rensoItemClickSlider.setTickPosition(QSlider.TickPosition.TicksAbove);
		rensoItemClickSlider.setTickInterval(1);
		rensoItemClickSlider.setFocusPolicy(Qt.FocusPolicy.StrongFocus);

		rensoItemClickSpinner = new QSpinBox();
		rensoItemClickSpinner.setRange(0,10);
		rensoItemClickSpinner.setSingleStep(1);
		
		rensoItemClickSlider.valueChanged.connect(rensoItemClickSpinner, "setValue(int)");
		rensoItemClickSpinner.valueChanged.connect(rensoItemClickSlider, "setValue(int)");
		rensoItemClickSlider.setValue(Global.getRensoItemClickWeight());
		
		QHBoxLayout rensoItemClickLayout = new QHBoxLayout();
		rensoItemClickLayout.addWidget(rensoItemClickSlider);
		rensoItemClickLayout.addWidget(rensoItemClickSpinner);
		
		// sameTagHistory
		sameTagSlider = new QSlider();
		sameTagSlider.setOrientation(Qt.Orientation.Horizontal);
		sameTagSlider.setRange(0, 10);
		sameTagSlider.setSingleStep(1);
		sameTagSlider.setTickPosition(QSlider.TickPosition.TicksAbove);
		sameTagSlider.setTickInterval(1);
		sameTagSlider.setFocusPolicy(Qt.FocusPolicy.StrongFocus);

		sameTagSpinner = new QSpinBox();
		sameTagSpinner.setRange(0,10);
		sameTagSpinner.setSingleStep(1);
		
		sameTagSlider.valueChanged.connect(sameTagSpinner, "setValue(int)");
		sameTagSpinner.valueChanged.connect(sameTagSlider, "setValue(int)");
		sameTagSlider.setValue(Global.getSameTagWeight());
		
		QHBoxLayout sameTagLayout = new QHBoxLayout();
		sameTagLayout.addWidget(sameTagSlider);
		sameTagLayout.addWidget(sameTagSpinner);
		
		// sameNotebookHistory
		sameNotebookSlider = new QSlider();
		sameNotebookSlider.setOrientation(Qt.Orientation.Horizontal);
		sameNotebookSlider.setRange(0, 10);
		sameNotebookSlider.setSingleStep(1);
		sameNotebookSlider.setTickPosition(QSlider.TickPosition.TicksAbove);
		sameNotebookSlider.setTickInterval(1);
		sameNotebookSlider.setFocusPolicy(Qt.FocusPolicy.StrongFocus);

		sameNotebookSpinner = new QSpinBox();
		sameNotebookSpinner.setRange(0,10);
		sameNotebookSpinner.setSingleStep(1);
		
		sameNotebookSlider.valueChanged.connect(sameNotebookSpinner, "setValue(int)");
		sameNotebookSpinner.valueChanged.connect(sameNotebookSlider, "setValue(int)");
		sameNotebookSlider.setValue(Global.getSameNotebookWeight());
		
		QHBoxLayout sameNotebookLayout = new QHBoxLayout();
		sameNotebookLayout.addWidget(sameNotebookSlider);
		sameNotebookLayout.addWidget(sameNotebookSpinner);	
		
		QFormLayout styleLayout = new QFormLayout();
		styleLayout.setHorizontalSpacing(10);
		styleLayout.setVerticalSpacing(30);
		styleLayout.addRow(tr("Browse Weight"), browseLayout);
		styleLayout.addRow(tr("Copy&Paste Weight"), copyPasteLayout);
		styleLayout.addRow(tr("Add New Note Weight"), addNewNoteLayout);
		styleLayout.addRow(tr("Renso Item Click Weight"), rensoItemClickLayout);
		styleLayout.addRow(tr("Same Tag Weight"), sameTagLayout);
		styleLayout.addRow(tr("Same Notebook Weight"), sameNotebookLayout);

		QGroupBox weightingGroup = new QGroupBox(tr("Weighting"));
		weightingGroup.setLayout(styleLayout);
		
		// ノートのマージ・複製の関連ノートリストへの適用
		mergeCheck = new QCheckBox(tr("When you merge the notes, also merge RensoNoteList"));
		mergeCheck.setChecked(Global.getMergeRensoNote());
		duplicateCheck = new QCheckBox(tr("When you duplicate the notes, also duplicate RensoNoteList"));
		duplicateCheck.setChecked(Global.getDuplicateRensoNote());
		verifyExclude = new QCheckBox(tr("Verify when you exclude the note"));
		verifyExclude.setChecked(Global.verifyExclude());
		
		// 連想ノートリスト最大アイテム表示数
		rensoListItemMaximumSpinner = new QSpinBox();
		rensoListItemMaximumSpinner.setRange(1,100);
		rensoListItemMaximumSpinner.setSingleStep(1);
		rensoListItemMaximumSpinner.setValue(Global.getRensoListItemMaximum());
		QFormLayout fLayout = new QFormLayout();
		fLayout.setHorizontalSpacing(100);
		fLayout.addRow(tr("Renso Note List Item Maximum"), rensoListItemMaximumSpinner);
		
		// その他のレイアウト
		QVBoxLayout othersLayout = new QVBoxLayout();
		othersLayout.addWidget(mergeCheck);
		othersLayout.addWidget(duplicateCheck);
		othersLayout.addWidget(verifyExclude);
		othersLayout.addLayout(fLayout);
		
		QGroupBox othersGroup = new QGroupBox(tr("Others"));
		othersGroup.setLayout(othersLayout);
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addWidget(weightingGroup);
		mainLayout.addWidget(othersGroup);
		mainLayout.addStretch(1);
		setLayout(mainLayout);
		
	}
	
	//*****************************************
	//* Browse Weight 
	//*****************************************
	public int getBrowseWeight() {
		return browseSpinner.value();
	}
	
	//*****************************************
	//* Copy & Paste Weight 
	//*****************************************
	public int getCopyPasteWeight() {
		return copyPasteSpinner.value();
	}
	
	//*****************************************
	//* Add New Note Weight 
	//*****************************************
	public int getAddNewNoteWeight() {
		return addNewNoteSpinner.value();
	}
	
	//*****************************************
	//* Renso Item Click Weight
	//*****************************************
	public int getRensoItemClickWeight() {
		return rensoItemClickSpinner.value();
	}
	
	//*****************************************
	//* Same Tag Weight
	//*****************************************
	public int getSameTagWeight() {
		return sameTagSpinner.value();
	}
	
	//*****************************************
	//* Same Notebook Weight
	//*****************************************
	public int getSameNotebookWeight() {
		return sameNotebookSpinner.value();
	}

	//*****************************************
	//* Merge Check
	//*****************************************
	public boolean getMergeChecked() {
		return mergeCheck.isChecked();
	}

	//*****************************************
	//* DuplicateCheck
	//*****************************************
	public boolean getDuplicateChecked() {
		return duplicateCheck.isChecked();
	}
	
	//*****************************************
	//* VerifyExcludeCheck
	//*****************************************
	public boolean getVerifyExcludeChecked() {
		return verifyExclude.isChecked();
	}

	//*****************************************
	//* RensoNoteListItemMaximum
	//*****************************************
	public int getRensoListItemMaximum() {
		return rensoListItemMaximumSpinner.value();
	}
}
