package music;

public class GeoPos
{

  static GeoPos minPos()
  {
    return new GeoPos(-Integer.MAX_VALUE, -Integer.MAX_VALUE);
  }

  static GeoPos maxPos()
  {
    return new GeoPos(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  static GeoPos zero()
  {
    return new GeoPos(0, 0);
  }
  public int x;
  public int y;

  public GeoPos(BassBoard.Pos pos, BassBoard.Pos center)
  {
    set(pos, center);
  }

  private GeoPos(int nx, int ny)
  {
    x = nx;
    y = ny;
  }

  int getRow()
  {
    return y;
  }

  final static int GRID_SCALE = 1;
  //final static int SKEW_SCALE = (int)(Math.tan(20) * GRID_SCALE);

  void set(BassBoard.Pos boardPos, BassBoard.Pos center)
  {
    x = boardPos.col - center.col;
    y = boardPos.row - center.row;
    //skewTrans(GRID_SCALE, SKEW_SCALE);
  }

  void add(GeoPos pos)
  {
    x += pos.x;
    y += pos.y;
  }

  void subtract(GeoPos pos)
  {
    x -= pos.x;
    y -= pos.y;
  }

  void divide(int scale)
  {
    x /= scale;
    y /= scale;
  }

  void max(GeoPos pos)
  {
    x = Math.max(x, pos.x);
    y = Math.max(y, pos.y);
  }

  void min(GeoPos pos)
  {
    x = Math.min(x, pos.x);
    y = Math.min(y, pos.y);
  }

  int absValue()
  {
    return Math.abs(x) + Math.abs(y);
  }

  int manDistTo(GeoPos another)
  {
    int xD = Math.abs(x - another.x);
    int yD = Math.abs(y - another.y);
    return Math.max(xD, yD);
  }

  private void skewTrans(int scale, int skew)
  {
    x = (x * scale) + (skew * y);
    y = y * scale;
  }
}
