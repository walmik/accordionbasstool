package music;

public class Chord 
{
	final static Interval[] MAJOR = {Interval.M3, Interval.m3};
	final static Interval[] MINOR = {Interval.m3, Interval.M3};
	final static Interval[] DOM7 =  {Interval.M3, Interval.Dim5};
	final static Interval[] DIM7 =  {Interval.m3, Interval.Dim5};
	
	public final Note[] notes;
	
	static class Mask
	{
		private int value = 0;
		
		Mask()  			{ 	value = 0; 		}
		Mask(int val) 		{ 	value = val; 	}
		
		Mask(Chord chord)
		{
			value = 0;
			
			for (int i = 0; i < chord.notes.length; i++)
			{
				int bit = (1 << chord.notes[i].value());
				if ((value & bit) != 0)
					bit <<= Note.NUM_HALFSTEPS;
				
				value |= bit;
			}
		}
		
		static boolean contains(int a, int b)
		{
			return (a | b) == a;
		}
		
		boolean contains(Mask otherMask)
		{
			return contains(value, otherMask.value);
		}
		
//		boolean containsSingleRegister(Mask otherMask)
//		{
//			int stripLowRegMask = otherMask.value % (1 << Note.NUM_HALFSTEPS);
//			return contains(value, stripLowRegMask);
//		}
		
		boolean equals(Mask otherMask)
		{
			return (value == otherMask.value);
		}
		
		boolean equals(Chord otherChord)
		{
			return equals(otherChord.getChordMask());
		}
		
		Mask concat(Mask otherMask)
		{
			return new Mask(value | otherMask.value);
		}
		
		void doubleNote(Note note)
		{
			value |= (1 << (note.value() + Note.NUM_HALFSTEPS));
		}
		
		boolean hasRootBassReq()
		{
			return (value >> Note.NUM_HALFSTEPS) != 0;
		}
		
		public void unmaskRegister(Mask otherMask)
		{
			int res = value & otherMask.value;
			if (res != 0)
				value = res;
		}
	}
	
	private Mask mask;
	
	public Chord(Note singleN, int dupCount)
	{
		notes = new Note[dupCount];
		
		initBassNote(0, singleN, dupCount);
	}
	

	
	public Chord(Note[] n)
	{
		notes = n;
	}
	
	public Chord(Note note, Interval[] ivals)
	{
		notes = new Note[ivals.length + 1];
		initFromIval(0, note, ivals);
	}
	
	public Chord(Note note, Interval[] ivals, Note bassNote, int dupCount)
	{
		assert(bassNote != null || dupCount == 0);
		
		notes = new Note[ivals.length + 1 + dupCount];
		
		initBassNote(0, bassNote, dupCount);
		initFromIval(dupCount, note, ivals);
	}
	
	private void initBassNote(int offset, Note singleN, int count)
	{
		for (int i = 0; i < count; i++)
		{
			notes[offset + i] = singleN;
		}	
	}
	
	private void initFromIval(int offset, Note note, Interval[] ivals)
	{
		notes[offset] = note;
		
		for (int i = 0; i < ivals.length; i++)
		{
			notes[offset + i + 1] = notes[offset + i].add(ivals[i]);
		}

	}
	public Chord(Chord one, Chord two)
	{
		Mask onemask = one.getChordMask();
		Mask twomask = two.getChordMask();
		
		int newMask = (~onemask.value | twomask.value);
		
		int numNotes = one.notes.length;
		
		// See which notes in two are not in one and add them
		// to new note list
		for (int i = 0; i < two.notes.length; i++)
		{
			if ((newMask & (1 << two.notes[i].value())) != 0)
			{
				numNotes++;
			}
		}
		
		notes = new Note[numNotes];
		
		System.arraycopy(one.notes, 0, notes, 0, one.notes.length);
		
		// Fill only the second chord's notes that don't overlap
		int count = one.notes.length;
		
		for (int i = 0; i < two.notes.length; i++)
		{
			if ((newMask & (1 << two.notes[i].value())) != 0)
			{
				notes[count++] = two.notes[i];
			}
		}
		
		assert(count == numNotes);
	}
		
	public void transpose(Interval ival)
	{
		for (int i = 0; i < notes.length; i++)
		{
			notes[i] = notes[i].add(ival);
		}
		mask = null;
	}
	
	public boolean isSingleNote()
	{
		return notes.length == 1;
	}
	
	public Note getRootNote()
	{
		return notes[0];
	}
	
	public boolean equals(Chord other)
	{
		return (getChordMask().equals(other.getChordMask()));
	}
	
//	public boolean contains(Chord other)
//	{
//		return getChordMask().contains(other.getChordMask());
//	}

	// Integer bitmask representing the chord
	// Bits 0-11 are set if the chord has each respective semitone 0-11
	// Compute on first use
	
	public Mask getChordMask()
	{
		if (mask == null)
		{
			mask = new Mask(this);
		}
		
		return mask;
	}
	
	public String toString()
	{
		String str = "";
		
		for (int i = 0; i < notes.length; i++)
		{
			str += notes[i].toString();
		}
		
		return str;
	}
}

