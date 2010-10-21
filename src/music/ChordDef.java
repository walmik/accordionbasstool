package music;

public class ChordDef
{

  public String name;
  public String abbrevHtml;
  public String abbrevPlain;
  //private String notestr;
  public music.Chord chord;

  public ChordDef(String _name, String _abb, String _notes)
  {
    name = _name;
    abbrevHtml = _abb.replace("[", "<sup>");
    abbrevHtml = abbrevHtml.replace("]", "</sup>");
    abbrevPlain = _abb.replace("[", "");
    abbrevPlain = abbrevPlain.replace("]", "");
    chord = music.ChordParser.parseNoteList(new util.Main.StringParser(_notes));
  }

  public ChordDef(Chord chord)
  {
    this.chord = chord;
    name = chord.toString();
    abbrevHtml = chord.toString("-", true);
    abbrevPlain = name;
  }

  public ChordDef()
  {
    chord = new Chord(new music.Note(), false);
    name = "";
    abbrevHtml = "";
    abbrevPlain = "";
  }

  public String getName()
  {
    return name;
  }

  public String getAbbrevHtml()
  {
    return abbrevHtml;
  }

  public String getAbbrevPlain()
  {
    return abbrevPlain;
  }

  public Chord getChord()
  {
    return chord;
  }

  public static String htmlify(String string)
  {
    return "<html>" + string + "</html>";
  }
}
