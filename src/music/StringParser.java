package music;

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
