/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NotePicker.java
 *
 * Created on Nov 5, 2010, 11:44:03 AM
 */

package render;

import music.core.Interval;
import music.core.Note;

/**
 *
 * @author Ilya
 */
public class NotePicker extends javax.swing.JPanel {

    /** Creates new form NotePicker */
    public NotePicker() {
        initComponents();

        currNote = Note.fromString(noteCombo.getSelectedItem().toString());
    }

    private Note currNote;

    public void setNote(Note newNote)
    {
      currNote = newNote;
      noteCombo.setSelectedItem(currNote.toString());
    }

    public Note getNote()
    {
      return currNote;
    }

    @Override
    public void setEnabled(boolean enabled)
    {
      super.setEnabled(enabled);
      noteCombo.setEnabled(enabled);
      sharpBut.setEnabled(enabled);
      flatBut.setEnabled(enabled);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    noteCombo = new javax.swing.JComboBox();
    jToolBar1 = new javax.swing.JToolBar();
    flatBut = new javax.swing.JButton();
    toggleSOF = new javax.swing.JButton();
    sharpBut = new javax.swing.JButton();

    setOpaque(false);
    setLayout(new java.awt.GridBagLayout());

    noteCombo.setFont(new java.awt.Font("Tahoma", 0, 18));
    noteCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "C", "C#", "D", "Db", "D#", "E", "Eb", "F", "F#", "G", "Gb", "G#", "A", "Ab", "A#", "B", "Bb" }));
    noteCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        NoteSelected(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    add(noteCombo, gridBagConstraints);

    jToolBar1.setFloatable(false);
    jToolBar1.setRollover(true);
    jToolBar1.setBorderPainted(false);
    jToolBar1.setOpaque(false);

    flatBut.setFont(flatBut.getFont().deriveFont(flatBut.getFont().getStyle() | java.awt.Font.BOLD));
    flatBut.setText("b");
    flatBut.setBorderPainted(false);
    flatBut.setFocusable(false);
    flatBut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    flatBut.setMargin(new java.awt.Insets(2, 4, 2, 4));
    flatBut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    flatBut.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        flatButActionPerformed(evt);
      }
    });
    jToolBar1.add(flatBut);

    toggleSOF.setFont(toggleSOF.getFont().deriveFont(toggleSOF.getFont().getStyle() | java.awt.Font.BOLD));
    toggleSOF.setText("b-#");
    toggleSOF.setBorderPainted(false);
    toggleSOF.setFocusable(false);
    toggleSOF.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    toggleSOF.setMargin(new java.awt.Insets(2, 4, 2, 4));
    toggleSOF.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    toggleSOF.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        toggleSOFActionPerformed(evt);
      }
    });
    jToolBar1.add(toggleSOF);

    sharpBut.setFont(sharpBut.getFont().deriveFont(sharpBut.getFont().getStyle() | java.awt.Font.BOLD));
    sharpBut.setText("#");
    sharpBut.setBorderPainted(false);
    sharpBut.setFocusable(false);
    sharpBut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    sharpBut.setMargin(new java.awt.Insets(2, 4, 2, 4));
    sharpBut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    sharpBut.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        sharpButActionPerformed(evt);
      }
    });
    jToolBar1.add(sharpBut);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    add(jToolBar1, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

    private void NoteSelected(java.awt.event.ActionEvent evt)//GEN-FIRST:event_NoteSelected
    {//GEN-HEADEREND:event_NoteSelected
      currNote = Note.fromString(noteCombo.getSelectedItem().toString());
      this.firePropertyChange("Note", null, currNote);
    }//GEN-LAST:event_NoteSelected

    private void flatButActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_flatButActionPerformed
    {//GEN-HEADEREND:event_flatButActionPerformed
      noteCombo.setSelectedItem(currNote.sub(Interval.halfStep).toString());
}//GEN-LAST:event_flatButActionPerformed

    private void sharpButActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sharpButActionPerformed
    {//GEN-HEADEREND:event_sharpButActionPerformed
      noteCombo.setSelectedItem(currNote.add(Interval.halfStep).toString());
}//GEN-LAST:event_sharpButActionPerformed

    private void toggleSOFActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_toggleSOFActionPerformed
    {//GEN-HEADEREND:event_toggleSOFActionPerformed
      Note newNote = currNote.eharmonic();
      if (!newNote.toString().equals(currNote.toString())) {
        noteCombo.setSelectedItem(newNote.toString());
      }
    }//GEN-LAST:event_toggleSOFActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton flatBut;
  private javax.swing.JToolBar jToolBar1;
  private javax.swing.JComboBox noteCombo;
  private javax.swing.JButton sharpBut;
  private javax.swing.JButton toggleSOF;
  // End of variables declaration//GEN-END:variables

}
