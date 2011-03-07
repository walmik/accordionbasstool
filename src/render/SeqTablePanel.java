/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SeqTablePanel.java
 *
 * Created on Oct 23, 2010, 4:14:34 PM
 */
package render;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TableColumnModelEvent;
import music.ButtonCombo;
import music.ChordRegistry;
import music.core.Note;

/**
 *
 * @authonr Ilya
 */
public class SeqTablePanel extends ToolPanel
{

  private SoundController sound;
  private SeqAnimController anim;
  ChordSeqCmd.Action actionRemove;
  ColumnCountListener colListener;
  boolean playOnSelect = false;
  SeqViewerController seqViewer;

  /** Creates new form SeqTablePanel */
  public SeqTablePanel()
  {
    initComponents();

    ChordRegistry.mainRegistry();

    this.seqTableScrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, controlPanel);
    this.seqTableScrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, new JPanel());

    colListener = new ColumnCountListener();
  }

  public void init(SeqColumnModel model,
          RenderBassBoard renderBoard,
          SeqAnimController anim,
          SoundController sound)
  {
    super.init(model);
    seqTable.setSelectionModel(columnModel.getRowSelModel());

    seqViewer = new SeqViewerController(seqTable, columnModel, seqTableScrollPane, renderBoard);

    this.sound = sound;
    this.anim = anim;

    //this.soundCtrlPanel1.init(sound);

    chordButtons.setLayout(new BoxLayout(chordButtons, BoxLayout.Y_AXIS));
    ChordSeqCmd.populateButtons(columnModel, JButton.class, chordButtons, 8);

    actionRemove = ChordSeqCmd.RemoveChord.action;

    toolPlay.setAction(anim.getPlayStopAction());

    seqTable.addMouseListener(new MouseAdapter()
    {

      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() >= 2) {
          ButtonCombo combo = columnModel.getSelectedButtonCombo();
          SeqTablePanel.this.sound.play(combo, false);
        }
      }
    });

  }

  void toggleLeftRight(boolean left)
  {
    this.setLayout(new BorderLayout());
    this.add(left ? BorderLayout.WEST : BorderLayout.EAST, sidebar);
    this.add(BorderLayout.CENTER, this.seqTableScrollPane);
    this.add(BorderLayout.SOUTH, this.statusText);
  }

  private class ColumnCountListener extends SeqTableEventAdapter
  {

    @Override
    public void columnCountChanged(TableColumnModelEvent e)
    {
      updateColumnChange();
    }
  }

  private void updateColumnChange()
  {
    if (columnModel == null) {
      return;
    }

    int colCount = columnModel.getColumnCount();

    if (toolPlay != null) {
      toolPlay.setVisible(colCount > 1);
    }
    anim.getPlayStopAction().setEnabled(colCount > 1);

//    if (colCount <= 1) {
//      doStop();
//    }

    boolean autoResize = (colCount <= 4);

    if (!autoResize) {
      seqTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    } else {
      seqTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    actionRemove.setEnabled(colCount > 1);

//    seqTable.getTableHeader().resizeAndRepaint();
  }

  @Override
  public void shown()
  {
    super.shown();
    updateColumnChange();
  }

  @Override
  public void hidden()
  {
    super.hidden();
    if (anim != null) {
      anim.stop();
    }
  }

  @Override
  public void toggleListeners(boolean attach)
  {
    if (columnModel == null) {
      return;
    }

    super.toggleListeners(attach);

    if (attach) {
      columnModel.addColumnModelListener(colListener);
    } else {
      columnModel.removeColumnModelListener(colListener);
    }
  }

  @Override
  protected boolean listenToCols()
  {
    return true;
  }

  @Override
  protected boolean listenToRows()
  {
    return true;
  }

  @Override
  public void syncUIToDataModel(SyncType sync)
  {
    ButtonCombo combo = columnModel.getSelectedButtonCombo();

    String text = "<html>";
    String space = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    if ((combo != null) && (combo.getLength() > 0)) {

      if (!anim.isRunning() && (sync != SyncType.SHOWN) && playOnSelect) {
        sound.play(combo, false);
      }

      Note lowest = combo.getLowestNote();

      text += "Low Note: " + "<b>" + (lowest.isBassNote() ? "Bass " : "Chord ") + lowest.toString() + "</b>";
      text += (sidebar.isVisible() ? space : "<br/>");
      text += "Buttons: " + "<b>" + combo.toButtonListingString(true) + "</b>";
      //text += " (" + combo.toSortedNoteString(true) + ") ";
      //text += "</b>";
    } else {
      text += "Not Possible on this board ";
    }

    if (sidebar.isVisible()) {
      text += "<br/>" + "Complete Sequence: ";
      text += columnModel.toHtmlString(true);
    }

    text += "</html>";

    statusText.setText(text);
  }

  void toggleSeqControls(boolean allowed)
  {
    sidebar.setVisible(allowed);

    //statusText.setVisible(allowed);

    if (!allowed && (columnModel.getColumnCount() > 1)) {
      columnModel.resetColumns(true);
    }

    if (allowed) {
      seqTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    } else {
      seqTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }
  }

  enum ChordSeqCmd
  {

    AddChord("Add Chord"),
    InsertChord("Insert Chord"),
    RemoveChord("Remove Chord"),
    Sep1(""),
    ResetAll("Clear All Chords");

    ChordSeqCmd(String text)
    {
      action = new Action(text);
    }
    final Action action;

    class Action extends AbstractAction
    {

      SeqColumnModel columnModel;
      String name;

      Action(String text)
      {
        name = text;
        this.putValue(NAME, text);
      }

      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (columnModel == null) {
          return;
        }

        switch (ChordSeqCmd.this) {
          case AddChord:
            columnModel.addColumn(columnModel.getColumnCount());
            break;

          case InsertChord:
            int index = columnModel.getSelectedColumn();
            if (index >= 0) {
              columnModel.addColumn(index);
            }
            break;

          case RemoveChord:
            columnModel.removeSelectedColumn();
            break;

          case ResetAll:
            columnModel.resetColumns(true);
            break;
        }
      }
    }

    public static <C extends AbstractButton> void populateButtons(
            SeqColumnModel model,
            Class<C> uiclass,
            JComponent panel,
            int space)
    {
      for (ChordSeqCmd type : values()) {
        Component comp;

        if (type.action.name.isEmpty()) {
          comp = new JSeparator();
        } else {
          try {
            AbstractButton button = uiclass.newInstance();
            //button.putClientProperty("JComponent.sizeVariant", "mini");
            button.setAction(type.action);
            //button.updateUI();
            type.action.columnModel = model;
            comp = button;
          } catch (Exception inst) {
            comp = new JSeparator();
          }
        }

        panel.add(comp);
        //comps[type.ordinal()] = comp;

        if (space > 0) {
          panel.add(Box.createVerticalStrut(space));
        }
      }
    }
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    controlPanel = new javax.swing.JPanel();
    toolPlay = new javax.swing.JButton();
    jLabel1 = new javax.swing.JLabel();
    seqTableScrollPane = new javax.swing.JScrollPane();
    seqTable = new javax.swing.JTable();
    sidebar = new javax.swing.JPanel();
    buttonHideEditor = new javax.swing.JButton();
    chordButtons = new javax.swing.JPanel();
    statusText = new render.TransparentTextPane();

    controlPanel.setMinimumSize(new java.awt.Dimension(90, 5));

    toolPlay.setText("Play");
    toolPlay.setActionCommand("PlaySeq");

    jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getSize()+4f));
    jLabel1.setText("<html>Sequence<br/>\nRank</html>");

    javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
    controlPanel.setLayout(controlPanelLayout);
    controlPanelLayout.setHorizontalGroup(
      controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
          .addComponent(toolPlay, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE))
        .addContainerGap())
    );
    controlPanelLayout.setVerticalGroup(
      controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlPanelLayout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(toolPlay)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );

    seqTable.setAutoCreateColumnsFromModel(false);
    seqTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {

      }
    ));
    seqTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    seqTable.setOpaque(false);
    seqTable.setRowHeight(64);
    seqTableScrollPane.setViewportView(seqTable);

    buttonHideEditor.setFont(buttonHideEditor.getFont().deriveFont(buttonHideEditor.getFont().getStyle() | java.awt.Font.BOLD));
    buttonHideEditor.setText("Hide Editor >>");

    chordButtons.setLayout(new java.awt.GridLayout(5, 1, 0, 8));

    javax.swing.GroupLayout sidebarLayout = new javax.swing.GroupLayout(sidebar);
    sidebar.setLayout(sidebarLayout);
    sidebarLayout.setHorizontalGroup(
      sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(sidebarLayout.createSequentialGroup()
        .addGroup(sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(buttonHideEditor)
          .addComponent(chordButtons, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE))
        .addGap(10, 10, 10))
    );
    sidebarLayout.setVerticalGroup(
      sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(sidebarLayout.createSequentialGroup()
        .addComponent(buttonHideEditor)
        .addGap(102, 102, 102)
        .addComponent(chordButtons, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    statusText.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addComponent(sidebar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(seqTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
        .addContainerGap())
      .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
          .addContainerGap()
          .addComponent(statusText, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
          .addContainerGap()))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(sidebar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(seqTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE))
        .addGap(69, 69, 69))
      .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
          .addContainerGap(298, Short.MAX_VALUE)
          .addComponent(statusText, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)))
    );
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  javax.swing.JButton buttonHideEditor;
  private javax.swing.JPanel chordButtons;
  private javax.swing.JPanel controlPanel;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JTable seqTable;
  private javax.swing.JScrollPane seqTableScrollPane;
  private javax.swing.JPanel sidebar;
  private render.TransparentTextPane statusText;
  private javax.swing.JButton toolPlay;
  // End of variables declaration//GEN-END:variables
}
