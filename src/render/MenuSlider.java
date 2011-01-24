/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package render;

import java.awt.Component;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

/**
 *
 * @author Ilya
 */
public class MenuSlider extends JSlider implements MenuElement
{
  JPanel panel = new JPanel();

  public MenuSlider()
  {
    this.setToolTipText("Sound Volume");
    this.setUI(new javax.swing.plaf.basic.BasicSliderUI(this));
  }

  public void init()
  {
    //setBorder(new CompoundBorder(
    //          new TitledBorder("Volume"),
    //          new EmptyBorder(6, 6, 6, 6)));

//  this.setUI(new javax.swing.plaf.basic.BasicSliderUI(this));
//  this.setPreferredSize(new Dimension(100, 20));

//    Dimension d = this.getUI().getPreferredSize(this);
//    d.width = 60;
//    Insets i = this.getInsets();
//    d.width += i.left + i.right;
//    d.height += i.top + i.bottom;
//    this.setPreferredSize(d);
  }
  
  @Override
  public Component getComponent()
  {
    return this;
  }

  @Override
  public MenuElement[] getSubElements()
  {
    return new MenuElement[0];
  }

  @Override
  public void menuSelectionChanged(boolean isIncluded)
  {
    if (isIncluded) {
      this.setBackground(SystemColor.controlHighlight);
    } else {
      this.setBackground(SystemColor.menu);
    }
  }

  @Override
  public void processKeyEvent(KeyEvent event, MenuElement[] path, MenuSelectionManager manager)
  {
    this.processKeyEvent(event);
  }

  @Override
  public void processMouseEvent(MouseEvent event, MenuElement[] path, MenuSelectionManager manager)
  {
//    if (event.getID() == MouseEvent.MOUSE_ENTERED) {
//      MenuElement[] newPath = new MenuElement[path.length + 1];
//      System.arraycopy(path, 0, newPath, 0, path.length);
//      newPath[path.length] = this;
//      manager.setSelectedPath(newPath);
//    }
//
//    if (event.getID() == MouseEvent.MOUSE_EXITED) {
//      MenuElement[] newPath = new MenuElement[path.length - 1];
//      System.arraycopy(path, 0, newPath, 0, path.length - 1);
//      manager.setSelectedPath(newPath);
//    }

    this.processMouseMotionEvent(event);
  }
}
