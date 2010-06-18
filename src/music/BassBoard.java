package music;


public class BassBoard 
{
	enum RowType
	{
		CB_MAJ3,
		ROOT,
		MAJ,
		MIN,
		MAJ7,
		DIM,
		CB_MIN3;
	};
	
	static class Pos
	{
		static Pos minPos()
		{
			return new Pos(-Integer.MAX_VALUE, -Integer.MAX_VALUE);
		}
		
		static Pos maxPos()
		{
			return new Pos(Integer.MAX_VALUE, Integer.MAX_VALUE);
		}
		
		public int row;
		public int col;
		
		Pos(int r, int c)
		{
			row = r;// - 1;
			col = c;// - (board.getCols() - 1) / 2;
		}
		
		int rowGeo()
		{
			return row;
		}
		
		int colGeo()
		{
			return col;// + (row - 1);
		}
		
		void add(Pos pos)
		{
			row += pos.row;
			col += pos.col;
		}
		
		void subtract(Pos pos)
		{
			row -= pos.row;
			col -= pos.col;
		}
		
		void divide(int scale)
		{
			row /= scale;
			col /= scale;
		}
		
		void max(Pos pos)
		{
			row = Math.max(row, pos.row);
			col = Math.max(col, pos.col);
		}
		
		void min(Pos pos)
		{
			row = Math.min(row, pos.row);
			col = Math.min(col, pos.col);
		}
		
		int absValue()
		{
			return Math.abs(row) + Math.abs(col);
		}
		
		int manDistTo(BassBoard.Pos another)
		{
			int x = Math.abs(row - another.row);
			int y = Math.abs(col - another.col);
			return Math.max(x, y);
		}
	}
	
	public static BassBoard bassBoard120()
	{
		RowType[] layout = 
		   {RowType.CB_MAJ3, 
			RowType.ROOT, 
			RowType.MAJ,
			RowType.MIN,
			RowType.MAJ7,
			RowType.DIM};
		
		return new BassBoard(layout, 20);
	}
	
	public static BassBoard bassBoardNoteRowOnly()
	{
		RowType[] layout = 
		   {RowType.CB_MAJ3, 
			RowType.ROOT,
			RowType.MAJ};
		
		return new BassBoard(layout, 20);
	}
	
	public static BassBoard bassBoardDebug()
	{
		RowType[] layout = 
		   {RowType.CB_MAJ3, 
			RowType.ROOT, 
			RowType.MAJ,
			RowType.MIN,
			RowType.MAJ7,
			RowType.DIM};
		
		return new BassBoard(layout, new Note(Note.ScaleNote.C, 0), 20);
	}

	
	public static BassBoard bassBoard32()
	{
		RowType[] layout = 
		   {RowType.CB_MAJ3, 
			RowType.ROOT, 
			RowType.MAJ,
			RowType.MIN};
		
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
		for (int i = 0; i < rowLayout.length; i++)
		{
			if (rowLayout[i] == RowType.ROOT)
				return i;
		}
		
		return 0;
	}
	
	public boolean isSingleBassRow(int row)
	{
		if (row < rowLayout.length)
		{
			return (rowLayout[row] == RowType.ROOT) || (rowLayout[row] == RowType.CB_MAJ3);
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
		
		for (int r = 0; r < getRows(); r++)
		{
			for (int c = 0; c < getCols(); c++)
			{
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
		if (row < 0 || row >= rowLayout.length)
		{
			throw new RuntimeException("OOB Row: " + row);
		}
		
		if (col < 0 || col >= cols)
		{
			throw new RuntimeException("OOB Col: " + col);
		}		
	}
	
	public Chord getChordFor(Note note, RowType row)
	{
		switch (row)
		{
		case CB_MAJ3:
			return new Chord(note.add(Interval.M3), 2);
			
		case CB_MIN3:
			return new Chord(note.add(Interval.m3), 2);
			
		case ROOT:
			return new Chord(note, 2);
			
		case MAJ:
			return new Chord(note, Chord.MAJOR);
			
		case MIN:
			return new Chord(note, Chord.MINOR);
			
		case MAJ7:
			return new Chord(note, Chord.DOM7);
			
		case DIM:
			return new Chord(note, Chord.DIM7);
			
		default:
			throw new RuntimeException("Invalid Row Specified: " + row);
		}
	}
	
	public String getChordName(int row, int col)
	{
		verifyRowCol(row, col);
		RowType rowType = rowLayout[row];
		
		Note rootNote = getNoteAt(col);
		
		String str = rootNote.toString();
		
		switch (rowType)
		{
		case CB_MAJ3:
			str = rootNote.add(Interval.M3).toString();
			break;
			
		case CB_MIN3:
			str = rootNote.add(Interval.m3).toString();
			break;
			
		case MAJ:
			str += "M";
			break;
			
		case MIN:
			str += "m";
			break;
			
		case MAJ7:
			str += "7";
			break;
			
		case DIM:
			str += "\u00B0"; //degree sign
			break;
		}
		
		return str;
	}
}
