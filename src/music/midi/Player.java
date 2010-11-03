/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package music.midi;

import javax.sound.midi.*;

/**
 *
 * @author Ilya
 */
public class Player
{

  Synthesizer synth;
  Instrument accordion;

  public boolean init()
  {
    try {
      synth = MidiSystem.getSynthesizer();
      synth.open();

      Instrument[] ins = synth.getAvailableInstruments();

      for (int i = 0; i < ins.length; i++) {
        if (ins[i].getName().equals("Accordion")) {
          accordion = ins[i];
          break;
        }
      }

      if (accordion != null) {
        synth.getChannels()[chanUsed].programChange(accordion.getPatch().getBank(), accordion.getPatch().getProgram());
      }

      return true;
    } catch (MidiUnavailableException md) {
      System.out.println(md);
      return false;
    }
  }

  int bitToMidi(int bit)
  {
    return bit + 48; //60 == Middle C
  }
  int chanUsed = 0;

  public void playChord(int value)
  {
    MidiChannel[] chans = synth.getChannels();
    int velocity = 50;

    if (value == 0) {
      return;
    }


    for (int i = 0; i < 32; i++) {
      if ((value & (1 << i)) != 0) {
        int midinote = bitToMidi(i);
        chans[chanUsed].noteOn(midinote, velocity);
        //System.out.println("Playing Note: " + midinote);
      }
    }
  }

  public void stopAll()
  {
    MidiChannel[] chans = synth.getChannels();
    chans[chanUsed].allNotesOff();
  }
}
