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
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
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
  SeqColumnModel columnModel;
  ChordTableAction chordTableAction;

  SeqViewerController seqViewer;
  SoundController sound;
  SeqAnimController anim;

  /** Creates new form SeqTablePanel */
  public SeqTablePanel()
  {
    initComponents();

    ChordRegistry.mainRegistry();

    chordTableAction = new ChordTableAction();
    toolAddChord.addActionListener(chordTableAction);
    toolInsert.addActionListener(chordTableAction);
    toolRemove.addActionListener(chordTableAction);
    toolPlay.addActionListener(chordTableAction);
    toolResetAll.addActionListener(chordTableAction);

    this.seqTableScrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, controlPanel);
    this.seqTableScrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, new JPanel());
  }

  public void init(SeqColumnModel model, RenderBassBoard renderBoard)
  {
    columnModel = model;
    seqTable.setSelectionModel(columnModel.getRowSelModel());
    seqViewer = new SeqViewerController(seqTable, columnModel, seqTableScrollPane, renderBoard);
    columnModel.addColumnModelListener(new ColumnChangeListener());
    columnModel.getRowSelModel().addListSelectionListener(new ColumnChangeListener());

    sound = new SoundController(true);
    anim = new SeqAnimController(renderBoard, columnModel, sound, 500, 100);
  }

  void toggleLeftRight(boolean left)
  {
    this.setLayout(new BorderLayout());
    this.add(left ? BorderLayout.WEST : BorderLayout.EAST, sidebar);
    this.add(BorderLayout.CENTER, this.seqTableScrollPane);
    this.add(BorderLayout.SOUTH, this.statusText);
  }

  public void setSoundEnabled(boolean soundEnabled)
  {
    sound.setEnabled(soundEnabled);
    volumeSlider.setEnabled(soundEnabled);
    checkArpegg.setEnabled(soundEnabled);
  }


  private class ColumnChangeListener extends SeqTableEventAdapter
  {

    @Override
    public void columnCountChanged(TableColumnModelEvent e)
    {
      int colCount = columnModel.getColumnCount();

      boolean autoResize = (colCount <= 4);

      if (!autoResize) {
        seqTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      } else {
        seqTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      }

      toolRemove.setEnabled(colCount > 1);
    }

    @Override
    public void selectionChanged(int index)
    {
      if (toolPlay != null) {
        toolPlay.setVisible(columnModel.getColumnCount() > 1);
      }

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
    //sidebar.setVisible(allowed);
    toolAddChord.setVisible(allowed);
    toolInsert.setVisible(allowed);
    toolRemove.setVisible(allowed);
    //transposePanel1.setVisible(allowed);
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

  class ChordTableAction extends AbstractAction
  {

    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (e.getActionCommand().equals("AddChord")) {
        columnModel.addColumn(columnModel.getColumnCount());

      } else if (e.getActionCommand().equals("InsertChord")) {
        int index = columnModel.getSelectedColumn();
        if (index >= 0) {
          columnModel.addColumn(index);
        }

      } else if (e.getActionCommand().equals("RemoveChord")) {
        columnModel.removeSelectedColumn();
      } else if (e.getActionCommand().equals("ResetAll")) {
        columnModel.resetColumns(true);
      } else if (e.getActionCommand().equals("PlaySeq")) {
        if (anim.toggleRun()) {
          toolPlay.setText("Stop");
        } else {
          toolPlay.setText("Play");
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
    toggleChordPicker = new javax.swing.JButton();
    toolAddChord = new javax.swing.JButton();
    toolInsert = new javax.swing.JButton();
    toolRemove = new javax.swing.JButton();
    checkSound = new javax.swing.JCheckBox();
    volumeSlider = new javax.swing.JSlider();
    checkArpegg = new javax.swing.JCheckBox();
    toolResetAll = new javax.swing.JButton();
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

    toggleChordPicker.setFont(toggleChordPicker.getFont().deriveFont(toggleChordPicker.getFont().getStyle() | java.awt.Font.BOLD));
    toggleChordPicker.setText("Hide Editor >>");

    toolAddChord.setAction(chordTableAction);
    toolAddChord.setText("Add Chord");
    toolAddChord.setActionCommand("AddChord");
    toolAddChord.setFocusable(false);
    toolAddChord.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    toolAddChord.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

    toolInsert.setAction(chordTableAction);
    toolInsert.setText("Insert Chord");
    toolInsert.setActionCommand("InsertChord");
    toolInsert.setFocusable(false);
    toolInsert.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    toolInsert.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

    toolRemove.setAction(chordTableAction);
    toolRemove.setText("Remove Chord");
    toolRemove.setActionCommand("RemoveChord");
    toolRemove.setFocusable(false);
    toolRemove.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    toolRemove.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

    checkSound.setText("Sound Enabled");
    checkSound.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        checkSoundItemStateChanged(evt);
      }
    });

    volumeSlider.setMaximum(255);
    volumeSlider.setSnapToTicks(true);
    volumeSlider.setToolTipText("Sound Volume");
    volumeSlider.setEnabled(false);
    volumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        volumeSliderStateChanged(evt);
      }
    });

    checkArpegg.setText("Arpeggiate Chords");
    checkArpegg.setActionCommand("Arepggiate Chords");
    checkArpegg.setEnabled(false);
    checkArpegg.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkArpeggActionPerformed(evt);
      }
    });

    toolResetAll.setText("Clear All Chords");
    toolResetAll.setActionCommand("ResetAll");

    javax.swing.GroupLayout sidebarLayout = new javax.swing.GroupLayout(sidebar);
    sidebar.setLayout(sidebarLayout);
    sidebarLayout.setHorizontalGroup(
      sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(sidebarLayout.createSequentialGroup()
        .addGroup(sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(toggleChordPicker)
          .addGroup(sidebarLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(checkSound)
              .addGroup(sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(volumeSlider, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                .addComponent(checkArpegg, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
          .addGroup(sidebarLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(toolAddChord)
              .addComponent(toolInsert)
              .addComponent(toolRemove)
              .addComponent(toolResetAll))))
        .addContainerGap(24, Short.MAX_VALUE))
    );
    sidebarLayout.setVerticalGroup(
      sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(sidebarLayout.createSequentialGroup()
        .addComponent(toggleChordPicker)
        .addGap(24, 24, 24)
        .addComponent(checkSound)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(volumeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(checkArpegg)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(toolAddChord)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(toolInsert)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(toolRemove)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(toolResetAll)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    statusText.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
    statusText.setText("Status");
    statusText.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(sidebar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(seqTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE))
          .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(statusText, javax.swing.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(seqTableScrollPane, 0, 0, Short.MAX_VALUE)
          .addComponent(sidebar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(statusText, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void checkSoundItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_checkSoundItemStateChanged
  {//GEN-HEADEREND:event_checkSoundItemStateChanged
    setSoundEnabled(evt.getStateChange() == ItemEvent.SELECTED);
  }//GEN-LAST:event_checkSoundItemStateChanged

  private void volumeSliderStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_volumeSliderStateChanged
  {//GEN-HEADEREND:event_volumeSliderStateChanged
    sound.setVolume(volumeSlider.getValue());
  }//GEN-LAST:event_volumeSliderStateChanged

  private void checkArpeggActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkArpeggActionPerformed
  {//GEN-HEADEREND:event_checkArpeggActionPerformed
    sound.setArpeggiating(checkArpegg.isSelected());
  }//GEN-LAST:event_checkArpeggActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox checkArpegg;
  private javax.swing.JCheckBox checkSound;
  private javax.swing.JPanel controlPanel;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JTable seqTable;
  private javax.swing.JScrollPane seqTableScrollPane;
  private javax.swing.JPanel sidebar;
  private javax.swing.JLabel statusText;
  javax.swing.JButton toggleChordPicker;
  private javax.swing.JButton toolAddChord;
  private javax.swing.JButton toolInsert;
  private javax.swing.JButton toolPlay;
  private javax.swing.JButton toolRemove;
  private javax.swing.JButton toolResetAll;
  private javax.swing.JSlider volumeSlider;
  // End of variables declaration//GEN-END:variables
}
