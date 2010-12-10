package music;
/**
 * 
 */

public class Interval
{
  public final static Interval halfStep = new Interval(1, 0);
	public final static Interval m2 = new Interval(1, 1);
  public final static Interval P5 = new Interval(7, 4);

  static public enum NamedInterval
  {
    m2(1, 1, "Min 2nd"),
    M2(1, 1, "Maj 2nd"),
    m3(3, 2, "Min 3rd"),
    M4(4, 2, "Maj 3rd"),
    P4(5, 3, "Perfect 4th"),
    Aug4(6, 4, "Aug 4th"),
    Dim5(6, 5, "Dim 5th"),
    P5(7, 4, "Perfect 5th"),
    m6(8, 5, "Min 6th"),
    M6(9, 5, "Maj 6th"),
    m7(10, 6, "Min 7th"),
    M7(11, 6, "Maj 7th");

    NamedInterval(int interval, int scale, String name)
    {
      this.interval = (short)interval;
      this.scale = (short)scale;
      this.name = name;
    }

    @Override
    public String toString()
    {
      return name;
    }

    public Interval getInterval()
    {
      return new Interval(interval, scale);
    }

    short interval;
    short scale;
    String name;

  }

//	final static Interval P1 = new Interval(0, 0, null);
//	final static Interval m2 = new Interval(1, 1, "Minor 2nd");
//	final static Interval M2 = new Interval(2, 1, "Major 2nd");
//	final static Interval Dim3 = new Interval(2, 2, null);
//	final static Interval m3 = new Interval(3, 2, "Minor 3rd");
//	final static Interval M3 = new Interval(4, 2, "Major 3rd");
//	final static Interval P4 = new Interval(5, 3, "Perfect 4th");
//	final static Interval Aug4 = new Interval(6, 3, "Augmented 4th");
//	final static Interval Dim5 = new Interval(6, 4, "Diminished 5th");
//	final static Interval P5 = new Interval(7, 4, "Perfect 5th");
//	final static Interval Aug5 = new Interval(8, 4, null);
//	final static Interval m6 = new Interval(8, 5, "Minor 6th");
//	final static Interval M6 = new Interval(9, 5, "Major 6th");
//	final static Interval Dim7 = new Interval(9, 6, null);
//	final static Interval m7 = new Interval(10, 6, "Minor 7th");
//	final static Interval M7 = new Interval(11, 6, "Major 7th");
//  final static Interval P8 = new Interval(12, 7, "Perfect Octave");
	
	public final int interval;
	public final int scaleDist;
	
	public Interval(int val, int dist)
	{
		interval = val;
		scaleDist = dist;
	}
	
	public Interval scale(int scaler)
	{
		int newInterval = (interval * scaler);// % 12;
		int newScaleDist = (scaleDist * scaler);// % 6;
		return new Interval(newInterval, newScaleDist);
	}
}