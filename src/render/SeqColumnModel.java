package render;

import music.ChordDef;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import music.BoardSearcher;
import music.ButtonComboSequence;
import music.Chord;

class SeqColumnModel extends DefaultTableColumnModel
{
  SelectedButtonCombo selComboModel;
  ButtonComboSequence[] allComboSeqs;
  BoardSearcher searcher;
  ChordPicker chordPicker;
  RenderBassBoard renderBoard;
  SeqDataModel dataModel;
  SeqRowHeaderData rowHeaderDataModel;
  ListSelectionModel rowSelModel;

  SeqColumnModel(ChordPicker picker, RenderBassBoard rBoard, ListSelectionModel selM)
  {
    selComboModel = new SelectedButtonCombo();
    selComboModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    allComboSeqs = new ButtonComboSequence[0];
    searcher = new BoardSearcher();

    dataModel = new SeqDataModel();
    rowHeaderDataModel = new SeqRowHeaderData();
    
    chordPicker = picker;
    renderBoard = rBoard;
    rowSelModel = selM;

    renderBoard.setSelectedButtonCombo(selComboModel);

    this.setSelectionModel(selComboModel);
  }

  void addColumn(int index)
  {
    ChordDef def = chordPicker.showChordPicker(null);
    addColumn(def, index);
  }

  void addColumn(ChordDef def, int index)
  {
    if (def == null) {
      return;
    }
    int lastColIndex = getColumnCount();
    if (index < 0) {
      index = lastColIndex;
    }
    TableColumn column = new TableColumn(lastColIndex, 120, null, null);
    column.setHeaderValue(def);
    addColumn(column);
    if (index < lastColIndex) {
      moveColumn(lastColIndex, index);
    } else {
      computeSeqs(index);
    }
  }

  @Override
  public void moveColumn(int columnIndex, int newIndex)
  {
    super.moveColumn(columnIndex, newIndex);
    if (columnIndex != newIndex) {
      syncModelToView(Math.min(columnIndex, newIndex));
    }
  }

  void removeSelectedColumn()
  {
    if (getSelectedColumnCount() < 1) {
      return;
    }
    if (getColumnCount() < 2) {
      return;
    }
    int index = getSelectedColumns()[0];
    removeColumn(getColumn(index));
    if (index < getColumnCount()) {
      syncModelToView(index);
    } else {
      computeSeqs(index - 1);
    }
  }

  private void syncModelToView(int index)
  {
    for (int i = index; i < getColumnCount(); i++) {
      this.getColumn(i).setModelIndex(i);
    }
    computeSeqs(index);
  }

  void editColumn(int index)
  {
    if ((index < 0) || (index > getColumnCount())) {
      return;
    }
    TableColumn column = this.getColumn(index);
    ChordDef def = (ChordDef) column.getHeaderValue();
    def = chordPicker.showChordPicker(def);
    if (def != null) {
      column.setHeaderValue(def);
      computeSeqs(index);
    }
  }

  ChordDef getChordDef(int index)
  {
    assert ((index >= 0) && (index < getColumnCount()));
    return (ChordDef) getColumn(index).getHeaderValue();
  }

  Vector<Chord> getAllChords()
  {
    Vector<Chord> vec = new Vector<Chord>();
    for (int i = 0; i < getColumnCount(); i++) {
      vec.add(getChordDef(i).getChord());
    }
    return vec;
  }

  void computeSeqs(int colIndex)
  {
    if (renderBoard == null) {
      return;
    }

    music.BassBoard currBassBoard = renderBoard.getBassBoard();
    allComboSeqs = searcher.parseSequence(currBassBoard, getAllChords());
    assert (allComboSeqs != null);

    rowHeaderDataModel.fireListDataChanged();
    dataModel.fireTableStructureChanged();
    
    selComboModel.setSelectionInterval(colIndex, colIndex);
    rowSelModel.setSelectionInterval(0, 0);
  }

  class SeqDataModel extends AbstractTableModel
  {

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
      return String.class;
    }

    @Override
    public String getColumnName(int column)
    {
      return SeqColumnModel.this.getChordDef(column).getAbbrevHtml();
    }

    public SeqDataModel()
    {
    }

    @Override
    public int getColumnCount()
    {
      return SeqColumnModel.this.getColumnCount();
    }

    @Override
    public int getRowCount()
    {
      return allComboSeqs.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
      music.ButtonCombo combo = allComboSeqs[rowIndex].getCombo(columnIndex);
      return combo.toString();
    }
  }

  class SeqRowHeaderData extends AbstractListModel
  {

    void fireListDataChanged()
    {
      fireContentsChanged(this, 0, allComboSeqs.length);
    }

    @Override
    public int getSize()
    {
      if (allComboSeqs == null) {
        return 0;
      }

      return allComboSeqs.length;
    }

    @Override
    public Object getElementAt(int index)
    {
      String str = "H: ";
      str += allComboSeqs[index].getHeur();
      return str;
    }
  }
}
