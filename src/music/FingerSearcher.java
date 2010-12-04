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
  final static int MAX_FINGER_SEQS = 50;
  final static int MAX_FINGER_SEQS_PER_BUTTONSEQ = 40;
  static boolean debugOut = false;
  final static int FINGER_2 = 0;
  final static int FINGER_3 = 1;
  final static int FINGER_4 = 2;
  final static int FINGER_5 = 3;

  public FingerComboSequence[] findAllFingers(ButtonComboSequence[] allseqs)
  {
    LinkedList<FingerComboSequence> allFingerComboSeqs = new LinkedList<FingerComboSequence>();

    for (ButtonComboSequence butseq : allseqs) {
      LinkedList<FingerComboSequence> newFingerSeqs = this.findAllFingers(butseq, MAX_FINGER_SEQS_PER_BUTTONSEQ);

      for (FingerComboSequence newSeq : newFingerSeqs) {
        BoardSearcher.sortedInsert(allFingerComboSeqs, newSeq, MAX_FINGER_SEQS);
      }
    }

    FingerComboSequence[] seqArray = new FingerComboSequence[allFingerComboSeqs.size()];
    allFingerComboSeqs.toArray(seqArray);
    return seqArray;
  }

  public LinkedList<FingerComboSequence> findAllFingers(ButtonComboSequence buttonseq, int maxSeqs)
  {
    LinkedList<FingerComboSequence> currSeqs = new LinkedList<FingerComboSequence>();
    currSeqs.add(new FingerComboSequence(buttonseq));

    LinkedList<FingerComboSequence> nextSeqs = new LinkedList<FingerComboSequence>();

    //ButtonCombo prevButtonCombo = null;

    for (int j = 0; j < buttonseq.getNumCombos(); j++) {
      ButtonCombo buttonCombo = buttonseq.getCombo(j);
      LinkedList<FingerCombo> fingerCombos = null;

      fingerCombos = this.findAllFingers(buttonCombo);

      // Iterate over curr seqs

      for (FingerComboSequence origSeq : currSeqs) {

        for (FingerCombo finger : fingerCombos) {

          FingerCombo lastFingerCombo = origSeq.getLastCombo();

          GeoPos newPos[] = new GeoPos[NUM_FINGERS];
          int transHeur = 0;

          transHeur = evalFingerTransition(newPos, finger, origSeq.currFingerPos, lastFingerCombo);
          if (transHeur < 0) {
            continue;
          }

          FingerComboSequence newSeq;

          if (finger == fingerCombos.getLast()) {
            newSeq = origSeq;
          } else {
            newSeq = origSeq.clone();
          }

          newSeq.add(finger, transHeur, newPos);

          BoardSearcher.sortedInsert(nextSeqs, newSeq, maxSeqs);
        }
      }

      // Swap next and current
      LinkedList<FingerComboSequence> temp = currSeqs;
      currSeqs = nextSeqs;
      nextSeqs = temp;
      nextSeqs.clear();

      //prevButtonCombo = buttonCombo;
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

    // Per Finger

    for (int i = 0; i < reverseFingerMap.length; i++) {
      heur += pos[i].absValue();
    }

    // Per Two Fingers

    GeoPos xAxis = new GeoPos(1, 0, SKEW_GRID, SKEW_ANGLE);
    GeoPos yAxis = new GeoPos(0, 1, SKEW_GRID, SKEW_ANGLE);

    for (int i = 0; i < reverseFingerMap.length - 1; i++) {
      int f1 = reverseFingerMap[i + 1];
      int f0 = reverseFingerMap[i];

      int fingerDist = Math.abs(f1 - f0);
      if (pos[i + 1].manDistTo(pos[i]) > fingerDist * MAX_DIST_BTWN_FINGER) {
        heur += fingerDist * 1000;
        return -1;
      }

      GeoPos d1 = pos[i + 1].subtract(pos[i]);
      if (f1 > f0) {
        d1 = d1.scale(-1);
      }
      double xdot = xAxis.dot(d1);
      double ydot = yAxis.dot(d1);

      if ((ydot < 0.0)) {
        return -1;
      }
//      System.out.println(Math.acos(theDot) * (180 / Math.PI) + " "
//              + String.valueOf(reverseFingerMap[i + 1] + 2) + ": " + pos[i + 1] + "-"
//              + String.valueOf(reverseFingerMap[i] + 2) + ": " + pos[i]);
    }

    // Per 3 Fingers

    for (int i = 0; i < reverseFingerMap.length - 2; i++) {
      GeoPos d1 = pos[i + 1].subtract(pos[i]);
      GeoPos d2 = pos[i + 1].subtract(pos[i + 2]);
      if (d1.dot(d2) <= 0) {
        return -1;
      }
    }

    byte finger2 = fingerMap[FINGER_2];
    byte finger3 = fingerMap[FINGER_3];
    byte finger4 = fingerMap[FINGER_4];
    byte finger5 = fingerMap[FINGER_5];

    // Finger 2 -- penalty if not on a chord row
    if (finger2 != -1) {
      if (combo.board.isSingleBassRow(combo.pos[finger2].row)) {
        //heur += 10;
      }
    }

    // Finger 3 -- penalty if not on a bass row
    if (finger3 != -1) {
      if (!combo.board.isSingleBassRow(combo.pos[finger3].row)) {
        //heur += 10;
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
      heur += 30;
    }

    return heur;
  }

  public int evalFingerTransition(GeoPos[] toPos, FingerCombo toCombo, GeoPos[] fromPos, FingerCombo fromCombo)
  {
    int heur = 0;

    fillFingerPos(toPos, toCombo);

    if (fromPos == null) {
      return heur;
    }

    for (int i = 0; i < NUM_FINGERS; i++) {
      if (fromPos[i] == null) {
        continue;
      }

      int dist = fromPos[i].manDistTo(toPos[i]);

      if ((fromCombo != null) && (toCombo.fingerMap[i] != -1) && (fromCombo.fingerMap[i] != -1)) {
        heur += 100;
        dist *= 2;
      }

      heur += dist;
    }

    return heur;
  }

  private void fillFingerPos(GeoPos[] pos, FingerCombo combo)
  {
    assert(combo.pos.length == combo.reverseFingerMap.length);
    for (int i = 0; i < combo.pos.length; i++) {
      int finger = combo.reverseFingerMap[i];
      pos[finger] = combo.pos[i];
    }

    fillDefaultFingerPos(pos);
  }

  private void fillDefaultFingerPos(GeoPos[] currFingerPos)
  {
    GeoPos naturalFingerDir = new GeoPos(0, 1, SKEW_GRID, SKEW_ANGLE);

    for (int i = 0; i < currFingerPos.length; i++) {
      if (currFingerPos[i] != null) {
        continue;
      }

      int min = -1;
      int max = -1;

      for (int j = i - 1; j >= 0; j--) {
        if (currFingerPos[j] != null) {
          min = j;
          break;
        }
      }

      for (int j = i + 1; j < currFingerPos.length; j++) {
        if (currFingerPos[j] != null) {
          max = j;
          break;
        }
      }

      if ((min >= 0) && (max >= 0)) {
        currFingerPos[i] = currFingerPos[min].add(currFingerPos[max]).divide(2);
      } else if (min >= 0) {
        currFingerPos[i] = currFingerPos[min].subtract(naturalFingerDir.scale(i - min));
      } else if (max >= 0) {
        currFingerPos[i] = currFingerPos[max].add(naturalFingerDir.scale(max - i));
      } else {
        currFingerPos[i] = GeoPos.zero();
      }
    }
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
