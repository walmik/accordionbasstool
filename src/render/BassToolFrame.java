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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
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
  RenderBassBoard renderBassBoard;
 
  /** Creates new form BassToolFrame */
  public BassToolFrame()
  {
    BoardRegistry.mainRegistry();

    initComponents();

    renderBassBoard = renderBoardControl.renderBassBoard;

    SeqColumnModel columnModel = new SeqColumnModel(renderBassBoard);

    seqTablePanel.init(columnModel, renderBassBoard);

    renderBoardControl.renderBoardHeader.
            initBoardHeader(renderBassBoard, renderBoardControl, columnModel, null);

    // Init Tabs
    for (int i = 0; i < toolTabs.getTabCount(); i++) {
      Component tab = toolTabs.getComponentAt(i);
      if (tab instanceof ToolPanel) {
        ((ToolPanel)tab).init(columnModel);
      }
    }

    if (!checkPitchDetectPermissions()) {
      toolTabs.setEnabledAt(toolTabs.indexOfComponent(tabPitchDetect), false);
    }

    mouseListener = new BoardMouseListener(renderBassBoard, columnModel, seqTablePanel.sound);

    renderBoardControl.renderBoardHeader.getExtPanel().add(this.checkHiliteRedunds);

    seqTablePanel.toggleChordPicker.addActionListener(this);

    controlSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);

    boardSplitPane.setDividerLocation(boardSplitPane.getMinimumDividerLocation());


    // Setup intiial state

    // Set Board to default 120
    renderBoardControl.renderBoardHeader.selectFirstBoardByBassCount(120);

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

//    boardAndHeaderPanel.remove(renderBoardHeader);
//    if (isHoriz && isTopLeft) {
//      boardAndHeaderPanel.add(BorderLayout.SOUTH, renderBoardHeader);
//      renderBoardHeader.flipHeader(true);
//    } else {
//      boardAndHeaderPanel.add(BorderLayout.NORTH, renderBoardHeader);
//      renderBoardHeader.flipHeader(false);
//    }
  }

 enum ToolMode
 {
    BoardOnly("Accordion Board Only"),

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
