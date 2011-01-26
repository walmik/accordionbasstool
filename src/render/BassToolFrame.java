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
import javax.sound.sampled.AudioPermission;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import music.BoardRegistry;
import render.SeqTablePanel.ChordSeqCmd;

/**
 *
 * @author Ilya
 */
public class BassToolFrame extends javax.swing.JFrame implements PropertyChangeListener, ActionListener
{

  boolean editorVis = true;
  boolean editorLeft = true;
  RenderBassBoard renderBassBoard;
  BoardMouseListener mouseListener;
  SeqColumnModel columnModel = null;
  SoundController sound;
  SeqAnimController anim;
  ToolPanel[] allTools;
  JRootPane origRootPane;
  boolean modeMenuEnabled;

  /** Creates new form BassToolFrame */
  public BassToolFrame()
  {
    this(ToolMode.findMode("Default"));
  }

  public BassToolFrame(ToolMode mode)
  {
    this(mode, true);
  }

  public BassToolFrame(ToolMode mode, boolean modeEnabled)
  {
    BoardRegistry.mainRegistry();

    initComponents();

    modeMenuEnabled = modeEnabled;

    origRootPane = this.getRootPane();

    // Save initial full tab layout
    allTools = new ToolPanel[toolTabs.getTabCount()];
    for (int i = 0; i < allTools.length; i++) {
      allTools[i] = (ToolPanel) toolTabs.getComponentAt(i);
      allTools[i].setName(toolTabs.getTitleAt(i));
    }

    renderBassBoard = renderBoardControl.renderBassBoard;
    renderBoardControl.renderBoardHeader.init(renderBassBoard, renderBoardControl);

    columnModel = new SeqColumnModel(renderBassBoard);
    sound = new SoundController(false);
    mouseListener = new BoardMouseListener(renderBassBoard, columnModel, sound);

    seqTablePanel.init(columnModel, renderBassBoard, sound);
    seqTablePanel.buttonHideEditor.addActionListener(this);

    controlSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);
    tabOptions.addPropertyChangeListener(this);

    initModeMenu(mode);

    init(mode);

    initTabsMenu();

    initOtherMenus();
  }

  public void init(ToolMode tool)
  {
    SeqColumnModel currColModel;

    if (tool.useModel) {
      currColModel = columnModel;
      renderBassBoard.setSelListeners(currColModel.selComboModel, currColModel.getRowSelModel());
    } else {
      currColModel = null;
      renderBassBoard.setSelListeners(null, null);
    }

    renderBoardControl.renderBoardHeader.setBoardConfig(currColModel, tool.useAllBoards, tool.startBoardSize);

    JComponent table;

    if (tool.useTable) {
      table = seqTablePanel;
      seqTablePanel.toggleSeqControls(tool.multiChord);
    } else {
      table = null;
    }

    if (tool.useMouse) {
      mouseListener.setColumnModel(currColModel, tool.useBlankChord);
      renderBassBoard.setMainMouseAdapter(mouseListener);
    } else {
      renderBassBoard.setMainMouseAdapter(null);
    }

    // Init Tabs
    toolTabs.removeAll();

    JComponent editor = null;

    if (tool.tabs.length == 0) {
      editor = null;
    } else if ((tool.tabs.length == 1) && (tool.tabs[0].equalsIgnoreCase("all"))) {
      // If special name "all" add all tabs
      for (ToolPanel tab : allTools) {
        tab.init(currColModel);
        toolTabs.add(tab.getName(), tab);
      }
      editor = toolTabs;
    } else if ((tool.tabs.length == 1)) {
      // If only one tab, use tool directly
      for (ToolPanel tab : allTools) {
        if (tab.getClass().getSimpleName().equals(tool.tabs[0])) {
          tab.init(currColModel);
          editor = tab;
          break;
        }
      }
    } else {

      for (String tabName : tool.tabs) {
        for (ToolPanel tab : allTools) {
          if (tab.getClass().getSimpleName().equals(tabName)) {
            tab.init(currColModel);
            toolTabs.add(tabName, tab);
            break;
          }
        }
      }

      editor = toolTabs;
    }

    toolTabs.setVisible(toolTabs.getTabCount() > 0);

    if ((tabPitchDetect.getParent() == toolTabs) && !checkPitchDetectPermissions()) {
      toolTabs.setEnabledAt(toolTabs.indexOfComponent(tabPitchDetect), false);
    }

    if (currColModel != null) {
      currColModel.shrinkToFirst(tool.useBlankChord);
    }

    JComponent top = null;

    if ((editor != null) && (table != null)) {
      this.setSplit(controlSplitPane, editor, table);
      top = controlSplitPane;
    } else {
      controlSplitPane.setVisible(false);
      if (editor != null) {
        top = editor;
      } else if (table != null) {
        top = table;
      }
    }

    JComponent center;

    if (top != null) {
      this.setSplit(boardSplitPane, top, renderBoardControl);
      center = boardSplitPane;
    } else {
      boardSplitPane.setVisible(false);
      center = renderBoardControl;
    }

    origRootPane.getContentPane().add(center, BorderLayout.CENTER);
    pack();

//    setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);

//    if (boardSplitPane.isVisible()) {
//      boardSplitPane.setDividerLocation(boardSplitPane.getMinimumDividerLocation());
//    }

//    boolean showAllMenus = ((tool.menus.length == 1) && tool.menus[0].equalsIgnoreCase("all"));
//
//    for (int i = 0; i < menuBar.getMenuCount(); i++)
//    {
//      JMenu menu = menuBar.getMenu(i);
//      boolean visible = showAllMenus;
//
//      if (!visible) {
//        for (String str : tool.menus) {
//          if (str.equals(menu.getName())) {
//            visible = true;
//            break;
//          }
//        }
//      }
//      menu.setVisible(visible);
//    }

    menuSound.setVisible(true);

    menuChords.setVisible(tool.multiChord);
    menuPlayback.setVisible(tool.multiChord);
    menuTranspose.setVisible(tool.multiChord);

    menuOptions.removeAll();

    if (currColModel != null) {
      menuOptions.add(this.miOptFingers);
      menuOptions.add(this.miOptRedunds);
    }

    if (menuOptions.getMenuComponentCount() > 0) {
      menuOptions.add(this.jSeparator1);
    }

    menuOptions.add(this.miOptVertical);

    if (this.boardSplitPane.isVisible() || this.controlSplitPane.isVisible()) {
      menuOptions.add(this.miOptBoardFirst);
    }

    menuOptions.add(this.jSeparator2);

    if (toolTabs.isVisible() && (tabOptions.getParent() == toolTabs)) {
      menuOptions.add(this.miOptGoAll);
    }

    menuTabs.setVisible(toolTabs.isVisible());

    menuMode.setVisible(modeMenuEnabled);

    if (modeMenuEnabled) {
      for (int i = 0; i < menuMode.getItemCount(); i++) {
        JMenuItem item = menuMode.getItem(i);
        if (item.getActionCommand().equals(tool.id)) {
          item.setSelected(true);
        }
      }
    }
  }

  public void restoreRootPane()
  {
    this.setRootPane(origRootPane);
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
  }

  public void toggleBoardPos()
  {
    //double weight = boardSplitPane.getResizeWeight();
    Component left = boardSplitPane.getLeftComponent();
    Component right = boardSplitPane.getRightComponent();
    boardSplitPane.setLeftComponent(null);
    boardSplitPane.setRightComponent(null);
    boardSplitPane.setLeftComponent(right);
    boardSplitPane.setRightComponent(left);
    //boardSplitPane.setResizeWeight(1.0 - weight);
    origRootPane.revalidate();

    this.computeDividerLocation();

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

    //origRootPane.revalidate();
    pack();

    this.computeDividerLocation();

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
    boolean isTopLeft = (boardSplitPane.getLeftComponent() == renderBoardControl);
    //boolean isHoriz = renderBassBoard.isHorizontal();

    if (isTopLeft) {
      boardSplitPane.setDividerLocation(boardSplitPane.getMaximumDividerLocation());
    } else {
      boardSplitPane.setDividerLocation(boardSplitPane.getMinimumDividerLocation());
    }

    // Must readjust editors as well..
    if (!controlSplitPane.isVisible()) {
      return;
    }

    if (!editorLeft) {
      controlSplitPane.setDividerLocation(controlSplitPane.getMaximumDividerLocation());
    } else {
      controlSplitPane.setDividerLocation(controlSplitPane.getMinimumDividerLocation());
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
        return theMode.id;
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
    if (!modeMenuEnabled) {
      return;
    }

    ButtonGroup group = new ButtonGroup();

    for (ToolMode mode : ToolMode.getAllModes()) {
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

    for (int i = 0; i
            < toolTabs.getTabCount(); i++) {
      JMenuItem item = new JRadioButtonMenuItem(new TabAction(i));
      group.add(item);


      if (i == toolTabs.getSelectedIndex()) {
        item.setSelected(true);
      }
      menuTabs.add(item);
    }
  }

  void initOtherMenus()
  {
    // Sound Menu
    SoundCtrlPanel soundCtrl = new SoundCtrlPanel();
    soundCtrl.init(sound);
    menuSound.add(soundCtrl);

    // Chords Menu
    ChordSeqCmd.populateButtons(columnModel, JMenuItem.class, menuChords, 0);

    // Playback Menu
    this.menuPlayback.add(seqTablePanel.actionPlay);

    // Transpose Menu
    TransposePanel trans = new TransposePanel();
    trans.init(columnModel);
    menuTranspose.add(trans);
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
    theMenuBar = new javax.swing.JMenuBar();
    menuMode = new javax.swing.JMenu();
    menuSound = new javax.swing.JMenu();
    menuChords = new javax.swing.JMenu();
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
    menuTranspose = new javax.swing.JMenu();

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
    menuMode.setName("Mode"); // NOI18N
    theMenuBar.add(menuMode);

    menuSound.setText("Sound");
    menuSound.setName("Sound"); // NOI18N
    theMenuBar.add(menuSound);

    menuChords.setText("Chords");
    menuChords.setName("Chords"); // NOI18N
    theMenuBar.add(menuChords);

    menuPlayback.setText("Playback");
    menuPlayback.setName("Playback"); // NOI18N
    theMenuBar.add(menuPlayback);

    menuOptions.setText("Options");
    menuOptions.setName("Options"); // NOI18N

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

    theMenuBar.add(menuOptions);

    menuTabs.setText("Tabs");
    menuTabs.setName("Tabs"); // NOI18N
    theMenuBar.add(menuTabs);

    menuTranspose.setText("Transpose");
    theMenuBar.add(menuTranspose);

    setJMenuBar(theMenuBar);
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
  private javax.swing.JPopupMenu.Separator jSeparator1;
  private javax.swing.JPopupMenu.Separator jSeparator2;
  private javax.swing.JMenu menuChords;
  private javax.swing.JMenu menuMode;
  private javax.swing.JMenu menuOptions;
  private javax.swing.JMenu menuPlayback;
  private javax.swing.JMenu menuSound;
  private javax.swing.JMenu menuTabs;
  private javax.swing.JMenu menuTranspose;
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
  private javax.swing.JMenuBar theMenuBar;
  private javax.swing.JTabbedPane toolTabs;
  // End of variables declaration//GEN-END:variables
}
