
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
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // get name of attr from row; map back through table
        //  only render if attr is "proper"
        if (value instanceof JPanel) {
            // needed to make the interactive histograms appear
            ((Component) value).setForeground(UIManager.getColor("Panel.foreground"));
            return ((Component) value);
        } else {
            // if not a histogram, use default renderer
            return new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        
    }
    
}
