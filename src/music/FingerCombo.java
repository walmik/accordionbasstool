/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package music;

/**
 *
 * @author Ilya
 */
public class FingerCombo
{
  ButtonCombo buttoncombo;
  final GeoPos[] pos;
  byte[] reverseFingerMap;
  byte[] fingerMap;
  int heur;

  FingerCombo(ButtonCombo buttonc, GeoPos[] geopos, byte[] fm, byte[] rfm, int h)
  {
    buttoncombo = buttonc;
    fingerMap = fm;
    reverseFingerMap = rfm;
    heur = h;
    pos = geopos;
  }

  public int getFingerAt(int row, int col)
  {
    for (int i = 0; i < buttoncombo.pos.length; i++)
    {
      if (buttoncombo.pos[i].equals(row, col)) {
        return toFinger(i);
      }
    }
    
    return -1;
  }

  public GeoPos getPosAtFinger(int finger)
  {
    assert(finger >= 0 && fingerMap[finger] >= 0);
    return pos[fingerMap[finger]];
  }

  private int toFinger(int index)
  {
    return (reverseFingerMap[index] + 2);
  }

  @Override
  public String toString()
  {
    String str = "";
    for (int i = 0; i < reverseFingerMap.length; i++) {
      if (i > 0) {
        str += ", ";
      }
      str += toFinger(i) + " on ";
      str += buttoncombo.getButtonNameAt(i, false);
    }
    return str;
  }

  public ButtonCombo getButtonCombo()
  {
    return buttoncombo;
  }

//  void acceptFingerCombo()
//  {
//    byte[] fingers = new byte[4];
//
//    for (int f = 0; f < fingerLayouts.size(); f++) {
//      boolean comboValid = false;
//
//      int fingInt = fingerLayouts.elementAt(f).intValue();
//
//      fingers[0] = (byte) (fingInt & 0xFF);
//      fingInt >>= 8;
//      fingers[1] = (byte) (fingInt & 0xFF);
//      fingInt >>= 8;
//      fingers[2] = (byte) (fingInt & 0xFF);
//      fingInt >>= 8;
//      fingers[3] = (byte) (fingInt & 0xFF);
//
//      for (int i = 0; i < pos.length - 1; i++) {
//        int dist = pos[i + 1].manDistTo(pos[i]);
//
//        // Filter out max finger dist
//        if (dist > ((fingers[i + 1] - fingers[i]) * MAX_DIST_BTWN_FINGER)) {
//          break;
//        }
//      }
//
//      for (int i = 0; i < pos.length - 2; i++) {
//        GeoPos v1 = pos[i + 1].subtract(pos[i]);
//        GeoPos v2 = pos[i + 2].subtract(pos[i + 1]);
//      }
//    }
//  }


}
