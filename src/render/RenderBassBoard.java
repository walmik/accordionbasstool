package render;

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
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import music.BassBoard;
import music.ButtonComboSequence;

public class RenderBassBoard extends JPanel implements ListSelectionListener
{

  final static long serialVersionUID = 1;



  public RenderBassBoard()
  {
    this(BassBoard.bassBoard32());
  }

  public RenderBassBoard(BassBoard newBoard)
  {
    setBassBoard(newBoard);

    if (RenderBoardUI.defaultUI == null) {
      RenderBoardUI.defaultUI = new RenderBoardUI();
    }

    _selCombo = new SelectedButtonCombo();

    buttonDrawer = RenderBoardUI.defaultUI.getButtonDrawer();
    textDrawer = RenderBoardUI.defaultUI.getTextDrawer();

    this._colToRow = RenderBoardUI.defaultUI._colToRow;
    this._isHoriz = RenderBoardUI.defaultUI._isHoriz;
    this._slantAngle = RenderBoardUI.defaultUI._defaultSlantAngle;

    this.addMouseListener(new MouseHandler());
    this.addMouseMotionListener(new MouseHandler());
    ToolTipManager.sharedInstance().registerComponent(this);

    int borderMargin = RenderBoardUI.defaultUI.buttonXMargin + borderWidth / 2;
    this._borderInsets = new Insets(borderMargin, borderMargin, borderMargin, borderMargin);
  }

  Dimension margin = new Dimension();
  int borderWidth = 16;
  Dimension _contentDim = new Dimension(0, 0);
  Insets _borderInsets = null;
  BassBoard.Pos clickPos = null;

  class MouseHandler extends MouseAdapter
  {

    @Override
    public void mousePressed(MouseEvent e)
    {
      // TODO Auto-generated method stub
      clickPos = hitTest(e.getX(), e.getY());

      // Debug Stuff
      //int value = _theBoard.getChordAt(clickPos).getChordMask().getValue();
      //int lowestBit = Integer.numberOfTrailingZeros(value);
      //int abc = ((1 << (Note.NUM_HALFSTEPS - lowestBit)) - 1) << lowestBit;
      //System.out.println(Integer.toBinaryString(abc));

      repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
      clickPos = hitTest(e.getX(), e.getY());
      repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
      clickPos = null;
      repaint();
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
    _theBoard = newBoard;
    if (_theBoard != null) {
      _rows = _theBoard.getNumRows();
      _cols = _theBoard.getNumCols();
    }
  }

  public BassBoard getBassBoard()
  {
    return _theBoard;
  }

  @Override
  public void valueChanged(ListSelectionEvent e)
  {
    repaint();
  }

  public void setSelectedButtonCombo(SelectedButtonCombo newSel)
  {
    _selCombo = newSel;
    if (_selCombo != null) {
      _selCombo.addListSelectionListener(this);
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

    w = getWidth() - (_borderInsets.left + _borderInsets.right) - margin.width;
    h = getHeight() - (_borderInsets.top + _borderInsets.bottom) - margin.height;

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
  public void update(Graphics graphics)
  {
    paint(graphics);
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

        graphics2D.translate(xP, yP);

        boolean pressed = (_selCombo != null && _selCombo.hasButtonPressed(realRow, realCol));
        boolean selected = (_selCombo != null && _selCombo.hasButtonInSeq(realRow, realCol));

        String textStr = null;
        int finger = -1;
        
        if (pressed) {
          finger = _selCombo.getFingerAt(realRow, realCol);
          if (finger >= 0) {
            textStr = String.valueOf(finger);
          }
        }

        if (textStr == null) {
          textStr = _theBoard.getChordName(realRow, realCol, false);
        }

        if (!pressed && (clickPos != null) && clickPos.equals(realRow, realCol)) {
          pressed = true;
        }

        RenderBoardUI.BoardButtonImage boardButton = RenderBoardUI.defaultUI.getBoardButtonImage(pressed, selected, finger);

        {
          buttonDrawer.draw(graphics2D, realCol, realRow, pressed, selected, boardButton);
          textDrawer.draw(graphics2D, realCol, realRow, pressed, textStr);
        }

        cOff += _cInc;
      }
      rOff += _rInc;
    }
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
