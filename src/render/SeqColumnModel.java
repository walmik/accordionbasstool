package render;

import music.ParsedChordDef;
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
import music.CollSequence;
import music.FingerComboSequence;
import music.FingerSearcher;
import music.Interval;
import music.Note;
import music.StringParser;

class SeqColumnModel extends DefaultTableColumnModel
{

  SelectedButtonCombo selComboModel;

  private ButtonComboSequence[] allComboSeqs;
  private BoardSearcher searcher;

  private FingerComboSequence[] fingerComboSeqs;
  private FingerSearcher fingerSearcher;

  private RenderBassBoard renderBoard;
  private CollSequence[] currSeqArray;

  private SeqRowHeaderData rowHeaderDataModel;
  private SeqDataModel dataModel;

  private ListSelectionModel rowSelModel;

  public boolean optFingerSearch = true;

  final static int DEFAULT_COL_WIDTH = 100;

  SeqColumnModel(RenderBassBoard rBoard, ListSelectionModel selM)
  {
    selComboModel = new SelectedButtonCombo();
    selComboModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    allComboSeqs = new ButtonComboSequence[0];
    searcher = new BoardSearcher();
    
    fingerComboSeqs = new FingerComboSequence[0];
    fingerSearcher = new FingerSearcher();

    currSeqArray = (optFingerSearch ? fingerComboSeqs : allComboSeqs);

    renderBoard = rBoard;
    rowSelModel = selM;

    dataModel = new SeqDataModel();
    rowHeaderDataModel = new SeqRowHeaderData();

//    if (renderBoard != null) {
//      renderBoard.setSelectedButtonCombo(selComboModel);
//    }

    this.setSelectionModel(selComboModel);
  }

  void addColumn(int index)
  {
    addColumn(ParsedChordDef.getDefaultChordDef(), index);
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

  void editSelectedColumn(ParsedChordDef newDef)
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
      getColumn(i).setModelIndex(i);
    }

    computeSeqs(selIndex);
  }

  public void transposeAllFromSelectedColumn(Note newNote)
  {
    TableColumn column = this.getColumn(getSelectedColumn());

    // Find interval diff between new and old and apply to all
    ParsedChordDef existingDef = (ParsedChordDef) column.getHeaderValue();

    Interval transDiff = newNote.diff(existingDef.rootNote);

    transposeAllByInterval(transDiff);
  }

  public void transposeAllByInterval(Interval transDiff)
  {
    ParsedChordDef existingDef;

    for (int i = 0; i < getColumnCount(); i++) {
      TableColumn currCol = this.getColumn(i);
      existingDef = (ParsedChordDef) currCol.getHeaderValue();
      currCol.setHeaderValue(existingDef.transposeBy(transDiff));
    }

    computeSeqs(getSelectedColumn());
  }

  private void editColumn(int index, ParsedChordDef newChordDef)
  {
    TableColumn column = this.getColumn(index);

    if (newChordDef == null) {
      return;
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

  SeqDataModel getDataModel()
  {
    return dataModel;
  }

  SeqRowHeaderData getRowHeaderDataModel()
  {
    return rowHeaderDataModel;
  }

  boolean setSelectedSeq(int index)
  {
    if (index < 0) {
      selComboModel.setButtonComboSeq(null);
      return false;
    }

    if (optFingerSearch) {
      if (index < fingerComboSeqs.length) {
        selComboModel.setFingerComboSeq(fingerComboSeqs[index]);
        return true;
      } else {
        selComboModel.setButtonComboSeq(null);
      }
    } else {
      if (index < allComboSeqs.length) {
        selComboModel.setButtonComboSeq(allComboSeqs[index]);
        return true;
      } else {
        selComboModel.setButtonComboSeq(null);
      }
    }

    return false;
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

    if (optFingerSearch) {
      fingerComboSeqs = fingerSearcher.findAllFingers(allComboSeqs);
      currSeqArray = fingerComboSeqs;
    } else {
      currSeqArray = allComboSeqs;
    }

    rowHeaderDataModel.fireListDataChanged();
    dataModel.fireTableDataChanged();

    if (selIndex < currSeqArray.length) {
      selComboModel.setSelectionInterval(selIndex, selIndex);
    } else {
      selComboModel.setSelectionInterval(0, 0);
    }
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
      return CollSequence.class;
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
      if (currSeqArray.length == 0) {
        return 1;
      }

      return currSeqArray.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
      if ((rowIndex < 0) || (rowIndex >= currSeqArray.length)) {
        return null;
      }

      if ((columnIndex < 0) || (columnIndex >= currSeqArray[rowIndex].getNumCombos())) {
        return null;
      }

      return currSeqArray[rowIndex].getCombo(columnIndex);
    }
  }

//  class ButtonSeqDataModel extends AbstractTableModel
//  {
//
//    @Override
//    public Class<?> getColumnClass(int columnIndex)
//    {
//      return ButtonCombo.class;
//    }
//
//    @Override
//    public String getColumnName(int column)
//    {
//      return SeqColumnModel.this.getChordDef(column).nameHtml;
//    }
//
//    public ButtonSeqDataModel()
//    {
//    }
//
//    @Override
//    public int getColumnCount()
//    {
//      return SeqColumnModel.this.getColumnCount();
//    }
//
//    @Override
//    public int getRowCount()
//    {
//      if (allComboSeqs.length == 0) {
//        return 1;
//      }
//
//      return allComboSeqs.length;
//    }
//
//    @Override
//    public Object getValueAt(int rowIndex, int columnIndex)
//    {
//      if ((rowIndex < 0) || (rowIndex >= allComboSeqs.length)) {
//        return null;
//      }
//
//      if ((columnIndex < 0) || (columnIndex >= allComboSeqs[rowIndex].getNumCombos())) {
//        return null;
//      }
//
//      return allComboSeqs[rowIndex].getCombo(columnIndex);
//    }
//  }

  class SeqRowHeaderData extends AbstractListModel
  {
    SeqRowHeaderData()
    {

    }

    void fireListDataChanged()
    {
      fireContentsChanged(this, 0, currSeqArray.length);
    }


    @Override
    public int getSize()
    {
      if (currSeqArray.length == 0) {
        return 1;
      }

      return currSeqArray.length;
    }

    @Override
    public Object getElementAt(int index)
    {
      if (currSeqArray.length == 0) {
        return null;
      }
      
      return currSeqArray[index];
    }
  }
  }
