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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import music.Chord;
import music.Note;
import music.ParsedChordDef;
import music.RelChord;

/**
 *
 * @author Ilya
 */
public class TabChordPicker extends ToolPanel
        implements ActionListener, PropertyChangeListener
{

  Note rootNote = new Note();
  RelChord currRelChord;
  Note addedBassNote = new Note();
  final static int DEFAULT_TABLE_COL_WIDTH = 96;
  int isUpdatingChord = 0;
  boolean usingAddedBass;

  public TabChordPicker()
  {
    initComponents();
  }

  @Override
  public void init(SeqColumnModel model)
  {
    super.init(model);
    notePickerRoot.addPropertyChangeListener("Note", this);
    notePickerAdd.addPropertyChangeListener("Note", this);
    chordPicker1.addPropertyChangeListener("Chord", this);
    inversionCombo.addActionListener(this);

    notePickerAdd.setVisible(usingAddedBass);
    labelAddBass.setVisible(usingAddedBass);
    labelSlash.setVisible(usingAddedBass);
  }

  @Override
  protected void columnSelectionChanged(int index)
  {
    setupChord(columnModel.getChordDef(index));
  }

  @Override
  protected boolean listenToCols()
  {
    return true;
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == this.inversionCombo) {
      this.changeInversion();
      this.updateChordInModel();
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    if (evt.getPropertyName().equals("Note")) {
      if (evt.getSource() == this.notePickerRoot) {
        rootNote = (Note) evt.getNewValue();
        changeInversion();

      } else if (evt.getSource() == this.notePickerAdd) {
        addedBassNote = (Note) evt.getNewValue();
        populateSyncInversionCombo();
      }
      updateChordInModel();
    }
    if (evt.getPropertyName().equals("Chord") && (evt.getSource() == chordPicker1)) {
      currRelChord = (RelChord) evt.getNewValue();

      populateSyncInversionCombo();
      changeInversion();

      updateChordInModel();
      return;
    }
  }

  private void updateChordInModel()
  {
    if (isUpdatingChord > 0) {
      return;
    }

    isUpdatingChord++;

    ParsedChordDef finalChord = getPickedChord();

    if (columnModel != null) {
      columnModel.editSelectedColumn(finalChord);
    }

    isUpdatingChord--;

    updateChordUI(finalChord);
  }

  private void updateChordUI(ParsedChordDef finalChord)
  {
    String notestr = finalChord.chord.toHtmlString();

    String info = "<html><b><font size=\'+2\'>" + finalChord.nameHtml + "</font></b>";

    if (!finalChord.detail.isEmpty()) {
      info += " - " + finalChord.detail;
      info += ": (" + notestr + ")";
    } else if (usingAddedBass) {
      info += ": (" + notestr + ")";
    }

    info += "</html>";

    statusLabel.setText(info);

    //hugeChordLabel.setText("<html>" + finalChord.nameHtml + "</html>");


//    if (currRelChord.origDef != null) {
//      chordLabel.setText("<html>" + currRelChord.origDef.name + "</html>");
//    } else {
//      chordLabel.setText("<html>Custom</html>");
//    }
  }

  ParsedChordDef getPickedChord()
  {
    //boolean usingAddedBass = addedBassCheck.isSelected();
    Note addedBass = (usingAddedBass ? addedBassNote : null);
    return new ParsedChordDef(rootNote, addedBass, currRelChord, getLowestBool());
  }

  private ParsedChordDef.BassSetting getLowestBool()
  {
    // If an inversion is set, then this value is irrelevant as the bass note
    // is already implied. Returning false to avoid duplicating the note an extra time.
    if (inversionCombo.getSelectedIndex() != 1) {
      return ParsedChordDef.BassSetting.NotLowestBass;
    }

//    if (mustBeLowestCheck.isSelected()) {
//      return ParsedChordDef.BassSetting.LowestBass;
//    }

    return ParsedChordDef.BassSetting.LowestBass;
  }

  @Override
  protected void syncUIToDataModel()
  {
    int index = columnModel.getSelectedColumn();
    if ((index >= 0) && (index < columnModel.getColumnCount())) {
      setupChord(this.columnModel.getChordDef(index));
    }
  }

  private void setupChord(ParsedChordDef possChordDef)
  {
    if (isUpdatingChord > 0) {
      return;
    }

    if ((possChordDef == null) || (possChordDef.rootNote == null)) {
      return;
    }

//    if ((possChordDef.relChord != null) && (possChordDef.relChord.getOrigDef() == null)) {
////      ParsedChordDef matchChord = ChordRegistry.mainRegistry().findFirstChordFromNotes(possChordDef.chord);
//      ParsedChordDef matchChord = columnModel.matchingChordStore.getFirstKnownMatchingChord();
//      if (matchChord != null) {
//        possChordDef = matchChord;
//      }
//    }

    Note newRoot = possChordDef.rootNote;

    if (newRoot == null) {
      return;
    }

    isUpdatingChord++;

    notePickerRoot.setNote(newRoot);

    if (possChordDef.relChord != null) {
      currRelChord = possChordDef.relChord.clone();
    } else {
      currRelChord = new RelChord(possChordDef.chord);
    }

    chordPicker1.setRelChord(currRelChord);

    Note newAddedBass = possChordDef.addedBassNote;

    if (newAddedBass != null) {
      //addedBassCheck.setSelected(true);
      usingAddedBass = true;
      notePickerAdd.setNote(newAddedBass);
    } else {
      usingAddedBass = false;
      //addedBassCheck.setSelected(false);
      // inversionCombo.setSelectedIndex(0);
    }
    populateSyncInversionCombo();

//    if (inversionCombo.getSelectedIndex() == 0) {
//      this.mustBeLowestCheck.setSelected(possChordDef.bassSetting == ParsedChordDef.BassSetting.LowestBass);
//    } else {
//      this.mustBeLowestCheck.setSelected(true);
//    }

    isUpdatingChord--;

    changeInversion();

    updateChordUI(possChordDef);
  }

  void populateSyncInversionCombo()
  {
    isUpdatingChord++;

    int chordLen = currRelChord.getChordLength();

    inversionCombo.removeAllItems();
    inversionCombo.addItem("None");
    inversionCombo.addItem("Custom");

    if (chordLen >= 2) {
      inversionCombo.addItem("Root");
      inversionCombo.addItem("1st");

      if (chordLen >= 3) {
        inversionCombo.addItem("2nd");

        if (chordLen >= 4) {
          inversionCombo.addItem("3rd");

          for (int i = 4; i < chordLen; i++) {
            inversionCombo.addItem(i + "th");
          }
        }
      }
    }

//    if (selectIndex >= inversionCombo.getItemCount()) {
//      selectIndex = 1;
//    }
//    if (selectIndex >= 0) {
//      inversionCombo.setSelectedIndex(selectIndex);
//    }


    Chord simpleChord = currRelChord.buildChord(rootNote);

    int index = 0;

    if (usingAddedBass) {
      index = simpleChord.findNotePos(addedBassNote);

      // First two items are None and Custom, so inversions start at 2
      if ((index == -1) || ((index + 2) >= inversionCombo.getItemCount())) {
        index = 1;
      } else {
        index += 2;
      }
    }

    inversionCombo.setSelectedIndex(index);
    //this.mustBeLowestCheck.setEnabled(index == 0);

    isUpdatingChord--;
  }

  void changeInversion()
  {
    if (isUpdatingChord > 0) {
      return;
    }

    int index = inversionCombo.getSelectedIndex();

//    if (currRelChord.getChordLength() == 1) {
//      index = 0;
//      isUpdatingChord++;
//      inversionCombo.setSelectedIndex(index);
//      isUpdatingChord--;
//    }

//    mustBeLowestCheck.setEnabled(true);

    // Index 0 is none, so toggle added bass UI
    usingAddedBass = (index > 0);

    notePickerAdd.setVisible(usingAddedBass);
    labelAddBass.setVisible(usingAddedBass);
    labelSlash.setVisible(usingAddedBass);

    if (index < 2) {
      return;
    }

    // Select specific inversion note

    index -= 2;

    Chord simpleChord = currRelChord.buildChord(rootNote);
    Note inverseRoot = simpleChord.getNoteAt(index);

    this.isUpdatingChord++;

    if (inverseRoot != null) {
      this.notePickerAdd.setNote(inverseRoot);
      //  this.addedBassCheck.setSelected(true);
      //  this.mustBeLowestCheck.setSelected(true);
      //  this.mustBeLowestCheck.setEnabled(false);
    }

    this.isUpdatingChord--;
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    mustBeLowestCheck = new javax.swing.JCheckBox();
    chordPicker1 = new render.ChordPicker();
    controlGrid = new javax.swing.JPanel();
    notePickerRoot = new render.NotePicker();
    labelRoot = new javax.swing.JLabel();
    inversionCombo = new javax.swing.JComboBox();
    notePickerAdd = new render.NotePicker();
    labelInversion = new javax.swing.JLabel();
    labelAddBass = new javax.swing.JLabel();
    labelSlash = new javax.swing.JLabel();
    statusLabel = new render.TransparentTextPane();

    mustBeLowestCheck.setSelected(true);
    mustBeLowestCheck.setText("Must be in Bass");
    mustBeLowestCheck.setActionCommand("Must be Bass Note");
    mustBeLowestCheck.setOpaque(false);
    mustBeLowestCheck.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        mustBeLowestCheckItemStateChanged(evt);
      }
    });

    labelRoot.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    labelRoot.setText("Chord Root:");

    inversionCombo.setFont(inversionCombo.getFont().deriveFont(inversionCombo.getFont().getStyle() | java.awt.Font.BOLD, inversionCombo.getFont().getSize()+3));

    labelInversion.setLabelFor(inversionCombo);
    labelInversion.setText("Inversion:");

    labelAddBass.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    labelAddBass.setText("Lowest Note:");

    labelSlash.setFont(new java.awt.Font("Tahoma", 1, 24));
    labelSlash.setText("/");

    javax.swing.GroupLayout controlGridLayout = new javax.swing.GroupLayout(controlGrid);
    controlGrid.setLayout(controlGridLayout);
    controlGridLayout.setHorizontalGroup(
      controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(controlGridLayout.createSequentialGroup()
        .addGroup(controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(notePickerRoot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(labelRoot, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(labelSlash)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(notePickerAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(labelAddBass, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(18, 18, 18)
        .addGroup(controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addGroup(controlGridLayout.createSequentialGroup()
            .addComponent(labelInversion)
            .addGap(38, 38, 38))
          .addComponent(inversionCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
    );
    controlGridLayout.setVerticalGroup(
      controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(controlGridLayout.createSequentialGroup()
        .addGroup(controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(controlGridLayout.createSequentialGroup()
            .addGroup(controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(labelRoot)
              .addComponent(labelAddBass))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(notePickerRoot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(labelSlash)
              .addComponent(notePickerAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
          .addGroup(controlGridLayout.createSequentialGroup()
            .addComponent(labelInversion)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(inversionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap(24, Short.MAX_VALUE))
    );

    statusLabel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
    statusLabel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
    statusLabel.setText("Info");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(controlGrid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
        .addContainerGap())
      .addComponent(chordPicker1, javax.swing.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(controlGrid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(chordPicker1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void mustBeLowestCheckItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_mustBeLowestCheckItemStateChanged
  {//GEN-HEADEREND:event_mustBeLowestCheckItemStateChanged
    this.updateChordInModel();
  }//GEN-LAST:event_mustBeLowestCheckItemStateChanged
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private render.ChordPicker chordPicker1;
  private javax.swing.JPanel controlGrid;
  private javax.swing.JComboBox inversionCombo;
  private javax.swing.JLabel labelAddBass;
  private javax.swing.JLabel labelInversion;
  private javax.swing.JLabel labelRoot;
  private javax.swing.JLabel labelSlash;
  private javax.swing.JCheckBox mustBeLowestCheck;
  private render.NotePicker notePickerAdd;
  private render.NotePicker notePickerRoot;
  private render.TransparentTextPane statusLabel;
  // End of variables declaration//GEN-END:variables
}
