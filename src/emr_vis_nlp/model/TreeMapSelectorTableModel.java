package emr_vis_nlp.model;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.view.doc_map.DocumentTreeMapView;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Table model for the tree map view's attribute selector table.
 * Responsibilities include displaying all attributes for the current dataset,
 * providing drop-down selector boxes for selecting and prioritizing attributes
 * in the treemap, and communicating with the controller upon selection update.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class TreeMapSelectorTableModel extends AbstractTableModel {

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
    private int numOptions = DocumentTreeMapView.DOCTREEMAP_GROUP_PALETTE.length + 1;
    private String[] optionList;

    public TreeMapSelectorTableModel(List<String> _allAttributes, MainController controller) {

        this.controller = controller;

        // build list of possible combo box items
        // TODO more interesting / informative selection options? better selection method beyond jcombobox?
        optionList = new String[numOptions];
        optionList[0] = "(not selected)";
        for (int i = 1; i < numOptions; i++) {
            optionList[i] = i + "!";
        }

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

            // add listener to check for changes?


        }

    }

    private void validateTableSelections(int updatedRow) {

        // ensure that there are no duplicate selections in the table;
        // if another row (besides updatedRow) has same value:
        //  if last element, simply replace and unselect other
        //  else, start at 2nd-to-last element, push each down to next lower 
        //   until reaching (and pushing) the other-row-with-same-value

        String updatedRowValue = allAttributesSelectorBoxes.get(updatedRow).getSelectedItem().toString();
        int updatedRowValInt = -1;
        try {
            updatedRowValInt = Integer.parseInt(updatedRowValue.substring(0, 1));
        } catch (NumberFormatException e) {}

        if (!updatedRowValue.equals(NOT_SEL_MSG)) {

            // see if any other rows have same value
            boolean updatedValInOtherRows = false;
            for (int r = 0; r < allAttributesSelectorBoxes.size(); r++) {
                if (r != updatedRow) {
                    String otherRowValue = allAttributesSelectorBoxes.get(r).getSelectedItem().toString();
                    if (otherRowValue.equals(updatedRowValue)) {
                        updatedValInOtherRows = true;
                        break;
                    }
                }
            }

            if (updatedValInOtherRows) {

                // need to remove duplicates from list, push items down
                // since few elements, just do simple bubblesort-style proceedure

                // find, remove last element (make sure not to be the new row!)
                boolean removedLast = false;
                for (int r = 0; r < allAttributesSelectorBoxes.size(); r++) {
                    if (r != updatedRow) {
                        String rowStrVal = allAttributesSelectorBoxes.get(r).getSelectedItem().toString();
                        int rowIntVal = Integer.MAX_VALUE;
                        try {
                            rowIntVal = Integer.parseInt(rowStrVal.substring(0, 1));
                        } catch (NumberFormatException e) {
                        }

                        if (rowIntVal == numOptions - 1) {
                            allAttributesSelectorBoxes.get(r).setSelectedIndex(0);
                            removedLast = true;
                        }
                    }
                }
                // debug
                if (!removedLast) {
                    System.err.println("TreeMapSelectorTableModel: could not find last element in jcomboboxes!");
                }

                // for each element from last to selected (exclusive), 
                //  reset 1+element (won't be present for largest numbered element)
                //  increment element

                for (int v = numOptions - 1; v >= updatedRowValInt; v--) {

                    // find position of v+1 (assume less than 10) (if present)
                    int v1Index = -1;
                    for (int r = 0; r < allAttributesSelectorBoxes.size(); r++) {
                        try {
                            int val = Integer.parseInt(allAttributesSelectorBoxes.get(r).getSelectedItem().toString().substring(0, 1));
                            if (val == v + 1) {
                                v1Index = r;
                            }
                        } catch (NumberFormatException e) {
                        }
                    }

                    if (v1Index != -1) {
                        JComboBox box = allAttributesSelectorBoxes.get(v1Index);
                        box.setSelectedIndex(0);
                    }

                    // find, increment v
                    // ensure we're not manipulating the updatedRow!
                    int vIndex = -1;
                    for (int r = 0; r < allAttributesSelectorBoxes.size(); r++) {
                        if (r != updatedRow) {
                            try {
                                if (Integer.parseInt(allAttributesSelectorBoxes.get(r).getSelectedItem().toString().substring(0, 1)) == v) {
                                    vIndex = r;
                                }
                            } catch (NumberFormatException e) {
                            }
                        }
                    }

                    if (vIndex != -1) {
                        JComboBox box = allAttributesSelectorBoxes.get(vIndex);
                        box.setSelectedIndex(box.getSelectedIndex() + 1);
                    } else {
                        System.err.println("TreeMapSelectorTableModel: could not find intermediate target element " + v + " in jcomboboxes!");
                    }

                }

                // inform table that values have changed
                fireTableDataChanged();

            }

        } else {
            
            // the null option was selected; 
            
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
            return "TreeMap Priority";
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
            controller.updateTreeMapAttributes();

        }
        
    }
    
    public List<String> getSelectedAttributeList() {
        
        List<String> selectedAttributeList = new ArrayList<>();
        for (int i=0; i<numOptions-1; i++) {
            selectedAttributeList.add("");
        }
        
        for (int r=0; r<allAttributesSelectorBoxes.size(); r++) {
            try {
                int rowSelectorVal = Integer.parseInt(allAttributesSelectorBoxes.get(r).getSelectedItem().toString().substring(0, 1));
                String rowAttrName = allAttributes.get(r);
                for (int a=0; a<selectedAttributeList.size(); a++) {
                    if (rowSelectorVal-1 == a) {
                        selectedAttributeList.remove(a);
                        selectedAttributeList.add(a, rowAttrName);
                    }
                }
            } catch (NumberFormatException e) {
            }

        }
        
        // remove all blanks
        for (int a=0; a<selectedAttributeList.size(); a++) {
            if (selectedAttributeList.get(a).equals("")) {
                selectedAttributeList.remove(a);
            }
        }
        
        return selectedAttributeList;
        
    }
    
}
