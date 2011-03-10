/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PlayerApplet.java
 *
 * Created on Mar 9, 2011, 4:41:14 PM
 */
package music.midi;

import java.awt.Component;
import javax.sound.midi.Instrument;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;
import music.core.Chord;
import music.core.Note;
import music.core.StringParser;

/**
 *
 * @author Ilya
 */
public class PlayerApplet extends javax.swing.JApplet implements Runnable
{

  Player player;
  boolean muted = false;

  class InstruComboRenderer extends DefaultListCellRenderer
  {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
      Instrument instru = (Instrument) value;
      String name = ((instru != null) ? instru.getName() : "");
      return super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
    }
  }

  /** Initializes the applet PlayerApplet */
  @Override
  public void init()
  {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

      java.awt.EventQueue.invokeAndWait(this);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void run()
  {
    initComponents();

    player = new Player();
    player.init();

    player.setVolume(sliderVolume.getValue());
    sliderVolume.setMaximum(player.getMaxVolume());

    comboInstrument.setModel(new DefaultComboBoxModel(player.getInstruments()));
    comboInstrument.setSelectedItem(player.getInstrument(false));

    InstruComboRenderer render = new InstruComboRenderer();
    this.comboInstrument.setRenderer(render);

    doSetLowC();
  }

//  public String[] parseNotes(String string)
//  {
//    Note[] notes = new StringParser(string).parseNoteListArray();
//    String[] noteStrs = new String[notes.length];
//    return noteStrs;
//  }
  public void setInstrument(String name)
  {
    player.setInstrument(name);
    this.comboInstrument.setSelectedItem(player.getInstrument(true));
  }

  public void playNote(String noteStr, int duration, int stopAll)
  {
    try {
      Note note = Note.fromString(noteStr.trim());
      if (note == null) {
        System.out.println("Null Note: " + noteStr);
      }
      if (stopAll != 0) {
        player.stopAll();
      }
      if (!muted && (note != null)) {
        player.playChord(new Chord(note, false).getChordMask().getValue(), duration, false);
      }
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  private void doSetLowC()
  {
    try {
      int value = Integer.parseInt(this.textLowC.getText());
      player.stopAll();
      player.setLowCValue(value);
    } catch (NumberFormatException c) {
      return;
    }
  }

  /** This method is called from within the init() method to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    sliderVolume = new javax.swing.JSlider();
    comboInstrument = new javax.swing.JComboBox();
    jLabel1 = new javax.swing.JLabel();
    checkMute = new javax.swing.JCheckBox();
    jLabel2 = new javax.swing.JLabel();
    textLowC = new javax.swing.JTextField();
    jLabel3 = new javax.swing.JLabel();

    sliderVolume.setValue(64);
    sliderVolume.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        sliderVolumeStateChanged(evt);
      }
    });

    comboInstrument.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    comboInstrument.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        comboInstrumentActionPerformed(evt);
      }
    });

    jLabel1.setText("Instrument:");

    checkMute.setText("Mute");
    checkMute.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkMuteActionPerformed(evt);
      }
    });

    jLabel2.setText("Volume:");

    textLowC.setText("60");
    textLowC.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        textLowCActionPerformed(evt);
      }
    });

    jLabel3.setText("MIDI C Value:");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel1)
              .addComponent(jLabel2))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(comboInstrument, 0, 148, Short.MAX_VALUE)
              .addComponent(sliderVolume, 0, 0, Short.MAX_VALUE)))
          .addGroup(layout.createSequentialGroup()
            .addComponent(checkMute)
            .addGap(18, 18, 18)
            .addComponent(jLabel3)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(textLowC, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(comboInstrument, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel1))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel2)
          .addComponent(sliderVolume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(checkMute)
          .addComponent(textLowC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel3))
        .addContainerGap())
    );
  }// </editor-fold>//GEN-END:initComponents

  private void checkMuteActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkMuteActionPerformed
  {//GEN-HEADEREND:event_checkMuteActionPerformed
    muted = checkMute.isSelected();
    if (muted && (player != null)) {
      player.stopAll();
    }
  }//GEN-LAST:event_checkMuteActionPerformed

  private void sliderVolumeStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_sliderVolumeStateChanged
  {//GEN-HEADEREND:event_sliderVolumeStateChanged
    if (player != null) {
      player.setVolume(sliderVolume.getValue());
    }
  }//GEN-LAST:event_sliderVolumeStateChanged

  private void comboInstrumentActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_comboInstrumentActionPerformed
  {//GEN-HEADEREND:event_comboInstrumentActionPerformed
    player.setInstrument(false, (Instrument) comboInstrument.getSelectedItem());
    player.setInstrument(true, (Instrument) comboInstrument.getSelectedItem());
  }//GEN-LAST:event_comboInstrumentActionPerformed

  private void textLowCActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_textLowCActionPerformed
  {//GEN-HEADEREND:event_textLowCActionPerformed
    doSetLowC();
  }//GEN-LAST:event_textLowCActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox checkMute;
  private javax.swing.JComboBox comboInstrument;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JSlider sliderVolume;
  private javax.swing.JTextField textLowC;
  // End of variables declaration//GEN-END:variables
}
