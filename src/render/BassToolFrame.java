/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BassToolFrame.java
 *
 * Created on Jun 19, 2010, 1:51:26 AM
 */
package render;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.sound.sampled.AudioPermission;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import music.BoardRegistry;
import music.BoardRegistry.BoardDef;
import music.ParsedChordDef;

/**
 *
 * @author Ilya
 */
public class BassToolFrame extends javax.swing.JFrame implements PropertyChangeListener, ActionListener
{

  boolean editorVis = true;
  boolean editorLeft = true;
  ModeSelector modeSelector;
  RenderBassBoard renderBassBoard;
  BoardMouseListener mouseListener;
  SeqColumnModel columnModel = null;
  SoundController sound;
  SeqAnimController anim;
  ToolPanel[] allTools;

  public static enum ToolMode
  {

    BoardOnly("Accordion Board Only"),
    ChordMatch("Chord Matching"),
    ChordPicker("Chord Picker"),
    SeqPicker("Pattern Picker"),
    Default("Full Tool");

    ToolMode(String str)
    {
      title = str;
    }
    String title;

    @Override
    public String toString()
    {
      return title;
    }
  }

  /** Creates new form BassToolFrame */
  public BassToolFrame()
  {
    this(ToolMode.Default);
  }

  public BassToolFrame(ToolMode mode)
  {
    BoardRegistry.mainRegistry();

    initComponents();

    // Save initial full tab layout
    allTools = new ToolPanel[toolTabs.getTabCount()];
    for (int i = 0; i < allTools.length; i++) {
      allTools[i] = (ToolPanel)toolTabs.getComponentAt(i);
      allTools[i].setName(toolTabs.getTitleAt(i));
    }

    renderBassBoard = renderBoardControl.renderBassBoard;

    initModeSelector(mode);

    init(mode);

    initTabsMenu();
//
//    this.jMenuBar1.add(Box.createHorizontalStrut(100));
//    this.jMenuBar1.add(new JCheckBox("Test"));
//    this.jMenuBar1.add(new JSlider());
  }

  private void init(ToolMode mode)
  {

    switch (mode) {
      case Default:
        initDefault();
        break;

      case BoardOnly:
        initBoardOnly();
        break;

      case ChordMatch:
        initChordMatcher();
        break;

      case ChordPicker:
        initChordPicker();
        break;

      case SeqPicker:
        initSeqPicker();
        break;
    }

    tabOptions.addPropertyChangeListener(this);

    pack();
    //setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
    if (boardSplitPane.isVisible()) {
      boardSplitPane.setDividerLocation(boardSplitPane.getMinimumDividerLocation());
    }
  }

  private void initBoardOnly()
  {
    columnModel = null;
    Vector<BoardDef> allowedBoards = BoardRegistry.mainRegistry().getStandardBoards();

    sound = new SoundController(false);
    mouseListener = new BoardMouseListener(renderBassBoard, null, sound);

    RenderBoardHeader header = this.renderBoardControl.renderBoardHeader;
    header.initBoardHeader(renderBassBoard, renderBoardControl, null, allowedBoards);
    header.selectFirstBoardByBassCount(48);

//    header.getExtPanel().add(checkSoundEnabled);
//    header.getExtPanel().add(checkVertical);

    this.controlSplitPane.setVisible(false);
    this.boardSplitPane.setVisible(false);
    //this.boardSplitPane.setTopComponent(null);
    this.getContentPane().add(this.renderBoardControl, BorderLayout.CENTER);
  }

  private void initChordMatcher()
  {
    if (columnModel == null) {
      columnModel = new SeqColumnModel(renderBassBoard);
    }

    sound = new SoundController(false);
    //sound.setEnabled(checkSoundEnabled.isSelected());

    Vector<BoardDef> allowedBoards = BoardRegistry.mainRegistry().getStandardBoards();

    mouseListener = new BoardMouseListener(renderBassBoard, columnModel, sound);

    tabChordInfo.init(columnModel);

    RenderBoardHeader header = this.renderBoardControl.renderBoardHeader;
    header.initBoardHeader(renderBassBoard, renderBoardControl, columnModel, allowedBoards);
    header.selectFirstBoardByBassCount(48);

    this.controlSplitPane.setVisible(false);
    this.setSplit(boardSplitPane, tabChordInfo, renderBoardControl);

    this.getContentPane().add(boardSplitPane, BorderLayout.CENTER);

    // Add Default Chord
    if (columnModel.getColumnCount() == 0) {
      columnModel.addColumn(ParsedChordDef.newEmptyChordDef(), 0);
    }
  }

  private void initSeqPicker()
  {
    if (columnModel == null) {
      columnModel = new SeqColumnModel(renderBassBoard);
    }

    sound = new SoundController(false);
    //sound.setEnabled(checkSoundEnabled.isSelected());

    Vector<BoardDef> allowedBoards = BoardRegistry.mainRegistry().getStandardBoards();

    //mouseListener = new BoardMouseListener(renderBassBoard, columnModel, sound);
    renderBassBoard.setMainMouseAdapter(null);

    this.seqPicker.init(columnModel);

    RenderBoardHeader header = this.renderBoardControl.renderBoardHeader;
    header.initBoardHeader(renderBassBoard, renderBoardControl, columnModel, allowedBoards);
    header.selectFirstBoardByBassCount(48);

    this.controlSplitPane.setVisible(false);
    this.setSplit(boardSplitPane, seqPicker, renderBoardControl);

    this.getContentPane().add(boardSplitPane, BorderLayout.CENTER);

    // Add Default Chord
    if (columnModel.getColumnCount() == 0) {
      columnModel.addColumn(ParsedChordDef.newEmptyChordDef(), 0);
    }
  }

  private void initChordPicker()
  {
    if (columnModel == null) {
      columnModel = new SeqColumnModel(renderBassBoard);

      sound = new SoundController(false);

      seqTablePanel.init(columnModel, renderBassBoard, sound);
      seqTablePanel.buttonHideEditor.addActionListener(this);
      seqTablePanel.toggleSeqControls(false);

      mouseListener = new BoardMouseListener(renderBassBoard, columnModel, sound);

      renderBoardControl.renderBoardHeader.getExtPanel().add(this.checkHiliteRedunds);
    }

    toolTabs.setVisible(false);
    tabChordPicker.init(columnModel);

    controlSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);

    RenderBoardHeader header = this.renderBoardControl.renderBoardHeader;
    header.initBoardHeader(renderBassBoard, renderBoardControl, columnModel, null);


    this.setSplit(controlSplitPane, tabChordPicker, seqTablePanel);
    this.setSplit(boardSplitPane, controlSplitPane, renderBoardControl);

    this.getContentPane().add(boardSplitPane, BorderLayout.CENTER);

    // Set Board to default 120
    renderBoardControl.renderBoardHeader.selectFirstBoardByBassCount(120);

    // Add Default Chord!
    if (columnModel.getColumnCount() == 0) {
      columnModel.addColumn(0);
    } else if (columnModel.getSelectedChordDef().isEmptyChord()) {
      columnModel.editSelectedColumn(ParsedChordDef.newDefaultChordDef());
    }
  }

  private void initDefault()
  {
    if (columnModel == null) {
      columnModel = new SeqColumnModel(renderBassBoard);

      renderBoardControl.renderBoardHeader.initBoardHeader(renderBassBoard, renderBoardControl, columnModel, null);

      sound = new SoundController(false);
      sound.initSoundMenu(menuSound);

      seqTablePanel.init(columnModel, renderBassBoard, sound);

      mouseListener = new BoardMouseListener(renderBassBoard, columnModel, sound);

      renderBoardControl.renderBoardHeader.getExtPanel().add(this.checkHiliteRedunds);

      seqTablePanel.buttonHideEditor.addActionListener(this);

      controlSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);
    }

    toolTabs.removeAll();

    // Init Tabs
    for (ToolPanel tab : allTools) {
      tab.init(columnModel);
      toolTabs.add(tab.getName(), tab);
    }

    if (!checkPitchDetectPermissions()) {
      toolTabs.setEnabledAt(toolTabs.indexOfComponent(tabPitchDetect), false);
    }

    toolTabs.setVisible(true);

    this.setSplit(controlSplitPane, toolTabs, seqTablePanel);
    this.setSplit(boardSplitPane, controlSplitPane, renderBoardControl);

    //this.getContentPane().removeAll();
    this.getContentPane().add(boardSplitPane, BorderLayout.CENTER);

    // Setup intiial state

    // Set Board to default 120
    renderBoardControl.renderBoardHeader.selectFirstBoardByBassCount(120);

    // Add Default Chord!
    if (columnModel.getColumnCount() == 0) {
      columnModel.addColumn(0);
    } else if (columnModel.getSelectedChordDef().isEmptyChord()) {
      columnModel.editSelectedColumn(ParsedChordDef.newDefaultChordDef());
    }
  }

  private void setSplit(JSplitPane pane, JComponent left, JComponent right)
  {
    pane.setLeftComponent(null);
    pane.setRightComponent(null);
    pane.setLeftComponent(left);
    pane.setRightComponent(right);
    pane.setVisible(true);
  }

  private boolean checkPitchDetectPermissions()
  {
    SecurityManager manager = System.getSecurityManager();
    if (manager == null) {
      return true;
    }

    try {
      manager.checkPermission(new AudioPermission("record"));
      return true;
    } catch (SecurityException sx) {
      return false;
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    if (evt.getPropertyName().equals(JSplitPane.DIVIDER_LOCATION_PROPERTY)) {
      dividerChanged(((Integer) evt.getNewValue()).intValue());
    } else if (evt.getSource().equals(this.tabOptions)) {
      String prop = evt.getPropertyName();
      if (TabOptions.TOGGLE_EDITOR_PROPERTY.equals(prop)) {
        this.toggleEditorLeft();
      } else if (TabOptions.TOGGLE_BOARDPOS_PROPERTY.equals(prop)) {
        this.toggleBoardPos();
      } else if (TabOptions.TOGGLE_ORIENT_PROPERTY.equals(prop)) {
        this.toggleOrientation();
      }
    }
  }

  private void dividerChanged(int newLoc)
  {
    if (!editorLeft) {
      this.editorVis = (newLoc <= controlSplitPane.getMaximumDividerLocation() + 10);
    } else {
      this.editorVis = (newLoc >= 10);
    }

    String brakLeft = "<< ";
    String brakRight = " >>";

    if (!renderBassBoard.isHorizontal()) {
      brakLeft = "^^ ";
      brakRight = " vv";
    }

    //System.out.println("newLoc: " + newLoc + "Width: " + controlSplitPane.getWidth() + " Max: " + this.controlSplitPane.getMaximumDividerLocation());
    if (editorVis) {
      seqTablePanel.buttonHideEditor.setText(editorLeft ? brakLeft + "Hide Editor" : "Hide Editor" + brakRight);
    } else {
      seqTablePanel.buttonHideEditor.setText(editorLeft ? "Show Editor" + brakRight : brakLeft + "Show Editor");
    }
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == seqTablePanel.buttonHideEditor) {
      if (editorVis) {
        controlSplitPane.setDividerLocation(editorLeft ? 0.0 : 1.0);
      } else {
        controlSplitPane.setDividerLocation(editorLeft ? controlSplitPane.getMinimumDividerLocation() : controlSplitPane.getMaximumDividerLocation());
      }
    }

    if ((modeSelector != null) && (e.getSource() == modeSelector.modeCombo)) {
      init((ToolMode) modeSelector.modeCombo.getSelectedItem());
    }
  }

  public void toggleBoardPos()
  {
    Component left = boardSplitPane.getLeftComponent();
    Component right = boardSplitPane.getRightComponent();
    boardSplitPane.setLeftComponent(null);
    boardSplitPane.setRightComponent(null);
    boardSplitPane.setLeftComponent(right);
    boardSplitPane.setRightComponent(left);

    this.computeDividerLocation();
  }

  public void toggleOrientation()
  {
    boolean isHoriz = renderBassBoard.isHorizontal();

    if (isHoriz) {
      controlSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
      boardSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    } else {
      boardSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
      controlSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    }

    renderBoardControl.toggleOrientation(!isHoriz);

    this.computeDividerLocation();
  }

  public void toggleEditorLeft()
  {
    editorLeft = !editorLeft;

    Component left = controlSplitPane.getLeftComponent();
    Component right = controlSplitPane.getRightComponent();
    controlSplitPane.setLeftComponent(null);
    controlSplitPane.setRightComponent(null);
    controlSplitPane.setLeftComponent(right);
    controlSplitPane.setRightComponent(left);

    seqTablePanel.toggleLeftRight(editorLeft);

    if (!editorLeft) {
      controlSplitPane.setDividerLocation(controlSplitPane.getMaximumDividerLocation());
    } else {
      controlSplitPane.setDividerLocation(controlSplitPane.getMinimumDividerLocation());
    }
  }

  private void computeDividerLocation()
  {
    boolean isTopLeft = (boardSplitPane.getRightComponent() == controlSplitPane);
    boolean isHoriz = renderBassBoard.isHorizontal();

    if (!isHoriz) {
      // Set divider to preferred board width to give tools/table panel as much space

      int prefWidth = renderBassBoard.getPreferredSize().width;
      if (isTopLeft) {
        boardSplitPane.setDividerLocation(boardSplitPane.getMinimumDividerLocation() + prefWidth);
      } else {
        boardSplitPane.setDividerLocation(boardSplitPane.getMaximumDividerLocation() - prefWidth);
      }

    } else {
      // Just set to min/max from the control pane..
      if (isTopLeft) {
        boardSplitPane.setDividerLocation(boardSplitPane.getMaximumDividerLocation());
      } else {
        boardSplitPane.setDividerLocation(boardSplitPane.getMinimumDividerLocation());
      }

      // Must readjust editors as well..

      if (!editorLeft) {
        controlSplitPane.setDividerLocation(controlSplitPane.getMaximumDividerLocation());
      } else {
        controlSplitPane.setDividerLocation(controlSplitPane.getMinimumDividerLocation());
      }
    }
  }

  // Menu Actions //
  class ModeAction extends AbstractAction
  {

    ModeAction(ToolMode mode)
    {
      theMode = mode;
    }

    @Override
    public Object getValue(String key)
    {
      if (key.equals(NAME)) {
        return theMode.toString();
      } else if (key.equals(ACTION_COMMAND_KEY)) {
        return theMode.name();
      }

      return super.getValue(key);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      init(theMode);
    }

    @Override
    public String toString()
    {
      return theMode.toString();
    }
    ToolMode theMode;
  }

  void initModeSelector(ToolMode startMode)
  {
    ButtonGroup group = new ButtonGroup();
    for (ToolMode mode : ToolMode.values()) {
      JMenuItem item = new JRadioButtonMenuItem(new ModeAction(mode));
      group.add(item);
      if (mode == startMode) {
        item.setSelected(true);
      }
      menuMode.add(item);
    }



//    modeSelector = new ModeSelector();
//    JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//    flowPanel.add(modeSelector);
//    getContentPane().add(flowPanel, java.awt.BorderLayout.NORTH);
//    modeSelector.modeCombo.setModel(new DefaultComboBoxModel(ToolMode.values()));
//    modeSelector.modeCombo.setSelectedItem(startMode);
//    modeSelector.modeCombo.addActionListener(this);
//
////    modeSelector.checkFingerSearch.setSelected(seqTablePanel.columnModel.optFingerSearch);
//
//    modeSelector.checkFingerSearch.addActionListener(new ActionListener()
//    {
//
//      @Override
//      public void actionPerformed(ActionEvent e)
//      {
//        if (columnModel != null) {
//          columnModel.optFingerSearch = (modeSelector.checkFingerSearch.isSelected());
//          columnModel.recomputeSeqs();
//        }
//      }
//    });

    //modeSelector.modeCombo.setSelectedItem(ToolMode.BUTTON_FOR_CHORD);
  }

  class TabAction extends AbstractAction
  {

    int theIndex;

    public TabAction(int index)
    {
      theIndex = index;
      this.putValue(NAME, toolTabs.getTitleAt(index));
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      toolTabs.setSelectedIndex(theIndex);
    }
  }

  private void initTabsMenu()
  {
    ButtonGroup group = new ButtonGroup();
    for (int i = 0; i < toolTabs.getTabCount(); i++) {
      JMenuItem item = new JRadioButtonMenuItem(
              new TabAction(i));
      group.add(item);
      if (i == toolTabs.getSelectedIndex()) {
        item.setSelected(true);
      }
      menuChords.add(item);
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

    checkHiliteRedunds = new javax.swing.JCheckBox();
    boardSplitPane = new javax.swing.JSplitPane();
    controlSplitPane = new javax.swing.JSplitPane();
    seqTablePanel = new render.SeqTablePanel();
    toolTabs = new javax.swing.JTabbedPane();
    tabChordInfo = new render.TabChordInfo();
    tabChordPicker = new render.TabChordPicker();
    tabSeqEditor = new render.TabSeqEditor();
    seqPicker = new render.SeqPicker();
    tabOptions = new render.TabOptions();
    tabPitchDetect = new render.TabPitchDetect();
    renderBoardControl = new render.RenderBoardControl();
    jMenuBar1 = new javax.swing.JMenuBar();
    menuMode = new javax.swing.JMenu();
    menuChords = new javax.swing.JMenu();
    menuSound = new javax.swing.JMenu();
    miSoundEnabled = new javax.swing.JCheckBoxMenuItem();
    miArpeggiateChord = new javax.swing.JCheckBoxMenuItem();
    menuPlayback = new javax.swing.JMenu();
    miPlayStop = new javax.swing.JMenuItem();
    miStop = new javax.swing.JMenuItem();
    menuOptions = new javax.swing.JMenu();
    miIncludeFingering = new javax.swing.JCheckBoxMenuItem();

    checkHiliteRedunds.setFont(new java.awt.Font("Tahoma", 1, 14));
    checkHiliteRedunds.setText("Press Redundant Buttons");
    checkHiliteRedunds.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    checkHiliteRedunds.setMargin(new java.awt.Insets(2, 2, 2, 20));
    checkHiliteRedunds.setOpaque(false);
    checkHiliteRedunds.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkHiliteRedundsActionPerformed(evt);
      }
    });

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("Accordion Bass Tool v0.8");

    boardSplitPane.setDividerSize(16);
    boardSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
    boardSplitPane.setOneTouchExpandable(true);

    controlSplitPane.setDividerSize(16);
    controlSplitPane.setAutoscrolls(true);
    controlSplitPane.setOneTouchExpandable(true);
    controlSplitPane.setRightComponent(seqTablePanel);

    toolTabs.addTab("Chord Info", tabChordInfo);
    toolTabs.addTab("Pick Chord", tabChordPicker);
    toolTabs.addTab("Edit Sequence", tabSeqEditor);
    toolTabs.addTab("Pick Scale/Pattern", seqPicker);
    toolTabs.addTab("Options", tabOptions);
    toolTabs.addTab("Pitch Detector", tabPitchDetect);

    controlSplitPane.setLeftComponent(toolTabs);

    boardSplitPane.setLeftComponent(controlSplitPane);
    boardSplitPane.setRightComponent(renderBoardControl);

    getContentPane().add(boardSplitPane, java.awt.BorderLayout.CENTER);

    menuMode.setText("Mode");
    jMenuBar1.add(menuMode);

    menuChords.setText("Sequence");
    jMenuBar1.add(menuChords);

    menuSound.setText("Sound");

    miSoundEnabled.setSelected(true);
    miSoundEnabled.setText("Enable Sound");
    menuSound.add(miSoundEnabled);

    miArpeggiateChord.setSelected(true);
    miArpeggiateChord.setText("Arpeggiate Chord");
    menuSound.add(miArpeggiateChord);

    jMenuBar1.add(menuSound);

    menuPlayback.setText("Playback");

    miPlayStop.setText("Play");
    miPlayStop.setActionCommand("miPlay");
    menuPlayback.add(miPlayStop);

    miStop.setText("Stop");
    menuPlayback.add(miStop);

    jMenuBar1.add(menuPlayback);

    menuOptions.setText("Options");

    miIncludeFingering.setSelected(true);
    miIncludeFingering.setText("Include Fingering");
    menuOptions.add(miIncludeFingering);

    jMenuBar1.add(menuOptions);

    setJMenuBar(jMenuBar1);
  }// </editor-fold>//GEN-END:initComponents

  private void checkHiliteRedundsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkHiliteRedundsActionPerformed
  {//GEN-HEADEREND:event_checkHiliteRedundsActionPerformed
    renderBassBoard.getSelectedButtonCombo().showRedunds = checkHiliteRedunds.isSelected();
    renderBassBoard.repaint();
}//GEN-LAST:event_checkHiliteRedundsActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JSplitPane boardSplitPane;
  private javax.swing.JCheckBox checkHiliteRedunds;
  private javax.swing.JSplitPane controlSplitPane;
  private javax.swing.JMenuBar jMenuBar1;
  private javax.swing.JMenu menuChords;
  private javax.swing.JMenu menuMode;
  private javax.swing.JMenu menuOptions;
  private javax.swing.JMenu menuPlayback;
  private javax.swing.JMenu menuSound;
  private javax.swing.JCheckBoxMenuItem miArpeggiateChord;
  private javax.swing.JCheckBoxMenuItem miIncludeFingering;
  private javax.swing.JMenuItem miPlayStop;
  private javax.swing.JCheckBoxMenuItem miSoundEnabled;
  private javax.swing.JMenuItem miStop;
  private render.RenderBoardControl renderBoardControl;
  private render.SeqPicker seqPicker;
  private render.SeqTablePanel seqTablePanel;
  private render.TabChordInfo tabChordInfo;
  private render.TabChordPicker tabChordPicker;
  private render.TabOptions tabOptions;
  private render.TabPitchDetect tabPitchDetect;
  private render.TabSeqEditor tabSeqEditor;
  private javax.swing.JTabbedPane toolTabs;
  // End of variables declaration//GEN-END:variables
}
