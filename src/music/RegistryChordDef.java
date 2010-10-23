package music;

public class RegistryChordDef
{
  final public String name;
  final public String abbrevHtml;
  final public String abbrevPlain;
  
  final public music.Chord chord;

  Interval[] ivals;
  final public short row;
  final public short col;

  RegistryChordDef(String _name, String _abb, String _notes, int r, int c)
  {
    name = _name;
    abbrevHtml = _abb.replace("[", "<sup>").replace("]", "</sup>");
    abbrevPlain = _abb.replace("[", "").replace("]", "");
    chord = music.ChordParser.parseNoteList(new util.Main.StringParser(_notes));

    ivals = this.chord.extractInterval();
    row = (short)r;
    col = (short)c;
  }

//  RegistryChordDef(Chord chord)
//  {
//    this.chord = chord;
//    name = chord.toString();
//    abbrevHtml = chord.toString("-", true);
//    abbrevPlain = name;
//  }
//
//  RegistryChordDef()
//  {
//    chord = new Chord(new music.Note(), false);
//    name = "";
//    abbrevHtml = "";
//    abbrevPlain = "";
//  }

//  public String getName()
//  {
//    return name;
//  }
//
//  public String getAbbrevHtml()
//  {
//    return abbrevHtml;
//  }
//
//  public String getAbbrevPlain()
//  {
//    return abbrevPlain;
//  }
//
//  public Chord getChord()
//  {
//    return chord;
//  }
//
//  public static String htmlify(String string)
//  {
//    return "<html>" + string + "</html>";
//  }
}
