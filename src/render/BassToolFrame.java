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

import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;

/**
 *
 * @author Ilya
 */
public class BassToolFrame extends javax.swing.JFrame {

    /** Creates new form BassToolFrame */
    public BassToolFrame() {
        initComponents();
    }

    public static RenderBassBoard getRenderBoard()
    {
      return renderBassBoard;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    splitPane = new javax.swing.JSplitPane();
    renderBoardScrollPane = new javax.swing.JScrollPane();
    renderBassBoard = new render.RenderBassBoard();
    toolTabs = new javax.swing.JTabbedPane();
    tabChordPicker1 = new render.TabSeqVisualizer();
    tabCustomBass1 = new render.TabCustomBass();
    tabTester1 = new render.TabTester();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("Accordion Bass Tool v0.1");
    setLocationByPlatform(true);

    splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

    javax.swing.GroupLayout renderBassBoardLayout = new javax.swing.GroupLayout(renderBassBoard);
    renderBassBoard.setLayout(renderBassBoardLayout);
    renderBassBoardLayout.setHorizontalGroup(
      renderBassBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 1280, Short.MAX_VALUE)
    );
    renderBassBoardLayout.setVerticalGroup(
      renderBassBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 298, Short.MAX_VALUE)
    );

    renderBoardScrollPane.setViewportView(renderBassBoard);

    renderBoardScrollPane.getVerticalScrollBar().setBlockIncrement(24);
    renderBoardScrollPane.getVerticalScrollBar().setUnitIncrement(8);
    splitPane.setRightComponent(renderBoardScrollPane);

    toolTabs.addTab("Simple Chord Visualizer", tabChordPicker1);
    toolTabs.addTab("Advanced Chord Visualizer", tabCustomBass1);

    javax.swing.GroupLayout tabTester1Layout = new javax.swing.GroupLayout(tabTester1);
    tabTester1.setLayout(tabTester1Layout);
    tabTester1Layout.setHorizontalGroup(
      tabTester1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 973, Short.MAX_VALUE)
    );
    tabTester1Layout.setVerticalGroup(
      tabTester1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 292, Short.MAX_VALUE)
    );

    toolTabs.addTab("Tester", tabTester1);

    splitPane.setTopComponent(toolTabs);

    getContentPane().add(splitPane, java.awt.BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private static render.RenderBassBoard renderBassBoard;
  private javax.swing.JScrollPane renderBoardScrollPane;
  private javax.swing.JSplitPane splitPane;
  private render.TabSeqVisualizer tabChordPicker1;
  private render.TabCustomBass tabCustomBass1;
  private render.TabTester tabTester1;
  private javax.swing.JTabbedPane toolTabs;
  // End of variables declaration//GEN-END:variables

}
