/**
 * 
 */
package music;

public class ButtonCombo
{
	private BassBoard.Pos[] pos;
	GeoPos center;
	int heur = 0;
	Hash hash;
	
	class Hash
	{
		int hash;
		
		Hash()
		{
			hash = 0;
		}
		
		Hash(ButtonCombo combo)
		{
			for (int i = 0; i < pos.length; i++)
			{
				hash |= (1<<pos[i].row);
				hash |= (1<<pos[i].col + BassBoard.RowType.values().length);
			}
		}
		
		boolean contains(Hash other)
		{
			return Chord.Mask.contains(hash, other.hash);
		}
	}
	
	ButtonCombo(BassBoard.Pos singlePos)
	{
		pos = new BassBoard.Pos[1];
		pos[0] = singlePos;
	}
	
//	ButtonCombo(Chord.Mask mask)
//	{
//		pos = new Vector<BassBoard.Pos>();
//		//chordMask = new Chord.Mask();
//	}
	
	ButtonCombo(BassBoard.Pos[] newPos)
	{
		pos = newPos;
	}
	
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
	
	public int evalHeur(final BassBoard.Pos boardCenter)
	{
		return evalHeur(boardCenter, null, null);
	}
	
	public int evalHeur(final BassBoard.Pos boardCenter,
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
	
	Hash getHash()
	{
		if (hash == null)
			hash = new Hash(this);
		
		return hash;
	}
	
	boolean contains(ButtonCombo other)
	{
		return getHash().contains(other.getHash());
		
//		if (other.pos.length > pos.length)
//			return false;
//		
//		for (int i = 0; i < other.pos.length; i++)
//		{
//			boolean found = false;
//			
//			for (int j = 0; j < pos.length; j++)
//			{
//				if (other.pos[i].equals(pos[j]))
//				{
//					found = true;
//					break;
//				}
//			}
//			
//			if (!found)
//				return false;
//		}
//		
//		return true;
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
		for (int i = 0; i < pos.length; i++)
		{
			BassBoard.Pos currPos = pos[i];
			if ((currPos.col == col) && (currPos.row == row))
			{
				return true;
			}
		}
		
		return false;
	}
}