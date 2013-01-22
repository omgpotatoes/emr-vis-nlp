
package emr_vis_nlp.model;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.view.doc_map.DocumentTreeMapView;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocGridTableSelectorModel extends AbstractTableModel {
    
    public static final String NOT_SEL_MSG = "(not selected)";
    /**
     * controller to which this model is responsible
     */
    private MainController controller;
    /**
     * list of all attribute names
     */
    private List<String> allAttributes;
    /**
     * list of jcomboboxes for selecting active attributes
     */
    private List<JComboBox> allAttributesSelectorBoxes;
    /**
     * number of options each combo box should present
     */
    private int numOptions = 3;
    private String[] optionList;
    
    
    public DocGridTableSelectorModel(List<String> _allAttributes, MainController controller) {

        this.controller = controller;

        // build list of possible combo box items
        // TODO more interesting / informative selection options? better selection method beyond jcombobox?
        optionList = new String[numOptions];
        optionList[0] = "(not selected)";
        optionList[1] = "x_axis";
        optionList[2] = "y_axis";
//        for (int i = 1; i < numOptions; i++) {
//            optionList[i] = i + "!";
//        }

        allAttributes = new ArrayList<>();
        allAttributesSelectorBoxes = new ArrayList<>();
        for (int a = 0; a < _allAttributes.size(); a++) {
            String attributeName = _allAttributes.get(a);
            allAttributes.add(attributeName);
            JComboBox attributeSelectorBox = new JComboBox(optionList);
            allAttributesSelectorBoxes.add(attributeSelectorBox);
            if (a < numOptions) {
                attributeSelectorBox.setSelectedIndex(a);
            } else {
                attributeSelectorBox.setSelectedIndex(0);
            }
            
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
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return "Attribute Name";
        } else if (columnIndex == 1) {
            return "Axis";
        }
        return "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return allAttributes.get(rowIndex);
        } else if (columnIndex == 1) {
            return allAttributesSelectorBoxes.get(rowIndex);
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return false;
        } else if (columnIndex == 1) {
            return true;
        }
        return false;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return String.class;
        } else if (columnIndex == 1) {
            return JComboBox.class;
        }
        return String.class;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {

        if (columnIndex == 1) {

            // check to ensure that there are no duplicate selections
            validateTableSelections(rowIndex);

            // inform controller of change
            controller.updateDocGridAttributes();

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
        
        return "";
        
    }
    
//    public List<String> getSelectedAttributeList() {
//        
//        List<String> selectedAttributeList = new ArrayList<>();
//        for (int i=0; i<numOptions-1; i++) {
//            selectedAttributeList.add("");
//        }
//        
//        for (int r=0; r<allAttributesSelectorBoxes.size(); r++) {
//            try {
//                int rowSelectorVal = Integer.parseInt(allAttributesSelectorBoxes.get(r).getSelectedItem().toString().substring(0, 1));
//                String rowAttrName = allAttributes.get(r);
//                for (int a=0; a<selectedAttributeList.size(); a++) {
//                    if (rowSelectorVal-1 == a) {
//                        selectedAttributeList.remove(a);
//                        selectedAttributeList.add(a, rowAttrName);
//                    }
//                }
//            } catch (NumberFormatException e) {
//            }
//
//        }
//        
//        // remove all blanks
//        for (int a=0; a<selectedAttributeList.size(); a++) {
//            if (selectedAttributeList.get(a).equals("")) {
//                selectedAttributeList.remove(a);
//            }
//        }
//        
//        return selectedAttributeList;
//        
//    }
    
    
}
