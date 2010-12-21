/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ChordPicker.java
 *
 * Created on Dec 11, 2010, 12:39:59 PM
 */
package render;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import music.ChordRegistry;
import music.ChordRegistry.ChordGroupSet;
import music.Note;
import music.RegistryChordDef;
import music.RelChord;
import music.RelChord.NoteDegreeType;

/**
 *
 * @author Ilya
 */
public class ChordPicker extends javax.swing.JPanel
{
  RelChord relChord;

  StepChangeListener[] stepChangers;

  RegistryChordDef customDef;
  Note rootNote = new Note();

  /** Creates new form ChordPicker */
  public ChordPicker()
  {
    //relChord = new RelChord("135");

    initComponents();

    //Chord Combo

    ChordGroupSet set = ChordRegistry.mainRegistry().findChordSet(ChordRegistry.ALL_CHORDS);

//    comboChords.setModel(new DefaultComboBoxModel(set.getChordDefs(0)));
//    comboChords.addActionListener(new ActionListener()
//    {
//
//      @Override
//      public void actionPerformed(ActionEvent e)
//      {
//        RegistryChordDef chordDef = (RegistryChordDef) comboChords.getSelectedItem();
//        selectChord(chordDef);
//      }
//    });
//
//    comboChords.setSelectedIndex(0);

    customDef = RegistryChordDef.getCustomDef();
    //relChord = customDef.relChord;

    Vector<RegistryChordDef> vec = new Vector<RegistryChordDef>();
    vec.add(customDef);
    for (RegistryChordDef def : set.getChordDefs(0)) {
      vec.add(def);
    }

    listChords.setListData(vec);

    listChords.addListSelectionListener(new ListSelectionListener()
    {

      @Override
      public void valueChanged(ListSelectionEvent e)
      {
        RegistryChordDef chordDef = (RegistryChordDef) listChords.getSelectedValue();
        selectChord(chordDef);
      }
    });

    //initBaseCombo();
    //initTable();
    initStepControls();

    listChords.setSelectedIndex(1);
  }

//  private void initBaseCombo()
//  {
//
//    comboBase.setModel(new DefaultComboBoxModel(BaseChordQual.values()));
//    comboBase.setSelectedItem(relChord.getBaseQual());
//    comboBase.addActionListener(new ActionListener()
//    {
//
//      @Override
//      public void actionPerformed(ActionEvent e)
//      {
//        relChord.setBaseQual((BaseChordQual) comboBase.getSelectedItem());
//        chordChanged();
//        matchListToSelection();
//      }
//    });
//  }
  class StepChangeListener implements ActionListener
  {

    final int stepIndex;
    JComboBox combo;
    JCheckBox check;

    StepChangeListener(int index, JComboBox combo, JCheckBox check)
    {
      stepIndex = index;

      this.combo = combo;
      this.check = check;

      combo.setModel(new DefaultComboBoxModel(NoteDegreeType.values()));
      combo.setSelectedItem(NoteDegreeType.Normal);
      
      combo.addActionListener(this);
      check.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (e.getSource() instanceof JCheckBox) {
        combo.setVisible(check.isSelected());
        
        if (check.isSelected()) {
          relChord.setStep(stepIndex, (NoteDegreeType) combo.getSelectedItem());
        } else {
          relChord.setStep(stepIndex, null);
        }
        matchListToSelection();

      } else if (e.getSource() instanceof JComboBox) {
        JComboBox box = (JComboBox) e.getSource();
        relChord.setStep(stepIndex, (NoteDegreeType) box.getSelectedItem());
        matchListToSelection();
      }
    }

    public void setStep(NoteDegreeType step)
    {
      if (step == null) {
        check.setSelected(false);
      } else {
        combo.setSelectedItem(step);
        check.setSelected(true);
      }
      combo.setVisible(check.isSelected());
    }

//    @Override
//    public void propertyChange(PropertyChangeEvent evt)
//    {
//      //ChordStepToolPanel stepTool = (ChordStepToolPanel)evt.getSource();
//      if (evt.getPropertyName().equals("Step")) {
//        relChord.setStep(stepIndex, (NoteDegreeType) evt.getNewValue());
//        matchListToSelection();
//      }
//    }
  }

  private void initStepControls()
  {
    JComboBox combos[] = {combo3, combo5, combo7, combo9, combo11, combo13};
    JCheckBox checks[] = {check3, check5, check7, check9, check11, check13};

    stepChangers = new StepChangeListener[combos.length];

//    GridBagLayout gridbag = new GridBagLayout();
//    chordGridPanel.removeAll();
//    chordGridPanel.setLayout(gridbag);
//
//    GridBagConstraints cons = new GridBagConstraints();
//    cons.gridx = cons.gridy = 0;
//    cons.ipadx = 2;
//    cons.ipady = 2;

    for (int i = 0; i < stepChangers.length; i++) {
      stepChangers[i] = new StepChangeListener(i, combos[i], checks[i]);

//      cons.gridx = 0;
//      cons.gridwidth = 1;
//      gridbag.setConstraints(checks[i], cons);
//      chordGridPanel.add(checks[i]);
//
//      cons.gridx = 1;
//      cons.gridwidth = 1;
//      gridbag.setConstraints(combos[i], cons);
//      chordGridPanel.add(combos[i]);
//
//      cons.gridy++;
    }
  }

//  private void initStepControls()
//  {
//    stepChecks = new JCheckBox[6];
//    //JComboBox combos[] = new JComboBox[6];
//    stepTools = new ChordStepToolPanelBase[6];
//    String text[] = {"3rd:", "5th:", "7th:", "9th (2nd):", "11th (4th):", "13th (6th):"};
//
//    GridBagLayout gridbag = new GridBagLayout();
//    chordGridPanel.setLayout(gridbag);
//
//    GridBagConstraints cons = new GridBagConstraints();
//    cons.gridx = cons.gridy = 0;
//    cons.ipadx = 2;
//    cons.ipady = 2;
//
//    for (int i = 0; i < stepTools.length; i++) {
//      StepChangeListener listener = new StepChangeListener(i);
//      stepTools[i] = new ChordStepToolPanel3();
//      stepTools[i].addPropertyChangeListener(listener);
//      stepChecks[i] = new JCheckBox(text[i]);
//      stepChecks[i].addActionListener(listener);
//      //labels[i].setHorizontalAlignment(JLabel.RIGHT);
//
//      cons.gridx = 0;
//      cons.gridwidth = 1;
//      gridbag.setConstraints(stepChecks[i], cons);
//      chordGridPanel.add(stepChecks[i]);
//
//      cons.gridx = 1;
//      cons.gridwidth = GridBagConstraints.REMAINDER;
//      gridbag.setConstraints(stepTools[i], cons);
//      chordGridPanel.add(stepTools[i]);
//
//      cons.gridy++;
//    }
//
////    this.stepCombos = combos;
////
////    for (int i = 0; i < stepCombos.length; i++) {
////      stepCombos[i].setModel(new DefaultComboBoxModel(NoteDegreeType.values()));
////      stepCombos[i].addActionListener(new StepChangeListener(i));
////    }
//  }
//  private void initTable()
//  {
//    tableModel = new ChordDataTableModel();
//    stepTable.setModel(tableModel);
//    TableColumnModel cm = stepTable.getColumnModel();
//    //cm.getColumn(1).setCellEditor(new DefaultCellEditor(comboBase));
//
//    JComboBox degreebox = new JComboBox(new DefaultComboBoxModel(NoteDegreeType.values()));
//    TableCellEditor editor = new DefaultCellEditor(degreebox);
//    cm.getColumn(1).setCellEditor(editor);
//  }
  private void selectChord(RegistryChordDef chordDef)
  {
    if (chordDef == customDef) {
      return;
    }
    relChord = (RelChord) chordDef.relChord.clone();
    //comboBase.setSelectedItem(relChord.getBaseQual());

    for (int i = 0; i < stepChangers.length; i++) {
      NoteDegreeType step = relChord.getStep(i);
      stepChangers[i].setStep(step);
    }

    chordChanged();
  }

  private void chordChanged()
  {
    //String string = relChord.buildChord(rootNote).toHtmlString();
    //statusLabel.setText("<html>" + string + "</html>");
    //System.out.println(string);
  }
  boolean isUpdating = false;

  private void matchListToSelection()
  {
    if (isUpdating) {
      return;
    }

    ListModel model = listChords.getModel();

    int index = 0;

    for (int i = 1; i < model.getSize(); i++) {
      RegistryChordDef def = (RegistryChordDef) model.getElementAt(i);
      if (def.relChord.equals(relChord)) {
        index = i;
        break;
      }
    }

    isUpdating = true;
    listChords.setSelectedIndex(index);
    listChords.scrollRectToVisible(listChords.getCellBounds(index, index));
    isUpdating = false;
  }

  abstract static class ChordStepToolPanelBase extends JPanel
  {

    public abstract void setStep(NoteDegreeType type);

    public abstract NoteDegreeType getStep();
  }

  static class ChordStepToolPanel extends ChordStepToolPanelBase implements ActionListener
  {

    private JToolBar toolbar;
    private NoteDegreeType currStep;
    private JToggleButton buttons[];
    private ButtonGroup group;
    private boolean isUpdating = false;

    ChordStepToolPanel()
    {
      currStep = null;
      toolbar = new JToolBar();
      toolbar.setFloatable(false);
      toolbar.setBorderPainted(false);

      final NoteDegreeType[] stepEnums = NoteDegreeType.values();

      buttons = new JToggleButton[stepEnums.length];
      group = new ButtonGroup();

      for (int i = 0; i < buttons.length; i++) {
        JToggleButton toggle = new JToggleButton(stepEnums[i].label);
        toggle.addActionListener(this);
        toggle.setActionCommand(stepEnums[i].label);
        toggle.setToolTipText(stepEnums[i].toString());
        group.add(toggle);
        buttons[i] = toggle;
        this.add(toggle);
        //toolbar.add(toggle);
      }

      //this.setLayout(new BorderLayout());
      //this.add(BorderLayout.CENTER, toolbar);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (isUpdating) {
        return;
      }
      for (int i = 0; i < buttons.length; i++) {
        if (buttons[i] == e.getSource()) {
          NoteDegreeType oldStep = currStep;
          currStep = NoteDegreeType.values()[i];
          this.firePropertyChange("Step", oldStep, currStep);
          return;
        }
      }
    }

    @Override
    public void setEnabled(boolean b)
    {
//      if (b) {
//        setStep(currStep);
//      } else {
//        group.clearSelection();
//      }

      for (JToggleButton button : buttons) {
        button.setEnabled(b);
      }

      toolbar.setEnabled(b);
      super.setEnabled(b);
    }

    public void setStep(NoteDegreeType type)
    {
      final NoteDegreeType[] stepEnums = NoteDegreeType.values();

      currStep = type;

      for (int i = 0; i < buttons.length; i++) {
        if (currStep == stepEnums[i]) {
          isUpdating = true;
          buttons[i].setSelected(true);
          isUpdating = false;
          return;
        }
      }
    }

    public NoteDegreeType getStep()
    {
      return (isEnabled() ? currStep : null);
    }
  }

  static class ChordStepToolPanel2 extends ChordStepToolPanelBase implements ChangeListener
  {

    private NoteDegreeType currStep;
    private boolean isUpdating = false;
    private JSlider slider;

    ChordStepToolPanel2()
    {
      currStep = null;
      slider = new JSlider();

      final NoteDegreeType[] stepEnums = NoteDegreeType.values();

      slider.setSnapToTicks(true);
      //slider.setPaintTicks(true);
      slider.setMinimum(0);
      slider.setMaximum(stepEnums.length - 1);
      slider.setMajorTickSpacing(1);
      slider.addChangeListener(this);

      slider.setMinimumSize(new Dimension(50, 50));

      //this.add(slider);

      this.setLayout(new BorderLayout());
      this.add(BorderLayout.CENTER, slider);
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
      if (isUpdating) {
        return;
      }

      if (e.getSource() == slider) {
        NoteDegreeType oldStep = currStep;
        currStep = NoteDegreeType.values()[slider.getValue()];
        this.firePropertyChange("Step", oldStep, currStep);
      }
    }

    @Override
    public void setEnabled(boolean b)
    {
      slider.setEnabled(b);
      super.setEnabled(b);
    }

    public void setStep(NoteDegreeType type)
    {
      final NoteDegreeType[] stepEnums = NoteDegreeType.values();

      for (int i = 0; i < stepEnums.length; i++) {
        if (type == stepEnums[i]) {
          isUpdating = true;
          slider.setValue(i);
          isUpdating = false;
          return;
        }
      }
    }

    public NoteDegreeType getStep()
    {
      return (isEnabled() ? currStep : null);
    }
  }

  static class ChordStepToolPanel3 extends ChordStepToolPanelBase implements ActionListener
  {

    private NoteDegreeType currStep;
    private boolean isUpdating = false;
    private JComboBox combo;

    ChordStepToolPanel3()
    {
      currStep = null;
      combo = new JComboBox(NoteDegreeType.values());
      combo.addActionListener(this);
      //this.add(combo);
      this.setLayout(new BorderLayout());
      this.add(BorderLayout.CENTER, combo);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (isUpdating) {
        return;
      }

      if (e.getSource() == combo) {
        NoteDegreeType oldStep = currStep;
        currStep = (NoteDegreeType) combo.getSelectedItem();
        this.firePropertyChange("Step", oldStep, currStep);
      }
    }

    @Override
    public void setEnabled(boolean b)
    {
      combo.setEnabled(b);
      super.setEnabled(b);
    }

    public void setStep(NoteDegreeType type)
    {
      isUpdating = true;
      currStep = type;
      combo.setSelectedItem(currStep);
      isUpdating = false;
    }

    public NoteDegreeType getStep()
    {
      return (isEnabled() ? currStep : null);
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

    jScrollPane2 = new javax.swing.JScrollPane();
    listChords = new javax.swing.JList();
    jComboBox1 = new javax.swing.JComboBox();
    jLabel1 = new javax.swing.JLabel();
    chordGridPanel = new javax.swing.JPanel();
    check3 = new javax.swing.JCheckBox();
    combo3 = new javax.swing.JComboBox();
    check5 = new javax.swing.JCheckBox();
    combo5 = new javax.swing.JComboBox();
    check7 = new javax.swing.JCheckBox();
    combo7 = new javax.swing.JComboBox();
    check9 = new javax.swing.JCheckBox();
    combo9 = new javax.swing.JComboBox();
    check11 = new javax.swing.JCheckBox();
    combo11 = new javax.swing.JComboBox();
    check13 = new javax.swing.JCheckBox();
    combo13 = new javax.swing.JComboBox();

    listChords.setModel(new javax.swing.AbstractListModel() {
      String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    jScrollPane2.setViewportView(listChords);

    jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

    jLabel1.setText("Show Chords:");

    chordGridPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
    chordGridPanel.setLayout(new java.awt.GridLayout(6, 2, 0, 8));

    check3.setLabel("3rd:");
    chordGridPanel.add(check3);
    chordGridPanel.add(combo3);

    check5.setText("5th:");
    chordGridPanel.add(check5);
    chordGridPanel.add(combo5);

    check7.setText("7th:");
    chordGridPanel.add(check7);
    chordGridPanel.add(combo7);

    check9.setText("9th (2nd):");
    chordGridPanel.add(check9);
    chordGridPanel.add(combo9);

    check11.setText("11th (4th):");
    check11.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        check11ActionPerformed(evt);
      }
    });
    chordGridPanel.add(check11);
    chordGridPanel.add(combo11);

    check13.setText("13th (6th):");
    chordGridPanel.add(check13);
    chordGridPanel.add(combo13);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(10, 10, 10)
        .addComponent(chordGridPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(chordGridPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(jLabel1)
              .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)))
        .addContainerGap())
    );
  }// </editor-fold>//GEN-END:initComponents

  private void check11ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_check11ActionPerformed
  {//GEN-HEADEREND:event_check11ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_check11ActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox check11;
  private javax.swing.JCheckBox check13;
  private javax.swing.JCheckBox check3;
  private javax.swing.JCheckBox check5;
  private javax.swing.JCheckBox check7;
  private javax.swing.JCheckBox check9;
  private javax.swing.JPanel chordGridPanel;
  private javax.swing.JComboBox combo11;
  private javax.swing.JComboBox combo13;
  private javax.swing.JComboBox combo3;
  private javax.swing.JComboBox combo5;
  private javax.swing.JComboBox combo7;
  private javax.swing.JComboBox combo9;
  private javax.swing.JComboBox jComboBox1;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JList listChords;
  // End of variables declaration//GEN-END:variables
}
