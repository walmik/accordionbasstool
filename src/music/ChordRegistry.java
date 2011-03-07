/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package music;

import music.core.StringParser;
import music.core.Chord;
import music.core.Note;
import java.util.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import app.Main;

/**
 *
 * @author Ilya
 */
public class ChordRegistry
{

  private static ChordRegistry _mainRegistry = null;
  //static RegistryChordDef defaultChord;

  public static ChordRegistry mainRegistry()
  {
    if (_mainRegistry == null) {
      _mainRegistry = new ChordRegistry("chorddefs.xml");
    }

    return _mainRegistry;
  }
  //==============================================================
  public final RegistryChordDef allChordDefs[];
  public final String[] allSeqGroupNames;
  public final SeqDef[][] allSeqDefs;
  //==============================================================
  
  public class SeqDef
  {
    public final String name;
    public final String noteSeq;

    SeqDef(Element elem)
    {
      name = elem.getAttribute("name");
      noteSeq = elem.getAttribute("notes");
    }

    @Override
    public String toString()
    {
      return name;
    }
  }


  public ChordRegistry(String filename)
  {
    Document doc = Main.loadXmlFile(filename, "./xml/");

    if (doc == null) {
      allChordDefs = new RegistryChordDef[0];
      allSeqDefs = new SeqDef[0][0];
      allSeqGroupNames = new String[0];
      return;
    }

    Element root = doc.getDocumentElement();

    // Load Chords
    NodeList chords = root.getElementsByTagName("chords");

    chords = ((Element) chords.item(0)).getElementsByTagName("chord");

    allChordDefs = new RegistryChordDef[chords.getLength()];

    for (int i = 0; i < chords.getLength(); i++) {
      Element chord = (Element) chords.item(i);
      String chordName = chord.getAttribute("name");
      String abbrev = chord.getAttribute("abbrev");
      String notelist = chord.getAttribute("notes");
      String group = chord.getAttribute("group");
      allChordDefs[i] = new RegistryChordDef(chordName, abbrev, notelist, group);
    }

    // Load Seqs
    NodeList seqGroups = root.getElementsByTagName("seqs");
    allSeqDefs = new SeqDef[seqGroups.getLength()][];
    allSeqGroupNames = new String[seqGroups.getLength()];

    for (int i = 0; i < seqGroups.getLength(); i++) {
      Element seqGroup = (Element)seqGroups.item(i);
      allSeqGroupNames[i] = seqGroup.getAttribute("name");

      NodeList seqs = seqGroup.getElementsByTagName("seq");
      allSeqDefs[i] = new SeqDef[seqs.getLength()];

      for (int j = 0; j < seqs.getLength(); j++) {
        allSeqDefs[i][j] = new SeqDef((Element)seqs.item(j));
      }
    }
  }

  public RegistryChordDef findChord(StringParser parser)
  {
    int prevMatchLength = 0;
    RegistryChordDef bestMatch = null;
    String chordToMatch = parser.input();

    for (int i = 0; i < allChordDefs.length; i++) {
      RegistryChordDef currChord = allChordDefs[i];
      String currAbbrev = currChord.abbrevPlain.trim();
      if ((currAbbrev.length() > prevMatchLength)
              && chordToMatch.startsWith(currAbbrev)) {
        bestMatch = currChord;
        prevMatchLength = currAbbrev.length();
      }
    }

    parser.incOffset(prevMatchLength);
    return bestMatch;
  }

  public RegistryChordDef findSingleNoteChord()
  {
    for (int i = 0; i < allChordDefs.length; i++) {
      if (allChordDefs[i].ivals.length == 0) {
        return allChordDefs[i];
      }
    }

    return null;
  }

  public Vector<ParsedChordDef> findAllChordsForNotes(Chord chord, boolean removeInversion)
  {
    Chord.Mask mask = new Chord.Mask();
    Note[] sortArray = new Note[Note.NUM_HALFSTEPS * 2];
    mask.sortChordNotes(chord, sortArray);

    Vector<ParsedChordDef> parsedDefs = findChordFromNotes(sortArray, mask, removeInversion, true, false);

    return parsedDefs;
  }

  public ParsedChordDef findFirstChordFromNotes(Chord chord)
  {
    Chord.Mask mask = new Chord.Mask();
    Note[] sortArray = new Note[Note.NUM_HALFSTEPS * 2];
    mask.sortChordNotes(chord, sortArray);

    Vector<ParsedChordDef> parsedDefs = findChordFromNotes(sortArray, mask, true, false, true);

    for (ParsedChordDef def : parsedDefs) {
      if (def.rootNote.equals(chord.getRootNote())) {
        return def;
      }
    }

    return (parsedDefs.isEmpty() ? null : parsedDefs.firstElement());
  }

//  public Vector<ParsedChordDef> findChordFromButtonCombo(ButtonCombo combo, boolean allowInversion, boolean setPrefCombo)
//  {
//    Chord.Mask mask = combo.getChordMask();
//    Note[] fullNotelist = ButtonCombo.sortedNotes;
//
//    Vector<ParsedChordDef> parsedDefs = findChordFromNotes(fullNotelist, mask, allowInversion, true);
//
//    for (ParsedChordDef def : parsedDefs) {
//      def.setPrefCombo(combo);
//    }
//
//    return parsedDefs;
//  }
  public Vector<ParsedChordDef> findChordFromNotes(
          Note[] fullNotelist,
          Chord.Mask mask,
          boolean removeInversion,
          boolean includeUnknown,
          boolean findFirstOnly)
  {
    // Create an array of masks with one note removed, to find chords with an added bass
    // Eg. C E G D, first search for exact match
    // Create submasks for D E G, C E G, C D G, C D E
    // This allows us to find CM = C E G

    //Chord.Mask mask = combo.getChordMask();
    //Note[] fullNotelist = ButtonCombo.sortedNotes;

    int upperMaskOrig = mask.getUpperValue();
    int upperMasks[] = new int[Note.NUM_HALFSTEPS];
    int numUpperMasks = 0;

    // Set the first entry to full mask
    //upperMasks[numUpperMasks++] = upperMaskOrig;

    for (int i = 0; i < Note.NUM_HALFSTEPS; i++) {
      if (mask.contains(i)) {
        upperMasks[numUpperMasks++] = (upperMaskOrig & ~(1 << Chord.Mask.toUpperOctaveBit(i)));
      }
    }

    Vector<ParsedChordDef> possChordDefs = new Vector<ParsedChordDef>();

    for (int n = Note.NUM_HALFSTEPS; n < fullNotelist.length; n++) {

      Note note = fullNotelist[n];

      if (note == null) {
        continue;
      }

      RelChord matchedRelChord = null;
      int matchedMask = 0;

      // First Pass, find exact mask
      for (int i = 0; i < allChordDefs.length; i++) {
        if (allChordDefs[i].getSimpleChordAt(note).getChordMask().equals(upperMaskOrig)) {
          matchedRelChord = allChordDefs[i].relChord;
          matchedMask = upperMaskOrig;
          break;
        }
      }


      // Second Pass, try to match to one of the submasks with each note removed

      if (matchedRelChord == null) {
        for (int i = 0; i < allChordDefs.length; i++) {
          for (int j = 0; j < numUpperMasks; j++) {
            if (allChordDefs[i].getSimpleChordAt(note).getChordMask().equals(upperMasks[j])) {
              matchedRelChord = allChordDefs[i].relChord;
              matchedMask = upperMasks[j];
              break;
            }
          }
          if (matchedRelChord != null) {
            break;
          }
        }
      }

      // If no registry chord found, just create a new chord
      if (matchedRelChord == null) {
        if (includeUnknown) {
          possChordDefs.add(getRotatedChordDef(fullNotelist, n));
        }
        continue;
      }

      // See if it's a simple chord with only one bass note added, otherwise
      // not a match

      //If matched a partial mask, than the difference must be a bass note!
      int bassNoteToMatch = upperMaskOrig & ~matchedMask;

      Note additionalBass = null;
      boolean validForm = true;

      int matchedChordCount = 0; // Number of matched chords for sorting

      for (int i = 0; i < Note.NUM_HALFSTEPS; i++) {
        if (fullNotelist[i] == null) {
          continue;
        }

        if (fullNotelist[i].isBassNote() != true) {
          System.out.println("isBassNote() false for " + fullNotelist[i]);
        }

        if (additionalBass != null) {
          validForm = false;
          continue;
        }

        if (!mask.contains(i)) {
          validForm = false;
          continue;
        }

        additionalBass = fullNotelist[i];
      }

      // Don't do additional bass for single note
      if (matchedRelChord.getOrigDef().ivals.length == 0) {
        additionalBass = null;
      }

      if (!validForm) {
        additionalBass = null;
      }

      if (removeInversion && (bassNoteToMatch == 0)) {
        additionalBass = null;
      }

      if (validForm || (bassNoteToMatch == 0)) {
        
        ParsedChordDef newChordDef =
                new ParsedChordDef(note, additionalBass, matchedRelChord, ParsedChordDef.BassSetting.LowestBass);

        possChordDefs.add(matchedChordCount++, newChordDef);
        if (findFirstOnly) {
          return possChordDefs;
        }

      } else {
        if (includeUnknown) {
          possChordDefs.add(getRotatedChordDef(fullNotelist, n));
        }
      }
    }

    return possChordDefs;
  }

  private static ParsedChordDef getRotatedChordDef(Note[] fullNotelist, int n)
  {
    Vector<Note> validNotes = new Vector<Note>();

    for (int i = 0; i < fullNotelist.length; i++) {
      int currIndex = (i + n) % fullNotelist.length;
      if (fullNotelist[currIndex] != null) {
        if (!fullNotelist[currIndex].isBassNote()) {
          validNotes.add(fullNotelist[currIndex]);
        }
      }
    }

    Chord theChord = new Chord(validNotes);

    ParsedChordDef chordDef = new ParsedChordDef(theChord);
    return chordDef;
  }
}
