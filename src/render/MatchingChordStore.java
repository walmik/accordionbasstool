package render;

import java.util.Vector;
import music.ButtonCombo;
import music.ChordRegistry;
import music.ParsedChordDef;

public class MatchingChordStore
{

  private Vector<ParsedChordDef> cacheMatchingChords;
  private int matchingChordCol = -1;
  private boolean removeInversion = false;
  private boolean validForNextChange = false;
  private SeqColumnModel columnModel;

  MatchingChordStore(SeqColumnModel columnModel)
  {
    this.columnModel = columnModel;
  }

  public Vector<ParsedChordDef> getAllMatchingSelChords(boolean preferCombo)
  {
    int colSel = columnModel.getSelectedColumn();
    if (colSel == -1) {
      cacheMatchingChords = new Vector<ParsedChordDef>();
      return cacheMatchingChords;
    }
    if ((cacheMatchingChords != null) && (matchingChordCol == colSel)) {
      return cacheMatchingChords;
    }

    ButtonCombo activeCombo = columnModel.getSelectedButtonCombo();

    if ((activeCombo != null) && !activeCombo.isEmpty()) {
      return getAllMatchingSelChords(activeCombo, preferCombo);
    }

    return getAllMatchingSelChords(columnModel.getSelectedChordDef());
  }

  public Vector<ParsedChordDef> getKnownMatchingSelChords(boolean preferCombo)
  {
    getAllMatchingSelChords(preferCombo);
    Vector<ParsedChordDef> knownChords = new Vector<ParsedChordDef>();
    for (ParsedChordDef def : cacheMatchingChords) {
      if (def.relChord.getOrigDef() != null) {
        knownChords.add(def);
      }
    }
    return knownChords;
  }

  public ParsedChordDef getFirstKnownMatchingChord(boolean preferCombo)
  {
    getAllMatchingSelChords(preferCombo);

    for (ParsedChordDef def : cacheMatchingChords) {
      if (def.relChord.getOrigDef() != null) {
        return def;
      }
    }

    return null;
  }

  public Vector<ParsedChordDef> getAllMatchingSelChords(ParsedChordDef existingChordDef)
  {
    if (existingChordDef == null) {
      cacheMatchingChords = new Vector<ParsedChordDef>();
    } else {
      cacheMatchingChords = ChordRegistry.mainRegistry().findAllChordsForNotes(existingChordDef.chord, removeInversion);
    }

    matchingChordCol = columnModel.getSelectedColumn();
    return cacheMatchingChords;
  }

  public Vector<ParsedChordDef> getAllMatchingSelChords(ButtonCombo activeCombo, boolean preferCombo)
  {
    if (activeCombo == null) {
      cacheMatchingChords = new Vector<ParsedChordDef>();
    } else {
      cacheMatchingChords = ChordRegistry.mainRegistry().findChordFromNotes(
              activeCombo.sortNotes(),
              activeCombo.getChordMask(), removeInversion, true, false);

      if (preferCombo) {
        for (ParsedChordDef def : cacheMatchingChords) {
          def.setPrefCombo(activeCombo);
        }
      }
    }

    matchingChordCol = columnModel.getSelectedColumn();
    return cacheMatchingChords;
  }

  public void setRemoveInversion(boolean value)
  {
    this.removeInversion = value;
    matchingChordCol = -1;
  }

  void setValid(boolean valid)
  {
    matchingChordCol = (valid ? columnModel.getSelectedColumn() : -1);
    validForNextChange = valid;
    if (!valid) {
      cacheMatchingChords = null;
    }
  }

  void resetIfNotValid()
  {
    if (!validForNextChange) {
      matchingChordCol = -1;
    }
    validForNextChange = false;
  }
}
