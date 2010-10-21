package render;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.SystemColor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import music.BassBoard;
import music.ButtonComboSequence;


public class RenderBassBoard extends JPanel implements ListSelectionListener
{
	final static long serialVersionUID = 1;

  public RenderBassBoard()
  {
    this(BassBoard.bassBoard120(), true);
  }
  
	public RenderBassBoard(BassBoard newBoard, boolean horiz)
	{
//		drawers.add(new DrawBassChord());

		setBassBoard(newBoard);
		_isHoriz = horiz;
		
		this.addMouseListener(new MouseAdapter()
		{

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				hitTest(e.getX(), e.getY());
			}
		});
	}

	public void setBassBoard(BassBoard newBoard)
	{
		_theBoard = newBoard;
		_rows = _theBoard.getRows();
		_cols = _theBoard.getCols();
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
    if (_selCombo != null)
      _selCombo.addListSelectionListener(this);
    repaint();
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

	public void setPrefs(boolean horiz, double ctr)
	{
		_isHoriz = horiz;
		_colToRow = ctr;
	}
	
	public void hitTest(int x, int y)
	{
		int row, col;
		int cPos, rPos;
		
		computeRenderOffsets();
		
		if (_isHoriz)
		{
			cPos = x;
			rPos = y;
		}
		else
		{
			cPos = y;
			rPos = x;
		}
		
		row = rPos / _rInc;
		cPos -= (_cStart + row*_slope);
		col = (cPos + _cInc) / _cInc - 1;
		
		if (!_isHoriz)
		{
			col = _cols - col - 1;
		}
		
		if ((row < _rows) && (col < _cols) && (row >= 0) && (col >= 0))
			System.out.println(_theBoard.getChordAt(row, col));
		else
			System.out.println("Row: " + row + " Col: " + col);
	}

	private void computeRenderOffsets()
	{
		_rInc = 0;
		_cInc = 0;

		_slope = 0;

		int w, h;

		if (_isHoriz)
		{
			w = getWidth();
			h = getHeight();
		}
		else
		{
			h = getWidth();
			w = getHeight();
		}
		
		double theTan = Math.abs(Math.tan(_slantAngle));

		double ratio = ((double)_cols / (double)_rows) * _colToRow;

		double theB = (w / (theTan + ratio));

		if (theB > h)
		{
			int divs = h / _rows;
			_slope = (theTan * divs);
			_rInc = divs;
			_cInc = (int)(divs * _colToRow);
		}
		else
		{	
			double theA = theB * theTan;
			double divs = ((w - theA) / _cols);
			_slope = (theA / _rows);
			_cInc = (int)divs;
			_rInc = (int)(divs / _colToRow);
		}

		_cStart = 0;

		if (_slantAngle < 0)
		{
			_cStart += _rows*_slope;
			_slope = -_slope;
		}
	}

	@Override
	public void paintComponent(Graphics graphics) 
	{
		super.paintComponent(graphics);
		
		Graphics2D graphics2D = (Graphics2D)graphics;
		
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
		
		if (_isHoriz)
		{
			xW = _cInc;
			yW = _rInc;
		}
		else
		{
			yW = _rInc;
			xW = _cInc;
		}
		
		float ellipseRatio = 3.0f / 4.0f;
		int margin = 10;

		int diamX = Math.min(xW, yW) - margin;
		int diamY = (int)(diamX * ellipseRatio);
		
		buttonDrawer.setup(graphics2D, xW, yW, diamX, diamY);
		textDrawer.setup(graphics2D, xW, yW, diamX, diamY);
		
		AffineTransform orig = graphics2D.getTransform();

		AffineTransform offset = new AffineTransform();
		int rOff = 0;

		for (int r = 0; r < _rows; r++)
		{
			double cOff = _cStart + r*_slope;

			for (int c = 0; c < _cols; c++)
			{
				offset.setTransform(orig);
				
				if (_isHoriz)
					offset.translate(cOff, rOff);
				else
					offset.translate(rOff, cOff);
				
				graphics2D.setTransform(offset);
				
				if (_isHoriz)
				{
					buttonDrawer.draw(graphics2D, c, r);
					textDrawer.draw(graphics2D, c, r);
			    }
				else
				{
					buttonDrawer.draw(graphics2D, _cols - c - 1, r);
					textDrawer.draw(graphics2D, _cols - c - 1, r);
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

		r = (int)(_theBoard.getCols() * _colToRow * _prefSize);
		c = (int)(_theBoard.getRows() * _prefSize) + 10;// - _prefSize/2;
		
		if (_isHoriz)
		{
			dim = new Dimension(r, c);
		}
		else
		{
			dim = new Dimension(c, r);
			//Dimension parentDim = this.getParent().getSize();
			//dim = new Dimension(parentDim.width, parentDim.height * r / c);
		}
				
		return dim;
	}
	
	
	private class ButtonDrawer
	{
		
		void setup(Graphics2D graphics, int xW, int yW, int diamX, int diamY)
		{
			_xP = (xW - diamX) / 2;
			_yP = (yW - diamY) / 2;


			int cylHeight = 20;
			int pressedHeight = cylHeight / 4;

			_unpressedCyl = createCylPath(diamX, diamY, cylHeight);

			_pressedCyl = createCylPath(diamX, diamY, pressedHeight);
			_pressedCylOff = cylHeight - pressedHeight;


			_buttonTop = new Ellipse2D.Float(0, 0, diamX, diamY);
		}

		Path2D createCylPath(int X, int Y, int height)
		{
			Path2D cyl = new Path2D.Float();

			// Right cyl line		
			cyl.append(new Line2D.Float(X, Y/2, 
					X, Y/2 + height), true);

			Arc2D cylBottom = new Arc2D.Float();
			cylBottom.setArc(0, height, X, Y, 0, -180, Arc2D.OPEN);
			cyl.append(cylBottom, true);

			// Left cy line
			cyl.append(new Line2D.Float(0, Y/2 + height, 
					0, Y/2), true);

			return cyl;
		}

		void draw(Graphics2D graphics, int col, int row)
		{
			boolean pressed = (_selCombo != null && _selCombo.hasButtonPressed(row, col));
			boolean selected = (_selCombo != null && _selCombo.hasButtonInSeq(row, col));

			graphics.translate(_xP, _yP);
			
			Color lighterFill = Color.LIGHT_GRAY;
			Color darkerFill = Color.GRAY;
			
			if (selected)
			{
        //TODO: System Color
				//lighterFill = Color.MAGENTA;
        lighterFill = SystemColor.textHighlight;
				darkerFill = lighterFill.darker();
			}
			
			if (pressed)
			{
				// Draw Pressed
				graphics.translate(0, _pressedCylOff);
				graphics.setColor(darkerFill);
				graphics.fill(_pressedCyl);
				graphics.setColor(lighterFill);
				graphics.fill(_buttonTop);
			}
			else
			{
				// Draw Unpressed
				graphics.setColor(darkerFill);
				graphics.fill(_unpressedCyl);
				graphics.setColor(lighterFill);
				graphics.fill(_buttonTop);
			}
		}

		int _xP, _yP;

		Path2D _unpressedCyl;
		Path2D _pressedCyl;
		Ellipse2D _buttonTop;

		int _pressedCylOff;
	}
	
	
	private class TextDrawer
	{
		private FontMetrics _fm;
		private int _tX, _tY;
		private int _textHeight;
		private Font _font = new Font("Default", Font.BOLD, 14);

		void setup(Graphics2D graphics, int xW, int yW, int diamX, int diamY)
		{			
			// Text Set up
			graphics.setFont(_font);
			_fm = graphics.getFontMetrics();
			int textDescent = _fm.getDescent() + 1;
			_textHeight = _fm.getHeight() - 4;
			
			_tX = diamX/2;
			_tY = diamY/2 + textDescent;
		}
		
		void draw(Graphics2D graphics, int col, int row)
		{
			boolean pressed = (_selCombo != null && _selCombo.hasButtonPressed(row, col));

			if (pressed)
				graphics.setColor(SystemColor.textHighlightText);
			else
				graphics.setColor(SystemColor.textText);
			
			String chordStr = _theBoard.getChordName(row, col);
			
			graphics.drawString(chordStr, 
								_tX - _fm.stringWidth(chordStr)/2, 
								_tY);
		}
	}


	//Member Vars
	//================================================================================
	BassBoard _theBoard;

	boolean _isHoriz = true;
	double _colToRow = 4.0 / 3.0;

	double _slantAngle = (20.0) * Math.PI / 180;
	
	final static int _prefSize = 48;
	
	int _rows = 0, _cols = 0;
	
	int _rInc = 0, _cInc = 0;
	double _slope = 0;

	int _cStart = 0;

//	final Vector<DrawBass> drawers = new Vector<DrawBass>();
	
	SelectedButtonCombo _selCombo = new SelectedButtonCombo();
	
	ButtonDrawer buttonDrawer = new ButtonDrawer();
	TextDrawer textDrawer = new TextDrawer();
}
