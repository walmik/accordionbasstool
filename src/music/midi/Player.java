/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package music.midi;

import java.util.Timer;
import java.util.TimerTask;
import javax.sound.midi.*;
import music.Note;

/**
 *
 * @author Ilya
 */
public class Player
{

  Synthesizer synth;
  Timer stopTimer;
  StopPlayingTask cancelTask;

  public boolean init()
  {
    try {
      synth = MidiSystem.getSynthesizer();
      synth.open();

      Instrument instru = findInstrument("Accordion");

      if (instru != null) {
        synth.getChannels()[chanUsed].programChange(instru.getPatch().getBank(), instru.getPatch().getProgram());
      }

      instru = findInstrument("Tango Accordion");

      if (instru != null) {
        synth.getChannels()[chanUsed + 1].programChange(instru.getPatch().getBank(), instru.getPatch().getProgram());
      }

      stopTimer = new Timer(true);

      return true;
    } catch (MidiUnavailableException md) {
      System.out.println(md);
      return false;
    }
  }

  private Instrument findInstrument(String name)
  {
    Instrument[] ins = synth.getAvailableInstruments();

    for (int i = 0; i < ins.length; i++) {
      //System.out.println("" + i + " " + ins[i].getName());
      if (ins[i].getName().equals(name)) {
        return ins[i];
      }
    }

    return null;
  }

  int bitToMidi(int bit)
  {
    return bit + 48;// + 24 * (bit / Note.NUM_HALFSTEPS);
  }
  int chanUsed = 0;

  public boolean playChord(int value)
  {
    try {
      MidiChannel[] chans = synth.getChannels();
      int velocity = 50;

      if (value == 0) {
        return false;
      }


      for (int i = 0; i < Note.NUM_HALFSTEPS * 2; i++) {
        if ((value & (1 << i)) != 0) {
          int midinote = bitToMidi(i);
          chans[chanUsed + (i / Note.NUM_HALFSTEPS)].noteOn(midinote, velocity);
          //System.out.println("Playing Note: " + midinote);
        }
      }

      if (cancelTask != null) {
        cancelTask.cancel();
      }
      cancelTask = new StopPlayingTask(value);

      stopTimer.schedule(cancelTask, 500);
      return true;

    } catch (Exception e) {
      System.out.println("MIDI Playback Exc: " + e);
      return false;
    }
  }

  public void stopAll()
  {
    MidiChannel[] chans = synth.getChannels();
    chans[chanUsed].allNotesOff();
    chans[chanUsed + 1].allNotesOff();
  }

  private void stopChord(int value)
  {
    MidiChannel[] chans = synth.getChannels();
    
    for (int i = 0; i < Note.NUM_HALFSTEPS * 2; i++) {
      if ((value & (1 << i)) != 0) {
        int midinote = bitToMidi(i);
        chans[chanUsed + (i / Note.NUM_HALFSTEPS)].noteOff(midinote, 50);
      }
    }
  }

  class StopPlayingTask extends TimerTask
  {
    int chordMask;

    StopPlayingTask(int cmask)
    {
      chordMask = cmask;
    }

    @Override
    public void run()
    {
      stopChord(chordMask);
    }
  }
}
