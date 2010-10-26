/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
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
  int defaultDiamX = 36;
  int defaultDiamY = 27;
  int defaultCylHeight = 16;
  float pressedRatio = 0.333f;
  final int buttonXMargin = 10;
  final double ellipseRatio = (3.f / 4.f);
  final double _colToRow = 4.0 / 3.0;
  boolean _isHoriz = true;
  double _defaultSlantAngle = (20.0) * Math.PI / 180;
  final static int _prefSize = 48;
  BufferedImage selectedIM, unselectedIM, pressedIM;
  boolean use3DDrawer = true;
  int lastIMScale = 0;

  RenderBoardUI()
  {
    //diamX /= shadowScale;
    //diamY /= shadowScale;

    createButtonImages(1);
  }

  private void createButtonImages(int scale)
  {
    if (scale == lastIMScale) {
      return;
    }

    createButtonImages(defaultDiamX * scale, defaultDiamY * scale, defaultCylHeight * scale);
    lastIMScale = scale;
  }

  private void createButtonImages(int diamX, int diamY, int cylHeight)
  {
    unpressedCyl = createCylPath(diamX, diamY, cylHeight);
    pressedCyl = createCylPath(diamX, diamY, cylHeight * pressedRatio);

    _buttonTop = new Ellipse2D.Float(0, 0, diamX, diamY);
    _shadowFloor = new Ellipse2D.Float(0, 0, diamX * shadowScale, diamY * shadowScale);
    //_pressedShadowFloor = new Ellipse2D.Float(0, 0, diamX * shadowScale * .5f, diamY * shadowScale * .5f);

    int imWidth = (int) (diamX * shadowScale);
    int imHeight = (int) (diamY * shadowScale) + cylHeight;

    selectedIM = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_INT_ARGB);
    unselectedIM = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_INT_ARGB);
    pressedIM = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_INT_ARGB);

    render3DButton(diamX, diamY, cylHeight, selectedIM.createGraphics(), Color.blue, true, false);
    render3DButton(diamY, diamY, cylHeight, unselectedIM.createGraphics(), Color.black, false, false);
    render3DButton(diamX, diamY, cylHeight, pressedIM.createGraphics(), Color.magenta, false, true);
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

    abstract void setup(Graphics2D graphics, int xW, int yW, int diamX, int diamY);

    abstract void draw(Graphics2D graphics, int col, int row, boolean pressed, boolean selected);
  }

  class IconButtonDrawer extends ButtonDrawer
  {

    int _xW, _yW;
    //int _diamX, _diamY;
    int imWidth, imHeight;
    int _pressedCylOff;

    @Override
    void setup(Graphics2D graphics, int xW, int yW, int diamX, int diamY)
    {
      _xW = xW;
      _yW = yW;
      //_diamX = diamX;
      //_diamY = diamY;

      int scale = (xW / defaultDiamX);
      if (scale < 1) {
        scale = 1;
      }
      //Recreate Images every time?
      //****************
      createButtonImages(scale);
      //****************

      imHeight = yW;
      imWidth = imHeight * pressedIM.getWidth() / pressedIM.getHeight();
      //imWidth = (int)(_diamX * shadowScale);
      //imHeight = imWidth * pressedIM.getHeight() / pressedIM.getWidth();
      _pressedCylOff = (int) ((defaultCylHeight * scale * (1.0 - pressedRatio)) * imWidth / pressedIM.getWidth());
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

    int _pressedCylOff;

    @Override
    void setup(Graphics2D graphics, int xW, int yW, int diamX, int diamY)
    {
      int pressedHeight = (int) (defaultCylHeight * pressedRatio);
      _pressedCylOff = defaultCylHeight - pressedHeight;
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

  void render3DButton(int diamX,
          int diamY,
          int cylHeight,
          Graphics2D graphics,
          Color selColor,
          boolean selected, boolean pressed)
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

  ///Border Stuff
//  private void drawShearBorder(Graphics2D g)
//  {
//    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//    //this.drawShape(outlinePoly, g, getWidth(), getHeight());
//
//    RoundRectangle2D roundRect = new RoundRectangle2D.Float(margin.width, margin.height,
//            _contentDim.width, _contentDim.height, 20, 10);
//
//    Color outer = Color.black;
//    Color inner = Color.darkGray;
//    int borderWidth = 16;
//
//    g.setPaint(outer);
//    g.fillRect(0, 0, getWidth(), getHeight());
//
//    AffineTransform orig = g.getTransform();
//
//    double slope = Math.abs(Math.tan(_slantAngle));
//    AffineTransform sheared = AffineTransform.getShearInstance(slope, 0.0f);
//
//    //g.transform(sheared);
//
//    //g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));
//    paintBorderShadow(g, roundRect, borderWidth, outer, inner);
//
//    //g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.f));
//    g.setPaint(getBackground());
//    g.fill(roundRect);
//
//    g.setTransform(orig);
//  }

  // From "Java 2D Trickery: Light and Shadow"
  // http://weblogs.java.net/blog/2006/07/27/java-2d-trickery-light-and-shadow
  static Color getMixedColor(Color c1, float pct1, Color c2, float pct2)
  {
    float[] clr1 = c1.getComponents(null);
    float[] clr2 = c2.getComponents(null);
    for (int i = 0; i < clr1.length; i++) {
      clr1[i] = (clr1[i] * pct1) + (clr2[i] * pct2);
    }
    return new Color(clr1[0], clr1[1], clr1[2], clr1[3]);
  }

  static void paintBorderShadow(Graphics2D g2, Shape clipShape, int shadowWidth,
          Color outer, Color inner)
  {
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
    int sw = shadowWidth * 2;
    for (int i = sw; i >= 2; i -= 2) {
      float pct = (float) (sw - i) / (sw - 1);
      g2.setColor(getMixedColor(inner, pct,
              outer, 1.0f - pct));
      g2.setStroke(new BasicStroke(i, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g2.draw(clipShape);
    }
  }
}

