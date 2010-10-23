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

  public BassBoard getBoard()
  {
    return board;
  }
	
	public void add(ButtonCombo newCombo)
	{
		combos.add(newCombo);
		heur = 0;
	}

  public int getHeur()
  {
    if (heur == 0)
      return evalHeuristic();
    else
      return heur;
  }

  @Override
	public String toString()
	{
		String str = "Heur: " + evalHeuristic() + " Seq: ";
		for (int i = 0; i < combos.size(); i++)
		{
			str += combos.elementAt(i).toString() + ", ";
		}
		
		return str;
	}
	
  @Override
	public ButtonComboSequence clone()
	{
		ButtonComboSequence copy = 
			new ButtonComboSequence((Vector<ButtonCombo>)combos.clone(), board);
		
		return copy;
	}

  public int debugForceHeur()
  {
    heur = 0;
    return evalHeuristic();
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
		
		GeoPos center = GeoPos.zero();
		GeoPos minP = GeoPos.maxPos();
		GeoPos maxP = GeoPos.minPos();
		
		ButtonCombo firstCombo = null;

    final BassBoard.Pos boardCenter = board.getCenter();
		
		while (combosList.hasMoreElements())
		{
			ButtonCombo combo = combosList.nextElement();
			
			dist += combo.evalHeur(boardCenter, maxP, minP);
			
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