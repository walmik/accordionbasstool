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
import music.ChordParser;
import util.Main.StringParser;

class SeqColumnModel extends DefaultTableColumnModel
{

  SelectedButtonCombo selComboModel;
  ButtonComboSequence[] allComboSeqs;
  BoardSearcher searcher;
  RenderBassBoard renderBoard;
  SeqDataModel dataModel;
  SeqRowHeaderData rowHeaderDataModel;
  ListSelectionModel rowSelModel;
  final static int DEFAULT_COL_WIDTH = 120;

  SeqColumnModel(RenderBassBoard rBoard, ListSelectionModel selM)
  {
    selComboModel = new SelectedButtonCombo();
    selComboModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    allComboSeqs = new ButtonComboSequence[0];
    searcher = new BoardSearcher();

    dataModel = new SeqDataModel();
    rowHeaderDataModel = new SeqRowHeaderData();

    renderBoard = rBoard;
    rowSelModel = selM;

//    if (renderBoard != null) {
//      renderBoard.setSelectedButtonCombo(selComboModel);
//    }

    this.setSelectionModel(selComboModel);
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
    TableColumn column = new TableColumn(lastColIndex, DEFAULT_COL_WIDTH, null, null);
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
      syncModelToView(Math.min(columnIndex, newIndex), newIndex);
    }
  }

  int getSelectedColumn()
  {
    if (this.getSelectedColumnCount() > 0) {
      return this.getSelectedColumns()[0];
    } else {
      return -1;
    }
  }

  void editSelectedColumn(ChordDef newDef)
  {
    int index = getSelectedColumn();
    if (index >= 0) {
      editColumn(index, newDef);
    }
  }

  void removeSelectedColumn()
  {
    if (getColumnCount() < 2) {
      return;
    }
    int index = getSelectedColumn();
    if (index < 0) {
      return;
    }
    removeColumn(getColumn(index));
    if (index < getColumnCount()) {
      syncModelToView(index, index);
    } else {
      computeSeqs(index - 1);
    }
  }
  boolean isPopulating = false;

  public void populateFromText(String text)
  {
    StringParser parser = new StringParser(text);
    Vector<ChordDef> chords = ChordParser.parseChords(parser);

    this.tableColumns.clear();

    isPopulating = true;

    for (int i = 0; i < chords.size(); i++) {
      this.addColumn(chords.elementAt(i), i);
    }

    isPopulating = false;
    syncModelToView(0, 0);
  }

  private void syncModelToView(int index, int selIndex)
  {
    for (int i = index; i < getColumnCount(); i++) {
      this.getColumn(i).setModelIndex(i);
    }
    computeSeqs(selIndex);
  }

  private void editColumn(int index, ChordDef newChordDef)
  {
    TableColumn column = this.getColumn(index);

    if (newChordDef != null) {
      column.setHeaderValue(newChordDef);
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

  void computeSeqs(int selIndex)
  {
    if (renderBoard == null) {
      return;
    }

    if (isPopulating) {
      return;
    }

    music.BassBoard currBassBoard = renderBoard.getBassBoard();
    allComboSeqs = searcher.parseSequence(currBassBoard, getAllChords());
    assert (allComboSeqs != null);

    rowHeaderDataModel.fireListDataChanged();
    dataModel.fireTableStructureChanged();

    selComboModel.setSelectionInterval(selIndex, selIndex);
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
      if (allComboSeqs.length == 0) {
        return 1;
      }

      return allComboSeqs.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
      if (allComboSeqs.length == 0) {
        return "Chord not Possible";
      }
      music.ButtonCombo combo = allComboSeqs[rowIndex].getCombo(columnIndex);
      return combo.toInfoString(allComboSeqs[rowIndex].getBoard());
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
      String str = "Option #" + (index + 1);
      //str += allComboSeqs[index].getHeur();
      return str;
    }
  }
}
