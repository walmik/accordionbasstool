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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
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

  /** Creates new form BassToolFrame */
  public BassToolFrame()
  {
    BoardRegistry.mainRegistry();

    initComponents();

    if (editorLeft) {
      controlSplitPane.setLeftComponent(toolTabs);
      controlSplitPane.setRightComponent(seqTablePanel);
    } else {
      controlSplitPane.setLeftComponent(seqTablePanel);
      controlSplitPane.setRightComponent(toolTabs);
    }

    seqTablePanel.initChordPicker(this.chordPicker1);
    seqTablePanel.initTextParser(this.textParserPanel1);

    tabOptions1.setSeqColModel(seqTablePanel.columnModel);

    renderBoardHeader1.initBoardHeader(renderBassBoard, renderBoardScrollPane, seqTablePanel.columnModel);
    renderBoardScrollPane.setColumnHeaderView(renderBoardHeader1);
    renderBoardScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
    renderBoardScrollPane.setBorder(BorderFactory.createEmptyBorder());
    renderBassBoard.setSelectedButtonCombo(seqTablePanel.columnModel.selComboModel);

    setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);

    seqTablePanel.toggleChordPicker.addActionListener(this);

    controlSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);

    this.boardSplitPane.setDividerLocation(boardSplitPane.getMinimumDividerLocation());

    if (!editorLeft) {
      this.controlSplitPane.setDividerLocation(controlSplitPane.getMaximumDividerLocation());
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
    if (editorVis) {
      controlSplitPane.setDividerLocation(editorLeft ? 0.0 : 1.0);
    } else {
      controlSplitPane.setDividerLocation(editorLeft ? controlSplitPane.getMinimumDividerLocation() : controlSplitPane.getMaximumDividerLocation());
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

    renderBoardHeader1.toggleOrientation(isHoriz);

//    boardAndHeaderPanel.remove(renderBoardHeader1);
//    if (isHoriz && isTopLeft) {
//      boardAndHeaderPanel.add(BorderLayout.SOUTH, renderBoardHeader1);
//      renderBoardHeader1.flipHeader(true);
//    } else {
//      boardAndHeaderPanel.add(BorderLayout.NORTH, renderBoardHeader1);
//      renderBoardHeader1.flipHeader(false);
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

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    boardSplitPane = new javax.swing.JSplitPane();
    boardAndHeaderPanel = new javax.swing.JPanel();
    renderBoardScrollPane = new javax.swing.JScrollPane();
    renderBassBoard = getRenderBoard();
    renderBoardHeader1 = new render.RenderBoardHeader();
    controlSplitPane = new javax.swing.JSplitPane();
    seqTablePanel = new render.SeqTablePanel();
    toolTabs = new javax.swing.JTabbedPane();
    chordPicker1 = new render.ChordPicker();
    textParserPanel1 = new render.TextParserPanel();
    tabOptions1 = new render.TabOptions();
    tabTester1 = new render.TabTester();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("Accordion Bass Tool v0.5");

    boardSplitPane.setDividerSize(10);
    boardSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

    boardAndHeaderPanel.setLayout(new java.awt.BorderLayout());

    javax.swing.GroupLayout renderBassBoardLayout = new javax.swing.GroupLayout(renderBassBoard);
    renderBassBoard.setLayout(renderBassBoardLayout);
    renderBassBoardLayout.setHorizontalGroup(
      renderBassBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 1316, Short.MAX_VALUE)
    );
    renderBassBoardLayout.setVerticalGroup(
      renderBassBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 334, Short.MAX_VALUE)
    );

    renderBoardScrollPane.setViewportView(renderBassBoard);

    renderBoardScrollPane.getVerticalScrollBar().setBlockIncrement(24);
    renderBoardScrollPane.getVerticalScrollBar().setUnitIncrement(8);
    boardAndHeaderPanel.add(renderBoardScrollPane, java.awt.BorderLayout.CENTER);
    boardAndHeaderPanel.add(renderBoardHeader1, java.awt.BorderLayout.PAGE_START);

    boardSplitPane.setRightComponent(boardAndHeaderPanel);

    controlSplitPane.setDividerSize(16);
    controlSplitPane.setAutoscrolls(true);
    controlSplitPane.setOneTouchExpandable(true);
    controlSplitPane.setRightComponent(seqTablePanel);

    toolTabs.addTab("Chord Picker", chordPicker1);
    toolTabs.addTab("Full Sequence Editor", textParserPanel1);
    toolTabs.addTab("Options", tabOptions1);

    javax.swing.GroupLayout tabTester1Layout = new javax.swing.GroupLayout(tabTester1);
    tabTester1.setLayout(tabTester1Layout);
    tabTester1Layout.setHorizontalGroup(
      tabTester1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 594, Short.MAX_VALUE)
    );
    tabTester1Layout.setVerticalGroup(
      tabTester1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 252, Short.MAX_VALUE)
    );

    toolTabs.addTab("Tester", tabTester1);

    controlSplitPane.setLeftComponent(toolTabs);

    boardSplitPane.setLeftComponent(controlSplitPane);

    getContentPane().add(boardSplitPane, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel boardAndHeaderPanel;
  private javax.swing.JSplitPane boardSplitPane;
  private render.ChordPicker chordPicker1;
  private javax.swing.JSplitPane controlSplitPane;
  private render.RenderBassBoard renderBassBoard;
  private render.RenderBoardHeader renderBoardHeader1;
  private javax.swing.JScrollPane renderBoardScrollPane;
  private render.SeqTablePanel seqTablePanel;
  private render.TabOptions tabOptions1;
  private render.TabTester tabTester1;
  private render.TextParserPanel textParserPanel1;
  private javax.swing.JTabbedPane toolTabs;
  // End of variables declaration//GEN-END:variables
}
