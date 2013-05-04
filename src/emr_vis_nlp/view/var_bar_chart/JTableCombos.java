package emr_vis_nlp.view.var_bar_chart;

import emr_vis_nlp.view.var_bar_chart.VarBarChartForCell;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.*;

/**
 * Extension to JTable in order to support JComboBox elements and custom
 * tooltips. Also supports interactive VarBarCharts.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class JTableCombos extends JTable {

    public JTableCombos() {
        super();
        // adopted from http://stackoverflow.com/questions/1378096/actionlistener-on-jlabel-or-jtable-cell
        this.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() == 2) {
                JTable target = (JTable) e.getSource();
                int row = target.getSelectedRow();
                int column = target.getSelectedColumn();
                if (column == 2) {
                    // in histogram column
                    // translate properly from table to model!
                    int modelRow = convertRowIndexToModel(row);
                    Rectangle cellRect = target.getCellRect(modelRow, column, false);
//                        Rectangle cellRect = target.getCellRect(row, column, true);
                    VarBarChartForCell chart = (VarBarChartForCell) (getModel().getValueAt(modelRow, column));
                    // figure out which of the cols was clicked
//                        int xPointer = e.getXOnScreen();
//                        int yPointer = e.getYOnScreen();
                    int xPointer = e.getX();
                    int yPointer = e.getY();
//                        int xChart = chart.getX();
//                        int yChart = chart.getY();
//                        int xChart = chart.getBounds().x;
//                        int yChart = chart.getBounds().y;
//                        int xChart = chart.getLocationOnScreen().x;
//                        int yChart = chart.getLocationOnScreen().y;
                    int xChart = cellRect.x;
                    int yChart = cellRect.y;
//                        double xChart = cellRect.getBounds2D().getMinX();
//                        double yChart = cellRect.getBounds2D().getMinY();
//                        int widthChart = chart.getWidth();
//                        int heightChart = chart.getHeight();
                    int widthChart = cellRect.width;
                    int heightChart = cellRect.height;
//                        double widthChart = cellRect.getBounds2D().getMaxX() - xChart;
//                        double heightChart = cellRect.getBounds2D().getMaxY() - yChart;



//                        chart.unhighlightCells();
                    
                    // hardcode for 3 regions (for now)
                    // TODO generalize this code to multiple regions!
//                    double regionBound1 = xChart + widthChart * 1.0 / 3.0;
//                    double regionBound2 = xChart + widthChart * 2.0 / 3.0;
//                    if (xPointer > xChart && xPointer < regionBound1) {
//                        chart.clickOnCell(0);
//                    } else if (xPointer < regionBound2) {
//                        chart.clickOnCell(1);
//                    } else if (xPointer < xChart + widthChart) {
//                        chart.clickOnCell(2);
//                    } else {
//                        // shouldn't happen
//                        System.err.println("JTableCombos: unexpected mouse click event: " + e.toString());
//                    }
                    
                    int numVals = chart.getNumVals();
                    if (numVals <= 0) numVals = 1;
                    double[] regionBounds = new double[numVals-1];
                    boolean cellClicked = false;
                    for (int i=0; i<regionBounds.length; i++) {
                        regionBounds[i] = xChart + widthChart * (i+1.0) / numVals;
                        if (xPointer < regionBounds[i]) {
                            chart.clickOnCell(i);
                            cellClicked = true;
                            break;
                        }
                    }
                    if (!cellClicked) {
                        chart.clickOnCell(numVals-1);
                    }
                    
                    
                    // clear all other boxes
                    // click now applies to filtering rather than highlighting
//                        int numRows = getModel().getRowCount();
//                        //for (int r=0; r<rows; r++) {
//                        for (int r=0; r<numRows; r++) {
//                            if (r != row) {
//                                VarBarChartForCell otherChart = (VarBarChartForCell)(getModel().getValueAt(r, column));
//                                otherChart.unhighlightCells();
//                            }
//                        }

                    ((AbstractTableModel) target.getModel()).fireTableDataChanged();
                }
//                }
            }
        });
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        Object value = super.getValueAt(row, column);
        if (value != null) {
            if (value instanceof JComboBox) {
                return new ComboBoxCellRenderer();
            }
            return getDefaultRenderer(value.getClass());
        }
        return super.getCellRenderer(row, column);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        Object value = super.getValueAt(row, column);
        if (value != null) {
            if (value instanceof JComboBox) {
                return new DefaultCellEditor((JComboBox) value);
            }
            return getDefaultEditor(value.getClass());
        }
        return super.getCellEditor(row, column);
    }

    
    
    static class ComboBoxCellRenderer extends DefaultTableCellRenderer {

        public ComboBoxCellRenderer() {
            super();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            if (value != null) {
                if (value instanceof JComboBox) {
                    String currentValue = ((JComboBox) value).getSelectedItem().toString();
                    JLabel label = new JLabel(currentValue);
                    return label;
                }
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        }
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        // method based on http://docs.oracle.com/javase/tutorial/uiswing/components/table.html
        String tip = null;
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);
        int realRowIndex = convertRowIndexToModel(rowIndex);
        int realColumnIndex = convertColumnIndexToModel(colIndex);

        if (realColumnIndex == 0) { // name?
            TableModel model = getModel();
            tip = (String) model.getValueAt(realRowIndex, 0);
        } else {
            tip = super.getToolTipText(e);
        }

        return tip;

    }
}
