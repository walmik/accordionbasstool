package music.core;

import java.util.Vector;

public class StringParser
{

  final String fullString;
  private int offset;

  public StringParser(String string)
  {
    fullString = string;
    offset = 0;
  }

  public void skipWhiteSpace()
  {
    while (!isDone()) {
      char nextCh = fullString.charAt(offset);
      if (Character.isWhitespace(nextCh)) {
        offset++;
      } else {
        break;
      }
    }
  }

  public void skipThrough(String str)
  {
    boolean found = false;
    while (!isDone() && !found) {
      char nextCh = fullString.charAt(offset);
      offset++;
      for (int i = 0; i < str.length(); i++) {
        if (nextCh == str.charAt(i)) {
          found = true;
          break;
        }
      }
    }
  }

  public String input()
  {
    skipWhiteSpace();
    return fullString.substring(offset);
  }

  public Note getNote()
  {
    Note note = Note.fromString(input());

    if (note != null) {
      incOffset(Note.getLastParserOffset());
    }

    return note;
  }

  public Note[] parseNoteListArray()
  {
    Vector<Note> notes = new Vector<Note>();
    Note newNote;

    while ((newNote = getNote()) != null) {
      notes.add(newNote);
      skipWhiteSpace();
    }

    if (notes.isEmpty()) {
      notes.add(new Note());
    }

    Note[] noteArray = new Note[notes.size()];
    notes.toArray(noteArray);
    return noteArray;
  }

  public Chord parseNoteList()
  {
    Note[] noteArray = parseNoteListArray();
    return new Chord(noteArray);
  }

  public char nextChar()
  {
    if (isDone()) {
      return 0;
    }
    return fullString.charAt(offset);
  }

  public void incOffset(int inc)
  {
    offset += inc;
  }

  public int getOffset()
  {
    return offset;
  }

  public boolean isDone()
  {
    return offset >= fullString.length();
  }
}
