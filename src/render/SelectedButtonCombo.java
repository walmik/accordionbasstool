package render;


import javax.swing.DefaultListSelectionModel;
import music.ButtonCombo;
import music.ButtonComboSequence;
import music.FingerComboSequence;

public class SelectedButtonCombo extends DefaultListSelectionModel
{
  private static final long serialVersionUID = 1L;
  ButtonComboSequence _comboSeq = null;
  FingerComboSequence _fingerSeq = null;

  public int getFingerAt(int row, int col)
  {
    if (_fingerSeq == null) {
      return -1;
    }
    int currIndex = this.getAnchorSelectionIndex();
    if ((currIndex < 0) || (_comboSeq == null)) {
      return -1; //none selected
    }
    if (currIndex < _fingerSeq.getNumCombos()) {
      return _fingerSeq.getCombo(currIndex).getFingerAt(row, col);
    }
    return -1;
  }

  public boolean hasButtonInSeq(int row, int col)
  {
    if (_comboSeq == null) {
      return false;
    }
    for (int i = 0; i < _comboSeq.getNumCombos(); i++) {
      if (_comboSeq.getCombo(i).hasButton(row, col)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasButtonPressed(int row, int col)
  {
    int currIndex = this.getAnchorSelectionIndex();
    if ((currIndex < 0) || (_comboSeq == null)) {
      return false; //none selected
    }
    if (currIndex < _comboSeq.getNumCombos()) {
      // return if button is in the currently selected combo seq
      return _comboSeq.getCombo(currIndex).hasButton(row, col);
    }
    return false;
  }

  public void setButtonComboSeq(ButtonComboSequence seq)
  {
    _comboSeq = seq;
    _fingerSeq = null;
    this.fireValueChanged(getAnchorSelectionIndex(), getAnchorSelectionIndex());
  }

  public void setButtonCombo(ButtonCombo combo)
  {
    _comboSeq = new ButtonComboSequence(combo.getBoard());
    _comboSeq.add(combo);
    _fingerSeq = null;
    this.setSelectionInterval(0, 0);
    this.fireValueChanged(getAnchorSelectionIndex(), getAnchorSelectionIndex());
  }

  public void setFingerComboSeq(FingerComboSequence seq)
  {
    _fingerSeq = seq;
    _comboSeq = seq.getButtonComboSeq();
    this.fireValueChanged(getAnchorSelectionIndex(), getAnchorSelectionIndex());
  }
}
