package render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;

import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import music.BassBoard;
import music.ButtonComboSequence;
import music.Chord;
import render.RenderBoardUI.BoardButtonImage;

public class RenderBassBoard extends JPanel
{

  final static long serialVersionUID = 1;
  BassBoard _theBoard;
  // Margins & Borders
  Dimension margin = new Dimension();
  int borderWidth = 16;
  int boardRoundCornerLength = 40;
  Dimension _contentDim = new Dimension(0, 0);
  Insets _borderInsets = null;
  final Point initialOff = new Point();
  final Point pad = new Point();
  // Draw Controls
  private boolean _isHoriz = false;
  double _colToRow = 0;
  double _slantAngle = 0;
  int _rows = 0, _cols = 0;
  int _rInc = 0, _cInc = 0;
  double _slope = 0;
  int _cStart = 0;
  // Selectors and Drawers
  SelectedButtonCombo _selCombo = null;
  ListSelectionModel _rowSel = null;
  RenderBoardUI.IconButtonDrawer buttonDrawer;
  RenderBoardUI.TextDrawer textDrawer;
  // Mouse
  BassBoard.Pos clickPos = null;
  boolean isDragging = false;
  MouseAdapter mainMouseAdapter = null;
  // Repainting
  RepaintListener colRepainter, rowRepainter;
  boolean computeNeeded = true;
  // Tooltip
  BassBoard.Pos tooltipPos = null;
  String lastToolTip = null;
  //Labels
  boolean isDrawHilites = true;
  int labelRowWidth = 30;
  int labelColHeight = 20;
  int labelFontHeight = 0;
  Color labelBg = new Color(0, 0, 0, 0xaf);

  public RenderBassBoard()
  {
    this(BassBoard.bassBoard32());
  }

  public RenderBassBoard(BassBoard newBoard)
  {
    _selCombo = new SelectedButtonCombo();

    setBackground(new Color(240, 240, 240));

    buttonDrawer = RenderBoardUI.defaultUI.getButtonDrawer();
    textDrawer = RenderBoardUI.defaultUI.getTextDrawer();

    this._colToRow = RenderBoardUI.defaultUI._colToRow;
    this._isHoriz = RenderBoardUI.defaultUI._isHoriz;
    this._slantAngle = RenderBoardUI.defaultUI._defaultSlantAngle;

    ToolTipManager.sharedInstance().registerComponent(this);

    int borderMargin = RenderBoardUI.defaultUI.buttonXMargin + borderWidth / 2;
    borderMargin += labelRowWidth;
    this._borderInsets = new Insets(borderMargin, borderMargin, borderMargin, borderMargin);
//    this._borderInsets.left += labelRowWidth;
//    this._borderInsets.top += labelColHeight;

    colRepainter = new RepaintListener();
    rowRepainter = new RepaintListener();

    this.addComponentListener(new ComponentAdapter()
    {

      @Override
      public void componentResized(ComponentEvent e)
      {
        computeNeeded = true;
        repaint();
      }
    });

    setBassBoard(newBoard);
  }

  public void setMainMouseAdapter(MouseAdapter mouse)
  {
    if (mainMouseAdapter != null) {
      removeMouseListener(mainMouseAdapter);
      removeMouseMotionListener(mainMouseAdapter);
    }

    mainMouseAdapter = mouse;

    if (mainMouseAdapter != null) {
      addMouseListener(mainMouseAdapter);
      addMouseMotionListener(mainMouseAdapter);
    }
  }

  @Override
  public JToolTip createToolTip()
  {
    JToolTip tooltip = super.createToolTip();
    tooltip.setFont(tooltip.getFont().deriveFont(16.f));
    return tooltip;
  }

  @Override
  public String getToolTipText(MouseEvent event)
  {
    BassBoard.Pos mouseOverPos = hitTest(event.getX(), event.getY());

    if (BassBoard.posEquals(mouseOverPos, tooltipPos)) {
      return lastToolTip;
    }

    tooltipPos = mouseOverPos;

    if (mouseOverPos != null) {
      String string = "<html>";
      string += "<b>" + _theBoard.getChordName(mouseOverPos, true) + "</b>";
      if (!_theBoard.isSingleBassRow(mouseOverPos.row)) {
        string += " - (";
        string += _theBoard.getChordAt(mouseOverPos).toHtmlString();
        string += ")";
      }
      string += "<br/>";
//      string += "Row: " + "<b>" + _theBoard.getRow(mouseOverPos.row).toString() + "</b>";
//      string += "<br/>";
//      string += "Column: " + "<b>" + _theBoard.getNoteAt(mouseOverPos.col) + "</b>";
      string += "<i>" + _theBoard.getNoteAt(mouseOverPos.col) + " ";
      string += _theBoard.getRow(mouseOverPos.row).toString() + "</i>";
      string += "</html>";
      lastToolTip = string;
    } else {
      lastToolTip = null;
    }

    return lastToolTip;
  }

  public void setBassBoard(BassBoard newBoard)
  {
    BassBoard oldBoard = _theBoard;
    _theBoard = newBoard;
    if (_theBoard != null) {
      _rows = _theBoard.getNumRows();
      _cols = _theBoard.getNumCols();
    }
    computeNeeded = true;
    this.firePropertyChange(BassBoard.class.getSimpleName(), oldBoard, newBoard);
  }

  public BassBoard getBassBoard()
  {
    return _theBoard;
  }

  class RepaintListener extends ListSelChangeListener
  {

    RepaintListener()
    {
      super(true);
    }

    @Override
    public void selectionChanged(int index)
    {
      repaint();
    }
  }

  public void setSelListeners(SelectedButtonCombo newSel, ListSelectionModel rowSel)
  {

    if (_selCombo != null) {
      _selCombo.removeListSelectionListener(colRepainter);
    }

    if (_rowSel != null) {
      _rowSel.removeListSelectionListener(rowRepainter);
    }

    _selCombo = newSel;
    _rowSel = rowSel;

    if (_selCombo != null) {
      _selCombo.addListSelectionListener(colRepainter);
    }

    if (_rowSel != null) {
      _rowSel.addListSelectionListener(rowRepainter);
    }

    repaint();
  }

  public SelectedButtonCombo getSelectedButtonCombo()
  {
    return _selCombo;
  }

  public void setSelectedSeq(ButtonComboSequence seq)
  {
    _selCombo.setButtonComboSeq(seq);
  }

  public void setSelectedSeqCombo(int index)
  {
    _selCombo.setSelectionInterval(index, index);
  }

//	public void addDrawer(DrawBass drawer)
//	{
//		drawers.add(drawer);
//	}
  public void setIsHorizontal(boolean horiz)
  {
    if (horiz == _isHoriz) {
      return;
    }
    _slantAngle = -_slantAngle;
    _isHoriz = horiz;
    computeNeeded = true;
    repaint();
  }

  public boolean isHorizontal()
  {
    return _isHoriz;
  }

  public void setClickPos(MouseEvent e, boolean dragging)
  {
    BassBoard.Pos newClickPos = hitTest(e);
    setClickPos(newClickPos, dragging);
  }

  public void setClickPos(BassBoard.Pos newClickPos, boolean dragging)
  {
    boolean changedPos = !BassBoard.posEquals(clickPos, newClickPos);

    if (changedPos) {
      if (isDrawHilites) {
        isDragging = dragging;
        clickPos = newClickPos;
        repaint();
      } else {

        if (clickPos != null) {
          BassBoard.Pos oldPos = clickPos;
          clickPos = null;
          this.repaintPos(oldPos, null, true);
        }

        isDragging = dragging;
        clickPos = newClickPos;

        if (clickPos != null) {
          this.repaintPos(clickPos,
                  (isDragging ? BoardButtonImage.FAST_CLICK
                  : BoardButtonImage.HOVER), true);
        }
      }

    } else if ((dragging != isDragging) && (clickPos != null)) {
      isDragging = dragging;
      this.repaintPos(clickPos,
              (isDragging ? BoardButtonImage.FAST_CLICK
              : BoardButtonImage.HOVER), true);
    }
  }

  public void clearClickPos(boolean erase)
  {
    if (erase && (clickPos != null)) {
      BassBoard.Pos oldPos = clickPos;
      clickPos = null;
      this.repaintPos(oldPos, null, true);
    }

    isDragging = false;
    clickPos = null;
  }

  public Chord getClickedChord()
  {
    if (clickPos != null) {
      return getBassBoard().getChordAt(clickPos);
    } else {
      return null;
    }
  }

  public BassBoard.Pos hitTest(MouseEvent e)
  {
    return hitTest(e.getX(), e.getY());
  }

  public BassBoard.Pos hitTest(int x, int y)
  {
    int row, col;
    int cPos, rPos;

    computeRenderOffsets();

    x -= _borderInsets.left;
    y -= _borderInsets.top;

    if (this.getAlignmentX() == JComponent.CENTER_ALIGNMENT) {
      x -= (getWidth() - _contentDim.width - margin.width) / 2;
    }

    if (_isHoriz) {
      cPos = x;
      rPos = y;
    } else {
      cPos = y;
      rPos = x;
    }

    rPos -= margin.height / 2;

    row = rPos / _rInc;
    if (rPos < 0) {
      row = -1;
    }
    cPos -= (_cStart + row * _slope);
    col = (cPos + _cInc) / _cInc - 1;

    if (!_isHoriz) {
      col = _cols - col - 1;
      row = _rows - row - 1;
    }

    if ((row < _rows) && (col < _cols) && (row >= 0) && (col >= 0)) {
      //System.out.println(_theBoard.getChordAt(row, col));
      return new BassBoard.Pos(row, col);
    } else {
      //System.out.println("Row: " + row + " Col: " + col);
      return null;
    }
  }

  private void computeRenderOffsets()
  {
    if (!computeNeeded) {
      return;
    }

    computeNeeded = false;

    _rInc = 0;
    _cInc = 0;

    _slope = 0;

    int w, h;

    margin.width = 3 * RenderBoardUI.defaultUI.buttonXMargin;
    margin.height = 2 * RenderBoardUI.defaultUI.buttonXMargin;

    int usedWidth = getWidth();
    int usedHeight = getHeight();

    w = usedWidth - (_borderInsets.left + _borderInsets.right) - margin.width;
    h = usedHeight - (_borderInsets.top + _borderInsets.bottom) - margin.height;

    if (!_isHoriz) {
      int t = w;
      w = h;
      h = t;
    }

    double theTan = Math.abs(Math.tan(_slantAngle));

    double ratio = ((double) _cols / (double) _rows) * _colToRow;

    double theB = (w / (theTan + ratio));

    if (theB > h) {
      int divs = h / _rows;
      _slope = (theTan * divs);
      _rInc = divs;
      _cInc = (int) (divs * _colToRow);
    } else {
      double theA = theB * theTan;
      double divs = ((w - theA) / _cols);
      _slope = (theA / _rows);
      _cInc = (int) divs;
      _rInc = (int) (divs / _colToRow);
    }

    _cStart = 0;

    if (_slantAngle < 0) {
      _cStart += _rows * _slope;
      _slope = -_slope;
    }

    if (_isHoriz) {
      _contentDim.width = (_cInc * _cols) + (int) (_slope * _rows);
      _contentDim.height = (_rInc * _rows) + margin.height;
    } else {
      _contentDim.width = (_rInc * _rows) + margin.height;
      _contentDim.height = (_cInc * _cols) - (int) (_slope * _rows);
    }

    initialOff.x = 0;

    if (this.getAlignmentX() == JComponent.CENTER_ALIGNMENT) {
      initialOff.x += (getWidth() - labelRowWidth - _contentDim.width - margin.width) / 2;
    }

    initialOff.y = 0;

    pad.x = _borderInsets.left + margin.width / 2;
    pad.y = _borderInsets.top + margin.height / 2;
  }

  @Override
  public void paintComponent(Graphics graphics)
  {
    Graphics2D graphics2D = (Graphics2D) graphics;

    switch (repaintInfo.type) {
      case NONE:
        break;

      case BUTTON:
        if (paintedSingleButton(graphics2D)) {
          return;
        } else {
          break;
        }

//      case ROW_LABEL:
//        this.paintBassRowHeader(graphics2D, repaintInfo.row);
//        return;
    }

    computeRenderOffsets();

    //if (labelRecomputeNeeded) {
    //this.computeBassRowRects(graphics2D);
    //labelRecomputeNeeded = false;
    //}

    Color outer = Color.black;
    Color inner = Color.gray;

    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    graphics2D.setPaint(outer);
    graphics2D.fillRect(0, 0, getWidth(), getHeight());

    graphics2D.translate(initialOff.x, initialOff.y);

    // Paint Border & Background
    RoundRectangle2D roundRect =
            new RoundRectangle2D.Float(_borderInsets.left, _borderInsets.top,
            _contentDim.width, _contentDim.height, boardRoundCornerLength, boardRoundCornerLength);

    RenderBoardUI.paintBorderShadow(graphics2D, roundRect, borderWidth, outer, inner);

    graphics2D.setPaint(getBackground());
    graphics2D.fill(roundRect);

    double ellipseRatio = RenderBoardUI.defaultUI.ellipseRatio;

    int diamX = Math.min(_cInc, _rInc) - RenderBoardUI.defaultUI.buttonXMargin;
    int diamY = (int) (diamX * ellipseRatio);

    graphics2D.translate(pad.x, pad.y);
//    int xP, yP;

//    xP = _borderInsets.left + margin.width / 2;
//    yP = _borderInsets.top + margin.height / 2;
//    graphics2D.translate(xP, yP);

    //********* TEMP ************
//    if (_selCombo._comboSeq != null) {
//      int iconIndex = this._selCombo._comboSeq.iconIndex;
//      if (iconIndex >= 0 && iconIndex < RenderBoardUI.defaultUI.optimalityIcons.length) {
//        int rgb = RenderBoardUI.defaultUI.optimalityImage.getRGB(iconIndex, 0);
//        RenderBoardUI.defaultUI.newButtonColor = new Color(rgb);
//      }
//    }
//    buttonDrawer = RenderBoardUI.defaultUI.getButtonDrawer();
    //***************************

    buttonDrawer.setup(graphics2D, _cInc, _rInc, diamX, diamY);
    textDrawer.setup(graphics2D, _cInc, _rInc, diamX, diamY);

    AffineTransform orig = graphics2D.getTransform();

    AffineTransform offset = new AffineTransform();
    int rOff = 0;

    for (int r = 0; r < _rows; r++) {
      int cOff = (int) (_cStart + r * _slope);

      for (int c = 0; c < _cols; c++) {
        offset.setTransform(orig);

        if (_isHoriz) {
          offset.translate(cOff, rOff);
        } else {
          offset.translate(rOff, cOff);
        }

        graphics2D.setTransform(offset);

        int realCol = c;
        int realRow = r;

        if (!_isHoriz) {
          realCol = _cols - c - 1;
          realRow = _rows - r - 1;
        }

        getStateBoardButton(realCol, realRow);

        if ((clickPos != null)) {
          if (clickPos.equals(realRow, realCol)) {
//            if (repaintInfo.button != null) {
//              currBoardButton = repaintInfo.button;
//            } else {
              currBoardButton = (isDragging ? BoardButtonImage.FAST_CLICK : BoardButtonImage.HOVER);
//            }
          } else if (isDrawHilites && (currBoardButton == BoardButtonImage.UNSELECTED) && (realRow == clickPos.row)) {
            currBoardButton = BoardButtonImage.HOVER_ROW;
          } else if (isDrawHilites && (currBoardButton == BoardButtonImage.UNSELECTED) && (realCol == clickPos.col)) {
            currBoardButton = BoardButtonImage.HOVER_COLUMN;
          }
        }

        buttonDrawer.draw(graphics2D, realCol, realRow, currSelected, currBoardButton);
        textDrawer.draw(graphics2D, realCol, realRow, currBoardButton, currTextStr);

        cOff += _cInc;
      }
      rOff += _rInc;
    }

    // Draw Labels Last    
    if (isDrawHilites && (clickPos != null)) {
      Font font = graphics2D.getFont();
      float fontSize = font.getSize2D() + 4.f;
      if (fontSize < 18.f) {
        fontSize = 18.f;
      }
   
      graphics2D.setFont(font.deriveFont(Font.BOLD, fontSize));
      graphics2D.setTransform(orig);

      paintBassRowHeader(graphics2D, clickPos.row);
      paintNoteHeader(graphics2D, clickPos.col);
    }
  }
  private BoardButtonImage currBoardButton;
  private String currTextStr;
  private boolean currSelected;

  private void getStateBoardButton(int realCol, int realRow)
  {
    boolean pressed = false;
    boolean selected = false;
    boolean redundant = false;

    if (_selCombo != null) {
      pressed = _selCombo.hasButtonPressed(realRow, realCol);
      currSelected = selected = _selCombo.hasButtonInSeq(realRow, realCol);
      redundant = _selCombo.hasButtonEquivToPressed(realRow, realCol);
    }

    currTextStr = null;
    int finger = -1;

    if (pressed) {
      finger = _selCombo.getFingerAt(realRow, realCol);
      if (finger >= 0) {
        currTextStr = String.valueOf(finger);
      }
    }

    if (currTextStr == null) {
      currTextStr = _theBoard.getChordName(realRow, realCol, false);
    }

    currBoardButton =
            RenderBoardUI.defaultUI.getBoardButtonImage(pressed, selected, redundant, finger);

  }

  enum RepaintType
  {

    NONE,
    BUTTON,
    ROW_LABEL,
    COL_LABEL,
  };

  private class RepaintButtonInfo
  {

    BoardButtonImage button;
    RepaintType type = RepaintType.NONE;
    int row, col;
    double px, py;
    int width, height;

    void translate(double dx, double dy)
    {
      px += dx;
      py += dy;
    }

    boolean matchesRect(Rectangle rect)
    {
      return (width == rect.width) && (height == rect.height);
    }
  }
  private final RepaintButtonInfo repaintInfo = new RepaintButtonInfo();

  public void repaintPos(BassBoard.Pos pos, BoardButtonImage boardButton, boolean unselOnly)
  {
    int col, row;

    if (!_isHoriz) {
      col = _cols - pos.col - 1;
      row = _rows - pos.row - 1;
    } else {
      col = pos.col;
      row = pos.row;
    }

    int cOff = (int) (_cStart + (row * _slope));
    cOff += (col * _cInc);
    int rOff = row * _rInc;

    getStateBoardButton(pos.col, pos.row);

    // if unselOnly, render only over unselected buttons
//    if (unselOnly && (currBoardButton != BoardButtonImage.UNSELECTED)) {
//      return;
//    }

    if (boardButton == null) {
      repaintInfo.button = currBoardButton;
    } else {
      repaintInfo.button = boardButton;
    }

    repaintInfo.px = (initialOff.x + pad.x);
    repaintInfo.py = (initialOff.y + pad.y);
    repaintInfo.width = buttonDrawer.imWidth;
    repaintInfo.height = buttonDrawer.imHeight;

    if (_isHoriz) {
      repaintInfo.translate((int) cOff, (int) rOff);
    } else {
      repaintInfo.translate((int) rOff, (int) cOff);
    }

    repaintInfo.col = col;
    repaintInfo.row = row;

    repaintInfo.type = RepaintType.BUTTON;
    this.paintImmediately((int) repaintInfo.px, (int) repaintInfo.py, repaintInfo.width, repaintInfo.height);

//    if ((bassRowRects != null) && unselOnly) {
//      repaintInfo.type = RepaintType.ROW_LABEL;
//      this.paintImmediately(this.bassRowRects[row]);
//    }

    repaintInfo.type = RepaintType.NONE;
  }

  private boolean paintedSingleButton(Graphics2D graphics2D)
  {
//    if (!repaintInfo.isActive) {
//      return false;
//    }
//
//    Rectangle clipBounds = graphics2D.getClipBounds();
//
//    if (!repaintInfo.matchesRect(clipBounds)) {
//      repaintInfo.isActive = false;
//      return false;
//    }

    graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.f));
    graphics2D.setPaint(getBackground());
    graphics2D.translate(repaintInfo.px, repaintInfo.py);
    graphics2D.fillRect(0, 0, repaintInfo.width, repaintInfo.height);

    buttonDrawer.draw(graphics2D, repaintInfo.col, repaintInfo.row, true, repaintInfo.button);

    textDrawer.setFont(graphics2D);
    textDrawer.draw(graphics2D, repaintInfo.col, repaintInfo.row, repaintInfo.button, currTextStr);

    repaintInfo.type = RepaintType.NONE;

    return true;
  }

  @Override
  public Dimension getPreferredSize()
  {
    int r, c;
    Dimension dim;

    int prefSize = RenderBoardUI.defaultUI._prefSize;

    r = (int) (_theBoard.getNumCols() * _colToRow * prefSize) + (_borderInsets.left + _borderInsets.right);
    c = (int) (_theBoard.getNumRows() * prefSize) + (_borderInsets.top + _borderInsets.bottom);
    r += this.labelRowWidth;
    c += this.labelColHeight;

    if (_isHoriz) {
      dim = new Dimension(r, c);
    } else {
      dim = new Dimension(c, r);
    }

    return dim;
  }
//  private Rectangle[] bassRowRects;
//  private TextLayout[] bassRowLayouts;
//  boolean labelRecomputeNeeded = false;

  void setDrawHilites(boolean draw)
  {
    isDrawHilites = draw;
    repaintInfo.button = null;
    repaint();
  }

  private void paintBassRowHeader(Graphics2D g, int row)
  {
    if ((row < 0) || (row >= _rows)) {
      return;
    }

    String string = _theBoard.getRow(row).toString();

    int tWidth = g.getFontMetrics().stringWidth(string);
    int tHeight = g.getFontMetrics().getHeight();
    int off = 4;

    int x, y;
    int inc = 0;

    if (_isHoriz) {
      x = (int) (_slope * row);
      y = _rInc * row;
      x -= tWidth + this.labelRowWidth;
      inc = (_cInc * _cols) + tWidth + RenderBoardUI.defaultUI.buttonXMargin;
    } else {
      x = _rInc * (_rows - row - 1);
      x -= (tWidth) / 2;
      x += off;
      //y = (int) ((1.f / _slope) * (_rows - row - 1)) + this.labelColHeight;
      y = -labelColHeight + (int) (-_slope * row);
      inc = (_cInc * _cols) + tHeight + RenderBoardUI.defaultUI.buttonXMargin;
    }

    for (int t = 0; t < 2; t++) {
      g.setComposite(AlphaComposite.SrcOver);
      g.setPaint(labelBg);
      g.fillRect(x - off, y - off, tWidth + 2 * off, tHeight + 2 * off);

      if ((clickPos != null) && (clickPos.row == row)) {
        g.setColor(BoardButtonImage.HOVER_ROW.color);
      } else {
        g.setColor(Color.white);
      }

      g.drawString(string, x, y + tHeight);// + (float) bounds.getHeight() / 2.f);
      if (_isHoriz) {
        x += inc;
      } else {
        y += inc;
      }
    }
  }

  private void paintNoteHeader(Graphics2D g, int col)
  {
    if ((col < 0) || (col >= _cols)) {
      return;
    }

    String string = _theBoard.getNoteAt(col).toString();

    int x, y;
    int tWidth = g.getFontMetrics().stringWidth(string);

    if (_isHoriz) {
      x = _cInc - RenderBoardUI.defaultUI.buttonXMargin;
      x /= 2;
      //x = _borderInsets.left;
      x -= _slope;
      x += col * _cInc;
      y = -labelColHeight;
    } else {
      x = -labelRowWidth - 10;
      y = ((_cols - col - 1) * _cInc);
      y += -_slope * (_rows + 1) + RenderBoardUI.defaultUI.buttonXMargin * 2;
    }

    if ((clickPos != null) && (clickPos.col == col)) {
      g.setColor(BoardButtonImage.HOVER_COLUMN.color);
    } else {
      g.setColor(Color.white);
    }

    x -= tWidth / 2;

    g.drawString(string, x, y);
    int inc = (_rInc * (_rows + 1)) + RenderBoardUI.defaultUI.buttonXMargin * 2;
    int mInc = (int) (_slope * (_rows + 1));
    if (_isHoriz) {
      g.drawString(string, x + mInc, y + inc);
    } else {
      g.drawString(string, x + inc, y + mInc);
    }
  }
}
