package util;

import music.StringParser;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.xml.parsers.DocumentBuilderFactory;


import music.ChordParser;
import org.w3c.dom.Document;
import render.BassToolFrame;

public class Main
{
  /**
   * @param args
   */
  public static JFrame _rootFrame;

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
    try {
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          return true;
        }
      }
    } catch (Exception e) {
      return false;
      // If Nimbus is not available, you can set the GUI to another look and feel.
    }

    return false;
  }

  static void miscTest()
  {
//    String notes = "C Eb Gb Bbb Db";
//    Chord chord = music.ChordParser.parseNoteList(new StringParser(notes));
//    System.out.println(new RelChord(chord));
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


    JFrame frame = new BassToolFrame();
    _rootFrame = frame;
    frame.setVisible(true);

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
