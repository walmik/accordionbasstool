/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package render;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
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

    //Create row header table
    JList rowHeader = new JList(columnModel.rowHeaderDataModel);
    rowHeader.setCellRenderer(new RowHeaderRenderer(header));
    rowHeader.setSelectionModel(seqTable.getSelectionModel());
    rowHeader.setFixedCellWidth(96);
    rowHeader.setFixedCellHeight(seqTable.getRowHeight());
    rowHeader.setOpaque(false);

    tableScrollPane.setRowHeaderView(rowHeader);

    MouseAdapter headerMouse = new HeaderMouseInputHandler();
    header.addMouseListener(headerMouse);
    header.addMouseMotionListener(headerMouse);
    seqTable.addMouseListener(headerMouse);

    header.setDefaultRenderer(new ColumnHeaderRenderer(header));

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

    //columnModel.addColumn(new ChordDef(), 0);
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

  class RowHeaderRenderer extends DefaultListCellRenderer
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
      lowered = noFocusBorder;
      raised = noFocusBorder;

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
    TableCellRenderer defaultRenderer;

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

      defaultRenderer = header.getDefaultRenderer();
      defColor = header.getBackground();
      selColor = defColor.darker();
      lowered = BorderFactory.createLoweredBevelBorder();
      raised = BorderFactory.createRaisedBevelBorder();

      Font font = header.getFont().deriveFont(18.f);
      plain = font.deriveFont(Font.PLAIN);
      bold = font.deriveFont(Font.BOLD);

      this.setHorizontalAlignment(CENTER);
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
      if (!(value instanceof ChordDef))
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

      ChordDef chordDef = (ChordDef) value;

      isSelected = table.isColumnSelected(column);
      hasFocus = false;

      //assert (comp == this);
      String info = "<html>" + chordDef.abbrevHtml + "</html>";

      String statusInfo = "<html><b>" + chordDef.getName() + "</b><br/>"
              + "(" + chordDef.getChord().toString("-", true) + ")</html>";

      JComponent comp = (JComponent)defaultRenderer.getTableCellRendererComponent(table, info, isSelected, hasFocus, row, column);
      //this.setText(info);

      comp.setToolTipText(statusInfo);

      if (table.isColumnSelected(column)) {
        comp.setFont(bold);
 //       this.setBackground(selColor);
 //       this.setBorder(lowered);
      } else {
        comp.setFont(plain);
 //       this.setBackground(defColor);
 //       this.setBorder(raised);
      }

      return comp;
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
      seqTable.repaint();
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
