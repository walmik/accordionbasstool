/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TabOptions.java
 *
 * Created on Nov 5, 2010, 11:23:35 AM
 */
package render;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.sound.midi.Instrument;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import music.BoardSearcher;
import music.ButtonCombo;
import music.midi.Player;

/**
 *
 * @author Ilya
 */
public class TabOptions extends ToolPanel
{

  public final static String TOGGLE_BOARDPOS_PROPERTY = "toggleBoardPos";
  public final static String TOGGLE_ORIENT_PROPERTY = "toggleOrient";
  public final static String TOGGLE_EDITOR_PROPERTY = "toggleEditor";

  /** Creates new form TabOptions */
  public TabOptions()
  {
    initComponents();

    initLNFCombo();
    syncUIToDataModel();
  }

  @Override
  public void init(SeqColumnModel mod)
  {
    super.init(mod);
    this.checkFingerSearch.setSelected(columnModel.optFingerSearch);
  }

  Player player;

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

  class ResetToAccordionAction extends AbstractAction
  {
    ResetToAccordionAction()
    {
      this.putValue(NAME, "Reset To Accordion");
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
      comboBassInstru.setSelectedItem(player.findInstrument("Accordion"));
      comboChordInstru.setSelectedItem(player.findInstrument("Tango Accordion"));
    }
  }

  ResetToAccordionAction getResetInstrumentsAction()
  {
    return new ResetToAccordionAction();
  }

  public void initSound(Player player)
  {
    this.player = player;

    this.comboBassInstru.setModel(new DefaultComboBoxModel(player.getInstruments()));
    this.comboChordInstru.setModel(new DefaultComboBoxModel(player.getInstruments()));

    this.comboBassInstru.setSelectedItem(player.getInstrument(false));
    this.comboChordInstru.setSelectedItem(player.getInstrument(true));

    InstruComboRenderer render = new InstruComboRenderer();
    this.comboBassInstru.setRenderer(render);
    this.comboChordInstru.setRenderer(render);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    super.addPropertyChangeListener(TOGGLE_BOARDPOS_PROPERTY, listener);
    super.addPropertyChangeListener(TOGGLE_ORIENT_PROPERTY, listener);
    super.addPropertyChangeListener(TOGGLE_EDITOR_PROPERTY, listener);
  }

  @Override
  protected void syncUIToDataModel()
  {
    this.maxComboThreshSpin.setValue(new Integer(ButtonCombo.optMaxDistThreshold));
    this.maxComboLenSpin.setValue(new Integer(BoardSearcher.optMaxComboLength));
    if (columnModel != null) {
      this.checkFingerSearch.setSelected(columnModel.optFingerSearch);
    }

    if (player != null) {
      this.comboBassInstru.setSelectedItem(player.getInstrument(false));
      this.comboChordInstru.setSelectedItem(player.getInstrument(true));
    }
  }

  private void recompute()
  {
    if (columnModel != null) {
      columnModel.recomputeSeqs();
    }
  }

  private void initLNFCombo()
  {
    lnfCombo.setModel(new DefaultComboBoxModel(UIManager.getInstalledLookAndFeels()));
    lnfCombo.setRenderer(new LnFComboRenderer());

    String currLook = UIManager.getLookAndFeel().getName();
    UIManager.LookAndFeelInfo lnfs[] = UIManager.getInstalledLookAndFeels();
    for (int i = 0; i < lnfs.length; i++) {
      if (lnfs[i].getName().equals(currLook)) {
        lnfCombo.setSelectedIndex(i);
        break;
      }
    }
  }

  void changeLNF()
  {
    try {
      UIManager.LookAndFeelInfo info =
              (UIManager.LookAndFeelInfo) lnfCombo.getSelectedItem();

      UIManager.setLookAndFeel(info.getClassName());

      //util.Main._rootFrame.repaint();
      Container topLevel = this.getTopLevelAncestor();
      if (topLevel != null) {
        SwingUtilities.updateComponentTreeUI(topLevel);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  class LnFComboRenderer extends DefaultListCellRenderer
  {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
      UIManager.LookAndFeelInfo info = (UIManager.LookAndFeelInfo) value;

      return super.getListCellRendererComponent(list, info.getName(), index, isSelected, cellHasFocus);
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

    jPanel1 = new javax.swing.JPanel();
    checkFingerSearch = new javax.swing.JCheckBox();
    checkIgnoreAllBass = new javax.swing.JCheckBox();
    checkAllowAllBass = new javax.swing.JCheckBox();
    checkIgnoreMaxThres = new javax.swing.JCheckBox();
    maxComboThreshSpin = new javax.swing.JSpinner();
    jLabel2 = new javax.swing.JLabel();
    maxComboLenSpin = new javax.swing.JSpinner();
    jPanel2 = new javax.swing.JPanel();
    lnfCombo = new javax.swing.JComboBox();
    jLabel1 = new javax.swing.JLabel();
    toggleBoardLeftTop = new javax.swing.JCheckBox();
    toggleBoardHoriz = new javax.swing.JCheckBox();
    toggleEditorPos = new javax.swing.JCheckBox();
    jPanel3 = new javax.swing.JPanel();
    comboBassInstru = new javax.swing.JComboBox();
    comboChordInstru = new javax.swing.JComboBox();
    resetInstruments = new javax.swing.JButton();

    jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Sequence Search Options:"));

    checkFingerSearch.setText("Finger Search Enabled");
    checkFingerSearch.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkFingerSearchActionPerformed(evt);
      }
    });

    checkIgnoreAllBass.setSelected(true);
    checkIgnoreAllBass.setText("Ignore All Bass Combos");
    checkIgnoreAllBass.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkIgnoreAllBassActionPerformed(evt);
      }
    });

    checkAllowAllBass.setSelected(true);
    checkAllowAllBass.setText("Allow if no better choice");
    checkAllowAllBass.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkAllowAllBassActionPerformed(evt);
      }
    });

    checkIgnoreMaxThres.setSelected(true);
    checkIgnoreMaxThres.setText("Ignore All Combos Wider Than:");
    checkIgnoreMaxThres.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkIgnoreMaxThresActionPerformed(evt);
      }
    });

    maxComboThreshSpin.setModel(new javax.swing.SpinnerNumberModel(6, 3, 20, 1));
    maxComboThreshSpin.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        maxComboThreshSpinStateChanged(evt);
      }
    });

    jLabel2.setText("Max Button Combo Size:");

    maxComboLenSpin.setModel(new javax.swing.SpinnerNumberModel(4, 1, 4, 1));
    maxComboLenSpin.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        maxComboLenSpinStateChanged(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(21, 21, 21)
            .addComponent(checkAllowAllBass, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addComponent(checkIgnoreAllBass, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(checkFingerSearch, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(checkIgnoreMaxThres)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(maxComboThreshSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(jLabel2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(maxComboLenSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(checkFingerSearch)
          .addComponent(checkIgnoreMaxThres)
          .addComponent(maxComboThreshSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(checkIgnoreAllBass)
          .addComponent(jLabel2)
          .addComponent(maxComboLenSpin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(checkAllowAllBass))
    );

    jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Appearence:"));

    lnfCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        lnfComboActionPerformed(evt);
      }
    });

    jLabel1.setText("Look and Feel:");

    toggleBoardLeftTop.setText("Board Left/Top");
    toggleBoardLeftTop.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        toggleBoardLeftTopActionPerformed(evt);
      }
    });

    toggleBoardHoriz.setText("Board Vertical");
    toggleBoardHoriz.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        toggleBoardHorizActionPerformed(evt);
      }
    });

    toggleEditorPos.setText("Editor Left/Right");
    toggleEditorPos.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        toggleEditorPosActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(toggleEditorPos)
              .addComponent(toggleBoardLeftTop)
              .addComponent(toggleBoardHoriz)))
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addGap(10, 10, 10)
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(lnfCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap(10, Short.MAX_VALUE))
    );
    jPanel2Layout.setVerticalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(lnfCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(toggleBoardHoriz)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(toggleBoardLeftTop)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(toggleEditorPos)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Sound"));

    comboBassInstru.setToolTipText("Bass Instrument");
    comboBassInstru.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        comboBassInstruActionPerformed(evt);
      }
    });

    comboChordInstru.setToolTipText("Chord Instrument");
    comboChordInstru.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        comboChordInstruActionPerformed(evt);
      }
    });

    resetInstruments.setAction(new ResetToAccordionAction());

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(comboBassInstru, 0, 155, Short.MAX_VALUE)
          .addComponent(comboChordInstru, javax.swing.GroupLayout.Alignment.TRAILING, 0, 155, Short.MAX_VALUE))
        .addContainerGap())
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
        .addGap(22, 22, 22)
        .addComponent(resetInstruments)
        .addContainerGap(110, Short.MAX_VALUE))
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addComponent(comboBassInstru, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(comboChordInstru, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(resetInstruments)
        .addContainerGap(37, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(7, 7, 7)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
    );
  }// </editor-fold>//GEN-END:initComponents

  private void lnfComboActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_lnfComboActionPerformed
  {//GEN-HEADEREND:event_lnfComboActionPerformed
    changeLNF();
  }//GEN-LAST:event_lnfComboActionPerformed

  private void toggleBoardLeftTopActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_toggleBoardLeftTopActionPerformed
  {//GEN-HEADEREND:event_toggleBoardLeftTopActionPerformed
    //((BassToolFrame) Main._rootFrame).toggleBoardPos();
    this.firePropertyChange(TOGGLE_BOARDPOS_PROPERTY, null, null);
  }//GEN-LAST:event_toggleBoardLeftTopActionPerformed

  private void toggleBoardHorizActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_toggleBoardHorizActionPerformed
  {//GEN-HEADEREND:event_toggleBoardHorizActionPerformed
    //((BassToolFrame) Main._rootFrame).toggleOrientation();
    this.firePropertyChange(TOGGLE_ORIENT_PROPERTY, null, null);
  }//GEN-LAST:event_toggleBoardHorizActionPerformed

  private void checkFingerSearchActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkFingerSearchActionPerformed
  {//GEN-HEADEREND:event_checkFingerSearchActionPerformed

    if (columnModel != null) {
      columnModel.optFingerSearch = (checkFingerSearch.isSelected());
      recompute();
    }
  }//GEN-LAST:event_checkFingerSearchActionPerformed

  private void checkIgnoreAllBassActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkIgnoreAllBassActionPerformed
  {//GEN-HEADEREND:event_checkIgnoreAllBassActionPerformed
    BoardSearcher.optIgnoreBassOnly = checkIgnoreAllBass.isSelected();
    this.checkAllowAllBass.setEnabled(checkIgnoreAllBass.isSelected());
    recompute();
  }//GEN-LAST:event_checkIgnoreAllBassActionPerformed

  private void checkIgnoreMaxThresActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkIgnoreMaxThresActionPerformed
  {//GEN-HEADEREND:event_checkIgnoreMaxThresActionPerformed
    if (checkIgnoreMaxThres.isSelected()) {
      Integer thres = (Integer) this.maxComboThreshSpin.getValue();
      ButtonCombo.setMaxComboDistThreshold(thres.intValue());
    } else {
      ButtonCombo.setMaxComboDistThreshold(0);
    }
    maxComboThreshSpin.setEnabled(checkIgnoreMaxThres.isSelected());
    recompute();
  }//GEN-LAST:event_checkIgnoreMaxThresActionPerformed

  private void checkAllowAllBassActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkAllowAllBassActionPerformed
  {//GEN-HEADEREND:event_checkAllowAllBassActionPerformed
    BoardSearcher.optAllowBassOnlyIfNoChords = checkAllowAllBass.isSelected();
    recompute();
  }//GEN-LAST:event_checkAllowAllBassActionPerformed

  private void maxComboThreshSpinStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_maxComboThreshSpinStateChanged
  {//GEN-HEADEREND:event_maxComboThreshSpinStateChanged
    Integer thres = (Integer) this.maxComboThreshSpin.getValue();
    ButtonCombo.setMaxComboDistThreshold(thres.intValue());
    recompute();
  }//GEN-LAST:event_maxComboThreshSpinStateChanged

  private void maxComboLenSpinStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_maxComboLenSpinStateChanged
  {//GEN-HEADEREND:event_maxComboLenSpinStateChanged
    Integer maxInt = (Integer) this.maxComboLenSpin.getValue();
    BoardSearcher.optMaxComboLength = maxInt.intValue();
    recompute();
  }//GEN-LAST:event_maxComboLenSpinStateChanged

  private void toggleEditorPosActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_toggleEditorPosActionPerformed
  {//GEN-HEADEREND:event_toggleEditorPosActionPerformed
    this.firePropertyChange(TOGGLE_EDITOR_PROPERTY, null, null);
    //((BassToolFrame) Main._rootFrame).toggleEditorLeft();
  }//GEN-LAST:event_toggleEditorPosActionPerformed

  private void comboBassInstruActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_comboBassInstruActionPerformed
  {//GEN-HEADEREND:event_comboBassInstruActionPerformed
    player.setInstrument(false, (Instrument) comboBassInstru.getSelectedItem());
  }//GEN-LAST:event_comboBassInstruActionPerformed

  private void comboChordInstruActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_comboChordInstruActionPerformed
  {//GEN-HEADEREND:event_comboChordInstruActionPerformed
    player.setInstrument(true, (Instrument) comboChordInstru.getSelectedItem());
  }//GEN-LAST:event_comboChordInstruActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox checkAllowAllBass;
  private javax.swing.JCheckBox checkFingerSearch;
  private javax.swing.JCheckBox checkIgnoreAllBass;
  private javax.swing.JCheckBox checkIgnoreMaxThres;
  private javax.swing.JComboBox comboBassInstru;
  private javax.swing.JComboBox comboChordInstru;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JComboBox lnfCombo;
  private javax.swing.JSpinner maxComboLenSpin;
  private javax.swing.JSpinner maxComboThreshSpin;
  private javax.swing.JButton resetInstruments;
  private javax.swing.JCheckBox toggleBoardHoriz;
  private javax.swing.JCheckBox toggleBoardLeftTop;
  private javax.swing.JCheckBox toggleEditorPos;
  // End of variables declaration//GEN-END:variables
}
