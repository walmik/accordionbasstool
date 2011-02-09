/*
 * RenderBoardControl.java
 *
 * Created on Jan 10, 2011, 6:10:44 PM
 */
package render;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

/**
 *
 * @author Ilya
 */
public class RenderBoardControl extends javax.swing.JPanel
{
  public final RenderBassBoard renderBassBoard;
  public final RenderBoardHeader renderBoardHeader;
  public final JScrollPane scrollPane;

  /** Creates new form RenderBoardControl */
  public RenderBoardControl()
  {
    renderBoardHeader = new render.RenderBoardHeader();
    renderBassBoard = new RenderBassBoard();
    renderBassBoard.setDoubleBuffered(true);

    this.setLayout(new BorderLayout());

    scrollPane = new JScrollPane();

    //scrollPane.setColumnHeaderView(renderBoardHeader);
    scrollPane.setViewportView(renderBassBoard);

    scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, renderBoardHeader.getCornerComp());

    scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
    scrollPane.setBorder(BorderFactory.createEmptyBorder());

    this.add(scrollPane, BorderLayout.CENTER);
    this.add(renderBoardHeader, BorderLayout.NORTH);
  }

  public void toggleOrientation(boolean isHoriz)
  {
    renderBassBoard.setIsHorizontal(isHoriz);
    renderBoardHeader.toggleOrientation(isHoriz);
    renderBassBoard.invalidate();
  }

//  @Override
//  public Dimension getPreferredSize()
//  {
//    Dimension dimy = renderBassBoard.getPreferredSize();
//    dimy.height += renderBoardHeader.getPreferredSize().height;
//    return dimy;
//  }
}
