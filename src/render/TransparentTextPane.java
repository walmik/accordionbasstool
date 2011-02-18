/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;

/**
 *
 * @author Ilya
 */
public class TransparentTextPane extends JScrollPane
{

  JTextPane textPane;

  static class EditAction implements ActionListener
  {

    JPopupMenu popup;
    JMenuItem copy;
    JMenuItem selectAll;
    JTextPane textPane;

    EditAction()
    {
      popup = new JPopupMenu();

      copy = new JMenuItem("Copy", KeyEvent.VK_CONTROL + KeyEvent.VK_C);
      copy.addActionListener(this);

      selectAll = new JMenuItem("Select All", KeyEvent.VK_CONTROL + KeyEvent.VK_A);
      selectAll.addActionListener(this);

      popup.add(copy);
      popup.add(new JSeparator());
      popup.add(selectAll);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
      if (textPane == null) {
        return;
      }

      if (e.getSource() == selectAll) {
        textPane.requestFocus();
        textPane.select(1, textPane.getDocument().getLength());
      } else if (e.getSource() == copy) {
        textPane.copy();
      }
    }

    private void showPopup(JTextPane pane, MouseEvent e)
    {
      textPane = pane;
      if (e.isPopupTrigger()) {
        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }
  static EditAction editAction = null;

  public TransparentTextPane()
  {
    textPane = new JTextPane();
    textPane.setBackground(new Color(0, 0, 0, 0));
    textPane.setContentType("text/html");
    textPane.setEditable(false);
    textPane.setOpaque(false);
    textPane.setFont(textPane.getFont().deriveFont(18.f));

    if (editAction == null) {
      editAction = new EditAction();
    }

    this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

    this.setViewportView(textPane);

    MouseAdapter adapt = new MouseAdapter()
    {

      @Override
      public void mousePressed(MouseEvent e)
      {
        if (editAction != null) {
          editAction.showPopup(textPane, e);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e)
      {
        if (editAction != null) {
          editAction.showPopup(textPane, e);
        }
      }
    };

    this.addMouseListener(adapt);
    textPane.addMouseListener(adapt);
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
    try {
      textPane.setText(text);
    } catch (NullPointerException exc) {
      System.err.println(exc);
    }
  }

  public String getText()
  {
    return textPane.getText();
  }
}
