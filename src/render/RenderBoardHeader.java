/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * RenderBoardHeader.java
 *
 * Created on Oct 25, 2010, 12:32:49 PM
 */
package render;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import music.BassBoard;
import music.BoardRegistry;

/**
 *
 * @author Ilya
 */
public class RenderBoardHeader extends javax.swing.JPanel implements ActionListener
{

  RenderBassBoard renderBoard;
  JScrollPane boardScrollPane;
  SeqColumnModel columnModel;
  boolean doGradient;

  /** Creates new form RenderBoardHeader */
  public RenderBoardHeader()
  {
    initComponents();

    boardCombo.addActionListener(this);
  }

  public void init(
          RenderBassBoard renderBoard,
          JScrollPane pane)
  {
    this.renderBoard = renderBoard;
    this.boardScrollPane = pane;
   
    //doGradient = AWTUtilities.isTranslucencySupported(AWTUtilities.Translucency.PERPIXEL_TRANSLUCENT);
    doGradient = true;
  }

  public void setColumnModel(SeqColumnModel model)
  {
    columnModel = model;
  }

  public void setBoardConfig(boolean useAllBoards, int defaultSize)
  {
    DefaultComboBoxModel boardmodel;

    if (!useAllBoards) {
      boardmodel = new DefaultComboBoxModel(BoardRegistry.mainRegistry().getStandardBoards());
    } else {
      boardmodel = new DefaultComboBoxModel(BoardRegistry.mainRegistry().allBoardDefs);
    }

    boardCombo.setModel(boardmodel);

    if (defaultSize > 0) {
      selectFirstBoardByBassCount(defaultSize);
    }
  }

  public boolean selectFirstBoardByBassCount(int bassCount)
  {
    BoardRegistry.BoardDef boardDef = BoardRegistry.mainRegistry().findByBassCount(bassCount);
    if (boardDef != null) {
      boardCombo.setSelectedItem(boardDef);
      return true;
    }

    return false;
  }
  Component hiddenBox;

  public void toggleOrientation(boolean isHoriz)
  {
    if (isHoriz) {
      setLayout(new FlowLayout());
    } else {
      this.jLabel1.setAlignmentX(CENTER_ALIGNMENT);
      this.boardCombo.setAlignmentX(CENTER_ALIGNMENT);
      this.infoLabel.setAlignmentX(CENTER_ALIGNMENT);
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      if (hiddenBox == null) {
        hiddenBox = Box.createVerticalStrut(30);
        add(hiddenBox);
      }
    }
  }

  @Override
  public void paintComponent(Graphics g)
  {
    if (doGradient) {
      paintGradientBack(this, g);
    } else {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
    }
  }

  private static void paintGradientBack(JComponent comp, Graphics g)
  {
    int height = comp.getHeight();
    Graphics2D g2 = (Graphics2D) g;
    Color lightCol = comp.getBackground();//new Color(53,180,209);
    g2.setPaint(new GradientPaint(0.f, height * 2 / 3, lightCol, 0.f, height, Color.black));
    g2.fillRect(0, 0, comp.getWidth(), comp.getHeight());
  }

  public Component getCornerComp()
  {
    JPanel corner = new JPanel()
    {

      @Override
      public void paintComponent(Graphics g)
      {
        paintGradientBack(this, g);
      }
    };
    corner.setBackground(this.getBackground());
    return corner;
  }

  private void autoSize()
  {
    Container cont = this.getTopLevelAncestor();

    // Autoresize -- horizontal
    if ((cont != null) && cont.isVisible() && (cont instanceof JFrame)) {
      JFrame frame = (JFrame) cont;

      if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0) {
        return;
      }

      int diff = 0;
      if (renderBoard.isHorizontal()) {
        diff = (renderBoard.getPreferredSize().height - renderBoard.getHeight());
      } else {
        diff = (renderBoard.getPreferredSize().width - renderBoard.getWidth());
      }

//      if (!boardScrollPane.isValid() || !renderBoard.isValid()) {
//        return;
//      }

      if (renderBoard.isHorizontal()) {
        renderBoard.setSize(renderBoard.getWidth(), renderBoard.getHeight() + diff);
        frame.setSize(frame.getWidth(), frame.getHeight() + diff);
      } else {
        return;
//        renderBoard.setSize(renderBoard.getWidth() + diff, renderBoard.getHeight());
//        frame.setSize(frame.getWidth() + diff, frame.getHeight());
      }

      //     System.out.println("New RB Height: " + (renderBoard.getHeight()));
      //     System.out.println("New Frame Height: " + (frame.getHeight()));
    }

    //boardScrollPane.revalidate();
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == boardCombo) {
      if (renderBoard != null) {
        BoardRegistry.BoardDef def = (BoardRegistry.BoardDef) boardCombo.getSelectedItem();

        BassBoard newBoard = def.createBoard();

        renderBoard.setBassBoard(newBoard);
        boardScrollPane.revalidate();
        //boardScrollPane.doLayout();
        renderBoard.repaint();

        autoSize();

        if (columnModel != null) {
          columnModel.recomputeSeqs();
        }

        String info = "Range: <b>";
        info += newBoard.getMinRootNote().toString(true);
        info += " to ";
        info += newBoard.getMaxRootNote().toString(true);
        info += "</b>";
        this.infoLabel.setText("<html>" + info + "</html>");

        String toolTip = "<html>Current Board: <b>" + def.toString() + "</b>";
        toolTip += "<br/>" + info;

        if (def.desc.length() > 0) {
          toolTip += "<br/><i>" + def.desc + "</i>";
        } else {
          toolTip += "<br/><i>Standard Board Size</i>";
        }

        this.setToolTipText(toolTip);
        jLabel1.setToolTipText(toolTip);
        this.boardCombo.setToolTipText(toolTip);

        //renderBoard.setBoardToolTip(toolTip);
        //this.labelDesc.setText(def.desc);
      }
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

    jLabel1 = new javax.swing.JLabel();
    labelDesc = new javax.swing.JLabel();
    boardCombo = new javax.swing.JComboBox();
    infoLabel = new javax.swing.JLabel();

    setBackground(java.awt.SystemColor.activeCaption);

    jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getSize()+5f));
    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel1.setText("Current Board:");
    add(jLabel1);

    labelDesc.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
    add(labelDesc);

    boardCombo.setFont(boardCombo.getFont().deriveFont(boardCombo.getFont().getStyle() | java.awt.Font.BOLD, boardCombo.getFont().getSize()+7));
    add(boardCombo);

    infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getSize()+5f));
    infoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    infoLabel.setText("info");
    infoLabel.setToolTipText("<html>\nThe chord on the left/bottom of the bass side to<br/>\nThe chord on the right/top of the bass side.<br/>\nHigher basses have a wider ranger of chords.<br/>\n72-bass boards and up have chords for all 12 notes\n</html>");
    add(infoLabel);
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox boardCombo;
  private javax.swing.JLabel infoLabel;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel labelDesc;
  // End of variables declaration//GEN-END:variables
}
