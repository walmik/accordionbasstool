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
      parser.skipWhiteSpace();
		}
		
		Note[] noteArray = new Note[notes.size()];
		return new Chord(notes.toArray(noteArray));
	}
	
	public static Vector<ParsedChordDef> parseChords(Main.StringParser parser)
	{
		Vector<ParsedChordDef> chordVec = new Vector<ParsedChordDef>();
		
		while (!parser.isDone())
		{		
			chordVec.add(parse(parser));

			parser.skipThrough(',');
		}
		
		return chordVec;
	}
	
	
	public static ParsedChordDef parse(Main.StringParser parser)
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
			return new ParsedChordDef(chord);
		}
		
		// Otherwise, parse the chord name
		Note rootNote = Note.fromString(parser);
		//Vector<Interval> ivals = new Vector<Interval>();
		
		String input = parser.input();

		// Single note
		if (input.length() == 0)
		{
			return new ParsedChordDef(rootNote);
		}

    RegistryChordDef result = ChordRegistry.mainRegistry().findChord(ChordRegistry.ALL_CHORDS, parser);

    // Additional Bass, root
		
		if (parser.nextChar() == '/')
		{
			parser.incOffset(1);
			
			Note bassNote = Note.fromString(parser);
			
			//return new Chord(rootNote, result.ivals, bassNote, true);
      return new ParsedChordDef(rootNote, bassNote, result, false);
		}

    // Additional Bass, non-root

		if (parser.nextChar() == '\\')
		{
			parser.incOffset(1);

			Note bassNote = Note.fromString(parser);

			//return new Chord(rootNote, result.ivals, bassNote, false);
      return new ParsedChordDef(rootNote, bassNote, result, true);
		}
	
		//return new Chord(rootNote, result.ivals);
    return new ParsedChordDef(rootNote, null, result, false);
  }


}