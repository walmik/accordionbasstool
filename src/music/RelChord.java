/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package music;

import music.core.NamedInterval;
import music.core.Interval;
import music.core.Chord;
import music.core.Note;
import java.util.Vector;

/**
 *
 * @author Ilya
 */
public class RelChord implements Cloneable
{

//  public static enum BaseChordQual
//  {
//    Root,
//    Major,
//    Minor,
//    Sus4,
//    Sus2,
//  };
  public static enum NoteDegreeType
  {
    //   None("-"),

    DoubleFlat("bb"),
    Flat("b"),
    Normal("N"),
    Sharp("#"),
    DoubleSharp("##");
    public final String label;

    NoteDegreeType()
    {
      label = "";
    }

    NoteDegreeType(String str)
    {
      label = str;
    }

    @Override
    public String toString()
    {
//      if (this == None) {
//        return "-";
//      }
//
      if (!label.isEmpty()) {
        return label + " - " + super.toString();
      } else {
        return super.toString();
      }
    }
  }
  //private BaseChordQual base = BaseChordQual.Major;
  public final static Interval standardIval[] = {NamedInterval.M3.interval,
    NamedInterval.P5.interval,
    NamedInterval.M7.interval,
    NamedInterval.M2.interval,
    NamedInterval.P4.interval,
    NamedInterval.M6.interval};
  private NoteDegreeType steps[] = new NoteDegreeType[standardIval.length];
  private RegistryChordDef origDef = null;

  public RelChord(Chord chord)
  {
    int offset = 0;
    int length = chord.getNumNotes();

    if (length == 0) {
      return;
    }

    Note root = chord.getNoteAt(offset);

    for (int i = 1; i < chord.getNumNotes(); i++) {
      Note note = chord.getNoteAt(i % length);

      if (note == null) {
        continue;
      }

      Interval ival = note.diff(root);
      int matchScaleDist = ival.getNormScaleDist();
      int matchHalfStep = ival.getNormHalfStep();

      for (int j = 0; j < standardIval.length; j++) {
        if (matchScaleDist == standardIval[j].scaleDist) {
          int ivalDiff = matchHalfStep - standardIval[j].getNormHalfStep();
          if (ivalDiff == 0) {
            steps[j] = NoteDegreeType.Normal;
          } else if (ivalDiff > 0) {
            steps[j] = (ivalDiff == 1 ? NoteDegreeType.Sharp : NoteDegreeType.DoubleSharp);
          } else {
            steps[j] = (ivalDiff == -1 ? NoteDegreeType.Flat : NoteDegreeType.DoubleFlat);
          }
          break;
        }
      }
    }
  }

  public RelChord(String string)
  {
    int offset = 0;

//    if (string.startsWith("13")) {
//      base = BaseChordQual.Major;
//    } else if (string.startsWith("1b3")) {
//      base = BaseChordQual.Minor;
//    } else if (string.startsWith("14")) {
//      base = BaseChordQual.Sus4;
//    } else if (string.startsWith("12")) {
//      base = BaseChordQual.Sus2;
//    }

    offset = 0;

    NoteDegreeType nextType;

    while (offset < string.length()) {
      char c = string.charAt(offset);
      nextType = NoteDegreeType.Normal;

      if (c == 'b') {
        nextType = NoteDegreeType.Flat;
      } else if (c == '#') {
        nextType = NoteDegreeType.Sharp;
      } else if (c == '5') {
        steps[1] = nextType;
      } else if (c == '7') {
        steps[2] = nextType;
      } else if (c == '9') {
        steps[3] = nextType;
      } else if ((c == '1') && ((offset + 1) < string.length())) {
        c = string.charAt(++offset);
        if (c == '1') {
          steps[4] = nextType;
        } else if (c == '3') {
          steps[5] = nextType;
        }
      }

      offset++;
    }
  }

  private RelChord(NoteDegreeType[] steps, RegistryChordDef orig)
  {
    this.steps = steps;
    this.origDef = orig;
  }

  public RelChord()
  {
    this.steps = new NoteDegreeType[standardIval.length];
    origDef = ChordRegistry.mainRegistry().findSingleNoteChord();
  }

  @Override
  public RelChord clone()
  {
    return new RelChord((NoteDegreeType[]) steps.clone(), origDef);
  }

  public Chord buildChord(Note root)
  {
    Vector<Note> notes = new Vector<Note>();

    notes.add(root);

    for (int i = 0; i < standardIval.length; i++) {
      addNoteDegree(notes, steps[i], root, standardIval[i]);
    }

    return new Chord(notes);
  }

  public int getChordLength()
  {
    int count = 1;

    for (int i = 0; i < steps.length; i++) {
      if (steps[i] != null) {
        count++;
      }
    }

    return count;
  }

  public int getNumEditableSteps()
  {
    return steps.length;
  }

  public void setStep(int index, NoteDegreeType stepVal)
  {
    assert (index < getNumEditableSteps());
    steps[index] = stepVal;
  }

  public NoteDegreeType getStep(int index)
  {
    return steps[index];
  }

  private void addNoteDegree(Vector<Note> notes,
          NoteDegreeType type,
          Note root,
          Interval ival)
  {
    if (type == null) {
      return;
    }

    switch (type) {
      case Flat:
        notes.add(root.add(ival.flatten()));
        return;

      case Sharp:
        notes.add(root.add(ival.sharpen()));
        return;

      case DoubleFlat:
        notes.add(root.add(ival.flatten().flatten()));
        break;

      case DoubleSharp:
        notes.add(root.add(ival.sharpen().sharpen()));
        return;

      case Normal:
        notes.add(root.add(ival));
    }
  }

  public static int indexToStep(int index)
  {
    return index * 2 + 5;
  }

  public void setOrigDef(RegistryChordDef def)
  {
    origDef = def;
  }

  public RegistryChordDef getOrigDef()
  {
    return origDef;
  }

  @Override
  public String toString()
  {
    return toString(", ");
  }

  public String toString(String sep)
  {
    //String str = base.toString() + " ";
    String str = "";

    if (this.origDef != null) {
      return origDef.toString();
    }

    for (int i = 0; i < steps.length; i++) {
      int qual = indexToStep(i - 1);

      if (steps[i] == null) {
        continue;
      }

      if (!str.isEmpty()) {
        str += sep;
      }

      if (steps[i] != NoteDegreeType.Normal) {
        str += steps[i].label;
      }

      str += qual;
    }

    return str;
  }

  public boolean equals(Object object)
  {
    if (object == null) {
      return false;
    }

    RelChord relChord = (RelChord)object;

    for (int i = 0; i < standardIval.length; i++) {
      if (steps[i] != relChord.steps[i]) {
        return false;
      }
    }

    return true;
  }
}
