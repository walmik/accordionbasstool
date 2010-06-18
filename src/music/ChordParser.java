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
	
	
	static class ChordString
	{
		String[] strings;
		Interval[] ivals;
		ChordString(String[] strs, Interval[] nIvals)
		{
			strings = strs;
			ivals = nIvals;
		}
	}
	
	private static Vector<ChordString> primaryChordStrs;
	
	// Assert last chord added to vector is the given string of notes
	static boolean verifyChordOnC(Vector<ChordString> chords, String notes)
	{
		assert(chords.size() > 0);
		
		Chord theCChord = new Chord(new Note(Note.ScaleNote.C, 0), chords.lastElement().ivals);
		Chord noteChord = parse(new Main.StringParser(notes));
		assert(theCChord.equals(noteChord)) : (theCChord.toString() + " != " + noteChord.toString());
		return true;
	}
	
	public static void initChords()
	{
		primaryChordStrs = new Vector<ChordString>();
		
	
		// 7th Chords
		//----------------------------
		// Dominant 7, standard
		{
			String[] chordStr = {"7*"};
			Interval[] ival = {Interval.M3, Interval.m3, Interval.m3};
			primaryChordStrs.add(new ChordString(chordStr, ival));
			assert(verifyChordOnC(primaryChordStrs, "[C E G Bb]"));
		}

		// Dominant 7, no 5
		{
			String[] chordStr = {"7"};
			Interval[] ival = Chord.DOM7;
			primaryChordStrs.add(new ChordString(chordStr, ival));
			assert(verifyChordOnC(primaryChordStrs, "[C E Bb]"));
		}	
		
		// Major 7
		{
			String[] chordStr = {"M7", "maj7"};
			Interval[] ival = {Interval.M3, Interval.m3, Interval.M3};
			primaryChordStrs.add(new ChordString(chordStr, ival));
			assert(verifyChordOnC(primaryChordStrs, "[C E G B]"));
		}
		
		// Minor-Major 7
		{
			String[] chordStr = {"mM7", "minM7"};
			Interval[] ival = {Interval.m3, Interval.M3, Interval.M3};
			primaryChordStrs.add(new ChordString(chordStr, ival));
			assert(verifyChordOnC(primaryChordStrs, "[C Eb G B]"));
		}
		
		
		// Augmented-Major 7
		{
			String[] chordStr = {"augM7", "+M7"};
			Interval[] ival = {Interval.M3, Interval.M3, Interval.m3};
			primaryChordStrs.add(new ChordString(chordStr, ival));
			assert(verifyChordOnC(primaryChordStrs, "[C E G# B]"));
		}
		
		// Augmented 7
		{
			String[] chordStr = {"aug7", "+7"};
			Interval[] ival = {Interval.M3, Interval.M3, Interval.Dim3};
			primaryChordStrs.add(new ChordString(chordStr, ival));
			assert(verifyChordOnC(primaryChordStrs, "[C E G# Bb]"));
		}
	
		// Minor 7
		{
			String[] chordStr = {"m7", "min7"};
			Interval[] ival = {Interval.m3, Interval.M3, Interval.m3};
			primaryChordStrs.add(new ChordString(chordStr, ival));
			assert(verifyChordOnC(primaryChordStrs, "[C Eb G Bb]"));
		}	
		
		// Half-Dim 7
		{
			String[] chordStr = {"h7", "hd7", "hdim7"};
			Interval[] ival = {Interval.m3, Interval.m3, Interval.M3};
			primaryChordStrs.add(new ChordString(chordStr, ival));
			assert(verifyChordOnC(primaryChordStrs, "[C Eb Gb Bb]"));
		}
		
		// Dim 7, standard
		{
			String[] chordStr = {"dim7", "d7"};
			Interval[] ival = {Interval.m3, Interval.m3, Interval.m3};
			primaryChordStrs.add(new ChordString(chordStr, ival));
			assert(verifyChordOnC(primaryChordStrs, "[C Eb Gb Bbb]"));
		}
		
		// Dim 7, no 5
		{
			String[] chordStr = {"dim", "d"};
			Interval[] ival = Chord.DIM7;
			primaryChordStrs.add(new ChordString(chordStr, ival));
			assert(verifyChordOnC(primaryChordStrs, "[C Eb Bbb]"));
		}
		
		// 9th Chords
		//-------------------------------
		
		// 9th +5
		{
			String[] chordStr = {"9*"};
			Interval[] ival = {Interval.M3, Interval.m3, Interval.m3, Interval.M3};
			primaryChordStrs.add(new ChordString(chordStr, ival));
			assert(verifyChordOnC(primaryChordStrs, "[C E G Bb D]"));
		}
		
		// 9th no 5th
		{
			String[] chordStr = {"9"};
			Interval[] ival = {Interval.M3, Interval.Dim5, Interval.M3};
			primaryChordStrs.add(new ChordString(chordStr, ival));
			assert(verifyChordOnC(primaryChordStrs, "[C E Bb D]"));
		}
		
		// Triads
		//-------------------------------
		
		// Major
		{
			String[] chordStr = {"maj", "M"};
			primaryChordStrs.add(new ChordString(chordStr, Chord.MAJOR));
			assert(verifyChordOnC(primaryChordStrs, "[C E G]"));
		}
		
		// Minor
		{
			String[] chordStr = {"min", "m"};
			primaryChordStrs.add(new ChordString(chordStr, Chord.MINOR));
			assert(verifyChordOnC(primaryChordStrs, "[C Eb G]"));
		}
		
		// Diminished, standard	
		{
			String[] chordStr = {"dim*", "d*"};
			Interval[] ival = {Interval.m3, Interval.m3};
			primaryChordStrs.add(new ChordString(chordStr, ival));
			assert(verifyChordOnC(primaryChordStrs, "[C Eb Gb]"));
		}
		
		// Augmented
		{
			String[] chordStr = {"aug", "+"};
			Interval[] ival = {Interval.M3, Interval.M3};
			primaryChordStrs.add(new ChordString(chordStr, ival));
			assert(verifyChordOnC(primaryChordStrs, "[C E G#]"));
		}
	}
	
//	public static boolean matches(String[] tokens, Main.StringParser parser)
//	{
//		for (int i = 0; i < tokens.length; i++)
//		{
//			if (parser.input().startsWith(tokens[i]))
//			{
//				parser.incOffset(tokens[i].length());
//				return true;
//			}
//		}
//		
//		return false;
//	}
//	
//	static String[] tMaj7 = {"M7", "maj7"};
//	
//	static String[] tMaj = {"M", "maj"};
//	static String[] tMin = {"m", "min"};
//	
//	static String[] tAug = {"+", "aug"};
//	
//	static String[] tDimTri = {"dim*", "d*"};
//	static String[] tDim7 = {"dim7", "d7"};
//	static String[] tDim7no5 = {"dim", "d"};
//	
//	static String[] t7 = {"7"};
//	static String[] tHalfD = {"7b5", "hdim7"};
//	
//	public static Chord newParse(Main.StringParser parser)
//	{
//		Vector<Interval> ivals = new Vector<Interval>();
//		
//		while (!parser.isDone())
//		{
//			// Major 7
//			if (matches(tMaj7, parser))
//			{
//				// If don't have a triad, implies a major triad too
//				if (ivals.size() == 0)
//				{
//					ivals.add(Interval.M3);
//					ivals.add(Interval.P5);
//				}
//				ivals.add(Interval.M7);
//				break;
//			}
//			
//			// Major			
//			if (matches(tMaj, parser))
//			{
//				ivals.add(Interval.M3);
//				ivals.add(Interval.P5);				
//			}
//			
//			// Minor			
//			if (matches(tMin, parser))
//			{
//				ivals.add(Interval.m3);
//				ivals.add(Interval.P5);				
//			}
//			
//			// Augmented			
//			if (matches(tAug, parser))
//			{
//				ivals.add(Interval.M3);
//				ivals.add(Interval.Aug5);				
//			}
//			
//			// Diminished - standard triad
//			if (matches(tDimTri, parser))
//			{
//				ivals.add(Interval.m3);
//				ivals.add(Interval.Dim5);				
//			}
//			
//			// Diminished 7	no 5
//			if (matches(tDim7no5, parser))
//			{
//				ivals.add(Interval.m3);
//				ivals.add(Interval.Dim7);
//				break;
//			}
//			
//			// Diminished 7 WITH 5	
//			if (matches(tDim7, parser))
//			{
//				ivals.add(Interval.m3);
//				ivals.add(Interval.Dim5);
//				ivals.add(Interval.Dim7);
//				break;
//			}
//			
//			if (matches(t7, parser))
//			{
//				if (ivals.size() == 0)
//				{
//					ivals.add(Interval.M3);
//					ivals.add(Interval.m3);
//				}
//				
//				ivals.add(Interval.m7);
//				break;
//			}
//			
//			if (matches(t7, parser))
//			{
//				if (ivals.size() == 0)
//				{
//					ivals.add(Interval.M3);
//					ivals.add(Interval.m3);
//				}
//				
//				ivals.add(Interval.m7);
//				break;
//			}
//		}
//	}
	
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
			return new Chord(rootNote, 1);
		}
		
		Interval[] matchIvals = null;
		
		
		for (int i = 0; i < primaryChordStrs.size(); i++)
		{
			ChordString match = primaryChordStrs.elementAt(i);
			
			for (int j = 0; j < match.strings.length; j++)
			{
				if (input.startsWith(match.strings[j]))
				{
					parser.incOffset(match.strings[j].length());
					matchIvals = match.ivals;
					i = primaryChordStrs.size();
					break;
				}
			}
		}
		
		if (matchIvals == null)
		{
			matchIvals = Chord.MAJOR;
		}
		
		input = parser.input();
		
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
		
		if (input.startsWith("/"))
		{
			parser.incOffset(1);
			
			Note bassNote = Note.fromString(parser);
			int bassCount = 1;
			
			if (parser.input().startsWith("*"))
				bassCount = 2;
			
			return new Chord(rootNote, matchIvals, bassNote, bassCount);
		}
		
		return new Chord(rootNote, matchIvals);
		
		
//		int offset = 0;
//		
//		while ((offset < input.length()) && Character.isLetterOrDigit(input.charAt(offset)))
//		{
//			offset++;
//		}
//		
//		String chordName = input.substring(0, offset);
//		parser.incOffset(offset);
//		
//		// Single note
//		if (chordName.length() == 0)
//		{
//			return new Chord(rootNote, 1);
//		}
//		
//		if (chordName.length() == 1)
//		{
//			int ch = chordName.charAt(0);
//			switch (ch)
//			{
//			case '7':
//				return new Chord(rootNote, Chord.M7);
//				
//			case 'm':
//				return new Chord(rootNote, Chord.MINOR);
//				
//			case 'M':
//				return new Chord(rootNote, Chord.MAJOR);
//				
//			case '*':
//				return new Chord(rootNote, Chord.DIM);
//			}
//		}
//		
////		if (chordName.startsWith("Maj"))
////		{
////			
////		}
//		
//		return null;
	}
}
