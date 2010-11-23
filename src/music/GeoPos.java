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

  public GeoPos(BassBoard.Pos pos, BassBoard.Pos center, int grid, int skewAngle)
  {
    set(pos, center, grid, skewAngle);
  }

  public GeoPos(int nx, int ny, int grid, int skewAngle)
  {
    x = nx;
    y = ny;
    skewTrans(grid, skewAngle);
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

  //final static int GRID_SCALE = 1;
  //final static int SKEW_SCALE = (int)(Math.tan(20) * GRID_SCALE);

  void set(BassBoard.Pos boardPos, BassBoard.Pos center)
  {
    x = boardPos.col - center.col;
    y = boardPos.row - center.row;
  }

  void set(BassBoard.Pos boardPos, BassBoard.Pos center, int grid, int skewAngle)
  {
    set(boardPos, center);
    skewTrans(grid, skewAngle);
  }

  GeoPos add(GeoPos pos)
  {
    return new GeoPos(x + pos.x, y + pos.y);
  }

  GeoPos subtract(GeoPos pos)
  {
    return new GeoPos(x - pos.x, y - pos.y);
  }

  GeoPos scale(int scale)
  {
    return new GeoPos(x * scale, y * scale);
  }
  
  GeoPos divide(int scale)
  {
    return new GeoPos(x / scale, y / scale);
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

  double dot(GeoPos pos)
  {
    double mag = Math.sqrt((pos.x * pos.x) + (pos.y + pos.y));
    return (x + pos.x/mag) + (y + pos.y/mag);
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

  private void skewTrans(int scale, int skewAngle)
  {
    int skew = (int)(Math.tan(skewAngle) * scale);
    x = (x * scale) + (skew * y);
    y = y * scale;
  }
}
