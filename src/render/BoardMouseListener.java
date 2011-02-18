package render;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import music.BassBoard;
import music.BoardSearcher;
import music.ButtonCombo;
import music.ParsedChordDef;

public class BoardMouseListener extends MouseAdapter
{

  private RenderBassBoard renderBoard;
  private SeqColumnModel columnModel;
  private BassBoard.Pos lastClickPos;
  private boolean isClickShiftMode = true;
  private boolean isInstaClick = false;
  private int clickIndex = -1;
  private Vector<BassBoard.Pos> clickButtons;
  private SoundController sound;
  private boolean allowBlanks = false;

  public BoardMouseListener(
          RenderBassBoard renderB,
          SeqColumnModel model,
          SoundController sound)
  {
    renderBoard = renderB;
    columnModel = model;
    clickButtons = new Vector<BassBoard.Pos>();
    this.sound = sound;
    assert (this.sound != null);

    renderBoard.setMainMouseAdapter(this);
  }

  public void setColumnModel(SeqColumnModel model, boolean allow)
  {
    columnModel = model;
    allowBlanks = allow;
  }

  public void setShiftClickMode(boolean shiftClick)
  {
    this.isClickShiftMode = shiftClick;
  }

  private void updateFromClicked()
  {
    if (columnModel == null) {
      return;
    }

    if (clickButtons.size() == 0) {
      if (allowBlanks) {
        columnModel.editSelectedColumn(ParsedChordDef.newEmptyChordDef());
      }
      return;
    }

    BassBoard.Pos[] allPos = new BassBoard.Pos[clickButtons.size()];
    clickButtons.toArray(allPos);
    ButtonCombo activeCombo = new ButtonCombo(allPos, renderBoard.getBassBoard());

    Vector<ParsedChordDef> matchedChords = columnModel.matchingChordStore.getAllMatchingSelChords(activeCombo, true);

    if (matchedChords.isEmpty()) {
      return;
    }

    ParsedChordDef activeParsedChord = matchedChords.firstElement();
    columnModel.editSelectedColumn(activeParsedChord, true);
  }

  private void syncActiveButtons()
  {
    if (columnModel == null) {
      return;
    }
    ButtonCombo selCombo = columnModel.getSelectedButtonCombo();
    if (selCombo == null) {
      return;
    }
    clickButtons.clear();
    for (BassBoard.Pos pos : selCombo.getAllPos()) {
      clickButtons.add(pos);
    }
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
    renderBoard.setClickPos(e, false);
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
    renderBoard.clearClickPos(columnModel == null);
  }

  @Override
  public void mouseMoved(MouseEvent e)
  {
    renderBoard.setClickPos(e, false);
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    BassBoard.Pos clickPos = renderBoard.hitTest(e);

    if (e.isAltDown() || (columnModel == null)) {
      isInstaClick = true;
      sound.play(renderBoard.getBassBoard().getChordAt(clickPos), true);
      renderBoard.setClickPos(clickPos, true);
      return;
    }

    renderBoard.clearClickPos((columnModel == null));

    if (isClickShiftMode) {
      if (!e.isShiftDown()) {
        clickButtons.clear();
      } else {
        syncActiveButtons();
      }
    } else {
      syncActiveButtons();
    }

    clickIndex = -1;
    if (clickPos != null) {
      clickIndex = clickButtons.indexOf(clickPos);
      if (clickIndex < 0) {
        if (clickButtons.size() < BoardSearcher.optMaxComboLength) {
          clickIndex = clickButtons.size();
          clickButtons.addElement(clickPos);
        } else {
          return;
        }
      } else {
        clickButtons.remove(clickIndex);
        clickIndex = -1;
      }
    }
    lastClickPos = clickPos;
    sound.play(renderBoard.getBassBoard().getChordAt(clickPos), false);
    updateFromClicked();
  }

  @Override
  public void mouseDragged(MouseEvent e)
  {
    BassBoard.Pos clickPos = renderBoard.hitTest(e);
    if (BassBoard.posEquals(lastClickPos, clickPos)) {
      return;
    }

    if (this.isInstaClick) {
      sound.play(renderBoard.getBassBoard().getChordAt(clickPos), true);
    } else {
      sound.play(renderBoard.getBassBoard().getChordAt(clickPos), false);
    }

    lastClickPos = clickPos;

    if (isInstaClick) {
      renderBoard.setClickPos(clickPos, true);
    } else if (clickIndex >= 0) {
      if ((clickPos != null) && (clickIndex < clickButtons.size()) && !clickButtons.contains(clickPos)) {
        clickButtons.setElementAt(clickPos, clickIndex);
      }
      updateFromClicked();
    }
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
    if (isInstaClick) {
      renderBoard.setClickPos(e, false);
      isInstaClick = false;
      sound.stop();
    }
  }
}
