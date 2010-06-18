package music;
/**
 * 
 */

public class Interval
{
	final static Interval P1 = new Interval(0, 0);
	final static Interval m2 = new Interval(1, 1);
	final static Interval M2 = new Interval(2, 1);
	final static Interval Dim3 = new Interval(2, 2);
	final static Interval m3 = new Interval(3, 2);
	final static Interval M3 = new Interval(4, 2);
	final static Interval P4 = new Interval(5, 3);
	final static Interval Aug4 = new Interval(6, 3);
	final static Interval Dim5 = new Interval(6, 4);
	final static Interval P5 = new Interval(7, 4);
	final static Interval Aug5 = new Interval(8, 4);
	final static Interval m6 = new Interval(8, 5);
	final static Interval M6 = new Interval(9, 5);
	final static Interval Dim7 = new Interval(9, 6);
	final static Interval m7 = new Interval(10, 6);
	final static Interval M7 = new Interval(11, 6);
	
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