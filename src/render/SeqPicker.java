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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
public class SeqPicker extends ToolPanel implements PropertyChangeListener, ChangeListener, ListSelectionListener
{

  String newNoteSeq = "";
  boolean isUpdating = false;
  boolean isMultiSeq = false;
  DefaultListModel seqListData;

  static class SubSeq implements Cloneable
  {

    String notes;
    Vector<ParsedChordDef> chords;

    SubSeq(Note newRoot, String notes)
    {
      this.notes = notes;

      StringParser parser = new StringParser(notes);
      chords = ChordParser.parseChords(parser, false);

      Interval ival = newRoot.diff(getRoot());
      transpose(ival);
    }

    void transpose(Interval ival)
    {
      if (ival.interval != 0) {
        ParsedChordDef.transposeAllByInterval(chords.listIterator(), ival);
        this.notes = ParsedChordDef.toString(chords.listIterator(), false);
      }
    }

    Note getRoot()
    {
      return (chords.isEmpty() ? new Note() : chords.firstElement().rootNote);
    }

    @Override
    public String toString()
    {
      return notes;
    }

    @Override
    protected Object clone()
    {
      return new SubSeq(getRoot(), notes);
    }
  }

  /** Creates new form SeqPicker */
  public SeqPicker()
  {
    initComponents();

    tabby.addChangeListener(this);
    notePickerRoot.addPropertyChangeListener("Note", this);
    simpleAccomp1.addPropertyChangeListener("Seq", this);
    listScales.addListSelectionListener(this);
  }

  @Override
  public void init(SeqColumnModel model)
  {
    super.init(model);

    filterCombo.setModel(new DefaultComboBoxModel(ChordRegistry.mainRegistry().allSeqGroupNames));

    if (ChordRegistry.mainRegistry().allSeqDefs.length > 0) {
      listScales.setListData(ChordRegistry.mainRegistry().allSeqDefs[0]);
    }

    seqListData = new DefaultListModel();
    seqListData.addElement(simpleAccomp1.getState());
    listAllSeqs.setModel(seqListData);
    listAllSeqs.setSelectedIndex(0);
    listAllSeqs.addListSelectionListener(this);

    toggleAllowMultiSeq(false);
  }

  void setAnim(SeqAnimController anim)
  {
    this.buttonPlay.setAction(anim.getPlayStopAction());
  }

  @Override
  protected void syncUIToDataModel()
  {
    if ((columnModel != null) && !isUpdating) {
      ParsedChordDef def = columnModel.getChordDef(0);

      if ((def == null) || def.isEmptyChord()) {
        return;
      }

      updateCurrSeq();

//      if (seqListData.isEmpty()) {
//        return;
//      }
//
//      SubSeq first = (SubSeq) seqListData.get(0);
//
//      Interval ival = def.rootNote.diff(first.getRoot());
//
//      transposeAllSeqs(ival);
    }
  }

  private void transposeAllSeqs(Interval ival)
  {
    if (!isVisible() || seqListData.isEmpty()) {
      return;
    }

    for (int i = 0; i < seqListData.size(); i++) {
      SubSeq seq = (SubSeq) seqListData.get(i);
      seq.transpose(ival);
    }

    selectCurrSeq();
    listAllSeqs.repaint();
  }

  boolean isAlreadyAdjusting = false;

  @Override
  public void valueChanged(ListSelectionEvent e)
  {
    if (!e.getValueIsAdjusting() && this.isAlreadyAdjusting) {
      this.isAlreadyAdjusting = false;
      return;
    }

    this.isAlreadyAdjusting = e.getValueIsAdjusting();

    if (e.getSource() == listAllSeqs) {
      selectCurrSeq();
    } else if (e.getSource() == listScales) {
      updateCurrSeq();
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    if (isUpdating) {
      return;
    }

    if (evt.getSource() == notePickerRoot) {
      this.simpleAccomp1.setRoot(notePickerRoot.getNote());
    } else if (evt.getSource() == simpleAccomp1) {
      updateCurrSeq();
    } else if (evt.getPropertyName().equals(TransposePanel.TRANSPOSE_PROP)) {
      transposeAllSeqs((Interval)evt.getNewValue());
    }
  }

  @Override
  public void stateChanged(ChangeEvent e)
  {
    if (e.getSource() == tabby) {
      updateCurrSeq();
    }
  }

  private void selectCurrSeq()
  {
    Object obj = listAllSeqs.getSelectedValue();
    if (obj == null) {
      return;
    }

    isUpdating = true;

    if (obj instanceof SimpleAccomp.AccompState) {
      SimpleAccomp.AccompState state = (SimpleAccomp.AccompState) obj;

      notePickerRoot.setNote(state.getRoot());
      simpleAccomp1.setState(state);
      tabby.setSelectedComponent(simpleAccomp1);

      newNoteSeq = state.notes;

    } else if (obj instanceof SubSeq) {
      SubSeq state = (SubSeq) obj;

      notePickerRoot.setNote(state.getRoot());
      listScales.setSelectedValue(state.notes, true);
      tabby.setSelectedComponent(panScales);

      newNoteSeq = state.notes;
    }

    isUpdating = false;

    updateStatus();
  }

  private void updateCurrSeq()
  {
    if (isUpdating) {
      return;
    }

    if (this.simpleAccomp1.isVisible()) {
      newNoteSeq = simpleAccomp1.getCurrSeq();
      seqListData.set(listAllSeqs.getSelectedIndex(), simpleAccomp1.getState());
    } else {
      ChordRegistry.SeqDef seqDef = (ChordRegistry.SeqDef) listScales.getSelectedValue();

      if (seqDef == null) {
        return;
      }

      SubSeq subSeq = new SubSeq(notePickerRoot.getNote(), seqDef.noteSeq);

      Vector<ParsedChordDef> chords = subSeq.chords;

      if (chords.isEmpty()) {
        return;
      }

      seqListData.set(listAllSeqs.getSelectedIndex(), subSeq);
      newNoteSeq = subSeq.notes;
    }

    updateStatus();
  }

  private void updateStatus()
  {
    String fullSeq = buildFullSeq();

    String text = "<html>";
    text += "Full Progression: <b>";
    text += fullSeq;
    text += "</b></html>";
    statusText.setText(text);

    updateTableModel(fullSeq);
  }

  public void toggleAllowMultiSeq(boolean multi)
  {
    isMultiSeq = multi;
    multiSeqPanel.setVisible(multi);

    if (!multi) {

      // If none-multi seq, erase all except first seq

      isUpdating = true;

      Object currSel = listAllSeqs.getSelectedValue();
      seqListData.clear();
      seqListData.addElement(currSel);

      isUpdating = false;
      listAllSeqs.setSelectedIndex(0);
    }

    //updateStatus();
    this.firePropertyChange(ToolPanel.RESET_TO_PREF_SIZE, null, null);
  }

  private String buildFullSeq()
  {
    String fullSeq = "";

    for (int i = 0; i < seqListData.getSize(); i++) {
      SubSeq seq = (SubSeq) seqListData.get(i);
      if (!fullSeq.isEmpty()) {
        fullSeq += ", ";
      }
      fullSeq += seq.notes;
    }

    return fullSeq;
  }

  private void updateTableModel(String fullSeq)
  {
    if (columnModel == null) {
      return;
    }

    isUpdating = true;
    this.columnModel.populateFromText(fullSeq, false, null);
    isUpdating = false;
  }

  private void insertSeq()
  {
    SubSeq subSeq = (SubSeq)listAllSeqs.getSelectedValue();
    int index = listAllSeqs.getSelectedIndex();

    isUpdating = true;
    seqListData.add(index, subSeq.clone());
    isUpdating = false;

    listAllSeqs.setSelectedIndex(index);
  }

  private void addSeq()
  {
    SubSeq subSeq = (SubSeq)listAllSeqs.getSelectedValue();
    int index = listAllSeqs.getSelectedIndex();

    isUpdating = true;
    seqListData.addElement(subSeq.clone());
    isUpdating = false;

    listAllSeqs.setSelectedIndex(index + 1);
  }

  private void removeSeq()
  {
    if (seqListData.size() <= 1) {
      return;
    }

    isUpdating = true;
    int index = listAllSeqs.getSelectedIndex();
    seqListData.remove(index);
    if (index >= seqListData.size()) {
      index--;
    }
    isUpdating = false;
    listAllSeqs.setSelectedIndex(index);
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
    listScales = new javax.swing.JList();
    multiSeqPanel = new javax.swing.JPanel();
    seqListScroller = new javax.swing.JScrollPane();
    listAllSeqs = new javax.swing.JList();
    buttonAdd = new javax.swing.JButton();
    buttonInsert = new javax.swing.JButton();
    buttonRemove = new javax.swing.JButton();
    jPanel1 = new javax.swing.JPanel();
    buttonPlay = new javax.swing.JButton();
    notePickerRoot = new render.NotePicker();
    allowMulti = new javax.swing.JCheckBox();
    statusText = new render.TransparentTextPane();

    filterCombo.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        filterComboItemStateChanged(evt);
      }
    });

    setPreferredSize(new java.awt.Dimension(410, 217));

    tabby.addTab("Common Accomp", simpleAccomp1);

    listScales.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    listScrollPane.setViewportView(listScales);

    javax.swing.GroupLayout panScalesLayout = new javax.swing.GroupLayout(panScales);
    panScales.setLayout(panScalesLayout);
    panScalesLayout.setHorizontalGroup(
      panScalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(listScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
    );
    panScalesLayout.setVerticalGroup(
      panScalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(listScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
    );

    tabby.addTab("Scales", panScales);

    multiSeqPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("All Sequences:"));

    seqListScroller.setBorder(null);

    listAllSeqs.setModel(new javax.swing.AbstractListModel() {
      String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    seqListScroller.setViewportView(listAllSeqs);

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

    buttonRemove.setText("Remove");
    buttonRemove.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonRemoveActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout multiSeqPanelLayout = new javax.swing.GroupLayout(multiSeqPanel);
    multiSeqPanel.setLayout(multiSeqPanelLayout);
    multiSeqPanelLayout.setHorizontalGroup(
      multiSeqPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(multiSeqPanelLayout.createSequentialGroup()
        .addGroup(multiSeqPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(buttonAdd)
          .addComponent(buttonInsert)
          .addComponent(buttonRemove))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(seqListScroller, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE))
    );
    multiSeqPanelLayout.setVerticalGroup(
      multiSeqPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(multiSeqPanelLayout.createSequentialGroup()
        .addComponent(buttonAdd)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(buttonInsert)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(buttonRemove)
        .addContainerGap())
      .addComponent(seqListScroller, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
    );

    buttonPlay.setFont(buttonPlay.getFont().deriveFont(buttonPlay.getFont().getStyle() | java.awt.Font.BOLD, buttonPlay.getFont().getSize()+3));
    buttonPlay.setText("<html> Play</html>");
    buttonPlay.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    buttonPlay.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonPlayActionPerformed(evt);
      }
    });

    notePickerRoot.setBorder(javax.swing.BorderFactory.createTitledBorder("Starting Note:"));

    allowMulti.setText("<html>Allow Multiple Sequences</html>");
    allowMulti.setToolTipText("<html>When checked, allows you to <br/>\ninsert, replace, and add sequences and scales<br/>\nto build a custom bass progression.\n</html>");
    allowMulti.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        allowMultiActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(buttonPlay, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
              .addComponent(notePickerRoot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
          .addComponent(allowMulti, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)))
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(buttonPlay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(notePickerRoot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(20, 20, 20)
        .addComponent(allowMulti, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );

    statusText.setMinimumSize(new java.awt.Dimension(5, 56));
    statusText.setText("[No Sequence Selected]");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(multiSeqPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(tabby, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)))
        .addContainerGap())
      .addComponent(statusText, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(jPanel1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(tabby, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(multiSeqPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(statusText, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
        .addContainerGap())
    );

    tabby.getAccessibleContext().setAccessibleName("Scales");
  }// </editor-fold>//GEN-END:initComponents

    private void filterComboItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_filterComboItemStateChanged
    {//GEN-HEADEREND:event_filterComboItemStateChanged
      int index = filterCombo.getSelectedIndex();
      if ((index >= 0) && (index < ChordRegistry.mainRegistry().allSeqDefs.length)) {
        listScales.setListData(ChordRegistry.mainRegistry().allSeqDefs[index]);
      } else {
        listScales.setListData(new Object[0]);
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

    private void buttonRemoveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonRemoveActionPerformed
    {//GEN-HEADEREND:event_buttonRemoveActionPerformed
      removeSeq();
    }//GEN-LAST:event_buttonRemoveActionPerformed

    private void allowMultiActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_allowMultiActionPerformed
    {//GEN-HEADEREND:event_allowMultiActionPerformed
      toggleAllowMultiSeq(allowMulti.isSelected());
    }//GEN-LAST:event_allowMultiActionPerformed

    private void buttonPlayActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonPlayActionPerformed
    {//GEN-HEADEREND:event_buttonPlayActionPerformed
      updateTableModel(buildFullSeq());
    }//GEN-LAST:event_buttonPlayActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox allowMulti;
  private javax.swing.JButton buttonAdd;
  private javax.swing.JButton buttonInsert;
  private javax.swing.JButton buttonPlay;
  private javax.swing.JButton buttonRemove;
  private javax.swing.JComboBox filterCombo;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JList listAllSeqs;
  private javax.swing.JList listScales;
  private javax.swing.JScrollPane listScrollPane;
  private javax.swing.JPanel multiSeqPanel;
  private render.NotePicker notePickerRoot;
  private javax.swing.JPanel panScales;
  private javax.swing.JScrollPane seqListScroller;
  private render.SimpleAccomp simpleAccomp1;
  private render.TransparentTextPane statusText;
  private javax.swing.JTabbedPane tabby;
  // End of variables declaration//GEN-END:variables
}
