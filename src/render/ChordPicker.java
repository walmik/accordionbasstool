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
import music.RegistryChordDef;
import music.ChordRegistry;
import music.Note;
import music.ParsedChordDef;
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
  RegistryChordDef currTableChord;
  Note addedBassNote = new Note();
  SeqColumnModel seqColumnModel = null;
  boolean isTransposeMode = false;

  String chordSet = ChordRegistry.ALL_CHORDS;
  boolean isUpdatingChord = false;
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

                setupChord(seqColumnModel.getChordDef(index));
              }
            });

  }

  public void init()
  {
    chordTable.setModel(new ChordTableModel());

    initChordTable();

    notePickerRoot.addPropertyChangeListener("Note", new PropertyChangeListener()
    {

      @Override
      public void propertyChange(PropertyChangeEvent evt)
      {
        rootNote = (Note) evt.getNewValue();
        updateChordInModel();
        chordTable.repaint();
      }
    });

    notePickerAdd.addPropertyChangeListener("Note", new PropertyChangeListener()
    {

      @Override
      public void propertyChange(PropertyChangeEvent evt)
      {
        addedBassNote = (Note) evt.getNewValue();
        updateChordInModel();
        chordTable.repaint();
      }
    });

    notePickerAdd.setVisible(addedBassCheck.isSelected());
    slashLabel.setVisible(addedBassCheck.isSelected());
    mustBeLowestCheck.setVisible(addedBassCheck.isSelected());

    addedBassCheck.addItemListener(new ItemListener()
    {

      @Override
      public void itemStateChanged(ItemEvent evt)
      {
        boolean usingAddedBass = (evt.getStateChange() == ItemEvent.SELECTED);
        notePickerAdd.setVisible(usingAddedBass);
        slashLabel.setVisible(usingAddedBass);
        mustBeLowestCheck.setVisible(usingAddedBass);
        chordTable.repaint();
        updateChordInModel();
      }
    });
  }
  

  private void updateChordInModel()
  {
    if (isUpdatingChord) {
      return;
    }

    isUpdatingChord = true;

    ParsedChordDef finalChord = getPickedChord();

    if (seqColumnModel != null) {
      seqColumnModel.editSelectedColumn(finalChord, isTransposeMode);
    }

    isUpdatingChord = false;

    updateChordUI(finalChord);
  }

  private void updateChordUI(ParsedChordDef finalChord)
  {
    String notestr = finalChord.chord.toHtmlString();

    String info = "<html><b>" + finalChord.nameHtml + "</b>"
            + "<br/>" + finalChord.detail + ": "
            + "(" + notestr + ")" + "</html>";

    statusLabel.setText(info);

    chordLabel.setText("<html>" + currTableChord.abbrevHtml + "</html>");
  }

  ParsedChordDef getPickedChord()
  {
    boolean usingAddedBass = addedBassCheck.isSelected();
    Note addedBass = (usingAddedBass ? addedBassNote : null);
    return new ParsedChordDef(rootNote, addedBass, currTableChord, mustBeLowestCheck.isSelected());
  }

  @Override
  public void setVisible(boolean visible)
  {
    if (visible && (seqColumnModel != null)) {
      int index = seqColumnModel.getSelectedColumn();
      if ((index >= 0) && (index < seqColumnModel.getColumnCount())) {
        setupChord(this.seqColumnModel.getChordDef(index));
      }
    }
    super.setVisible(visible);
  }

  private void setupChord(ParsedChordDef possChordDef)
  {
    if (isUpdatingChord) {
      return;
    }

//    String chordStr = possChordDef.namePlain;
//    StringParser parser = new StringParser(chordStr);
//    Note newRoot = Note.fromString(parser);

    Note newRoot = possChordDef.rootNote;

    if (newRoot == null) {
      return;
    }

    if ((chordTable.getRowCount() == 0) || (chordTable.getColumnCount() == 0)) {
      return;
    }

    isUpdatingChord = true;

    notePickerRoot.setNote(newRoot.toString());

    int row = 0, col = 0;
//    if (possChordDef.registryDef == null) {
//      RegistryChordDef regChordDef = null;
//
//      regChordDef = ChordRegistry.mainRegistry().findChord(this.chordSet, parser);
//
//      if (regChordDef != null) {
//        row = regChordDef.row;
//        col = chordTable.convertColumnIndexToView(regChordDef.col);
//      }
//      System.out.println("Null Chord Def");
//    } else {
    row = possChordDef.regRow;
    col = possChordDef.regCol;
//    }

    chordTable.setRowSelectionInterval(row, row);
    chordTable.setColumnSelectionInterval(col, col);

    Note newAddedBass = possChordDef.addedBassNote;

//    if (parser.nextChar() == '/') {
//      parser.incOffset(1);
//      newAddedBass = Note.fromString(parser);
//    }

    if (newAddedBass != null) {
      addedBassCheck.setSelected(true);
      notePickerAdd.setNote(newAddedBass.toString());
    } else {
      addedBassCheck.setSelected(false);
    }

    isUpdatingChord = false;

    updateChordUI(possChordDef);
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

    private RegistryChordDef getChordAt(int row, int col)
    {
      col = chordTable.convertColumnIndexToModel(col);
      return (RegistryChordDef) chordTable.getModel().getValueAt(row, col);
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
      ListSelectionModel model = (ListSelectionModel) e.getSource();
      RegistryChordDef chordDef;

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
      updateChordInModel();
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
      return RegistryChordDef.class;
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

    chordTable.setDefaultRenderer(RegistryChordDef.class, renderer);

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
      RegistryChordDef chordDef = (RegistryChordDef) value;

      String cellText = rootNote.toString(true) + chordDef.abbrevHtml;

      if (addedBassCheck.isSelected()) {
        cellText += "/" + addedBassNote.toString(true);
      }

      button.setText("<html>" + cellText + "</html>");

      String statusInfo = "<html><b>" + rootNote.toString(true) + " " + chordDef.name + "</b><br/>" + chordDef.getTransposedString(rootNote) + "</html>";

      this.setToolTipText(statusInfo);

      button.setSelected(isSelected);
      button.setFont(isSelected ? bold : plain);
      button.setVisible(true);
      button.setEnabled(table.isEnabled());

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
    addedBassCheck = new javax.swing.JCheckBox();
    statusLabel = new javax.swing.JLabel();
    showAdvanced = new javax.swing.JCheckBox();
    jLabel1 = new javax.swing.JLabel();
    transCheck = new javax.swing.JCheckBox();
    notePickerRoot = new render.NotePickerAlt();
    notePickerAdd = new render.NotePickerAlt();
    slashLabel = new javax.swing.JLabel();
    chordLabel = new javax.swing.JLabel();
    mustBeLowestCheck = new javax.swing.JCheckBox();

    setBackground(java.awt.SystemColor.inactiveCaptionBorder);

    jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));

    chordTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    chordTable.setFillsViewportHeight(true);
    chordTable.setRowHeight(30);
    jScrollPane1.setViewportView(chordTable);

    addedBassCheck.setText("Additional Note:");
    addedBassCheck.setOpaque(false);

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

    transCheck.setText("Transposing Mode");
    transCheck.setOpaque(false);
    transCheck.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        transCheckItemStateChanged(evt);
      }
    });

    slashLabel.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
    slashLabel.setText("/");

    chordLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
    chordLabel.setText("MMM");

    mustBeLowestCheck.setText("Must Be a Bass");
    mustBeLowestCheck.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        mustBeLowestCheckItemStateChanged(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(transCheck)
          .addComponent(showAdvanced, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel1))
              .addGroup(layout.createSequentialGroup()
                .addComponent(notePickerRoot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(addedBassCheck)
                  .addGroup(layout.createSequentialGroup()
                    .addComponent(slashLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(notePickerAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(mustBeLowestCheck)))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(jLabel1)
              .addComponent(addedBassCheck)
              .addComponent(mustBeLowestCheck))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(notePickerRoot, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(slashLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(chordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
              .addComponent(notePickerAdd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE))
          .addGroup(layout.createSequentialGroup()
            .addComponent(transCheck)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
            .addComponent(showAdvanced, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)))
        .addGap(11, 11, 11))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void showAdvancedItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_showAdvancedItemStateChanged
  {//GEN-HEADEREND:event_showAdvancedItemStateChanged
    boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
    this.changeChordSet(!selected);
  }//GEN-LAST:event_showAdvancedItemStateChanged

  private void transCheckItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_transCheckItemStateChanged
  {//GEN-HEADEREND:event_transCheckItemStateChanged
    isTransposeMode = (evt.getStateChange() == ItemEvent.SELECTED);
    this.chordTable.setEnabled(!isTransposeMode);
    this.addedBassCheck.setEnabled(!isTransposeMode);
    this.notePickerAdd.setEnabled(!isTransposeMode);
    this.showAdvanced.setEnabled(!isTransposeMode);
    this.updateChordInModel();
  }//GEN-LAST:event_transCheckItemStateChanged

  private void mustBeLowestCheckItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_mustBeLowestCheckItemStateChanged
  {//GEN-HEADEREND:event_mustBeLowestCheckItemStateChanged
    this.updateChordInModel();
  }//GEN-LAST:event_mustBeLowestCheckItemStateChanged

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox addedBassCheck;
  private javax.swing.JLabel chordLabel;
  private javax.swing.JTable chordTable;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JCheckBox mustBeLowestCheck;
  private render.NotePickerAlt notePickerAdd;
  private render.NotePickerAlt notePickerRoot;
  private javax.swing.JCheckBox showAdvanced;
  private javax.swing.JLabel slashLabel;
  private javax.swing.JLabel statusLabel;
  private javax.swing.JCheckBox transCheck;
  // End of variables declaration//GEN-END:variables
}
