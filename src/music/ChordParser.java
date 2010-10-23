package music;

import java.util.Vector;

import util.Main;

public class ChordParser {

	public static Chord parseNoteList(Main.StringParser parser)
	{
		Vector<Note> notes = new Vector<Note>();
		Note newNote;
		
		while ((newNote = Note.fromString(parser)) != null)
		{
			notes.add(newNote);
		}
		
		Note[] noteArray = new Note[notes.size()];
		return new Chord(notes.toArray(noteArray));
	}
	
	public static Vector<ChordDef> parseChords(Main.StringParser parser)
	{
		Vector<ChordDef> chordVec = new Vector<ChordDef>();
		
		while (!parser.isDone())
		{		
			chordVec.add(parse(parser));

			parser.skipThrough(',');
		}
		
		return chordVec;
	}
	
	
	public static ChordDef parse(Main.StringParser parser)
	{
		// If starting with [ then we have a list of notes
		// so parse them individually
		if (parser.input().startsWith("["))
		{
			parser.incOffset(1);
			Chord chord = parseNoteList(parser);
			if (parser.input().startsWith("]"))
			{
				parser.incOffset(1);
			}
			return new ChordDef(chord);
		}
		
		// Otherwise, parse the chord name
		Note rootNote = Note.fromString(parser);
		//Vector<Interval> ivals = new Vector<Interval>();
		
		String input = parser.input();

		// Single note
		if (input.length() == 0)
		{
			return new ChordDef(new Chord(rootNote, false));
		}

    ChordRegistry.ExtChordDef result =
            ChordRegistry.mainRegistry().findChord(ChordRegistry.ALL_CHORDS, parser);

    // Additional Bass, root
		
		if (parser.nextChar() == '/')
		{
			parser.incOffset(1);
			
			Note bassNote = Note.fromString(parser);
			
			//return new Chord(rootNote, result.ivals, bassNote, true);
      return buildChord(rootNote, bassNote, result, false);
		}

    // Additional Bass, non-root

		if (parser.nextChar() == '\\')
		{
			parser.incOffset(1);

			Note bassNote = Note.fromString(parser);

			//return new Chord(rootNote, result.ivals, bassNote, false);
      return buildChord(rootNote, bassNote, result, true);
		}
	
		//return new Chord(rootNote, result.ivals);
    return buildChord(rootNote, null, result, false);
  }

  public static ChordDef buildChord(Note rootNote, Note addedBassNote, ChordDef currTableChord, boolean addedBassLowest)
  {

    Chord fullChord =
            new Chord(currTableChord.chord,
            rootNote,
            addedBassNote, addedBassLowest);

    ChordDef finalChord = new ChordDef();

    finalChord.chord = fullChord;

    // -- Set HTML Abbrev
    finalChord.abbrevHtml = rootNote.toString(true) + currTableChord.abbrevHtml;

    if (addedBassNote != null) {
      finalChord.abbrevHtml += "/" + addedBassNote.toString(true);
    }

    // -- Set Plain Abbrev

    finalChord.abbrevPlain = rootNote.toString() + currTableChord.abbrevPlain;

    if (addedBassNote != null) {
      finalChord.abbrevPlain += "/" + addedBassNote.toString();
    }

    // -- Set Name
    finalChord.name = rootNote.toString(true) + " " + currTableChord.name;

    if (addedBassNote != null) {
      finalChord.name += " over " + addedBassNote.toString(true);
    }

    return finalChord;
  }

}