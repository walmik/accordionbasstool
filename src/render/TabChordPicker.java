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
  RelChord currRelChord;
  Note addedBassNote = new Note();
  SeqColumnModel seqColumnModel = null;
  final static int DEFAULT_TABLE_COL_WIDTH = 96;
  int isUpdatingChord = 0;
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
        populateInversionCombo(inversionCombo.getSelectedIndex());
        changeInversion();
        updateChordInModel();
      }
    });

    notePickerAdd.setVisible(addedBassCheck.isSelected());
    //slashLabel.setVisible(addedBassCheck.isSelected());
    mustBeLowestCheck.setVisible(addedBassCheck.isSelected());

    addedBassCheck.addItemListener(new ItemListener()
    {

      @Override
      public void itemStateChanged(ItemEvent evt)
      {
        boolean usingAddedBass = (evt.getStateChange() == ItemEvent.SELECTED);

        notePickerAdd.setVisible(usingAddedBass);
        //slashLabel.setVisible(usingAddedBass);
        mustBeLowestCheck.setVisible(usingAddedBass);


        if (!usingAddedBass) {
          inversionCombo.setSelectedIndex(0);
          mustBeLowestCheck.setEnabled(true);
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

    String info = "<html><b><font size=\'+2\'>" + finalChord.nameHtml + "</font></b>";

    if (!finalChord.detail.isEmpty()) {
      info += " - " + finalChord.detail;
      info += ": (" + notestr + ")";
    } else if (addedBassCheck.isSelected()) {
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
    boolean usingAddedBass = addedBassCheck.isSelected();
    Note addedBass = (usingAddedBass ? addedBassNote : null);
    return new ParsedChordDef(rootNote, addedBass, currRelChord, getLowestBool());
  }

  private ParsedChordDef.BassSetting getLowestBool()
  {
    // If an inversion is set, then this value is irrelevant as the bass note
    // is already implied. Returning false to avoid duplicating the note an extra time.
    if (inversionCombo.getSelectedIndex() != 0) {
      return ParsedChordDef.BassSetting.NotLowestBass;
    }

    if (mustBeLowestCheck.isSelected()) {
      return ParsedChordDef.BassSetting.LowestBass;
    }

    return ParsedChordDef.BassSetting.NotLowestBass;
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
      possChordDef.updateStrings();
    } else {
      currRelChord = new RelChord();
    }

    Note newAddedBass = possChordDef.addedBassNote;

    if (newAddedBass != null) {
      addedBassCheck.setSelected(true);
      notePickerAdd.setNote(newAddedBass);
    } else {
      addedBassCheck.setSelected(false);
     // inversionCombo.setSelectedIndex(0);
    }

    if (inversionCombo.getSelectedIndex() == 0) {
      this.mustBeLowestCheck.setSelected(possChordDef.bassSetting == ParsedChordDef.BassSetting.LowestBass);
    } else {
      this.mustBeLowestCheck.setSelected(true);
    }
    
    isUpdatingChord--;

    updateChordUI(possChordDef);
  }

  void populateInversionCombo(
          int selectIndex)
  {
    int chordLen = currRelChord.getChordLength();

    inversionCombo.removeAllItems();
    inversionCombo.addItem("Any");

    if (chordLen >= 2) {
      inversionCombo.addItem("Root");
      inversionCombo.addItem("1st");

      if (chordLen >= 3) {
        inversionCombo.addItem("2nd");

        if (chordLen >= 4) {
          inversionCombo.addItem("3rd");

          for (int i = 4; i < chordLen; i++)
          {
            inversionCombo.addItem(i + "th");
          }
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
    if ((index + 1) >= inversionCombo.getItemCount()) {
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

//    if (currRelChord.getChordLength() == 1) {
//      index = 0;
//      isUpdatingChord++;
//      inversionCombo.setSelectedIndex(index);
//      isUpdatingChord--;
//    }

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
//    if ((index > 4)) {
//      return;
//    }

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
      this.mustBeLowestCheck.setEnabled(false);
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

    statusLabel = new javax.swing.JLabel();
    chordPicker1 = new render.ChordPicker();
    controlGrid = new javax.swing.JPanel();
    notePickerRoot = new render.NotePicker();
    jLabel1 = new javax.swing.JLabel();
    mustBeLowestCheck = new javax.swing.JCheckBox();
    inversionCombo = new javax.swing.JComboBox();
    addedBassCheck = new javax.swing.JCheckBox();
    notePickerAdd = new render.NotePicker();
    jLabel2 = new javax.swing.JLabel();

    statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getSize()+3f));
    statusLabel.setText("Label");
    statusLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
    statusLabel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel1.setText("Root:");

    mustBeLowestCheck.setSelected(true);
    mustBeLowestCheck.setText("Must be in Bass");
    mustBeLowestCheck.setActionCommand("Must be Bass Note");
    mustBeLowestCheck.setOpaque(false);
    mustBeLowestCheck.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        mustBeLowestCheckItemStateChanged(evt);
      }
    });

    inversionCombo.setFont(inversionCombo.getFont().deriveFont(inversionCombo.getFont().getStyle() | java.awt.Font.BOLD, inversionCombo.getFont().getSize()+3));
    inversionCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Any", "Root", "1st", "2nd", "3rd" }));
    inversionCombo.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(java.awt.event.ItemEvent evt) {
        inversionComboItemStateChanged(evt);
      }
    });

    addedBassCheck.setText("Add Note:");
    addedBassCheck.setOpaque(false);

    jLabel2.setLabelFor(inversionCombo);
    jLabel2.setText("Inversion:");

    javax.swing.GroupLayout controlGridLayout = new javax.swing.GroupLayout(controlGrid);
    controlGrid.setLayout(controlGridLayout);
    controlGridLayout.setHorizontalGroup(
      controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(controlGridLayout.createSequentialGroup()
        .addGroup(controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(controlGridLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addComponent(addedBassCheck))
        .addGap(6, 6, 6)
        .addGroup(controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(controlGridLayout.createSequentialGroup()
            .addComponent(notePickerRoot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addGroup(controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(inversionCombo, 0, 87, Short.MAX_VALUE)
              .addComponent(jLabel2)))
          .addGroup(controlGridLayout.createSequentialGroup()
            .addComponent(notePickerAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(mustBeLowestCheck)))
        .addContainerGap())
    );
    controlGridLayout.setVerticalGroup(
      controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(controlGridLayout.createSequentialGroup()
        .addGroup(controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(notePickerRoot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlGridLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(inversionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(controlGridLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel1)))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(addedBassCheck)
          .addGroup(controlGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(mustBeLowestCheck)
            .addComponent(notePickerAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(controlGrid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE))
      .addComponent(chordPicker1, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(controlGrid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(chordPicker1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addContainerGap())
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
  private render.ChordPicker chordPicker1;
  private javax.swing.JPanel controlGrid;
  private javax.swing.JComboBox inversionCombo;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JCheckBox mustBeLowestCheck;
  private render.NotePicker notePickerAdd;
  private render.NotePicker notePickerRoot;
  private javax.swing.JLabel statusLabel;
  // End of variables declaration//GEN-END:variables
}
