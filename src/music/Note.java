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

    @Override
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
		
		public static ScaleNote fromString(String input)
		{	
			if (input.length() < 1)
			{
				return null;
			}

      int theChar = input.charAt(0);
			
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
				return null;
			}
		}
	}

	ScaleNote note;
	short sharpsOrFlats;

  //Default note is C
  public Note()
  {
    this(ScaleNote.C, 0);
  }

	Note(ScaleNote n, int sof)
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

  public int getSharpOrFlat()
  {
    return sharpsOrFlats;
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
	
	static String printNote(ScaleNote note, int sharpsOrFlats, boolean html)
	{
    if (sharpsOrFlats > 0)
    {
      switch (note)
      {
        case B:
          note = ScaleNote.C;
          sharpsOrFlats--;
          break;

        case E:
          note = ScaleNote.F;
          sharpsOrFlats--;
          break;
      }
    }
    else if (sharpsOrFlats < 0)
    {
      switch (note)
      {
        case C:
          note = ScaleNote.B;
          sharpsOrFlats++;
          break;

        case F:
          note = ScaleNote.E;
          sharpsOrFlats++;
          break;
      }
    }
		
		String base = note.toString();

    if (html && (sharpsOrFlats != 0))
      base += "<sup>";

    if (sharpsOrFlats > 0)
		{
			for (int i = 0; i < sharpsOrFlats; i++)
			{
				//base += (html ? "\u266F" : "#");
        base += "#";
			}
		}
		else
		{
			for (int i = sharpsOrFlats; i < 0; i++)
			{
				//base += (html ? "\u266D" : "b");
        base += "b";
			}
		}

    if (html && (sharpsOrFlats != 0))
    {
      base += "</sup>";
    }

		
		return base;		
	}

  @Override
  public String toString()
  {
    return toString(2, false);
  }

  public String toString(boolean html)
  {
    return toString(2, html);
  }

  private String toString(int maxAccidental, boolean html)
	{
		if (sharpsOrFlats >= maxAccidental)
		{
			return printNote(ScaleNote.values()[posmod(note.scaleDist + 1, NUM_NOTES)],
							 sharpsOrFlats - 2, html);
		}
		else if (sharpsOrFlats <= -maxAccidental)
		{
			return printNote(ScaleNote.values()[posmod(note.scaleDist - 1, NUM_NOTES)], 
							 sharpsOrFlats + 2, html);
		}
		else
		{
			return printNote(note, sharpsOrFlats, html);
		}
	}
  
  static int lastParserOffset = 0;

  public static Note fromString(Main.StringParser parser)
  {
    Note note = fromString(parser.input());
    
    if (note != null)
      parser.incOffset(lastParserOffset);

    return note;
  }
	
	public static Note fromString(String input)
	{
		ScaleNote scaleNote = ScaleNote.fromString(input);

    if (scaleNote == null)
      return null;

		int sharpsOrFlats = 0;
		int offset;

		for (offset = 1; offset < input.length(); offset++)
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

    lastParserOffset = offset;
		
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