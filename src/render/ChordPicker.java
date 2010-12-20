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
import javax.swing.JComboBox;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import music.ChordRegistry;
import music.ChordRegistry.ChordGroupSet;
import music.Note;
import music.RegistryChordDef;
import music.RelChord;
import music.RelChord.BaseChordQual;
import music.RelChord.NoteDegreeType;

/**
 *
 * @author Ilya
 */
public class ChordPicker extends javax.swing.JPanel
{

  //ChordDataTableModel tableModel;
  RelChord relChord;
  JComboBox stepCombos[];
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

    Vector<RegistryChordDef> vec = new Vector<RegistryChordDef>();
    for (RegistryChordDef def : set.getChordDefs(0)) {
      vec.add(def);
    }
    vec.add(customDef);

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

    listChords.setSelectedIndex(0);

    initBaseCombo();
    //initTable();
    initStepCombos();
  }

  private void initBaseCombo()
  {

    comboBase.setModel(new DefaultComboBoxModel(BaseChordQual.values()));
    comboBase.setSelectedItem(relChord.getBaseQual());
    comboBase.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        relChord.setBaseQual((BaseChordQual) comboBase.getSelectedItem());
        chordChanged();
        matchListToSelection();
      }
    });
  }

  class StepComboListener implements ActionListener
  {

    final int stepIndex;

    StepComboListener(int index)
    {
      stepIndex = index;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      JComboBox box = (JComboBox) e.getSource();
      relChord.setStep(stepIndex, (NoteDegreeType) box.getSelectedItem());
      matchListToSelection();
    }
  }

  private void initStepCombos()
  {
    JComboBox combos[] = {comboStep5, comboStep7, comboStep9, comboStep11, comboStep13};
    this.stepCombos = combos;

    for (int i = 0; i < stepCombos.length; i++) {
      stepCombos[i].setModel(new DefaultComboBoxModel(NoteDegreeType.values()));
      stepCombos[i].addActionListener(new StepComboListener(i));
    }
  }

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
    comboBase.setSelectedItem(relChord.getBaseQual());

    if (stepCombos != null) {

      for (int i = 0; i < stepCombos.length; i++) {
        stepCombos[i].setSelectedItem(relChord.getStep(i));
      }
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

    int index = model.getSize() - 1;

    for (int i = 0; i < model.getSize() - 1; i++) {
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

//  class ChordDataTableModel extends AbstractTableModel
//  {
//
//    String[] columnNames = {"Note Step", "Value"};
//
//    public void update()
//    {
//      this.fireTableDataChanged();
//    }
//
//    @Override
//    public Class<?> getColumnClass(int columnIndex)
//    {
//      if (columnIndex == 0) {
//        return String.class;
//      } else {
//        return NoteDegreeType.class;
//      }
//    }
//
//    @Override
//    public String getColumnName(int column)
//    {
//      return columnNames[column];
//    }
//
//    @Override
//    public boolean isCellEditable(int rowIndex, int columnIndex)
//    {
//      if (columnIndex == 1) {
//        return true;
//      } else {
//        return false;
//      }
//    }
//
//    @Override
//    public int getColumnCount()
//    {
//      return columnNames.length;
//    }
//
//    @Override
//    public int getRowCount()
//    {
//      return RelChord.standardIval.length - 1;
//    }
//
//    @Override
//    public Object getValueAt(int rowIndex, int columnIndex)
//    {
//      if (columnIndex == 0) {
//        String str = "" + RelChord.indexToStep(rowIndex);
//        str += "th";
//        return str;
//      } else if (columnIndex == 1) {
//        return relChord.getStep(rowIndex);
//      } else {
//        return null;
//      }
//    }
//
//    @Override
//    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
//    {
//      if (columnIndex == 1) {
//        if (aValue instanceof NoteDegreeType) {
//          relChord.setStep(rowIndex, (NoteDegreeType) aValue);
//          chordChanged();
//          matchListToSelection();
//        }
//      }
//    }
//  }
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
    customCheck = new javax.swing.JCheckBox();
    jPanel1 = new javax.swing.JPanel();
    jLabel2 = new javax.swing.JLabel();
    comboBase = new javax.swing.JComboBox();
    labelStep5 = new javax.swing.JLabel();
    comboStep5 = new javax.swing.JComboBox();
    labelStep7 = new javax.swing.JLabel();
    comboStep7 = new javax.swing.JComboBox();
    labelStep9 = new javax.swing.JLabel();
    comboStep9 = new javax.swing.JComboBox();
    labelStep11 = new javax.swing.JLabel();
    comboStep11 = new javax.swing.JComboBox();
    labelStep13 = new javax.swing.JLabel();
    comboStep13 = new javax.swing.JComboBox();

    listChords.setModel(new javax.swing.AbstractListModel() {
      String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    jScrollPane2.setViewportView(listChords);

    jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

    jLabel1.setText("Show Chords:");

    customCheck.setText("Build Custom Chord");

    jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
    jPanel1.setLayout(new java.awt.GridLayout(6, 2, 12, 0));

    jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    jLabel2.setText("Base Chord:");
    jPanel1.add(jLabel2);
    jPanel1.add(comboBase);

    labelStep5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    labelStep5.setText("5th:");
    jPanel1.add(labelStep5);

    comboStep5.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    jPanel1.add(comboStep5);

    labelStep7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    labelStep7.setText("7th:");
    jPanel1.add(labelStep7);

    comboStep7.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    jPanel1.add(comboStep7);

    labelStep9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    labelStep9.setText("9th:");
    jPanel1.add(labelStep9);

    comboStep9.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    jPanel1.add(comboStep9);

    labelStep11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    labelStep11.setText("11th:");
    jPanel1.add(labelStep11);

    comboStep11.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    jPanel1.add(comboStep11);

    labelStep13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    labelStep13.setText("13th:");
    jPanel1.add(labelStep13);

    comboStep13.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    jPanel1.add(comboStep13);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(10, 10, 10)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(customCheck)
          .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(customCheck))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(jScrollPane2)
          .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox comboBase;
  private javax.swing.JComboBox comboStep11;
  private javax.swing.JComboBox comboStep13;
  private javax.swing.JComboBox comboStep5;
  private javax.swing.JComboBox comboStep7;
  private javax.swing.JComboBox comboStep9;
  private javax.swing.JCheckBox customCheck;
  private javax.swing.JComboBox jComboBox1;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JLabel labelStep11;
  private javax.swing.JLabel labelStep13;
  private javax.swing.JLabel labelStep5;
  private javax.swing.JLabel labelStep7;
  private javax.swing.JLabel labelStep9;
  private javax.swing.JList listChords;
  // End of variables declaration//GEN-END:variables
}
