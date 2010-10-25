/**
 * 
 */
package music;

import java.util.BitSet;

public class ButtonCombo
{
	final private BassBoard.Pos[] pos;
  //final private Chord.Mask chordMask;
	GeoPos center;
	int heur = 0;
	Hash hash;
	
	class Hash
	{
		BitSet bitset;
		
		Hash(ButtonCombo combo, int numRows, int numCols)
		{
      bitset = new BitSet(numRows * numCols);

			for (int i = 0; i < pos.length; i++)
			{
        bitset.set((pos[i].row * numCols) + pos[i].col);
			}
		}

    // Check if other hash is contained in this hash
    // For all bits set in other hash, bit must be set here, otherwise false
		boolean contains(Hash other)
		{
			for (int i = other.bitset.nextSetBit(0); i >= 0; i = other.bitset.nextSetBit(i + 1))
      {
        if (!bitset.get(i)) {
          return false;
        }
      }

      return true;
		}

    boolean contains(int row, int col, BassBoard board)
    {
      return bitset.get((row * board.getCols()) + col);
    }
	}
	
	ButtonCombo(BassBoard.Pos singlePos)
	{
		pos = new BassBoard.Pos[1];
		pos[0] = singlePos;
	}
		
	ButtonCombo(BassBoard.Pos[] newPos)
	{
		pos = newPos;
	}

  @Override
	public String toString()
	{
		String str = this.heur + ": <";
		for (int i = 0; i < pos.length; i++)
		{
			BassBoard.Pos currPos = pos[i];
			str += "<" + currPos.col + ", " + currPos.row + ">";
		}
		
		str += ">";
		return str;
	}

  public String toButtonListingString(BassBoard board)
	{
    String str = "";

    // Print in reverse as we were built depth first, so top button is last
		for (int i = pos.length - 1; i >=0; i--)
		{
      //str += (board.isSingleBassRow(pos[i].row) ? "Bass " : "Chord ");
			str += board.getChordName(pos[i].row, pos[i].col);
      if (i > 0)
        str += " + ";
		}

    return str;
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
	
	int evalHeur(final BassBoard.Pos boardCenter)
	{
		return evalHeur(boardCenter, null, null);
	}
	
	int evalHeur(final BassBoard.Pos boardCenter,
						GeoPos boundsMax,
						GeoPos boundsMin)
	{
		if ((heur != 0) && (boundsMax == null && boundsMin == null))
		{
			return heur;
		}
		
		heur = 1;
		
		center = GeoPos.zero();
		GeoPos minP = GeoPos.maxPos();
		GeoPos maxP = GeoPos.minPos();
    GeoPos currPos = GeoPos.zero();

    // Compute center and corners of the buttoncombo
		for (int i = 0; i < pos.length; i++)
		{
			currPos.set(pos[i], boardCenter);
			center.add(currPos);
			minP.min(currPos);
			maxP.max(currPos);
		}
		
		if (pos.length > 0)
		{
			center.divide(pos.length);
		}

    // Update max min bounds, if provided
		if (boundsMax != null && boundsMin != null)
		{
			boundsMax.max(maxP);
			boundsMin.min(minP);
		}
		
		maxP.subtract(minP);

    // Span width + height
		heur += maxP.absValue();
		//heur += center.absValue();
		heur += (pos.length - 1) * (50 * GeoPos.GRID_SCALE);
		
		return heur;
	}
	
	Hash getHash(BassBoard board)
	{
		if (hash == null)
			hash = new Hash(this, board.getRows(), board.getCols());
		
		return hash;
	}
	
	boolean contains(ButtonCombo other, BassBoard board)
	{
		return getHash(board).contains(other.getHash(board));
	}

  boolean isUsingBassOnly(BassBoard board)
  {
    for (int i = 0; i < pos.length; i++)
    {
      if (!board.isSingleBassRow(pos[i].row))
        return false;
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
	
	public boolean hasButton(int row, int col, BassBoard board)
	{
    //Using hash version
    return getHash(board).contains(row, col, board);
    //Compare version
//		for (int i = 0; i < pos.length; i++)
//		{
//			BassBoard.Pos currPos = pos[i];
//			if ((currPos.col == col) && (currPos.row == row))
//			{
//				return true;
//			}
//		}
//
//		return false;
	}
}