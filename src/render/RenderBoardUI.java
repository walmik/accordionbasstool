/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author Ilya
 */
public class RenderBoardUI
{

  static RenderBoardUI defaultUI = new RenderBoardUI();
  private Path2D unpressedCyl, pressedCyl;
  private Ellipse2D _buttonTop, _shadowFloor;


  float shadowScale = 1.1f;
  final int dimScale = 2;

  int diamX = 36 * dimScale;
  int diamY = 27 * dimScale;
  int cylHeight = 16 * dimScale;
  
  float pressedRatio = 0.25f;

  final int buttonXMargin = 10;

  final double ellipseRatio = (3.f / 4.f);
  final double _colToRow = 4.0 / 3.0;
  boolean _isHoriz = true;
  double _defaultSlantAngle = (20.0) * Math.PI / 180;
  final static int _prefSize = 48;
  BufferedImage selectedIM, unselectedIM, pressedIM;
  boolean use3DDrawer = true;

  RenderBoardUI()
  {
    //diamX /= shadowScale;
    //diamY /= shadowScale;
    unpressedCyl = createCylPath(diamX, diamY, cylHeight);
    pressedCyl = createCylPath(diamX, diamY, cylHeight * pressedRatio);

    _buttonTop = new Ellipse2D.Float(0, 0, diamX, diamY);
    _shadowFloor = new Ellipse2D.Float(0, 0, diamX * shadowScale, diamY * shadowScale);
    //_pressedShadowFloor = new Ellipse2D.Float(0, 0, diamX * shadowScale * .5f, diamY * shadowScale * .5f);

    int imWidth = (int)(diamX * shadowScale);
    int imHeight = (int)(diamY * shadowScale) + cylHeight;

    selectedIM = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_INT_ARGB);
    unselectedIM = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_INT_ARGB);
    pressedIM = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_INT_ARGB);

    render3DButton(selectedIM.createGraphics(), Color.blue, true, false);
    render3DButton(unselectedIM.createGraphics(), Color.black, false, false);
    render3DButton(pressedIM.createGraphics(), Color.magenta, false, true);
  }

  ButtonDrawer getButtonDrawer()
  {
    if (use3DDrawer) {
      return new IconButtonDrawer();
    } else {
      return new FlatButtonDrawer();
    }
  }

  TextDrawer getTextDrawer()
  {
    return new TextDrawer();
  }

  abstract class ButtonDrawer
  {
    int _pressedCylOff;

    void setup(Graphics2D graphics, int xW, int yW, int diamX, int diamY)
    {
      int pressedHeight = (int) (cylHeight * pressedRatio);
      _pressedCylOff = cylHeight - pressedHeight;
    }

    abstract void draw(Graphics2D graphics, int col, int row, boolean pressed, boolean selected);
  }

  class IconButtonDrawer extends ButtonDrawer
  {

    int _xW, _yW;
    int _diamX, _diamY;
    int imWidth, imHeight;

    @Override
    void setup(Graphics2D graphics, int xW, int yW, int diamX, int diamY)
    {
      super.setup(graphics, xW, yW, diamX, diamY);
      
      _xW = xW;
      _yW = yW;
      _diamX = diamX;
      _diamY = diamY;

      imHeight = yW;
      imWidth = imHeight * pressedIM.getWidth() / pressedIM.getHeight();

      //imWidth = (int)(_diamX * shadowScale);
      //imHeight = imWidth * pressedIM.getHeight() / pressedIM.getWidth();
      _pressedCylOff = _pressedCylOff * imWidth / pressedIM.getWidth();
      //System.out.println("IM: " + imWidth + ", " + imHeight);
    }

    @Override
    void draw(Graphics2D graphics, int col, int row, boolean pressed, boolean selected)
    {
 
      //graphics.setComposite(AlphaComposite.SrcAtop);
      //graphics.drawRect(0, 0, _xW, _yW);

      if (pressed) {
        graphics.drawImage(pressedIM, 0, 0, imWidth, imHeight, null);
        graphics.translate(0, _pressedCylOff);
      } else if (selected) {
        graphics.drawImage(selectedIM, 0, 0, imWidth, imHeight, null);
      } else {
        graphics.drawImage(unselectedIM, 0, 0, imWidth, imHeight, null);
      }
    }
  }

  class FlatButtonDrawer extends ButtonDrawer
  {

    @Override
    void setup(Graphics2D graphics, int xW, int yW, int diamX, int diamY)
    {
      super.setup(graphics, xW, yW, diamX, diamY);

//      _unpressedCyl = createCylPath(diamX, diamY, cylHeight);
//
//      _pressedCyl = createCylPath(diamX, diamY, (int) (cylHeight * pressedRatio));
//
//      _buttonTop = new Ellipse2D.Float(0, 0, diamX, diamY);
    }

    @Override
    void draw(Graphics2D graphics, int col, int row, boolean pressed, boolean selected)
    {
      Color lighterFill = Color.LIGHT_GRAY;
      Color darkerFill = Color.GRAY;

      if (selected) {
        //TODO: System Color
        //lighterFill = Color.MAGENTA;
        lighterFill = SystemColor.textHighlight;
        darkerFill = lighterFill.darker();
      }

      if (pressed) {
        // Draw Pressed
        graphics.translate(0, _pressedCylOff);
        graphics.setColor(darkerFill);
        graphics.fill(_pressedCyl);
        graphics.setColor(lighterFill);
        graphics.fill(_buttonTop);
      } else {
        // Draw Unpressed
        graphics.setColor(darkerFill);
        graphics.fill(_unpressedCyl);
        graphics.setColor(lighterFill);
        graphics.fill(_buttonTop);
      }
    }
    Path2D _unpressedCyl;
    Path2D _pressedCyl;
    Ellipse2D _buttonTop;
  }

  static class TextDrawer
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

      _tX = diamX / 2;
      _tY = diamY / 2 + textDescent;
    }

    void draw(Graphics2D graphics, int col, int row, boolean pressed, String chordStr)
    {
      if (pressed) {
        graphics.setColor(SystemColor.textText);
      } else {
        graphics.setColor(SystemColor.textText);
      }

      graphics.drawString(chordStr,
              _tX - _fm.stringWidth(chordStr) / 2,
              _tY);
    }
  }

    static Path2D createCylPath(float X, float Y, float height)
  {
    Path2D cyl = new Path2D.Float();

    // Right cyl line
    cyl.append(new Line2D.Float(X, Y / 2,
            X, Y / 2 + height), true);

    Arc2D cylBottom = new Arc2D.Float();
    cylBottom.setArc(0, height, X, Y, 0, -180, Arc2D.OPEN);
    cyl.append(cylBottom, true);

    // Left cy line
    cyl.append(new Line2D.Float(0, Y / 2 + height,
            0, Y / 2), true);


    Arc2D cylTop = new Arc2D.Float();
    cylTop.setArc(0, 0, X, Y, 0, 180, Arc2D.OPEN);

    AffineTransform trans;
    trans = AffineTransform.getRotateInstance(-Math.PI);
    trans.translate(-X, -Y);
    cyl.append(cylTop.getPathIterator(trans), true);

    return cyl;
  }

  void render3DButton(Graphics2D graphics, Color selColor, boolean selected, boolean pressed)
  {
    graphics.setComposite(AlphaComposite.SrcOver);
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int currCylHeight = cylHeight;

    if (pressed) {
      currCylHeight *= pressedRatio;
      graphics.translate(0, cylHeight - currCylHeight);
    }

    if (!pressed) {
      graphics.translate(0, currCylHeight);
      graphics.setComposite(AlphaComposite.SrcOver);
      graphics.setPaint(new Color(0.f, 0.f, 0.f, 0.25f));
      graphics.fill(_shadowFloor);
      graphics.translate(0, -currCylHeight);
    }

    Point2D start = new java.awt.geom.Point2D.Float(0, diamY);
    Point2D end = new Point2D.Float(diamX, diamY);
    float fractions[] = {0.0f, 0.3f, 1.0f};
    Color colors[] = {Color.darkGray, Color.white, Color.black};

    Paint cylPaint = new LinearGradientPaint(start, end, fractions, colors);

    {
      graphics.setComposite(AlphaComposite.SrcOver);
      graphics.setPaint(selColor);
      graphics.fill(pressed ? pressedCyl : unpressedCyl);
    }

    graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
    graphics.setPaint(cylPaint);
    graphics.fill(pressed ? pressedCyl : unpressedCyl);


    //graphics.setComposite(AlphaComposite.SrcOver);
    graphics.setPaint(selColor);
    graphics.fill(_buttonTop);

    // LINEAR GRADIENT
    start = new java.awt.geom.Point2D.Float(0, diamY / 2);
    end = new Point2D.Float(diamX, 0);
    float frac3[] = {0.0f, 1.0f};
    Color col3[] = {Color.black, Color.white};
    graphics.setPaint(new LinearGradientPaint(start, end, frac3, col3));
    graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
    graphics.fill(_buttonTop);


    float frac2[] = {0.0f, 1.0f};
    Color col2[] = {Color.white, selColor};
    graphics.setPaint(
            new RadialGradientPaint(diamX / 2, diamY / 2,
            (pressed ? diamY * 2.0f : diamY),
            diamX / 2, diamY / 2,
            frac2, col2,
            RadialGradientPaint.CycleMethod.NO_CYCLE));

    graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (pressed ? 0.9f : 0.5f)));
    graphics.fill(_buttonTop);
  }
}
