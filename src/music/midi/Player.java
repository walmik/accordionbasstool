/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package music.midi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
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

  private Synthesizer synth;
  LinkedList<AudioTask> cancelTasks;
  int velocity = 64;
  Instrument bassInstrument;
  Instrument chordInstrument;
  Soundbank customBank = null;

  public boolean init()
  {
    try {
      cancelTasks = new LinkedList<AudioTask>();
      synth = MidiSystem.getSynthesizer();
      synth.open();

      Instrument instrs[] = null;
      Soundbank bank = synth.getDefaultSoundbank();

      if (bank != null) {
        instrs = bank.getInstruments();
      }

      if ((instrs == null) || (instrs.length == 0)) {
        //System.out.println("Bad Bank: " + bank + ", loading our own");
        try {
          URL url = getClass().getClassLoader().getResource("soundbank.gm");
          bank = customBank = MidiSystem.getSoundbank(url.openStream());
          synth.loadAllInstruments(customBank);
          System.out.println("Soundbank From: " + url + 
                  ", " + customBank.getInstruments().length + " instruments");

        } catch (Exception e) {
          System.out.println(e);
        }
      } else {
        System.out.println("Using Default MIDI Soundbank with " + instrs.length + " instruments");
      }

      this.setInstrument(false, findInstrument("Accordion"));
      this.setInstrument(true, findInstrument("Tango Accordion"));

      //audioTimer = new Timer(true);

      return true;
    } catch (MidiUnavailableException md) {
      System.out.println(md);
      return false;
    }
  }

//  public Synthesizer getSynth()
//  {
//    return synth;
//  }

  public Instrument[] getInstruments()
  {
    if (customBank != null) {
      return customBank.getInstruments();
    } else {
      return synth.getAvailableInstruments();
    }
  }

  public Instrument getInstrument(boolean chord)
  {
    return (chord ? chordInstrument : bassInstrument);
  }

  public void setInstrument(boolean chord, Instrument instru)
  {
    if (instru == null) {
      return;
    }

    int chan;

    if (chord) {
      if (instru == chordInstrument) {
        return;
      }
      chordInstrument = instru;
      chan = chanUsed + 1;
    } else {
      if (instru == bassInstrument) {
        return;
      }
      bassInstrument = instru;
      chan = chanUsed;
    }

    synth.getChannels()[chan].programChange(instru.getPatch().getBank(), instru.getPatch().getProgram());
  }

  public Instrument findInstrument(String name)
  {
    Instrument[] ins = getInstruments();

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
