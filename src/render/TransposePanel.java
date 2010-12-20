/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TransposePanel.java
 *
 * Created on Dec 8, 2010, 3:46:43 PM
 */
package render;

import javax.swing.DefaultComboBoxModel;
import music.Interval;
import music.NamedInterval;

/**
 *
 * @author Ilya
 */
public class TransposePanel extends javax.swing.JPanel
{
  
  SeqColumnModel columnModel;


  /** Creates new form TransposePanel */
  public TransposePanel()
  {
    initComponents();

    ivalCombo.setModel(new DefaultComboBoxModel(NamedInterval.values()));
    ivalCombo.setSelectedItem(NamedInterval.P5);
  }
  
  void setSeqColModel(SeqColumnModel model)
  {
    columnModel = model;
  }

  Interval getCurrInterval()
  {
    NamedInterval entry = (NamedInterval)ivalCombo.getSelectedItem();
    return entry.interval;
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jButton1 = new javax.swing.JButton();
    ivalCombo = new javax.swing.JComboBox();
    downButton = new javax.swing.JButton();
    upButton = new javax.swing.JButton();
    jLabel1 = new javax.swing.JLabel();

    jButton1.setText("jButton1");

    ivalCombo.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    ivalCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

    downButton.setText("Down");
    downButton.setFocusable(false);
    downButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    downButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    downButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        downButtonActionPerformed(evt);
      }
    });

    upButton.setText("Up");
    upButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        upButtonActionPerformed(evt);
      }
    });

    jLabel1.setText("Transpose:");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(ivalCombo, javax.swing.GroupLayout.Alignment.CENTER, 0, 122, Short.MAX_VALUE)
          .addComponent(jLabel1)
          .addGroup(layout.createSequentialGroup()
            .addComponent(downButton)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
            .addComponent(upButton)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(jLabel1)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(ivalCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(downButton)
          .addComponent(upButton))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void downButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_downButtonActionPerformed
  {//GEN-HEADEREND:event_downButtonActionPerformed
    columnModel.transposeAllByInterval(getCurrInterval().scale(-1));
  }//GEN-LAST:event_downButtonActionPerformed

  private void upButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_upButtonActionPerformed
  {//GEN-HEADEREND:event_upButtonActionPerformed
    columnModel.transposeAllByInterval(getCurrInterval());
  }//GEN-LAST:event_upButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton downButton;
  private javax.swing.JComboBox ivalCombo;
  private javax.swing.JButton jButton1;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JButton upButton;
  // End of variables declaration//GEN-END:variables
}
