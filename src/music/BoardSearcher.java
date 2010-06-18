package music;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

public class BoardSearcher 
{
	final static int MAX_SEQS = 20;
	final static int MAX_COMBOS = 400;
	
	public LinkedList<ButtonComboSequence> parseSequence(BassBoard board, Vector<Chord> chordSeq)
	{
		Enumeration<Chord> eChord = chordSeq.elements();
		
		LinkedList<ButtonComboSequence> currSeqs = new LinkedList<ButtonComboSequence>();
		currSeqs.add(new ButtonComboSequence(board));
		
		LinkedList<ButtonComboSequence> nextSeqs = new LinkedList<ButtonComboSequence>();
		
		while (eChord.hasMoreElements())
		{
			Chord chord = eChord.nextElement();
			ButtonCombo[] combo = null;
			
			long start = System.currentTimeMillis();
			
			if (chord.isSingleNote())
			{
				// Optimized search for notes only
				//combo = findAllNotes(board, chord.notes[0]);
				combo = findAllIter(board, chord);
			}
			else
			{
				combo = findAllIter(board, chord);
			}
			
			long end = System.currentTimeMillis();
			
			System.out.println("Elapsed: " + (end - start));

			
			if (combo == null)
			{
				//TODO handle null case?
				continue;
			}
			
			System.out.println("Combos " + combo.length);
			
			// Iterate over curr seqs
			
			ListIterator<ButtonComboSequence> currIter = 
				currSeqs.listIterator();
			while (currIter.hasNext())
			{
				ButtonComboSequence origSeq = currIter.next();

				for (int i = combo.length - 1; i >= 0; i--)
				{
					ButtonComboSequence seq = origSeq;
					if (i > 0)
					{
						seq = seq.clone();
					}
					seq.add(combo[i]);
					
					int heur = seq.evalHeuristic();
					boolean inserted = false;
					
					// Quick check, if we are at max sequences and the heuristic
					// is greater than the last one, skip immediately
					if ((nextSeqs.size() == MAX_SEQS) && 
						(heur >= nextSeqs.getLast().evalHeuristic()))
					{
						continue;
					}
					
					ListIterator<ButtonComboSequence> nextIter = 
						nextSeqs.listIterator();
					while (nextIter.hasNext())
					{
						int index = nextIter.nextIndex();
						if (heur < nextIter.next().evalHeuristic())
						{
							nextSeqs.add(index, seq);
							inserted = true;
							break;
						}
					}
					
					if (!inserted)
					{
						if (nextSeqs.size() < MAX_SEQS)
						{
							nextSeqs.add(seq);	
						}
					}
					else if (nextSeqs.size() > MAX_SEQS)
					{
						nextSeqs.removeLast();
					}
				}	
			}
			
			// Swap next and current			
			LinkedList<ButtonComboSequence> temp = currSeqs;
			currSeqs = nextSeqs;
			nextSeqs = temp;
			nextSeqs.clear();			
		}
		
//		ListIterator<ButtonComboSequence> currIter = 
//			currSeqs.listIterator();
//		
//		while (currIter.hasNext())
//		{
//			System.out.println("#" + currIter.nextIndex() + " " + currIter.next());
//		}
		
		return currSeqs;
	}
	
	ButtonCombo[] findAllNotes(BassBoard board, Note note)
	{
		Vector<ButtonCombo> combos = new Vector<ButtonCombo>();
		
		for (int r = 0; r < board.getRows(); r++)
		{
			// Only doing single note rows
			if (!board.isSingleBassRow(r))
			{
				continue;
			}
			
			for (int c = 0; c < board.getCols(); c++)
			{
				Chord chord = board.getChordAt(r, c);
				
				if (chord.isSingleNote() && chord.notes[0].equals(note))
				{
					combos.add(new ButtonCombo(new BassBoard.Pos(r, c)));
				}
			}
		}
		
		ButtonCombo[] array = new ButtonCombo[combos.size()];
		return combos.toArray(array);
	}
	

	
	
	class ButtonComboLink
	{
		Chord.Mask chordMask;
		Chord.Mask linkMask;
		
		BassBoard.Pos pos;
		ButtonComboLink last;
		int len;
		int posIndex;
		ButtonCombo theCombo;
		
		String indent;
		
		ButtonComboLink(BassBoard.Pos newPos,
						int index,
						Chord.Mask newChordMask,
						ButtonComboLink lastLink)
		{
			pos = newPos;
			posIndex = index;
			last = lastLink;
			linkMask = newChordMask;
			chordMask = lastLink.chordMask.concat(newChordMask);
			len = lastLink.len + 1;
			theCombo = null;
			
			indent = lastLink.indent + "  ";
		}
		
		ButtonComboLink()
		{
			pos = new BassBoard.Pos(0, -1);
			posIndex = -1;
			last = null;
			chordMask = new Chord.Mask();
			linkMask = new Chord.Mask();
			len = 0;
			theCombo = null;
			
			indent = "";
		}
		
		ButtonCombo getCombo()
		{
			if (theCombo != null)
			{
				return theCombo;
			}
			
			BassBoard.Pos[] posVec = new BassBoard.Pos[len];
			
			ButtonComboLink curr = this;
			int count = 0;
			
			while (curr != null && curr.last != null)
			{
				posVec[count] = curr.pos;
				count++;
				curr = curr.last;
			}
			
			// verify len is the actual # of links
			assert(count == len);
			
			theCombo = new ButtonCombo(posVec);
			return theCombo;
		}
	}
	
	// All Chords
	ButtonCombo[] findAllIter(BassBoard board, Chord fullChord)
	{
		LinkedList<ButtonCombo> combos = new LinkedList<ButtonCombo>();
		
		// Build mask cache, use instead of board.getChord access		
		Chord.Mask[][] masks = board.buildChordMaskCache();
		Chord.Mask fullChordMask = fullChord.getChordMask();
		
		boolean hasSetRoots = fullChordMask.hasRootBassReq();
		
		Vector<BassBoard.Pos> validPos = new Vector<BassBoard.Pos>();
		
		// Find all button positions that are part of the fullChord
		// to narrow the search space
		
		// First, chord rows
		for (int r = 0; r < board.getRows(); r++)
		{
			if (!board.isSingleBassRow(r))
			{
				for (int c = 0; c < board.getCols(); c++)
				{
					if (fullChordMask.contains(masks[r][c]))
					{
						validPos.add(new BassBoard.Pos(r, c));
					}
				}	
			}
		}
		
		// Non-chord rows
		
		for (int r = 0; r < board.getRows(); r++)
		{
			if (board.isSingleBassRow(r))
			{
				for (int c = 0; c < board.getCols(); c++)
				{
					if (!hasSetRoots)
						masks[r][c].unmaskRegister(fullChordMask);
					
					if (fullChordMask.contains(masks[r][c]))
					{
						validPos.add(new BassBoard.Pos(r, c));
					}
				}	
			}
		}
		
		System.out.println("Num Valid: " + validPos.size());
		
		ButtonComboLink currLink = new ButtonComboLink();
		int startIndex = 0;
		
		while (currLink != null)
		{
			for (int i = startIndex; i < validPos.size(); i++)
			{
				BassBoard.Pos newPos = validPos.elementAt(i);
				int r = newPos.row;
				int c = newPos.col;
				
				Chord.Mask newChordMask = masks[r][c];
				
//					System.out.println(currLink.indent + "R: " + r + " C: " + c + " - " + currLink.chordMask.value);
				
				// If already contain the new chord, skip immediately
				if (currLink.chordMask.contains(newChordMask))
				{
					continue;
				}
				
				
											
				ButtonComboLink newLink = 
					new ButtonComboLink(newPos, i, newChordMask, currLink);
							
				if (newLink.chordMask.equals(fullChord.getChordMask()))
				{
					ListIterator<ButtonCombo> nextIter = 
												combos.listIterator();
					
					int containCount = 0;
					
					ButtonCombo newCombo = newLink.getCombo();
					
					while (nextIter.hasNext())
					{
						ButtonCombo existingCombo = nextIter.next();
						if (existingCombo.contains(newCombo))
						{
							if (containCount == 0)
								nextIter.set(newCombo);
							else
								nextIter.remove();
							
							containCount++;
						}
					}
					
					if (containCount == 0)
					{
						combos.addLast(newCombo);
					}
					
//					System.out.println(newLink.indent + "MATCH " + newLink.chordMask.value);

//					ButtonCombo newCombo = newLink.getCombo();
//					int heur = newCombo.evalHeur(board.getCenter());
//					
//					boolean inserted = false;
//					
//					assert(combos.getLast().heur != 0);
//					
//					// Quick check, if we are at max sequences and the heuristic
//					// is greater than the last one, skip immediately
//					if ((combos.size() == MAX_COMBOS) && 
//						(heur >= combos.getLast().heur))
//					{
//						continue;
//					}
//					
//					ListIterator<ButtonCombo> nextIter = 
//						combos.listIterator();
//					while (nextIter.hasNext())
//					{
//						int index = nextIter.nextIndex();
//						if (heur < nextIter.next().heur)
//						{
//							combos.add(index, newCombo);
//							inserted = true;
//							break;
//						}
//					}
//					
//					if (!inserted)
//					{
//						if (combos.size() < MAX_COMBOS)
//						{
//							combos.add(newCombo);	
//						}
//					}
//					else if (combos.size() > MAX_COMBOS)
//					{
//						combos.removeLast();
//					}
				}
				else
				{
					if (newLink.len >= 4)
						continue;
						
//					if (combos.size() == MAX_COMBOS)
//					{
//						ButtonCombo newCombo = newLink.getCombo();
//						int heur = newCombo.evalHeur(board.getCenter());
//						
//						if (heur >= combos.getLast().heur)
//							continue;
//					}
					
					// Inc length of combo, effectively recurse to next level
					currLink = newLink;	
				}
			}
			
			startIndex = currLink.posIndex + 1;
			
			currLink = currLink.last;
		}		
		
		ButtonCombo[] array = new ButtonCombo[combos.size()];
		return combos.toArray(array);
	}
	
	
	// All Chords
//	ButtonCombo[] findAll(BassBoard board, Chord fullChord)
//	{
//		LinkedList<ButtonCombo> combos = new LinkedList<ButtonCombo>();
//		
//		ButtonCombo emptyCombo = new ButtonCombo();
//		
//		//findSingleCombo(board, fullChord, emptyCombo, combos, 0, 0, "  ");
//				
//		ButtonCombo[] array = new ButtonCombo[combos.size()];
//		return combos.toArray(array);
//	}
//	
//	void  findSingleCombo(BassBoard board, 
//						  Chord fullChord,
//						  ButtonCombo currCombo,
//						  LinkedList<ButtonCombo> combos,
//						  int rStart, int cStart,
//						  String indent)
//	{
//		
//		for (int r = rStart; r < board.getRows(); r++)
//		{
//			for (int c = cStart; c < board.getCols(); c++)
//			{
//				Chord newChord = board.getChordAt(r, c);
//				
//				System.out.println(indent + "R: " + r + " C: " + c + " - " + currCombo.chordMask.value);
//				
//				if (fullChord.contains(newChord) && 
//				    !currCombo.chordMask.contains(newChord))
//				{
//					BassBoard.Pos newPos = new BassBoard.Pos(r, c);
//					
//					int maxHeur = 0;
//					if (combos.size() > 0)
//						maxHeur = combos.getLast().evalHeur(board.getCenter());
//					
//					int heur = 0;
////					heur = currCombo.incHeur(board.getCenter());
////					
////					// If at max seqs and heuristic is above the last max,
////					// skip this combo immediately
////					if ((combos.size() == MAX_SEQS) && 
////						(heur >= maxHeur))
////					{
////						skipCount++;
////						continue;
////					}
//					
//					ButtonCombo newCombo = currCombo.expand(newPos, newChord);
//					heur = newCombo.evalHeur(board.getCenter());
//					
//					// If at max seqs and heuristic is above the last max,
//					// skip this combo immediately
//					if ((combos.size() == MAX_SEQS) && 
//						(heur >= maxHeur))
//					{
//						//skipCount++;
//						continue;
//					}
//					
//					if (newCombo.chordMask.equals(fullChord))
//					{
//						if (heur < maxHeur)
//							combos.addFirst(newCombo);
//						else
//							combos.addLast(newCombo);
//						
//						if (combos.size() > MAX_SEQS)
//							combos.removeLast();
//						
//						return;
//					}
//					else
//					{
//						// Increment next search position
//						// Inc by col, unless at end, then inc row
//						// If at last row, recursion will stop here
//						int newCStart = c + 1;
//						int newRStart = r;
//						
//						if (newCStart >= board.getCols())
//						{
//							newCStart = 0;
//							newRStart++;
//						}
//
//						findSingleCombo(board, fullChord, newCombo, combos, newRStart, newCStart, indent + "  ");
//					}
//				}				
//			}
//			
//			//Reset to 0 to start fully on next row			
//			cStart = 0;
//		}
//		
//		
//	}
//	
	
}
