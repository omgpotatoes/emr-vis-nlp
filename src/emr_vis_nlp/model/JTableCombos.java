package emr_vis_nlp.model;

import java.awt.Component;
import java.awt.event.MouseEvent;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * extends JTable in order to support JComboBox elements. Also supports custom tooltips.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class JTableCombos extends JTable {

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
        int realColumnIndex = convertColumnIndexToModel(colIndex);

        if (realColumnIndex == 0) { // name?
            TableModel model = getModel();
            tip = (String)model.getValueAt(rowIndex,0);
        } else { 
            tip = super.getToolTipText(e);
        }
        
        return tip;
        
    }
    
}
