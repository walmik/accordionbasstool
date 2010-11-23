/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package music;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Ilya
 */
public class BoardRow
{

  final String name;
  final String desc;
  final String shortname;
  final boolean isSingleNote;
  final Interval[] ivals;
  final Interval firstNoteOffset;

  BoardRow(Element root)
  {
    name = root.getAttribute("name");
    desc = root.getAttribute("desc");
    shortname = root.getAttribute("shortname");

    String chordName = root.getAttribute("chord");
    Chord chord = ChordParser.parseNoteList(new StringParser(chordName));

    isSingleNote = chord.isSingleNote();
    ivals = chord.extractInterval();
    // Get Offset from C
    firstNoteOffset = chord.getRootNote().diff(new Note());
  }

  @Override
  public String toString()
  {
    return desc;
  }

  Chord getChord(Note rootNote)
  {
    if (isSingleNote) {
      return new Chord(rootNote.add(firstNoteOffset), true);
    } else {
      return new Chord(rootNote.add(firstNoteOffset), ivals);
    }
  }

  String getChordName(Note rootNote, boolean html)
  {
    return rootNote.add(firstNoteOffset).toString(html) + shortname;
  }
  
  static BoardRow[] allRows;

  public static void loadRows(Element root)
  {
    NodeList nodes = root.getElementsByTagName("rows");

    if (nodes.getLength() == 0) {
      allRows = new BoardRow[0];
      return;
    }

    nodes = ((Element) nodes.item(0)).getElementsByTagName("rowdef");

    allRows = new BoardRow[nodes.getLength()];

    for (int i = 0; i < allRows.length; i++) {
      allRows[i] = new BoardRow((Element) nodes.item(i));
    }
  }

  public static BoardRow findFromString(String name)
  {
    for (int i = 0; i < allRows.length; i++) {
      if (allRows[i].name.equals(name)) {
        return allRows[i];
      }
    }

    return null;
  }

  public static BoardRow[] findRows(BassBoard.RowType[] rowTypes)
  {
    BoardRow rows[] = new BoardRow[rowTypes.length];
    //TODO: search efficiency
    for (int i = 0; i < rowTypes.length; i++) {
      rows[i] = findFromString(rowTypes[i].toString());
    }

    return rows;
  }

  public static BoardRow[] findRows(String[] names)
  {
    BoardRow rows[] = new BoardRow[names.length];
    //TODO: search efficiency
    for (int i = 0; i < names.length; i++) {
      rows[i] = findFromString(names[i]);
    }

    return rows;
  }
}


