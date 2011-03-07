package music.core;

/**
 * 
 */
public class Note
{

  public final static int NUM_HALFSTEPS = 12;
  public final static int NUM_NOTES = 7;

  enum ScaleNote
  {

    C(0, 0),
    D(1, 2),
    E(2, 4),
    F(3, 5),
    G(4, 7),
    A(5, 9),
    B(6, 11);
    final short halfStep;
    final short scaleDist;

    private ScaleNote(int dist, int hs)
    {
      scaleDist = (short) dist;
      halfStep = (short) hs;
    }

    public static ScaleNote fromString(String input)
    {
      if (input.length() < 1) {
        return null;
      }

      char theChar = Character.toUpperCase(input.charAt(0));

      for (ScaleNote scale : values()) {
        if (scale.toString().charAt(0) == theChar) {
          return scale;
        }
      }

      return null;
    }
  }

  private final static Note[] tuple(ScaleNote scale)
  {
    Note note = new Note(scale, 0);
    Note tup[] = {note, note};
    return tup;
  }

  private final static Note[] tuple(ScaleNote scaleSharp, ScaleNote scaleFlat)
  {
    Note tup[] = {new Note(scaleSharp, 1), new Note(scaleFlat, -1)};
    return tup;
  }

  final static Note[][] reverseMapping = {
    tuple(ScaleNote.C),
    tuple(ScaleNote.C, ScaleNote.D),
    tuple(ScaleNote.D),
    tuple(ScaleNote.D, ScaleNote.E),
    tuple(ScaleNote.E),
    tuple(ScaleNote.F),
    tuple(ScaleNote.F, ScaleNote.G),
    tuple(ScaleNote.G),
    tuple(ScaleNote.G, ScaleNote.A),
    tuple(ScaleNote.A),
    tuple(ScaleNote.A, ScaleNote.B),
    tuple(ScaleNote.B)
  };

  final ScaleNote note;
  final short sharpsOrFlats;
  boolean isBass = false;

  //Default note is C
  public Note()
  {
    this(ScaleNote.C, 0);
  }

  private Note(ScaleNote n, int sof)
  {
    note = n;
    sharpsOrFlats = (short) sof;
  }

  public Note getNote(int index, int flatBit)
  {
    assert ((index >= 0) && (index < reverseMapping.length));
    return reverseMapping[index][flatBit];
  }

  Note duplicate()
  {
    return new Note(this.note, this.sharpsOrFlats);
  }

  private int halfStepValue()
  {
    return note.halfStep + sharpsOrFlats;
  }

  public int value()
  {
    return posmod(halfStepValue(), NUM_HALFSTEPS);
  }

  public int getChordBitValue()
  {
    int bit = value();
    if (!isBass) {
      bit += NUM_HALFSTEPS;
    }
    return bit;
  }

  public int getSharpOrFlat()
  {
    return sharpsOrFlats;
  }

  @Override
  public boolean equals(Object other)
  {
    if (other == null) {
      return false;
    }

    return (value() == ((Note) other).value());
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

  public Note eharmonic()
  {
    if (sharpsOrFlats > 0) {
      return this.add(NamedInterval.m2.interval).sub(Interval.halfStep);
    } else if (sharpsOrFlats < 0) {
      return this.sub(NamedInterval.m2.interval).add(Interval.halfStep);
    } else {
      return this;
    }
  }

  public boolean isBassNote()
  {
    //return (octaveBit >= 0 && Chord.Mask.isLowerOctaveBit(this.octaveBit));
    return isBass;
  }

  static String printNote(ScaleNote note, int sharpsOrFlats, boolean html)
  {
    String base = note.toString();

    if (html && (sharpsOrFlats != 0)) {
      base += "<sup>";
    }

    if (sharpsOrFlats > 0) {
      for (int i = 0; i < sharpsOrFlats; i++) {
        //base += (html ? "\u266F" : "#");
        base += "#";
      }
    } else {
      for (int i = sharpsOrFlats; i < 0; i++) {
        //base += (html ? "\u266D" : "b");
        base += "b";
      }
    }

    if (html && (sharpsOrFlats != 0)) {
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
    Note note = normalize(maxAccidental);
    return printNote(note.note, note.sharpsOrFlats, html);
  }

  public Note normalize(int maxAccidental)
  {
    ScaleNote newNote = note;
    int newSOF = sharpsOrFlats;
    int noteChange = 0;

    do {
      noteChange = 0;

      switch (newNote) {
        case B:
        case E:
          if (newSOF >= 1) {
            newSOF--;
            noteChange++;
          }
          break;

        case C:
        case F:
          if (newSOF <= -1) {
            newSOF++;
            noteChange--;
          }
          break;
      }

      if (noteChange == 0) {
        if (newSOF >= maxAccidental) {
          newSOF -= maxAccidental;
          noteChange++;
        } else if (newSOF <= -maxAccidental) {
          newSOF += maxAccidental;
          noteChange--;
        }
      }

      if (noteChange != 0) {
        newNote = ScaleNote.values()[posmod(newNote.scaleDist + noteChange, NUM_NOTES)];
      }

    } while (noteChange != 0);

    return new Note(newNote, newSOF);
  }

  private static int lastParserOffset = 0;

  public static int getLastParserOffset()
  {
    return lastParserOffset;
  }

//  public static Note fromString(StringParser parser)
//  {
//    Note note = fromString(parser.input());
//
//    if (note != null) {
//      parser.incOffset(lastParserOffset);
//    }
//
//    return note;
//  }

  public static Note fromString(String input)
  {
    ScaleNote scaleNote = ScaleNote.fromString(input);

    if (scaleNote == null) {
      return null;
    }

    int sharpsOrFlats = 0;
    int offset;

    for (offset = 1; offset < input.length(); offset++) {
      int nextChar = input.charAt(offset);
      if ((nextChar == '#') && (sharpsOrFlats >= 0)) {
        sharpsOrFlats++;
      } else if ((nextChar == 'b') && (sharpsOrFlats <= 0)) {
        sharpsOrFlats--;
      } else {
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
    return m - (m / (mod / 2)) * mod;
  }
}
