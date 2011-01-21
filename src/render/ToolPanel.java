/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import javax.swing.JPanel;

/**
 *
 * @author Ilya
 */
public abstract class ToolPanel extends JPanel
{

  protected SeqColumnModel columnModel;
  ToolSelChangeListener colChange;
  ToolSelChangeListener rowChange;

  class ToolSelChangeListener extends ListSelChangeListener
  {
    boolean isColumnChanging;

    ToolSelChangeListener(boolean columnChanging)
    {
      isColumnChanging = columnChanging;
    }

    @Override
    public void selectionChanged(int index)
    {
      if (isColumnChanging)
        ToolPanel.this.columnSelectionChanged(index);
      else
        ToolPanel.this.rowSelectionChanged(index);
    }
  }

  public void init(SeqColumnModel model)
  {
    toggleListeners(false);

    columnModel = model;

    this.colChange = (listenToCols() ? new ToolSelChangeListener(true) : null);
    this.rowChange = (listenToRows() ? new ToolSelChangeListener(false) : null);

    if (isVisible()) {
      toggleListeners(true);
    }
  }

  private void toggleListeners(boolean attach)
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
  public void setVisible(boolean visible)
  {
    toggleListeners(visible);

    if (visible && (columnModel != null)) {
      syncUIToDataModel();
    }

    super.setVisible(visible);
  }

  abstract protected void syncUIToDataModel();

  protected boolean listenToCols()
  {
    return true;
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
