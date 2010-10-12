/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ChordPicker.java
 *
 * Created on Sep 23, 2010, 5:34:18 PM
 */
package render;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.DocumentBuilderFactory;
import music.Chord;
import music.Note;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Ilya
 */
public class ChordPicker extends javax.swing.JPanel
{

  Note rootNote = new Note();
  Chord currChord;
  ChordDef currChordDef;
  Note addedBassNote = new Note();
  boolean addedBassLowest = false;
  boolean usingAddedBass = false;

  /** Creates new form ChordPicker */
  public ChordPicker()
  {
    initComponents();
    initChordTable();

    ChordTableSelectListener tablelist = new ChordTableSelectListener();
    chordTable.getSelectionModel().addListSelectionListener(tablelist);
    chordTable.getColumnModel().getSelectionModel().addListSelectionListener(tablelist);

    chordTable.setRowSelectionInterval(0, 0);
    chordTable.setColumnSelectionInterval(0, 0);

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
      }
    });
    notePicker2.setVisible(usingAddedBass);
  }

  private void updateCurrChord()
  {
    if (currChordDef == null) {
      return;
    }

    currChord = new Chord(currChordDef.chord, rootNote, (usingAddedBass ? addedBassNote : null), addedBassLowest);

    String info = "<html><b>" + rootNote.toString(true) + currChordDef.abbrev;

    if (usingAddedBass) {
      info += "/" + addedBassNote.toString(true);
    }

    info += "</b><br/>" + rootNote.toString(true) + " " + currChordDef.name;

    if (usingAddedBass) {
      info += " over " + addedBassNote.toString(true);
    }

    info += ": "
            + "(" + currChord.toString("-", true) + ")" + "</html>";

    currChordLabel.setText(info);
  }
  boolean simpleMode = false;

  class ChordTableSelectListener implements ListSelectionListener
  {

    int selRow = 0;
    int selCol = -1;
    int invalidRow = -1, invalidCol = -1;

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
      ListSelectionModel model = (ListSelectionModel) e.getSource();
      ChordDef chordDef;


      if (model == chordTable.getSelectionModel()) {
        int newRow = model.getAnchorSelectionIndex();
        if ((newRow == selRow) || (newRow < 0)) {
          return;
        }

        if ((invalidCol >= 0)
                && (chordTable.getModel().getValueAt(newRow, invalidCol) != null)) {
          int newCol = invalidCol;
          invalidCol = -1;
          selRow = newRow;
          chordTable.setColumnSelectionInterval(newCol, newCol);
          return;

        }

        chordDef = (ChordDef) chordTable.getModel().getValueAt(newRow, selCol);

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
        if ((newCol == selCol) || (newCol < 0)) {
          return;
        }

        chordDef = (ChordDef) chordTable.getModel().getValueAt(selRow, newCol);

        if (chordDef == null) {
          invalidCol = newCol;
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


      currChordDef = chordDef;
      updateCurrChord();
    }
  }

  class ChordDef
  {

    String name;
    String abbrev;
    String notestr;
    music.Chord chord;

    ChordDef(String _name, String _abb, String _notes)
    {
      name = _name;
      abbrev = _abb;
      abbrev = abbrev.replace("[", "<sup>");
      abbrev = abbrev.replace("]", "</sup>");

      notestr = _notes;
      chord = music.ChordParser.parseNoteList(new util.Main.StringParser(notestr));
    }
  }

  class ChordTableModel extends AbstractTableModel
  {

    ChordDef allChords[][];
    String groupNames[];
    int maxRows = 0;

    ChordTableModel()
    {
      loadFromXml("./xml/chorddefs.xml");
    }

    private void loadFromXml(String url)
    {
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc = dbf.newDocumentBuilder().parse(url);
        Element element = doc.getDocumentElement();

        loadChords(element);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    private void loadChords(Element root)
    {
      NodeList groups = root.getElementsByTagName("chordgroup");
      groupNames = new String[groups.getLength()];
      allChords = new ChordDef[groupNames.length][];

      for (int i = 0; i < allChords.length; i++) {
        Element group = (Element) groups.item(i);
        groupNames[i] = group.getAttribute("name");

        NodeList chords = group.getElementsByTagName("chord");

        allChords[i] = new ChordDef[chords.getLength()];
        maxRows = Math.max(maxRows, allChords[i].length);

        for (int j = 0; j < allChords[i].length; j++) {
          Element chord = (Element) chords.item(j);
          String name = chord.getAttribute("name");
          String abbrev = chord.getAttribute("abbrev");
          String notelist = chord.getAttribute("notes");
          allChords[i][j] = new ChordDef(name, abbrev, notelist);
        }
      }
    }

    @Override
    public String getColumnName(int column)
    {
      assert (groupNames != null);
      return groupNames[column];
    }

    @Override
    public int getColumnCount()
    {
      assert (groupNames != null);
      return (simpleMode ? 1 : groupNames.length);
    }

    @Override
    public int getRowCount()
    {
      assert ((allChords != null) && (allChords.length > 0));
      return (simpleMode ? allChords[0].length : maxRows);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
      assert ((rowIndex >= 0) && (columnIndex >= 0));
      if (rowIndex < allChords[columnIndex].length) {
        return allChords[columnIndex][rowIndex];
      } else {
        return null;
      }
    }
  }

  void initChordTable()
  {
    TableColumnModel model = chordTable.getColumnModel();
    model.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    ToggleButtonRenderer renderer = new ToggleButtonRenderer();

    for (int i = 0; i < model.getColumnCount(); i++) {
      model.getColumn(i).setCellRenderer(renderer);
    }

    chordTable.getTableHeader().setReorderingAllowed(false);
  }
  static JLabel emptyLabel = new JLabel();

  class ToggleButtonRenderer extends javax.swing.JToggleButton
          implements javax.swing.table.TableCellRenderer
  {

    Font plain;
    Font bold;

    public ToggleButtonRenderer()
    {
      plain = chordTable.getFont().deriveFont(Font.PLAIN);
      bold = plain.deriveFont(Font.BOLD);
      setFont(plain);
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
      } else {
        // Chord Cell Rendering
        ChordDef chordDef = (ChordDef) value;

        this.setText("<html>" + rootNote.toString(true) + chordDef.abbrev + "</html>");

        String statusInfo = "<html><b>" + rootNote.toString(true) + " " + chordDef.name + "</b><br/>" + chordDef.chord.getTransposedString(rootNote) + "</html>";

        this.setToolTipText(statusInfo);

        this.setSelected(isSelected);
        this.setFont(isSelected ? bold : plain);
        this.setVisible(true);
      }

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

    rootButtonGroup = new javax.swing.ButtonGroup();
    accButtonGroup = new javax.swing.ButtonGroup();
    jScrollPane1 = new javax.swing.JScrollPane();
    chordTable = new javax.swing.JTable();
    notePicker1 = new render.NotePicker();
    notePicker2 = new render.NotePicker();
    additionalBassCheckbox = new javax.swing.JCheckBox();
    currChordLabel = new javax.swing.JLabel();
    jButton1 = new javax.swing.JButton();
    jButton2 = new javax.swing.JButton();
    showAdvancedCheck = new javax.swing.JCheckBox();

    chordTable.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
    chordTable.setModel(new ChordTableModel());
    chordTable.setColumnSelectionAllowed(true);
    chordTable.setEditingColumn(0);
    chordTable.setEditingRow(0);
    chordTable.setFillsViewportHeight(true);
    chordTable.setRowHeight(25);
    chordTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    chordTable.setShowHorizontalLines(false);
    chordTable.setShowVerticalLines(false);
    chordTable.getTableHeader().setReorderingAllowed(false);
    jScrollPane1.setViewportView(chordTable);
    chordTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);

    notePicker1.setBorder(javax.swing.BorderFactory.createTitledBorder("Root:"));

    notePicker2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

    additionalBassCheckbox.setText("Additional Bass");
    additionalBassCheckbox.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        addedBassToggled(evt);
      }
    });

    currChordLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
    currChordLabel.setBorder(javax.swing.BorderFactory.createTitledBorder("Current Chord:"));

    jButton1.setText("OK");

    jButton2.setText("Cancel");

    showAdvancedCheck.setSelected(true);
    showAdvancedCheck.setText("Show All Chords");
    showAdvancedCheck.setEnabled(false);
    showAdvancedCheck.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        toggleShowAllChords(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(additionalBassCheckbox)
            .addContainerGap())
          .addGroup(layout.createSequentialGroup()
            .addComponent(notePicker2, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
            .addGap(143, 143, 143))
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
              .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
              .addComponent(currChordLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(notePicker1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
              .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
              .addComponent(showAdvancedCheck))
            .addContainerGap())))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(currChordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(notePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(additionalBassCheckbox)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(notePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(layout.createSequentialGroup()
            .addGap(11, 11, 11)
            .addComponent(jButton1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButton2)
            .addGap(18, 18, 18)
            .addComponent(showAdvancedCheck)))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

    private void addedBassToggled(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_addedBassToggled
      usingAddedBass = (evt.getStateChange() == ItemEvent.SELECTED);
      notePicker2.setVisible(usingAddedBass);
      updateCurrChord();
    }//GEN-LAST:event_addedBassToggled

    private void toggleShowAllChords(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_toggleShowAllChords
      this.simpleMode = (evt.getStateChange() == ItemEvent.DESELECTED);
      //this.additionalBassCheckbox.setSelected(!simpleMode);
      this.additionalBassCheckbox.setVisible(!simpleMode);
      this.chordTable.setColumnSelectionInterval(0, 0);

      int rowSel = chordTable.getSelectedRow();

      //((ChordTableModel)chordTable.getModel()).fireTableDataChanged();
      ((ChordTableModel) chordTable.getModel()).fireTableStructureChanged();
      initChordTable();

      chordTable.setColumnSelectionInterval(0, 0);

      if (rowSel >= chordTable.getRowCount()) {
        rowSel = 0;
      }

      chordTable.setRowSelectionInterval(rowSel, rowSel);
    }//GEN-LAST:event_toggleShowAllChords
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.ButtonGroup accButtonGroup;
  private javax.swing.JCheckBox additionalBassCheckbox;
  private javax.swing.JTable chordTable;
  private javax.swing.JLabel currChordLabel;
  private javax.swing.JButton jButton1;
  private javax.swing.JButton jButton2;
  private javax.swing.JScrollPane jScrollPane1;
  private render.NotePicker notePicker1;
  private render.NotePicker notePicker2;
  private javax.swing.ButtonGroup rootButtonGroup;
  private javax.swing.JCheckBox showAdvancedCheck;
  // End of variables declaration//GEN-END:variables
}
