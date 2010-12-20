package music;

public enum NamedInterval
{
  m2(1, 1, "Min 2nd"),
  M2(2, 1, "Maj 2nd"),
  m3(3, 2, "Min 3rd"),
  M3(4, 2, "Maj 3rd"),
  P4(5, 3, "Perfect 4th"),
  Aug4(6, 4, "Aug 4th"),
  Dim5(6, 5, "Dim 5th"),
  P5(7, 4, "Perfect 5th"),
  m6(8, 5, "Min 6th"),
  M6(9, 5, "Maj 6th"),
  m7(10, 6, "Min 7th"),
  M7(11, 6, "Maj 7th");

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
