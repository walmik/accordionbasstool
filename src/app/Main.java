package app;

import java.awt.BorderLayout;
import java.awt.Color;
import music.StringParser;
import java.net.URL;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.xml.parsers.DocumentBuilderFactory;


import music.ChordParser;
import org.w3c.dom.Document;
import render.BassToolFrame;
import render.ToolMode;

public class Main
{

  /**
   * @param args
   */
  //public static JFrame _rootFrame;
  private static Document tryDoc(DocumentBuilderFactory dbf, String string)
  {
    try {
      URL url = Main.class.getClassLoader().getResource(string);
      if (url != null) {
        string = url.toString();
      }

      Document doc = dbf.newDocumentBuilder().parse(string);

      System.out.println("Read Doc From: " + string);

      return doc;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static Document loadXmlFile(String filename, String optDir)
  {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    Document doc = tryDoc(dbf, filename);

    if ((doc == null) && (optDir != null)) {
      doc = tryDoc(dbf, optDir + filename);
    }

    return doc;
  }

  public static boolean setNimbus()
  {
    return setLookAndFeel("Nimbus");
  }

  public static boolean setLookAndFeel(String lookandfeel)
  {
    if (UIManager.getLookAndFeel().getName().contains(lookandfeel)) {
      return true;
    }

    try {
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if (info.getName().equals(lookandfeel)) {
          UIManager.setLookAndFeel(info.getClassName());
          return true;
        }
      }
    } catch (Exception e) {
      return false;
    }

    return false;
  }

//  static void themeUI()
//  {
//    UIManager.put("nimbusBase", getHSBColor(0, 162, 140));
//
//    NimbusThemeCreator.main(new String[0]);
//  }


  static Color getHSBColor(int h, int s, int b)
  {
    return new Color(Color.HSBtoRGB(h / 255.f, s / 255.f, b / 255.f));
  }

  static void miscTest()
  {
//    String notes = "C Eb Gb Bbb Db";
//    Chord chord = music.ChordParser.parseNoteList(new StringParser(notes));
//    System.out.println(new RelChord(chord));
  }

  static void initApplet(JApplet applet)
  {
    applet.setSize(400, 400);
    applet.init();
    JFrame frame = new JFrame();
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(applet, BorderLayout.CENTER);
    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args)
  {
    miscTest();
    try {
      boolean success = false;
      success = setNimbus();

      if (!success) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }

    } catch (Exception exc) {
    }


    //themeUI();
    
    JFrame frame = new BassToolFrame(ToolMode.findMode("BoardOnly"));
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//    initApplet(new AccordionBoardViewer());
//    initApplet(new ClickChordMatcher());

//		JFrame jframe = new JFrame("BassLayout");
//		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		//jframe.setPreferredSize(new Dimension(335, 500));
//
//		BassBoard board = BassBoard.bassBoard120();
//
//		BoardPanel boardPanel = new BoardPanel();
//    boardPanel.setBassBoard(board);
//    _mainBoardPanel = boardPanel;
//
//		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
////		tabs.addTab("Common Chords", new JPanel());
//		tabs.addTab("Advanced", new TabCustomBass());
//    tabs.addTab("Common Chords", new TabCommonChords());
//
//		JPanel content = new JPanel();
//		content.setLayout(new BorderLayout(0, 0));
//		content.add(BorderLayout.SOUTH, boardPanel);
//		content.add(BorderLayout.CENTER, tabs);
//
//		jframe.setContentPane(content);
//		jframe.pack();
//		jframe.setVisible(true);
  }

  public static void runUnitTests()
  {
    verifyChordString("A");
    verifyChordString("CDbEFb");
  }

  public static void verifyChordString(String input)
  {
    String output = ChordParser.parse(new StringParser("[" + input + "]"), false).toString();

    assert (output.equals(input)) : ("Parsing Test Fail - Input: "
            + input + " Output: " + output);
  }
}
