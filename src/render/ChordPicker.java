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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import music.Chord;
import music.ChordRegistry;
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
  boolean isUpdating = false;

  enum ChordFilter
  {

    ShowAll("All"),
    ShowStandard("Standard"),
    Show6th("6th"),
    Show7th("7th"),
    Show9th("9th"),
    Show11th("11th"),
    ShowMisc("Misc"),
    ShowCustomOnly("Only Custom");
    String desc;

    ChordFilter(String str)
    {
      desc = str;
    }

    boolean accepts(RegistryChordDef def)
    {
      String relChordString = def.relChord.toString();

      switch (this) {
        case ShowAll:
          return true;

        case ShowStandard:
          return (def.group.equals("std"));

        case Show6th:
          return (relChordString.contains("13"));

        case Show7th:
          return (relChordString.contains("7") && !relChordString.contains("9"));

        case Show9th:
          return (relChordString.contains("9"));

        case Show11th:
          return (relChordString.contains("11"));

        case ShowMisc:
          return (def.group.equals("misc"));

        case ShowCustomOnly:
          return false;
      }

      return false;
    }

    @Override
    public String toString()
    {
      return "Show " + desc + " Chords";
    }
  };

  /** Creates new form ChordPicker */
  public ChordPicker()
  {
    //relChord = new RelChord("135");

    initComponents();

    customDef = RegistryChordDef.getCustomDef();

    filterCombo.setModel(new DefaultComboBoxModel(ChordFilter.values()));

    populateFilteredList();

    listChords.addListSelectionListener(new ListSelectionListener()
    {
      @Override
      public void valueChanged(ListSelectionEvent e)
      {
        if (!isUpdating) {
          RegistryChordDef chordDef = (RegistryChordDef) listChords.getSelectedValue();
          selectChord(chordDef);
        }
      }
    });

    initStepControls();
  }

  void populateFilteredList()
  {
    ChordFilter filter = (ChordFilter) filterCombo.getSelectedItem();

    Vector<RegistryChordDef> vec = new Vector<RegistryChordDef>();
    vec.add(customDef);
    for (RegistryChordDef def : ChordRegistry.mainRegistry().allChordDefs) {
      if (filter.accepts(def)) {
        vec.add(def);
      }
    }
    listChords.setListData(vec);

    matchListToSelection();
    chordChanged();
  }

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
      if (isUpdating) {
        return;
      }

      if (e.getSource() instanceof JCheckBox) {
        combo.setVisible(check.isSelected());

        if (check.isSelected()) {
          relChord.setStep(stepIndex, (NoteDegreeType) combo.getSelectedItem());
        } else {
          relChord.setStep(stepIndex, null);
        }
        matchListToSelection();
        chordChanged();

      } else if (e.getSource() instanceof JComboBox) {
        JComboBox box = (JComboBox) e.getSource();
        relChord.setStep(stepIndex, (NoteDegreeType) box.getSelectedItem());
        matchListToSelection();
        chordChanged();
      }
    }

    public void setStep(NoteDegreeType step)
    {
      isUpdating = true;

      if (step == null) {
        check.setSelected(false);
      } else {
        combo.setSelectedItem(step);
        check.setSelected(true);
      }
      combo.setVisible(check.isSelected());

      isUpdating = false;
    }
  }

  private void initStepControls()
  {
    JComboBox combos[] = {combo3, combo5, combo7, combo9, combo11, combo13};
    JCheckBox checks[] = {check3, check5, check7, check9, check11, check13};

    stepChangers = new StepChangeListener[combos.length];

    for (int i = 0; i < stepChangers.length; i++) {
      stepChangers[i] = new StepChangeListener(i, combos[i], checks[i]);
    }
  }

  private void selectChord(RegistryChordDef chordDef)
  {
    if ((chordDef == customDef) || (chordDef == null)) {
      return;
    }

    relChord = (RelChord) chordDef.relChord.clone();

    for (int i = 0; i < stepChangers.length; i++) {
      NoteDegreeType step = relChord.getStep(i);
      stepChangers[i].setStep(step);
    }

    chordChanged();
  }

  public void setRelChord(RelChord newRelChord)
  {
    //relChord = (RelChord) newRelChord.clone();
    relChord = newRelChord;

    for (int i = 0; i < stepChangers.length; i++) {
      NoteDegreeType step = relChord.getStep(i);
      stepChangers[i].setStep(step);
    }

    matchListToSelection();
  }

  private void chordChanged()
  {
    this.firePropertyChange("Chord", null, relChord);
    //String string = relChord.buildChord(rootNote).toHtmlString();
    //statusLabel.setText("<html>" + string + "</html>");
    //System.out.println(string);
  }

  private void matchListToSelection()
  {
    if (isUpdating || (relChord == null)) {
      return;
    }

    ListModel model = listChords.getModel();

    int index = 0;

    relChord.setOrigDef(null);
    
    Note defaultRoot = new Note();

    Chord.Mask matchMask = relChord.buildChord(defaultRoot).getChordMask();

    for (int i = 1; i < model.getSize(); i++) {
      RegistryChordDef def = (RegistryChordDef) model.getElementAt(i);
      if (def.matchesMask(matchMask)) {
        index = i;
        relChord.setOrigDef(def);
        break;
      }
    }

    isUpdating = true;
    listChords.setSelectedIndex(index);
    listChords.scrollRectToVisible(listChords.getCellBounds(index, index));
    isUpdating = false;
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
    filterCombo = new javax.swing.JComboBox();
    jLabel1 = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    chordGridPanel = new javax.swing.JPanel();
    combo3 = new javax.swing.JComboBox();
    combo5 = new javax.swing.JComboBox();
    combo7 = new javax.swing.JComboBox();
    combo9 = new javax.swing.JComboBox();
    combo11 = new javax.swing.JComboBox();
    combo13 = new javax.swing.JComboBox();
    jPanel1 = new javax.swing.JPanel();
    check3 = new javax.swing.JCheckBox();
    check5 = new javax.swing.JCheckBox();
    check7 = new javax.swing.JCheckBox();
    check9 = new javax.swing.JCheckBox();
    check11 = new javax.swing.JCheckBox();
    check13 = new javax.swing.JCheckBox();

    listChords.setModel(new javax.swing.AbstractListModel() {
      String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    jScrollPane2.setViewportView(listChords);

    filterCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        filterComboActionPerformed(evt);
      }
    });

    jLabel1.setText("Select Chord");

    jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
    jPanel2.setLayout(new java.awt.BorderLayout());

    chordGridPanel.setLayout(new java.awt.GridLayout(6, 1, 0, 4));
    chordGridPanel.add(combo3);
    chordGridPanel.add(combo5);
    chordGridPanel.add(combo7);
    chordGridPanel.add(combo9);
    chordGridPanel.add(combo11);
    chordGridPanel.add(combo13);

    jPanel2.add(chordGridPanel, java.awt.BorderLayout.CENTER);

    jPanel1.setLayout(new java.awt.GridLayout(6, 1, 0, 4));

    check3.setLabel("3rd:");
    jPanel1.add(check3);

    check5.setText("5th:");
    jPanel1.add(check5);

    check7.setText("7th:");
    jPanel1.add(check7);

    check9.setText("9th (2nd):");
    jPanel1.add(check9);

    check11.setText("11th (4th):");
    jPanel1.add(check11);

    check13.setText("13th (6th):");
    jPanel1.add(check13);

    jPanel2.add(jPanel1, java.awt.BorderLayout.WEST);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(layout.createSequentialGroup()
            .addGap(2, 2, 2)
            .addComponent(jLabel1)
            .addGap(18, 18, 18)
            .addComponent(filterCombo, 0, 127, Short.MAX_VALUE))
          .addComponent(jScrollPane2))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(filterCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE))
      .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 167, Short.MAX_VALUE)
    );
  }// </editor-fold>//GEN-END:initComponents

  private void filterComboActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_filterComboActionPerformed
  {//GEN-HEADEREND:event_filterComboActionPerformed
    populateFilteredList();
  }//GEN-LAST:event_filterComboActionPerformed
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
  private javax.swing.JComboBox filterCombo;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JList listChords;
  // End of variables declaration//GEN-END:variables
}
