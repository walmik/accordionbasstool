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
import music.Note;

/**
 *
 * @author Ilya
 */
public class SeqTablePanel extends javax.swing.JPanel
{

  private SeqColumnModel columnModel;
  private SoundController sound;
  private SeqAnimController anim;
  ChordSeqCmd.Action actionRemove;
  PlayStopAction actionPlay;

  /** Creates new form SeqTablePanel */
  public SeqTablePanel()
  {
    initComponents();

    ChordRegistry.mainRegistry();

    this.seqTableScrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, controlPanel);
    this.seqTableScrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, new JPanel());
  }

  public void init(SeqColumnModel model,
          RenderBassBoard renderBoard,
          SoundController sound)
  {
    columnModel = model;
    seqTable.setSelectionModel(columnModel.getRowSelModel());
    SeqViewerController seqViewer = new SeqViewerController(seqTable, columnModel, seqTableScrollPane, renderBoard);
    columnModel.addColumnModelListener(new ColumnChangeListener());
    columnModel.getRowSelModel().addListSelectionListener(new ColumnChangeListener());
    this.sound = sound;

    //this.soundCtrlPanel1.init(sound);

    chordButtons.setLayout(new BoxLayout(chordButtons, BoxLayout.Y_AXIS));
    ChordSeqCmd.populateButtons(columnModel, JButton.class, chordButtons, 8);

    actionRemove = ChordSeqCmd.RemoveChord.action;

    anim = new SeqAnimController(renderBoard, columnModel, sound, 500, 100);

    actionPlay = new PlayStopAction();
    toolPlay.setAction(actionPlay);
  }

  void toggleLeftRight(boolean left)
  {
    this.setLayout(new BorderLayout());
    this.add(left ? BorderLayout.WEST : BorderLayout.EAST, sidebar);
    this.add(BorderLayout.CENTER, this.seqTableScrollPane);
    this.add(BorderLayout.SOUTH, this.statusText);
  }


  private class ColumnChangeListener extends SeqTableEventAdapter
  {

    @Override
    public void columnCountChanged(TableColumnModelEvent e)
    {
      int colCount = columnModel.getColumnCount();

      if (toolPlay != null) {
        toolPlay.setVisible(colCount > 1);
      }
      actionPlay.setEnabled(colCount > 1);
      if (colCount <= 1) {
        anim.stop();
      }

      boolean autoResize = (colCount <= 4);

      if (!autoResize) {
        seqTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      } else {
        seqTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      }

      actionRemove.setEnabled(colCount > 1);
    }

    @Override
    public void selectionChanged(int index)
    {
      ButtonCombo combo = columnModel.getSelectedButtonCombo();

      String text = "<html>";

      if ((combo != null) && (combo.getLength() > 0)) {
        //****
        sound.play(combo, anim.isRunning());
        //****

        Note lowest = combo.getLowestNote();

        text += "Low Note: " + "<b>" + (lowest.isBassNote() ? "Bass " : "Chord ") + lowest.toString() + "</b>";
        text += " Buttons: " + "<b>" + combo.toButtonListingString(true) + "</b>";
        //text += " (" + combo.toSortedNoteString(true) + ") ";
        //text += "</b>";
      } else {
        text += "Not Possible on this board ";
      }
      text += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Complete Sequence: ";
      text += columnModel.toHtmlString(true);
      text += "</html>";

      statusText.setText(text);
    }
  }

  void toggleSeqControls(boolean allowed)
  {
    sidebar.setVisible(allowed);

    statusText.setVisible(allowed);

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
            button.setAction(type.action);
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
  };

  class PlayStopAction extends AbstractAction
  {
    @Override
    public Object getValue(String key)
    {
      if (key.equals(NAME)) {
        return anim.isRunning() ? "Stop" : "Play";
      }

      return super.getValue(key);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      anim.toggleRun();
      this.firePropertyChange(NAME, null, getValue(NAME));
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
    statusText = new javax.swing.JLabel();

    toolPlay.setText("Play");
    toolPlay.setActionCommand("PlaySeq");

    jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getSize()+4f));
    jLabel1.setText("<html>Sequence<br/>\nRank</html>");

    javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
    controlPanel.setLayout(controlPanelLayout);
    controlPanelLayout.setHorizontalGroup(
      controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(controlPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel1)
          .addComponent(toolPlay))
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
        .addGap(4, 4, 4))
    );
    sidebarLayout.setVerticalGroup(
      sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(sidebarLayout.createSequentialGroup()
        .addComponent(buttonHideEditor)
        .addGap(102, 102, 102)
        .addComponent(chordButtons, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(43, Short.MAX_VALUE))
    );

    statusText.setFont(new java.awt.Font("Tahoma", 0, 16));
    statusText.setText("Status");
    statusText.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(sidebar, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(seqTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE))
          .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(statusText, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(seqTableScrollPane, 0, 0, Short.MAX_VALUE)
          .addComponent(sidebar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(statusText, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
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
  private javax.swing.JLabel statusText;
  private javax.swing.JButton toolPlay;
  // End of variables declaration//GEN-END:variables
}
