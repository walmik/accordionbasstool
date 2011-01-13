/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 *
 * @author Ilya
 */
public class AppletController implements ActionListener
{

  /*
   * To change this template, choose Tools | Templates
   * and open the template in the editor.
   */
  JFrame frame;
  JButton toggleFrameButton;
  JApplet theApplet;
  BorderLayout frameLayout, appletLayout;

  /** Initializes the applet AccordApplet */
  public AppletController(JApplet app)
  {
    theApplet = app;
    LayoutManager objLayout = theApplet.getContentPane().getLayout();
    assert (objLayout instanceof BorderLayout);
    appletLayout = (BorderLayout) objLayout;


    frame = new JFrame(theApplet.getName());
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frameLayout = new BorderLayout();
    frame.getContentPane().setLayout(frameLayout);
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

    theApplet.getContentPane().add(BorderLayout.NORTH, toggleFrameButton);
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    toggleFrame(!frame.isVisible());
  }

  private void toggleFrame(boolean seperate)
  {
    if (seperate) {
      toggleFrameButton.setText("Hide Seperate Window");

      Component comp = appletLayout.getLayoutComponent(BorderLayout.CENTER);

      if (comp != null) {
        theApplet.getContentPane().remove(comp);
        theApplet.getContentPane().repaint();

        frame.getContentPane().add(BorderLayout.CENTER, comp);
        frame.pack();
        frame.setVisible(true);
      }

      theApplet.getContentPane().add(BorderLayout.CENTER, toggleFrameButton);

    } else {
      toggleFrameButton.setText("Show In Separate Window");

      Component comp = frameLayout.getLayoutComponent(BorderLayout.CENTER);

      if (comp != null) {
        frame.setVisible(false);
        frame.getContentPane().remove(comp);
        theApplet.getContentPane().add(BorderLayout.CENTER, comp);
      }

      theApplet.getContentPane().add(BorderLayout.NORTH, toggleFrameButton);
    }
  }
}
