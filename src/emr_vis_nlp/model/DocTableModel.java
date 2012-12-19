
package emr_vis_nlp.model;

import emr_vis_nlp.model.mpqa_colon.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/**
 * Table model for the main document table view. MainController is responsible 
 * for generating a model of this type in accordance with currently-loaded 
 * MainModel and any filtering applied by MainView. MainController is also 
 * responsible for keeping the references in this class up-to-date.
 * 
 * 
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocTableModel extends AbstractTableModel {
    
    /** list of all documents */
    private List<Document> allDocs;
    /** list of all attributes */
    private List<String> allAttributes;
    /** documents to be displayed in table */
    private List<Document> visibleDocs;
    /** indices from visible docs back to backing model */
    private List<Integer> docIndices;
    /** list of attributes to be displayed */
    private List<String> visibleAttributes;
    /** indices from visible attributes back to backing model */
    private List<Integer> attributeIndices;
    
    public DocTableModel(List<Document> allDocs, List<Boolean> allDocsEnabled, List<String> allAttributes, List<Boolean> allAttributesEnabled) {
        
        this.allDocs = allDocs;
        this.allAttributes = allAttributes;
        
        // tableModel should only be concerned with visible docs
        visibleDocs = new ArrayList<>();
        docIndices = new ArrayList<>();
        for (int d=0; d<allDocs.size(); d++) {
            Document doc = allDocs.get(d);
            boolean isDocEnabled = allDocsEnabled.get(d);
            if (isDocEnabled) {
                visibleDocs.add(doc);
                docIndices.add(d);
            }
        }
        
        // tableModel should only be concerned with visible attributes
        visibleAttributes = new ArrayList<>();
        attributeIndices = new ArrayList<>();
        for (int a=0; a<allAttributes.size(); a++) {
            String attrName = allAttributes.get(a);
            boolean isAttrEnabled = allAttributesEnabled.get(a);
            if (isAttrEnabled) {
                visibleAttributes.add(attrName);
                attributeIndices.add(a);
            }
        }
        
    }
    
    @Override
    public int getRowCount() {
        if (visibleDocs != null) return visibleDocs.size();
        return 0;
    }

    @Override
    public int getColumnCount() {
        if (visibleAttributes != null) return visibleAttributes.size();
        return 0;
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        if (visibleAttributes != null) return visibleAttributes.get(columnIndex);
        return "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Document selectedDoc = visibleDocs.get(rowIndex);
        Map<String, String> selectedDocAttrs = selectedDoc.getAttributes();
        String selectedAttrName = visibleAttributes.get(columnIndex);
        if (selectedDocAttrs.containsKey(selectedAttrName)) return selectedDocAttrs.get(selectedAttrName);
        return "";
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // for now, leave all uneditable;
        //  editing is the concern of the details window only (for now)
        return false;
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        // assume all columns are strings (for now, may change this later)
        return String.class;
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        // for now, do nothing (since table is currently not editable)
        return;
    }
    
    public String getDocTextAtIndex(int rowIndex) {
        return visibleDocs.get(rowIndex).getText();
    }
    
    public int getGlobalIndexForModelRow(int localIndex) {
        return docIndices.get(localIndex);
    }
    
}
