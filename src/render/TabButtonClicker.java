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
import javax.swing.JTabbedPane;
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
  //SelectedButtonCombo localButtonCombo;
  ButtonCombo activeCombo;

  BoardMouseListener listener;
  SeqColumnModel columnModel;
  ParsedChordDef activeParsedChord;
  SeqColumnModel.TableEventAdapter colModListener;

  /** Creates new form TabButtonClicker */
  public TabButtonClicker()
  {
    initComponents();

    renderBoard = BassToolFrame.getRenderBoard();

    listener = new BoardMouseListener();
    renderBoard.addMouseListener(listener);
    renderBoard.addMouseMotionListener(listener);

    colModListener = new SeqColumnModel.TableEventAdapter()
    {
      @Override
      public void columnSelectionChanged(ListSelectionEvent e)
      {
        syncUI();
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
  public void setVisible(boolean visible)
  {
    if (visible) {
      //renderBoard.addMouseListener(listener);
      //renderBoard.addMouseMotionListener(listener);
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

      //renderBoard.removeMouseListener(listener);
      //renderBoard.removeMouseMotionListener(listener);
      renderBoard.removePropertyChangeListener(BassBoard.class.getSimpleName(), listener);
    }

    super.setVisible(visible);
  }

  private void clearCombo()
  {
    if (columnModel != null) {
      columnModel.clearPrefSeq();
    }

    //clickButtons.clear();
    //clickCombo = null;
  }
  int isUpdating = 0;

  private void updateFromClicked()
  {
    if (listener.clickButtons.size() == 0) {
      clickedLabel.setText("<html>None</html>");
      possChordList.setListData(new ParsedChordDef[0]);
      return;
    }

    BassBoard.Pos allPos[] = new BassBoard.Pos[listener.clickButtons.size()];
    listener.clickButtons.toArray(allPos);
    activeCombo = new ButtonCombo(allPos, renderBoard.getBassBoard());

    Vector<ParsedChordDef> matches = updateChordDefsList();

    if (matches.isEmpty()) {
      return;
    }

    for (ParsedChordDef def : matches) {
      def.setPrefCombo(activeCombo);
    }

    isTableDriven = true;

    this.activeParsedChord = matches.firstElement();
    columnModel.editSelectedColumn(activeParsedChord);

    if (possChordList.getModel().getSize() > 0) {
      possChordList.setSelectedIndex(0);
    }

    isTableDriven = false;
  }

  private Vector<ParsedChordDef> updateChordDefsList()
  {
    if (activeCombo == null) {
      return new Vector<ParsedChordDef>();
    }

    String sortedNotesStr = activeCombo.toSortedNoteString(true);

    Vector<ParsedChordDef> matches =
            ChordRegistry.mainRegistry().
            findChordFromNotes(ButtonCombo.sortedNotes, activeCombo.getChordMask(), this.checkIncludeInv.isSelected(), true);

    String info = "<html>";
    info += "Buttons: ";
    info += "<b>" + activeCombo.toButtonListingString(true) + "</b><br/>";
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
        if (def != null && def.relChord.getOrigDef() != null) {
          filtered.add(def);
        }
      }

      possChordList.setListData(filtered);
    }

    //possChordList.repaint();

    isTableDriven = false;

    return matches;
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

    listener.clickButtons.clear();

    for (BassBoard.Pos pos : selCombo.getAllPos()) {
      listener.clickButtons.add(pos);
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

    //System.out.println(columnModel.selComboModel.getSelectedButtonCombo());

    syncActiveButtons();
    activeCombo = columnModel.getSelectedButtonCombo();

    if (activeCombo != null) {
      updateChordDefsList();
    }

    activeParsedChord = columnModel.getSelectedChordDef();

    isTableDriven = true;
    possChordList.setSelectedValue(activeParsedChord, true);
    isTableDriven = false;
  }

  class ChordListRender extends DefaultListCellRenderer
  {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
      ParsedChordDef chordDef = (ParsedChordDef) value;

      String info = "<html>";
      info += chordDef.nameHtml;
      info += "</html>";

      return super.getListCellRendererComponent(list, info, index, isSelected, cellHasFocus);
    }
  }

  class BoardMouseListener extends MouseAdapter implements PropertyChangeListener
  {
    BassBoard.Pos lastClickPos;
    boolean isClickShiftMode = true;
    boolean isAltClick = false;
    int clickIndex = -1;
    Vector<BassBoard.Pos> clickButtons = new Vector<BassBoard.Pos>();


    @Override
    public void mouseDragged(MouseEvent e)
    {
      if (isAltClick) {
        renderBoard.setClickPos(e);
        return;
      }

      if (clickIndex >= 0) {
        BassBoard.Pos clickPos = renderBoard.hitTest(e);

        if (BassBoard.posEquals(lastClickPos, clickPos)) {
          return;
        }

        lastClickPos = clickPos;

        if ((clickPos != null) && (clickIndex < clickButtons.size()) && !clickButtons.contains(clickPos)) {
          clickButtons.setElementAt(clickPos, clickIndex);
        }
      }

      updateFromClicked();
    }

    @Override
    public void mousePressed(MouseEvent e)
    {     
      if (e.isAltDown() || e.isAltGraphDown()) {
        isAltClick = true;
        renderBoard.setClickPos(e);
        return;
      }

      if (isClickShiftMode) {
        if (!e.isShiftDown()) {
          clickButtons.clear();
        } else {
          syncActiveButtons();
        }
      } else {
        syncActiveButtons();
      }

      BassBoard.Pos clickPos = renderBoard.hitTest(e);
      clickIndex = -1;

      if (clickPos != null) {
        clickIndex = clickButtons.indexOf(clickPos);

        if (clickIndex < 0) {
          if (clickButtons.size() < BoardSearcher.optMaxComboLength) {
            clickIndex = clickButtons.size();
            clickButtons.addElement(clickPos);
          }
        } else {
          clickButtons.remove(clickIndex);
          clickIndex = -1;
        }
      }
      lastClickPos = clickPos;

      updateFromClicked();
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
      if (isAltClick) {
        renderBoard.clearClickPos();
        isAltClick = false;
      }
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
    checkHiliteRedunds = new javax.swing.JCheckBox();

    clickedLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
    clickedLabel.setText("None");
    clickedLabel.setBorder(javax.swing.BorderFactory.createTitledBorder("Currenly Select on Board:"));

    buttonClear.setText("Clear Clicked");
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

    checkShowUnknownChords.setText("Show Unnamed Inversions");
    checkShowUnknownChords.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkShowUnknownChordsActionPerformed(evt);
      }
    });

    buttonSelPref.setText("Select Clicked");
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

    checkHiliteRedunds.setText("Hilite Redundant Buttons");
    checkHiliteRedunds.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkHiliteRedundsActionPerformed(evt);
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
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addComponent(buttonClear)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSelPref))
              .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(checkShowUnknownChords)
                  .addComponent(checkIncludeInv))
                .addGap(36, 36, 36))
              .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(checkHiliteRedunds, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE))))
          .addComponent(clickedLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(clickedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(buttonClear)
          .addComponent(buttonSelPref)
          .addComponent(checkHiliteRedunds))
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGap(38, 38, 38)
            .addComponent(checkShowUnknownChords)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(checkIncludeInv))
          .addGroup(layout.createSequentialGroup()
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)))
        .addContainerGap())
    );
  }// </editor-fold>//GEN-END:initComponents

  private void checkShowUnknownChordsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkShowUnknownChordsActionPerformed
  {//GEN-HEADEREND:event_checkShowUnknownChordsActionPerformed
    updateChordDefsList();

    isTableDriven = true;

    possChordList.setSelectedValue(activeParsedChord, true);

    isTableDriven = false;
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
    columnModel.selPrefSeq();
  }//GEN-LAST:event_buttonSelPrefActionPerformed

  private void checkIncludeInvActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkIncludeInvActionPerformed
  {//GEN-HEADEREND:event_checkIncludeInvActionPerformed
    // Reselect the same chord in the list, may however be with an inversion
    int index = possChordList.getSelectedIndex();
    this.updateChordDefsList();
    possChordList.setSelectedIndex(index);
  }//GEN-LAST:event_checkIncludeInvActionPerformed

  private void checkHiliteRedundsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkHiliteRedundsActionPerformed
  {//GEN-HEADEREND:event_checkHiliteRedundsActionPerformed
    if (columnModel != null) {
      columnModel.selComboModel.showRedunds = this.checkHiliteRedunds.isSelected();
      renderBoard.repaint();
    }
  }//GEN-LAST:event_checkHiliteRedundsActionPerformed

  private void buttonClearActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonClearActionPerformed
  {//GEN-HEADEREND:event_buttonClearActionPerformed
    clearCombo();
  }//GEN-LAST:event_buttonClearActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton buttonClear;
  private javax.swing.JButton buttonSelPref;
  private javax.swing.JCheckBox checkHiliteRedunds;
  private javax.swing.JCheckBox checkIncludeInv;
  private javax.swing.JCheckBox checkShowUnknownChords;
  private javax.swing.JLabel clickedLabel;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JList possChordList;
  // End of variables declaration//GEN-END:variables
}
