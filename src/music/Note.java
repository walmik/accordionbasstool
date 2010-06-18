package music;
import util.Main;

/**
 * 
 */

public class Note
{	
	final static int NUM_HALFSTEPS = 12;
	final static int NUM_NOTES = 7;
	
	enum ScaleNote
	{
		C(0, 0),
		D(1, 2),
		E(2, 4),
		F(3, 5),
		G(4, 7),
		A(5, 9),
		B(6, 11);

		short halfStep;
		short scaleDist;

		private ScaleNote(int dist, int hs)
		{
			scaleDist = (short)dist;			
			halfStep = (short)hs;
		}
		
		public String toString()
		{

			switch (this)
			{
			case A:
				return "A";
			case B:
				return "B";
			case C:
				return "C";
			case D:
				return "D";
			case E:
				return "E";
			case F:
				return "F";
			case G:
				return "G";
			default:
				return "A";
			}
		}
		
		public static ScaleNote fromString(Main.StringParser parser)
		{
			int theChar = 0;
			
			String input = parser.input();
			
			if (input.length() > 0)
			{
				theChar = input.charAt(0);
				parser.incOffset(1);
			}
			
			switch (theChar)
			{
			case 'A':
			case 'a':
				return A;
				
			case 'B':
			case 'b':
				return B;
				
			case 'C':
			case 'c':
				return C;
				
			case 'D':
			case 'd':
				return D;				
				
			case 'E':
			case 'e':
				return E;
				
			case 'F':
			case 'f':
				return F;
				
			case 'G':
			case 'g':
				return G;

			default:
				parser.incOffset(-1);
				return A;
			}
		}
	}

	ScaleNote note;
	short sharpsOrFlats;

	public Note(ScaleNote n, int sof)
	{
		note = n;
		sharpsOrFlats = (short)sof;
	}

	private int halfStepValue()
	{
		return note.halfStep + sharpsOrFlats;
	}
	
	public int value()
	{
		return posmod(halfStepValue(), NUM_HALFSTEPS);	
	}

	public boolean equals(Note other)
	{
		return (value() == other.value());
	}
	
	public Interval diff(Note other)
	{
		return new Interval(halfStepValue() - other.halfStepValue(),
							note.scaleDist - other.note.scaleDist);
	}
	
	public Note add(Interval ival)
	{
		ScaleNote[] notes = ScaleNote.values();
		int newHalfStep = halfStepValue() + ival.interval;
		
		int newScaleNote = posmod(note.scaleDist + ival.scaleDist, NUM_NOTES);
		ScaleNote newNote = notes[newScaleNote];
		
		return new Note(newNote, signmod(newHalfStep - newNote.halfStep, NUM_HALFSTEPS));
	}
	
	public Note sub(Interval ival)
	{
		return add(ival.scale(-1));
	}
	
	public static String printNote(ScaleNote note, int sharpsOrFlats)
	{
		String base = note.toString();
		
		if (sharpsOrFlats > 0)
		{
			for (int i = 0; i < sharpsOrFlats; i++)
			{
				base += "#";
			}
		}
		else
		{
			for (int i = sharpsOrFlats; i < 0; i++)
			{
				base += "b";
			}
		}
		
		return base;		
	}

	public String toString()
	{
		if (sharpsOrFlats >= 2)
		{
			return printNote(ScaleNote.values()[posmod(note.scaleDist + 1, NUM_NOTES)],
							 sharpsOrFlats - 2);
		}
		else if (sharpsOrFlats <= -2)
		{
			return printNote(ScaleNote.values()[posmod(note.scaleDist - 1, NUM_NOTES)], 
							 sharpsOrFlats + 2);			
		}
		else
		{
			return printNote(note, sharpsOrFlats);
		}
	}
	
	public static Note fromString(Main.StringParser parser)
	{
		int initialParserOffset = parser.getOffset();
		ScaleNote scaleNote = ScaleNote.fromString(parser);
		
		// if parser offset wasn't incremented, a valid scale note
		// was not read, so there's not a valid note to be parsed
		if (parser.getOffset() == initialParserOffset)
		{
			return null;
		}
		
		String input = parser.input();

		int sharpsOrFlats = 0;
		int offset;

		for (offset = 0; offset < input.length(); offset++)
		{
			int nextChar = input.charAt(offset);
			if ((nextChar == '#') && (sharpsOrFlats >= 0))
			{
				sharpsOrFlats++;
			}
			else if ((nextChar == 'b') && (sharpsOrFlats <= 0))
			{
				sharpsOrFlats--;
			}
			else
			{
				break;
			}
		}
		
		parser.incOffset(offset);
		
		return new Note(scaleNote, sharpsOrFlats);
	}
	
	// Range of [0, mod-1)
	
	public static int posmod(int value, int mod)
	{
		int m = value % mod;
		return (m + mod) % mod;
	}
	
	// Range of [-mod/2, mod/2)
	
	public static int signmod(int value, int mod)
	{
		int m = value % mod;
		return m - (m/(mod/2)) * mod;
	}
}