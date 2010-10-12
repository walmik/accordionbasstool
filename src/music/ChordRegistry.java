/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package music;

import java.util.Hashtable;
import java.util.Vector;
import util.Main;


/**
 *
 * @author Ilya
 */
public class ChordRegistry
{
  // Stradella Chords
	final static Interval[] MAJOR = {Interval.M3, Interval.m3};
	final static Interval[] MINOR = {Interval.m3, Interval.M3};
	final static Interval[] DOM =  {Interval.M3, Interval.Dim5};
	final static Interval[] DIM =  {Interval.m3, Interval.Dim5};


  public static class ChordEntry
	{
    public String name;
		String[] strings;
		Interval[] ivals;
    public String group;

		ChordEntry(String csname, String[] strs, Interval[] nIvals, String gname)
		{
      name = csname;
			strings = strs;
			ivals = nIvals;
      group = gname;
      
      Vector<ChordEntry> grouplist = groupedChordStrs.get(gname);

      if (grouplist == null)
      {
        grouplist = new Vector<ChordEntry>();
        grouplist.add(this);
        groupedChordStrs.put(group, grouplist);
      }
      else
      {
        grouplist.add(this);
      }
    }
  }

	static Vector<ChordEntry> primaryChordStrs;

  public static Hashtable<String, Vector<ChordEntry> > groupedChordStrs;

	// Assert last chord added to vector is the given string of notes
	static boolean verifyChordOnC(Vector<ChordEntry> chords, String notes)
	{
		assert(chords.size() > 0);

		Chord theCChord = new Chord(new Note(Note.ScaleNote.C, 0), chords.lastElement().ivals);
		Chord noteChord = ChordParser.parse(new Main.StringParser(notes));
		assert(theCChord.equals(noteChord)) : (theCChord.toString() + " != " + noteChord.toString());
		return true;
	}

	public static void initChords()
	{
    groupedChordStrs = new Hashtable<String, Vector<ChordEntry> >();
		primaryChordStrs = new Vector<ChordEntry>();

    // No chord entry
		{
      String name = "No Chord";
			String[] chordStr = {""};
			Interval[] ival = {};
			primaryChordStrs.add(new ChordEntry(name, chordStr, ival, "Common"));
			assert(verifyChordOnC(primaryChordStrs, "[C]"));
		}

		// 7th Chords
		//----------------------------
		// Dominant 7, standard
		{
      String name = "Dom 7th w/ 5";
			String[] chordStr = {"7*"};
			Interval[] ival = {Interval.M3, Interval.m3, Interval.m3};
			primaryChordStrs.add(new ChordEntry(name, chordStr, ival, "7th"));
			assert(verifyChordOnC(primaryChordStrs, "[C E G Bb]"));
		}

		// Dominant 7, no 5
		{
      String name = "Dom 7th";
      String[] chordStr = {"7"};
			Interval[] ival = ChordRegistry.DOM;
			primaryChordStrs.add(new ChordEntry(name, chordStr, ival, "Common"));
			assert(verifyChordOnC(primaryChordStrs, "[C E Bb]"));
		}

		// Major 7
		{
      String name = "Major 7";
      String[] chordStr = {"M7", "maj7"};
			Interval[] ival = {Interval.M3, Interval.m3, Interval.M3};
			primaryChordStrs.add(new ChordEntry(name, chordStr, ival, "7th"));
			assert(verifyChordOnC(primaryChordStrs, "[C E G B]"));
		}

		// Minor-Major 7
		{
      String name = "Min-Maj 7";
      String[] chordStr = {"mM7", "minM7"};
			Interval[] ival = {Interval.m3, Interval.M3, Interval.M3};
			primaryChordStrs.add(new ChordEntry(name, chordStr, ival, "7th"));
			assert(verifyChordOnC(primaryChordStrs, "[C Eb G B]"));
		}


		// Augmented-Major 7
		{
      String name = "Aug Maj 7";
			String[] chordStr = {"augM7", "+M7"};
			Interval[] ival = {Interval.M3, Interval.M3, Interval.m3};
			primaryChordStrs.add(new ChordEntry(name, chordStr, ival, "7th"));
			assert(verifyChordOnC(primaryChordStrs, "[C E G# B]"));
		}

		// Augmented 7
		{
      String name = "Aug 7";
			String[] chordStr = {"aug7", "+7"};
			Interval[] ival = {Interval.M3, Interval.M3, Interval.Dim3};
			primaryChordStrs.add(new ChordEntry(name, chordStr, ival, "7th"));
			assert(verifyChordOnC(primaryChordStrs, "[C E G# Bb]"));
		}

		// Minor 7
		{
      String name = "Minor 7";
			String[] chordStr = {"m7", "min7"};
			Interval[] ival = {Interval.m3, Interval.M3, Interval.m3};
			primaryChordStrs.add(new ChordEntry(name, chordStr, ival, "7th"));
			assert(verifyChordOnC(primaryChordStrs, "[C Eb G Bb]"));
		}

		// Half-Dim 7
		{
      String name = "Half-Dim 7";
			String[] chordStr = {"m7(b5)", "min7(b5)"};
			Interval[] ival = {Interval.m3, Interval.m3, Interval.M3};
			primaryChordStrs.add(new ChordEntry(name, chordStr, ival, "7th"));
			assert(verifyChordOnC(primaryChordStrs, "[C Eb Gb Bb]"));
		}

		// Dim 7, standard
		{
      String name = "Dim 7 w/ 5";
			String[] chordStr = {"dim7", "d7"};
			Interval[] ival = {Interval.m3, Interval.m3, Interval.m3};
			primaryChordStrs.add(new ChordEntry(name, chordStr, ival, "7th"));
			assert(verifyChordOnC(primaryChordStrs, "[C Eb Gb Bbb]"));
		}

		// Dim 7, no 5
		{
      String name = "Dim 7";
			String[] chordStr = {"dim", "d"};
			Interval[] ival = ChordRegistry.DIM;
			primaryChordStrs.add(new ChordEntry(name, chordStr, ival, "Common"));
			assert(verifyChordOnC(primaryChordStrs, "[C Eb Bbb]"));
		}

		// 9th Chords
		//-------------------------------

		// 9th +5
		{
      String name = "Major 9 w/ 5";
			String[] chordStr = {"9*"};
			Interval[] ival = {Interval.M3, Interval.m3, Interval.m3, Interval.M3};
			primaryChordStrs.add(new ChordEntry(name, chordStr, ival, "9th"));
			assert(verifyChordOnC(primaryChordStrs, "[C E G Bb D]"));
		}

		// 9th no 5th
		{
      String name = "Major 9";
      String[] chordStr = {"9"};
			Interval[] ival = {Interval.M3, Interval.Dim5, Interval.M3};
			primaryChordStrs.add(new ChordEntry(name, chordStr, ival, "9th"));
			assert(verifyChordOnC(primaryChordStrs, "[C E Bb D]"));
		}

		// Triads
		//-------------------------------

		// Major
		{
      String name = "Major";
			String[] chordStr = {"maj", "M"};
			primaryChordStrs.add(new ChordEntry(name, chordStr, ChordRegistry.MAJOR, "Common"));
			assert(verifyChordOnC(primaryChordStrs, "[C E G]"));
		}

		// Minor
		{
      String name = "Minor";
			String[] chordStr = {"min", "m"};
			primaryChordStrs.add(new ChordEntry(name, chordStr, ChordRegistry.MINOR, "Common"));
			assert(verifyChordOnC(primaryChordStrs, "[C Eb G]"));
		}

		// Diminished, standard
		{
      String name = "Dim";
			String[] chordStr = {"dim*", "d*"};
			Interval[] ival = {Interval.m3, Interval.m3};
			primaryChordStrs.add(new ChordEntry(name, chordStr, ival, "Triad"));
			assert(verifyChordOnC(primaryChordStrs, "[C Eb Gb]"));
		}

		// Augmented
		{
      String name = "Aug";
			String[] chordStr = {"aug", "+"};
			Interval[] ival = {Interval.M3, Interval.M3};
			primaryChordStrs.add(new ChordEntry(name, chordStr, ival, "Triad"));
			assert(verifyChordOnC(primaryChordStrs, "[C E G#]"));
		}
	}
}
