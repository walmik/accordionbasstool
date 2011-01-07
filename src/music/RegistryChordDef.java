package music;

final public class RegistryChordDef
{
  final public String name;
  final public String abbrevHtml;
  final public String abbrevPlain;
  final public String group;
  
  final private music.Chord chord;
  final Interval[] ivals;
  final public RelChord relChord;

  RegistryChordDef(String _name, String _abb, String _notes, String _group)
  {
    name = _name;
    abbrevHtml = _abb.replace("[", "<sup>").replace("]", "</sup>");
    abbrevPlain = _abb.replace("[", "").replace("]", "");
    chord = music.ChordParser.parseNoteList(new StringParser(_notes));
    group = _group;

    relChord = new RelChord(chord);
    relChord.setOrigDef(this);

    ivals = this.chord.extractInterval();
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

  public boolean matchesMask(Chord.Mask mask)
  {
    return chord.getChordMask().equals(mask);
  }

  public int getChordLength()
  {
    return ivals.length + 1;
  }

  public static RegistryChordDef getCustomDef()
  {
    return new RegistryChordDef("(Custom)", "(Custom)", "C", "");
  }

  @Override
  public String toString()
  {
    return name;
  }
}
