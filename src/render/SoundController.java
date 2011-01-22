/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
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
  int playDuration = 500;
  int arpegStepDuration = 200;

  public SoundController(boolean initNow)
  {
    if (initNow) {
      player = new music.midi.Player();
      player.init();
    }
  }

  public void setEnabled(boolean enabled)
  {
    soundEnabled = enabled;
    if (player != null) {
      player.stopAll();
    }
  }

  class SoundAction extends AbstractAction
  {
    
    String name;

    public SoundAction(String name)
    {
      this.name = name;
      this.putValue(NAME, name);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
    }
  }

  public void initSoundMenu(JMenu menu)
  {
    MenuSlider slider = new MenuSlider();
    slider.setMaximum(10);

    //JMenu insert = new JMenu("Sound Volume");
    //insert.add(slider);

    menu.add(slider);
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

  public void stop()
  {
    if (player != null) {
      player.stopAll();
    }
  }

  public boolean play(ButtonCombo combo, boolean manualStop)
  {
    if (manualStop) {
      stop();
    }

    if (combo != null) {
      return play(combo.getChordMask(), manualStop);
    }

    return false;
  }

  public boolean play(Chord chord, boolean manualStop)
  {
    if (chord != null) {
      return play(chord.getChordMask(), manualStop);
    } else if (manualStop) {
      stop();
    }

    return false;
  }

  public boolean play(Chord.Mask chordMask, boolean manualStop)
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
      return player.playArpeggiate(chordMask.getValue(), arpegStepDuration);
    } else {
      return player.playChord(chordMask.getValue(), playDuration, manualStop);
    }
  }
}
