package render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;

import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import music.BassBoard;
import music.ButtonComboSequence;
import music.Chord;

public class RenderBassBoard extends JPanel
{

  final static long serialVersionUID = 1;

  public RenderBassBoard()
  {
    this(BassBoard.bassBoard32());
  }

  public RenderBassBoard(BassBoard newBoard)
  {
    setBassBoard(newBoard);

    _selCombo = new SelectedButtonCombo();

    buttonDrawer = RenderBoardUI.defaultUI.getButtonDrawer();
    textDrawer = RenderBoardUI.defaultUI.getTextDrawer();

    this._colToRow = RenderBoardUI.defaultUI._colToRow;
    this._isHoriz = RenderBoardUI.defaultUI._isHoriz;
    this._slantAngle = RenderBoardUI.defaultUI._defaultSlantAngle;

    ToolTipManager.sharedInstance().registerComponent(this);

    int borderMargin = RenderBoardUI.defaultUI.buttonXMargin + borderWidth / 2;
    this._borderInsets = new Insets(borderMargin, borderMargin, borderMargin, borderMargin);
  }
  Dimension margin = new Dimension();
  int borderWidth = 16;
  Dimension _contentDim = new Dimension(0, 0);
  Insets _borderInsets = null;
  BassBoard.Pos clickPos = null;
  MouseAdapter mainMouseAdapter = null;

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

  public void toggleMouseClickHilite(boolean on)
  {
    if (on) {
      this.addMouseListener(new MouseHandler(this));
      this.addMouseMotionListener(new MouseHandler(this));
    } else {
      this.removeMouseListener(new MouseHandler(this));
      this.removeMouseMotionListener(new MouseHandler(this));
    }
  }

  public static class MouseHandler extends MouseAdapter
  {

    RenderBassBoard renderBoard;

    MouseHandler(RenderBassBoard board)
    {
      this.renderBoard = board;
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
      // TODO Auto-generated method stub
      renderBoard.setClickPos(e);

      // Debug Stuff
      //int value = _theBoard.getChordAt(clickPos).getChordMask().getValue();
      //int lowestBit = Integer.numberOfTrailingZeros(value);
      //int abc = ((1 << (Note.NUM_HALFSTEPS - lowestBit)) - 1) << lowestBit;
      //System.out.println(Integer.toBinaryString(abc));
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
      renderBoard.setClickPos(e);
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
      renderBoard.clearClickPos();
    }
  }

  @Override
  public String getToolTipText(MouseEvent event)
  {
    BassBoard.Pos mouseOverPos = hitTest(event.getX(), event.getY());

    if (mouseOverPos != null) {
      String string = "<html>";
      string += "<b>" + _theBoard.getChordName(mouseOverPos, true) + "</b>";
      string += " - (";
      string += _theBoard.getChordAt(mouseOverPos).toHtmlString();
      string += ")</html>";
      return string;
    }

    return null;
  }

  public void setBassBoard(BassBoard newBoard)
  {
    BassBoard oldBoard = _theBoard;
    _theBoard = newBoard;
    if (_theBoard != null) {
      _rows = _theBoard.getNumRows();
      _cols = _theBoard.getNumCols();
    }
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
    _selCombo = newSel;
    if (_selCombo != null) {
      _selCombo.addListSelectionListener(new RepaintListener());
    }
    if (rowSel != null) {
      rowSel.addListSelectionListener(new RepaintListener());
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
    repaint();
  }

  public boolean isHorizontal()
  {
    return _isHoriz;
  }

  public void setClickPos(MouseEvent e)
  {
    BassBoard.Pos newClickPos = hitTest(e);
    setClickPos(newClickPos);
  }

  public void setClickPos(BassBoard.Pos newClickPos)
  {
    if (!BassBoard.posEquals(clickPos, newClickPos)) {
      if (clickPos != null) {
        this.drawPos(clickPos, null);
      }
      //repaint();
      clickPos = newClickPos;

      if (clickPos != null) {
        this.drawPos(clickPos, RenderBoardUI.BoardButtonImage.FAST_CLICK);
      }
    }
  }

  public void clearClickPos()
  {
    setClickPos((BassBoard.Pos) null);
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

  }

  @Override
  public void paint(Graphics graphics)
  {

    Graphics2D graphics2D = (Graphics2D) graphics;

    computeRenderOffsets();

    Color outer = Color.black;
    Color inner = Color.gray;

    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    graphics2D.setPaint(outer);
    graphics2D.fillRect(0, 0, getWidth(), getHeight());


    //if (_isHoriz) {
    if (this.getAlignmentX() == JComponent.CENTER_ALIGNMENT) {
      graphics2D.translate((getWidth() - _contentDim.width - margin.width) / 2, 0);
    }
    //} else {
    //   if (this.getAlignmentY() == JComponent.CENTER_ALIGNMENT) {
    //     graphics2D.translate(0, (getHeight() - _contentDim.height - margin.height) / 2);
    //   }
    //}

    // Paint Border & Background
    RoundRectangle2D roundRect =
            new RoundRectangle2D.Float(_borderInsets.left, _borderInsets.top,
            _contentDim.width, _contentDim.height, 40, 40);

    RenderBoardUI.paintBorderShadow(graphics2D, roundRect, borderWidth, outer, inner);

    graphics2D.setPaint(getBackground());
    graphics2D.fill(roundRect);

    int xW, yW;

    if (_isHoriz) {
      xW = _cInc;
      yW = _rInc;
    } else {
      yW = _rInc;
      xW = _cInc;
    }

    double ellipseRatio = RenderBoardUI.defaultUI.ellipseRatio;

    int diamX = Math.min(xW, yW) - margin.width / 3;
    int diamY = (int) (diamX * ellipseRatio);

    int xP, yP;

//    if (boardAlign == JComponent.CENTER_ALIGNMENT) {
//      xP = (getWidth() - _contentDim.width) / 2 + margin;
//    } else {
//      xP = _borderInsets.left + margin;
//    }


    xP = _borderInsets.left + margin.width / 2;
    yP = _borderInsets.top + margin.height / 2;

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

    buttonDrawer.setup(graphics2D, xW, yW, diamX, diamY);
    textDrawer.setup(graphics2D, xW, yW, diamX, diamY);

    AffineTransform orig = graphics2D.getTransform();
    orig.translate(xP, yP);

    AffineTransform offset = new AffineTransform();
    int rOff = 0;

    for (int r = 0; r < _rows; r++) {
      double cOff = _cStart + r * _slope;

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

        //graphics2D.translate(xP, yP);

        getStateBoardButton(realCol, realRow);
        {
          buttonDrawer.draw(graphics2D, realCol, realRow, currSelected, currBoardButton);
          textDrawer.draw(graphics2D, realCol, realRow, currBoardButton, currTextStr);
        }

        cOff += _cInc;
      }
      rOff += _rInc;
    }
  }
  private RenderBoardUI.BoardButtonImage currBoardButton;
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

    boolean fastClick = false;

//    if (!pressed && (clickPos != null) && clickPos.equals(realRow, realCol)) {
//      fastClick = true;
//    }

    currBoardButton =
            RenderBoardUI.defaultUI.getBoardButtonImage(pressed, selected, redundant, fastClick, finger);

  }

  public void drawPos(BassBoard.Pos pos, RenderBoardUI.BoardButtonImage boardButton)
  {
    int col, row;

    if (!_isHoriz) {
      col = _cols - pos.col - 1;
      row = _rows - pos.row - 1;
    } else {
      col = pos.col;
      row = pos.row;
    }

    double cOff = _cStart + (row * _slope);
    cOff += (col * _cInc);
    double rOff = row * _rInc;

    getStateBoardButton(pos.col, pos.row);
    if (boardButton == null) {
      boardButton = currBoardButton;
    }

    Graphics2D graphics2D = (Graphics2D) this.getGraphics();

    if (this.getAlignmentX() == JComponent.CENTER_ALIGNMENT) {
      graphics2D.translate((getWidth() - _contentDim.width - margin.width) / 2, 0);
    }

    int xP = _borderInsets.left + margin.width / 2;
    int yP = _borderInsets.top + margin.height / 2;
    graphics2D.translate(xP, yP);

    if (_isHoriz) {
      graphics2D.translate(cOff, rOff);
    } else {
      graphics2D.translate(rOff, cOff);
    }

    graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    graphics2D.setPaint(getBackground());
    graphics2D.setClip(0, 0, _cInc, _rInc);
    graphics2D.fillRect(0, 0, boardButton.image.getWidth(), boardButton.image.getHeight());

    buttonDrawer.draw(graphics2D, col, row, true, boardButton);

    textDrawer.setFont(graphics2D);
    textDrawer.draw(graphics2D, col, row, boardButton, currTextStr);
  }

  @Override
  public Dimension getPreferredSize()
  {
    int r, c;
    Dimension dim;

    int prefSize = RenderBoardUI.defaultUI._prefSize;

    r = (int) (_theBoard.getNumCols() * _colToRow * prefSize) + (_borderInsets.left + _borderInsets.right);
    c = (int) (_theBoard.getNumRows() * prefSize) + (_borderInsets.top + _borderInsets.bottom);

    if (_isHoriz) {
      dim = new Dimension(r, c);
    } else {
      dim = new Dimension(c, r);
      //Dimension parentDim = this.getParent().getSize();
      //dim = new Dimension(parentDim.width, parentDim.height * r / c);
    }

    return dim;
  }

  private void paintNoteHeader(JComponent comp, Graphics g)
  {
    //double cOff = _cStart + r * _slope;
    int x = RenderBoardUI.defaultUI.buttonXMargin + (int) _slope;
    int y = RenderBoardUI.defaultUI.buttonXMargin;

    g.setColor(Color.black);
    g.fillRect(0, 0, comp.getWidth(), comp.getHeight());

    computeRenderOffsets();
    g.setColor(Color.white);
    g.setFont(this.getFont());

    for (int c = 0; c < _cols; c++) {
      g.drawString(_theBoard.getNoteAt(c).toString(), x, y);
      x += _cInc;
    }
  }

  private void paintBassRowHeader(JComponent comp, Graphics g)
  {
    int x = RenderBoardUI.defaultUI.buttonXMargin;
    int y = RenderBoardUI.defaultUI.buttonXMargin + _rInc / 2 + g.getFontMetrics().getHeight() / 2;

    g.setColor(Color.black);
    g.fillRect(0, 0, comp.getWidth(), comp.getHeight());

    computeRenderOffsets();
    g.setColor(Color.white);
    g.setFont(this.getFont());

    for (int r = 0; r < _rows; r++) {
      g.drawString(_theBoard.getRow(r).toString(), x, y);
      y += _rInc;
    }
  }
  //Member Vars
  //================================================================================
  BassBoard _theBoard;
  private boolean _isHoriz = false;
  double _colToRow = 0;
  double _slantAngle = 0;
  int _rows = 0, _cols = 0;
  int _rInc = 0, _cInc = 0;
  double _slope = 0;
  int _cStart = 0;
//	final Vector<DrawBass> drawers = new Vector<DrawBass>();
  SelectedButtonCombo _selCombo;
  RenderBoardUI.ButtonDrawer buttonDrawer;
  RenderBoardUI.TextDrawer textDrawer;
}
