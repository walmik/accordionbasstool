/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.parser.ParserDelegator;
import render.BassToolFrame;
import render.ToolMode;

/**
 *
 * @author Ilya
 */
public class AppletMain extends JApplet implements ActionListener, PropertyChangeListener
{

  /*
   * To change this template, choose Tools | Templates
   * and open the template in the editor.
   */
  BassToolFrame frame;
  JButton toggleFrameButton;
  JPanel panel;
  Dimension initialSize = null;
  String currMode = null;
  // For JavaScript comm
  Object jsobj = null;
  Method jsEval = null;

  /** Initializes the applet AccordApplet */
  public AppletMain()
  {
  }

  private ToolMode getFrameParam()
  {
    String modeVal = null;
    if (modeVal == null) {
      modeVal = this.getParameter("mode");
    }
    return ToolMode.findMode(modeVal);
  }

  @Override
  public void init()
  {
    //Workaround for latest JRE bug, must call to setDTD() to non-null
    new ParserDelegator();

    initialSize = this.getSize();

    Main.setNimbus();
    //Main.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    //getContentPane().setLayout(new CenterLayout());
    getContentPane().setLayout(new BorderLayout());
    getContentPane().setBackground(Color.black);
    //horizStrut = Box.createHorizontalStrut(80);

    frame = new BassToolFrame(getFrameParam(), false);

    frame.addWindowListener(new WindowAdapter()
    {

      @Override
      public void windowClosing(WindowEvent e)
      {
        toggleFrame(false);
      }
    });

    toggleFrameButton = new JButton("Show in Separate Window");
    toggleFrameButton.addActionListener(this);

    panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new FlowLayout(FlowLayout.CENTER));
    panel.add(toggleFrameButton);

    frame.addPropertyChangeListener("prefLayoutChange", this);

    toggleFrame(false);
  }

  static class CenterLayout implements LayoutManager
  {

    Component content;

    @Override
    public void addLayoutComponent(String name, Component comp)
    {
      content = comp;
    }

    @Override
    public void layoutContainer(Container parent)
    {
      synchronized (parent.getTreeLock()) {
        if (content == null) {
          if (parent.getComponentCount() > 0) {
            content = parent.getComponent(0);
          } else {
            return;
          }
        }

        Dimension dim = content.getPreferredSize();
        Dimension max = parent.getSize();
        Insets insets = parent.getInsets();
        max.width -= insets.left + insets.right;
        max.height -= insets.top + insets.bottom;

        int x, y;

        if (dim.width > max.width) {
          x = 0;
          dim.width = max.width;
        } else {
          x = (max.width - dim.width) / 2;
        }


        if (dim.height > max.height) {
          y = 0;
          dim.height = max.height;
        } else {
          //y = (max.height - dim.height) / 2;
          y = 0;
        }

        content.setBounds(x, y, dim.width, dim.height);
      }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent)
    {
      synchronized (parent.getTreeLock()) {
        Dimension dim = (content != null) ? content.getMinimumSize() : new Dimension(20, 20);
        Insets insets = parent.getInsets();
        dim.width += insets.left + insets.right;
        dim.height += insets.top + insets.bottom;
        return dim;
      }
    }

    @Override
    public Dimension preferredLayoutSize(Container parent)
    {
      synchronized (parent.getTreeLock()) {
        Dimension dim = (content != null) ? content.getPreferredSize() : new Dimension(20, 20);
        Insets insets = parent.getInsets();
        dim.width += insets.left + insets.right;
        dim.height += insets.top + insets.bottom;
        return dim;
      }
    }

    @Override
    public void removeLayoutComponent(Component comp)
    {
      content = null;
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    if (evt.getPropertyName().equals("prefLayoutChange")) {
      attemptResizeApplet();
    }
  }

  private void attemptResizeApplet()
  {
    if (!isValid()) {
      validate();
    }

    Dimension dim = getPreferredSize();
    attemptResizeApplet(dim);
  }

  private void attemptResizeApplet(Dimension dim)
  {
    try {
//      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
//      dim.width = Math.max(initialSize.width, dim.width);
//      dim.height = Math.max(initialSize.height, dim.height);
//
//      if (dim.equals(initialSize)) {
//        return;
//      }

      String resizeFunc = getParameter("resizeFunc");

      if ((resizeFunc == null) || resizeFunc.isEmpty()) {
        return;
      }

      if (jsobj == null) {
        Class jsClass = Class.forName("netscape.javascript.JSObject");
        Method meth = jsClass.getMethod("getWindow", Applet.class);

        jsobj = meth.invoke(null, (Applet) this);

        jsEval = jsClass.getMethod("eval", String.class);
      }

      String evalStr = resizeFunc + "(" + dim.width + ", " + dim.height + ");";

      if ((jsEval != null) && (jsobj != null)) {
        jsEval.invoke(jsobj, evalStr);
      }

    } catch (Exception e) {
      System.err.println("JavaScript Resizing Probably Failed: " + e);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    toggleFrame(!frame.isVisible());
  }

  @Override
  public String getAppletInfo()
  {
    return frame.getTitle();
  }

  public void setMode(String string)
  {
    currMode = string;
    SwingUtilities.invokeLater(new ModeSwitcher(string));
  }

  class ModeSwitcher implements Runnable
  {

    String modeString;

    ModeSwitcher(String string)
    {
      modeString = string;
    }

    @Override
    public void run()
    {
      try {
        ToolMode mode = ToolMode.findMode(modeString);
        frame.init(mode);
        attemptResizeApplet();

      } catch (Exception arg) {
      }
    }
  }

  private void toggleFrame(boolean seperate)
  {
    frame.tempSound(false);

    if (seperate) {
      toggleFrameButton.setText("Hide Seperate Window");

      frame.restoreRootPane();
      frame.pack();
      frame.setVisible(true);

      getContentPane().removeAll();
      getContentPane().add(panel);

      //this.setRootPane(altRootPane);

    } else {
      toggleFrameButton.setText("Show In Separate Window");

      JRootPane frameRoot = frame.getRootPane();

      frame.setVisible(false);

      //this.setRootPane(frameRoot);
      getContentPane().removeAll();
      getContentPane().add(frameRoot);

      JMenuBar bar = frameRoot.getJMenuBar();

      if (bar != null) {
        bar.add(panel);
      }
    }

    attemptResizeApplet();

    frame.tempSound(true);
  }
}
