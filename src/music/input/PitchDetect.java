package music.input;

import java.util.Comparator;
import java.util.PriorityQueue;
import javax.sound.sampled.*;
import music.core.Interval;
import music.core.Note;

public class PitchDetect
{

  float samplingRate = 44100 / 4;
  int buffWindowSize = 4096;
  
  public synchronized int getSamplingRate()
  {
    return (int) samplingRate;
  }

  public synchronized void setSamplingRate(int rate)
  {
    samplingRate = rate;
  }

  public synchronized void setSamplingSize(int size)
  {
    buffWindowSize = size;
  }

  public synchronized int getSampleSize()
  {
    return buffWindowSize;
  }

  private int readWindowSize()
  {
    return buffWindowSize * 2;
  }

  static class PitchMin
  {

    public int sum;
    public int offset;

    PitchMin(int _sum, int _offset)
    {
      sum = _sum;
      offset = _offset;
    }

    @Override
    public String toString()
    {
      // TODO Auto-generated method stub
      return "{" + offset + " " + sum + "}";
    }
  }

  public static interface PitchUpdater
  {

    public void newNote(Note note, double freq);
  }
  PitchMin pitchMin;
  final GraphPanel graphPanel;
  Thread samplingThread;
  boolean running = false;
  double standardAFreq = samplingRate / (samplingRate / 440);
  boolean debugOut = true;
  double minFreq = 200;
  double maxFreq = 1000;
  int ampNoiseThresh = 5;

  public PitchDetect()
  {
    graphPanel = new GraphPanel();
    pitchMin = new PitchMin(0, 0);
  }

  public synchronized void start(PitchUpdater updater)
  {
    if (running) {
      return;
    }
    running = true;
    samplingThread = new Thread(new AudioSampleTask(updater));
    samplingThread.setDaemon(true);
    samplingThread.start();
  }

  public synchronized void stop()
  {
    running = false;
  }

  public synchronized boolean isRunning()
  {
    return running;
  }

  public GraphPanel getGraphPanel()
  {
    return graphPanel;
  }

  public static void main(String[] args)
  {
    PitchDetect pdetect = new PitchDetect();
    pdetect.graphPanel.showTopLevel();
    pdetect.start(null);
  }

  class AudioSampleTask implements Runnable
  {

    AudioFormat format;
    DataLine.Info info;
    PitchUpdater updater;

    AudioSampleTask(PitchUpdater updater)
    {
      this.updater = updater;

      format = new AudioFormat(samplingRate, 8, 1, true, true);

      info = new DataLine.Info(TargetDataLine.class, format);

      if (!AudioSystem.isLineSupported(info)) {
        throw new RuntimeException("Can't Open Audio System");
      }
    }

    @Override
    public void run()
    {
      TargetDataLine line = null;

      try {
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        byte buffer[] = new byte[readWindowSize()];
        byte buffCopy[] = new byte[readWindowSize()];

        double freq = 0;
        // double freqSum = 0;
        boolean lastNoise = false;

        while (running) {
          //int count = line.read(buffer, 0, readWindowSize());
          System.arraycopy(buffer, buffWindowSize, buffer, 0, buffWindowSize);
          int count = line.read(buffer, buffWindowSize, buffWindowSize);
          //int totalSound = noiseFilter(buffer, buffWindowSize, buffWindowSize, 8);
          assert (count == buffWindowSize);

          int offset = 0;

          //offset = runAutoCorrE(buffer, pitchMin);

          offset = runAutoCorrAMDF(buffer, pitchMin, minFreq, maxFreq);

          synchronized (graphPanel) {
            System.arraycopy(buffer, 0, buffCopy, 0, buffer.length);
            graphPanel.setPlot(buffCopy);
          }

          if ((offset <= 2) || (buffWindowSize - offset) <= 2) {
            if (!lastNoise) {
              if (debugOut) {
                System.out.println("Too Noisy");
              }
            }
            lastNoise = true;
            if (updater != null) {
              updater.newNote(null, 0);
            }
            continue;
          }

          lastNoise = false;

          double newFreq = samplingRate / offset;

//          if (lastOffset > 0) {
//            freq = (freq * .5) + (newFreq * .5);
//          } else {
//            freq = newFreq;
//          }
          freq = newFreq;

          Note newNote = freqToNote(freq, standardAFreq);

          if (debugOut) {
            System.out.println(newNote + " " + freq + " " + pitchMin.sum);
          }

//          computeSubtractFreq(offset);
          if (updater != null) {
            updater.newNote(newNote, freq);
          }

//          for (int i = 0; i < queue.size(); i++) {
//            if (queue == null || queue.size() == 0) {
//              break;
//            }
//            PitchMin min = queue.poll();
//            if (i > 0) {
//              System.out.print(", ");
//            }
//            System.out.print(freqToNote(samplingRate / min.offset, 441) + " " + samplingRate / min.offset);
//          }
//          System.out.println();

          //Thread.sleep(200);
        }

      } catch (Exception ex) {
        ex.printStackTrace();
      } finally {
        if (line != null) {
          line.stop();
          line.flush();
          line.close();
        }
      }
    }
  }

  static int noiseFilter(byte[] window, int offset, int length, int ampClip)
  {
    int sum = 0;

    for (int i = 0; i < length; i++) {
      int amp = Math.abs(window[i + offset]);
      if (amp < ampClip) {
        window[i + offset] = 0;
        amp = 0;
      }
      sum += amp;
    }

    return sum;
  }

  int computeAMDF(byte[] window, int length, int offset)
  {
    int sum = 0;

    for (int i = 0; i < length; i++) {
      int diff = window[i] - window[i + offset];
      sum += Math.abs(diff);
      //sum += (diff * diff);
    }
    //sum /= length;
    return sum;
  }

  static int computeAutoCorrProd(byte[] window, int length, int offset)
  {
    int sum = 0;

    for (int i = 0; i < length; i++) {
      sum += window[i] * window[i + offset];
    }

    return sum;
  }

  public static Note freqToNote(double freq, double middleA)
  {
    double res = Math.log(freq / middleA) / Math.log(2);
    res *= 12;
    res += 57;
    int noteVal = (int) Math.round(res);
    noteVal = noteVal % 12;

    Note note = new Note();
    note = note.add(Interval.halfStep.scale(noteVal));
    return note;
  }

  static class PitchCompare implements Comparator<PitchMin>
  {

    @Override
    public int compare(PitchMin arg0, PitchMin arg1)
    {
      // TODO Auto-generated method stub
      if (arg0.sum < arg1.sum) {
        return -1;
      } else if (arg0.sum == arg1.sum) {
        return -1;
      } else {
        return 1;
      }
    }
  }
  PriorityQueue<PitchMin> queue =
          new PriorityQueue<PitchMin>(1, new PitchCompare());

  int runAutoCorrAMDF(
          byte[] window,
          PitchMin pitchMin,
          double minFreq,
          double maxFreq)
  {
    int minOffset = 0;

    boolean lookForMax = true;

    int minSum = Integer.MAX_VALUE;
    int lastSum = -Integer.MAX_VALUE;

    int start = Math.max(1, (int) (samplingRate / maxFreq));
    int end = Math.min((int) (samplingRate / minFreq), buffWindowSize);

    //queue.clear();
    int totalSum = 0;

    for (int offset = start; offset <= end; offset++) {
      //int span = size - offset;
      int sum = computeAMDF(window, buffWindowSize, offset);
      totalSum += sum;

      if (lookForMax) {
        if (sum < lastSum) {
          lookForMax = false;
        }
      } else if (sum < minSum) {
        minOffset = offset - 1;
        minSum = lastSum;
        queue.add(new PitchMin(minSum, minOffset));
      }

      lastSum = sum;
    }

    int avgAmp = (totalSum / (buffWindowSize * 128));

    if (debugOut) {
      //System.out.println ("Sum: " + avgAmp);
    }

    if (avgAmp < ampNoiseThresh) {
      minOffset = 0;
    }

    pitchMin.offset = minOffset;
    pitchMin.sum = minSum;

    return minOffset;
  }
  int[] newWindow = new int[readWindowSize()];
  byte[] graphArray = new byte[readWindowSize()];

  int runAutoCorrE(
          byte[] window,
          PitchMin pitchMax)
  {
    for (int offset = 0; offset < readWindowSize(); offset++) {
      int span = readWindowSize() - offset;
      int sum = computeAutoCorrProd(window, span, offset);
      if (sum < 0) {
        sum = 0;
      }

      int newVal = (window[offset] - 2 * sum);
      if (newVal < 0) {
        newVal = 0;
      }

      //newVal /= 128;
      newWindow[offset] = newVal;

      graphArray[offset] = (byte) newWindow[offset];
    }

    pitchMax.offset = 0;
    pitchMax.sum = 0;

    for (int offset = 0; offset < readWindowSize(); offset++) {
      if (newWindow[offset] > pitchMax.sum) {
        pitchMax.sum = newWindow[offset];
        pitchMax.offset = offset;
      }
    }

    System.out.println(pitchMax.sum);

    return pitchMax.offset;
  }
//  static void computeSubtractFreq(byte[] window, int period)
//  {
//    int lastSign = 0;
//    int zeroOffset = 0;
//
//    for (int i = 0; i < buffWindowSize; i++)
//    {
//      int sign = (window[i] < 0) ? -1 : 1;
//      if (lastSign != 0 && sign != lastSign) {
//        zeroOffset = i;
//        break;
//      }
//      lastSign = sign;
//    }
//
//    for (int i = 0; i < readWindowSize(); i++)
//    {
//      window[i] -= Math.sin((zeroOffset - i) * (period / Math.PI));
//    }
//  }
}
