package render;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import music.BassBoard;
import music.BoardSearcher;
import music.ButtonCombo;
import music.Chord;
import music.ChordRegistry;
import music.ParsedChordDef;

class BoardMouseListener extends MouseAdapter
{

  private RenderBassBoard renderBoard;
  private SeqColumnModel columnModel;
  private TabButtonClicker tabClicker;
  private BassBoard.Pos lastClickPos;
  private boolean isClickShiftMode = true;
  private boolean isInstaClick = false;
  private int clickIndex = -1;
  private Vector<BassBoard.Pos> clickButtons;
  private SoundController sound;

  public BoardMouseListener(RenderBassBoard renderB,
          SeqColumnModel model,
          TabButtonClicker clicker,
          SoundController sound)
  {
    renderBoard = renderB;
    columnModel = model;
    clickButtons = new Vector<BassBoard.Pos>();
    tabClicker = clicker;
    this.sound = sound;
  }

  private void updateFromClicked()
  {
    BassBoard.Pos[] allPos = new BassBoard.Pos[clickButtons.size()];
    clickButtons.toArray(allPos);
    ButtonCombo activeCombo = new ButtonCombo(allPos, renderBoard.getBassBoard());

    Vector<ParsedChordDef> matches = ChordRegistry.mainRegistry().
            findChordFromNotes(ButtonCombo.sortedNotes, activeCombo.getChordMask(), tabClicker.optIncludeInversion, true, true);

    if (matches.isEmpty()) {
      return;
    }

    for (ParsedChordDef def : matches) {
      def.setPrefCombo(activeCombo);
    }

    ParsedChordDef activeParsedChord = matches.firstElement();
    columnModel.editSelectedColumn(activeParsedChord);
    tabClicker.setMatchedChords(matches);
  }

  @Override
  public void mouseDragged(MouseEvent e)
  {
    BassBoard.Pos clickPos = renderBoard.hitTest(e);
    if (BassBoard.posEquals(lastClickPos, clickPos)) {
      return;
    }

    sound.play(renderBoard.getBassBoard().getChordAt(clickPos));

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

  private void syncActiveButtons()
  {
    if (columnModel == null) {
      return;
    }
    ButtonCombo selCombo = columnModel.selComboModel.getSelectedButtonCombo();
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
    sound.play(renderBoard.getBassBoard().getChordAt(clickPos));
    
    if (e.isAltDown()) {
      isInstaClick = true;
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
        }
      } else {
        clickButtons.remove(clickIndex);
        clickIndex = -1;
      }
    }
    lastClickPos = clickPos;
    updateFromClicked();
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
    if (isInstaClick) {
      renderBoard.clearClickPos();
      isInstaClick = false;
    }
  }
}
