/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TabSimpleAccomp.java
 *
 * Created on Feb 1, 2011, 2:15:30 AM
 */
package render;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import music.BassBoard;
import music.BoardRow;
import music.Chord;
import music.NamedInterval;
import music.Note;

/**
 *
 * @author Ilya
 */
public class SimpleAccomp extends JPanel implements ActionListener, ListSelectionListener, PropertyChangeListener
{

  final static boolean ROOT = false;
  final static boolean CHORD = true;

  enum RhythmType
  {

    Polka("Polka", new boolean[]{ROOT, CHORD}),
    Waltz("Waltz", new boolean[]{ROOT, CHORD, CHORD});
//    Turkish4("Turkish 4"), {ROOT, CHORD, CHORD, CHORD, ROOT, ROOT, CHORD});
//    Simple7("Simple 7 (3+3+2)", {ROOT, ROOT, CHORD

    RhythmType(String name, boolean[] pattern)
    {
      this.name = name;
      this.pattern = pattern;
    }

    String getPattern(Note root, String chordName)
    {
      String str = "";

      for (int i = 0; i < pattern.length; i++) {
        if (i > 0) {
          str += ", ";
        }

        str += (pattern[i] ? chordName : root);
      }

      return str;
    }
    String name;
    boolean[] pattern;
  }

  private Note root = new Note();
  private String seq = "";
  private BassBoard.RowType rowType = null;

  private boolean isUpdating = false;

  /** Creates new form TabSimpleAccomp */
  public SimpleAccomp()
  {
    initComponents();

    checkAlt3rd.addActionListener(this);
    checkAlt5th.addActionListener(this);
    radio7th.addActionListener(this);
    radioMajor.addActionListener(this);
    radioMinor.addActionListener(this);

    listRhythms.setListData(RhythmType.values());
    listRhythms.addListSelectionListener(this);
    listRhythms.setSelectedIndex(0);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    updateSeq();
  }

  @Override
  public void valueChanged(ListSelectionEvent e)
  {
    updateSeq();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    updateSeq();
  }

  public void updateSeq()
  {
    if (isUpdating) {
      return;
    }

    rowType = BassBoard.RowType.BassRoot;

    if (radioMajor.isSelected()) {
      rowType = BassBoard.RowType.ChordMajor;
    } else if (radioMinor.isSelected()) {
      rowType = BassBoard.RowType.ChordMinor;
    } else if (radio7th.isSelected()) {
      rowType = BassBoard.RowType.Chord7th;
    }

    BoardRow row = BoardRow.findFromString(rowType.toString());

    if (row == null) {
      return;
    }

    Chord chord = row.getChord(root);
    String chordName = row.getChordName(root, false);

    RhythmType rhythm = (RhythmType) listRhythms.getSelectedValue();

    if (rhythm == null) {
      return;
    }

    seq = rhythm.getPattern(root, chordName);

    if (checkAlt5th.isSelected()) {
      seq += ", ";
      seq += rhythm.getPattern(root.add(NamedInterval.P5.interval), chordName);
    }

    if (checkAlt3rd.isSelected()) {
      seq += ", ";
      seq += rhythm.getPattern(chord.getNoteAt(1), chordName);
    }

    this.firePropertyChange("Seq", null, seq);
  }

  public void setRoot(Note root)
  {
    this.root = root;
    this.updateSeq();
  }

  public String getCurrSeq()
  {
    return seq;
  }

  class AccompState extends SeqPicker.SubSeq
  {
    RhythmType rhythm;
    BassBoard.RowType row;
    boolean cross5th;
    boolean cross3rd;

    AccompState()
    {
      super(SimpleAccomp.this.root,
            SimpleAccomp.this.seq);
      
      this.rhythm = (RhythmType) SimpleAccomp.this.listRhythms.getSelectedValue();
      this.row = SimpleAccomp.this.rowType;
      this.cross5th = SimpleAccomp.this.checkAlt5th.isSelected();
      this.cross3rd = SimpleAccomp.this.checkAlt3rd.isSelected();
    }
  }

  AccompState getState()
  {
    return new AccompState();
  }

  void setState(AccompState state)
  {
    isUpdating = true;

    this.listRhythms.setSelectedValue(state.rhythm, true);
    this.rowType = state.row;

    switch (rowType)
    {
      case ChordMajor:
        radioMajor.setSelected(true);
        break;

      case ChordMinor:
        radioMinor.setSelected(true);
        break;

      case Chord7th:
        radio7th.setSelected(true);
        break;
    }
    this.checkAlt5th.setSelected(state.cross5th);
    this.checkAlt3rd.setSelected(state.cross3rd);
    this.root = state.getRoot();
    
    isUpdating = false;

    updateSeq();
  }

  @Override
  public String toString()
  {
    String string = "Accomp-";
    string = (rowType != null ? rowType.toString() : "null");
    string += "-";

    RhythmType rhythm = (RhythmType) listRhythms.getSelectedValue();
    string += (rhythm != null ? rhythm.toString() : "null");

    if (checkAlt5th.isSelected()) {
      string += "5";
    }

    if (checkAlt3rd.isSelected()) {
      string += "3";
    }

    return string;
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    groupChordType = new javax.swing.ButtonGroup();
    jPanel1 = new javax.swing.JPanel();
    radioMajor = new javax.swing.JRadioButton();
    radioMinor = new javax.swing.JRadioButton();
    radio7th = new javax.swing.JRadioButton();
    jScrollPane1 = new javax.swing.JScrollPane();
    listRhythms = new javax.swing.JList();
    jPanel2 = new javax.swing.JPanel();
    checkAlt3rd = new javax.swing.JCheckBox();
    checkAlt5th = new javax.swing.JCheckBox();

    setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Chord Button:"));
    jPanel1.setPreferredSize(new java.awt.Dimension(100, 120));

    groupChordType.add(radioMajor);
    radioMajor.setSelected(true);
    radioMajor.setText("Major");

    groupChordType.add(radioMinor);
    radioMinor.setText("Minor");

    groupChordType.add(radio7th);
    radio7th.setText("7th");

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(radio7th)
          .addComponent(radioMajor)
          .addComponent(radioMinor))
        .addContainerGap(29, Short.MAX_VALUE))
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addComponent(radioMajor)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(radioMinor)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(radio7th))
    );

    add(jPanel1);

    jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Rhythm:"));
    jScrollPane1.setPreferredSize(new java.awt.Dimension(100, 120));
    jScrollPane1.setViewportView(listRhythms);

    add(jScrollPane1);

    jPanel2.setPreferredSize(new java.awt.Dimension(100, 60));

    checkAlt3rd.setText("Alternate 3rd");

    checkAlt5th.setText("Alternate 5th");

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(checkAlt3rd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(checkAlt5th))
        .addContainerGap())
    );
    jPanel2Layout.setVerticalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(checkAlt5th)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(checkAlt3rd)
        .addContainerGap())
    );

    add(jPanel2);
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox checkAlt3rd;
  private javax.swing.JCheckBox checkAlt5th;
  private javax.swing.ButtonGroup groupChordType;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JList listRhythms;
  private javax.swing.JRadioButton radio7th;
  private javax.swing.JRadioButton radioMajor;
  private javax.swing.JRadioButton radioMinor;
  // End of variables declaration//GEN-END:variables
}
