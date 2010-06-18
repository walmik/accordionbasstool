package music;

import java.util.Enumeration;
import java.util.Vector;


public class ButtonComboSequence implements Cloneable
{
	private final Vector<ButtonCombo> combos;
	private final BassBoard board;
	
	// Cached heuristic of this combo seq
	private int heur = 0;
	
	public ButtonComboSequence(BassBoard theBoard)
	{
		combos = new Vector<ButtonCombo>();
		board = theBoard;
	}
	
	private ButtonComboSequence(Vector<ButtonCombo> copyCombos,
								BassBoard theBoard)
	{
		combos = copyCombos;
		board = theBoard;
	}
	
	public ButtonCombo getCombo(int num)
	{
		return combos.elementAt(num);
	}
	
	public int getNumCombos()
	{
		return combos.size();
	}
	
	public void add(ButtonCombo newCombo)
	{
		combos.add(newCombo);
		heur = 0;
	}
	
	public String toString()
	{
		String str = "Heur: " + evalHeuristic() + " Seq: ";
		for (int i = 0; i < combos.size(); i++)
		{
			str += combos.elementAt(i).toString() + ", ";
		}
		
		return str;
	}
	
	public ButtonComboSequence clone()
	{
		ButtonComboSequence copy = 
			new ButtonComboSequence((Vector<ButtonCombo>)combos.clone(), board);
		
		return copy;
	}
	
	int evalHeuristic()
	{
		if (heur != 0)
		{
			return heur;
		}
		
		Enumeration<ButtonCombo> combosList = combos.elements();
		
		ButtonCombo prevCombo = null;
		int dist = 0;
		
		BassBoard.Pos center = new BassBoard.Pos(0, 0);
		BassBoard.Pos minP = BassBoard.Pos.maxPos();
		BassBoard.Pos maxP = BassBoard.Pos.minPos();
		
		ButtonCombo firstCombo = null;
		
		while (combosList.hasMoreElements())
		{
			ButtonCombo combo = combosList.nextElement();
			
			dist += combo.evalHeur(board.getCenter(), maxP, minP);
			
			if (prevCombo != null)
			{
				dist += combo.center.manDistTo(prevCombo.center);
			}
			else
			{
				firstCombo = combo;
			}
			
			prevCombo = combo;
		}
		
		// Total row and col range
		maxP.subtract(minP);
		dist += maxP.absValue();
		
		// Last to first button distance
		//dist += prevCombo.distanceTo(firstCombo);
		
		//dist += (meanR / combos.size());
		//dist += (meanC / combos.size());
		
		heur = dist;
		
		return heur;
	}
}