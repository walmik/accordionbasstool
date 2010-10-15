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
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JTable;
import javax.swing.JToggleButton;
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
import util.Main.StringParser;

/**
 *
 * @author Ilya
 */
public class ChordPicker extends javax.swing.JDialog
{

  Note rootNote = new Note();
  ChordDef currTableChord;
  Note addedBassNote = new Note();
  boolean addedBassLowest = false;
  boolean usingAddedBass = false;
  boolean changeConfirmed = false;

  /** Creates new form ChordPicker */
  public ChordPicker(java.awt.Frame parent, boolean modal)
  {
    super(parent, modal);
    initComponents();
    initChordTable();

    this.getRootPane().setDefaultButton(okButton);

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
        chordTable.repaint();
      }
    });
    notePicker2.setVisible(usingAddedBass);
  }
  
  ChordDef finalChord = new ChordDef();

  public ChordDef getDefaultChordDef()
  {
    return finalChord;
  }

  private void updateCurrChord()
  {
    if (currTableChord == null) {
      return;
    }

    finalChord.chord = new Chord(currTableChord.chord, rootNote, (usingAddedBass ? addedBassNote : null), addedBassLowest);

    // -- Set HTML Abbrev
    finalChord.abbrevHtml = rootNote.toString(true) + currTableChord.abbrevHtml;

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

    // -- Set Note List
    String notestr = finalChord.chord.toString("-", true);

    String info = "<html><b>" + finalChord.abbrevHtml + "</b>"
            + "<br/>" + finalChord.name + ": "
            + "(" + notestr + ")" + "</html>";

    currChordLabel.setText(info);
  }

  ChordDef showChordPicker(ChordDef startChordDef)
  {
    if (startChordDef != null) {
      finalChord = startChordDef;
      setupChord(finalChord.abbrevPlain);
    } else {
      finalChord = new ChordDef();
      setupChord("C");
    }

    //this.setupChord("A#minM9/D#");
    this.setModal(true);
    this.setVisible(true);

    //---> Modal Loop Here

    if (changeConfirmed)
    {
      updateCurrChord();
      return finalChord;
    }

    return null;
  }

  void setupChord(String chordStr)
  {
    StringParser parser = new StringParser(chordStr);
    Note newRoot = Note.fromString(parser);
    notePicker1.setNote(newRoot);

    //this.rootNote = newRoot;

    Point chordLoc = ((ChordTableModel) chordTable.getModel()).findChord(parser);
    int row = chordLoc.y;
    int col = chordTable.convertColumnIndexToView(chordLoc.x);

    chordTable.setRowSelectionInterval(row, row);
    chordTable.setColumnSelectionInterval(col, col);

    Note newAddedBass = null;

    if (parser.nextChar() == '/') {
      parser.incOffset(1);
      newAddedBass = Note.fromString(parser);
    }

    if (newAddedBass != null) {
      this.additionalBassCheckbox.setSelected(true);
      notePicker2.setNote(newAddedBass);
    } else {
      this.additionalBassCheckbox.setSelected(false);
    }
  }
  boolean simpleMode = false;

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

    ChordDef allChords[][];
    String groupNames[];
    int maxRows = 0;

    ChordTableModel()
    {
      loadFromXml("C:/Users/Ilya/workspace/AccordionBassTool/xml/chorddefs.xml");
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
    public Class<?> getColumnClass(int columnIndex)
    {
      return ChordDef.class;
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

    public Point findChord(StringParser parser)
    {
      int prevMatchLength = 0;
      Point bestMatch = new Point(0, 0);
      String chordToMatch = parser.input();

      for (int col = 0; col < allChords.length; col++) {
        for (int row = 0; row < allChords[col].length; row++) {
          String currAbbrev = allChords[col][row].abbrevPlain;
          if ((currAbbrev.length() > prevMatchLength) &&
              chordToMatch.startsWith(currAbbrev)) {
            bestMatch.x = col;
            bestMatch.y = row;
            prevMatchLength = currAbbrev.length();
          }
        }
      }

      parser.incOffset(prevMatchLength);
      return bestMatch;
    }
  }

  void initChordTable()
  {
    TableColumnModel model = chordTable.getColumnModel();
    model.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    ToggleButtonRenderer renderer = new ToggleButtonRenderer();

    chordTable.setDefaultRenderer(ChordDef.class, renderer);

    chordTable.getTableHeader().setReorderingAllowed(true);
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
    okButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();
    showAdvancedCheck = new javax.swing.JCheckBox();

    setTitle("Chord Picker");

    chordTable.setFont(new java.awt.Font("Tahoma", 0, 14));
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

    okButton.setText("OK");
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        okCancelButtonClicked(evt);
      }
    });

    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        okCancelButtonClicked(evt);
      }
    });

    showAdvancedCheck.setSelected(true);
    showAdvancedCheck.setText("Show All Chords");
    showAdvancedCheck.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        toggleShowAllChords(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addGap(10, 10, 10)
        .addComponent(additionalBassCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE)
        .addContainerGap())
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(notePicker2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
          .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(notePicker1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
              .addComponent(currChordLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addGroup(layout.createSequentialGroup()
            .addGap(18, 18, 18)
            .addComponent(showAdvancedCheck))
          .addGroup(layout.createSequentialGroup()
            .addGap(10, 10, 10)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(cancelButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(okButton)
            .addGap(3, 3, 3)
            .addComponent(cancelButton))
          .addComponent(currChordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(showAdvancedCheck)
          .addComponent(notePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(additionalBassCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(notePicker2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
        .addContainerGap())
    );

    java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    setBounds((screenSize.width-610)/2, (screenSize.height-519)/2, 610, 519);
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

      //initChordTable();

      if (rowSel >= chordTable.getRowCount()) {
        rowSel = chordTable.getRowCount() - 1;
      }

      chordTable.setRowSelectionInterval(rowSel, rowSel);
      chordTable.setColumnSelectionInterval(0, 0);
    }//GEN-LAST:event_toggleShowAllChords

    private void okCancelButtonClicked(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okCancelButtonClicked
    {//GEN-HEADEREND:event_okCancelButtonClicked
      changeConfirmed = (evt.getActionCommand() == "OK");
      this.setVisible(false);
    }//GEN-LAST:event_okCancelButtonClicked
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.ButtonGroup accButtonGroup;
  private javax.swing.JCheckBox additionalBassCheckbox;
  private javax.swing.JButton cancelButton;
  private javax.swing.JTable chordTable;
  private javax.swing.JLabel currChordLabel;
  private javax.swing.JScrollPane jScrollPane1;
  private render.NotePicker notePicker1;
  private render.NotePicker notePicker2;
  private javax.swing.JButton okButton;
  private javax.swing.ButtonGroup rootButtonGroup;
  private javax.swing.JCheckBox showAdvancedCheck;
  // End of variables declaration//GEN-END:variables
}
