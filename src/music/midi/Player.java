/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package music.midi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import javax.swing.Timer;
import javax.sound.midi.*;
import music.Note;

/**
 *
 * @author Ilya
 */
public class Player implements ActionListener
{

  Synthesizer synth;
  //Timer audioTimer;
  LinkedList<AudioTask> cancelTasks;
  int velocity = 64;

  public boolean init()
  {
    try {
      cancelTasks = new LinkedList<AudioTask>();
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

      //audioTimer = new Timer(true);

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

  public boolean playChords(int[] chordMasks, int delay)
  {
    int totalDelay = 0;

    for (int i = 0; i < chordMasks.length; i++) {
      AudioTask playTask = new AudioTask(chordMasks[i], delay - 1, totalDelay);
      //audioTimer.schedule(playTask, totalDelay);
      playTask.start();
      totalDelay += delay;
    }
    return true;
  }

  public boolean playArpeggiate(int chordMask, int delay)
  {
    int totalDelay = 0;

    for (int i = 0; i < Note.NUM_HALFSTEPS * 2; i++) {
      if ((chordMask & (1 << i)) != 0) {
        AudioTask playTask = new AudioTask(1 << i, delay - 1, totalDelay);
        //audioTimer.schedule(playTask, totalDelay);
        playTask.start();
        totalDelay += delay;
      }
    }

    return true;
  }

  public boolean playChord(int chordMask, int duration, boolean manualStop)
  {
    try {
      MidiChannel[] chans = synth.getChannels();
      //int velocity = 50;

      if (chordMask == 0) {
        return false;
      }

      for (int i = 0; i < Note.NUM_HALFSTEPS * 2; i++) {
        if ((chordMask & (1 << i)) != 0) {
          int midinote = bitToMidi(i);
          chans[chanUsed + (i / Note.NUM_HALFSTEPS)].noteOn(midinote, velocity);
          //System.out.println("Playing Note: " + midinote);
        }
      }

      if (!manualStop) {
        AudioTask cancelTask = new AudioTask(chordMask, 0, duration);
        //audioTimer.schedule(cancelTask, duration);
        cancelTask.start();
        cancelTasks.add(cancelTask);
      }
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
    cancelAllTasks();
  }

  public void setVelocity(int vel)
  {
    velocity = vel;
  }

  public void setVolume(int volume)
  {
    MidiChannel[] channels = synth.getChannels();

    for (int i = 0; i < channels.length; i++) {
      channels[i].controlChange(7, volume);
    }
  }

  public void cancelAllTasks()
  {
    if (!cancelTasks.isEmpty()) {
      for (AudioTask task : cancelTasks) {
        task.stop();
      }
      cancelTasks.clear();
    }
  }

  private void stopChord(int chordMask)
  {
    MidiChannel[] chans = synth.getChannels();

    for (int i = 0; i < Note.NUM_HALFSTEPS * 2; i++) {
      if ((chordMask & (1 << i)) != 0) {
        int midinote = bitToMidi(i);
        chans[chanUsed + (i / Note.NUM_HALFSTEPS)].noteOff(midinote, 50);
      }
    }
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    AudioTask task = (AudioTask) e.getSource();
    task.run();
  }

  class AudioTask extends Timer
  {

    int chordMask;
    int duration;
    boolean toPlay;

    AudioTask(int cmask, int dur, int delay)
    {
      super(delay, Player.this);
      this.setRepeats(false);
      chordMask = cmask;
      duration = dur;
      toPlay = (dur > 0);
    }

    public void run()
    {
      if (toPlay) {
        playChord(chordMask, duration, false);
      } else {
        stopChord(chordMask);
      }
    }
  }
}
