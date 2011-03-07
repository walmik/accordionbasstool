package music;

import music.core.StringParser;
import music.core.Chord;
import music.core.Note;
import java.util.Vector;

public class ChordParser
{
  public static Vector<ParsedChordDef> parseChords(StringParser parser, boolean removeDupNotes)
  {
    Vector<ParsedChordDef> chordVec = new Vector<ParsedChordDef>();

    while (!parser.isDone()) {
      chordVec.add(parse(parser, removeDupNotes));

      parser.skipThrough(", -+");
    }

    return chordVec;
  }

  public static ParsedChordDef parse(StringParser parser, boolean removeDupNotes)
  {
    // If starting with [ then we have a list of notes
    // so parse them individually
    if (parser.input().startsWith("[")) {
      parser.incOffset(1);
      Chord chord = parser.parseNoteList();
      if (removeDupNotes) {
        chord = chord.getUndupedChord();
      }
      if (parser.input().startsWith("]")) {
        parser.incOffset(1);
      }
      return new ParsedChordDef(chord);
    }

    // Otherwise, parse the chord name
    Note rootNote = parser.getNote();

    // Convert invalid note to a default C
    if (rootNote == null) {
      rootNote = new Note();
    }
    //Vector<Interval> ivals = new Vector<Interval>();

    String input = parser.input();

    // Single note
    if (input.length() == 0) {
      return new ParsedChordDef(rootNote);
    }

    RegistryChordDef result = ChordRegistry.mainRegistry().findChord(parser);

    RelChord relChord = null;

    if (result != null) {
      relChord = result.relChord;
    }

    // Additional Bass, non-root

    if (parser.nextChar() == '\\') {
      parser.incOffset(1);

      Note bassNote = parser.getNote();

      //return new Chord(rootNote, result.ivals, bassNote, true);
      return new ParsedChordDef(rootNote, bassNote, relChord, ParsedChordDef.BassSetting.NotLowestBass);
    }

    // Additional Bass, root

    if (parser.nextChar() == '/') {
      parser.incOffset(1);

      Note bassNote = parser.getNote();

      //return new Chord(rootNote, result.ivals, bassNote, false);
      return new ParsedChordDef(rootNote, bassNote, relChord, ParsedChordDef.BassSetting.LowestBass);
    }

    //return new Chord(rootNote, result.ivals);
    return new ParsedChordDef(rootNote, null, relChord, ParsedChordDef.BassSetting.NotLowestBass);
  }
}
