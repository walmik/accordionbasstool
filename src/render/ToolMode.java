package render;

import app.Main;
import org.w3c.dom.*;

public class ToolMode
{
//  BoardOnly("Accordion Board Only"),
//  ChordMatch("Chord Matching"),
//  ChordPicker("Chord Picker"),
//  SeqPicker("Pattern Picker"),
//  Default("Full Tool");

  public static ToolMode findMode(String name)
  {
    initFromXml();

    for (ToolMode tool : allToolModes) {
      if (tool.id.equals(name)) {
        return tool;
      }
    }

    return getDefaultMode();
  }

  public static ToolMode getDefaultMode()
  {
    if (defaultToolMode == null) {
      defaultToolMode = new ToolMode();
    }

    return defaultToolMode;
  }

  static void initFromXml()
  {
    if (allToolModes == null) {
      loadFromXml("toolmodes.xml");
    }
  }

  private static void loadFromXml(String filename)
  {
    Document doc = Main.loadXmlFile(filename, "./xml/");

    if (doc == null) {
      allToolModes = new ToolMode[0];
      return;
    }

    Element root = doc.getDocumentElement();

    NodeList modes = root.getElementsByTagName("mode");

    allToolModes = new ToolMode[modes.getLength()];

    for (int i = 0; i < modes.getLength(); i++) {
      allToolModes[i] = new ToolMode((Element) modes.item(i));
    }
  }

  public static ToolMode[] getAllModes()
  {
    initFromXml();
    return allToolModes;
  }

  @Override
  public String toString()
  {
    return title;
  }

  private ToolMode(Element root)
  {
    id = root.getAttribute("id");
    title = root.getAttribute("title");

    useModel = Boolean.parseBoolean(root.getAttribute("useModel"));
    useTable = Boolean.parseBoolean(root.getAttribute("useTable"));
    useMouse = Boolean.parseBoolean(root.getAttribute("useMouse"));
    useBlankChord = Boolean.parseBoolean(root.getAttribute("useBlankChord"));
    useAllBoards = Boolean.parseBoolean(root.getAttribute("useAllBoards"));
    multiChord = Boolean.parseBoolean(root.getAttribute("multiChord"));

    int boardSize;
    try {
      boardSize = Integer.parseInt(root.getAttribute("startBoardSize"));
    } catch (NumberFormatException n) {
      boardSize = 0;
    }

    startBoardSize = boardSize;

    tabs = loadStringList(root, "tab", "name");
  }

  private String[] loadStringList(Element root, String childElemName, String propName)
  {
    NodeList nodes = root.getElementsByTagName(childElemName);
    String[] strings = new String[nodes.getLength()];

    for (int i = 0; i < nodes.getLength(); i++) {
      strings[i] = ((Element) nodes.item(i)).getAttribute(propName);
    }

    return strings;
  }

  private ToolMode()
  {
    id = "";
    title = "Full";
    useModel = true;
    useTable = true;
    useMouse = true;
    useBlankChord = true;
    useAllBoards = true;
    multiChord = true;
    startBoardSize = 120;

    tabs = new String[]{"all"};
  }
  private static ToolMode defaultToolMode;
  private static ToolMode[] allToolModes;
  final String id;
  final String title;
  final boolean useModel;
  final boolean useTable;
  final boolean useMouse;
  final boolean useBlankChord;
  final boolean useAllBoards;
  final boolean multiChord;
  final int startBoardSize;
  final String[] tabs;
}
