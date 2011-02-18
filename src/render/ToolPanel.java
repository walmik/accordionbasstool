/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 *
 * @author Ilya
 */
public abstract class ToolPanel extends JPanel implements AncestorListener, PropertyChangeListener
{

  protected SeqColumnModel columnModel;
  ListSelChangeListener colChange;
  ListSelChangeListener rowChange;
  final static String RESET_TO_PREF_SIZE = "toolResetToPrefSize";

  public enum SyncType
  {
    SHOWN,
    COLUMN_CHANGE,
    ROW_CHANGE,
  }

  class ToolSelChangeListener extends ListSelChangeListener
  {

    boolean isColumn;

    ToolSelChangeListener(boolean columnChanging)
    {
      isColumn = columnChanging;
    }

    @Override
    public void selectionChanged(int index)
    {
      if (isColumn) {
        ToolPanel.this.columnSelectionChanged(index);
      } else {
        ToolPanel.this.rowSelectionChanged(index);
      }
    }
  }

  public void init(SeqColumnModel model)
  {
    this.addAncestorListener(this);

    toggleListeners(false);

    columnModel = model;

    this.colChange = (listenToCols() ? newListener(true) : null);
    this.rowChange = (listenToRows() ? newListener(false) : null);

    if (isDisplayable()) {
      toggleListeners(true);
    }
  }

  protected ListSelChangeListener newListener(boolean isColumn)
  {
    return new ToolSelChangeListener(isColumn);
  }

  protected void toggleListeners(boolean attach)
  {
    if (columnModel == null) {
      return;
    }

    if (attach) {
      if (colChange != null) {
//        colChange.clearLastIndex();
        columnModel.selComboModel.addListSelectionListener(colChange);
      }

      if (rowChange != null) {
//        rowChange.clearLastIndex();
        columnModel.getRowSelModel().addListSelectionListener(rowChange);
      }

    } else {
      if (colChange != null) {
        columnModel.selComboModel.removeListSelectionListener(colChange);
      }

      if (rowChange != null) {
        columnModel.getRowSelModel().removeListSelectionListener(rowChange);
      }
    }
  }

  @Override
  public void ancestorAdded(AncestorEvent event)
  {
    shown();
  }

  @Override
  public void ancestorMoved(AncestorEvent event)
  {
  }

  @Override
  public void ancestorRemoved(AncestorEvent event)
  {
    hidden();
  }

  protected void shown()
  {
    toggleListeners(true);
    if (columnModel != null) {
      syncUIToDataModel(SyncType.SHOWN);
    }

    //this.firePropertyChange(ToolPanel.RESET_TO_PREF_SIZE, null, Boolean.TRUE);
  }

  protected void hidden()
  {
    toggleListeners(false);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
  }

  @Override
  public final void setVisible(boolean visible)
  {
    super.setVisible(visible);
  }

  abstract protected void syncUIToDataModel(SyncType sync);

  protected boolean listenToCols()
  {
    return false;
  }

  protected boolean listenToRows()
  {
    return false;
  }

  protected void columnSelectionChanged(int index)
  {
    if (columnModel != null) {
      this.syncUIToDataModel(SyncType.COLUMN_CHANGE);
    }
  }

  protected void rowSelectionChanged(int index)
  {
    if (columnModel != null) {
      this.syncUIToDataModel(SyncType.ROW_CHANGE);
    }
  }
}
