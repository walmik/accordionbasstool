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
  
  FingerComboSequence(ButtonComboSequence butSeq)
  {
    buttonSeq = butSeq;
    combos = new Vector<FingerCombo>();
    heur = buttonSeq.evalHeuristic();
  }

  private FingerComboSequence(ButtonComboSequence butSeq, Vector<FingerCombo> copyCombos, int copyHeur)
  {
    buttonSeq = butSeq;
    combos = copyCombos;
    heur = copyHeur;
  }

  public ButtonComboSequence getButtonComboSeq()
  {
    return buttonSeq;
  }

  public void add(FingerCombo combo, int transHeur)
  {
    combos.add(combo);
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
    return new FingerComboSequence(buttonSeq, (Vector<FingerCombo>)combos.clone(), heur);
  }
}
