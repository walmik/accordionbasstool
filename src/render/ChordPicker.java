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
import music.Chord;
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
  String chordSet = ChordRegistry.ALL_CHORDS;
  int isUpdatingChord = 0;
  final static int DEFAULT_TABLE_COL_WIDTH = 96;
  int lastColSel = -1;

  void setSeqColModel(SeqColumnModel model)
  {
    seqColumnModel = model;

    //Column Selection Change
    seqColumnModel.getSelectionModel().addListSelectionListener(
            new javax.swing.event.ListSelectionListener()
            {

              @Override
              public void valueChanged(ListSelectionEvent e)
              {
                if (!isVisible()) {
                  return;
                }

                int index = seqColumnModel.getSelectedColumn();
                if (index == lastColSel) {
                  return;
                }

                lastColSel = index;

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
        changeInversion();
        updateChordInModel();
      }
    });

    notePickerAdd.addPropertyChangeListener("Note", new PropertyChangeListener()
    {

      @Override
      public void propertyChange(PropertyChangeEvent evt)
      {
        addedBassNote = (Note) evt.getNewValue();
        syncInversionCombo();
        updateChordInModel();
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

        if (showAdvanced.isSelected()) {
          notePickerAdd.setVisible(usingAddedBass);
          slashLabel.setVisible(usingAddedBass);
          mustBeLowestCheck.setVisible(usingAddedBass);
        }

        if (!usingAddedBass) {
          if (isUpdatingChord == 0) {
            inversionCombo.setSelectedIndex(0);
            mustBeLowestCheck.setEnabled(true);
          }
        }

        updateChordInModel();
      }
    });
  }

  private void updateChordInModel()
  {
    if (isUpdatingChord > 0) {
      return;
    }

    isUpdatingChord++;

    ParsedChordDef finalChord = getPickedChord();

    if (seqColumnModel != null) {
      seqColumnModel.editSelectedColumn(finalChord, false);
    }

    isUpdatingChord--;

    updateChordUI(finalChord);
  }

  private void updateChordUI(ParsedChordDef finalChord)
  {
    String notestr = finalChord.chord.toHtmlString();

    String info = "<html>"//<b>" + finalChord.nameHtml + "</b>" + "<br/>" +
            + finalChord.detail + ": "
            + "(" + notestr + ")" + "</html>";

    hugeChordLabel.setText("<html>" + finalChord.nameHtml + "</html>");

    statusLabel.setText(info);

    chordLabel.setText("<html>" + currTableChord.name + "</html>");

    chordTable.repaint();

  }

  ParsedChordDef getPickedChord()
  {
    boolean usingAddedBass = addedBassCheck.isSelected();
    Note addedBass = (usingAddedBass ? addedBassNote : null);
    return new ParsedChordDef(rootNote, addedBass, currTableChord, getLowestBool());
  }

  private boolean getLowestBool()
  {
    // If an inversion is set, then this value is irrelevant as the bass note
    // is already implied. Returning false to avoid duplicating the note an extra time.
    if (inversionCombo.getSelectedIndex() != 0) {
      return false;
    }

    return mustBeLowestCheck.isSelected();
  }

  @Override
  public void setVisible(boolean visible)
  {
    if (visible && (seqColumnModel != null)) {
      int index = seqColumnModel.getSelectedColumn();
      if ((index >= 0) && (index < seqColumnModel.getColumnCount())) {
        setupChord(this.seqColumnModel.getChordDef(index));
        lastColSel = -1;
      }
    }
    super.setVisible(visible);
  }

  private void setupChord(ParsedChordDef possChordDef)
  {
    if (isUpdatingChord > 0) {
      return;
    }

    Note newRoot = possChordDef.rootNote;

    if (newRoot == null) {
      return;
    }

    if ((chordTable.getRowCount() == 0) || (chordTable.getColumnCount() == 0)) {
      return;
    }

    isUpdatingChord++;

    notePickerRoot.setNote(newRoot);

    int row = 0, col = 0;

    row = possChordDef.regRow;
    col = possChordDef.regCol;

    if ((col < chordTable.getColumnCount()) && (row < chordTable.getRowCount())) {
      chordTable.setColumnSelectionInterval(col, col);
      chordTable.setRowSelectionInterval(row, row);
      chordTable.scrollRectToVisible(chordTable.getCellRect(row, col, true));
    }

    Note newAddedBass = possChordDef.addedBassNote;

    if (newAddedBass != null) {
      addedBassCheck.setSelected(true);
      notePickerAdd.setNote(newAddedBass);
    } else {
      addedBassCheck.setSelected(false);
    }

    this.mustBeLowestCheck.setSelected(possChordDef.bassLowest);

    isUpdatingChord--;

    updateChordUI(possChordDef);
  }

  void changeChordSet(boolean simpleMode)
  {
    this.chordSet = (simpleMode ? ChordRegistry.SIMPLE_CHORDS : ChordRegistry.ALL_CHORDS);

    //Toggle Visibility
    this.addedBassCheck.setVisible(!simpleMode);
    this.mustBeLowestCheck.setVisible(!simpleMode);
    if (simpleMode) {

      this.notePickerAdd.setVisible(false);
      this.slashLabel.setVisible(false);

      // If not on any inversion, then we must turn off added bass note
      int index = this.inversionCombo.getSelectedIndex();
      if (index == 0) {
        this.addedBassCheck.setSelected(false);
      }

    } else {
      boolean usingAddedBass = addedBassCheck.isSelected();
      notePickerAdd.setVisible(usingAddedBass);
      slashLabel.setVisible(usingAddedBass);
      mustBeLowestCheck.setVisible(usingAddedBass);
    }

//    this.chordTable.setColumnSelectionInterval(0, 0);

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

    int row = 0;
    int col = 0;

    if (currTableChord != null) {
      RegistryChordDef newTableChord = ChordRegistry.mainRegistry().findChord(chordSet, new StringParser(currTableChord.abbrevPlain));

      if (newTableChord != null) {
        row = newTableChord.row;
        col = newTableChord.col;
      }

      chordTable.setRowSelectionInterval(row, row);
      chordTable.setColumnSelectionInterval(col, col);
      chordTable.scrollRectToVisible(chordTable.getCellRect(row, col, true));
    }
  }

  void populateInversionCombo(
          int selectIndex)
  {
    int chordLen = currTableChord.getChordLength();

    inversionCombo.removeAllItems();
    inversionCombo.addItem("Any");
    inversionCombo.addItem("Root");

    if (chordLen >= 2) {
      inversionCombo.addItem("1st");

      if (chordLen >= 3) {
        inversionCombo.addItem("2nd");

        if (chordLen >= 4) {
          inversionCombo.addItem("3rd");
        }
      }
    }

    if (selectIndex < inversionCombo.getItemCount()) {
      inversionCombo.setSelectedIndex(selectIndex);
    }
  }

  void syncInversionCombo()
  {
    isUpdatingChord++;

    Chord simpleChord = currTableChord.getSimpleChordAt(rootNote);

    int index = simpleChord.findNotePos(addedBassNote);
    // -1 = Any, 0-3 = root, 1st, etc... inversions so just add 1
    inversionCombo.setSelectedIndex(index + 1);
    this.mustBeLowestCheck.setEnabled(index + 1 == 0);

    isUpdatingChord--;
  }

  void changeInversion()
  {
    if (isUpdatingChord > 0) {
      return;
    }

    int index = inversionCombo.getSelectedIndex();

    mustBeLowestCheck.setEnabled(true);

    // Index 0 is any, so removing added bass option
    if (index == 0) {
      if (!addedBassCheck.isVisible()) {
        addedBassCheck.setSelected(false);
      }
      return;
    }
    //Skipping the any option and the option
    //Root = 1, 1st = 2, 2nd = 3, 3rd = 4
    if ((index > 4)) {
      return;
    }

    this.isUpdatingChord++;

    if (index > currTableChord.getChordLength()) {
      index = 1;
    }

    Chord simpleChord = currTableChord.getSimpleChordAt(rootNote);
    Note inverseRoot = simpleChord.getNoteAt(index - 1);

    if (inverseRoot != null) {
      this.notePickerAdd.setNote(inverseRoot);
      this.addedBassCheck.setSelected(true);
      this.mustBeLowestCheck.setSelected(true);
      mustBeLowestCheck.setEnabled(false);
    }

    this.isUpdatingChord--;
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
      changeInversion();
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
  DefaultTableCellRenderer emptyLabel = new DefaultTableCellRenderer();

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

    //chordTable.setBackground(emptyLabel.getBackground());
    //jScrollPane1.setBackground(emptyLabel.getBackground());

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

    public ToggleButtonRenderer()
    {
      button = new JToggleButton();

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

//      if (addedBassCheck.isSelected()) {
//        cellText += "/" + addedBassNote.toString(true);
//      }

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
    statusLabel = new javax.swing.JLabel();
    showAdvanced = new javax.swing.JCheckBox();
    hugeChordLabel = new javax.swing.JLabel();
    jPanel1 = new javax.swing.JPanel();
    notePickerRoot = new render.NotePickerAlt();
    jLabel1 = new javax.swing.JLabel();
    slashLabel = new javax.swing.JLabel();
    chordLabel = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    inversionCombo = new javax.swing.JComboBox();
    mustBeLowestCheck = new javax.swing.JCheckBox();
    addedBassCheck = new javax.swing.JCheckBox();
    notePickerAdd = new render.NotePickerAlt();
    jLabel3 = new javax.swing.JLabel();

    setBackground(java.awt.SystemColor.inactiveCaptionBorder);

    jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));

    chordTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    chordTable.setFillsViewportHeight(true);
    chordTable.setRowHeight(30);
    jScrollPane1.setViewportView(chordTable);

    statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getSize()+3f));
    statusLabel.setText("Label");
    statusLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
    statusLabel.setBorder(javax.swing.BorderFactory.createTitledBorder("Chord Details:"));

    showAdvanced.setText("Advanced Mode");
    showAdvanced.setOpaque(false);
    showAdvanced.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        showAdvancedItemStateChanged(evt);
      }
    });

    hugeChordLabel.setFont(hugeChordLabel.getFont().deriveFont(hugeChordLabel.getFont().getStyle() | java.awt.Font.BOLD, hugeChordLabel.getFont().getSize()+15));
    hugeChordLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    hugeChordLabel.setText("jLabel3");

    jPanel1.setOpaque(false);

    jLabel1.setText("Root Note:");

    slashLabel.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
    slashLabel.setText("/");
    slashLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

    chordLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    chordLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    chordLabel.setText("MMM");
    chordLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

    jLabel2.setLabelFor(inversionCombo);
    jLabel2.setText("Inversion:");

    inversionCombo.setFont(inversionCombo.getFont().deriveFont(inversionCombo.getFont().getStyle() | java.awt.Font.BOLD, inversionCombo.getFont().getSize()+3));
    inversionCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Any", "Root", "1st", "2nd", "3rd" }));
    inversionCombo.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        inversionComboItemStateChanged(evt);
      }
    });

    mustBeLowestCheck.setSelected(true);
    mustBeLowestCheck.setText("Must be in Bass");
    mustBeLowestCheck.setActionCommand("Must be Bass Note");
    mustBeLowestCheck.setOpaque(false);
    mustBeLowestCheck.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        mustBeLowestCheckItemStateChanged(evt);
      }
    });

    addedBassCheck.setText("Additional Note");
    addedBassCheck.setOpaque(false);

    jLabel3.setText("Chord:");

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(notePickerRoot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel1))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(chordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel3))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(addedBassCheck)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(slashLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(notePickerAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addGap(18, 18, 18)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel2)
          .addComponent(mustBeLowestCheck, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
          .addComponent(inversionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap())
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel3)
          .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
            .addComponent(notePickerAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(chordLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(notePickerRoot, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addComponent(addedBassCheck)
        .addGap(55, 55, 55))
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addGap(38, 38, 38)
        .addComponent(slashLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addComponent(jLabel2)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(inversionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
        .addComponent(mustBeLowestCheck))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(statusLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
              .addComponent(hugeChordLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
              .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
          .addGroup(layout.createSequentialGroup()
            .addGap(10, 10, 10)
            .addComponent(showAdvanced)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(layout.createSequentialGroup()
            .addGap(16, 16, 16)
            .addComponent(showAdvanced)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(hugeChordLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
          .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))
        .addContainerGap())
    );
  }// </editor-fold>//GEN-END:initComponents

  private void showAdvancedItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_showAdvancedItemStateChanged
  {//GEN-HEADEREND:event_showAdvancedItemStateChanged
    boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
    this.changeChordSet(!selected);
  }//GEN-LAST:event_showAdvancedItemStateChanged

  private void mustBeLowestCheckItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_mustBeLowestCheckItemStateChanged
  {//GEN-HEADEREND:event_mustBeLowestCheckItemStateChanged
    this.updateChordInModel();
  }//GEN-LAST:event_mustBeLowestCheckItemStateChanged

  private void inversionComboItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_inversionComboItemStateChanged
  {//GEN-HEADEREND:event_inversionComboItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
      this.changeInversion();
      this.updateChordInModel();
    }
  }//GEN-LAST:event_inversionComboItemStateChanged
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox addedBassCheck;
  private javax.swing.JLabel chordLabel;
  private javax.swing.JTable chordTable;
  private javax.swing.JLabel hugeChordLabel;
  private javax.swing.JComboBox inversionCombo;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JCheckBox mustBeLowestCheck;
  private render.NotePickerAlt notePickerAdd;
  private render.NotePickerAlt notePickerRoot;
  private javax.swing.JCheckBox showAdvanced;
  private javax.swing.JLabel slashLabel;
  private javax.swing.JLabel statusLabel;
  // End of variables declaration//GEN-END:variables
}
