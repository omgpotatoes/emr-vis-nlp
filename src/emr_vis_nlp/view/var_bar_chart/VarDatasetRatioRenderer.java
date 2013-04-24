
package emr_vis_nlp.view.var_bar_chart;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * 
 * This class serves as the renderer to generate cells in 
 * DatasetVarsTableModel for indicating the fraction of total and selected
 * datapoints with a particular var value.
 * 
 * Loosely based on examples: 
 * http://docs.oracle.com/javase/tutorial/uiswing/examples/components/TableDialogEditDemoProject/src/components/ColorRenderer.java
 * http://stackoverflow.com/questions/8259103/jpanel-as-tablecelleditor-disappearing
 * 
 * @author alexander.p.conrad@gmail.com
 */
public class VarDatasetRatioRenderer implements TableCellRenderer {
    
//    public VarDatasetRatioRenderer() {
//        
//        setOpaque(true);  // do this for background to show up?
//        
//    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // get name of attr from row; map back through table
        //  only render if attr is "proper"
        if (value instanceof JPanel) {
//            if (isSelected || hasFocus) {
//                ((Component) value).setForeground(UIManager.getColor("List.selectionForeground"));
//                ((Component) value).setBackground(UIManager.getColor("List.selectionBackground"));
//            } else {
//                ((Component) value).setForeground(UIManager.getColor("Panel.foreground"));
//                ((Component) value).setBackground(UIManager.getColor("Panel.background"));
//            }
            
            ((Component) value).setForeground(UIManager.getColor("Panel.foreground"));
            return ((Component) value);
        } else {
//            if (value instanceof String) {
//            if (column == 0) {
//                setToolTipText((String)value);
//            int rowModel = table.convertRowIndexToModel(row);
//            String valueFromModel = (String)table.getModel().getValueAt(rowModel,column);
//            setToolTipText(valueFromModel);
//            }
            return new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        
    }
    
}
