package render;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

public class SeqTableEventAdapter implements TableColumnModelListener
{
  public SeqTableEventAdapter()
  {
  }

  @Override
  public void columnAdded(TableColumnModelEvent e)
  {
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
  public final void columnSelectionChanged(ListSelectionEvent e)
  {

  }

  public void columnCountChanged(TableColumnModelEvent e)
  {
  }
}
