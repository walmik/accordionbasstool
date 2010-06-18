package render;

import java.util.LinkedList;

import javax.swing.AbstractListModel;

import music.ButtonComboSequence;

public class BestMatchListModel extends AbstractListModel {

	ButtonComboSequence[] _currSeqs = null;	
	

	@Override
	public ButtonComboSequence getElementAt(int index) {
		
		return (_currSeqs != null ? _currSeqs[index] : null);
	}

	@Override
	public int getSize() {
		return (_currSeqs != null ? _currSeqs.length : 0);
	}
	
	public void setComboSeqs(LinkedList<ButtonComboSequence> seqs)
	{
		_currSeqs = new ButtonComboSequence[seqs.size()];
		seqs.toArray(_currSeqs);
		this.fireContentsChanged(this, 0, _currSeqs.length);
	}
}
