/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package music;

/**
 *
 * @author Ilya
 */
public class ParsedChordDef
{

  public final String namePlain;
  public final String nameHtml;
  public final String detail;
  public final Note rootNote;
  public final Note addedBassNote;
  public final Chord chord;
  //public final RegistryChordDef registryDef;
  public final short regRow, regCol;

  public ParsedChordDef()
  {
    this(new Note());
  }

  public ParsedChordDef(Note root)
  {
    rootNote = root;
    addedBassNote = null;
    chord = new Chord(root, false);
    namePlain = chord.toString();
    nameHtml = chord.toHtmlString();
    detail = nameHtml;
    regRow = regCol = 0;
  }

  public ParsedChordDef(Chord achord)
  {
    rootNote = achord.getRootNote();
    addedBassNote = null; //assume none
    chord = achord;
    namePlain = chord.toString();
    nameHtml = chord.toHtmlString();
    detail = nameHtml;
    regRow = regCol = 0;
  }

  public ParsedChordDef(Note root,
          Note addedBass,
          RegistryChordDef currTableChord,
          boolean addedBassLowest)
  {
    rootNote = root;

    //Unknown Chord
    if (currTableChord == null) {
      addedBassNote = null;
      chord = new Chord(root, false);
      namePlain = chord.toString();
      nameHtml = chord.toHtmlString();
      detail = nameHtml;
      regRow = regCol = 0;
      System.out.println("Null ChordDef");
      return;
    }

    regRow = currTableChord.row;
    regCol = currTableChord.col;

    addedBassNote = addedBass;

    chord = new Chord(currTableChord.chord,
            rootNote,
            addedBassNote, addedBassLowest);


    // -- Set HTML Abbrev
    String html = rootNote.toString(true) + currTableChord.abbrevHtml;

    if (addedBassNote != null) {
      html += "/" + addedBassNote.toString(true);
    }
    nameHtml = html;

    // -- Set Plain Abbrev

    String plain = rootNote.toString() + currTableChord.abbrevPlain;

    if (addedBassNote != null) {
      plain += "/" + addedBassNote.toString();
    }
    namePlain = plain;

    // -- Set Name
    String det = rootNote.toString(true) + " " + currTableChord.name;

    if (addedBassNote != null) {
      det += " over " + addedBassNote.toString(true);
    }
    detail = det;

  }
}
