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
  //private final RegistryChordDef registryDef;
  public final RelChord relChord;
  //public final short regRow, regCol;
  public final boolean bassLowest;

  public static ParsedChordDef getDefaultChordDef()
  {
    return new ParsedChordDef(new Note(), null, new RelChord(), false);
  }

  public ParsedChordDef(Note root)
  {
    this(new Chord(root, false));
  }

  public ParsedChordDef(Chord achord)
  {
    rootNote = achord.getRootNote();
    addedBassNote = null; //assume none
    chord = achord;

    if (achord.isSingleNote()) {
      namePlain = achord.toString();
      nameHtml = chord.toHtmlString();
    } else {
      namePlain = "[" + chord.toString() + "]";
      nameHtml = chord.toHtmlString();
    }

    relChord = null;
    detail = nameHtml;
    bassLowest = false;
  }

  public ParsedChordDef(Note root,
          Note addedBass,
          RelChord pickedChord,
          boolean addedBassLowest)
  {
    rootNote = root;

    relChord = (pickedChord != null ? pickedChord : new RelChord());

    Interval[] ivals;

    String tempAbbrevHtml;
    String tempAbbrevPlain;
    String tempDet;

    {
      RegistryChordDef origDef = relChord.origDef;

      // If custom chord
      if (origDef == null) {
        Chord simpleChord = relChord.buildChord(root);
        ivals = simpleChord.extractInterval();
        tempAbbrevHtml = simpleChord.toHtmlString();
        tempAbbrevPlain = simpleChord.toString();
        tempDet = tempAbbrevPlain;
      } else {
        ivals = origDef.ivals;

        tempAbbrevHtml = rootNote.toString(true) + origDef.abbrevHtml;
        
        tempAbbrevPlain = rootNote.toString();

        if (ivals.length > 0) {
          tempAbbrevPlain += origDef.abbrevPlain;
        }

        tempDet = rootNote.toString(true) + " " + origDef.name;
      }
    }

    addedBassNote = addedBass;

    chord = new Chord(rootNote, ivals,
            addedBassNote, addedBassLowest);


    // -- Set HTML Abbrev
    String html = tempAbbrevHtml;

    if (addedBassNote != null) {
      html += "/" + addedBassNote.toString(true);
    }
    nameHtml = html;

    // -- Set Plain Abbrev

    String plain = tempAbbrevPlain;

    if (addedBassNote != null) {
      plain += "/" + addedBassNote.toString();
    }
    namePlain = plain;

    // -- Set Name
    String det = tempDet;

    if (addedBassNote != null) {
      det += " over " + addedBassNote.toString(true);
    }
    detail = det;

    bassLowest = addedBassLowest;

  }

  public ParsedChordDef transposeBy(Interval ival)
  {
    Note newAddedBass = null;

    if (addedBassNote != null) {
      newAddedBass = addedBassNote.add(ival);
    }

    if (relChord == null) {
      return new ParsedChordDef(chord.transposeBy(ival));
    } else {
      return new ParsedChordDef(rootNote.add(ival), newAddedBass, this.relChord, this.bassLowest);
    }
  }
}
