package render;

import java.util.LinkedList;

import javax.swing.AbstractListModel;

import music.ButtonComboSequence;

public class BestMatchListModel extends AbstractListModel {

	ButtonComboSequence[] _currSeqs = null;	
	

	@Override
	public String getElementAt(int index)
  {
    String str = "";

    if ((_currSeqs != null) && (index < _currSeqs.length))
      str = "Option " + index + " (" + _currSeqs[index].getHeur() + ")";

		return str;
	}

  public ButtonComboSequence getSeqAt(int index)
  {

		return (_currSeqs != null ? _currSeqs[index] : null);
	}

	@Override
	public int getSize() {
		return (_currSeqs != null ? _currSeqs.length : 0);
	}
	
	public void setComboSeqs(ButtonComboSequence[] seqs)
	{
		_currSeqs = seqs;
		this.fireContentsChanged(this, 0, _currSeqs.length);
	}
}
