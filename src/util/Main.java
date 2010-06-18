package util;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;


import music.BassBoard;
import music.ChordParser;
import render.BoardPanel;
import render.TabCommonChords;
import render.TabCustomBass;


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

  public static BoardPanel _mainBoardPanel = null;

	public static void main(String[] args) {
		runUnitTests();
		ChordParser.initChords();

		JFrame jframe = new JFrame("BassLayout");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//jframe.setPreferredSize(new Dimension(335, 500));
		
		BassBoard board = BassBoard.bassBoard120();
		
		BoardPanel boardPanel = new BoardPanel();
    boardPanel.setBassBoard(board);
    _mainBoardPanel = boardPanel;
		
//		boardScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//		Dimension d = renderBoard.getPreferredSize();
//		d.width += 25;
//		boardScrollPane.setPreferredSize(d);
//		BassPatterFinderController seqcontrol = new BassPatterFinderController();
//		seqcontrol.init(renderBoard);

		
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
//		tabs.addTab("Common Chords", new JPanel());
		tabs.addTab("Advanced", new TabCustomBass());
    tabs.addTab("Common Chords", new TabCommonChords());
		
		JPanel content = new JPanel();
		content.setLayout(new BorderLayout(0, 0));
		content.add(BorderLayout.SOUTH, boardPanel);
		content.add(BorderLayout.CENTER, tabs);

		jframe.setContentPane(content);
		jframe.pack();
		jframe.setVisible(true);
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
