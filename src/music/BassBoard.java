package music;

public class BassBoard
{

  public enum RowType
  {

    BassMaj3("Counter-Bass Major", true),
    BassRoot("Bass Root", true),
    ChordMajor("Major Chord", false),
    ChordMinor("Minor Chord", false),
    Chord7th("7th Chord", false),
    ChordDim("Diminished Chord", false),
    BassMin3("Counter-Bass Minor", true);

    
    final String name;
    final boolean singleNote;

    RowType(String name, boolean single)
    {
      this.name = name;
      this.singleNote = single;
    }

    @Override
    public String toString()
    {
      return name;
    }

    public static RowType fromString(String key)
    {
      try {
        return RowType.valueOf(key);
      } catch (IllegalArgumentException il) {
        return BassRoot;
      }
    }

    Chord getChord(Note note)
    {
      switch (this) {
        case BassMaj3:
          return new Chord(note.add(Interval.M3), true);

        case BassMin3:
          return new Chord(note.add(Interval.m3), true);

        case BassRoot:
          return new Chord(note, true);

        case ChordMajor:
          return new Chord(note, ChordRegistry.MAJOR);

        case ChordMinor:
          return new Chord(note, ChordRegistry.MINOR);

        case Chord7th:
          return new Chord(note, ChordRegistry.DOM);

        case ChordDim:
          return new Chord(note, ChordRegistry.DIM);

        default:
          throw new RuntimeException("Invalid Row Specified: " + toString());
      }
    }

    String getChordName(Note rootNote)
    {
      switch (this) {
        case BassMaj3:
          return rootNote.add(Interval.M3).toString();

        case BassMin3:
          return rootNote.add(Interval.m3).toString();

        case ChordMajor:
          return rootNote.toString() + "M";

        case ChordMinor:
          return rootNote.toString() + "m";

        case Chord7th:
          return rootNote.toString() + "7";

        case ChordDim:
          return rootNote.toString() + "\u00B0"; //degree sign

        default:
          return rootNote.toString();
      }
    }
  }

  public static class Pos
  {

    final int row;
    final int col;

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
  final protected RowType[] rowLayout;
  final int cols;
  final Pos centerPos;
  final protected Note middleNote;

  public int getRows()
  {
    return rowLayout.length;
  }

  public int getCols()
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
      if (rowLayout[i] == RowType.BassRoot) {
        return i;
      }
    }

    return 0;
  }

  public boolean isSingleBassRow(int row)
  {
    if (row < rowLayout.length) {
      return rowLayout[row].singleNote;
    }

    return false;
  }

  protected BassBoard(RowType[] layout, Note center, int numCols)
  {
    rowLayout = layout;
    cols = numCols;
    middleNote = center;

    centerPos = new Pos(findRootRow(), (cols - 1) / 2);
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
    Chord.Mask[][] masks = new Chord.Mask[getRows()][getCols()];

    for (int r = 0; r < getRows(); r++) {
      for (int c = 0; c < getCols(); c++) {
        masks[r][c] = getChordAt(r, c).getChordMask();
      }
    }

    return masks;
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

  public Chord getChordFor(Note note, RowType row)
  {
    return row.getChord(note);
  }

  public String getChordName(int row, int col)
  {
    verifyRowCol(row, col);
    RowType rowType = rowLayout[row];

    Note rootNote = getNoteAt(col);

    return rowType.getChordName(rootNote);
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
