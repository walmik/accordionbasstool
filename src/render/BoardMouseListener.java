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

  public BoardMouseListener(RenderBassBoard renderB,
          SeqColumnModel model,
          SoundController sound)
  {
    renderBoard = renderB;
    columnModel = model;
    clickButtons = new Vector<BassBoard.Pos>();
    this.sound = sound;

    renderBoard.addMouseListener(this);
    renderBoard.addMouseMotionListener(this);
  }

  private void updateFromClicked()
  {
    if (columnModel == null) {
      return;
    }

    if (clickButtons.size() == 0) {
      columnModel.editSelectedColumn(ParsedChordDef.newEmptyChordDef());
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
  public void mousePressed(MouseEvent e)
  {
    BassBoard.Pos clickPos = renderBoard.hitTest(e);

    if (e.isAltDown() || (columnModel == null)) {
      isInstaClick = true;
      sound.play(renderBoard.getBassBoard().getChordAt(clickPos), true);
      renderBoard.setClickPos(clickPos);
      return;
    }

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
      renderBoard.setClickPos(clickPos);
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
      renderBoard.clearClickPos();
      isInstaClick = false;
      sound.stop();
    }
  }
}
