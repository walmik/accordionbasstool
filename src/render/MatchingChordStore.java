package render;

import java.util.Vector;
import music.ButtonCombo;
import music.ChordRegistry;
import music.ParsedChordDef;

public class MatchingChordStore
{
  private Vector<ParsedChordDef> cacheMatchingChords;
  private int matchingChordCol = -1;

  private boolean optIncludeInv = true;
  private boolean optFirstOnly = false;
  private boolean validForNextChange = false;
  private SeqColumnModel columnModel;

  MatchingChordStore(SeqColumnModel columnModel)
  {
    this.columnModel = columnModel;
  }

  public Vector<ParsedChordDef> getAllMatchingSelChords()
  {
    int colSel = columnModel.getSelectedColumn();
    if (colSel == -1) {
      cacheMatchingChords = new Vector<ParsedChordDef>();
      return cacheMatchingChords;
    }
    if ((cacheMatchingChords != null) && (matchingChordCol == colSel)) {
      return cacheMatchingChords;
    }

    return getAllMatchingSelChords(columnModel.getSelectedChordDef());
    //ButtonCombo activeCombo = columnModel.getSelectedButtonCombo();
    //return getAllMatchingSelChords(activeCombo);
  }

  public Vector<ParsedChordDef> getKnownMatchingSelChords()
  {
    getAllMatchingSelChords();
    Vector<ParsedChordDef> knownChords = new Vector<ParsedChordDef>();
    for (ParsedChordDef def : cacheMatchingChords) {
      if (def.relChord.getOrigDef() != null) {
        knownChords.add(def);
      }
    }
    return knownChords;
  }

  public ParsedChordDef getFirstKnownMatchingChord()
  {
    getAllMatchingSelChords();

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
      cacheMatchingChords = ChordRegistry.mainRegistry().findAllChordsForNotes(existingChordDef.chord, optIncludeInv);
    }

    matchingChordCol = columnModel.getSelectedColumn();
    return cacheMatchingChords;
  }

  public Vector<ParsedChordDef> getAllMatchingSelChords(ButtonCombo activeCombo)
  {
    if (activeCombo == null) {
      cacheMatchingChords = new Vector<ParsedChordDef>();
    } else {
      cacheMatchingChords = ChordRegistry.mainRegistry().findChordFromNotes(
              ButtonCombo.sortedNotes,
              activeCombo.getChordMask(),
              optIncludeInv,
              true,
              optFirstOnly);
    }
    matchingChordCol = columnModel.getSelectedColumn();
    return cacheMatchingChords;
  }

  public void toggleOptInclude(boolean value)
  {
    this.optIncludeInv = value;
    this.matchingChordCol = -1;
  }

  public void toggleOptFirstOnly(boolean value)
  {
    this.optFirstOnly = value;
    this.matchingChordCol = -1;
  }

  void setValid(Vector<ParsedChordDef> matchingChords)
  {
    matchingChordCol = columnModel.getSelectedColumn();
    cacheMatchingChords = matchingChords;
    validForNextChange = true;
  }

  void resetIfNotValid()
  {
    if (!validForNextChange) {
      matchingChordCol = -1;
    }
    validForNextChange = false;
  }
}
