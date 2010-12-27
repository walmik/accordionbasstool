package music;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

public class BoardSearcher
{

  final static int MAX_SEQS = 20;
  final static int MAX_COMBOS = 400;
  final static boolean debugOut = false;

  public ButtonComboSequence[] parseSequence(BassBoard board, Vector<Chord> chordSeq)
  {
    LinkedList<ButtonComboSequence> currSeqs = new LinkedList<ButtonComboSequence>();
    if (!chordSeq.isEmpty()) {
      currSeqs.add(new ButtonComboSequence(board));
    }

    LinkedList<ButtonComboSequence> nextSeqs = new LinkedList<ButtonComboSequence>();

    for (Chord chord : chordSeq) {

      LinkedList<ButtonCombo> combo = null;

      long start = (debugOut ? System.currentTimeMillis() : 0);

      // Iterate over all combinations of buttons
      combo = findAllIter(board, chord);

      if (debugOut) {
        long end = System.currentTimeMillis();
        System.out.println("Elapsed: " + (end - start));
      }


      if (combo == null) {
        //TODO handle null case?
        continue;
      }

      if (debugOut) {
        System.out.println("Combos " + combo.size());
      }

      // Iterate over curr seqs

      for (ButtonComboSequence origSeq : currSeqs) {

        for (ButtonCombo curr : combo) {
          ButtonComboSequence seq;

          if (curr == combo.getLast()) {
            seq = origSeq;
          } else {
            seq = origSeq.clone();
          }

          seq.add(curr);

          sortedInsert(nextSeqs, seq, MAX_SEQS);
        }
      }

      // Swap next and current
      LinkedList<ButtonComboSequence> temp = currSeqs;
      currSeqs = nextSeqs;
      nextSeqs = temp;
      nextSeqs.clear();
    }

    ButtonComboSequence[] seqArray = new ButtonComboSequence[currSeqs.size()];
    currSeqs.toArray(seqArray);
    return seqArray;
  }

  LinkedList<ButtonCombo> findAllNotes(BassBoard board, Note note)
  {
    LinkedList<ButtonCombo> combos = new LinkedList<ButtonCombo>();

    for (int r = 0; r < board.getNumRows(); r++) {
      // Only doing single note rows
      if (!board.isSingleBassRow(r)) {
        continue;
      }

      for (int c = 0; c < board.getNumCols(); c++) {
        Chord chord = board.getChordAt(r, c);

        if (chord.isSingleNote() && chord.notes[0].equals(note)) {
          combos.add(new ButtonCombo(new BassBoard.Pos(r, c), board));
        }
      }
    }

    return combos;
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

      if (debugOut) {
        indent = lastLink.indent + "  ";
      }
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

      if (debugOut) {
        indent = "";
      }
    }

    ButtonCombo getCombo(BassBoard board)
    {
      if (theCombo != null) {
        return theCombo;
      }

      BassBoard.Pos[] posVec = new BassBoard.Pos[len];

      ButtonComboLink curr = this;
      int count = 0;

      while (curr != null && curr.last != null) {
        posVec[count] = curr.pos;
        count++;
        curr = curr.last;
      }

      // verify len is the actual # of links
      assert (count == len);

      theCombo = new ButtonCombo(posVec, board);
      return theCombo;
    }
  }
  //Options
  public static boolean optStrictBass = true;
  public static boolean optIgnoreBassOnly = true;
  public static boolean optAllowBassOnlyIfNoChords = true;
  public static int optMaxComboLength = 4;

  // All Chords
  LinkedList<ButtonCombo> findAllIter(BassBoard board, Chord fullChord)
  {
    LinkedList<ButtonCombo> combos = new LinkedList<ButtonCombo>();

    // Build mask cache, use instead of board.getChord access
    Chord.Mask[][] masks = board.buildChordMaskCache();
    Chord.Mask fullChordMask = fullChord.getChordMask();
    int lowestBit = Integer.numberOfTrailingZeros(fullChordMask.getValue());

    // ***************************
    boolean restrictLowerBasses = optStrictBass && (lowestBit < Note.NUM_HALFSTEPS);
    boolean ignoreAllBassCombos = optIgnoreBassOnly;
    boolean chordComboFound = !optAllowBassOnlyIfNoChords;
    // ***************************

    Vector<BassBoard.Pos> validPos = new Vector<BassBoard.Pos>();

    // Find all button positions that are part of the fullChord
    // to narrow the search space

    // First, chord rows
    for (int r = 0; r < board.getNumRows(); r++) {
      if (!board.isSingleBassRow(r)) {
        for (int c = 0; c < board.getNumCols(); c++) {
          if (fullChordMask.contains(masks[r][c])) {
            validPos.add(new BassBoard.Pos(r, c));
          }
        }
      }
    }

    // Non-chord rows
    for (int r = 0; r < board.getNumRows(); r++) {
      if (board.isSingleBassRow(r)) {
        for (int c = 0; c < board.getNumCols(); c++) {
          if (restrictLowerBasses) {
            masks[r][c].unmaskLowRegisterAndAbove(fullChordMask, lowestBit);
          } else {
            masks[r][c].unmaskRegister(fullChordMask);
          }

          if (fullChordMask.contains(masks[r][c])) {
            validPos.add(new BassBoard.Pos(r, c));
          }
        }
      }
    }

    if (debugOut) {
      System.out.println("Num Valid: " + validPos.size());
    }

    ButtonComboLink currLink = new ButtonComboLink();
    int startIndex = 0;

    while (currLink != null) {
      for (int i = startIndex; i < validPos.size(); i++) {
        BassBoard.Pos newPos = validPos.elementAt(i);
        int r = newPos.row;
        int c = newPos.col;

        Chord.Mask newChordMask = masks[r][c];

        if (debugOut) {
          System.out.println(currLink.indent + "R: " + r + " C: " + c + " - " + Integer.toBinaryString(currLink.chordMask.getValue()));
        }

        // If already contain the new chord, skip immediately
        if (currLink.chordMask.contains(newChordMask)) {
          continue;
        }

        ButtonComboLink newLink =
                new ButtonComboLink(newPos, i, newChordMask, currLink);

        // See if the new link is a finished combo
        if (newLink.chordMask.equals(fullChord.getChordMask())) {
          ListIterator<ButtonCombo> nextIter =
                  combos.listIterator();

          int containCount = 0;

          ButtonCombo newCombo = newLink.getCombo(board);

          while (nextIter.hasNext()) {
            ButtonCombo existingCombo = nextIter.next();
            if (existingCombo.contains(newCombo)) {
              if (containCount == 0) {
                nextIter.set(newCombo);
              } else {
                nextIter.remove();
              }

              containCount++;
            }
          }

          // If we're already contained in the existing combo, ignore
          if (containCount != 0) {
            continue;
          }

          if (ignoreAllBassCombos) {
            boolean isBassOnly = newCombo.isUsingBassOnly();
            if (chordComboFound && isBassOnly) {
              continue;
            }

            chordComboFound = (chordComboFound || !isBassOnly);
          }

          // Insert any combo-level heuristic filtering here...
          if (newCombo.isAcceptable()) {
            combos.addLast(newCombo);
          }
        } else {
          // Combo not matched, continue building combo
          // Ignore combos of optMaxComboLength or more in length
          if (newLink.len >= optMaxComboLength) {
            continue;
          }

          // Inc length of combo, effectively recurse to next level
          currLink = newLink;
        }
      }

      startIndex = currLink.posIndex + 1;

      currLink = currLink.last;
    }

    return combos;
  }

  public static <T extends CollSequence>
          boolean sortedInsert(LinkedList<T> nextSeqs, T newSeq, int maxSeqs)
  {
    boolean inserted = false;

    int heur = newSeq.getHeur();

    // Quick check, if we are at max sequences and the heuristic
    // is greater than the last one, skip immediately
    if ((nextSeqs.size() == maxSeqs)
            && (heur >= nextSeqs.getLast().getHeur())) {
      return inserted;
    }

    ListIterator<T> nextIter =
            nextSeqs.listIterator();
    while (nextIter.hasNext()) {
      int index = nextIter.nextIndex();
      if (heur < nextIter.next().getHeur()) {
        nextSeqs.add(index, newSeq);
        inserted = true;
        break;
      }
    }

    if (!inserted) {
      if (nextSeqs.size() < maxSeqs) {
        nextSeqs.add(newSeq);
      }
    } else if (nextSeqs.size() > maxSeqs) {
      nextSeqs.removeLast();
    }

    return inserted;
  }
}
