/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TabButtonClicker.java
 *
 * Created on Dec 24, 2010, 8:19:14 PM
 */
package render;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import music.BassBoard;
import music.BoardSearcher;
import music.ButtonCombo;
import music.ChordRegistry;
import music.ParsedChordDef;

/**
 *
 * @author Ilya
 */
public class TabButtonClicker extends javax.swing.JPanel
{

  RenderBassBoard renderBoard;
  int clickIndex;
  //SelectedButtonCombo localButtonCombo;
  ButtonCombo clickCombo;
  Vector<BassBoard.Pos> activeButtons;
  BoardMouseListener listener;
  SeqColumnModel columnModel;
  ParsedChordDef activeParsedChord;
  SeqColumnModel.TableEventAdapter colModListener;

  /** Creates new form TabButtonClicker */
  public TabButtonClicker()
  {
    initComponents();

    //localButtonCombo = new SelectedButtonCombo();

    activeButtons = new Vector<BassBoard.Pos>();

    renderBoard = BassToolFrame.getRenderBoard();

    listener = new BoardMouseListener();

    colModListener = new SeqColumnModel.TableEventAdapter()
    {
      @Override
      public void columnSelectionChanged(ListSelectionEvent e)
      {
        //syncUI();
      }
    };

    this.possChordList.setCellRenderer(new ChordListRender());
  }

  public void setSeqColModel(SeqColumnModel model)
  {
    columnModel = model;
    if (columnModel != null) {
      columnModel.addColumnModelListener(colModListener);
    }
  }

  @Override
  public void setVisible(boolean b)
  {
    if (b) {
//      ButtonCombo combo = localButtonCombo.getSelectedButtonCombo();
//
//      if ((activeButtons.size() == 0)
//              || (combo == null)
//              || (combo.getBoard() != renderBoard.getBassBoard())) {
//
//        clearCombo();
//        updateCombo();
//      }

      //renderBoard.setSelectedButtonCombo(localButtonCombo);

      renderBoard.addMouseListener(listener);
      renderBoard.addMouseMotionListener(listener);
      renderBoard.addPropertyChangeListener(BassBoard.class.getSimpleName(), listener);

      if (columnModel != null) {
        columnModel.addColumnModelListener(colModListener);
      }

      syncUI();

    } else {
      if (columnModel != null) {
        renderBoard.setSelectedButtonCombo(columnModel.selComboModel);
        columnModel.removeColumnModelListener(colModListener);
      }

      renderBoard.removeMouseListener(listener);
      renderBoard.removeMouseMotionListener(listener);
      renderBoard.removePropertyChangeListener(BassBoard.class.getSimpleName(), listener);
    }

    super.setVisible(b);
  }

  private void clearCombo()
  {
    if (columnModel != null) {
      columnModel.clearPrefSeq();
    }
    
    activeButtons.clear();
    clickCombo = null;
    //activeButtons.add(renderBoard.getBassBoard().getCenter());
  }
  
  int isUpdating = 0;

  private void updateComboAndListUI(boolean updateCurrColumn)
  {
    if (activeButtons.size() == 0) {
      clickedLabel.setText("<html>None</html>");
      possChordList.setListData(new ParsedChordDef[0]);
      return;
    }
    
    BassBoard.Pos allPos[] = new BassBoard.Pos[activeButtons.size()];
    activeButtons.toArray(allPos);
    clickCombo = new ButtonCombo(allPos, renderBoard.getBassBoard());
    //localButtonCombo.setButtonCombo(clickCombo);

    String sortedNotesStr = clickCombo.toSortedNoteString(true);

    Vector<ParsedChordDef> matches =
            ChordRegistry.mainRegistry().
            findChordFromButtonCombo(clickCombo, this.checkIncludeInv.isSelected());

    String info = "<html>";
    info += "Buttons: ";
    info += "<b>" + clickCombo.toButtonListingString(true) + "</b><br/>";
    info += "Notes: ";
    //info += "<font size=\'-1\'>";
    info += "<b>" + sortedNotesStr + "</b>";
    //info += "</font>";
    info += "</html>";
    this.clickedLabel.setText(info);

    isTableDriven = true;

    if (checkShowUnknownChords.isSelected()) {
      possChordList.setListData(matches);
    } else {
      Vector<ParsedChordDef> filtered = new Vector<ParsedChordDef>();

      for (ParsedChordDef def : matches) {
        if (def.relChord != null && def.relChord.getOrigDef() != null) {
          filtered.add(def);
        }
      }

      possChordList.setListData(filtered);
    }

    if (updateCurrColumn) {
      this.activeParsedChord = matches.firstElement();
      columnModel.editSelectedColumn(activeParsedChord);
    }

    if (possChordList.getModel().getSize() > 0) {
      possChordList.setSelectedIndex(0);
    }

    possChordList.repaint();

    isTableDriven = false;
  }

  private void handleBoardChange(BassBoard oldBoard, BassBoard newBoard)
  {
//    this.syncUI();
  }

  private void syncActiveButtons()
  {
    if (columnModel == null) {
      return;
    }

    ButtonCombo selCombo = columnModel.selComboModel.getSelectedButtonCombo();

    if (selCombo == null) {
      return;
    }

    activeButtons.clear();

    for (BassBoard.Pos pos : selCombo.getAllPos()) {
      activeButtons.add(pos);
    }
  }
  
  boolean isTableDriven = false;
  boolean isClickDriven = false;

  private void syncUI()
  {
    if (isClickDriven || isTableDriven) {
      return;
    }

    if (columnModel == null) {
      return;
    }

    isTableDriven = true;

    //System.out.println(columnModel.selComboModel.getSelectedButtonCombo());

    syncActiveButtons();
    updateComboAndListUI(false);

    activeParsedChord = columnModel.getSelectedChordDef();
    
    possChordList.setSelectedValue(activeParsedChord, true);

    isTableDriven = false;
  }

  class ChordListRender extends DefaultListCellRenderer
  {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
      ParsedChordDef chordDef = (ParsedChordDef)value;

      String info = "<html>";
      info += chordDef.nameHtml;
      info += "</html>";

      return super.getListCellRendererComponent(list, info, index, isSelected, cellHasFocus);
    }
  }

  class BoardMouseListener extends MouseAdapter implements PropertyChangeListener
  {

    @Override
    public void mouseDragged(MouseEvent e)
    {
      if (clickIndex >= 0) {
        BassBoard.Pos clickPos = renderBoard.hitTest(e);
        if ((clickPos != null) && (clickIndex < activeButtons.size()) && !activeButtons.contains(clickPos)) {
          activeButtons.setElementAt(clickPos, clickIndex);
        }
      }

      updateComboAndListUI(true);
    }
    boolean isClickShiftMode = true;

    @Override
    public void mousePressed(MouseEvent e)
    {
      if (isClickShiftMode) {
        if (!e.isShiftDown()) {
          activeButtons.clear();
        } else {
          syncActiveButtons();
        }
      } else {
        syncActiveButtons();
      }

      BassBoard.Pos clickPos = renderBoard.hitTest(e);
      clickIndex = -1;

      if (clickPos != null) {
        clickIndex = activeButtons.indexOf(clickPos);

        if (clickIndex < 0) {
          if (activeButtons.size() < BoardSearcher.optMaxComboLength) {
            clickIndex = activeButtons.size();
            activeButtons.addElement(clickPos);
          }
        } else {
          activeButtons.remove(clickIndex);
          clickIndex = -1;
        }
      }

      updateComboAndListUI(true);
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
      super.mouseReleased(e);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
      if (evt.getSource() == renderBoard) {
        handleBoardChange((BassBoard) evt.getOldValue(), (BassBoard) evt.getNewValue());
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

    clickedLabel = new javax.swing.JLabel();
    buttonClear = new javax.swing.JButton();
    jScrollPane1 = new javax.swing.JScrollPane();
    possChordList = new javax.swing.JList();
    checkShowUnknownChords = new javax.swing.JCheckBox();
    buttonSelPref = new javax.swing.JButton();
    checkIncludeInv = new javax.swing.JCheckBox();

    clickedLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
    clickedLabel.setText("None");
    clickedLabel.setBorder(javax.swing.BorderFactory.createTitledBorder("Clicked Buttons:"));

    buttonClear.setText("Clear");
    buttonClear.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonClearActionPerformed(evt);
      }
    });

    jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Possible Chords:"));

    possChordList.setFont(possChordList.getFont().deriveFont(possChordList.getFont().getStyle() | java.awt.Font.BOLD, possChordList.getFont().getSize()+3));
    possChordList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
        possChordListValueChanged(evt);
      }
    });
    jScrollPane1.setViewportView(possChordList);

    checkShowUnknownChords.setText("Show Unnamed Chords");
    checkShowUnknownChords.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkShowUnknownChordsActionPerformed(evt);
      }
    });

    buttonSelPref.setText("Selected Picked Buttons");
    buttonSelPref.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonSelPrefActionPerformed(evt);
      }
    });

    checkIncludeInv.setText("Include Chord Inversion");
    checkIncludeInv.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkIncludeInvActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(clickedLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
            .addContainerGap())
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addComponent(buttonClear)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSelPref))
              .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(checkShowUnknownChords)
              .addComponent(checkIncludeInv))
            .addGap(36, 36, 36))))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(clickedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(buttonClear)
          .addComponent(buttonSelPref))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGap(27, 27, 27)
            .addComponent(checkShowUnknownChords)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(checkIncludeInv))
          .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void buttonClearActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonClearActionPerformed
  {//GEN-HEADEREND:event_buttonClearActionPerformed
    clearCombo();
    updateComboAndListUI(true);
  }//GEN-LAST:event_buttonClearActionPerformed

  private void checkShowUnknownChordsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkShowUnknownChordsActionPerformed
  {//GEN-HEADEREND:event_checkShowUnknownChordsActionPerformed
    updateComboAndListUI(true);
  }//GEN-LAST:event_checkShowUnknownChordsActionPerformed

  private void possChordListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_possChordListValueChanged
  {//GEN-HEADEREND:event_possChordListValueChanged
    if (isTableDriven) {
      return;
    }

    isClickDriven = true;

    ParsedChordDef chordDef = (ParsedChordDef) possChordList.getSelectedValue();

    if ((chordDef != null) && (chordDef != activeParsedChord)) {
      columnModel.editSelectedColumn(chordDef);
      activeParsedChord = chordDef;
    }

    isClickDriven = false;
  }//GEN-LAST:event_possChordListValueChanged

  private void buttonSelPrefActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonSelPrefActionPerformed
  {//GEN-HEADEREND:event_buttonSelPrefActionPerformed
    this.updateComboAndListUI(true);
    columnModel.selPrefSeq();
  }//GEN-LAST:event_buttonSelPrefActionPerformed

  private void checkIncludeInvActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkIncludeInvActionPerformed
  {//GEN-HEADEREND:event_checkIncludeInvActionPerformed
    this.updateComboAndListUI(true);
  }//GEN-LAST:event_checkIncludeInvActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton buttonClear;
  private javax.swing.JButton buttonSelPref;
  private javax.swing.JCheckBox checkIncludeInv;
  private javax.swing.JCheckBox checkShowUnknownChords;
  private javax.swing.JLabel clickedLabel;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JList possChordList;
  // End of variables declaration//GEN-END:variables
}
