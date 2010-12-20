package music;
/**
 * 
 */

public class Interval
{
  public final static Interval halfStep = new Interval(1, 0);

	
	public final short interval;
	public final short scaleDist;
	
	public Interval(int val, int dist)
	{
		interval = (short)val;
		scaleDist = (short)dist;
	}
	
	public Interval scale(int scaler)
	{
		int newInterval = (interval * scaler);// % 12;
		int newScaleDist = (scaleDist * scaler);// % 6;
		return new Interval(newInterval, newScaleDist);
	}

  public Interval flatten()
  {
    return new Interval(interval - 1, scaleDist);
  }

  public Interval sharpen()
  {
    return new Interval(interval + 1, scaleDist);
  }
}