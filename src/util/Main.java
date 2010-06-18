package util;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import render.RenderBassBoard;
import render.BassPatterFinderController;

import music.BassBoard;
import music.ChordParser;
import render.TabCommonChords;


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

		JFrame jframe = new JFrame("BassLayout");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//jframe.setPreferredSize(new Dimension(335, 500));
		
		BassBoard board = BassBoard.bassBoard120();
		
		RenderBassBoard renderBoard = new RenderBassBoard(board, true);
		
		JScrollPane boardScrollPane = new JScrollPane(renderBoard);
		boardScrollPane.getVerticalScrollBar().setBlockIncrement(24);
		boardScrollPane.getVerticalScrollBar().setUnitIncrement(8);
//		boardScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//		Dimension d = renderBoard.getPreferredSize();
//		d.width += 25;
//		boardScrollPane.setPreferredSize(d);
		BassPatterFinderController seqcontrol = new BassPatterFinderController();
		seqcontrol.init(renderBoard);

		
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
//		tabs.addTab("Common Chords", new JPanel());
		tabs.addTab("Advanced", seqcontrol);
    tabs.addTab("Common Chords", new TabCommonChords());
		
		JPanel content = new JPanel();
		content.setLayout(new BorderLayout(16, 16));
		content.add(BorderLayout.SOUTH, boardScrollPane);
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
