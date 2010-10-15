package render;

import music.Chord;

class ChordDef
{

  String name;
  String abbrevHtml;
  String abbrevPlain;
  //private String notestr;
  music.Chord chord;

  ChordDef(String _name, String _abb, String _notes)
  {
    name = _name;
    abbrevHtml = _abb.replace("[", "<sup>");
    abbrevHtml = abbrevHtml.replace("]", "</sup>");
    abbrevPlain = _abb.replace("[", "");
    abbrevPlain = abbrevPlain.replace("]", "");
    chord = music.ChordParser.parseNoteList(new util.Main.StringParser(_notes));
  }

  ChordDef()
  {
    chord = new Chord(new music.Note(), false);
    name = "";
    abbrevHtml = "";
    abbrevPlain = "";
  }

  String getName()
  {
    return name;
  }

  String getAbbrevHtml()
  {
    return abbrevHtml;
  }

  String getAbbrevPlain()
  {
    return abbrevPlain;
  }

  Chord getChord()
  {
    return chord;
  }

  static String htmlify(String string)
  {
    return "<html>" + string + "</html>";
  }
}
