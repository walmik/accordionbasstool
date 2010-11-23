package music;

public class Chord
{

  final Note[] notes;

  public static class Mask
  {

    private int value = 0;

    Mask()
    {
      value = 0;
    }

    Mask(int val)
    {
      value = val;
    }

    Mask(Chord chord)
    {
      value = 0;

      for (int i = 0; i < chord.notes.length; i++) {
        value = noteToMask(chord.notes[i], value);
      }
    }

    static int noteToMask(Note note, int value)
    {
      int bit = toUpperOctaveBit(note);
      if ((value & (1 << bit)) != 0) {
        bit = toLowerOctaveBit(bit);
      }
      note.octaveBit = (short) bit;
      value |= (1 << bit);
      return value;
    }

    public static void sortNotesFromMask(Chord chord, Note[] sortArray)
    {
      // Highest bit in the byte is the lowest note
      int value = 0;

      for (int i = 0; i < chord.notes.length; i++) {
        value = noteToMask(chord.notes[i], value);

        sortArray[chord.notes[i].octaveBit] = chord.notes[i];
      }

      chord.mask = new Mask(value);
    }

    final static private int toUpperOctaveBit(Note note)
    {
      return (note.value() + Note.NUM_HALFSTEPS);
    }

    final static private int toLowerOctaveBit(int upperbit)
    {
      return (upperbit - Note.NUM_HALFSTEPS);
    }

    final static boolean isLowerOctaveBit(int bit)
    {
      return (bit < Note.NUM_HALFSTEPS);
    }

//    final private int toLowerOctaveBit(Note note)
//    {
//      return (1 << note.value());
//    }
    static boolean contains(int a, int b)
    {
      return (a | b) == a;
    }

    boolean contains(Mask otherMask)
    {
      return contains(value, otherMask.value);
    }

    boolean contains(int bit)
    {
      return ((value & (1 << bit)) != 0);
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

    void concatMe(Mask otherMask)
    {
      value |= otherMask.value;
    }

//		void doubleNote(Note note)
//		{
//			value |= (1 << (note.value() + Note.NUM_HALFSTEPS));
//		}
    // Are there any bass buttons in the mask
    public boolean hasLowerOctave()
    {
      //return (value >> Note.NUM_HALFSTEPS) != 0;
      return (value & ((1 << Note.NUM_HALFSTEPS) - 1)) != 0;
    }

    void unmaskRegister(Mask otherMask)
    {
      int res = value & otherMask.value;
      if (res != 0) {
        value = res;
      }
    }

    void unmaskLowRegisterAndAbove(Mask otherMask, int lowestBit)
    {
      int maskToRestrict = otherMask.value;

      // Restrict bass notes lower than the lowest bit desired in the chord
      if (lowestBit < Note.NUM_HALFSTEPS) {
        maskToRestrict |= ((1 << lowestBit) - 1);
      }

      int res = value & maskToRestrict;
      if (res != 0) {
        value = res;
      }
    }

    public int getValue()
    {
      return value;
    }
  }
  protected Mask mask;

  public Chord(Note singleN, boolean mustRoot)
  {
    int dupCount = (mustRoot ? 2 : 1);

    notes = new Note[dupCount];

    initBassNote(singleN, dupCount);
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

  public Chord(Note note, Interval[] ivals, Note extraBass, boolean mustBeBassRoot)
  {
    int extraLen = 0;
    if (extraBass != null) {
      extraLen++;
      if (mustBeBassRoot) {
        extraLen++;
      }
    }

    this.notes = new Note[ivals.length + 1 + extraLen];

    if (extraBass != null) {
      initBassNote(extraBass, extraLen);
    }

    initFromIval(extraLen, note, ivals);
  }

  private void initBassNote(Note singleN, int count)
  {
    for (int i = 0; i < count; i++) {
      notes[i] = singleN;
    }
  }

  private void initFromIval(int offset, Note note, Interval[] ivals)
  {
    notes[offset] = note;

    for (int i = 0; i < ivals.length; i++) {
      notes[offset + i + 1] = notes[offset + i].add(ivals[i]);
    }

  }

//	public Chord(Chord one, Chord two)
//	{
//		Mask onemask = one.getChordMask();
//		Mask twomask = two.getChordMask();
//
//		int newMask = (~onemask.value | twomask.value);
//
//		int numNotes = one.notes.length;
//
//		// See which notes in two are not in one and add them
//		// to new note list
//		for (int i = 0; i < two.notes.length; i++)
//		{
//			if ((newMask & (1 << two.notes[i].value())) != 0)
//			{
//				numNotes++;
//			}
//		}
//
//		notes = new Note[numNotes];
//
//		System.arraycopy(one.notes, 0, notes, 0, one.notes.length);
//
//		// Fill only the second chord's notes that don't overlap
//		int count = one.notes.length;
//
//		for (int i = 0; i < two.notes.length; i++)
//		{
//			if ((newMask & (1 << two.notes[i].value())) != 0)
//			{
//				notes[count++] = two.notes[i];
//			}
//		}
//
//		assert(count == numNotes);
//	}
  // extraBass may be null
  public Chord(Chord existing, Note newRoot, Note extraBass, boolean mustBeBassRoot)
  {
    int extraLen = 0;
    if (extraBass != null) {
      extraLen++;
      if (mustBeBassRoot) {
        extraLen++;
      }
    }

    this.notes = new Note[existing.notes.length + extraLen];

    if (extraBass != null) {
      initBassNote(extraBass, extraLen);
    }

    Interval ival = newRoot.diff(existing.notes[0]);

    for (int i = 0; i < existing.notes.length; i++) {
      notes[extraLen + i] = existing.notes[i].add(ival);
    }
  }

  public Interval[] extractInterval()
  {
    Interval ivals[] = new Interval[notes.length - 1];
    for (int i = 0; i < notes.length - 1; i++) {
      ivals[i] = notes[i + 1].diff(notes[i]);
    }
    return ivals;
  }

  public Chord transposeBy(Interval ival)
  {
    Note[] newNotes = new Note[notes.length];

    for (int i = 0; i < notes.length; i++) {
      newNotes[i] = notes[i].add(ival);
    }

    return new Chord(newNotes);
  }

  public String getTransposedString(Note newRoot)
  {
    Interval ival = newRoot.diff(notes[0]);

    String str = "";

    for (int i = 0; i < notes.length; i++) {
      if (i > 0) {
        str += "-";
      }
      str += notes[i].add(ival).toString();
    }

    return str;
  }

  public boolean isSingleNote()
  {
    return notes.length == 1;
  }

  public Note getRootNote()
  {
    return notes[0];
  }

  public int getNumNotes()
  {
    return notes.length;
  }

  public Note getNoteAt(int pos)
  {
    if ((pos < 0) || (pos >= notes.length)) {
      return null;
    }

    return notes[pos];
  }

  public int findNotePos(Note note)
  {
    for (int i = 0; i < notes.length; i++) {
      if (notes[i].equals(note)) {
        return i;
      }
    }

    return -1;
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
    if (mask == null) {
      mask = new Mask(this);
    }

    return mask;
  }

  private String toString(String sep, boolean html)
  {
    String str = "";

    for (int i = 0; i < notes.length; i++) {
      if (i > 0) {
        str += sep;
      }
      str += notes[i].toString(html);
    }

    return str;
  }

  @Override
  public String toString()
  {
    return toString("", false);
  }

  public String toHtmlString()
  {
    return toString("+", true);
  }
}

