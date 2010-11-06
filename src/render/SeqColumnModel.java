package render;

import music.ParsedChordDef;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import music.BoardSearcher;
import music.ButtonCombo;
import music.ButtonComboSequence;
import music.Chord;
import music.ChordParser;
import music.Interval;
import music.Note;
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

  void addColumn(ParsedChordDef def, int index)
  {
    if (def == null) {
      return;
    }
    int lastColIndex = getColumnCount();
    if (index < 0) {
      index = lastColIndex;
    }
    TableColumn column = new TableColumn(lastColIndex, 0, null, null);
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

  void editSelectedColumn(ParsedChordDef newDef, boolean transposeAll)
  {
    int index = getSelectedColumn();
    if (index >= 0) {
      editColumn(index, newDef, transposeAll);
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
    Vector<ParsedChordDef> chords = ChordParser.parseChords(parser);

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

  public void transposeAllFromSelectedColumn(Note newNote)
  {
    TableColumn column = this.getColumn(getSelectedColumn());

    // Find interval diff between new and old and apply to all
    ParsedChordDef existingDef = (ParsedChordDef) column.getHeaderValue();

    Interval transDiff = newNote.diff(existingDef.rootNote);

    for (int i = 0; i < getColumnCount(); i++) {
      TableColumn currCol = this.getColumn(i);
      existingDef = (ParsedChordDef) currCol.getHeaderValue();
      currCol.setHeaderValue(existingDef.transposeBy(transDiff));
    }

    computeSeqs(getSelectedColumn());
  }

  private void editColumn(int index, ParsedChordDef newChordDef, boolean transposeAll)
  {
    TableColumn column = this.getColumn(index);

    if (newChordDef == null) {
      return;
    }

    if (transposeAll) {
      // Find interval diff between new and old and apply to all
      ParsedChordDef existingDef = (ParsedChordDef) column.getHeaderValue();

      Interval transDiff = newChordDef.rootNote.diff(existingDef.rootNote);

      for (int i = 0; i < getColumnCount(); i++) {
        // Current column already at transposed value, skip
        if (i == index) {
          continue;
        }
        TableColumn currCol = this.getColumn(i);
        existingDef = (ParsedChordDef) currCol.getHeaderValue();
        currCol.setHeaderValue(existingDef.transposeBy(transDiff));
      }
    }

    column.setHeaderValue(newChordDef);
    computeSeqs(index);
  }

  ParsedChordDef getChordDef(int index)
  {
    assert ((index >= 0) && (index < getColumnCount()));
    return (ParsedChordDef) getColumn(index).getHeaderValue();
  }

  Vector<Chord> getAllChords()
  {
    Vector<Chord> vec = new Vector<Chord>();
    for (int i = 0; i < getColumnCount(); i++) {
      vec.add(getChordDef(i).chord);
    }
    return vec;
  }

  @Override
  public String toString()
  {
    String str = "";
    for (int i = 0; i < getColumnCount(); i++) {
      if (i > 0) {
        str += ", ";
      }

      ParsedChordDef def = getChordDef(i);
      str += def.namePlain;
    }

    return str;
  }

  public String toHtmlString(boolean hiliteSelected)
  {
    String str = "";

    int selIndex = -1;
    if (hiliteSelected) {
      selIndex = this.getSelectedColumn();
    }

    for (int i = 0; i < getColumnCount(); i++) {
      if (i > 0) {
        str += ", ";
      }

      ParsedChordDef def = getChordDef(i);

      if (i == selIndex) {
        str += "<b>" + def.nameHtml + "</b>";
      } else {
        str += def.nameHtml;
      }
    }

    return str;
  }

  void computeSeqs(int selIndex)
  {
    if (renderBoard == null) {
      return;
    }

    if (isPopulating) {
      return;
    }

    //System.out.println(toString());

    music.BassBoard currBassBoard = renderBoard.getBassBoard();
    allComboSeqs = searcher.parseSequence(currBassBoard, getAllChords());
    assert (allComboSeqs != null);

    rowHeaderDataModel.fireListDataChanged();
    dataModel.fireTableStructureChanged();

    selComboModel.setSelectionInterval(selIndex, selIndex);
    rowSelModel.setSelectionInterval(0, 0);
  }

  public void recomputeSeqs()
  {
    computeSeqs(getSelectedColumn());
  }

  class SeqDataModel extends AbstractTableModel
  {

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
      return ButtonCombo.class;
    }

    @Override
    public String getColumnName(int column)
    {
      return SeqColumnModel.this.getChordDef(column).nameHtml;
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
      if ((rowIndex < 0) || (rowIndex >= allComboSeqs.length)) {
        return null;
      }

      if ((columnIndex < 0) || (columnIndex >= allComboSeqs[rowIndex].getNumCombos())) {
        return null;
      }

      return allComboSeqs[rowIndex].getCombo(columnIndex);
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
      String str = "#" + (index + 1);
      str += " (" + allComboSeqs[index].getHeur() + ")";
      return str;
    }
  }
}
