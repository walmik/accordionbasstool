package music;

final public class RegistryChordDef
{
  final public String name;
  final public String abbrevHtml;
  final public String abbrevPlain;
  
  final private music.Chord chord;
  final Interval[] ivals;
  final public RelChord relChord;

  final public short row;
  final public short col;

  RegistryChordDef(String _name, String _abb, String _notes, int r, int c)
  {
    name = _name;
    abbrevHtml = _abb.replace("[", "<sup>").replace("]", "</sup>");
    abbrevPlain = _abb.replace("[", "").replace("]", "");
    chord = music.ChordParser.parseNoteList(new StringParser(_notes));

    relChord = new RelChord(chord);

    ivals = this.chord.extractInterval();
    row = (short)r;
    col = (short)c;
  }

  public String getTransposedString(Note rootNote)
  {
    //return new Chord(rootNote, ivals).toString();
    return chord.getTransposedString(rootNote);
  }

  public Chord getSimpleChordAt(Note rootNote)
  {
    return new Chord(rootNote, ivals);
  }

  public int getChordLength()
  {
    return ivals.length + 1;
  }

  public static RegistryChordDef getCustomDef()
  {
    return new RegistryChordDef("Custom", "Custom", "C", 0, 0);
  }

  @Override
  public String toString()
  {
    return name;
  }
}
