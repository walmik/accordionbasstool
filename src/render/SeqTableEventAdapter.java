/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import javax.swing.JList;
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
  int lastIndex = -1;

  public void clearLastIndex()
  {
    lastIndex = -2;
  }

  @Override
  public void columnAdded(TableColumnModelEvent e)
  {
    clearLastIndex();
    columnCountChanged(e);
  }

  @Override
  public void columnMarginChanged(ChangeEvent e)
  {
  }

  @Override
  public void columnMoved(TableColumnModelEvent e)
  {
    clearLastIndex();
  }

  @Override
  public void columnRemoved(TableColumnModelEvent e)
  {
    clearLastIndex();
    columnCountChanged(e);
  }

  @Override
  public final void valueChanged(ListSelectionEvent e)
  {
    if (e.getSource() instanceof ListSelectionModel) {
      handleSelectionChanged(((ListSelectionModel) e.getSource()).getAnchorSelectionIndex());
    } else if (e.getSource() instanceof JList) {
      handleSelectionChanged(((JList) e.getSource()).getSelectedIndex());
    }
  }

  @Override
  public final void columnSelectionChanged(ListSelectionEvent e)
  {
    if (e.getSource() instanceof ListSelectionModel) {
      handleSelectionChanged(((ListSelectionModel) e.getSource()).getAnchorSelectionIndex());
    }
  }

  private void handleSelectionChanged(int index)
  {
    if (index != lastIndex) {
      if (index >= 0) {
        selectionChanged(index);
      }
      lastIndex = index;
    }
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
    clearLastIndex();
  }
}
