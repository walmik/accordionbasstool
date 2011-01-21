/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package render;

import java.awt.Color;
import java.awt.Component;
import java.awt.Component.BaselineResizeBehavior;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.accessibility.Accessible;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.plaf.TableHeaderUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import music.ButtonCombo;
import music.ButtonComboSequence;
import music.CollSequence;
import music.FingerCombo;
import music.FingerComboSequence;
import music.Note;
import music.ParsedChordDef;

/**
 *
 * @author Ilya
 */
public class SeqViewerController
{

  JTable seqTable;
  JScrollPane tableScrollPane;
  SeqColumnModel columnModel;

  SeqViewerController(JTable table, SeqColumnModel model, JScrollPane scroll, RenderBassBoard renderBoard)
  {
    seqTable = table;
    tableScrollPane = scroll;
    columnModel = model;

    seqTable.setAutoCreateColumnsFromModel(false);
    seqTable.setColumnModel(columnModel);
    seqTable.setModel(columnModel.getDataModel());

    seqTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    tableScrollPane.setBorder(BorderFactory.createEmptyBorder());

    seqTable.setDefaultRenderer(CollSequence.class, new CellRenderer(seqTable));

    JTableHeader header = seqTable.getTableHeader();
    header.setResizingAllowed(true);
    header.setDefaultRenderer(new ColumnHeaderRenderer(header));

    //header.setUI(new SliderTableHeaderUI(header.getUI(), columnModel));

    seqTable.setTableHeader(header);

    final int DEFAULT_ROW_HEADER_WIDTH = 96;

    //Create row header table
    JList rowHeader = new JList(columnModel.getRowHeaderDataModel());
    rowHeader.setCellRenderer(new RowHeaderRenderer(header));
    rowHeader.setSelectionModel(seqTable.getSelectionModel());
    //rowHeader.setFixedCellWidth(DEFAULT_ROW_HEADER_WIDTH);
    rowHeader.setFixedCellHeight(seqTable.getRowHeight());
    rowHeader.setOpaque(false);
    //rowHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
    rowHeader.setToolTipText("Button Combinations, Best to Worse");

    tableScrollPane.setRowHeaderView(rowHeader);

    MouseAdapter headerMouse = new HeaderMouseInputHandler();
    header.addMouseListener(headerMouse);
    header.addMouseMotionListener(headerMouse);
    seqTable.addMouseListener(headerMouse);

    // Row Selection
    seqTable.getSelectionModel().addListSelectionListener(new ListSelChangeListener()
    {

      @Override
      protected void selectionChanged(int index)
      {
        //if (columnModel.setSelectedSeq(index)) {
          seqTable.scrollRectToVisible(seqTable.getCellRect(index, seqTable.getSelectedColumn(), true));
        //}
      }
    });

    // Column Selection
    columnModel.selComboModel.addListSelectionListener(new ListSelChangeListener()
    {

      @Override
      protected void selectionChanged(int index)
      {
        seqTable.scrollRectToVisible(seqTable.getCellRect(seqTable.getSelectedRow(), index, true));
        seqTable.getTableHeader().repaint();
      }
      
    });



    // Row Selection Change
//    seqTable.getSelectionModel().addListSelectionListener(
//            new javax.swing.event.ListSelectionListener()
//            {
//
//              int lastIndex = -1;
//
//              @Override
//              public void valueChanged(ListSelectionEvent e)
//              {
//                int index = seqTable.getSelectedRow();
//                if (index == lastIndex) {
//                  return;
//                }
//                lastIndex = index;
//
//                if (columnModel.setSelectedSeq(index)) {
//                  seqTable.scrollRectToVisible(seqTable.getCellRect(index, seqTable.getSelectedColumn(), true));
//                }
//              }
//            });
//
//    //Column Selection Change
//    columnModel.getSelectionModel().addListSelectionListener(
//            new javax.swing.event.ListSelectionListener()
//            {
//
//              int lastSel = -1;
//
//              @Override
//              public void valueChanged(ListSelectionEvent e)
//              {
//                int newSel = seqTable.getSelectedColumn();
//                if (lastSel == newSel) {
//                  return;
//                }
//
//                lastSel = newSel;
//                seqTable.getTableHeader().repaint();
//              }
//            });
  }

  static class SliderTableHeaderUI extends TableHeaderUI
          implements TableColumnModelListener, ChangeListener
  {

    @Override
    public boolean contains(JComponent c, int x, int y)
    {
      return existing.contains(c, x, y);
    }

    @Override
    public Accessible getAccessibleChild(JComponent c, int i)
    {
      return existing.getAccessibleChild(c, i);
    }

    @Override
    public int getAccessibleChildrenCount(JComponent c)
    {
      return existing.getAccessibleChildrenCount(c);
    }

    @Override
    public int getBaseline(JComponent c, int width, int height)
    {
      return existing.getBaseline(c, width, height);
    }

    @Override
    public BaselineResizeBehavior getBaselineResizeBehavior(JComponent c)
    {
      return existing.getBaselineResizeBehavior(c);
    }

    @Override
    public Dimension getMaximumSize(JComponent c)
    {
      Dimension dim = existing.getMaximumSize(c);
      if (columnModel.getColumnCount() > 1) {
        dim.height += sliderHeight;

      }
      return dim;
    }

    @Override
    public Dimension getMinimumSize(JComponent c)
    {
      Dimension dim = existing.getMinimumSize(c);
      if (columnModel.getColumnCount() > 1) {
        dim.height += sliderHeight;

      }
      return dim;
    }

    @Override
    public void paint(Graphics g, JComponent c)
    {
      existing.paint(g, c);
    }

    @Override
    public void uninstallUI(JComponent c)
    {
      existing.uninstallUI(c);
    }

    @Override
    public void update(Graphics g, JComponent c)
    {
      existing.update(g, c);
    }
    int sliderHeight = 30;
    TableHeaderUI existing;
    JSlider slider;
    boolean updateLock = false;
    SeqColumnModel columnModel;
    JTable table;

    SliderTableHeaderUI(TableHeaderUI existing, SeqColumnModel model,
            JTable newTable)
    {
      this.existing = existing;
      this.columnModel = model;
      this.table = newTable;

      slider = new JSlider();
      slider.setOpaque(false);
      slider.setPaintTicks(true);
      slider.setPaintTrack(true);
      slider.setFocusable(false);
      slider.setSnapToTicks(true);

      slider.addMouseListener(new MouseAdapter()
      {

        @Override
        public void mousePressed(MouseEvent e)
        {
          table.requestFocusInWindow();
        }
      });

      slider.addChangeListener(this);
      columnModel.addColumnModelListener(this);
    }

    @Override
    public Dimension getPreferredSize(JComponent c)
    {
      Dimension dim = existing.getPreferredSize(c);

      if (columnModel.getColumnCount() > 1) {
        dim.height += sliderHeight;
      }

      return dim;
    }

    @Override
    public void installUI(JComponent c)
    {
      existing.installUI(c);
      c.setLayout(null);
      c.add(slider);
    }

    void resizeSlider()
    {
      int numCols = columnModel.getColumnCount();

      if (numCols < 2) {
        slider.setVisible(false);
        return;
      }

      int x1 = columnModel.getColumn(0).getWidth() / 2;
      int x2 = columnModel.getTotalColumnWidth() - columnModel.getColumn(numCols - 1).getWidth() / 2;
      x1 -= 8;
      x2 += 8;

      slider.setBounds(x1, 0, x2 - x1, sliderHeight);

      slider.setMinimum(0);
      slider.setMaximum(numCols - 1);

      updateLock = true;
      slider.setValue(columnModel.getSelectedColumn());
      updateLock = false;

      slider.setVisible(true);
      slider.repaint();
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
      if (updateLock) {
        return;
      }

      int index = slider.getValue();
      updateLock = true;
      columnModel.getSelectionModel().setSelectionInterval(index, index);
      updateLock = false;
    }

    @Override
    public void columnAdded(TableColumnModelEvent e)
    {
      resizeSlider();
    }

    @Override
    public void columnMarginChanged(ChangeEvent e)
    {

      resizeSlider();
    }

    @Override
    public void columnMoved(TableColumnModelEvent e)
    {
      resizeSlider();
    }

    @Override
    public void columnRemoved(TableColumnModelEvent e)
    {
      resizeSlider();
    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e)
    {
      if (updateLock) {
        return;
      }

      updateLock = true;
      int index = columnModel.getSelectedColumn();
      if (index >= 0) {
        slider.setValue(index);
      }
      updateLock = false;
    }
  }

  class HeaderMouseInputHandler extends MouseAdapter
  {

    private void updateColumn(MouseEvent e)
    {
      int column = seqTable.columnAtPoint(e.getPoint());

      if (column < 0) {
        return;
      }

      seqTable.setColumnSelectionInterval(column, column);
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
      updateColumn(e);
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
//      updateColumn(e);
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
//      if (e.getClickCount() > 1) {
//        int column = seqTable.columnAtPoint(e.getPoint());
//
//        columnModel.editColumn(column);
//      }
    }
  }

  static class RowHeaderRenderer extends DefaultListCellRenderer
  {

    Font plain, bold;
    Color defColor, selColor;
    Border lowered, raised;
    JTableHeader header;
    TableCellRenderer defRenderer;

    RowHeaderRenderer(JTableHeader head)
    {
      header = head;
      initUI();
    }

    private void initUI()
    {
      if (header == null) {
        return;
      }

      defRenderer = header.getDefaultRenderer();
      defColor = header.getBackground();
      selColor = defColor.darker();
      //selColor = SystemColor.textHighlight;
      //lowered = BorderFactory.createLoweredBevelBorder();
      //raised = BorderFactory.createRaisedBevelBorder();
      raised = noFocusBorder;
      lowered = noFocusBorder;

      Font font = header.getFont().deriveFont(14.f);
      plain = font.deriveFont(Font.PLAIN);
      bold = font.deriveFont(Font.BOLD);

      RenderBoardUI.defaultUI.renderOptimalityImage();
      RenderBoardUI.defaultUI.buildOptimalityIcons(32);

      this.setIconTextGap(8);
      //this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 24));
      this.setHorizontalAlignment(CENTER);
      this.setHorizontalTextPosition(LEFT);
      this.setVerticalTextPosition(CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
      ButtonComboSequence seq = null;
      int heur = 0;

      if (value instanceof FingerComboSequence) {
        FingerComboSequence fingerSeq = (FingerComboSequence) value;
        seq = fingerSeq.getButtonComboSeq();
        heur = fingerSeq.getHeur();
      } else if (value != null) {
        seq = (ButtonComboSequence) value;
        heur = seq.getHeur();
      }

      if (seq != null) {
        float ratio = ((float) heur / seq.getNumCombos()) / 100.f;
        if (ratio > 1.f) {
          ratio = 1.f;
        }

        int iconIndex = (int) (ratio * (RenderBoardUI.defaultUI.optimalityIcons.length - 1));
        this.setIcon(RenderBoardUI.defaultUI.optimalityIcons[iconIndex]);

        seq.iconIndex = iconIndex;

        String str = "#" + (index + 1);
        //str += " (" + heur + ")";
        this.setText(str);

        if (seq.getExtraneous()) {
          this.setForeground(Color.red);
        } else {
          this.setForeground(Color.black);
        }
        
      } else {
        this.setIcon(RenderBoardUI.defaultUI.optimalityIcons[RenderBoardUI.defaultUI.optimalityIcons.length - 1]);
        this.setText("None");
      }

      if (isSelected) {
        this.setBorder(lowered);
        this.setFont(bold);
        this.setBackground(selColor);
      } else {
        this.setBorder(raised);
        this.setFont(plain);
        this.setBackground(defColor);
      }

      int newPrefWidth = this.getPreferredSize().width + 24;
      if (newPrefWidth >= list.getFixedCellWidth()) {
        list.setFixedCellWidth(newPrefWidth);
      }

      return this;
    }
  }

  class ColumnHeaderRenderer extends DefaultTableCellRenderer
  {

    Font plain, bold;
    Color defColor, selColor;
    Border lowered, raised;
    JTableHeader header;
    ImageIcon notSelectedIcon, selectedIcon;

    ColumnHeaderRenderer(JTableHeader head)
    {
      header = head;
      initUI();
    }

    private void initUI()
    {
      if (header == null) {
        return;
      }

      header.setUI(new SliderTableHeaderUI(header.getUI(), columnModel, header.getTable()));

      defColor = header.getBackground();
      selColor = defColor.darker();
      lowered = BorderFactory.createLoweredBevelBorder();
      raised = BorderFactory.createRaisedBevelBorder();

      this.setOpaque(false);

      if (RenderBoardUI.defaultUI != null) {
        notSelectedIcon = new ImageIcon(RenderBoardUI.BoardButtonImage.SELECTED.image.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        selectedIcon = new ImageIcon(RenderBoardUI.BoardButtonImage.PRESSED_ANY.image.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
      }

      Font font = getFont().deriveFont(18.f);
      plain = font.deriveFont(Font.PLAIN);
      bold = font.deriveFont(Font.BOLD);

      this.setHorizontalAlignment(CENTER);
      this.setVerticalAlignment(BOTTOM);

      this.setHorizontalTextPosition(CENTER);
      this.setIconTextGap(0);
      this.setVerticalTextPosition(BOTTOM);

      this.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
    }

    @Override
    public void updateUI()
    {
      super.updateUI();
      initUI();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
      assert (value instanceof ParsedChordDef);

      ParsedChordDef chordDef = (ParsedChordDef) value;

      assert (table != null);

      isSelected = table.isColumnSelected(column);
      hasFocus = false;

      //assert (comp == this);
      String info = "<html>" + chordDef.nameHtml + "</html>";

      String statusInfo = "<html><b>" + chordDef.detail + "</b><br/>"
              + "(" + chordDef.chord.toHtmlString() + ")</html>";

      //JComponent comp = (JComponent) defaultRenderer.getTableCellRendererComponent(table, info, isSelected, hasFocus, row, column);
      this.setText(info);

      this.setToolTipText(statusInfo);

      if (table.isColumnSelected(column)) {
        this.setIcon(selectedIcon);
        this.setFont(bold);
        //       this.setBackground(selColor);
        //       this.setBorder(lowered);
      } else {
        this.setIcon(notSelectedIcon);
        this.setFont(plain);
        //       this.setBackground(defColor);
        //       this.setBorder(raised);
      }

      TableColumn theColumn = table.getColumnModel().getColumn(column);
      int newPref = Math.max(this.getPreferredSize().width + 24, SeqColumnModel.DEFAULT_COL_WIDTH);

      if (newPref >= theColumn.getPreferredWidth()) {
        theColumn.setPreferredWidth(newPref);
      }

      return this;
    }
  }

  static class CellRenderer extends DefaultTableCellRenderer
  {

    Color defSelColor;
    Color defPlainColor1, defPlainColor2;
    Border highliteBorder;
    Border stdBorder;
    Font font;
    JTable table;

    CellRenderer(JTable theTable)
    {
      table = theTable;
      initUI();
    }

    private void initUI()
    {
      if (table == null) {
        return;
      }
      
      highliteBorder = new CompoundBorder(
              new LineBorder(Color.black, 2),
              new EmptyBorder(2, 2, 2, 2));

      stdBorder = new EmptyBorder(4, 4, 4, 4);
      //stdBorder = BorderFactory.createMatteBorder(1, 1, 0, 0, Color.black);

      defSelColor = super.getTableCellRendererComponent(table, "", true, false, 0, 0).getBackground();

      defPlainColor1 = Color.white;//super.getTableCellRendererComponent(seqTable, "", false, false, 0, 0).getBackground();
      defPlainColor2 = UIManager.getColor("Table.alternateRowColor");

      this.setVerticalAlignment(CENTER);
      this.setHorizontalAlignment(CENTER);
      font = getFont().deriveFont(14.f);
    }

    @Override
    public void updateUI()
    {
      super.updateUI();
      initUI();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object objValue, boolean isSelected, boolean hasFocus, int row, int column)
    {
      FingerCombo fingerCombo = null;
      ButtonCombo buttonCombo = null;

      if (objValue instanceof FingerCombo) {
        fingerCombo = (FingerCombo) objValue;
        buttonCombo = ((fingerCombo != null) ? fingerCombo.getButtonCombo() : null);
      } else {
        buttonCombo = (ButtonCombo) objValue;
      }

      boolean html = true;
      String text = (html ? "<html>" : "");
      String space = (html ? "&nbsp;" : " ");

      if (fingerCombo != null) {
        text += fingerCombo.toString(html);
        if (html) {
          text += space + space + space + "on" + space + space + space;
        }
      }

      if (buttonCombo != null) {
        text += "<span>" + buttonCombo.toButtonListingString(html) + "</span>";
      }

      if ((buttonCombo != null) && (buttonCombo.getLength() > 0)) {
        if (!buttonCombo.isSingleBass()) {
          Note lowest = buttonCombo.getLowestNote();
          String lowestInfo = (lowest.isBassNote() ? "Bass" : "Mid") + space + lowest.toString(html);
          //return "<html>" + info + " Low: " + lowest + "</html>";
          if (html) {
            text += " (<i>" + lowestInfo + "</i>)";
          } else {
            text += " (" + lowestInfo + ")";
          }
          //text = combo.toSortedNoteString(false);
          this.setToolTipText(buttonCombo.toSortedNoteString(false));
        }
      } else {
        text += "Chord Not Possible";
      }
      if (html) {
        text += "</html>";
      }

      Component defRendComp = super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
      JComponent jcomp = (JComponent) defRendComp;
      assert (jcomp == this);

      if (isSelected) {
        // See if this is the anchor sell even if it doesn't have focus
        if (!hasFocus) {
          int r = table.getSelectionModel().getAnchorSelectionIndex();
          int c = table.getColumnModel().getSelectionModel().getAnchorSelectionIndex();
          hasFocus = (r == row) && (c == column);
        }

        if (hasFocus) {
          setBorder(highliteBorder);
          jcomp.setBackground(defSelColor.darker());
        } else {
          jcomp.setBackground(defSelColor);
          jcomp.setBorder(stdBorder);
        }
      } else {
        jcomp.setBackground((row % 2) == 0 ? defPlainColor1 : defPlainColor2);
        jcomp.setBorder(stdBorder);
      }


//      if ((buttonCombo != null) && (buttonCombo.isPreferred())) {
//        jcomp.setForeground(getForeground().darker());
//      }
      jcomp.setFont(font);

      return jcomp;
    }
  }
}
