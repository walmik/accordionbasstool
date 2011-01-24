/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import netscape.javascript.JSObject;
import render.BassToolFrame;
import render.BassToolFrame.ToolMode;

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

  private BassToolFrame.ToolMode getFrameParam()
  {
    String modeVal = this.getParameter("mode");
    try {
      return BassToolFrame.ToolMode.valueOf(modeVal);
    } catch (Exception arg) {
      return BassToolFrame.ToolMode.Default;
    }
  }
  JPanel panel;

  @Override
  public void init()
  {
    Main.setNimbus();

    altRootPane = new JRootPane();
    //horizStrut = Box.createHorizontalStrut(80);

    frame = new BassToolFrame(getFrameParam());

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

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    if (evt.getPropertyName().equals("prefLayoutChange")) {
      //Dimension dim = (Dimension) evt.getNewValue();
      attemptResizeApplet();
    }
  }

  JSObject jsobj = null;

  private void attemptResizeApplet()
  {
    try {
      //Dimension dim = this.getRootPane().getSize();
      //if (dim.width < 10 || dim.height < 10) {
      this.getRootPane().validate();
      Dimension dim = this.getRootPane().getPreferredSize();
      //}

      if (jsobj == null) {
        jsobj = netscape.javascript.JSObject.getWindow(this);
      }

      String evalStr = "resizeApplet(" + dim.width + ", " + dim.height + ");";
      System.out.println(evalStr);
      
      jsobj.eval(evalStr);

    } catch (Exception e) {
      System.out.println(e);
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
      ToolMode mode = ToolMode.valueOf(string);

      frame.init(mode);

      if (frame.isVisible()) {
        frame.validate();
      } else {
        this.getContentPane().validate();
        this.validate();
      }

      attemptResizeApplet();

      return true;
    } catch (IllegalArgumentException arg) {
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

      altRootPane.getContentPane().add(BorderLayout.CENTER, panel);
      this.setRootPane(altRootPane);

      this.attemptResizeApplet();
      
    } else {
      toggleFrameButton.setText("Show In Separate Window");

      JRootPane frameRoot = frame.getRootPane();

      frame.setVisible(false);

      this.setRootPane(frameRoot);

      JMenuBar bar = frameRoot.getJMenuBar();

      if (bar != null) {
        bar.add(panel);
      } else {
        getContentPane().add(BorderLayout.NORTH, panel);
      }

      this.attemptResizeApplet();
    }

    this.validate();
  }
}
