package render;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.Main.StringParser;

import music.BassBoard;
import music.BoardSearcher;
import music.ButtonComboSequence;
import music.Chord;
import music.ChordParser;

public class BassPatterFinderController extends JPanel
{
	final BoardSearcher _searcher = new BoardSearcher();
	RenderBassBoard _renderBoard = null;
	
	Vector<Chord> _currChordSeq;
	
	JTextArea _chordInputArea;
	JButton  _computeButton;
	JSlider _comboSlider;
	JList   _seqList;
	
	BestMatchListModel _bestMatchListModel = null;
	final SelectedButtonCombo _selCombo = new SelectedButtonCombo();
	ButtonComboSequence _selSeq = null;
	
	public void init(RenderBassBoard bassBoard)
	{
		_renderBoard = bassBoard;
		_renderBoard._selCombo = _selCombo;
		initUI();
	}	
	
	private void initUI()
	{
		// Entry Panel
		JPanel entryPan = new JPanel();
		entryPan.setLayout(new BoxLayout(entryPan, BoxLayout.Y_AXIS));
		entryPan.setBorder(BorderFactory.createTitledBorder("Chord/Note Sequence"));

		_chordInputArea = new JTextArea(5, 5);
		entryPan.add(_chordInputArea);
		_computeButton = new JButton("Find Sequence...");
		entryPan.add(_computeButton);
		
		// Results
		JPanel resultsPan = new JPanel();
		resultsPan.setLayout(new BoxLayout(resultsPan, BoxLayout.Y_AXIS));
		resultsPan.setBorder(BorderFactory.createTitledBorder("Bass Sequences:"));
		_comboSlider = new JSlider(JSlider.HORIZONTAL);
		resultsPan.add(BorderLayout.NORTH, _comboSlider);
		_bestMatchListModel = new BestMatchListModel();
		_seqList = new JList(_bestMatchListModel);
		resultsPan.add(BorderLayout.CENTER, new JScrollPane(_seqList));
		
		
		_comboSlider.setPaintLabels(true);
		_comboSlider.setPaintTicks(false);
		_comboSlider.setPaintTrack(false);
		_comboSlider.setSnapToTicks(true);
		_comboSlider.setEnabled(false);
		_comboSlider.setMinimum(-1);
		
		// Add Together
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(BorderLayout.NORTH, entryPan);
		add(BorderLayout.CENTER, resultsPan);
		
		initUIListeners();
		
		//debug
		String t = "[C], [D], [E], [F], [G], [A], [B], [C]";
		_chordInputArea.setText(t);
	}
	
	private void initUIListeners()
	{
		// Compute Button Click Event
		_computeButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0) {
				computeChordSeqs();
			}	
		});
		
		// Sequence List Selection Event
		_seqList.addListSelectionListener(new ListSelectionListener()
		{

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				selectComboSeq(_seqList.getSelectedIndex());				
			}
		});
		
		// Slider Change Event
		_comboSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent arg0) {
				_selCombo.setSelectedIndex(_comboSlider.getValue());
				_renderBoard.repaint();
			}
		});
	}
	
	void computeChordSeqs()
	{
		//TODO: err check
		String text = _chordInputArea.getText();
		_currChordSeq = ChordParser.parseChords(new StringParser(text));
		
		
		_comboSlider.setMaximum(_currChordSeq.size() - 1);
		
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		
		labelTable.put(new Integer(-1), new JLabel("None"));
		
		for (int i = 0; i < _currChordSeq.size(); i++)
		{
			labelTable.put(new Integer(i), new JLabel(_currChordSeq.elementAt(i).toString()));
		}
		
		//labelTable.put(new Integer(_currChordSeq.size()), new JLabel("All"));
		
		_comboSlider.setLabelTable(labelTable);
		
		_selSeq = null;
		
		_bestMatchListModel.setComboSeqs(
				_searcher.parseSequence(_renderBoard.getBassBoard(), _currChordSeq));
		
		_comboSlider.setValue(-1);
		
	}
	
	private void selectComboSeq(int index)
	{
		_selSeq = _bestMatchListModel.getElementAt(index);
		_selCombo.setButtonComboSeq(_selSeq);

		
		if (_selSeq == null)
		{
			_comboSlider.setMaximum(-1);
			_comboSlider.setEnabled(false);
		}
		else
		{
			_comboSlider.setEnabled(true);
			//_comboSlider.requestFocus();
		}
		
		_comboSlider.setValue(-1);
		_renderBoard.repaint();
	}
}
