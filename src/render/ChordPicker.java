/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ChordPicker.java
 *
 * Created on Oct 18, 2010, 4:17:22 PM
 */
package render;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import music.Chord;
import music.ChordDef;
import music.ChordRegistry;
import music.ChordRegistry.ExtChordDef;
import music.Note;
import util.Main.StringParser;

/**
 *
 * @author Ilya
 */
public class ChordPicker extends javax.swing.JPanel
{

  /** Creates new form ChordPicker */
  public ChordPicker()
  {
    initComponents();
  }
  Note rootNote = new Note();
  ChordDef currTableChord;
  Note addedBassNote = new Note();
  SeqColumnModel seqColumnModel;
//  JTable chordTable;
//  NotePicker notePicker1;
//  NotePicker notePicker2;
//  JCheckBox addedBassCheck;
  String chordSet = ChordRegistry.ALL_CHORDS;

  int isUpdatingChord = 0;


  void setSeqColModel(SeqColumnModel model)
  {
    seqColumnModel = model;

    //Column Selection Change
    seqColumnModel.getSelectionModel().addListSelectionListener(
            new javax.swing.event.ListSelectionListener()
            {

              int lastSel = -1;

              @Override
              public void valueChanged(ListSelectionEvent e)
              {
                if (!isVisible()) {
                  return;
                }

                int index = seqColumnModel.getSelectedColumn();
                if (index == lastSel) {
                  return;
                }

                lastSel = index;

                if (index < 0) {
                  return;
                }

                setupChord(index);
              }
            });

  }

  public void init()
  {
    chordTable = chordTable;
    chordTable.setModel(new ChordTableModel());

    initChordTable();

    notePicker1.addPropertyChangeListener("Note", new PropertyChangeListener()
    {

      @Override
      public void propertyChange(PropertyChangeEvent evt)
      {
        rootNote = (Note) evt.getNewValue();
        updateCurrChord();
        chordTable.repaint();
      }
    });

    if (notePicker2 != null && addedBassCheck != null) {

      notePicker2.addPropertyChangeListener("Note", new PropertyChangeListener()
      {

        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
          addedBassNote = (Note) evt.getNewValue();
          updateCurrChord();
          chordTable.repaint();
        }
      });
//      notePicker2.setVisible(addedBassCheck.isSelected());

      addedBassCheck.addItemListener(new ItemListener()
      {

        @Override
        public void itemStateChanged(ItemEvent evt)
        {
          boolean usingAddedBass = (evt.getStateChange() == ItemEvent.SELECTED);
          notePicker2.setVisible(usingAddedBass);
          updateCurrChord();
        }
      });
    }
  }

  ChordDef getPickedChord()
  {
    ChordDef finalChord = buildChord();
    return finalChord;
  }

  void updateCurrChord()
  {
    ChordDef finalChord = buildChord();

    String notestr = finalChord.chord.toString("-", true);

    String info = "<html><b>" + finalChord.abbrevHtml + "</b>"
            + "<br/>" + finalChord.name + ": "
            + "(" + notestr + ")" + "</html>";

    statusLabel.setText(info);

    if (isUpdatingChord > 0) {
      return;
    }

    isUpdatingChord++;

    if (seqColumnModel != null) {
      seqColumnModel.setSelectedColumn(finalChord);
    }

    isUpdatingChord--;
  }

  ChordDef buildChord()
  {
//    if (isUpdatingChord > 0) {
//      return;
//    }
//
    //TODO implement
    boolean addedBassLowest = false;

    Note addedBass = (addedBassCheck.isSelected() ? addedBassNote : null);

    Chord fullChord =
            new Chord(currTableChord.chord,
            rootNote,
            addedBass, addedBassLowest);

    ChordDef finalChord = new ChordDef();

    finalChord.chord = fullChord;

    // -- Set HTML Abbrev
    finalChord.abbrevHtml = rootNote.toString(true) + currTableChord.abbrevHtml;

    boolean usingAddedBass = addedBassCheck.isSelected();

    if (usingAddedBass) {
      finalChord.abbrevHtml += "/" + addedBassNote.toString(true);
    }

    // -- Set Plain Abbrev

    finalChord.abbrevPlain = rootNote.toString() + currTableChord.abbrevPlain;

    if (usingAddedBass) {
      finalChord.abbrevPlain += "/" + addedBassNote.toString();
    }

    // -- Set Name
    finalChord.name = rootNote.toString(true) + " " + currTableChord.name;

    if (usingAddedBass) {
      finalChord.name += " over " + addedBassNote.toString(true);
    }

    return finalChord;
  }

  @Override
  public void setVisible(boolean visible)
  {
    if (visible)
    {
      int index = seqColumnModel.getSelectedColumn();
      if (index >= 0)
        setupChord(this.seqColumnModel.getChordDef(index));
    }
    super.setVisible(visible);
  }

  private void setupChord(int index)
  {
    if (isUpdatingChord > 0) {
      return;
    }
    isUpdatingChord++;
    setupChord(seqColumnModel.getChordDef(index));
    isUpdatingChord--;
  }

  private void setupChord(ChordDef possChordDef)
  {
    String chordStr = possChordDef.abbrevPlain;
    StringParser parser = new StringParser(chordStr);
    Note newRoot = Note.fromString(parser);

    if (newRoot == null) {
      return;
    }

    notePicker1.setNote(newRoot);

    if ((chordTable.getRowCount() == 0) || (chordTable.getColumnCount() == 0)) {
      return;
    }

    ChordRegistry.ExtChordDef extChordDef = null;

    extChordDef = ChordRegistry.mainRegistry().findChord(this.chordSet, parser);

    int row = extChordDef.r;
    int col = chordTable.convertColumnIndexToView(extChordDef.c);

    chordTable.setRowSelectionInterval(row, row);
    chordTable.setColumnSelectionInterval(col, col);

    Note newAddedBass = null;

    if (notePicker2 == null || addedBassCheck == null) {
      return;
    }

    if (parser.nextChar() == '/') {
      parser.incOffset(1);
      newAddedBass = Note.fromString(parser);
    }

    if (newAddedBass != null) {
      addedBassCheck.setSelected(true);
      notePicker2.setNote(newAddedBass);
    } else {
      addedBassCheck.setSelected(false);
    }
  }

  void changeChordSet(boolean simpleMode)
  {
    this.chordSet = (simpleMode ? ChordRegistry.SIMPLE_CHORDS : ChordRegistry.ALL_CHORDS);

    //this.addedBassCheck.setVisible(!simpleMode);

    this.chordTable.setColumnSelectionInterval(0, 0);

//    if (simpleMode && addedBassCheck.isSelected()) {
//      this.addedBassCheck.doClick();
//    }

    ((ChordTableModel) chordTable.getModel()).fireTableStructureChanged();

    chordTable.setRowSelectionInterval(0, 0);
    chordTable.setColumnSelectionInterval(0, 0);
  }

  private class ChordTableSelectListener implements ListSelectionListener
  {

    int selRow = 0;
    int selCol = -1;
    int invalidRow = -1, invalidCol = -1;

    private ChordDef getChordAt(int row, int col)
    {
      col = chordTable.convertColumnIndexToModel(col);
      return (ChordDef) chordTable.getModel().getValueAt(row, col);
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
      ListSelectionModel model = (ListSelectionModel) e.getSource();
      ChordDef chordDef;

      if (model == chordTable.getSelectionModel()) {
        int newRow = model.getAnchorSelectionIndex();

        if ((newRow < 0) || (selCol < 0)) {
          selRow = newRow;
          return;
        }

        if (newRow == selRow) {
          return;
        }

        if ((invalidCol >= 0)
                && (getChordAt(newRow, invalidCol) != null)) {
          int newCol = invalidCol;
          invalidCol = -1;
          selRow = newRow;
          chordTable.setColumnSelectionInterval(newCol, newCol);
          return;
        }

        chordDef = getChordAt(newRow, selCol);

        if (chordDef == null) {
          invalidRow = newRow;
          chordTable.setRowSelectionInterval(selRow, selRow);
          return;
        } else {
          selRow = newRow;
        }

        if (!e.getValueIsAdjusting()) {
          invalidRow = -1;
        }

      } else if (model == chordTable.getColumnModel().getSelectionModel()) {
        int newCol = model.getAnchorSelectionIndex();

        if ((newCol < 0) || (selRow < 0)) {
          selCol = newCol;
          return;
        }

        if (newCol == selCol) {
          return;
        }

        chordDef = getChordAt(selRow, newCol);

        if (chordDef == null) {
          invalidCol = newCol;
          if (selCol < 0) {
            selCol = 0;
          }
          chordTable.setColumnSelectionInterval(selCol, selCol);
          return;
        } else {
          selCol = newCol;
        }

        if (!e.getValueIsAdjusting()) {
          invalidCol = -1;
        }
      } else {
        return;
      }

      currTableChord = chordDef;
      updateCurrChord();
    }
  }

  class ChordTableModel extends AbstractTableModel
  {

    ChordTableModel()
    {
    }

    private ChordRegistry.ChordGroupSet getSet()
    {
      ChordRegistry.ChordGroupSet theSet = ChordRegistry.mainRegistry().findChordSet(chordSet);

      assert (theSet != null);
      return theSet;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
      return ChordDef.class;
    }

    @Override
    public String getColumnName(int column)
    {
      return getSet().groupNames[column];
    }

    @Override
    public int getColumnCount()
    {
      return getSet().groupNames.length;
    }

    @Override
    public int getRowCount()
    {
      return getSet().maxChordsInGroup;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
      return getSet().getChordDef(columnIndex, rowIndex);
    }
  }

  void initChordTable()
  {
    TableColumnModel model = chordTable.getColumnModel();
    model.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    ToggleButtonRenderer renderer = new ToggleButtonRenderer();

    chordTable.setDefaultRenderer(ChordDef.class, renderer);

    chordTable.getTableHeader().setReorderingAllowed(true);

    chordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    chordTable.setCellSelectionEnabled(true);
    chordTable.setShowGrid(false);

    if (!addedBassCheck.isSelected()) {
      notePicker2.setVisible(false);
    }

    ChordTableSelectListener tablelist = new ChordTableSelectListener();
    chordTable.getSelectionModel().addListSelectionListener(tablelist);
    chordTable.getColumnModel().getSelectionModel().addListSelectionListener(tablelist);

    if ((chordTable.getRowCount() > 0) && (chordTable.getColumnCount() > 0)) {
      chordTable.setRowSelectionInterval(0, 0);
      chordTable.setColumnSelectionInterval(0, 0);
    }
  }

  class ToggleButtonRenderer extends javax.swing.table.DefaultTableCellRenderer
  {

    Font plain;
    Font bold;
    JToggleButton button = new JToggleButton();

    public ToggleButtonRenderer()
    {
      this.setText("");
      plain = chordTable.getFont().deriveFont(Font.PLAIN);
      bold = plain.deriveFont(Font.BOLD);
      button.setFont(plain);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row, int column)
    {
      if (value == null) {
        return this;
      } else {
        // Chord Cell Rendering
        ChordDef chordDef = (ChordDef) value;

        String cellText = rootNote.toString(true) + chordDef.abbrevHtml;

        if (addedBassCheck.isSelected()) {
          cellText += "/" + addedBassNote.toString(true);
        }

        button.setText("<html>" + cellText + "</html>");

        String statusInfo = "<html><b>" + rootNote.toString(true) + " " + chordDef.name + "</b><br/>" + chordDef.chord.getTransposedString(rootNote) + "</html>";

        button.setToolTipText(statusInfo);

        button.setSelected(isSelected);
        button.setFont(isSelected ? bold : plain);
        button.setVisible(true);
      }

      return button;
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

    jScrollPane1 = new javax.swing.JScrollPane();
    chordTable = new javax.swing.JTable();
    notePicker1 = new render.NotePicker();
    addedBassCheck = new javax.swing.JCheckBox();
    notePicker2 = new render.NotePicker();
    statusLabel = new javax.swing.JLabel();
    showAdvanced = new javax.swing.JCheckBox();

    setBackground(java.awt.SystemColor.inactiveCaptionBorder);

    jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));

    chordTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {
        {null, null, null, null},
        {null, null, null, null},
        {null, null, null, null},
        {null, null, null, null}
      },
      new String [] {
        "Title 1", "Title 2", "Title 3", "Title 4"
      }
    ));
    chordTable.setFillsViewportHeight(true);
    chordTable.setRowHeight(30);
    jScrollPane1.setViewportView(chordTable);

    notePicker1.setBorder(javax.swing.BorderFactory.createTitledBorder("Chord Root:"));
    notePicker1.setOpaque(false);

    addedBassCheck.setText("Additional Bass:");
    addedBassCheck.setOpaque(false);

    notePicker2.setOpaque(false);

    statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getSize()+3f));
    statusLabel.setText("Label");
    statusLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
    statusLabel.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected Chord:"));

    showAdvanced.setText("Advanced Chords");
    showAdvanced.setOpaque(false);
    showAdvanced.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        showAdvancedItemStateChanged(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(notePicker1, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(addedBassCheck)
              .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(showAdvanced)))
          .addComponent(notePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(notePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(addedBassCheck))
          .addGroup(layout.createSequentialGroup()
            .addComponent(showAdvanced)
            .addGap(26, 26, 26)
            .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
        .addComponent(notePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void showAdvancedItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_showAdvancedItemStateChanged
  {//GEN-HEADEREND:event_showAdvancedItemStateChanged
    boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
    this.changeChordSet(!selected);
  }//GEN-LAST:event_showAdvancedItemStateChanged
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox addedBassCheck;
  private javax.swing.JTable chordTable;
  private javax.swing.JScrollPane jScrollPane1;
  private render.NotePicker notePicker1;
  private render.NotePicker notePicker2;
  private javax.swing.JCheckBox showAdvanced;
  private javax.swing.JLabel statusLabel;
  // End of variables declaration//GEN-END:variables
}
