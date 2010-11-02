package music;

public class BassBoard
{

  public enum RowType
  {
    BassRoot,
    BassMaj3,
    BassMin3,
    ChordMajor,
    ChordMinor,
    Chord7th,
    ChordDim,
  }
  

  public static class Pos
  {

    public final int row;
    public final int col;

    public Pos(int r, int c)
    {
      row = r;
      col = c;
    }

    public boolean equals(int r, int c)
    {
      return (row == r) && (col == c);
    }
  }

  public static BassBoard bassBoard120()
  {
    BoardRegistry.mainRegistry();
    
    RowType[] layout = {RowType.BassMaj3,
      RowType.BassRoot,
      RowType.ChordMajor,
      RowType.ChordMinor,
      RowType.Chord7th,
      RowType.ChordDim};

    return new BassBoard(layout, 20);
  }

  public static BassBoard bassBoardNoteRowOnly()
  {
    RowType[] layout = {RowType.BassMaj3,
      RowType.BassRoot,
      RowType.ChordMajor};

    return new BassBoard(layout, 20);
  }

  public static BassBoard bassBoardDebug()
  {
    RowType[] layout = {RowType.BassMaj3,
      RowType.BassRoot,
      RowType.ChordMajor,
      RowType.ChordMinor,
      RowType.Chord7th,
      RowType.ChordDim};

    return new BassBoard(layout, new Note(Note.ScaleNote.C, 0), 20);
  }

  public static BassBoard bassBoard32()
  {
    RowType[] layout = {RowType.BassMaj3,
      RowType.BassRoot,
      RowType.ChordMajor,
      RowType.ChordMinor};

    return new BassBoard(layout, 8);
  }

  final protected BoardRow[] rowLayout;
  final int cols;
  final Pos centerPos;
  final protected Note middleNote;

  public BoardRow getRow(int index)
  {
    assert(index < rowLayout.length);
    return rowLayout[index];
  }

  public int getNumRows()
  {
    return rowLayout.length;
  }

  public int getNumCols()
  {
    return cols;
  }

  public Pos getCenter()
  {
    return centerPos;
  }

  public int findRootRow()
  {
    for (int i = 0; i < rowLayout.length; i++) {
      if (rowLayout[i].name.equals(RowType.BassRoot.toString())) {
        return i;
      }
    }

    return 0;
  }

  public boolean isSingleBassRow(int row)
  {
    if (row < rowLayout.length) {
      return rowLayout[row].isSingleNote;
    }

    return false;
  }

  protected BassBoard(BoardRow[] rows, Note center, int numCols)
  {
    rowLayout = rows;
    cols = numCols;
    middleNote = center;

    centerPos = new Pos(findRootRow(), (cols - 1) / 2);
  }

  protected BassBoard(String[] layout, Note center, int numCols)
  {
    this(BoardRow.findRows(layout), center, numCols);
  }

  protected BassBoard(RowType[] layout, Note center, int numCols)
  {
    this(BoardRow.findRows(layout), center, numCols);
  }

  protected BassBoard(RowType[] layout, int numCols)
  {
    this(layout, new Note(Note.ScaleNote.C, 0), numCols);
  }

  public Note getNoteAt(int col)
  {
    int diffCol = col - ((cols - 1) / 2);

    //NoteValue rootNote = new NoteValue(NoteValue.Note.C, 0).add(Interval.P5.scale(diffCol));
    Note rootNote = middleNote.add(Interval.P5.scale(diffCol));

    return rootNote;
  }

//	public BassBoard.Pos getMiddleNotePos()
//	{
//		return new BassBoard.Pos(1, (cols - 1) / 2);
//	}
  Chord.Mask[][] buildChordMaskCache()
  {
    Chord.Mask[][] masks = new Chord.Mask[getNumRows()][getNumCols()];

    for (int r = 0; r < getNumRows(); r++) {
      for (int c = 0; c < getNumCols(); c++) {
        masks[r][c] = getChordAt(r, c).getChordMask();
      }
    }

    return masks;
  }

  public Chord getChordAt(Pos pos)
  {
    return getChordAt(pos.row, pos.col);
  }

  public Chord getChordAt(int row, int col)
  {
    verifyRowCol(row, col);

    Note rootNote = getNoteAt(col);

    return getChordFor(rootNote, rowLayout[row]);
  }

  private void verifyRowCol(int row, int col)
  {
    if (row < 0 || row >= rowLayout.length) {
      throw new RuntimeException("OOB Row: " + row);
    }

    if (col < 0 || col >= cols) {
      throw new RuntimeException("OOB Col: " + col);
    }
  }

  public Chord getChordFor(Note note, BoardRow row)
  {
    return row.getChord(note);
  }

  public String getChordName(Pos pos, boolean html)
  {
    return getChordName(pos.row, pos.col, html);
  }

  public String getChordName(int row, int col, boolean html)
  {
    verifyRowCol(row, col);
    BoardRow rowType = rowLayout[row];

    Note rootNote = getNoteAt(col);

    return rowType.getChordName(rootNote, html);
  }

  public Note getMinRootNote()
  {
    return getNoteAt(0);
  }

  public Note getMaxRootNote()
  {
    return getNoteAt(cols - 1);
  }
}
