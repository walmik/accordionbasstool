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
import javax.swing.Timer;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import music.ButtonCombo;
import music.ChordRegistry;
import music.FingerCombo;
import music.Note;

/**
 *
 * @author Ilya
 */
public class SeqTablePanel extends javax.swing.JPanel implements ListSelectionListener
{

  SeqColumnModel columnModel;
  ChordTableAction chordTableAction;
  Timer playTimer;
  JPanel cornerPanel = new JPanel();
  JButton toolPlay;
  SeqViewerController seqViewer;

  /** Creates new form SeqTablePanel */
  public SeqTablePanel()
  {
    initComponents();

    ChordRegistry.mainRegistry();

    seqViewer = new SeqViewerController(seqTable, seqTableScrollPane);
    columnModel = seqViewer.columnModel;
    columnModel.selComboModel.addListSelectionListener(this);

    transposePanel1.setSeqColModel(columnModel);

    toolPlay = new JButton("Play");
    toolPlay.setActionCommand("PlaySeq");

    chordTableAction = new ChordTableAction();
    toolAddChord.addActionListener(chordTableAction);
    toolInsert.addActionListener(chordTableAction);
    toolRemove.addActionListener(chordTableAction);
    toolPlay.addActionListener(chordTableAction);

    playTimer = new Timer(1000, chordTableAction);
    playTimer.setActionCommand("Timer");


    cornerPanel = new JPanel();
    cornerPanel.add(toolPlay);
    this.seqTableScrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, cornerPanel);
    this.seqTableScrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, new JPanel());
  }

  void toggleLeftRight(boolean left)
  {
    this.setLayout(new BorderLayout());
    this.add(left ? BorderLayout.WEST : BorderLayout.EAST, sidebar);
    this.add(BorderLayout.CENTER, this.seqTableScrollPane);
    this.add(BorderLayout.SOUTH, this.statusText);
  }

  void initChordPicker(TabChordPicker picker)
  {
    picker.init();
    picker.changeChordSet(true);
    picker.setSeqColModel(columnModel);
    //columnModel.addColumn(picker.getPickedChord(), 0);
  }

  void initTextParser(TabSeqEditor textParser)
  {
    textParser.setSeqColModel(columnModel, seqTable, null);
  }
  music.midi.Player player;
  boolean soundEnabled = false;

  public void setSoundEnabled(boolean sound)
  {
    soundEnabled = sound;
    if (player != null) {
      player.stopAll();
    }
  }

  private boolean playCombo(ButtonCombo combo)
  {
    if (!soundEnabled) {
      if (player != null) {
        player.stopAll();
      }
      return false;
    }

    if (combo == null) {
      return false;
    }

    if (player == null) {
      player = new music.midi.Player();
      player.init();
    }

    player.stopAll();
    return player.playChord(combo.getChordMaskValue());
  }

  private ButtonCombo getCurrCombo()
  {
    int row = seqTable.getSelectedRow();
    int col = columnModel.getSelectedColumn();

    Object obj = seqTable.getModel().getValueAt(row, col);
    if (obj instanceof FingerCombo) {
      FingerCombo fingerCombo = (FingerCombo)obj;
          return ((fingerCombo != null) ? (fingerCombo.getButtonCombo()) : null);
    } else if (obj instanceof ButtonCombo) {
      return (ButtonCombo)obj;
    } else {
      return null;
    }
  }

  @Override
  public void valueChanged(ListSelectionEvent e)
  {
    if (cornerPanel != null) {
      cornerPanel.setVisible(columnModel.getColumnCount() > 1);
    }

    ButtonCombo combo = getCurrCombo();

    //****
    playCombo(combo);
    //****
    
    String text = "<html>";

    if (combo != null) {
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

  void toggleSeqControls(boolean allowed)
  {
    //sidebar.setVisible(allowed);
    toolAddChord.setVisible(allowed);
    toolInsert.setVisible(allowed);
    toolRemove.setVisible(allowed);
    transposePanel1.setVisible(allowed);
    statusText.setVisible(allowed);

    if (!allowed && (columnModel.getColumnCount() > 1)) {
      columnModel.resetToSingleColumn();
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
      } else if (e.getActionCommand().equals("PlaySeq")) {
        if (!playTimer.isRunning()) {
          toolPlay.setText("Stop");
          playCombo(getCurrCombo());
          playTimer.restart();
        } else {
          toolPlay.setText("Play");
          playTimer.stop();
        }
      } else if (e.getActionCommand().equals("Timer")) {

        int index = columnModel.getSelectedColumn();

        index++;
        if (index >= columnModel.getColumnCount()) {
          index = 0;
        }

        columnModel.selComboModel.setSelectionInterval(index, index);
        seqTable.scrollRectToVisible(seqTable.getCellRect(seqTable.getSelectedRow(), index, true));
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

    seqTableScrollPane = new javax.swing.JScrollPane();
    seqTable = new javax.swing.JTable();
    sidebar = new javax.swing.JPanel();
    toggleChordPicker = new javax.swing.JButton();
    toolAddChord = new javax.swing.JButton();
    toolInsert = new javax.swing.JButton();
    toolRemove = new javax.swing.JButton();
    soundCheck = new javax.swing.JCheckBox();
    transposePanel1 = new render.TransposePanel();
    statusText = new javax.swing.JLabel();

    seqTable.setAutoCreateColumnsFromModel(false);
    seqTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {

      },
      new String [] {

      }
    ));
    seqTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    seqTable.setOpaque(false);
    seqTable.setRowHeight(24);
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

    soundCheck.setText("Sound On");
    soundCheck.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        soundCheckItemStateChanged(evt);
      }
    });

    javax.swing.GroupLayout sidebarLayout = new javax.swing.GroupLayout(sidebar);
    sidebar.setLayout(sidebarLayout);
    sidebarLayout.setHorizontalGroup(
      sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(sidebarLayout.createSequentialGroup()
        .addGroup(sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(toggleChordPicker)
          .addGroup(sidebarLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(toolAddChord))
          .addGroup(sidebarLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(toolInsert))
          .addGroup(sidebarLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(toolRemove))
          .addGroup(sidebarLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(soundCheck))
          .addComponent(transposePanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 125, Short.MAX_VALUE))
        .addContainerGap())
    );
    sidebarLayout.setVerticalGroup(
      sidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(sidebarLayout.createSequentialGroup()
        .addComponent(toggleChordPicker)
        .addGap(24, 24, 24)
        .addComponent(soundCheck)
        .addGap(18, 18, 18)
        .addComponent(toolAddChord)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(toolInsert)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(toolRemove)
        .addGap(18, 18, 18)
        .addComponent(transposePanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(72, Short.MAX_VALUE))
    );

    statusText.setFont(new java.awt.Font("Tahoma", 0, 16));
    statusText.setText("Status");
    statusText.setBorder(javax.swing.BorderFactory.createEtchedBorder());

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(layout.createSequentialGroup()
            .addGap(10, 10, 10)
            .addComponent(statusText, javax.swing.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE))
          .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
            .addComponent(sidebar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(seqTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGap(11, 11, 11)
            .addComponent(seqTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE))
          .addComponent(sidebar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(statusText, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void soundCheckItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_soundCheckItemStateChanged
  {//GEN-HEADEREND:event_soundCheckItemStateChanged
    setSoundEnabled(evt.getStateChange() == ItemEvent.SELECTED);
  }//GEN-LAST:event_soundCheckItemStateChanged

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTable seqTable;
  private javax.swing.JScrollPane seqTableScrollPane;
  private javax.swing.JPanel sidebar;
  private javax.swing.JCheckBox soundCheck;
  private javax.swing.JLabel statusText;
  javax.swing.JButton toggleChordPicker;
  private javax.swing.JButton toolAddChord;
  private javax.swing.JButton toolInsert;
  private javax.swing.JButton toolRemove;
  private render.TransposePanel transposePanel1;
  // End of variables declaration//GEN-END:variables
}
