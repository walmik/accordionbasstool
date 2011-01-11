/*
 * RenderBoardControl.java
 *
 * Created on Jan 10, 2011, 6:10:44 PM
 */
package render;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

/**
 *
 * @author Ilya
 */
public class RenderBoardControl extends javax.swing.JScrollPane
{
  private RenderBassBoard renderBassBoard;
  private RenderBoardHeader renderBoardHeader;

  /** Creates new form RenderBoardControl */
  public RenderBoardControl()
  {
    renderBoardHeader = new render.RenderBoardHeader();
    renderBassBoard = RenderBassBoard.getStaticRenderBoard();

//    add(renderBoardHeader);
//    add(renderBassBoard);

    setColumnHeaderView(renderBoardHeader);
    setViewportView(renderBassBoard);

    setCorner(JScrollPane.UPPER_RIGHT_CORNER, renderBoardHeader.getCornerComp());

    setViewportBorder(BorderFactory.createEmptyBorder());
    setBorder(BorderFactory.createEmptyBorder());
  }

  public RenderBoardHeader getHeader()
  {
    return renderBoardHeader;
  }

  public void toggleOrientation(boolean isHoriz)
  {
    renderBassBoard.setIsHorizontal(isHoriz);
    renderBoardHeader.toggleOrientation(isHoriz);
  }
}
