
package emr_vis_nlp.model;

import emr_vis_nlp.controller.MainController;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Table model bot running the manual attribute selector table. 
 * Responsibilities include displaying all attributes for the current dataset 
 * and communicating with the controller when attribute values change.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class AttrTableModel extends AbstractTableModel {
    
    /** controller to which this model is responsible */
    private MainController controller;
    
    /** list of all attributes */
    private List<String> allAttributes;
    /** indicates which attributes are currently enabled */
    private List<Boolean> allAttributesEnabled;

    public AttrTableModel(List<String> allAttributes, List<Boolean> allAttributesEnabled, MainController controller) {
        this.controller = controller;
        // create a copy of the lists, so that changes don't impact the original lists\
//        this.allAttributes = allAttributes;
//        this.allAttributesEnabled = allAttributesEnabled;
        this.allAttributes = new ArrayList<>();
        this.allAttributesEnabled = new ArrayList<>();
        for (int a=0; a<allAttributes.size(); a++) {
            this.allAttributes.add(allAttributes.get(a));
            this.allAttributesEnabled.add(allAttributesEnabled.get(a));
        }
    }
    
    @Override
    public int getRowCount() {
        if (allAttributes != null) return allAttributes.size();
        return 0;
    }
    
    @Override
    public int getColumnCount() {
        return 2;
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) return "Attribute Name";
        else if (columnIndex == 1) return "Enabled?";
        return "";
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return allAttributes.get(rowIndex);
        } else if (columnIndex == 1) {
            return allAttributesEnabled.get(rowIndex);
        }
        return null;
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) return false;
        else if (columnIndex == 1) return true;
        return false;
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) return String.class;
        else if (columnIndex == 1) return Boolean.class;
        return String.class;
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            try {
                Boolean boolValue = (Boolean)value;
                allAttributesEnabled.remove(rowIndex);
                allAttributesEnabled.add(rowIndex, boolValue);
                // update controller
                controller.attributeSelectionUpdated(allAttributesEnabled);
            } catch (ClassCastException e) {
                // shouldn't happen
            }
        }
        
    }
    
}
