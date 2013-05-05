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
                    int xPointer = e.getX();
//                    int yPointer = e.getY();
                    int xChart = cellRect.x;
//                    int yChart = cellRect.y;
                    int widthChart = cellRect.width;
//                    int heightChart = cellRect.height;
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

    /**
     * custom CellRenderer for the JComboBox table elements
     */
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
