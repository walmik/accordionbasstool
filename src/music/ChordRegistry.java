/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package music;

import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import util.Main.StringParser;

/**
 *
 * @author Ilya
 */
public class ChordRegistry
{
  // Stradella Chords

  final static Interval[] MAJOR = {Interval.M3, Interval.m3};
  final static Interval[] MINOR = {Interval.m3, Interval.M3};
  final static Interval[] DOM = {Interval.M3, Interval.Dim5};
  final static Interval[] DIM = {Interval.m3, Interval.Dim5};
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
  static public class ChordGroupSet
  {
    // Chord Defs (loaded from XML)

    final RegistryChordDef groupedChordDefs[][];
    public final String groupNames[];
    public final int maxChordsInGroup;
    public final String name;

    ChordGroupSet(String chordSetName)
    {
      name = chordSetName;
      groupedChordDefs = new RegistryChordDef[0][];
      groupNames = new String[0];
      maxChordsInGroup = 0;
    }

    ChordGroupSet(Element root)
    {
      name = root.getAttribute("name");
      NodeList groups = root.getElementsByTagName("chordgroup");
      groupNames = new String[groups.getLength()];
      groupedChordDefs = new RegistryChordDef[groupNames.length][];
      int maxChords = 0;

      for (int i = 0; i < groupedChordDefs.length; i++) {
        Element group = (Element) groups.item(i);
        groupNames[i] = group.getAttribute("name");

        NodeList chords = group.getElementsByTagName("chord");

        groupedChordDefs[i] = new RegistryChordDef[chords.getLength()];
        maxChords = Math.max(maxChords, groupedChordDefs[i].length);

        for (int j = 0; j < groupedChordDefs[i].length; j++) {
          Element chord = (Element) chords.item(j);
          String chordName = chord.getAttribute("name");
          String abbrev = chord.getAttribute("abbrev");
          String notelist = chord.getAttribute("notes");
          groupedChordDefs[i][j] = new RegistryChordDef(chordName, abbrev, notelist, j, i);
        }
      }

      maxChordsInGroup = maxChords;
    }

    public RegistryChordDef getChordDef(int groupIndex, int chordIndex)
    {
      assert ((chordIndex >= 0) && (groupIndex >= 0));

      if (chordIndex < groupedChordDefs[groupIndex].length) {
        return groupedChordDefs[groupIndex][chordIndex];
      } else {
        return null;
      }
    }

    public RegistryChordDef findChord(StringParser parser)
    {
      int prevMatchLength = 0;
      RegistryChordDef bestMatch = null;
      String chordToMatch = parser.input();

      for (int col = 0; col < groupedChordDefs.length; col++) {
        for (int row = 0; row < groupedChordDefs[col].length; row++) {
          RegistryChordDef currChord = groupedChordDefs[col][row];
          String currAbbrev = currChord.abbrevPlain.trim();
          if ((currAbbrev.length() > prevMatchLength)
                  && chordToMatch.startsWith(currAbbrev)) {

            bestMatch = currChord;
            prevMatchLength = currAbbrev.length();
          }
        }
      }

      parser.incOffset(prevMatchLength);
      return bestMatch;
    }
  }
  public final static String ALL_CHORDS = "AllChordSet";
  public final static String SIMPLE_CHORDS = "SimpleChordSet";
  private ChordGroupSet[] allSets;

  public ChordRegistry()
  {
  }

  private Document tryDoc(DocumentBuilderFactory dbf, String string)
  {
    try {
      URL url = getClass().getClassLoader().getResource(string);
      if (url != null) {
        string = url.toString();
      }

      Document doc = dbf.newDocumentBuilder().parse(string);

      System.out.println("Read Doc From: " + string);

      return doc;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public void loadFromXml(String filename)
  {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    Document doc = tryDoc(dbf, filename);

    if (doc == null) {
      doc = tryDoc(dbf, "./xml/" + filename);
    }

    if (doc == null) {
      allSets = new ChordGroupSet[1];
      allSets[0] = new ChordGroupSet(ALL_CHORDS);
      return;
    }

    Element root = doc.getDocumentElement();

    NodeList chordsets = root.getElementsByTagName("chordset");

    allSets = new ChordGroupSet[chordsets.getLength()];

    for (int i = 0; i < chordsets.getLength(); i++) {
      allSets[i] = new ChordGroupSet((Element) chordsets.item(i));
    }
  }

  public ChordGroupSet findChordSet(String chordSetName)
  {
    for (ChordGroupSet set : allSets) {
      if (set.name.equals(chordSetName)) {
        return set;
      }
    }

    return null;
  }

  public RegistryChordDef findChord(String chordSetName, StringParser parser)
  {
    ChordGroupSet chordSet = findChordSet(chordSetName);

    if (chordSet != null) {
      return chordSet.findChord(parser);
    } else {
      return null;
    }
  }

  public RegistryChordDef getDefaultChordDef()
  {
    if (allSets.length == 0)
      return null;

    if (allSets[0].groupedChordDefs.length == 0)
      return null;

    if (allSets[0].groupedChordDefs[0].length == 0)
      return null;

    return allSets[0].groupedChordDefs[0][0];
  }
}
