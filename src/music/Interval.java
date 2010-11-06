package music;
/**
 * 
 */

public class Interval
{
	final static Interval P1 = new Interval(0, 0, null);
  public final static Interval halfStep = new Interval(1, 0, null);
	final static Interval m2 = new Interval(1, 1, "Minor 2nd");
	final static Interval M2 = new Interval(2, 1, "Major 2nd");
	final static Interval Dim3 = new Interval(2, 2, null);
	final static Interval m3 = new Interval(3, 2, "Minor 3rd");
	final static Interval M3 = new Interval(4, 2, "Major 3rd");
	final static Interval P4 = new Interval(5, 3, "Perfect 4th");
	final static Interval Aug4 = new Interval(6, 3, "Augmented 4th");
	final static Interval Dim5 = new Interval(6, 4, "Diminished 5th");
	final static Interval P5 = new Interval(7, 4, "Perfect 5th");
	final static Interval Aug5 = new Interval(8, 4, null);
	final static Interval m6 = new Interval(8, 5, "Minor 6th");
	final static Interval M6 = new Interval(9, 5, "Major 6th");
	final static Interval Dim7 = new Interval(9, 6, null);
	final static Interval m7 = new Interval(10, 6, "Minor 7th");
	final static Interval M7 = new Interval(11, 6, "Major 7th");
  final static Interval P8 = new Interval(12, 7, "Perfect Octave");
	
	public final int interval;
	public final int scaleDist;
  public final String name;
	
	public Interval(int val, int dist, String iname)
	{
		interval = val;
		scaleDist = dist;
    name = iname;
	}
	
	public Interval scale(int scaler)
	{
		int newInterval = (interval * scaler);// % 12;
		int newScaleDist = (scaleDist * scaler);// % 6;
		return new Interval(newInterval, newScaleDist, null);
	}
}