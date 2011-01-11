/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package render;

import music.ButtonCombo;
import music.Chord;
import music.midi.Player;

/**
 *
 * @author Ilya
 */
public class SoundController
{
  Player player;
  boolean soundEnabled = false;
  boolean arpeggiating = false;

  public void setEnabled(boolean enabled)
  {
    soundEnabled = enabled;
    if (player != null) {
      player.stopAll();
    }
  }

  public void setVolume(int value)
  {
    if (player != null) {
      player.setVelocity(value);
    }
  }

  public void setArpeggiating(boolean arp)
  {
    arpeggiating = arp;
  }

  public boolean play(ButtonCombo combo)
  {
    if (combo != null) {
      return play(combo.getChordMask());
    }

    return false;
  }

  public boolean play(Chord chord)
  {
    if (chord != null) {
      return play(chord.getChordMask());
    }

    return false;
  }

  public boolean play(Chord.Mask chordMask)
  {
    if (!soundEnabled || (chordMask == null)) {
      if (player != null) {
        player.stopAll();
      }
      return false;
    }

    if (player == null) {
      player = new music.midi.Player();
      player.init();
    }

    player.stopAll();

    if (arpeggiating) {
      return player.playArpeggiate(chordMask.getValue(), 200);
    } else {
      return player.playChord(chordMask.getValue(), 500);
    }
  }
}
