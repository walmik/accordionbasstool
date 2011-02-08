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
import javax.swing.ImageIcon;

/**
 *
 * @author Ilya
 */
public class RenderBoardUI
{

  public final static RenderBoardUI defaultUI = new RenderBoardUI();
  private Path2D unpressedCyl, pressedCyl;
  private Ellipse2D _buttonTop, _shadowFloor;
  float shadowScale = 1.1f;
  int defaultDiamX = 25;
  int defaultDiamY = 20;
  int defaultCylHeight = 16;
  float pressedRatio = 0.333f;
  final int buttonXMargin = 10;
  final double ellipseRatio = (3.f / 4.f);
  final double _colToRow = 4.0 / 3.0;
  boolean _isHoriz = true;
  double _defaultSlantAngle = (20.0) * Math.PI / 180;
  final static int _prefSize = 48;
  //BufferedImage selectedIM, unselectedIM, pressedIM;
  boolean use3DDrawer = true;
  int lastIMScale = 0;
  BufferedImage optimalityImage;
  ImageIcon optimalityIcons[];

  public static enum BoardButtonImage
  {

    UNSELECTED(Color.black, 0.75f, false),
    SELECTED(Color.blue, false),
    FAST_CLICK(Color.green, 1.f, true),
    HOVER(Color.green, 0.75f, false),
    REDUNDS(Color.gray, 0.75f, true),
    //Press - Fingers
    PRESSED_2(Color.green, 1.f, true),
    PRESSED_3(Color.cyan, 1.f, true),
    PRESSED_4(Color.orange, 1.f, true),
    PRESSED_5(Color.red, 1.f, true),
    //Pressed - No Fingers
    PRESSED_ANY(Color.magenta, true);

    BoardButtonImage(Color bcol, boolean press)
    {
      color = bcol;
      alpha = 1.0f;
      pressed = press;
    }

    BoardButtonImage(Color bcol, float alph, boolean press)
    {
      color = bcol;
      alpha = alph;
      pressed = press;
    }

    public BufferedImage getImage()
    {
      return image;
    }
    Color color;
    BufferedImage image;
    float alpha;
    boolean pressed;
  }
  int currImageWidth = 1;
  int currImageHeight = 1;

  RenderBoardUI()
  {
    createButtonImages(1);
  }

  private void createButtonImages(int scale)
  {
    if ((scale == lastIMScale)) {
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

    currImageWidth = Math.round(diamX * shadowScale);
    currImageHeight = Math.round(diamY * shadowScale) + cylHeight;

    for (BoardButtonImage boardButton : BoardButtonImage.values()) {
      boardButton.image = new BufferedImage(currImageWidth, currImageHeight, BufferedImage.TYPE_INT_ARGB);
      render3DButton(diamX, diamY, cylHeight, boardButton.image.createGraphics(), boardButton.color, boardButton.pressed);
    }


//    selectedIM = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_INT_ARGB);
//    unselectedIM = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_INT_ARGB);
//    pressedIM = new BufferedImage(imWidth, imHeight, BufferedImage.TYPE_INT_ARGB);
//
//    render3DButton(diamX, diamY, cylHeight, selectedIM.createGraphics(), Color.blue, true, false);
//    render3DButton(diamY, diamY, cylHeight, unselectedIM.createGraphics(), Color.black, false, false);
//    render3DButton(diamX, diamY, cylHeight, pressedIM.createGraphics(), Color.magenta, false, true);
  }

  BoardButtonImage getBoardButtonImage(
          boolean pressed,
          boolean selected,
          boolean redundant,
          int finger)
  {
    if (!selected && !pressed && !redundant) {
      return BoardButtonImage.UNSELECTED;
    }

    if (finger < 2) {
      if (redundant && !pressed) {
        return BoardButtonImage.REDUNDS;
      }

      if (selected && !pressed) {
        return BoardButtonImage.SELECTED;
      }

      return BoardButtonImage.PRESSED_ANY;
    }

    if (finger <= 5) {
      return BoardButtonImage.values()[finger - 2 + BoardButtonImage.PRESSED_2.ordinal()];
    }

    return BoardButtonImage.PRESSED_ANY;
  }

  IconButtonDrawer iconDrawer;
  TextDrawer textDrawer;

  IconButtonDrawer getButtonDrawer()
  {
    if (iconDrawer == null) {
      iconDrawer = new IconButtonDrawer();
    }

    return iconDrawer;
  }

  TextDrawer getTextDrawer()
  {
    if (textDrawer == null) {
      textDrawer = new TextDrawer();
    }

    return textDrawer;
  }

  abstract class ButtonDrawer
  {

    abstract void setup(Graphics2D graphics, int xW, int yW, int diamX, int diamY);

    abstract void draw(Graphics2D graphics, int col, int row,
            boolean selected, BoardButtonImage boardButton);
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
      imWidth = imHeight * currImageWidth / currImageHeight;
      //imWidth = (int)(_diamX * shadowScale);
      //imHeight = imWidth * pressedIM.getHeight() / pressedIM.getWidth();
      _pressedCylOff = (int) ((defaultCylHeight * scale * (1.0 - pressedRatio)) * imWidth / currImageWidth);
      //System.out.println("IM: " + imWidth + ", " + imHeight);

    }

    @Override
    void draw(Graphics2D graphics, int col, int row,
            boolean selected, BoardButtonImage boardButton)
    {
      if (boardButton == null) {
        return;
      }

      //graphics.setComposite(AlphaComposite.SrcAtop);
      //graphics.drawRect(0, 0, _xW, _yW);

      graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, boardButton.alpha));
      graphics.drawImage(boardButton.image, 0, 0, imWidth, imHeight, null);

      //graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.f));

      if (boardButton.pressed) {
        //     graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        //graphics.drawImage(pressedIM, 0, 0, imWidth, imHeight, null);
        graphics.translate(0, _pressedCylOff);
      } else if (selected) {
        //     graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        //graphics.drawImage(selectedIM, 0, 0, imWidth, imHeight, null);
      } else {
        //     graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        //graphics.drawImage(unselectedIM, 0, 0, imWidth, imHeight, null);
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
    void draw(Graphics2D graphics, int col, int row,
            boolean selected, BoardButtonImage boardButton)
    {
      Color lighterFill = Color.LIGHT_GRAY;
      Color darkerFill = Color.GRAY;

      if (selected) {
        //TODO: System Color
        //lighterFill = Color.MAGENTA;
        if (boardButton.pressed) {
          lighterFill = Color.magenta;
          darkerFill = Color.magenta.darker();
        } else {
          lighterFill = Color.blue;
          darkerFill = Color.blue.darker();
        }
      }

      if (boardButton.pressed) {
        // Draw Pressed
        graphics.translate(0, _pressedCylOff);
        graphics.setColor(darkerFill);
        graphics.fill(pressedCyl);
        graphics.setColor(lighterFill);
        graphics.fill(_buttonTop);
      } else {
        // Draw Unpressed
        graphics.setColor(darkerFill);
        graphics.fill(unpressedCyl);
        graphics.setColor(lighterFill);
        graphics.fill(_buttonTop);
      }
    }
  }

  static class TextDrawer
  {

    private FontMetrics _fm;
    private int _tX, _tY;
    //private int _textHeight;
    private Font _origFont = new Font("Default", Font.BOLD, 14);
    private Font _font = _origFont;

    float minSize = 10.f;

    void setup(Graphics2D graphics, int xW, int yW, int diamX, int diamY)
    {
      // Text Set up
      int fontHeight = graphics.getFontMetrics(_origFont).getHeight();
      _font = _origFont.deriveFont(_origFont.getSize2D() * (diamY * .5f) / fontHeight);

      if (_font.getSize() < minSize) {
        return;
      }

      graphics.setFont(_font);
      _fm = graphics.getFontMetrics();

      int textDescent = 3;
      //_textHeight = _fm.getHeight() - 4;

      _tX = RenderBoardUI.defaultUI.getButtonDrawer().imWidth / 2 - 2;
      _tY = diamY / 2 + textDescent;
    }

    void setFont(Graphics2D graphics)
    {
      graphics.setFont(_font);
    }

    void draw(Graphics2D graphics, int col, int row, BoardButtonImage buttonImage, String chordStr)
    {
      if (_font.getSize() < minSize) {
        return;
      }

      if (buttonImage.pressed) {
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
          boolean pressed)
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

      // Draw Shadow Gray
      graphics.setComposite(AlphaComposite.SrcOver);
      graphics.setPaint(new Color(0.f, 0.f, 0.f, 0.25f));
      graphics.fill(_shadowFloor);

      // Draw Shadow Color
      graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
      graphics.setPaint(selColor);
      graphics.fill(_shadowFloor);

      graphics.translate(0, -currCylHeight);
    }

    Point2D start = new java.awt.geom.Point2D.Float(0, diamY);
    Point2D end = new Point2D.Float(diamX, diamY);
    float fractions[] = {0.0f, 0.3f, 1.0f};
    Color colors[] = {Color.darkGray, Color.white, Color.black};

    Paint cylPaint = new LinearGradientPaint(start, end, fractions, colors);


    // Draw Cyl Color
    graphics.setComposite(AlphaComposite.SrcOver);
    graphics.setPaint(selColor);
    graphics.fill(pressed ? pressedCyl : unpressedCyl);

    // Draw Cyl Gradient Composite
    graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
    graphics.setPaint(cylPaint);
    graphics.fill(pressed ? pressedCyl : unpressedCyl);


    // Draw Button Top Color
    //graphics.setComposite(AlphaComposite.SrcOver);
    graphics.setPaint(selColor);
    graphics.fill(_buttonTop);

    // LINEAR GRADIENT
    start = new java.awt.geom.Point2D.Float(0, diamY / 2);
    end = new Point2D.Float(diamX, 0);
    float frac3[] = {0.0f, 1.0f};
    Color col3[] = {Color.black, Color.white};

    // Draw Button Top Linear Gradient
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

    // Draw Button Top Radial Hightlight
    graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (pressed ? 0.9f : 0.5f)));
    graphics.fill(_buttonTop);
  }

  // Optimality Icons
  public void renderOptimalityImage()
  {
    int imageDim = 32;

    float fractions[] = {0.0f, 0.5f, 1.0f};
    Color colors[] = {Color.green.darker(), Color.yellow, Color.red};
    optimalityImage = new BufferedImage(imageDim, imageDim, BufferedImage.TYPE_INT_ARGB);
    LinearGradientPaint gradient = new LinearGradientPaint(0, imageDim / 2, imageDim, imageDim / 2, fractions, colors);

    Graphics2D graphics = (Graphics2D) optimalityImage.getGraphics();
    graphics.setPaint(gradient);
    graphics.fillRect(0, 0, imageDim, imageDim);
  }

  public void buildOptimalityIcons(int numIcons)
  {
    optimalityIcons = new ImageIcon[numIcons];

    // Golden Ratio
    int iconHeight = 16;
    int iconWidth = (int) (iconHeight);// * 1.61803399);

    int stepWidth = optimalityImage.getWidth() / numIcons;
    int stepHeight = optimalityImage.getHeight();

    for (int i = 0; i < optimalityIcons.length; i++) {
      optimalityIcons[i] = new ImageIcon(optimalityImage.getSubimage(i * stepWidth, 0, stepWidth, stepHeight).
              getScaledInstance(iconWidth, iconHeight, BufferedImage.SCALE_FAST));
    }
  }

  public ImageIcon getOptimalityIcon(int index)
  {
    return optimalityIcons[index];
  }

  public int getNumOptimalityIcons()
  {
    return optimalityIcons.length;
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

