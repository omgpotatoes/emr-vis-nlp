
package emr_vis_nlp.view.doc_grid;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.view.var_bar_chart.VarBarChartForCell;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;

/**
 * backing TableModel for the document grid selector table containing the JComboBox axes selectors and interactive histograms.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocGridTableSelectorModel extends AbstractTableModel {
    
//    public static final String NOT_SEL_MSG = "(not selected)";
    public static final String NOT_SEL_MSG = "";
    public static final String X_AXIS_SEL = "x_axis";
    public static final String Y_AXIS_SEL = "y_axis";
//    public static final String SHAPE_SEL = "shape";
    public static final String COLOR_SEL = "color";
    
    // map listing attribute names which do not have normal categorical values
//    private static Map<String, Boolean> abnormalNamesMap = null;
    
    /**
     * controller to which this model is responsible
     */
    private MainController controller;
    /**
     * list of all attribute names
     */
    private List<String> allAttributes;
    /**
     * indicates whether row is selectable as axis
     */
    private List<Boolean> canSelect;
    /**
     * list of varBarCharts to be displayed within the table
     */
    private List<JPanel> distribDisplays;
    /**
     * list of jcomboboxes for selecting active attributes
     */
    private List<JComboBox> allAttributesSelectorBoxes;
    /**
     * number of options each combo box should present
     */
    private int numOptions = 4;
    private String[] optionList;
    
    
    public DocGridTableSelectorModel(List<String> _allAttributes) {

        this.controller = MainController.getMainController();

        // build list of possible combo box items
        optionList = new String[numOptions];
        optionList[0] = NOT_SEL_MSG;
        optionList[1] = X_AXIS_SEL;
        optionList[2] = Y_AXIS_SEL;
//        optionList[3] = SHAPE_SEL;
        optionList[3] = COLOR_SEL;

        allAttributes = new ArrayList<>();
        allAttributesSelectorBoxes = new ArrayList<>();
        for (int a = 0; a < _allAttributes.size(); a++) {
            String attributeName = _allAttributes.get(a);
            allAttributes.add(attributeName);
            JComboBox attributeSelectorBox = new JComboBoxSortable(optionList);
            allAttributesSelectorBoxes.add(attributeSelectorBox);
            if (a < numOptions) {
                attributeSelectorBox.setSelectedIndex(a);
            } else {
                attributeSelectorBox.setSelectedIndex(0);
            }
            
        }
        
        // build varBarCharts
//        getAbnormalNameMap();
        distribDisplays = new ArrayList<>();
        canSelect = new ArrayList<>();
        for (int a = 0; a < allAttributes.size(); a++) {
            String name = allAttributes.get(a);
//            if (abnormalNamesMap.containsKey(name)) {
//                canSelect.add(false);
//            } else {
                canSelect.add(true);
//            }
            // build varBarChart
            VarBarChartForCell barChart = controller.getVarBarChartForCell(name);
            distribDisplays.add(barChart);
//            }
        }
        
    }

    private void validateTableSelections(int updatedRow) {

        // ensure that there are no duplicate selections in the table;
        // if another row (besides updatedRow) has same value:
        //  simply deselect that other row
        String updatedRowValue = allAttributesSelectorBoxes.get(updatedRow).getSelectedItem().toString();
        
        if (!updatedRowValue.equals(NOT_SEL_MSG)) {
            for (int r = 0; r < allAttributesSelectorBoxes.size(); r++) {
                if (r != updatedRow) {
                    String otherRowValue = allAttributesSelectorBoxes.get(r).getSelectedItem().toString();
                    if (otherRowValue.equals(updatedRowValue)) {
                        allAttributesSelectorBoxes.get(r).setSelectedIndex(0);
                        break;
                    }
                }
            }
        } else {
            // if NOT_SEL_MSG, make sure 2 axes are selected? or, simply let fail?
        }

    }

    @Override
    public int getRowCount() {
        if (allAttributes != null) {
            return allAttributes.size();
        }
        return 0;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return "Attribute Name";
        } else if (columnIndex == 1) {
            return "Axis";
        } else if (columnIndex == 2) {
            return "Skew";
        }
        return "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return allAttributes.get(rowIndex);
        } else if (columnIndex == 1) {
            return allAttributesSelectorBoxes.get(rowIndex);
        } else if (columnIndex == 2) {
            return distribDisplays.get(rowIndex);
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return false;
        } else if (columnIndex == 1) {
            // ensure that we're in a valid row
            if (canSelect.get(rowIndex).booleanValue()) {
                return true;
            }
            return false;
        } else if (columnIndex == 2) {
            return false;
        }
        return false;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return String.class;
        } else if (columnIndex == 1) {
            return JComboBox.class;
        } else if (columnIndex == 2) {
            return JPanel.class;
        }
        return String.class;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {

        if (columnIndex == 1) {
            if (canSelect.get(rowIndex).booleanValue()) {
                // check to ensure that there are no duplicate selections
                validateTableSelections(rowIndex);
                // inform controller of change
                controller.updateDocumentGrid();
                // reset view
                fireTableDataChanged();
            }
        }

    }
    
    public String getXAxisAttribute() {
        
        for (int r=0; r<allAttributesSelectorBoxes.size(); r++) {
            int selectedIndex = allAttributesSelectorBoxes.get(r).getSelectedIndex();
            if (selectedIndex == 1) {
                String attributeName = allAttributes.get(r);
                return attributeName;
            }
            
        }
        
        // shouldn't reach here, means nothing is selected!
        return "";
        
    }
    
    public String getYAxisAttribute() {
        
        for (int r=0; r<allAttributesSelectorBoxes.size(); r++) {
            int selectedIndex = allAttributesSelectorBoxes.get(r).getSelectedIndex();
            if (selectedIndex == 2) {
                String attributeName = allAttributes.get(r);
                return attributeName;
            }
            
        }
        
        // shouldn't reach here, means nothing is selected!
        return "";
        
    }
    
//    public String getShapeAttribute() {
//        
//        for (int r=0; r<allAttributesSelectorBoxes.size(); r++) {
//            int selectedIndex = allAttributesSelectorBoxes.get(r).getSelectedIndex();
//            if (selectedIndex == 3) {
//                String attributeName = allAttributes.get(r);
//                return attributeName;
//            }
//            
//        }
//        
//        // shouldn't reach here, means nothing is selected!
//        return "";
//        
//    }
    
    public String getColorAttribute() {
        
        for (int r=0; r<allAttributesSelectorBoxes.size(); r++) {
            int selectedIndex = allAttributesSelectorBoxes.get(r).getSelectedIndex();
            if (selectedIndex == 3) {
                String attributeName = allAttributes.get(r);
                return attributeName;
            }
            
        }
        
        // shouldn't reach here, means nothing is selected!
        return "";
        
    }
    
    public void resetVarBarCharts() {
        // reset all VarBarCharts by enabling all cells
        for (JPanel chart : distribDisplays) {
            ((VarBarChartForCell)chart).enableAllCells();
        }
        fireTableDataChanged();
    }
    
    // extention to JComboBox to support sortability within JTable
    public class JComboBoxSortable extends JComboBox {
        
        public JComboBoxSortable(String[] vals) {
            super(vals);
        }
        
        @Override
        public String toString() {
            return getSelectedItem().toString();
        }
        
    }
    
}
