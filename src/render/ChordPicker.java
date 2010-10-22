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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.PanelUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import music.ChordDef;
import music.ChordParser;
import music.ChordRegistry;
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
  SeqColumnModel seqColumnModel = null;
//  JTable chordTable;
//  NotePicker notePicker1;
//  NotePicker notePicker2;
//  JCheckBox addedBassCheck;
  String chordSet = ChordRegistry.ALL_CHORDS;
  int isUpdatingChord = 0;
  final static int DEFAULT_TABLE_COL_WIDTH = 96;

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
      notePicker2.setVisible(addedBassCheck.isSelected());

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
      seqColumnModel.editSelectedColumn(finalChord);
    }

    isUpdatingChord--;
  }

  ChordDef buildChord()
  {
    boolean usingAddedBass = addedBassCheck.isSelected();
    Note addedBass = (usingAddedBass ? addedBassNote : null);
    return ChordParser.buildChord(rootNote, addedBass, currTableChord, false);
  }

  @Override
  public void setVisible(boolean visible)
  {
    if (visible && (seqColumnModel != null)) {
      int index = seqColumnModel.getSelectedColumn();
      if (index >= 0) {
        setupChord(this.seqColumnModel.getChordDef(index));
      }
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

    if (simpleMode) {
      chordTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    } else {

      chordTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      for (int i = 0; i < chordTable.getColumnCount(); i++) {
        chordTable.getColumnModel().getColumn(i).setPreferredWidth(DEFAULT_TABLE_COL_WIDTH);
      }
    }

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
    TableColumnModel colModel = chordTable.getColumnModel();
    colModel.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    ToggleButtonRenderer renderer = new ToggleButtonRenderer();

    chordTable.setDefaultRenderer(ChordDef.class, renderer);

    chordTable.getTableHeader().setReorderingAllowed(true);

    chordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    chordTable.setCellSelectionEnabled(true);
    chordTable.setShowGrid(false);

    ChordTableSelectListener tablelist = new ChordTableSelectListener();
    chordTable.getSelectionModel().addListSelectionListener(tablelist);
    colModel.getSelectionModel().addListSelectionListener(tablelist);

    if ((chordTable.getRowCount() > 0) && (chordTable.getColumnCount() > 0)) {
      chordTable.setRowSelectionInterval(0, 0);
      chordTable.setColumnSelectionInterval(0, 0);
    }
  }

  class ToggleButtonRenderer extends JPanel implements TableCellRenderer
  {

    Font plain;
    Font bold;
    JToggleButton button = null;
    DefaultTableCellRenderer emptyLabel = null;

    public ToggleButtonRenderer()
    {
      button = new JToggleButton();
      emptyLabel = new DefaultTableCellRenderer();

      this.setLayout(new BorderLayout());
      this.add(button, BorderLayout.CENTER);

      initUI();
    }

    private void initUI()
    {
      if (button == null) {
        return;
      }

      plain = getFont().deriveFont(Font.PLAIN);
      bold = plain.deriveFont(Font.BOLD);
      button.setFont(plain);
    }

    @Override
    public void setUI(PanelUI ui)
    {
      super.setUI(ui);
      initUI();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row, int column)
    {
      if (value == null) {
        return emptyLabel;
      }

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

      return this;
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
    jLabel1 = new javax.swing.JLabel();

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
    chordTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    chordTable.setFillsViewportHeight(true);
    chordTable.setRowHeight(30);
    jScrollPane1.setViewportView(chordTable);

    notePicker1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
    notePicker1.setOpaque(false);

    addedBassCheck.setText("Additional Bass:");
    addedBassCheck.setOpaque(false);

    notePicker2.setOpaque(false);

    statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getSize()+3f));
    statusLabel.setText("Label");
    statusLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
    statusLabel.setBorder(javax.swing.BorderFactory.createTitledBorder("Selected Chord:"));

    showAdvanced.setText("<html>Show<br/> Advanced Chords</html>");
    showAdvanced.setOpaque(false);
    showAdvanced.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        showAdvancedItemStateChanged(evt);
      }
    });

    jLabel1.setText("Root Note:");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel1)
              .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(showAdvanced, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)))
              .addComponent(addedBassCheck))
            .addContainerGap())
          .addGroup(layout.createSequentialGroup()
            .addComponent(notePicker1, javax.swing.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
            .addGap(48, 48, 48))
          .addGroup(layout.createSequentialGroup()
            .addComponent(notePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(41, Short.MAX_VALUE))))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(jLabel1)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(notePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addGroup(layout.createSequentialGroup()
            .addComponent(showAdvanced, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(addedBassCheck)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(notePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(22, 22, 22))
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
  private javax.swing.JLabel jLabel1;
  private javax.swing.JScrollPane jScrollPane1;
  private render.NotePicker notePicker1;
  private render.NotePicker notePicker2;
  private javax.swing.JCheckBox showAdvanced;
  private javax.swing.JLabel statusLabel;
  // End of variables declaration//GEN-END:variables
}
