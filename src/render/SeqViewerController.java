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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import music.ChordDef;

/**
 *
 * @author Ilya
 */
public class SeqViewerController
{

  JTable seqTable;
  JScrollPane tableScrollPane;
  SeqColumnModel columnModel;

  SeqViewerController(JTable table, JScrollPane scroll)
  {
    seqTable = table;
    tableScrollPane = scroll;

    RenderBassBoard renderBoard = BassToolFrame.getRenderBoard();

    columnModel = new SeqColumnModel(renderBoard, seqTable.getSelectionModel());

    seqTable.setAutoCreateColumnsFromModel(false);
    seqTable.setColumnModel(columnModel);
    seqTable.setModel(columnModel.dataModel);

    seqTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    tableScrollPane.setBorder(BorderFactory.createEmptyBorder());

    seqTable.setDefaultRenderer(String.class, new CellRenderer());

    JTableHeader header = seqTable.getTableHeader();
    header.setResizingAllowed(true);
    header.setDefaultRenderer(new ColumnHeaderRenderer(header));

    //header.setUI(new SliderTableHeaderUI(header.getUI(), columnModel));

    seqTable.setTableHeader(header);

    final int DEFAULT_ROW_HEADER_WIDTH = 96;

    //Create row header table
    JList rowHeader = new JList(columnModel.rowHeaderDataModel);
    rowHeader.setCellRenderer(new RowHeaderRenderer(header));
    rowHeader.setSelectionModel(seqTable.getSelectionModel());
    rowHeader.setFixedCellWidth(DEFAULT_ROW_HEADER_WIDTH);
    rowHeader.setFixedCellHeight(seqTable.getRowHeight());
    rowHeader.setOpaque(false);

    tableScrollPane.setRowHeaderView(rowHeader);

    MouseAdapter headerMouse = new HeaderMouseInputHandler();
    header.addMouseListener(headerMouse);
    header.addMouseMotionListener(headerMouse);
    seqTable.addMouseListener(headerMouse);

    // Row Selection Change
    seqTable.getSelectionModel().addListSelectionListener(
            new javax.swing.event.ListSelectionListener()
            {

              @Override
              public void valueChanged(ListSelectionEvent e)
              {
                int index = seqTable.getSelectedRow();
                if ((index >= 0) && (index < columnModel.allComboSeqs.length)) {
                  columnModel.selComboModel.setButtonComboSeq(columnModel.allComboSeqs[index]);
                  seqTable.scrollRectToVisible(seqTable.getCellRect(index, seqTable.getSelectedColumn(), true));
                } else {
                  columnModel.selComboModel.setButtonComboSeq(null);
                }
              }
            });

    //Column Selection Change
    columnModel.getSelectionModel().addListSelectionListener(
            new javax.swing.event.ListSelectionListener()
            {

              int lastSel = -1;

              @Override
              public void valueChanged(ListSelectionEvent e)
              {
                int newSel = seqTable.getSelectedColumn();
                if (lastSel == newSel) {
                  return;
                }

                lastSel = newSel;
                seqTable.getTableHeader().repaint();
              }
            });
  }

  void registerPanelListener(Component top)
  {
    top.addComponentListener(new ComponentAdapter()
    {

      @Override
      public void componentHidden(ComponentEvent e)
      {
        RenderBassBoard renderBoard = BassToolFrame.getRenderBoard();
        if (renderBoard != null) {
          renderBoard.setSelectedButtonCombo(null);
        }
      }

      @Override
      public void componentShown(ComponentEvent e)
      {
        RenderBassBoard renderBoard = BassToolFrame.getRenderBoard();
        if (renderBoard != null) {
          renderBoard.setSelectedButtonCombo(columnModel.selComboModel);
        }
      }
    });
  }

  class SliderTableHeaderUI extends TableHeaderUI
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
      ;
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
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
      //Component comp = defRenderer.getTableCellRendererComponent(seqTable, value, false, cellHasFocus, -1, index);
      this.setText(value.toString());

      if (isSelected) {
        this.setBorder(lowered);
        this.setFont(bold);
        this.setBackground(selColor);
      } else {
        this.setBorder(raised);
        this.setFont(plain);
        this.setBackground(defColor);
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

      if (RenderBoardUI.defaultUI != null) {
        notSelectedIcon = new ImageIcon(RenderBoardUI.defaultUI.selectedIM.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        selectedIcon = new ImageIcon(RenderBoardUI.defaultUI.pressedIM.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
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
      assert (value instanceof ChordDef);

      ChordDef chordDef = (ChordDef) value;

      assert (table != null);


      isSelected = table.isColumnSelected(column);
      hasFocus = false;

      //assert (comp == this);
      String info = "<html>" + chordDef.abbrevHtml + "</html>";

      String statusInfo = "<html><b>" + chordDef.getName() + "</b><br/>"
              + "(" + chordDef.getChord().toString("-", true) + ")</html>";

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

      return this;
    }
  }

  class CellRenderer extends DefaultTableCellRenderer
  {

    Color defSelColor;
    Color defPlainColor;
    Border highliteBorder;
    Border stdBorder;

    CellRenderer()
    {
      initUI();
    }

    private void initUI()
    {
      highliteBorder = new CompoundBorder(
              new LineBorder(Color.black, 2),
              new EmptyBorder(2, 2, 2, 2));

      stdBorder = new EmptyBorder(4, 4, 4, 4);
      //stdBorder = BorderFactory.createMatteBorder(1, 1, 0, 0, Color.black);

      defSelColor = super.getTableCellRendererComponent(seqTable, "", true, false, 0, 0).getBackground();
      defPlainColor = super.getTableCellRendererComponent(seqTable, "", false, false, 0, 0).getBackground();
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
      Component defRendComp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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
        jcomp.setBackground(defPlainColor);
        jcomp.setBorder(stdBorder);
      }

      return jcomp;
    }
  }
}
