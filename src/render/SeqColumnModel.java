package render;

import java.util.ListIterator;
import music.ParsedChordDef;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import music.BoardSearcher;
import music.ButtonCombo;
import music.ButtonComboSequence;
import music.ChordParser;
import music.CollSequence;
import music.FingerCombo;
import music.FingerComboSequence;
import music.FingerSearcher;
import music.Interval;
import music.Note;
import music.StringParser;

public class SeqColumnModel extends DefaultTableColumnModel
{

  public final SelectedButtonCombo selComboModel;
  public final MatchingChordStore matchingChordStore;
  private ButtonComboSequence[] allComboSeqs;
  private BoardSearcher searcher;
  private FingerComboSequence[] fingerComboSeqs;
  private FingerSearcher fingerSearcher;
  private RenderBassBoard renderBoard;
  private CollSequence[] currSeqArray;
  private SeqRowHeaderData rowHeaderDataModel;
  private SeqDataModel dataModel;
  private DefaultListSelectionModel rowSelModel;
  private ChordIterator chordSeqIter;
  public boolean optFingerSearch = false;
  final static int DEFAULT_COL_WIDTH = 120;
  //SeqViewerController control;

  public SeqColumnModel(RenderBassBoard rBoard)
  {
    selComboModel = new SelectedButtonCombo();
    selComboModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    allComboSeqs = new ButtonComboSequence[0];
    searcher = new BoardSearcher();

    fingerComboSeqs = new FingerComboSequence[0];
    fingerSearcher = new FingerSearcher();

    currSeqArray = (optFingerSearch ? fingerComboSeqs : allComboSeqs);

    chordSeqIter = new ChordIterator();

    renderBoard = rBoard;
    rowSelModel = new DefaultListSelectionModel();
    rowSelModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    dataModel = new SeqDataModel();
    rowHeaderDataModel = new SeqRowHeaderData();

    this.setSelectionModel(selComboModel);

    matchingChordStore = new MatchingChordStore(this);

    rowSelModel.addListSelectionListener(new ListSelChangeListener()
    {

      @Override
      protected void selectionChanged(int index)
      {
        setSelectedSeq(index);
      }
    });
  }

  public void addColumn(int index)
  {
    addColumn(ParsedChordDef.newDefaultChordDef(), index);
  }

  public void addColumn(ParsedChordDef def, int index)
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

  @Override
  public int getSelectedColumnCount()
  {
    return (this.selComboModel.getAnchorSelectionIndex() == -1 ? 0 : 1);
  }

  public int getSelectedColumn()
  {
    return this.selComboModel.getAnchorSelectionIndex();
  }

  public ButtonCombo getSelectedButtonCombo()
  {
    int row = this.rowSelModel.getAnchorSelectionIndex();
    int col = this.getSelectedColumn();

    Object obj = this.getDataModel().getValueAt(row, col);

    if (obj == null) {
      return null;
    }

    if (obj instanceof ButtonCombo) {
      return (ButtonCombo) obj;
    } else if (obj instanceof FingerCombo) {
      return ((FingerCombo) obj).getButtonCombo();
    }

    return null;
  }

  public String getSelectedComboStateString()
  {
    ButtonCombo activeCombo = getSelectedButtonCombo();
    ParsedChordDef activeChord = getSelectedChordDef();

    String info = "<html>";

    if ((activeChord == null) || activeChord.isEmptyChord() || (activeCombo == null)) {
      info += "<i>No Buttons Clicked</i>";
    } else {
      String sortedNotesStr;

      if (activeCombo.isEmpty()) {
        info += "Buttons: <i>None possible on this board</i>";
        sortedNotesStr = activeChord.toSortedNoteString(true);
      } else {
        info += "Buttons: ";
        info += "<b>" + activeCombo.toButtonListingString(true) + "</b>";
        sortedNotesStr = activeCombo.toSortedNoteString(true);
      }

      info += "<br/>Notes: ";
      info += "<b>" + sortedNotesStr + "</b>";
    }

    info += "</html>";
    return info;
  }

  public boolean editSelectedColumn(ParsedChordDef newDef)
  {
    return editSelectedColumn(newDef, false);
  }

  public boolean editSelectedColumn(ParsedChordDef newDef, boolean keepMatchedChords)
  {
    int index = getSelectedColumn();
    if (index >= 0) {
      matchingChordStore.setValid(keepMatchedChords);
      boolean updated = editColumn(index, newDef);
      if (!updated) {
        matchingChordStore.resetIfNotValid();
      }
      return updated;
    }

    return false;
  }

  public void resetColumns(boolean addFirst)
  {
    this.tableColumns.clear();

    if (addFirst) {
      this.selComboModel.setAnchorSelectionIndex(0);
      this.addColumn(0);
    } else {
      computeSeqs(-1);
      setSelectedSeq(-1);
    }
  }

  public void shrinkToFirst(boolean useBlank)
  {
    if (tableColumns.isEmpty()) {
      addColumn(useBlank ? ParsedChordDef.newEmptyChordDef()
              : ParsedChordDef.newDefaultChordDef(), 0);
      return;
    }

    while (tableColumns.size() > 1) {
      tableColumns.remove(1);
    }

    if (!useBlank && getChordDef(0).isEmptyChord()) {
      getColumn(0).setModelIndex(0);
      this.editColumn(0, ParsedChordDef.newDefaultChordDef());
    } else {
      syncModelToView(0, 0);
    }
  }

  public void removeSelectedColumn()
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

  public void populateFromText(String text, boolean removeDupNotes, Note rootNote)
  {
    StringParser parser = new StringParser(text);
    Vector<ParsedChordDef> chords = ChordParser.parseChords(parser, removeDupNotes);

    int colSel = this.getSelectedColumn();

    this.tableColumns.clear();

    isPopulating = true;

    for (int i = 0; i < chords.size(); i++) {
      this.addColumn(chords.elementAt(i), i);
    }

    if (rootNote != null) {
      transposeAllFromFirstColumn(rootNote);
    }
    isPopulating = false;

    computeSeqs(colSel);
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

    transposeAllFromColumn(newNote, column);
  }

  public void transposeAllFromFirstColumn(Note newNote)
  {
    TableColumn column = this.getColumn(0);

    transposeAllFromColumn(newNote, column);
  }

  public void transposeAllFromColumn(Note newNote, TableColumn column)
  {
    if (column == null) {
      return;
    }

    // Find interval diff between new and old and apply to all
    ParsedChordDef existingDef = (ParsedChordDef) column.getHeaderValue();

    Interval transDiff = newNote.diff(existingDef.rootNote);

    transposeAllByInterval(transDiff);
  }

  public void transposeAllByInterval(Interval transDiff)
  {
    ParsedChordDef.transposeAllByInterval(this.getChordSeqIter(), transDiff);
    recomputeSeqs();
  }

  private boolean editColumn(int index, ParsedChordDef newChordDef)
  {
    TableColumn column = this.getColumn(index);

    if (newChordDef == null) {
      return false;
    }

    ParsedChordDef oldChordDef = (ParsedChordDef) column.getHeaderValue();
    column.setHeaderValue(newChordDef);

    if (!oldChordDef.equalForRecompute(newChordDef)) {
      computeSeqs(index);
      return true;
    } else {
      this.fireColumnMarginChanged();
      return false;
    }
  }

  public ParsedChordDef getChordDef(int index)
  {
    if ((index >= 0) && (index < getColumnCount())) {
      return (ParsedChordDef) getColumn(index).getHeaderValue();
    } else {
      return null;
    }
  }

  public ParsedChordDef getSelectedChordDef()
  {
    int index = getSelectedColumn();
    return ((index >= 0) && (index < getColumnCount()) ? getChordDef(index) : null);
  }

  SeqDataModel getDataModel()
  {
    return dataModel;
  }

  SeqRowHeaderData getRowHeaderDataModel()
  {
    return rowHeaderDataModel;
  }

  public ListSelectionModel getRowSelModel()
  {
    return this.rowSelModel;
  }

//  public void addTableAndColumnListener(SeqTableEventAdapter listener)
//  {
//    this.addColumnModelListener(listener);
//    this.getDataModel().addTableModelListener(listener);
//  }
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
    chordSeqIter.reset();
    return ParsedChordDef.toString(chordSeqIter, false);
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

  void computeSeqs(int colSel)
  {
    if (renderBoard == null) {
      return;
    }

    if (isPopulating) {
      return;
    }

    int rowSel = rowSelModel.getAnchorSelectionIndex();

    chordSeqIter.reset();
    music.BassBoard currBassBoard = renderBoard.getBassBoard();
    allComboSeqs = searcher.parseSequence(currBassBoard, chordSeqIter);
    assert (allComboSeqs != null);

    if (optFingerSearch) {
      fingerComboSeqs = fingerSearcher.findAllFingers(allComboSeqs);
      currSeqArray = fingerComboSeqs;
    } else {
      currSeqArray = allComboSeqs;
    }

    // ...
    this.matchingChordStore.resetIfNotValid();

    ListSelChangeListener.setIsClearing(true);
    
    rowHeaderDataModel.fireListDataChanged();
    dataModel.fireTableDataChanged();

    ListSelChangeListener.setIsClearing(false);

    if ((colSel < 0) || (colSel >= getColumnCount())) {
      colSel = 0;
    }

    selComboModel.setSelectionInterval(colSel, colSel);

    // Set Row Selection
    rowSel = updateRowSel(rowSel);
    rowSelModel.setSelectionInterval(rowSel, rowSel);
  }

  public void recomputeSeqs()
  {
    computeSeqs(getSelectedColumn());
  }

  public void selPrefSeq()
  {
    int index = findRowForPrefSeq();

    if (index >= 0) {
      rowSelModel.setSelectionInterval(index, index);
    }
  }

  private int updateRowSel(int prevRow)
  {
    int index = findRowForPrefSeq();

    if (index < 0) {
      index = prevRow;
    }

    if ((index >= dataModel.getRowCount()) || (index < 0)) {
      index = (dataModel.getRowCount() >= 0) ? 0 : -1;
    }

    return index;
  }

  public void clearPrefSeq()
  {
    boolean clearedAny = false;

    for (int col = 0; col < this.getColumnCount(); col++) {
      ParsedChordDef def = this.getChordDef(col);
      if (def.getPrefCombo() != null) {
        def.setPrefCombo(null);
        clearedAny = true;
      }
    }

    if (clearedAny) {
      this.recomputeSeqs();
    }
  }

  private int findRowForPrefSeq()
  {
    boolean hasPrefCombo = false;

    for (int row = 0; row < allComboSeqs.length; row++) {

      boolean rowMatches = true;

      if (allComboSeqs[row].getNumCombos() < this.getColumnCount()) {
        continue;
      }

      for (int col = 0; col < this.getColumnCount(); col++) {

        ButtonCombo matchCombo = this.getChordDef(col).getPrefCombo();

        if (matchCombo == null) {
          continue;
        }

        hasPrefCombo = true;

        if (!allComboSeqs[row].getCombo(col).equals(matchCombo)) {
          rowMatches = false;
        }
      }

      if (!hasPrefCombo) {
        return -1;
      }

      if (rowMatches) {
        //rowSelModel.setSelectionInterval(row, row);
        return row;
      }
    }

    //rowSelModel.clearSelection();

    //rowSelModel.setSelectionInterval(-1, -1);
    return -1;
  }

  public ChordIterator getChordSeqIter()
  {
    return new ChordIterator();
  }

  public class ChordIterator implements ListIterator<ParsedChordDef>
  {

    private short index = 0;
    private short setIndex = -1;

    public void reset()
    {
      index = 0;
    }

    @Override
    public void add(ParsedChordDef e)
    {
      throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int nextIndex()
    {
      return index;
    }

    @Override
    public boolean hasNext()
    {
      return (index < SeqColumnModel.this.getColumnCount());
    }

    @Override
    public ParsedChordDef next()
    {
      setIndex = index;
      return SeqColumnModel.this.getChordDef(index++);
    }

    @Override
    public int previousIndex()
    {
      return (index - 1);
    }

    @Override
    public boolean hasPrevious()
    {
      return (index >= 1);
    }

    @Override
    public ParsedChordDef previous()
    {
      setIndex = index;
      return SeqColumnModel.this.getChordDef(--index);
    }

    @Override
    public void set(ParsedChordDef chordDef)
    {
      if ((setIndex >= 0) && (setIndex <= SeqColumnModel.this.getColumnCount())) {
        SeqColumnModel.this.getColumn(setIndex).setHeaderValue(chordDef);
      }
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException("Not supported");
    }
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
