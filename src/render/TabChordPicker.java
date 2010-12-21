/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TabChordPicker3.java
 *
 * Created on Oct 18, 2010, 4:17:22 PM
 */
package render;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ListSelectionEvent;
import music.Chord;
import music.RegistryChordDef;
import music.ChordRegistry;
import music.Note;
import music.ParsedChordDef;
import music.RelChord;

/**
 *
 * @author Ilya
 */
public class TabChordPicker extends javax.swing.JPanel
{

  /** Creates new form TabChordPicker3 */
  public TabChordPicker()
  {
    initComponents();
  }
  Note rootNote = new Note();
  RelChord currRelChord = new RelChord();
  //RegistryChordDef currTableChord;
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

    chordPicker1.addPropertyChangeListener("Chord", new PropertyChangeListener()
    {

      @Override
      public void propertyChange(PropertyChangeEvent evt)
      {
        currRelChord = (RelChord) evt.getNewValue();
        changeInversion();
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

        notePickerAdd.setVisible(usingAddedBass);
        slashLabel.setVisible(usingAddedBass);
        mustBeLowestCheck.setVisible(usingAddedBass);


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
      seqColumnModel.editSelectedColumn(finalChord);
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

    if (currRelChord.origDef != null) {
      chordLabel.setText("<html>" + currRelChord.origDef.name + "</html>");
    } else {
      chordLabel.setText("<html>Custom</html>");
    }
  }

  ParsedChordDef getPickedChord()
  {
    boolean usingAddedBass = addedBassCheck.isSelected();
    Note addedBass = (usingAddedBass ? addedBassNote : null);
    return new ParsedChordDef(rootNote, addedBass, currRelChord, getLowestBool());
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

    isUpdatingChord++;

    notePickerRoot.setNote(newRoot);

    if (possChordDef.relChord != null) {
      chordPicker1.setRelChord(possChordDef.relChord);
      currRelChord = possChordDef.relChord;
    } else {
      currRelChord = new RelChord();
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

  void populateInversionCombo(
          int selectIndex)
  {
    int chordLen = currRelChord.getChordLength();

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

    Chord simpleChord = currRelChord.buildChord(rootNote);

    int index = simpleChord.findNotePos(addedBassNote);
    // -1 = Any, 0-3 = root, 1st, etc... inversions so just add 1
    if (index >= inversionCombo.getItemCount()) {
      index = -1;
    }
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

    if (index > currRelChord.getChordLength()) {
      index = 1;
    }

    Chord simpleChord = currRelChord.buildChord(rootNote);
    Note inverseRoot = simpleChord.getNoteAt(index - 1);

    if (inverseRoot != null) {
      this.notePickerAdd.setNote(inverseRoot);
      this.addedBassCheck.setSelected(true);
      this.mustBeLowestCheck.setSelected(true);
      mustBeLowestCheck.setEnabled(false);
    }

    this.isUpdatingChord--;
  }

//  private class ChordTableSelectListener implements ListSelectionListener
//  {
//
//    int selRow = 0;
//    int selCol = -1;
//    int invalidRow = -1, invalidCol = -1;
//
//    private RegistryChordDef getChordAt(int row, int col)
//    {
//      col = chordTable.convertColumnIndexToModel(col);
//      return (RegistryChordDef) chordTable.getModel().getValueAt(row, col);
//    }
//
//    @Override
//    public void valueChanged(ListSelectionEvent e)
//    {
//      ListSelectionModel model = (ListSelectionModel) e.getSource();
//      RegistryChordDef chordDef;
//
//      if (model == chordTable.getSelectionModel()) {
//        int newRow = model.getAnchorSelectionIndex();
//
//        if ((newRow < 0) || (selCol < 0)) {
//          selRow = newRow;
//          return;
//        }
//
//        if (newRow == selRow) {
//          return;
//        }
//
//        if ((invalidCol >= 0)
//                && (getChordAt(newRow, invalidCol) != null)) {
//          int newCol = invalidCol;
//          invalidCol = -1;
//          selRow = newRow;
//          chordTable.setColumnSelectionInterval(newCol, newCol);
//          return;
//        }
//
//        chordDef = getChordAt(newRow, selCol);
//
//        if (chordDef == null) {
//          invalidRow = newRow;
//          chordTable.setRowSelectionInterval(selRow, selRow);
//          return;
//        } else {
//          selRow = newRow;
//        }
//
//        if (!e.getValueIsAdjusting()) {
//          invalidRow = -1;
//        }
//
//      } else if (model == chordTable.getColumnModel().getSelectionModel()) {
//        int newCol = model.getAnchorSelectionIndex();
//
//        if ((newCol < 0) || (selRow < 0)) {
//          selCol = newCol;
//          return;
//        }
//
//        if (newCol == selCol) {
//          return;
//        }
//
//        chordDef = getChordAt(selRow, newCol);
//
//        if (chordDef == null) {
//          invalidCol = newCol;
//          if (selCol < 0) {
//            selCol = 0;
//          }
//          chordTable.setColumnSelectionInterval(selCol, selCol);
//          return;
//        } else {
//          selCol = newCol;
//        }
//
//        if (!e.getValueIsAdjusting()) {
//          invalidCol = -1;
//        }
//      } else {
//        return;
//      }
//
//      currTableChord = chordDef;
//      changeInversion();
//      updateChordInModel();
//    }
//  }
//  class ChordTableModel extends AbstractTableModel
//  {
//
//    ChordTableModel()
//    {
//    }
//
//    private ChordRegistry.ChordGroupSet getSet()
//    {
//      ChordRegistry.ChordGroupSet theSet = ChordRegistry.mainRegistry().findChordSet(chordSet);
//
//      assert (theSet != null);
//      return theSet;
//    }
//
//    @Override
//    public Class<?> getColumnClass(int columnIndex)
//    {
//      return RegistryChordDef.class;
//    }
//
//    @Override
//    public String getColumnName(int column)
//    {
//      return getSet().groupNames[column];
//    }
//
//    @Override
//    public int getColumnCount()
//    {
//      return getSet().groupNames.length;
//    }
//
//    @Override
//    public int getRowCount()
//    {
//      return getSet().maxChordsInGroup;
//    }
//
//    @Override
//    public Object getValueAt(int rowIndex, int columnIndex)
//    {
//      return getSet().getChordDef(columnIndex, rowIndex);
//    }
//  }
//  DefaultTableCellRenderer emptyLabel = new DefaultTableCellRenderer();
//  void initChordTable()
//  {
//    TableColumnModel colModel = chordTable.getColumnModel();
//    colModel.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//
//    //TableCellRenderer renderer = new ToggleButtonRenderer();
//    TableCellRenderer renderer = new ChordCellRenderer();
//    chordTable.setDefaultRenderer(RegistryChordDef.class, renderer);
//
//    JTableHeader header = chordTable.getTableHeader();
//    header.setReorderingAllowed(true);
//    header.setFont(header.getFont().deriveFont(Font.ITALIC | Font.BOLD, 14));
//
//    chordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//    chordTable.setCellSelectionEnabled(true);
//    chordTable.setShowGrid(true);
//
//    //chordTable.setBackground(emptyLabel.getBackground());
//    //jScrollPane1.setBackground(emptyLabel.getBackground());
//
//    ChordTableSelectListener tablelist = new ChordTableSelectListener();
//    chordTable.getSelectionModel().addListSelectionListener(tablelist);
//    colModel.getSelectionModel().addListSelectionListener(tablelist);
//
//    if ((chordTable.getRowCount() > 0) && (chordTable.getColumnCount() > 0)) {
//      chordTable.setRowSelectionInterval(0, 0);
//      chordTable.setColumnSelectionInterval(0, 0);
//    }
//  }
  private String getCellText(RegistryChordDef chordDef)
  {
    String cellText = chordDef.name;

    if (cellText.length() == 0) {
      cellText = "(note)";
    }

//      if (addedBassCheck.isSelected()) {
//        cellText += "/" + addedBassNote.toString(true);
//      }

    return cellText;//("<html>" + cellText + "</html>");
  }

//  class ChordCellRenderer extends DefaultTableCellRenderer
//  {
//
//    Font plain, bold;
//
//    ChordCellRenderer()
//    {
//      initUI();
//    }
//
//    private void initUI()
//    {
//      plain = getFont().deriveFont(Font.PLAIN, 18);
//      bold = plain.deriveFont(Font.BOLD);
//      this.setHorizontalAlignment(CENTER);
//    }
//
//    @Override
//    public Component getTableCellRendererComponent(JTable table,
//            Object value,
//            boolean isSelected,
//            boolean hasFocus,
//            int row, int column)
//    {
//      if (value == null) {
//        return emptyLabel;
//      }
//
//      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//
//      // Chord Cell Rendering
//      RegistryChordDef chordDef = (RegistryChordDef) value;
//
//      setText(getCellText(chordDef));
//
//      String statusInfo =
//              "<html><b>" + rootNote.toString(true) + " " + chordDef.name + "</b><br/>" + chordDef.getTransposedString(rootNote) + "</html>";
//
//      this.setToolTipText(statusInfo);
//
//      //setSelected(isSelected);
//      setFont(isSelected ? bold : plain);
//      //button.setVisible(true);
//      //button.setEnabled(table.isEnabled());
//
//      return this;
//    }
//  }
//
//  class ToggleButtonRenderer extends JPanel implements TableCellRenderer
//  {
//
//    Font plain;
//    Font bold;
//    JToggleButton button = null;
//
//    public ToggleButtonRenderer()
//    {
//      button = new JToggleButton();
//
//      this.setLayout(new BorderLayout());
//      this.add(button, BorderLayout.CENTER);
//
//      initUI();
//    }
//
//    private void initUI()
//    {
//      if (button == null) {
//        return;
//      }
//
//      plain = getFont().deriveFont(Font.PLAIN, 18);
//      bold = plain.deriveFont(Font.BOLD);
//      button.setFont(plain);
//    }
//
//    @Override
//    public void setUI(PanelUI ui)
//    {
//      super.setUI(ui);
//      initUI();
//    }
//
//    @Override
//    public Component getTableCellRendererComponent(JTable table,
//            Object value,
//            boolean isSelected,
//            boolean hasFocus,
//            int row, int column)
//    {
//      if (value == null) {
//        return emptyLabel;
//      }
//
//      // Chord Cell Rendering
//      RegistryChordDef chordDef = (RegistryChordDef) value;
//
//      button.setText(getCellText(chordDef));
//
//      String statusInfo = "<html><b>" + rootNote.toString(true) + " " + chordDef.name + "</b><br/>" + chordDef.getTransposedString(rootNote) + "</html>";
//
//      this.setToolTipText(statusInfo);
//
//      button.setSelected(isSelected);
//      button.setFont(isSelected ? bold : plain);
//      button.setVisible(true);
//      button.setEnabled(table.isEnabled());
//
//      return this;
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

    statusLabel = new javax.swing.JLabel();
    hugeChordLabel = new javax.swing.JLabel();
    jPanel1 = new javax.swing.JPanel();
    notePickerRoot = new render.NotePicker();
    jLabel1 = new javax.swing.JLabel();
    slashLabel = new javax.swing.JLabel();
    chordLabel = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    inversionCombo = new javax.swing.JComboBox();
    mustBeLowestCheck = new javax.swing.JCheckBox();
    addedBassCheck = new javax.swing.JCheckBox();
    notePickerAdd = new render.NotePicker();
    jLabel3 = new javax.swing.JLabel();
    chordPicker1 = new render.ChordPicker();

    statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getSize()+3f));
    statusLabel.setText("Label");
    statusLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
    statusLabel.setBorder(javax.swing.BorderFactory.createTitledBorder("Chord Details:"));

    hugeChordLabel.setFont(hugeChordLabel.getFont().deriveFont(hugeChordLabel.getFont().getStyle() | java.awt.Font.BOLD, hugeChordLabel.getFont().getSize()+15));
    hugeChordLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    hugeChordLabel.setText("jLabel3");

    jPanel1.setOpaque(false);

    jLabel1.setText("Root Note:");

    slashLabel.setFont(new java.awt.Font("Tahoma", 1, 24));
    slashLabel.setText("/");
    slashLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

    chordLabel.setFont(new java.awt.Font("Tahoma", 1, 12));
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
        .addComponent(jLabel1)
        .addGap(46, 46, 46)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(chordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel3))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(slashLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(notePickerAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(20, 20, 20))
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(addedBassCheck)
            .addGap(18, 18, 18)))
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel2)
          .addComponent(inversionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(mustBeLowestCheck, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
        .addContainerGap())
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addComponent(notePickerRoot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(360, 360, 360))
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(jLabel2)
              .addComponent(addedBassCheck))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(inversionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mustBeLowestCheck))
              .addComponent(notePickerAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
          .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel3)
              .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(chordLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(slashLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE))
              .addComponent(notePickerRoot, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        .addContainerGap())
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
          .addComponent(hugeChordLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(chordPicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(layout.createSequentialGroup()
            .addGap(35, 35, 35)
            .addComponent(hugeChordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
            .addContainerGap())
          .addComponent(chordPicker1, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)))
    );
  }// </editor-fold>//GEN-END:initComponents

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
  private render.ChordPicker chordPicker1;
  private javax.swing.JLabel hugeChordLabel;
  private javax.swing.JComboBox inversionCombo;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JCheckBox mustBeLowestCheck;
  private render.NotePicker notePickerAdd;
  private render.NotePicker notePickerRoot;
  private javax.swing.JLabel slashLabel;
  private javax.swing.JLabel statusLabel;
  // End of variables declaration//GEN-END:variables
}
