/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SeqPicker.java
 *
 * Created on Jan 14, 2011, 3:29:04 PM
 */
package render;

import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import music.ChordParser;
import music.ChordRegistry;
import music.Interval;
import music.Note;
import music.ParsedChordDef;
import music.StringParser;

/**
 *
 * @author Ilya
 */
public class SeqPicker extends ToolPanel implements PropertyChangeListener, ChangeListener, TableModelListener, ListSelectionListener
{

  String newNoteSeq = "";
  boolean isUpdating = false;
  boolean isAutoReplace = false;

  /** Creates new form SeqPicker */
  public SeqPicker()
  {
    initComponents();

    tabby.addChangeListener(this);
    notePickerRoot.addPropertyChangeListener("Note", this);
    simpleAccomp1.addPropertyChangeListener("Seq", this);
    listSeqs.addListSelectionListener(this);

    statusText.setBackground(new Color(0, 0, 0, 0));

    toggleAutoReplace(true);
  }

  @Override
  public void init(SeqColumnModel model)
  {
    super.init(model);

    filterCombo.setModel(new DefaultComboBoxModel(ChordRegistry.mainRegistry().allSeqGroupNames));

    if (ChordRegistry.mainRegistry().allSeqDefs.length > 0) {
      listSeqs.setListData(ChordRegistry.mainRegistry().allSeqDefs[0]);
    }

    columnModel.getDataModel().addTableModelListener(this);
  }

  @Override
  protected void toggleListeners(boolean attach)
  {
    super.toggleListeners(attach);

    if (columnModel == null) {
      return;
    }

    if (attach) {
      columnModel.getDataModel().addTableModelListener(this);
    } else {
      columnModel.getDataModel().removeTableModelListener(this);
    }
  }

  @Override
  public void shown()
  {
    super.shown();
//    if (isAutoReplace) {
//      updateCurrSeq();
//    }
  }

  @Override
  protected void syncUIToDataModel()
  {
    if ((columnModel != null) && !isUpdating) {
      ParsedChordDef def = columnModel.getChordDef(0);

      if ((def == null) || def.isEmptyChord()) {
        return;
      }

      notePickerRoot.setNote(def.rootNote);
    }
  }

  @Override
  public void tableChanged(TableModelEvent e)
  {
    if (isVisible()) {
      syncUIToDataModel();
    }
  }

  @Override
  public void valueChanged(ListSelectionEvent e)
  {
    updateCurrSeq();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    if (isUpdating) {
      return;
    }

    if (evt.getSource() == notePickerRoot) {
      this.simpleAccomp1.setRoot(notePickerRoot.getNote());
    } else {
      updateCurrSeq();
    }
  }

  @Override
  public void stateChanged(ChangeEvent e)
  {
    if (e.getSource() == tabby) {
      updateCurrSeq();
    }
  }

  private void updateCurrSeq()
  {
    if (this.simpleAccomp1.isVisible()) {
      newNoteSeq = simpleAccomp1.getCurrSeq();
    } else {
      ChordRegistry.SeqDef seqDef = (ChordRegistry.SeqDef) listSeqs.getSelectedValue();

      if (seqDef == null) {
        return;
      }

      StringParser parser = new StringParser(seqDef.noteSeq);
      Vector<ParsedChordDef> chords = ChordParser.parseChords(parser, false);

      if (chords.isEmpty()) {
        return;
      }

      Note rootNote = notePickerRoot.getNote();

      Interval ival = rootNote.diff(chords.firstElement().rootNote);

      ParsedChordDef.transposeAllByInterval(chords.listIterator(), ival);

      newNoteSeq = ParsedChordDef.toString(chords.listIterator(), false);
    }

    if (isAutoReplace) {
      replaceSeq();
    } else {
      updateStatus();
    }
  }

  private void updateStatus()
  {

    String text = "<html>";
    text += "Selected Sequence: <b>";
    text += newNoteSeq;
    if (!isAutoReplace) {
      text += "<br/>";
      text += "</b>Full Progression: <b>";
      if (columnModel != null) {
        text += columnModel.toHtmlString(false);
      }
      text += "</b>";
    }
    text += "</html>";

    statusText.setText(text);
  }

  public void toggleAutoReplace(boolean auto)
  {
    isAutoReplace = auto;
    buttonAdd.setVisible(!auto);
    buttonInsert.setVisible(!auto);
    buttonReplace.setVisible(!auto);

    if (isAutoReplace) {
      replaceSeq();
    } else {
      updateStatus();
    }
  }

  private void replaceSeq()
  {
    if ((columnModel != null) && isVisible()) {
      isUpdating = true;
      columnModel.populateFromText(newNoteSeq, false, null);
      updateStatus();
      isUpdating = false;
    }
  }

  private void insertSeq()
  {
    if (columnModel == null) {
      return;
    }

    int selCol = columnModel.getSelectedColumn();

    String prevSeq = columnModel.toString();

    int index = 0;

    while (selCol > 0) {
      index = prevSeq.indexOf(',', index + 1);
      selCol--;
    }

    assert (index >= 0);

    String start, end;

    if (index == 0) {
      start = "";
      end = ", " + prevSeq;
    } else {
      start = prevSeq.substring(0, index) + ", ";
      end = prevSeq.substring(index);
    }

    isUpdating = true;
    columnModel.populateFromText(start + newNoteSeq + end, false, null);
    updateStatus();
    isUpdating = false;
  }

  private void addSeq()
  {
    String existing = columnModel.toString();
    isUpdating = true;
    columnModel.populateFromText(existing + "," + newNoteSeq, false, null);
    updateStatus();
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

    filterCombo = new javax.swing.JComboBox();
    tabby = new javax.swing.JTabbedPane();
    simpleAccomp1 = new render.SimpleAccomp();
    panScales = new javax.swing.JPanel();
    listScrollPane = new javax.swing.JScrollPane();
    listSeqs = new javax.swing.JList();
    statusTextPane = new javax.swing.JScrollPane();
    statusText = new javax.swing.JTextPane();
    jPanel1 = new javax.swing.JPanel();
    buttonAdd = new javax.swing.JButton();
    buttonInsert = new javax.swing.JButton();
    buttonReplace = new javax.swing.JButton();
    notePickerRoot = new render.NotePicker();
    checkAuto = new javax.swing.JCheckBox();

    filterCombo.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        filterComboItemStateChanged(evt);
      }
    });

    setPreferredSize(new java.awt.Dimension(410, 217));

    tabby.addTab("Common Accomp", simpleAccomp1);

    listSeqs.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    listScrollPane.setViewportView(listSeqs);

    javax.swing.GroupLayout panScalesLayout = new javax.swing.GroupLayout(panScales);
    panScales.setLayout(panScalesLayout);
    panScalesLayout.setHorizontalGroup(
      panScalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(listScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
    );
    panScalesLayout.setVerticalGroup(
      panScalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(listScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
    );

    tabby.addTab("Scales", panScales);

    statusText.setContentType("text/html");
    statusText.setEditable(false);
    statusText.setFont(statusText.getFont().deriveFont(statusText.getFont().getSize()+3f));
    statusText.setText("[No Sequence Selected]");
    statusText.setOpaque(false);
    statusTextPane.setViewportView(statusText);

    buttonAdd.setText("Add Seq");
    buttonAdd.setToolTipText("Add selected sequence to the end");
    buttonAdd.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonAddActionPerformed(evt);
      }
    });

    buttonInsert.setText("Insert Seq");
    buttonInsert.setToolTipText("");
    buttonInsert.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonInsertActionPerformed(evt);
      }
    });

    buttonReplace.setText("Set Seq");
    buttonReplace.setToolTipText("");
    buttonReplace.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonReplaceActionPerformed(evt);
      }
    });

    notePickerRoot.setBorder(javax.swing.BorderFactory.createTitledBorder("Starting Note:"));

    checkAuto.setText("<html>Custom Bass<br/ Progression</html>");
    checkAuto.setToolTipText("<html>When checked, allows you to <br/>\ninsert, replace, and add sequences and scales<br/>\nto build a custom bass progression.\n</html>");
    checkAuto.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkAutoActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(notePickerRoot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(buttonReplace)
          .addComponent(buttonInsert)
          .addComponent(buttonAdd)
          .addComponent(checkAuto, 0, 0, Short.MAX_VALUE))
        .addContainerGap(11, Short.MAX_VALUE))
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(checkAuto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(buttonReplace)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(buttonInsert)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(buttonAdd)
        .addGap(18, 18, 18)
        .addComponent(notePickerRoot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(25, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(statusTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
          .addComponent(tabby, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(statusTextPane, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(tabby, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE))
      .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );

    tabby.getAccessibleContext().setAccessibleName("Scales");
  }// </editor-fold>//GEN-END:initComponents

    private void filterComboItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_filterComboItemStateChanged
    {//GEN-HEADEREND:event_filterComboItemStateChanged
      int index = filterCombo.getSelectedIndex();
      if ((index >= 0) && (index < ChordRegistry.mainRegistry().allSeqDefs.length)) {
        listSeqs.setListData(ChordRegistry.mainRegistry().allSeqDefs[index]);
      } else {
        listSeqs.setListData(new Object[0]);
      }
    }//GEN-LAST:event_filterComboItemStateChanged

    private void buttonAddActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonAddActionPerformed
    {//GEN-HEADEREND:event_buttonAddActionPerformed
      addSeq();
    }//GEN-LAST:event_buttonAddActionPerformed

    private void buttonInsertActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonInsertActionPerformed
    {//GEN-HEADEREND:event_buttonInsertActionPerformed
      insertSeq();
    }//GEN-LAST:event_buttonInsertActionPerformed

    private void buttonReplaceActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonReplaceActionPerformed
    {//GEN-HEADEREND:event_buttonReplaceActionPerformed
      replaceSeq();
    }//GEN-LAST:event_buttonReplaceActionPerformed

    private void checkAutoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkAutoActionPerformed
    {//GEN-HEADEREND:event_checkAutoActionPerformed
      toggleAutoReplace(!checkAuto.isSelected());
    }//GEN-LAST:event_checkAutoActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton buttonAdd;
  private javax.swing.JButton buttonInsert;
  private javax.swing.JButton buttonReplace;
  private javax.swing.JCheckBox checkAuto;
  private javax.swing.JComboBox filterCombo;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane listScrollPane;
  private javax.swing.JList listSeqs;
  private render.NotePicker notePickerRoot;
  private javax.swing.JPanel panScales;
  private render.SimpleAccomp simpleAccomp1;
  private javax.swing.JTextPane statusText;
  private javax.swing.JScrollPane statusTextPane;
  private javax.swing.JTabbedPane tabby;
  // End of variables declaration//GEN-END:variables
}
