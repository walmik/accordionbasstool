package music.input;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import javax.swing.JFrame;
import javax.swing.JPanel;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ilya
 */
public class GraphPanel extends JPanel
{
  byte[] plotData;

  void setPlot(byte[] newData)
  {
    plotData = newData;
    repaint();
  }

  void scale(Point p, int index)
  {
    p.x = (index * getWidth() / plotData.length);
    p.y = (plotData[index] * getHeight() / (Byte.MAX_VALUE - Byte.MIN_VALUE)) + (getHeight() / 2);
  }

  @Override
  public synchronized void paintComponent(Graphics g)
  {
    if (plotData == null || plotData.length == 0) {
      return;
    }

    Graphics2D g2 = (Graphics2D)g;
    Path2D.Float line = new Path2D.Float();
    
    g2.setColor(getBackground());
    g2.fillRect(0, 0, getWidth(), getHeight());

    Point p = new Point();

    scale(p, 0);
    line.moveTo(p.x, p.y);

    for (int i = 1; i < plotData.length; i++)
    {
      scale(p, i);
      line.lineTo(p.x, p.y);
    }

    g2.setColor(getForeground());
    g2.draw(line);
  }

  public void showTopLevel()
  {
    JFrame frame = new JFrame("Pitch Detector v0.1");
    frame.setSize(600, 400);
    setBackground(Color.white);
    frame.getContentPane().add(this);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }
}
