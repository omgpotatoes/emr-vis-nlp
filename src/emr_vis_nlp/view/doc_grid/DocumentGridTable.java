package emr_vis_nlp.view.doc_grid;

import emr_vis_nlp.model.Document;
import java.util.*;
import prefuse.data.Table;
import prefuse.visual.VisualItem;

/**
 * Backing Prefuse table for the DocumentGrid view.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocumentGridTable extends Table {

    public static final String NODE_ID = "id";
    public static final String NODE_NAME = "name";
    public static final String NODE_TEXT = "text";
    public static final String NODE_FOCUS_TEXT = "focus_text";
    public static final String NODE_ISACTIVE = "isactive";
    public static final String CONTINUOUS_SUFFIX = "_range";
    
    private Map<String, Map<String, Integer>> attrToValueToIntMap;
    
    /**
     * Builds table representing all active documents. 1 row-per-doc, 1
     * col-per-attr. note: Since name, text are (sometimes) attributes
     * (depending on the dataset), we should be careful about adding them twice?
     *
     * @param allAttributes names of all attributes in the documents
     * @param allDocs list of documents to be represented in this table
     * @param allDocsEnabled mask-like boolean list indicating which docs should
     * be active
     *
     */
    public DocumentGridTable(List<String> allAttributes, List<Document> allDocs, List<Boolean> allDocsEnabled) {
        // builds the superclass table (around which this subclass is essentially a wrapper)
        super();
        
        // initialize table schema for given fields, attributes
        addColumn(NODE_ID, int.class);
        addColumn(NODE_NAME, String.class);
        addColumn(NODE_TEXT, String.class);
        addColumn(NODE_FOCUS_TEXT, String.class);
        addColumn(NODE_ISACTIVE, boolean.class);
        // ensure that X2, Y2 are enabled
        addColumn(VisualItem.X2, double.class);
        addColumn(VisualItem.Y2, double.class);
        for (String attrName : allAttributes) {
            if (!hasColumn(attrName)) {
                addColumn(attrName, String.class);
            }
            if (!hasColumn(attrName+CONTINUOUS_SUFFIX)) {
                addColumn(attrName+CONTINUOUS_SUFFIX, int.class);
            }
        }

        // find all possible values for each attribute
        // map each value to an integer
        attrToValueToIntMap = new HashMap<>();
        for (String attrName : allAttributes) {
            Map<String, Integer> valueToIntMap = new HashMap<>();
            attrToValueToIntMap.put(attrName, valueToIntMap);
            int valueCounter = 0;  // will prefuse complain if we start indexing at 0?
            for (int d = 0; d < allDocs.size(); d++) {
                Document doc = allDocs.get(d);
                Map<String, String> docAttrs = doc.getAttributes();
                String value = "";
                if (docAttrs.containsKey(attrName)) {
                    value = docAttrs.get(attrName);
                }
                if (!valueToIntMap.containsKey(value)) {
                    valueToIntMap.put(value, valueCounter);
                    valueCounter++;
                }
            }
        }
        
        // populate table data
        // include continuous-value-based attribute for display
        int rowCounter = 0;
        for (int d=0; d<allDocs.size(); d++) {
            Document doc = allDocs.get(d);
            String name = doc.getName();
            String text = doc.getText();
            boolean isActive = allDocsEnabled.get(d);
            Map<String, String> docAttrs = doc.getAttributes();
            
            addRow();
            set(rowCounter, NODE_ID, rowCounter);
            set(rowCounter, NODE_NAME, name);
            set(rowCounter, NODE_TEXT, text);
            set(rowCounter, NODE_FOCUS_TEXT, text);
            set(rowCounter, NODE_ISACTIVE, isActive);
            for (String attrName : allAttributes) {
                String value = "";
                if (docAttrs.containsKey(attrName)) {
                    value = docAttrs.get(attrName);
                }
                int valueInt = attrToValueToIntMap.get(attrName).get(value);
                set(rowCounter, attrName, value);
                set(rowCounter, attrName+CONTINUOUS_SUFFIX, valueInt);
            }
            
            rowCounter++;
        }

    }
    
    public List<String> getValueListForAttribute(String attribute) {
        
        if (!attrToValueToIntMap.containsKey(attribute)) {
            System.err.println("DocumentGridTable: attribute "+attribute+" not contained in attrToValueToIntMap!");
            return new ArrayList<>();
        }
        
        Map<String, Integer> valueToIntMap = attrToValueToIntMap.get(attribute);
        Set<String> keySet = valueToIntMap.keySet();
        int numKeys = keySet.size();
        List<String> valueList = new ArrayList<>(numKeys);
        for (int i=0; i<numKeys; i++) valueList.add("");
        
        for (String key : keySet) {
            int index = valueToIntMap.get(key);
            valueList.remove(index);
            valueList.add(index, key);
        }
        
        return valueList;
        
    }
    
}
