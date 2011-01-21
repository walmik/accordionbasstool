/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 *
 * @author Ilya
 */
public abstract class ListSelChangeListener implements ListSelectionListener
{

  boolean acceptNoneSel;
  boolean isAlreadyAdjusting = false;

  public ListSelChangeListener()
  {
    acceptNoneSel = false;
  }

  public ListSelChangeListener(boolean noneSel)
  {
    acceptNoneSel = noneSel;
  }

  @Override
  public final void valueChanged(ListSelectionEvent e)
  {
    //if (this.getClass().equals(NoDupSingleSelListener.class)) {

    //}

    if (!e.getValueIsAdjusting() && this.isAlreadyAdjusting) {
      this.isAlreadyAdjusting = false;
      return;
    }

    this.isAlreadyAdjusting = e.getValueIsAdjusting();

    int index = ((ListSelectionModel) e.getSource()).getAnchorSelectionIndex();

    //if (index != lastIndex) {
    if (acceptNoneSel || (index >= 0)) {
      selectionChanged(index);
    }
    //lastIndex = index;
    //}
  }

  abstract protected void selectionChanged(int index);
}
