/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
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
  BorderLayout frameLayout, appletLayout;
  JRootPane altRootPane;
  //Component horizStrut;

  /** Initializes the applet AccordApplet */
  public AppletMain()
  {
  }

  private ToolMode getFrameParam()
  {
    String modeVal = this.getParameter("mode");
    return ToolMode.findMode(modeVal);
  }
  JPanel panel;

  @Override
  public void init()
  {
    Main.setNimbus();

    altRootPane = new JRootPane();
    getContentPane().setLayout(new FlowLayout());
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

//  @Override
//  public void setRootPane(JRootPane root)
//  {
//    rootPane = root;
//    this.setLayout(new FlowLayout(FlowLayout.CENTER));
//    this.add(root);
//  }
  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    if (evt.getPropertyName().equals("prefLayoutChange")) {
      attemptResizeApplet();
    }
  }
  Object jsobj = null;
  Method jsEval = null;

  private void attemptResizeApplet()
  {
    validate();
    Dimension dim = this.getRootPane().getPreferredSize();
    attemptResizeApplet(dim);
  }

  private void attemptResizeApplet(Dimension dim)
  {
    try {
//      this.validate();

//      if (true) {
//        return;
//      }

//      Container cont = this;
//      while (cont.getParent() != null) {
//        cont = cont.getParent();
//        System.out.println(cont);
//      }
//
//      if (cont instanceof Window) {
//        ((Window)cont).setSize(dim);
//        ((Window)cont).pack();
//      }

      Dimension max = Toolkit.getDefaultToolkit().getScreenSize();
      dim.width = Math.min(max.width, dim.width);
      dim.height = Math.min(max.height, dim.height);

      if (jsobj == null) {
        Class jsClass = Class.forName("netscape.javascript.JSObject");
        Method meth = jsClass.getMethod("getWindow", Applet.class);

        jsobj = meth.invoke(null, (Applet) this);

        jsEval = jsClass.getMethod("eval", String.class);
      }

      String evalStr = "resizeApplet(" + dim.width + ", " + dim.height + ");";

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

  public boolean setMode(String string)
  {
    try {
      ToolMode mode = ToolMode.findMode(string);

      if (!frame.isVisible()) {
        frame.restoreRootPane();
        frame.init(mode);
        getContentPane().removeAll();
        attemptResizeApplet(frame.getPreferredSize());
        getContentPane().add(frame.getRootPane());
      } else {
        frame.init(mode);
        attemptResizeApplet();
      }

      return true;
    } catch (Exception arg) {
      return false;
    }
  }

  private void toggleFrame(boolean seperate)
  {
    if (seperate) {
      toggleFrameButton.setText("Hide Seperate Window");

      frame.restoreRootPane();
      frame.pack();
      frame.setVisible(true);

      getContentPane().removeAll();
      getContentPane().add(panel);
      //this.setRootPane(altRootPane);

      this.attemptResizeApplet();

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

      this.attemptResizeApplet();
    }

    this.validate();
  }
}
