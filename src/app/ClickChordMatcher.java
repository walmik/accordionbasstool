/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ClickChordMatcher.java
 *
 * Created on Jan 12, 2011, 6:25:12 PM
 */
package app;

import java.awt.Graphics;
import java.util.Vector;
import javax.swing.DefaultListSelectionModel;
import music.BoardRegistry;
import music.BoardRegistry.BoardDef;
import music.ParsedChordDef;
import render.BoardMouseListener;
import render.RenderBassBoard;
import render.RenderBoardHeader;
import render.SeqColumnModel;
import render.SeqTableEventAdapter;
import render.SoundController;

/**
 *
 * @author Ilya
 */
public class ClickChordMatcher extends javax.swing.JApplet
{

  BoardMouseListener mouseListener;
  SeqColumnModel columnModel;

  /** Initializes the applet ClickChordMatcher */
  public void init()
  {
    util.Main.setNimbus();

    try {
      java.awt.EventQueue.invokeAndWait(new Runnable()
      {

        public void run()
        {
          initComponents();
          initClientApplet();
        }
      });
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  protected void initClientApplet()
  {
    AppletController controller = new AppletController(this);

    RenderBassBoard renderBoard = RenderBassBoard.getStaticRenderBoard();

    columnModel = new SeqColumnModel(renderBoard, new DefaultListSelectionModel());
    columnModel.addColumn(ParsedChordDef.newEmptyChordDef(), 0);

    SoundController sound = new SoundController(false);
    //sound.setEnabled(checkSoundEnabled.isSelected());

    Vector<BoardDef> allowedBoards = BoardRegistry.mainRegistry().getStandardBoards();

    mouseListener = new BoardMouseListener(renderBoard, columnModel, sound);

    RenderBoardHeader header = this.renderBoardControl.getHeader();
    header.initBoardHeader(renderBoard, renderBoardControl, columnModel, allowedBoards);
    header.selectFirstBoardByBassCount(48);

    transposePanel.setSeqColModel(columnModel);
    clickedLabel.setText(columnModel.getSelectedComboStateString());

    columnModel.getRowSelModel().addListSelectionListener(new SeqTableEventAdapter()
    {

      @Override
      public void selectionChanged(int index)
      {
        matchesListBox.setListData(columnModel.matchingChordStore.getKnownMatchingSelChords());
        clickedLabel.setText(columnModel.getSelectedComboStateString());
      }
    });

    //header.getExtPanel().add(checkSoundEnabled);
    //header.getExtPanel().add(checkVertical);
  }

  /** This method is called from within the init() method to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jPanel1 = new javax.swing.JPanel();
    transposePanel = new render.TransposePanel();
    clickedLabel = new javax.swing.JLabel();
    jScrollPane1 = new javax.swing.JScrollPane();
    matchesListBox = new javax.swing.JList();
    renderBoardControl = new render.RenderBoardControl();

    setName("Accordion Board Clicker"); // NOI18N

    clickedLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
    clickedLabel.setText("<None>");

    jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Possible Chords:"));

    matchesListBox.setFont(new java.awt.Font("Monospaced", 1, 17)); // NOI18N
    jScrollPane1.setViewportView(matchesListBox);

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(clickedLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(transposePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(18, Short.MAX_VALUE))
      .addComponent(renderBoardControl, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addComponent(clickedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(transposePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(11, 11, 11)
        .addComponent(renderBoardControl, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
    );

    getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel clickedLabel;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JList matchesListBox;
  private render.RenderBoardControl renderBoardControl;
  private render.TransposePanel transposePanel;
  // End of variables declaration//GEN-END:variables
}
