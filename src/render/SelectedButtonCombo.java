package render;

import javax.swing.DefaultSingleSelectionModel;

import music.ButtonCombo;
import music.ButtonComboSequence;

public class SelectedButtonCombo extends DefaultSingleSelectionModel
{
	private static final long serialVersionUID = 1L;
	
	ButtonComboSequence _comboSeq = null;
	
	public void setButtonComboSeq(ButtonComboSequence seq)
	{
		_comboSeq = seq;
		this.setSelectedIndex(-1);
	}
	
	public boolean hasButtonInSeq(int row, int col)
	{
		if (_comboSeq == null)
		{
			return false;
		}
		
		for (int i = 0; i < _comboSeq.getNumCombos(); i++)
		{
			if (_comboSeq.getCombo(i).hasButton(row, col))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasButtonPressed(int row, int col)
	{
		int currIndex = this.getSelectedIndex();
		if ((currIndex < 0) || (_comboSeq == null))
		{
			return false; //none selected
		}
		
		if (currIndex < _comboSeq.getNumCombos())
		{
			// return if button is in the currently selected combo seq
			return _comboSeq.getCombo(currIndex).hasButton(row, col);
		}
		
		if (currIndex == _comboSeq.getNumCombos())
		{
			// return true for all buttons in the all comboseqs
			return hasButtonInSeq(row, col);
		}
		
		// some other invalid state?
		return false;
	}
	
//	public ButtonCombo getCombo()
//	{
//		if (_comboSeq == null)
//		{
//			return null;
//		}
//		
//		int currIndex = this.getSelectedIndex();
//		
//		if ((currIndex < 0) || (currIndex > _comboSeq.getNumCombos()))
//		{
//			return null;
//		}
//		
//		return _comboSeq.getCombo(currIndex);
//	}
}
