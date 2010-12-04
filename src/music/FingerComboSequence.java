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
public class FingerComboSequence implements Cloneable, CollSequence<FingerCombo>
{

  private ButtonComboSequence buttonSeq;
  private Vector<FingerCombo> combos;
  private int heur = 0;
  GeoPos[] currFingerPos;

  FingerComboSequence(ButtonComboSequence butSeq)
  {
    buttonSeq = butSeq;
    combos = new Vector<FingerCombo>();
    heur = buttonSeq.evalHeuristic();
  }

  private FingerComboSequence(ButtonComboSequence butSeq, 
          Vector<FingerCombo> copyCombos, int copyHeur, GeoPos[] copyPos)
  {
    buttonSeq = butSeq;
    combos = copyCombos;
    heur = copyHeur;
    currFingerPos = copyPos;

  }

//  private void initPos(FingerCombo firstCombo)
//  {
//    currFingerPos = new GeoPos[FingerSearcher.NUM_FINGERS];
//
//    GeoPos naturalFingerDir = new GeoPos(0, 1, FingerSearcher.SKEW_GRID, FingerSearcher.SKEW_ANGLE);
//
//    for (int i = 0; i < firstCombo.reverseFingerMap.length; i++) {
//      currFingerPos[firstCombo.reverseFingerMap[i]] = firstCombo.pos[i];
//    }
//
//    // Fill in the missing fingers with default positions
//
//    for (int i = 0; i < firstCombo.fingerMap.length; i++) {
//      if (currFingerPos[i] != null) {
//        continue;
//      }
//
//      int min = -1;
//      int max = -1;
//
//      for (int j = i - 1; j >= 0; j--) {
//        if (firstCombo.fingerMap[j] >= 0) {
//          min = j;
//          break;
//        }
//      }
//
//      for (int j = i + 1; j < firstCombo.fingerMap.length; j++) {
//        if (firstCombo.fingerMap[j] >= 0) {
//          max = j;
//          break;
//        }
//      }
//
//      if ((min >= 0) && (max >= 0)) {
//        currFingerPos[i] = firstCombo.getPosAtFinger(min).add(firstCombo.getPosAtFinger(max)).divide(2);
//      } else if (min >= 0) {
//        currFingerPos[i] = firstCombo.getPosAtFinger(min).subtract(naturalFingerDir.scale(i - min));
//      } else if (max >= 0) {
//        currFingerPos[i] = firstCombo.getPosAtFinger(max).add(naturalFingerDir.scale(max - i));
//      } else {
//        currFingerPos[i] = GeoPos.zero();
//      }
//
//    }
//  }

  public ButtonComboSequence getButtonComboSeq()
  {
    return buttonSeq;
  }

  public void add(FingerCombo combo, int transHeur, GeoPos[] newPos)
  {
//    if (combos.isEmpty()) {
//      initPos(combo);
//    }
    combos.add(combo);
    this.currFingerPos = newPos;
    heur += combo.heur;
    heur += transHeur;
  }

  @Override
  public int getNumCombos()
  {
    return combos.size();
  }

  @Override
  public FingerCombo getCombo(int index)
  {
    return combos.elementAt(index);
  }

  public FingerCombo getLastCombo()
  {
    return ((combos.size() > 0) ? combos.lastElement() : null);
  }

  public int getHeur()
  {
    return heur;
  }

  @Override
  public FingerComboSequence clone()
  {
    return new FingerComboSequence(buttonSeq, (Vector<FingerCombo>) combos.clone(), 
            heur, (currFingerPos != null ? currFingerPos.clone() : null));
  }
}
