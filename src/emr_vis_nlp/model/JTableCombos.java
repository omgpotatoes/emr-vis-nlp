package emr_vis_nlp.model;

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * extends JTable in order to support JComboBox elements
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
    
}
