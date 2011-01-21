package render;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public abstract class SeqTableEventAdapter extends ListSelChangeListener implements TableColumnModelListener, TableModelListener
{

  public SeqTableEventAdapter()
  {
  }

  public SeqTableEventAdapter(boolean noneSel)
  {
    super(noneSel);
  }

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
  public final void columnSelectionChanged(ListSelectionEvent e)
  {
    super.valueChanged(e);
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
