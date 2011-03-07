/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TabPitchDetect.java
 *
 * Created on Nov 25, 2010, 11:49:39 AM
 */
package render;

import java.awt.BorderLayout;
import java.awt.Graphics;
import javax.swing.AbstractSpinnerModel;
import javax.swing.JPanel;
import music.core.Note;
import music.ParsedChordDef;
import music.input.PitchDetect;

/**
 *
 * @author Ilya
 */
public class TabPitchDetect extends ToolPanel implements PitchDetect.PitchUpdater
{

  PitchDetect pitchDetector;
  Note lastNote, newNote;

  /** Creates new form TabPitchDetect */
  public TabPitchDetect()
  {
    initComponents();

    pitchDetector = new PitchDetect();

    JPanel graphPanel = pitchDetector.getGraphPanel();
    graphPanel.setBackground(jPanel1.getBackground());
    this.jPanel1.add(BorderLayout.CENTER, graphPanel);

    this.sampleSize.setModel(new ExpSpinnerModel(pitchDetector.getSampleSize(), 2, 1 << 9, 1 << 13));
    this.samplingRate.setModel(new ExpSpinnerModel(pitchDetector.getSamplingRate(), 2, 44100 / 8, 44100 * 2));
  }

  @Override
  public synchronized void newNote(Note note, double freq)
  {
    if (note == null) {
      detectedText.setText("Current Note: Too Noisy");
      return;
    }

    detectedText.setText("Current Note: " + note.toString());

    if ((columnModel != null) && !note.equals(lastNote)) {
      newNote = note;
      repaint();
    }
  }

  @Override
  protected void syncUIToDataModel(SyncType sync)
  {
  }

  @Override
  public synchronized void paint(Graphics g)
  {
    super.paint(g);

    if ((newNote != null) && !newNote.equals(lastNote)) {
      columnModel.editSelectedColumn(new ParsedChordDef(newNote));
      lastNote = newNote;
    }
  }

  public void shown()
  {
    super.shown();
    detectedText.setText("Current Note: Too Noisy");
  }

  public void hidden()
  {
    super.hidden();
    if (pitchDetector.isRunning()) {
      togglePitchDetect();
    }
  }

  void togglePitchDetect()
  {
    boolean running = pitchDetector.isRunning();
    if (running) {
      toggleDetect.setText("Start Detecting");
      pitchDetector.stop();
    } else {
      toggleDetect.setText("Stop Detecting");
      pitchDetector.start(this);
    }
    this.sampleSize.setEnabled(running);
    this.samplingRate.setEnabled(running);
  }

  class ExpSpinnerModel extends AbstractSpinnerModel
  {

    int sMin;
    int sMax;
    Integer value;
    int factor;

    ExpSpinnerModel(int start, int fact, int min, int max)
    {
      sMin = min;
      sMax = max;
      value = Integer.valueOf(start);
      factor = fact;
    }

    @Override
    public Object getNextValue()
    {
      int newVal = value.intValue() * factor;
      if (newVal > sMax) {
        return null;
      }
      return Integer.valueOf(newVal);
    }

    @Override
    public Object getPreviousValue()
    {
      int newVal = value.intValue() / factor;
      if (newVal < sMin) {
        return null;
      }
      return Integer.valueOf(newVal);
    }

    @Override
    public Object getValue()
    {
      return value;
    }

    @Override
    public void setValue(Object value)
    {
      this.value = (Integer) value;
      this.fireStateChanged();
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

    toggleDetect = new javax.swing.JButton();
    detectedText = new javax.swing.JLabel();
    jPanel1 = new javax.swing.JPanel();
    samplingRate = new javax.swing.JSpinner();
    jLabel1 = new javax.swing.JLabel();
    sampleSize = new javax.swing.JSpinner();
    jLabel2 = new javax.swing.JLabel();

    toggleDetect.setText("Start Pitch Detect");
    toggleDetect.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        toggleDetectActionPerformed(evt);
      }
    });

    detectedText.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
    detectedText.setText("Not Detected");

    jPanel1.setBackground(new java.awt.Color(255, 255, 255));
    jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    jPanel1.setLayout(new java.awt.BorderLayout());

    samplingRate.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        samplingRateStateChanged(evt);
      }
    });

    jLabel1.setText("Sampling Rate:");

    sampleSize.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        sampleSizeStateChanged(evt);
      }
    });

    jLabel2.setText("Sample Size:");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(toggleDetect)
              .addComponent(detectedText, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel2)
              .addComponent(jLabel1))
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(samplingRate)
              .addComponent(sampleSize, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE))))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(toggleDetect)
            .addGap(12, 12, 12)
            .addComponent(detectedText))
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(jLabel1)
              .addComponent(samplingRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(sampleSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(jLabel2))))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
        .addContainerGap())
    );
  }// </editor-fold>//GEN-END:initComponents

  private void toggleDetectActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_toggleDetectActionPerformed
  {//GEN-HEADEREND:event_toggleDetectActionPerformed
    // TODO add your handling code here:
    this.togglePitchDetect();
  }//GEN-LAST:event_toggleDetectActionPerformed

  private void samplingRateStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_samplingRateStateChanged
  {//GEN-HEADEREND:event_samplingRateStateChanged
    pitchDetector.setSamplingRate(((Integer) samplingRate.getValue()).intValue());
  }//GEN-LAST:event_samplingRateStateChanged

  private void sampleSizeStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_sampleSizeStateChanged
  {//GEN-HEADEREND:event_sampleSizeStateChanged
    pitchDetector.setSamplingSize(((Integer) sampleSize.getValue()).intValue());
  }//GEN-LAST:event_sampleSizeStateChanged
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel detectedText;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JSpinner sampleSize;
  private javax.swing.JSpinner samplingRate;
  private javax.swing.JButton toggleDetect;
  // End of variables declaration//GEN-END:variables
}
