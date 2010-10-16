/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
import music.Note;
import util.Main.StringParser;

/**
 *
 * @author Ilya
 */
public class ChordPickerController
{

  Note rootNote = new Note();
  ChordDef currTableChord;
  Note addedBassNote = new Note();
  JTable chordTable;
  NotePicker notePicker1;
  NotePicker notePicker2;
  JCheckBox addedBassCheck;
  PropertyChangeSupport propDelegates;
  String chordSet = ChordRegistry.ALL_CHORDS;

  public ChordPickerController(JTable pickerTable, NotePicker rootPicker,
          NotePicker addedBassPicker,
          JCheckBox adbCheck)
  {
    propDelegates = new PropertyChangeSupport(this);

    chordTable = pickerTable;
    chordTable.setModel(new ChordTableModel());
    notePicker1 = rootPicker;
    notePicker2 = addedBassPicker;
    addedBassCheck = adbCheck;

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

  class ChordUpdateInfo
  {

    Note rootNote;
    Note addedBass;
    Chord fullChord;
    ChordDef tableChordDef;
  }

  void updateCurrChord()
  {
    ChordUpdateInfo updateInfo = new ChordUpdateInfo();

    //TODO implement
    boolean addedBassLowest = false;

    updateInfo.rootNote = this.rootNote;
    updateInfo.tableChordDef = this.currTableChord;
    updateInfo.addedBass = (addedBassCheck.isSelected() ? addedBassNote : null);
    updateInfo.fullChord = new Chord(updateInfo.tableChordDef.chord,
            updateInfo.rootNote,
            updateInfo.addedBass, addedBassLowest);

    this.propDelegates.firePropertyChange("updateCurrChord", null, updateInfo);
  }

  void setupChord(String chordStr)
  {
    StringParser parser = new StringParser(chordStr);
    Note newRoot = Note.fromString(parser);

    if (newRoot == null) {
      return;
    }

    notePicker1.setNote(newRoot);

    ChordRegistry.ChordFindResult result =
            ChordRegistry.mainRegistry.findChord(ChordRegistry.ALL_CHORDS, parser);

    int row = result.y;
    int col = chordTable.convertColumnIndexToView(result.x);

    chordTable.setRowSelectionInterval(row, row);
    chordTable.setColumnSelectionInterval(col, col);

    Note newAddedBass = null;

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

    this.addedBassCheck.setVisible(!simpleMode);

    this.chordTable.setColumnSelectionInterval(0, 0);

    if (simpleMode && addedBassCheck.isSelected()) {
      this.addedBassCheck.doClick();
    }

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
      ChordRegistry.ChordGroupSet theSet = ChordRegistry.mainRegistry.findChordSet(chordSet);

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

//        if (usingAddedBass) {
//          cellText += "/" + addedBassNote.toString(true);
//        }

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
}
