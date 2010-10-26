package render;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import javax.swing.JComponent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
//		drawers.add(new DrawBassChord());

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

//    this.addMouseListener(new MouseHandler());
//    this.addMouseMotionListener(new MouseHandler());
  }

  BassBoard.Pos clickPos = null;

  class MouseHandler extends MouseAdapter
  {

    @Override
    public void mousePressed(MouseEvent e)
    {
      // TODO Auto-generated method stub
      clickPos = hitTest(e.getX(), e.getY());
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

  public void setBassBoard(BassBoard newBoard)
  {
    _theBoard = newBoard;
    _rows = _theBoard.getNumRows();
    _cols = _theBoard.getNumCols();
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
//	public void setPrefs(boolean horiz, double ctr)
//	{
//		_isHoriz = horiz;
//		_colToRow = ctr;
//	}
  public BassBoard.Pos hitTest(int x, int y)
  {
    int row, col;
    int cPos, rPos;

    computeRenderOffsets();

    if (_isHoriz) {
      cPos = x;
      rPos = y;
    } else {
      cPos = y;
      rPos = x;
    }

    row = rPos / _rInc;
    cPos -= (_cStart + row * _slope);
    col = (cPos + _cInc) / _cInc - 1;

    if (!_isHoriz) {
      col = _cols - col - 1;
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

    if (_isHoriz) {
      w = getWidth();
      h = getHeight();
      h -= RenderBoardUI.defaultUI.buttonXMargin * 2;
    } else {
      h = getWidth();
      w = getHeight();
      w -= RenderBoardUI.defaultUI.buttonXMargin * 2;
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
  }

  @Override
  public void paintComponent(Graphics graphics)
  {
    super.paintComponent(graphics);

    Graphics2D graphics2D = (Graphics2D) graphics;

    computeRenderOffsets();

//		Iterator<DrawBass> it = drawers.iterator();
//		while (it.hasNext())
//		{
//			if (_isHoriz)
//				it.next().beginDrawButtons(this, graphics, _cInc, _rInc, _isHoriz);				
//			else
//				it.next().beginDrawButtons(this, graphics, _rInc, _cInc, _isHoriz);
//		}

    int xW, yW;

    if (_isHoriz) {
      xW = _cInc;
      yW = _rInc;
    } else {
      yW = _rInc;
      xW = _cInc;
    }

    double ellipseRatio = RenderBoardUI.defaultUI.ellipseRatio;
    int margin = RenderBoardUI.defaultUI.buttonXMargin;

    int diamX = Math.min(xW, yW) - margin;
    int diamY = (int) (diamX * ellipseRatio);

    int xP = margin;
    int yP = margin;

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
        if (!_isHoriz) {
          realCol = _cols - c - 1;
        }

        graphics2D.translate(xP, yP);

        boolean pressed = (_selCombo != null && _selCombo.hasButtonPressed(r, realCol));
        boolean selected = (_selCombo != null && _selCombo.hasButtonInSeq(r, realCol));

        if (!pressed && (clickPos != null) && clickPos.equals(r, realCol)) {
          pressed = true;
        }

        {
          buttonDrawer.draw(graphics2D, realCol, r, pressed, selected);
          textDrawer.draw(graphics2D, realCol, r, pressed, _theBoard.getChordName(r, realCol));
        }


//				it = drawers.iterator();
//				while (it.hasNext())
//				{
//					if (_isHoriz)
//						it.next().drawButton(graphics, (int)_cOff, _rOff, _cInc, _rInc,
//								c, r);
//					else
//						it.next().drawButton(graphics, _rOff, (int)_cOff, _rInc, _cInc,
//								_cols - c - 1, r);
//				}

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
    int buttonMargin = RenderBoardUI.defaultUI.buttonXMargin;

    r = (int) (_theBoard.getNumCols() * _colToRow * prefSize);
    c = (int) (_theBoard.getNumRows() * prefSize);// - _prefSize/2;

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
    int x = RenderBoardUI.defaultUI.buttonXMargin + (int)_slope;
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

  public void setupHeaders(JScrollPane scrollPane)
  {
    JPanel noteHeader = new JPanel()
    {
      public void paint(Graphics g)
      {
        paintNoteHeader(this, g);
      }

      public Dimension getPreferredSize()
      {
        return new Dimension(50, 20);
      }
    };

    JPanel bassRowHeader = new JPanel()
    {
      public void paint(Graphics g)
      {
        paintBassRowHeader(this, g);
      }

      public Dimension getPreferredSize()
      {
        return new Dimension(100, 20);
      }
    };


    
    scrollPane.setColumnHeaderView(noteHeader);
    scrollPane.setRowHeaderView(bassRowHeader);
  }


  //Member Vars
  //================================================================================
  BassBoard _theBoard;
  boolean _isHoriz = false;
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
