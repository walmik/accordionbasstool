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
  private final RegistryChordDef registryDef;
  public final short regRow, regCol;

  public static ParsedChordDef getDefaultChordDef()
  {
    return new ParsedChordDef(new Note(), null, ChordRegistry.mainRegistry().getDefaultChordDef(), false);
  }

  public ParsedChordDef(Note root)
  {
    this(new Chord(root, false));
  }

  public ParsedChordDef(Chord achord)
  {
    rootNote = achord.getRootNote();
    addedBassNote = null; //assume none
    registryDef = null;
    chord = achord;

    if (achord.isSingleNote())
    {
      namePlain = achord.toString();
      nameHtml = chord.toHtmlString();
    }
    else
    {
      namePlain = "[" + chord.toString() + "]";
      nameHtml = chord.toHtmlString();
    }

    detail = nameHtml;
    regRow = regCol = 0;
  }

  public ParsedChordDef(Note root,
          Note addedBass,
          RegistryChordDef currTableChord,
          boolean addedBassLowest)
  {
    rootNote = root;

    // Use Default Chord
    if (currTableChord == null) {
      currTableChord = ChordRegistry.mainRegistry().getDefaultChordDef();
    }

    registryDef = currTableChord;
    
    //Unknown Chord if default doesn't exist..
    if (currTableChord == null) {
      addedBassNote = addedBass;
      chord = new Chord(root, false);
      namePlain = chord.toString();
      nameHtml = chord.toHtmlString();
      detail = nameHtml;
      regRow = regCol = 0;
      return;
    }

    regRow = currTableChord.row;
    regCol = currTableChord.col;

    addedBassNote = addedBass;

    chord = new Chord(rootNote, currTableChord.ivals,
            addedBassNote, addedBassLowest);


    // -- Set HTML Abbrev
    String html = rootNote.toString(true) + currTableChord.abbrevHtml;

    if (addedBassNote != null) {
      html += "/" + addedBassNote.toString(true);
    }
    nameHtml = html;

    // -- Set Plain Abbrev

    String plain = rootNote.toString();

    if (!chord.isSingleNote())
    {
      plain += currTableChord.abbrevPlain;
    }

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

  public ParsedChordDef transposeBy(Interval ival)
  {
    Note newAddedBass = null;
    
    if (addedBassNote != null) {
      newAddedBass = addedBassNote.add(ival);
    }

    //TODO: another pass on this,

    if (registryDef == null) {
      this.chord.transpose(ival);
      return new ParsedChordDef(this.chord);
    } else {
      return new ParsedChordDef(rootNote.add(ival), newAddedBass, this.registryDef, false);
    }
  }
}
