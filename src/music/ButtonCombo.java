/**
 * 
 */
package music;

import java.util.BitSet;

public class ButtonCombo
{
  final private BassBoard.Pos[] pos;
  final BassBoard board;

  private Chord.Mask allChordsMask = null;
  int boardDim = 0;
  GeoPos center;
  int heur = 0;
  Hash hash;
  GeoPos boundsMin, boundsMax;
  private Note lowestNote;
  boolean preferred = false;

  class Hash
  {

    BitSet bitset;

    Hash(int numRows, int numCols)
    {
      bitset = new BitSet(numRows * numCols);

      for (int i = 0; i < pos.length; i++) {
        bitset.set((pos[i].row * numCols) + pos[i].col);
      }
    }

    // Check if other hash is contained in this hash
    // For all bits set in other hash, bit must be set here, otherwise false
    boolean contains(Hash other)
    {
      for (int i = other.bitset.nextSetBit(0); i >= 0; i = other.bitset.nextSetBit(i + 1)) {
        if (!bitset.get(i)) {
          return false;
        }
      }

      return true;
    }

    boolean contains(int row, int col)
    {
      return bitset.get((row * board.getNumCols()) + col);
    }

    public boolean equals(Hash hash)
    {
      return bitset.equals(hash.bitset);
    }
  }

  public ButtonCombo(BassBoard.Pos singlePos, BassBoard board)
  {
    this.board = board;
    pos = new BassBoard.Pos[1];
    pos[0] = singlePos;
  }

  public ButtonCombo(BassBoard.Pos[] newPos, BassBoard board)
  {
    this.board = board;
    pos = newPos;
  }

  @Override
  public String toString()
  {
    String str = this.heur + ": <";
    for (int i = 0; i < pos.length; i++) {
      BassBoard.Pos currPos = pos[i];
      str += "<" + currPos.col + ", " + currPos.row + ">";
    }

    str += ">";
    return str;
  }

  public String getButtonNameAt(int index, boolean html)
  {
    return board.getChordName(pos[index], html);
  }

  public BassBoard getBoard()
  {
    return board;
  }

  public String toButtonListingString(boolean html)
  {
    String str = "";
    String space = (html ? "&nbsp;" : " ");

    // Print in reverse as we were built depth first, so top button is last
    for (int i = pos.length - 1; i >= 0; i--) {
      //str += (board.isSingleBassRow(pos[i].row) ? "Bass " : "Chord ");
      str += getButtonNameAt(i, html);
      if (i > 0) {
        str += space + "+" + space;
      }
    }

    return str;
  }

  public boolean isPreferred()
  {
    return this.preferred;
  }

  public void setPreferred(boolean b)
  {
    preferred = b;
  }

  public BassBoard.Pos getPos(int index)
  {
    return pos[index];
  }

  public BassBoard.Pos[] getAllPos()
  {
    return pos;
  }

  void setPos(int index, BassBoard.Pos newPos)
  {
    this.hash = null;
    this.heur = 0;
    this.lowestNote = null;
    this.boardDim = 0;
    pos[index] = newPos;
  }

  public int getLength()
  {
    return pos.length;
  }

//  public String toChordVoicingString(BassBoard board)
//	{
//    String str = "";
//
//		for (int i = 0; i < (Note.NUM_HALFSTEPS * 2); i++)
//		{
//      if (chordMask.contains(i)) {
////        if (!str.isEmpty()) {
////          str += " + ";
////        }
//        //str += Note.printNote(Note.ScaleNote.C, i % Note.NUM_HALFSTEPS, false);
//        str += "1";
//      } else {
//        str += "0";
//      }
//		}
//
//    return str;
//	}
  public boolean isAcceptable()
  {
    if (optMaxDistThreshold == 0) {
      return true;
    }

    return computeComboBounds() < optMaxDistThreshold;
  }

  public boolean isSingleBass()
  {
    if (pos.length != 1) {
      return false;
    }

    return board.isSingleBassRow(pos[0].row);
  }
  public static int optMaxDistThreshold = 5;

  public static void setMaxComboDistThreshold(int threshold)
  {
    optMaxDistThreshold = threshold;
  }

  private int computeComboBounds()
  {
    if (boardDim != 0) {
      return boardDim;
    }

    boundsMin = GeoPos.maxPos();
    boundsMax = GeoPos.minPos();
    GeoPos currPos = GeoPos.zero();
    center = GeoPos.zero();

    // Compute center and corners of the buttoncombo
    for (int i = 0; i < pos.length; i++) {
      currPos.set(pos[i], board.getCenter());
      boundsMin.min(currPos);
      boundsMax.max(currPos);
      center = center.add(currPos);
    }

    if (pos.length > 0) {
      center = center.divide(pos.length);
    }

    boardDim = boundsMax.manDistTo(boundsMin);
    return boardDim;
  }

  int evalHeur(final BassBoard.Pos boardCenter)
  {
    if ((heur != 0)) {
      return heur;
    }

    heur = 1;

//    center = GeoPos.zero();
//    GeoPos minP = GeoPos.maxPos();
//    GeoPos maxP = GeoPos.minPos();
//    GeoPos currPos = GeoPos.zero();
//
//    // Compute center and corners of the buttoncombo
//    for (int i = 0; i < pos.length; i++) {
//      currPos.set(pos[i], boardCenter);
//      center.add(currPos);
//      minP.min(currPos);
//      maxP.max(currPos);
//    }
//
//    if (pos.length > 0) {
//      center.divide(pos.length);
//    }
//
//    // Update max min bounds, if provided
//    if (boundsMax != null && boundsMin != null) {
//      boundsMax.max(maxP);
//      boundsMin.min(minP);
//    }
//
//    maxP.subtract(minP);

    // Span width + height
    heur += this.computeComboBounds() * 8;
    //heur += center.absValue();
    heur += (pos.length - 1) * 10;

    return heur;
  }

  Hash getHash()
  {
    if (hash == null) {
      hash = new Hash(board.getNumRows(), board.getNumCols());
    }

    return hash;
  }

  boolean contains(ButtonCombo other)
  {
    return getHash().contains(other.getHash());
  }

  boolean isUsingBassOnly()
  {
    for (int i = 0; i < pos.length; i++) {
      if (!board.isSingleBassRow(pos[i].row)) {
        return false;
      }
    }

    return true;
  }

//	ButtonCombo expand(BassBoard.Pos newPos, Chord newChord)
//	{
//		Chord.Mask newChordMask = chordMask.concat(newChord);
//		ButtonCombo combo = 
//			new ButtonCombo((Vector<BassBoard.Pos>)pos.clone(), newPos, newChordMask);
//		
//		return combo;
//	}
//	public int distanceTo(ButtonCombo prevCombo)
//	{
//		int colDelta = Math.abs(pos[0].colGeo() - prevCombo.pos[0].colGeo());
//		int rowDelta = Math.abs(pos[0].rowGeo() - prevCombo.pos[0].rowGeo());
//		return Math.max(rowDelta, colDelta);
//		//return (rowDelta * rowDelta) + (colDelta * colDelta);
//	}
  public boolean hasButton(int row, int col)
  {
    //Using hash version
    return getHash().contains(row, col);
  }
  
  public final static Note[] sortedNotes = new Note[Note.NUM_HALFSTEPS * 2];

  private void sortNotes()
  {
    allChordsMask = new Chord.Mask();

    for (int i = 0; i < pos.length; i++) {
      Chord theChord = board.getChordAt(pos[i]);
      allChordsMask.sortChordNotes(theChord, sortedNotes);
    }
  }

  public Note getLowestNote()
  {
    if (lowestNote == null) {
      for (int i = 0; i < sortedNotes.length; i++) {
        sortedNotes[i] = null;
      }

      sortNotes();

      for (int i = 0; i < sortedNotes.length; i++) {
        if (sortedNotes[i] != null) {
          lowestNote = sortedNotes[i];
          break;
        }
      }
    }

    return lowestNote;
  }

  public String toSortedNoteString(boolean html)
  {
    String str = "";

    for (int i = 0; i < sortedNotes.length; i++) {
      sortedNotes[i] = null;
    }

    sortNotes();

    for (int i = 0; i < sortedNotes.length; i++) {
      if (sortedNotes[i] == null) {
        continue;
      }

      if (str.length() > 0) {
        str += " + ";
      }
      str += sortedNotes[i].toString(html);

    }

    return str;
  }

  public Chord.Mask getChordMask()
  {
    if (this.allChordsMask == null) {
      sortNotes();
    }

    return allChordsMask;
  }

  @Override
  public boolean equals(Object object)
  {
    return equals((ButtonCombo)object);
  }

  public boolean equals(ButtonCombo combo)
  {
    if (combo == null) {
      return false;
    }
    
    return (getHash().equals(combo.getHash()));
  }
}
