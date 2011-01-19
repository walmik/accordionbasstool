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
public class SeqTableEventAdapter implements TableColumnModelListener,
        TableModelListener,
        ListSelectionListener
{

  //int lastIndex = -1;
  boolean acceptNoneSel;
  boolean isAlreadyAdjusting = false;

  public SeqTableEventAdapter()
  {
    acceptNoneSel = false;
  }

  public SeqTableEventAdapter(boolean noneSel)
  {
    acceptNoneSel = noneSel;
  }

//  public void clearLastIndex()
//  {
//    lastIndex = -2;
//  }

  @Override
  public void columnAdded(TableColumnModelEvent e)
  {
    //clearLastIndex();
    columnCountChanged(e);
  }

  @Override
  public void columnMarginChanged(ChangeEvent e)
  {
  }

  @Override
  public void columnMoved(TableColumnModelEvent e)
  {
    //clearLastIndex();
  }

  @Override
  public void columnRemoved(TableColumnModelEvent e)
  {
    //clearLastIndex();
    columnCountChanged(e);
  }

  @Override
  public final void valueChanged(ListSelectionEvent e)
  {
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

  @Override
  public final void columnSelectionChanged(ListSelectionEvent e)
  {
    valueChanged(e);
  }

  protected void selectionChanged(int index)
  {
  }

  public void columnCountChanged(TableColumnModelEvent e)
  {
  }

  @Override
  public void tableChanged(TableModelEvent e)
  {
    //clearLastIndex();
  }
}
