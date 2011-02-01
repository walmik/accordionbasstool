/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Ilya
 */
public abstract class ListSelChangeListener implements ListSelectionListener
{

  boolean acceptNoneSel;
  boolean isAlreadyAdjusting = false;
  //short lastIndex = -1;

  public ListSelChangeListener()
  {
    acceptNoneSel = false;
  }

  public ListSelChangeListener(boolean noneSel)
  {
    acceptNoneSel = noneSel;
  }
  static boolean isClearing = false;

  public static void setIsClearing(boolean clearing)
  {
    isClearing = clearing;
  }

  @Override
  public final void valueChanged(ListSelectionEvent e)
  {
    if (!e.getValueIsAdjusting() && this.isAlreadyAdjusting) {
      this.isAlreadyAdjusting = false;
      return;
    }

    this.isAlreadyAdjusting = e.getValueIsAdjusting();

    ListSelectionModel model = ((ListSelectionModel) e.getSource());

    int index = model.getAnchorSelectionIndex();
    
    if (isClearing) {
      index = -1;
    }

    //System.out.println(e.getSource());

    //if (index != lastIndex) {
    if (acceptNoneSel || (index >= 0)) {
      selectionChanged(index);
    }
    //lastIndex = (short)index;
    //}
  }

  abstract protected void selectionChanged(int index);
}
