/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package music;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import util.Main;

/**
 *
 * @author Ilya
 */
public class ChordRegistry
{

  private static ChordRegistry _mainRegistry = null;
  static RegistryChordDef defaultChord;

  public static ChordRegistry mainRegistry()
  {
    if (_mainRegistry == null) {
      _mainRegistry = new ChordRegistry();
      _mainRegistry.loadFromXml("chorddefs.xml");
    }

    return _mainRegistry;
  }
  //==============================================================
//  static public class ChordGroupSet
//  {
//    // Chord Defs (loaded from XML)
//
//    final RegistryChordDef groupedChordDefs[][];
//    public final String groupNames[];
//    public final int maxChordsInGroup;
//    public final String name;
//
//    ChordGroupSet(String chordSetName)
//    {
//      name = chordSetName;
//      groupedChordDefs = new RegistryChordDef[0][];
//      groupNames = new String[0];
//      maxChordsInGroup = 0;
//    }
//
//    ChordGroupSet(Element root)
//    {
//      name = root.getAttribute("name");
//      NodeList groups = root.getElementsByTagName("chordgroup");
//      groupNames = new String[groups.getLength()];
//      groupedChordDefs = new RegistryChordDef[groupNames.length][];
//      int maxChords = 0;
//
//      for (int i = 0; i < groupedChordDefs.length; i++) {
//        Element group = (Element) groups.item(i);
//        groupNames[i] = group.getAttribute("name");
//
//        NodeList chords = group.getElementsByTagName("chord");
//
//        groupedChordDefs[i] = new RegistryChordDef[chords.getLength()];
//        maxChords = Math.max(maxChords, groupedChordDefs[i].length);
//
//        for (int j = 0; j < groupedChordDefs[i].length; j++) {
//          Element chord = (Element) chords.item(j);
//          String chordName = chord.getAttribute("name");
//          String abbrev = chord.getAttribute("abbrev");
//          String notelist = chord.getAttribute("notes");
//          groupedChordDefs[i][j] = new RegistryChordDef(chordName, abbrev, notelist, j, i);
//        }
//      }
//
//      maxChordsInGroup = maxChords;
//    }
//
//    public RegistryChordDef getChordDef(int groupIndex, int chordIndex)
//    {
//      assert ((chordIndex >= 0) && (groupIndex >= 0));
//
//      if (chordIndex < groupedChordDefs[groupIndex].length) {
//        return groupedChordDefs[groupIndex][chordIndex];
//      } else {
//        return null;
//      }
//    }
//
//    public RegistryChordDef[] getChordDefs(int groupIndex)
//    {
//      assert(groupIndex >= 0);
//      return groupedChordDefs[groupIndex];
//    }
//
//    public RegistryChordDef findChord(StringParser parser)
//    {
//      int prevMatchLength = 0;
//      RegistryChordDef bestMatch = null;
//      String chordToMatch = parser.input();
//
//      for (int col = 0; col < groupedChordDefs.length; col++) {
//        for (int row = 0; row < groupedChordDefs[col].length; row++) {
//          RegistryChordDef currChord = groupedChordDefs[col][row];
//          String currAbbrev = currChord.abbrevPlain.trim();
//          if ((currAbbrev.length() > prevMatchLength)
//                  && chordToMatch.startsWith(currAbbrev)) {
//
//            bestMatch = currChord;
//            prevMatchLength = currAbbrev.length();
//          }
//        }
//      }
//
//      parser.incOffset(prevMatchLength);
//      return bestMatch;
//    }
//  }
  private RegistryChordDef allChordDefs[];

  public ChordRegistry()
  {
  }

  public RegistryChordDef[] getAllChords()
  {
    return allChordDefs;
  }

  public void loadFromXml(String filename)
  {
    Document doc = Main.loadXmlFile(filename, "./xml/");

    if (doc == null) {
      allChordDefs = new RegistryChordDef[0];
      return;
    }

    Element root = doc.getDocumentElement();

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
//  public ChordGroupSet findChordSet(String chordSetName)
//  {
//    if (allSets == null)
//      return null;
//
//    for (ChordGroupSet set : allSets) {
//      if (set.name.equals(chordSetName)) {
//        return set;
//      }
//    }
//
//    return null;
//  }
//
//  public RegistryChordDef findChord(String chordSetName, StringParser parser)
//  {
//    ChordGroupSet chordSet = findChordSet(chordSetName);
//
//    if (chordSet != null) {
//      return chordSet.findChord(parser);
//    } else {
//      return null;
//    }
//  }
//
//  public RegistryChordDef getDefaultChordDef()
//  {
//    if (allSets.length == 0)
//      return null;
//
//    if (allSets[0].groupedChordDefs.length == 0)
//      return null;
//
//    if (allSets[0].groupedChordDefs[0].length == 0)
//      return null;
//
//    return allSets[0].groupedChordDefs[0][0];
//  }
}
