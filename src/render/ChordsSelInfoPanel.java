/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ChordsSelInfoPanel.java
 *
 * Created on Jan 14, 2011, 12:33:07 AM
 */
package render;

import java.awt.Component;
import java.util.Vector;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import music.ParsedChordDef;

/**
 *
 * @author Ilya
 */
public class ChordsSelInfoPanel extends ToolPanel
{

  boolean displayUnknown = false;
  boolean listChanging = false;

  /** Creates new form ChordsSelInfoPanel */
  public ChordsSelInfoPanel()
  {
    initComponents();
    matchesListBox.setCellRenderer(new ChordListRender());
  }

  @Override
  public void init(SeqColumnModel model)
  {
    super.init(model);
    clickedLabel.setText(columnModel.getSelectedComboStateString());
  }

  @Override
  protected void syncUIToDataModel()
  {
    listChanging = true;

    Vector<ParsedChordDef> listItems;

    columnModel.matchingChordStore.setValid(false);

    if (displayUnknown) {
      listItems = columnModel.matchingChordStore.getAllMatchingSelChords(true);
    } else {
      listItems = columnModel.matchingChordStore.getKnownMatchingSelChords(true);
    }

    matchesListBox.setListData(listItems);

    matchesListBox.setSelectedValue(columnModel.getSelectedChordDef(), true);

    listChanging = false;

    clickedLabel.setText(columnModel.getSelectedComboStateString());
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

  public void setDisplayUnknown(boolean display)
  {
    displayUnknown = display;
    syncUIToDataModel();
  }

  public void setDisplayInversion(boolean display)
  {
    columnModel.matchingChordStore.setRemoveInversion(display);
    syncUIToDataModel();
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

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jScrollPane1 = new javax.swing.JScrollPane();
    matchesListBox = new javax.swing.JList();
    clickedLabel = new javax.swing.JLabel();
    checkShowUnknownChords = new javax.swing.JCheckBox();
    checkIgnoreInversion = new javax.swing.JCheckBox();

    jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Possible Chords:"));
    jScrollPane1.setToolTipText("<html>\nA list of other possible chords for the current button combination pressed,\nThe current chord, if listed, is selected.\n</html>");

    matchesListBox.setFont(new java.awt.Font("Monospaced", 1, 17));
    matchesListBox.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
        matchesListBoxValueChanged(evt);
      }
    });
    jScrollPane1.setViewportView(matchesListBox);

    clickedLabel.setFont(new java.awt.Font("Tahoma", 0, 18));
    clickedLabel.setText("Info");
    clickedLabel.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected On Board:"));

    checkShowUnknownChords.setText("List Unknown Chords");
    checkShowUnknownChords.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkShowUnknownChordsActionPerformed(evt);
      }
    });

    checkIgnoreInversion.setText("Don't Show Chord Inversion");
    checkIgnoreInversion.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkIgnoreInversionActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(checkShowUnknownChords)
          .addComponent(checkIgnoreInversion))
        .addGap(63, 63, 63))
      .addComponent(clickedLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(clickedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(checkShowUnknownChords)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(checkIgnoreInversion)
            .addGap(97, 97, 97))
          .addGroup(layout.createSequentialGroup()
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void matchesListBoxValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_matchesListBoxValueChanged
  {//GEN-HEADEREND:event_matchesListBoxValueChanged
    if (listChanging) {
      return;
    }

    ParsedChordDef chordDef = (ParsedChordDef) this.matchesListBox.getSelectedValue();
    if (chordDef != null) {
      columnModel.editSelectedColumn(chordDef, true);
    }
  }//GEN-LAST:event_matchesListBoxValueChanged

  private void checkShowUnknownChordsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkShowUnknownChordsActionPerformed
  {//GEN-HEADEREND:event_checkShowUnknownChordsActionPerformed
    this.setDisplayUnknown(this.checkShowUnknownChords.isSelected());
}//GEN-LAST:event_checkShowUnknownChordsActionPerformed

  private void checkIgnoreInversionActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkIgnoreInversionActionPerformed
  {//GEN-HEADEREND:event_checkIgnoreInversionActionPerformed
    // Reselect the same chord in the list, may however be with an inversion
    this.setDisplayInversion(this.checkIgnoreInversion.isSelected());
}//GEN-LAST:event_checkIgnoreInversionActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox checkIgnoreInversion;
  private javax.swing.JCheckBox checkShowUnknownChords;
  private javax.swing.JLabel clickedLabel;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JList matchesListBox;
  // End of variables declaration//GEN-END:variables
}