/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package music;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 *
 * @author Ilya
 */
public class FingerSearcher
{

  final static int SKEW_GRID = 8;
  final static int SKEW_ANGLE = 20;
  final static int MAX_DIST_BTWN_FINGER = SKEW_GRID * 4;
  final static int MAX_SEQS = 50;
  static boolean debugOut = false;
  final static int FINGER_2 = 0;
  final static int FINGER_3 = 1;
  final static int FINGER_4 = 2;
  final static int FINGER_5 = 3;

  public FingerComboSequence[] findAllFingers(ButtonComboSequence[] allseqs)
  {
    LinkedList<FingerComboSequence> allFingerComboSeqs = new LinkedList<FingerComboSequence>();

    for (ButtonComboSequence butseq : allseqs) {
      LinkedList<FingerComboSequence> newFingerSeqs = this.findAllFingers(butseq);

      for (FingerComboSequence newSeq : newFingerSeqs) {
        ListIterator<FingerComboSequence> iter = allFingerComboSeqs.listIterator();
        boolean inserted = false;

        if ((allFingerComboSeqs.size() == MAX_SEQS)
                && (newSeq.getHeur() >= allFingerComboSeqs.getLast().getHeur())) {
          continue;
        }

        while (iter.hasNext()) {
          int index = iter.nextIndex();
          if (newSeq.getHeur() < iter.next().getHeur()) {
            allFingerComboSeqs.add(index, newSeq);
            inserted = true;
            break;
          }
        }

        if (!inserted) {
          if (allFingerComboSeqs.size() < MAX_SEQS) {
            allFingerComboSeqs.add(newSeq);
          }
        } else if (allFingerComboSeqs.size() > MAX_SEQS) {
          allFingerComboSeqs.removeLast();
        }
      }
    }

    FingerComboSequence[] seqArray = new FingerComboSequence[allFingerComboSeqs.size()];
    allFingerComboSeqs.toArray(seqArray);
    return seqArray;
  }

  public LinkedList<FingerComboSequence> findAllFingers(ButtonComboSequence buttonseq)
  {
    LinkedList<FingerComboSequence> currSeqs = new LinkedList<FingerComboSequence>();
    currSeqs.add(new FingerComboSequence(buttonseq));

    LinkedList<FingerComboSequence> nextSeqs = new LinkedList<FingerComboSequence>();

    ButtonCombo prevButtonCombo = null;

    for (int j = 0; j < buttonseq.getNumCombos(); j++) {
      ButtonCombo buttonCombo = buttonseq.getCombo(j);
      LinkedList<FingerCombo> fingerCombos = null;

      fingerCombos = this.findAllFingers(buttonCombo);

      // Iterate over curr seqs

      for (FingerComboSequence origSeq : currSeqs) {

        for (FingerCombo finger : fingerCombos) {

          FingerCombo lastFingerCombo = origSeq.getLastCombo();

          int transHeur = 0;

          if (lastFingerCombo != null) {
            transHeur = evalFingerTransition(finger, lastFingerCombo);
            if (transHeur < 0) {
              continue;
            }
          }

          FingerComboSequence seq;
          if (finger == fingerCombos.getLast()) {
            seq = origSeq;
          } else {
            seq = origSeq.clone();
          }

          seq.add(finger, transHeur);

          int heur = seq.getHeur();

          boolean inserted = false;

          // Quick check, if we are at max sequences and the heuristic
          // is greater than the last one, skip immediately
          if ((nextSeqs.size() == MAX_SEQS)
                  && (heur >= nextSeqs.getLast().getHeur())) {
            continue;
          }

          ListIterator<FingerComboSequence> nextIter =
                  nextSeqs.listIterator();
          while (nextIter.hasNext()) {
            int index = nextIter.nextIndex();
            if (heur < nextIter.next().getHeur()) {
              nextSeqs.add(index, seq);
              inserted = true;
              break;
            }
          }

          if (!inserted) {
            if (nextSeqs.size() < MAX_SEQS) {
              nextSeqs.add(seq);
            }
          } else if (nextSeqs.size() > MAX_SEQS) {
            nextSeqs.removeLast();
          }
        }
      }

      // Swap next and current
      LinkedList<FingerComboSequence> temp = currSeqs;
      currSeqs = nextSeqs;
      nextSeqs = temp;
      nextSeqs.clear();

      prevButtonCombo = buttonCombo;
    }

    return currSeqs;
  }

  public LinkedList<FingerCombo> findAllFingers(ButtonCombo combo)
  {
    LinkedList<FingerCombo> fingerCombos = new LinkedList<FingerCombo>();

    int comboLen = combo.pos.length;

    GeoPos[] pos = new GeoPos[comboLen];

    for (int i = 0; i < pos.length; i++) {
      pos[i] = new GeoPos(combo.pos[i], combo.board.getCenter(), SKEW_GRID, SKEW_ANGLE);
    }

    if (fingerPerms == null) {
      buildFingerPerms();
    }

    byte[] fingerComboArray = fingerPerms[pos.length - 1];

    // Arrange positions by fingers
    for (int i = 0; i < fingerComboArray.length; i++) {

      byte[] fingerMap = new byte[NUM_FINGERS];
      byte[] reverseFingerMap = new byte[pos.length];

      for (int j = 0; j < fingerMap.length; j++) {
        fingerMap[j] = -1;
      }

      int val = fingerComboArray[i];

      for (byte j = 0; j < comboLen; j++) {
        byte finger = (byte) (val & 0x03);
        reverseFingerMap[j] = finger;
        fingerMap[finger] = j;
        val >>= 2;
      }

      int fingerHeur = evalFingerCombo(combo, pos, reverseFingerMap, fingerMap);

      if (fingerHeur >= 0) {
        fingerCombos.add(new FingerCombo(combo, pos, fingerMap, reverseFingerMap, fingerHeur));
      }
    }

    return fingerCombos;
  }

  int evalFingerCombo(ButtonCombo combo,
          GeoPos pos[],
          byte[] reverseFingerMap,
          byte[] fingerMap)
  {
    int heur = 0;

    GeoPos naturalFingerDir = new GeoPos(0, 1, SKEW_GRID, SKEW_ANGLE);

    for (int i = 0; i < reverseFingerMap.length; i++) {

      if (i < (reverseFingerMap.length - 1)) {
        GeoPos dirVec = pos[i + 1].subtract(pos[i]);

        //heur += (int)((1 - dirVec.dot(naturalFingerDir)) * 10);

        int dist = dirVec.absValue();
        //int dist = pos[i + 1].manDistTo(pos[i]);

        // Filter out max finger dist
//        if (dist > ((reverseFingerMap[i + 1] - reverseFingerMap[i]) * MAX_DIST_BTWN_FINGER)) {
//          return -1;
//        }
      }

      heur += pos[i].absValue() / 2;
    }

//    if (true == true) {
//      return heur;
//    }

    byte finger2 = fingerMap[FINGER_2];
    byte finger3 = fingerMap[FINGER_3];
    byte finger4 = fingerMap[FINGER_4];
    byte finger5 = fingerMap[FINGER_5];

    // Finger 2 -- penalty if not on a chord row
    if (finger2 != -1) {
      if (combo.board.isSingleBassRow(combo.pos[finger2].row)) {
        heur += 10;
      }
    }

    // Finger 3 -- penalty if not on a bass row
    if (finger3 != -1) {
      if (!combo.board.isSingleBassRow(combo.pos[finger3].row)) {
        heur += 10;
      }
    }

    // Finger 4 -- penalty if not on a bass row
    if (finger4 != -1) {
      if (!combo.board.isSingleBassRow(combo.pos[finger4].row)) {
        heur += 10;
      }
    }

    // Finger 5 -- rarely used, penalty for any use
    if (finger5 != -1) {
      heur += 20;
    }

    if (reverseFingerMap.length == 3) {
      //TODO:
    }

    return heur;
  }

  public int evalFingerTransition(FingerCombo toCombo, FingerCombo fromCombo)
  {
    int heur = 0;

    GeoPos naturalFingerDir = new GeoPos(0, 1, SKEW_GRID, SKEW_ANGLE);

    for (int i = 0; i < NUM_FINGERS; i++) {
      
      if (toCombo.fingerMap[i] == -1) {
        continue;
      }

      GeoPos tpos = toCombo.getPosAtFinger(i);
      GeoPos fpos;

      if (fromCombo.fingerMap[i] != -1) {
        // Penalty for same finger use
        heur += 100;
        fpos = fromCombo.getPosAtFinger(i);
      } else {
        int min = -1;
        int max = -1;

        for (int j = i - 1; j >= 0; j--) {
          if (fromCombo.fingerMap[j] >= 0) {
            min = j;
            break;
          }
        }

        for (int j = i + 1; j < NUM_FINGERS; j++) {
          if (fromCombo.fingerMap[j] >= 0) {
            max = j;
            break;
          }
        }

//        if ((min >= 0) && (max >= 0)) {
//          fpos = fromCombo.getPosAtFinger(min).add(fromCombo.getPosAtFinger(max)).divide(2);
//        } else
        if (min >= 0) {
          fpos = fromCombo.getPosAtFinger(min).subtract(naturalFingerDir.scale(i - min));
        } else if (max >= 0) {
          fpos = fromCombo.getPosAtFinger(max).add(naturalFingerDir.scale(max - i));
        } else {
          fpos = GeoPos.zero();
        }
      }

      heur += tpos.manDistTo(fpos);

//      GeoPos dirVec = fpos.subtract(tpos);
//
//      heur += (int)((1 - dirVec.dot(naturalFingerDir)) * 10);
    }

    //TODO

    return heur;
  }

  static byte[][] fingerPerms = null;
  final static int NUM_FINGERS = 4;

  public static void buildFingerPerms()
  {
    fingerPerms = new byte[NUM_FINGERS][];

    for (int i = 0; i < fingerPerms.length; i++) {
      fingerPerms[i] = buildFingerPerms(i + 1);
    }
  }

  public static byte[] buildFingerPerms(int totalLen)
  {
    LinkedList<Integer> fingerVec = new LinkedList<Integer>();
    fingerVec.add(Integer.valueOf(0));

    for (int f = 0; f < totalLen; f++) {
      int startingNum = fingerVec.size();

      for (int j = 0; j < startingNum; j++) {
        for (int i = 0; i < NUM_FINGERS; i++) {
          int val = fingerVec.getFirst().intValue();
          if ((val & (1 << (i + 24))) == 0) {
            int newVal = (1 << (i + 24)) | val | (i << (f * 2));
            fingerVec.add(Integer.valueOf(newVal));
          }
        }
        fingerVec.removeFirst();
      }
    }

    ListIterator<Integer> iter = fingerVec.listIterator();

    byte[] fingerComboArray = new byte[fingerVec.size()];
    int index = 0;

    while (iter.hasNext()) {
      fingerComboArray[index] = (byte) iter.next().intValue();
      index++;
    }

    //*** Debug Out *** //
    if (debugOut) {
      for (int i = 0; i < fingerComboArray.length; i++) {
        int val = fingerComboArray[i];
        for (int j = 0; j < totalLen; j++) {
          System.out.print((val & 0x03) + 2);
          val >>= 2;
        }
        System.out.println();
      }
      System.out.println(fingerVec.size());
    }

    return fingerComboArray;
  }
}
