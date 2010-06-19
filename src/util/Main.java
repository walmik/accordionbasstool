package util;


import javax.swing.JFrame;
import javax.swing.UIManager;


import music.ChordParser;
import render.BassToolFrame;


public class Main {

	// Represents the parsing of a note
	public static class StringParser
	{
		final String fullString;
		private int offset;
		
		public StringParser(String string)
		{
			fullString = string;
			offset = 0;			
		}
		
		public void skipWhiteSpace()
		{
			while (!isDone())
			{
				char nextCh = fullString.charAt(offset);
				if (Character.isWhitespace(nextCh))
					offset++;
				else
					break;
			}
		}
		
		public void skipThrough(char ch)
		{
			while (!isDone())
			{
				char nextCh = fullString.charAt(offset);
				offset++;
				if (nextCh == ch)
					break;
			}
		}
		
		public String input()
		{
			skipWhiteSpace();
			return fullString.substring(offset);
		}
		
		public void incOffset(int inc)
		{
			offset += inc;
		}
		
		public int getOffset()
		{
			return offset;
		}
		
		public boolean isDone()
		{
			return (offset >= fullString.length());
		}
	}
	
	/**
	 * @param args
	 */

	public static void main(String[] args) {
		runUnitTests();
		ChordParser.initChords();
    
    try
    {
      UIManager.setLookAndFeel(
              UIManager.getSystemLookAndFeelClassName());
    } catch (Exception exc)
    {
      
    }


    JFrame frame = new BassToolFrame();
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
		String output = ChordParser.parse(new StringParser("[" + input + "]")).toString();
		
		assert(output.equals(input)) : ("Parsing Test Fail - Input: " + 
									   input + " Output: " + output);
	}

}
