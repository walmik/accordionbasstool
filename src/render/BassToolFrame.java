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
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import music.BoardRegistry;
import music.BoardRegistry.BoardDef;
import music.ParsedChordDef;
import render.SeqTablePanel.ChordSeqCmd;

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
  JRootPane origRootPane;

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

    origRootPane = this.getRootPane();

    // Save initial full tab layout
    allTools = new ToolPanel[toolTabs.getTabCount()];
    for (int i = 0; i < allTools.length; i++) {
      allTools[i] = (ToolPanel) toolTabs.getComponentAt(i);
      allTools[i].setName(toolTabs.getTitleAt(i));
    }

    renderBassBoard = renderBoardControl.renderBassBoard;
    sound = new SoundController(false);

    init(mode);

    initModeMenu(mode);

    initTabsMenu();

    initOtherMenus();
  }

  public void restoreRootPane()
  {
    this.setRootPane(origRootPane);
  }

  public void init(ToolMode mode)
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

//    if (boardSplitPane.isVisible()) {
//      boardSplitPane.setDividerLocation(boardSplitPane.getMinimumDividerLocation());
//    }

    for (int i = 0; i < menuMode.getItemCount(); i++) {
      JMenuItem item = menuMode.getItem(i);
      if (item.getActionCommand().equals(mode.name())) {
        item.setSelected(true);
      }
    }

    System.gc();
  }

  private void initBoardOnly()
  {
    columnModel = null;
    Vector<BoardDef> allowedBoards = BoardRegistry.mainRegistry().getStandardBoards();

    mouseListener = new BoardMouseListener(renderBassBoard, null, sound);

    RenderBoardHeader header = this.renderBoardControl.renderBoardHeader;
    header.initBoardHeader(renderBassBoard, renderBoardControl, null, allowedBoards);
    header.selectFirstBoardByBassCount(48);

    this.controlSplitPane.setVisible(false);
    this.boardSplitPane.setVisible(false);
    //this.boardSplitPane.setTopComponent(null);
    origRootPane.getContentPane().add(this.renderBoardControl, BorderLayout.CENTER);

    // UI Hiding

    this.menuTabs.setVisible(false);
    this.menuChords.setVisible(false);

    this.menuPlayback.setVisible(false);
    this.menuSound.setVisible(true);
    this.menuMode.setVisible(false);

    this.menuOptions.setVisible(true);
    this.menuOptions.removeAll();
    this.menuOptions.add(this.miOptVertical);
  }

  private void initChordMatcher()
  {
    //if (columnModel == null) {
    columnModel = new SeqColumnModel(renderBassBoard);
    //} else {
    //
    //}
    //sound.setEnabled(checkSoundEnabled.isSelected());

    Vector<BoardDef> allowedBoards = BoardRegistry.mainRegistry().getStandardBoards();

    mouseListener = new BoardMouseListener(renderBassBoard, columnModel, sound);

    tabChordInfo.init(columnModel);

    RenderBoardHeader header = this.renderBoardControl.renderBoardHeader;
    header.initBoardHeader(renderBassBoard, renderBoardControl, columnModel, allowedBoards);
    header.selectFirstBoardByBassCount(48);

    this.controlSplitPane.setVisible(false);
    this.setSplit(boardSplitPane, tabChordInfo, renderBoardControl);

    origRootPane.getContentPane().add(boardSplitPane, BorderLayout.CENTER);

    // Add Default Chord
    columnModel.shrinkToFirst();

    // UI Hiding
    this.menuMode.setVisible(false);
    this.menuTabs.setVisible(false);
    this.menuChords.setVisible(false);

    this.menuPlayback.setVisible(false);
    this.menuSound.setVisible(true);

    this.menuOptions.setVisible(true);
    this.menuOptions.removeAll();
    this.menuOptions.add(this.miOptRedunds);
    this.menuOptions.add(new JSeparator());
    this.menuOptions.add(this.miOptBoardFirst);
    this.menuOptions.add(this.miOptVertical);
  }

  private void initSeqPicker()
  {
    if (columnModel == null) {
      columnModel = new SeqColumnModel(renderBassBoard);
    }

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

    origRootPane.getContentPane().add(boardSplitPane, BorderLayout.CENTER);

    // Add Default Chord
    if (columnModel.getColumnCount() == 0) {
      columnModel.addColumn(ParsedChordDef.newEmptyChordDef(), 0);
    }
  }

  private void initChordPicker()
  {
    if (columnModel == null) {
      columnModel = new SeqColumnModel(renderBassBoard);

      seqTablePanel.init(columnModel, renderBassBoard, sound);
      seqTablePanel.buttonHideEditor.addActionListener(this);
      seqTablePanel.toggleSeqControls(false);

      mouseListener = new BoardMouseListener(renderBassBoard, columnModel, sound);
    }

    toolTabs.setVisible(false);
    tabChordPicker.init(columnModel);

    controlSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);

    RenderBoardHeader header = this.renderBoardControl.renderBoardHeader;
    header.initBoardHeader(renderBassBoard, renderBoardControl, columnModel, null);


    this.setSplit(controlSplitPane, tabChordPicker, seqTablePanel);
    this.setSplit(boardSplitPane, controlSplitPane, renderBoardControl);

    origRootPane.getContentPane().add(boardSplitPane, BorderLayout.CENTER);

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

      seqTablePanel.init(columnModel, renderBassBoard, sound);

      mouseListener = new BoardMouseListener(renderBassBoard, columnModel, sound);

      //renderBoardControl.renderBoardHeader.getExtPanel().add(this.checkHiliteRedunds);

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
    origRootPane.getContentPane().add(boardSplitPane, BorderLayout.CENTER);

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
    double weight = boardSplitPane.getResizeWeight();
    Component left = boardSplitPane.getLeftComponent();
    Component right = boardSplitPane.getRightComponent();
    boardSplitPane.setLeftComponent(null);
    boardSplitPane.setRightComponent(null);
    boardSplitPane.setLeftComponent(right);
    boardSplitPane.setRightComponent(left);
    boardSplitPane.setResizeWeight(1.0 - weight);

    this.computeDividerLocation();
    renderBoardControl.revalidate();

    this.firePropertyChange("prefLayoutChange", null, origRootPane.getPreferredSize());
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

    //boardSplitPane.setResizeWeight(1.0 - boardSplitPane.getResizeWeight());

    renderBoardControl.toggleOrientation(!isHoriz);

    this.computeDividerLocation();

    renderBoardControl.revalidate();

    this.firePropertyChange("prefLayoutChange", null, origRootPane.getPreferredSize());
  }

  public void toggleEditorLeft()
  {
    editorLeft = !editorLeft;

    double weight = controlSplitPane.getResizeWeight();
    Component left = controlSplitPane.getLeftComponent();
    Component right = controlSplitPane.getRightComponent();
    controlSplitPane.setLeftComponent(null);
    controlSplitPane.setRightComponent(null);
    controlSplitPane.setLeftComponent(right);
    controlSplitPane.setRightComponent(left);
    controlSplitPane.setResizeWeight(1.0 - weight);

    seqTablePanel.toggleLeftRight(editorLeft);

//    if (!editorLeft) {
//      controlSplitPane.setDividerLocation(controlSplitPane.getMaximumDividerLocation());
//    } else {
//      controlSplitPane.setDividerLocation(controlSplitPane.getMinimumDividerLocation());
//    }
  }

  private void computeDividerLocation()
  {
    if (true)
      return;
    
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

  void initModeMenu(ToolMode startMode)
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
      JMenuItem item = new JRadioButtonMenuItem(new TabAction(i));
      group.add(item);
      if (i == toolTabs.getSelectedIndex()) {
        item.setSelected(true);
      }
      menuTabs.add(item);
    }
  }

  class SoundToggle extends AbstractAction implements ChangeListener
  {

    JSlider volumeSlider;
    boolean isArpeggiate;

    SoundToggle(JSlider slider, boolean arpegg, String name)
    {
      volumeSlider = slider;
      isArpeggiate = arpegg;
      this.putValue(AbstractAction.NAME, name);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      AbstractButton button = (AbstractButton) e.getSource();
      if (!isArpeggiate) {
        sound.setEnabled(button.isSelected());
      } else {
        sound.setArpeggiating(button.isSelected());
      }
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
      sound.setVolume(volumeSlider.getValue());
    }
  }

  void initOtherMenus()
  {
    menuSound.removeAll();
    SoundCtrlPanel soundCtrl = new SoundCtrlPanel();
    soundCtrl.init(sound);
    menuSound.add(soundCtrl);

    ChordSeqCmd.populateButtons(columnModel, JMenuItem.class, menuChords, 0);

    this.menuPlayback.add(seqTablePanel.actionPlay);
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

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
    menuPlayback = new javax.swing.JMenu();
    menuOptions = new javax.swing.JMenu();
    miOptFingers = new javax.swing.JCheckBoxMenuItem();
    miOptRedunds = new javax.swing.JCheckBoxMenuItem();
    jSeparator1 = new javax.swing.JPopupMenu.Separator();
    miOptVertical = new javax.swing.JCheckBoxMenuItem();
    miOptBoardFirst = new javax.swing.JCheckBoxMenuItem();
    jSeparator2 = new javax.swing.JPopupMenu.Separator();
    miOptGoAll = new javax.swing.JMenuItem();
    menuTabs = new javax.swing.JMenu();

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    setTitle("Accordion Bass Tool v0.9");

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

    menuChords.setText("Chords");
    jMenuBar1.add(menuChords);

    menuSound.setText("Sound");
    jMenuBar1.add(menuSound);

    menuPlayback.setText("Playback");
    jMenuBar1.add(menuPlayback);

    menuOptions.setText("Options");

    miOptFingers.setText("Include Fingering");
    menuOptions.add(miOptFingers);

    miOptRedunds.setText("Press Redundant Buttons");
    miOptRedunds.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        miOptRedundsActionPerformed(evt);
      }
    });
    menuOptions.add(miOptRedunds);
    menuOptions.add(jSeparator1);

    miOptVertical.setText("Board Vertical");
    miOptVertical.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        miOptVerticalActionPerformed(evt);
      }
    });
    menuOptions.add(miOptVertical);

    miOptBoardFirst.setText("Board Top/Left");
    miOptBoardFirst.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        miOptBoardFirstActionPerformed(evt);
      }
    });
    menuOptions.add(miOptBoardFirst);
    menuOptions.add(jSeparator2);

    miOptGoAll.setText("Go to All Options");
    miOptGoAll.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        miOptGoAllActionPerformed(evt);
      }
    });
    menuOptions.add(miOptGoAll);

    jMenuBar1.add(menuOptions);

    menuTabs.setText("Tabs");
    jMenuBar1.add(menuTabs);

    setJMenuBar(jMenuBar1);
  }// </editor-fold>//GEN-END:initComponents

  private void miOptRedundsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miOptRedundsActionPerformed
  {//GEN-HEADEREND:event_miOptRedundsActionPerformed
    renderBassBoard.getSelectedButtonCombo().showRedunds = miOptRedunds.isSelected();
    renderBassBoard.repaint();
  }//GEN-LAST:event_miOptRedundsActionPerformed

  private void miOptGoAllActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miOptGoAllActionPerformed
  {//GEN-HEADEREND:event_miOptGoAllActionPerformed
    toolTabs.setSelectedComponent(tabOptions);
  }//GEN-LAST:event_miOptGoAllActionPerformed

  private void miOptVerticalActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miOptVerticalActionPerformed
  {//GEN-HEADEREND:event_miOptVerticalActionPerformed
    this.toggleOrientation();
  }//GEN-LAST:event_miOptVerticalActionPerformed

  private void miOptBoardFirstActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_miOptBoardFirstActionPerformed
  {//GEN-HEADEREND:event_miOptBoardFirstActionPerformed
    this.toggleBoardPos();
  }//GEN-LAST:event_miOptBoardFirstActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JSplitPane boardSplitPane;
  private javax.swing.JSplitPane controlSplitPane;
  private javax.swing.JMenuBar jMenuBar1;
  private javax.swing.JPopupMenu.Separator jSeparator1;
  private javax.swing.JPopupMenu.Separator jSeparator2;
  private javax.swing.JMenu menuChords;
  private javax.swing.JMenu menuMode;
  private javax.swing.JMenu menuOptions;
  private javax.swing.JMenu menuPlayback;
  private javax.swing.JMenu menuSound;
  private javax.swing.JMenu menuTabs;
  private javax.swing.JCheckBoxMenuItem miOptBoardFirst;
  private javax.swing.JCheckBoxMenuItem miOptFingers;
  private javax.swing.JMenuItem miOptGoAll;
  private javax.swing.JCheckBoxMenuItem miOptRedunds;
  private javax.swing.JCheckBoxMenuItem miOptVertical;
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
