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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.sound.sampled.AudioPermission;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import music.BoardRegistry;

/**
 *
 * @author Ilya
 */
public class BassToolFrame extends javax.swing.JFrame implements PropertyChangeListener, ActionListener
{

  boolean editorVis = true;
  boolean editorLeft = true;
  ModeSelector modeSelector;
  BoardMouseListener mouseListener;

  enum ToolMode
  {

    BUTTON_FOR_CHORD("Single Chord Buttons"),
    CHORD_SEQ("Chord Sequence Buttons"),
    //CHORD_FOR_BUTTON,
    ALL("All");

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
    BoardRegistry.mainRegistry();

    initComponents();

    //toolTabs.remove(tabButtonClicker);
    //toolTabs.remove(tabChordPicker);

    // Init Tabs
    seqTablePanel.initChordPicker(tabChordPicker);
    seqTablePanel.initTextParser(tabSeqEditor);
    tabOptions.setSeqColModel(seqTablePanel.columnModel);

    if (checkPitchDetectPermissions()) {
      tabPitchDetect.setSeqColModel(seqTablePanel.columnModel);
    } else {
      toolTabs.setEnabledAt(toolTabs.indexOfComponent(tabPitchDetect), false);
    }
    
    tabButtonClicker.setSeqColModel(seqTablePanel.columnModel);
    
    mouseListener =
            new BoardMouseListener(renderBassBoard,
            seqTablePanel.columnModel,
            tabButtonClicker,
            seqTablePanel.sound);
    
    renderBassBoard.addMouseListener(mouseListener);
    renderBassBoard.addMouseMotionListener(mouseListener);
    
    //initModeSelector();

    renderBoardHeader.initBoardHeader(
            renderBassBoard,
            renderBoardScrollPane,
            seqTablePanel.columnModel,
            null);

    renderBoardScrollPane.setColumnHeaderView(renderBoardHeader);
    renderBoardScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, renderBoardHeader.getCornerComp());
    renderBoardScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
    renderBoardScrollPane.setBorder(BorderFactory.createEmptyBorder());

    renderBassBoard.setSelectedButtonCombo(seqTablePanel.columnModel.selComboModel);

    seqTablePanel.toggleChordPicker.addActionListener(this);

    controlSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);

    boardSplitPane.setDividerLocation(boardSplitPane.getMinimumDividerLocation());

    // Add Default Chord!
    seqTablePanel.columnModel.addColumn(0);

    pack();
    setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
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
      int newLoc = ((Integer) evt.getNewValue()).intValue();

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
        seqTablePanel.toggleChordPicker.setText(editorLeft ? brakLeft + "Hide Editor" : "Hide Editor" + brakRight);
      } else {
        seqTablePanel.toggleChordPicker.setText(editorLeft ? "Show Editor" + brakRight : brakLeft + "Show Editor");
      }
    }
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == seqTablePanel.toggleChordPicker) {
      if (editorVis) {
        controlSplitPane.setDividerLocation(editorLeft ? 0.0 : 1.0);
      } else {
        controlSplitPane.setDividerLocation(editorLeft ? controlSplitPane.getMinimumDividerLocation() : controlSplitPane.getMaximumDividerLocation());
      }
    }

    if ((modeSelector != null) && (e.getSource() == modeSelector.modeCombo)) {
      switchMode((ToolMode) modeSelector.modeCombo.getSelectedItem());
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

    renderBassBoard.setIsHorizontal(!isHoriz);

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

    renderBoardHeader.toggleOrientation(isHoriz);

//    boardAndHeaderPanel.remove(renderBoardHeader);
//    if (isHoriz && isTopLeft) {
//      boardAndHeaderPanel.add(BorderLayout.SOUTH, renderBoardHeader);
//      renderBoardHeader.flipHeader(true);
//    } else {
//      boardAndHeaderPanel.add(BorderLayout.NORTH, renderBoardHeader);
//      renderBoardHeader.flipHeader(false);
//    }
  }
  private static RenderBassBoard theBoard = null;

  public static RenderBassBoard getRenderBoard()
  {
    if (theBoard == null) {
      theBoard = new RenderBassBoard();
    }

    return theBoard;
  }

  void initModeSelector()
  {
    modeSelector = new ModeSelector();
    JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    flowPanel.add(modeSelector);
    getContentPane().add(flowPanel, java.awt.BorderLayout.NORTH);
    modeSelector.modeCombo.setModel(new DefaultComboBoxModel(ToolMode.values()));
    modeSelector.modeCombo.addActionListener(this);

    modeSelector.checkFingerSearch.setSelected(seqTablePanel.columnModel.optFingerSearch);

    modeSelector.checkFingerSearch.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (seqTablePanel.columnModel != null) {
          seqTablePanel.columnModel.optFingerSearch = (modeSelector.checkFingerSearch.isSelected());
          seqTablePanel.columnModel.recomputeSeqs();
        }
      }
    });

    modeSelector.modeCombo.setSelectedItem(ToolMode.BUTTON_FOR_CHORD);
  }

  void switchMode(ToolMode mode)
  {
    switch (mode) {
      case BUTTON_FOR_CHORD:
        seqTablePanel.toggleSeqControls(false);
        toolTabs.removeAll();
        toolTabs.addTab("Chord Picker", tabChordPicker);
        toolTabs.addTab("Options", tabOptions);
        break;

      case CHORD_SEQ:
        seqTablePanel.toggleSeqControls(true);
        toolTabs.removeAll();
        toolTabs.addTab("Chord Picker", tabChordPicker);
        toolTabs.addTab("Sequence Editor", tabSeqEditor);
        toolTabs.addTab("Options", tabOptions);
        break;

      case ALL:
        seqTablePanel.toggleSeqControls(true);
        toolTabs.removeAll();
        toolTabs.addTab("Chord Picker", tabChordPicker);
        toolTabs.addTab("Sequence Editor", tabSeqEditor);
        toolTabs.addTab("Options", tabOptions);
        if (checkPitchDetectPermissions()) {
          toolTabs.addTab("Pitch Detect", tabPitchDetect);
        }
        //toolTabs.addTab("Tester", tabTester);
        break;
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

    renderBoardHeader = new render.RenderBoardHeader();
    boardSplitPane = new javax.swing.JSplitPane();
    renderBoardScrollPane = new javax.swing.JScrollPane();
    renderBassBoard = getRenderBoard();
    controlSplitPane = new javax.swing.JSplitPane();
    seqTablePanel = new render.SeqTablePanel();
    toolTabs = new javax.swing.JTabbedPane();
    tabButtonClicker = new render.TabButtonClicker();
    tabChordPicker = new render.TabChordPicker();
    tabSeqEditor = new render.TabSeqEditor();
    tabOptions = new render.TabOptions();
    tabPitchDetect = new render.TabPitchDetect();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("Accordion Bass Tool v0.8");

    boardSplitPane.setDividerSize(16);
    boardSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
    boardSplitPane.setOneTouchExpandable(true);

    javax.swing.GroupLayout renderBassBoardLayout = new javax.swing.GroupLayout(renderBassBoard);
    renderBassBoard.setLayout(renderBassBoardLayout);
    renderBassBoardLayout.setHorizontalGroup(
      renderBassBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 1091, Short.MAX_VALUE)
    );
    renderBassBoardLayout.setVerticalGroup(
      renderBassBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 268, Short.MAX_VALUE)
    );

    renderBoardScrollPane.setViewportView(renderBassBoard);

    renderBoardScrollPane.getVerticalScrollBar().setBlockIncrement(24);
    renderBoardScrollPane.getVerticalScrollBar().setUnitIncrement(8);
    boardSplitPane.setBottomComponent(renderBoardScrollPane);

    controlSplitPane.setDividerSize(16);
    controlSplitPane.setAutoscrolls(true);
    controlSplitPane.setOneTouchExpandable(true);
    controlSplitPane.setRightComponent(seqTablePanel);

    toolTabs.addTab("Pick Chord/Notes", tabButtonClicker);
    toolTabs.addTab("Edit Chord", tabChordPicker);
    toolTabs.addTab("Edit Sequence/Scale", tabSeqEditor);
    toolTabs.addTab("Options", tabOptions);
    toolTabs.addTab("Pitch Detector", tabPitchDetect);

    controlSplitPane.setLeftComponent(toolTabs);

    boardSplitPane.setLeftComponent(controlSplitPane);

    getContentPane().add(boardSplitPane, java.awt.BorderLayout.CENTER);

    getAccessibleContext().setAccessibleName("Accordion Bass Tool v0.8");
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JSplitPane boardSplitPane;
  private javax.swing.JSplitPane controlSplitPane;
  private render.RenderBassBoard renderBassBoard;
  private render.RenderBoardHeader renderBoardHeader;
  private javax.swing.JScrollPane renderBoardScrollPane;
  private render.SeqTablePanel seqTablePanel;
  private render.TabButtonClicker tabButtonClicker;
  private render.TabChordPicker tabChordPicker;
  private render.TabOptions tabOptions;
  private render.TabPitchDetect tabPitchDetect;
  private render.TabSeqEditor tabSeqEditor;
  private javax.swing.JTabbedPane toolTabs;
  // End of variables declaration//GEN-END:variables
}
