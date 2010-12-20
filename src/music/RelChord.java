/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package music;

import java.util.Vector;

/**
 *
 * @author Ilya
 */
public class RelChord implements Cloneable
{

  public static enum BaseChordQual
  {
    Root,
    Major,
    Minor,
    Sus4,
    Sus2,
  };

  public static enum NoteDegreeType
  {
    None,
    DoubleFlat("bb"),
    Flat("b"),
    Normal,
    Sharp("#"),
    DoubleSharp("##");

    String label;

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
      if (this == None) {
        return "-";
      }
      
      if (!label.isEmpty()) {
        return label + " - " + super.toString();
      } else {
        return super.toString();
      }
    }
  }

  private BaseChordQual base = BaseChordQual.Major;

  public final static Interval standardIval[] =
   {NamedInterval.M3.interval, NamedInterval.P5.interval, NamedInterval.M7.interval,
    NamedInterval.M2.interval, NamedInterval.P4.interval, NamedInterval.M6.interval};

  private NoteDegreeType steps[] = new NoteDegreeType[standardIval.length];


  public RelChord(Chord chord)
  {
    Note root = chord.notes[0];

    for (int i = 1; i < chord.notes.length; i++) {
      Note note = chord.notes[i];

      Interval ival = note.diff(root);

      for (int j = 0; j < standardIval.length; j++) {
        if (ival.scaleDist == standardIval[j].scaleDist) {
          int ivalDiff = ival.interval - standardIval[j].interval;
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
    
    // Find if Major, Minor or Sus2 or Sus4

    if (steps[0] == null) {
      if (steps[3] != null) {
        base = BaseChordQual.Sus2;
        steps[3] = null;
      } else if (steps[4] != null) {
        base = BaseChordQual.Sus4;
        steps[4] = null;
      } else {
        base = BaseChordQual.Root;
      }
    } else if (steps[0] == NoteDegreeType.Flat) {
      base = BaseChordQual.Minor;
    } else {
      base = BaseChordQual.Major;
    }

    for (int i = 1; i < steps.length; i++) {
      if (steps[i] == null) {
        steps[i] = NoteDegreeType.None;
      }
    }
  }

  public RelChord(String string)
  {
    int offset = 0;

    if (string.startsWith("13")) {
      base = BaseChordQual.Major;
    } else if (string.startsWith("1b3")) {
      base = BaseChordQual.Minor;
    } else if (string.startsWith("14")) {
      base = BaseChordQual.Sus4;
    } else if (string.startsWith("12")) {
      base = BaseChordQual.Sus2;
    }

    offset = 2;

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

    for (int i = 1; i < steps.length; i++)
    {
      if (steps[i] == null)
        steps[i] = NoteDegreeType.None;
    }
  }

  private RelChord(BaseChordQual base, NoteDegreeType[] steps)
  {
    this.base = base;
    this.steps = steps;
  }

  @Override
  public RelChord clone()
  {
    return new RelChord(base, (NoteDegreeType[])steps.clone());
  }

  public Chord buildChord(Note root)
  {
    Vector<Note> notes = new Vector<Note>();

    switch (base) {
      case Root:
        notes.add(root);
        break;

      case Major:
        notes.add(root);
        notes.add(root.add(NamedInterval.M3.interval));
        break;

      case Minor:
        notes.add(root);
        notes.add(root.add(NamedInterval.m3.interval));
        break;

      case Sus4:
        notes.add(root);
        notes.add(root.add(NamedInterval.P4.interval));
        break;

      case Sus2:
        notes.add(root);
        notes.add(root.add(NamedInterval.M2.interval));
        break;
    }

    for (int i = 1; i < standardIval.length; i++)
    {
      addNoteDegree(notes, steps[i], root, standardIval[i]);
    }

    return new Chord(notes);
  }

  public int getNumEditableSteps()
  {
    return steps.length - 1;
  }

  public void setStep(int index, NoteDegreeType stepVal)
  {
    assert(index < getNumEditableSteps());
    steps[index + 1] = stepVal;
  }

  public NoteDegreeType getStep(int index)
  {
    return steps[index + 1];
  }

  public BaseChordQual getBaseQual()
  {
    return base;
  }

  public void setBaseQual(BaseChordQual qual)
  {
    base = qual;
  }

  private void addNoteDegree(Vector<Note> notes,
          NoteDegreeType type,
          Note root,
          Interval ival)
  {
    switch (type) {
      case None:
        return;

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
    return index*2 + 5;
  }

  @Override
  public String toString()
  {
    String str = base.toString() + " ";

    for (int i = 1; i < steps.length; i++)
    {
      int qual = indexToStep(i - 1);
      
      if (steps[i] == NoteDegreeType.None) {
        continue;
      }

      str += steps[i].label + qual + ", ";
    }

    return str;
  }

  public boolean equals(RelChord relChord)
  {
    if (relChord == null) {
      return false;
    }

    if (base != relChord.base) {
      return false;
    }

    for (int i = 1; i < standardIval.length; i++) {
      if (steps[i] != relChord.steps[i]) {
        return false;
      }
    }

    return true;
  }
}
