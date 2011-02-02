/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JPanel;

/**
 *
 * @author Ilya
 */
public abstract class ToolPanel extends JPanel implements ComponentListener
{

  protected SeqColumnModel columnModel;
  ListSelChangeListener colChange;
  ListSelChangeListener rowChange;

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
    this.addComponentListener(this);

    toggleListeners(false);

    columnModel = model;

    this.colChange = (listenToCols() ? newListener(true) : null);
    this.rowChange = (listenToRows() ? newListener(false) : null);

    if (isVisible()) {
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
  public void componentHidden(ComponentEvent e)
  {
  }

  @Override
  public void componentMoved(ComponentEvent e)
  {
  }

  @Override
  public void componentResized(ComponentEvent e)
  {
  }

  @Override
  public void componentShown(ComponentEvent e)
  {
    if (columnModel != null) {
      syncUIToDataModel();
    }
  }

  @Override
  public final void setVisible(boolean visible)
  {
    toggleListeners(visible);
    super.setVisible(visible);
  }

  abstract protected void syncUIToDataModel();

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
      this.syncUIToDataModel();
    }
  }

  protected void rowSelectionChanged(int index)
  {
    if (columnModel != null) {
      this.syncUIToDataModel();
    }
  }
}
