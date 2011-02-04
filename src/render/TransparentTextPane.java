/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 *
 * @author Ilya
 */
public class TransparentTextPane extends JScrollPane
{
  JTextPane textPane = new JTextPane();

  public TransparentTextPane()
  {
    textPane.setBackground(new Color(0, 0, 0, 0));
    textPane.setContentType("text/html");
    textPane.setEditable(false);
    textPane.setOpaque(false);
    textPane.setFont(textPane.getFont().deriveFont(18.f));

    this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

    this.setViewportView(textPane);
  }

  @Override
  public void setFont(Font font)
  {
    super.setFont(font);
    if (textPane != null) {
      textPane.setFont(font);
    }
  }

  @Override
  public Font getFont()
  {
    if (textPane != null) {
      return textPane.getFont();
    } else {
      return super.getFont();
    }
  }

  public void setText(String text)
  {
    textPane.setText(text);
  }

  public String getText()
  {
    return textPane.getText();
  }
}
