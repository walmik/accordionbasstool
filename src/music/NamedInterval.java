package music;

public enum NamedInterval
{
  m2(1, 1, "Minor 2nd"),
  M2(2, 1, "Major 2nd"),
  m3(3, 2, "Minor 3rd"),
  M3(4, 2, "Major 3rd"),
  P4(5, 3, "Perfect 4th"),
  Aug4(6, 4, "Augmented 4th"),
  Dim5(6, 5, "Diminished 5th"),
  P5(7, 4, "Perfect 5th"),
  m6(8, 5, "Minor 6th"),
  M6(9, 5, "Major 6th"),
  m7(10, 6, "Minor 7th"),
  M7(11, 6, "Major 7th");

  NamedInterval(int halfStepIval, int scale, String name)
  {
    this.interval = new Interval(halfStepIval, scale);
    this.name = name;
  }

  @Override
  public String toString()
  {
    return name;
  }

  public final Interval interval;
  public final String name;
}
