/**
 * 
 */
package music;

public class ButtonCombo
{
	private BassBoard.Pos[] pos;
	BassBoard.Pos center;
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
		String str = "<";
		for (int i = 0; i < pos.length; i++)
		{
			BassBoard.Pos currPos = pos[i];
			str += "<" + currPos.col + ", " + currPos.row + ">";
		}
		
		str += ">";
		return str;
	}
	
	public int evalHeur(BassBoard.Pos boardCenter)
	{
		return evalHeur(boardCenter, null, null);
	}
	
	public int evalHeur(BassBoard.Pos boardCenter,
						BassBoard.Pos gMaxP,
						BassBoard.Pos gMinP)
	{
		if ((heur != 0) && (gMaxP == null && gMinP == null))
		{
			return heur;
		}
		
		heur = 0;
		
		center = new BassBoard.Pos(0, 0);
		BassBoard.Pos minP = BassBoard.Pos.maxPos();
		BassBoard.Pos maxP = BassBoard.Pos.minPos();
		
		for (int i = 0; i < pos.length; i++)
		{
			BassBoard.Pos currPos = pos[i];
			center.add(currPos);
			minP.min(currPos);
			maxP.max(currPos);
		}
		
		if (pos.length > 0)
		{
			center.divide(pos.length);
			center.subtract(boardCenter);	
		}
		
		if (gMaxP != null && gMinP != null)
		{
			gMaxP.max(maxP);
			gMinP.min(minP);
		}
		
		maxP.subtract(minP);
		
		heur += maxP.absValue();
	//	heur += center.absValue();
		heur += (pos.length - 1) * 50; 
		
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