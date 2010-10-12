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
	
	public static Vector<Chord> parseChords(Main.StringParser parser)
	{
		Vector<Chord> chordVec = new Vector<Chord>();
		
		while (!parser.isDone())
		{		
			chordVec.add(parse(parser));

			parser.skipThrough(',');
		}
		
		return chordVec;
	}
	
	
	public static Chord parse(Main.StringParser parser)
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
			return chord;
		}
		
		// Otherwise, parse the chord name
		Note rootNote = Note.fromString(parser);
		//Vector<Interval> ivals = new Vector<Interval>();
		
		String input = parser.input();

		// Single note
		if (input.length() == 0)
		{
			return new Chord(rootNote, false);
		}
		
		Interval[] matchIvals = null;

    Vector<ChordRegistry.ChordEntry> chordStrs
            = ChordRegistry.primaryChordStrs;
		
		for (int i = 0; i < chordStrs.size(); i++)
		{
			ChordRegistry.ChordEntry match = chordStrs.elementAt(i);
			
			for (int j = 0; j < match.strings.length; j++)
			{
				if (input.startsWith(match.strings[j]))
				{
					parser.incOffset(match.strings[j].length());
					matchIvals = match.ivals;
					i = chordStrs.size();
					break;
				}
			}
		}
		
		if (matchIvals == null)
		{
			matchIvals = ChordRegistry.MAJOR;
		}
		
		input = parser.input();
    
    //while (!input.isEmpty())
    //{
      matchIvals = parseChordMod(input, parser, matchIvals);
    //}

    // Additional Bass, root
		
		if (input.startsWith("/"))
		{
			parser.incOffset(1);
			
			Note bassNote = Note.fromString(parser);
			
			return new Chord(rootNote, matchIvals, bassNote, true);
		}

    // Additional Bass, non-root

		if (input.startsWith("&"))
		{
			parser.incOffset(1);

			Note bassNote = Note.fromString(parser);

			return new Chord(rootNote, matchIvals, bassNote, false);
		}
		
		return new Chord(rootNote, matchIvals);

  }

  static Interval[] parseChordMod(String input,
                                  Main.StringParser parser,
                                  Interval[] matchIvals)
  {
    // Sustained
		if (input.startsWith("sus2"))
		{
			matchIvals = matchIvals.clone();
			matchIvals[0] = Interval.M2;
			parser.incOffset(4);
			input = parser.input();
		}

		if (input.startsWith("sus4"))
		{
			matchIvals = matchIvals.clone();
			matchIvals[0] = Interval.P4;
			parser.incOffset(4);
			input = parser.input();			
		}

    return matchIvals;
  }

}