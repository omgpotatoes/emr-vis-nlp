package emr_vis_nlp.model;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.model.mpqa_colon.Dataset;
import java.io.File;
import java.util.*;
import javax.swing.table.TableModel;

/**
 *
 * @author alexander.p.conrad@gmail.com
 */
public class MpqaColonMainModel implements MainModel {

    /**
     * legacy code representing the active dataset; just use this to load the
     * documents
     */
    private Dataset dataset;
    /**
     * list of all documents in dataset
     */
    private List<Document> documentList;
    /**
     * list of indicators as to which documents are currently enabled
     */
    private List<Boolean> documentEnabledList;
    /**
     * list of all attributes in dataset
     */
    private List<String> attributeList;
    /**
     * list of indicators as to which documents are currently enabled
     */
    private List<Boolean> attributeEnabledList;
    /**
     * this MainModel's governing MainController
     */
    private MainController controller;
    
    /**
     * Attributes to be passed along to the front-end
     */
    private static List<String> focusAttrs = null;
    /**
     * Map of focus attrs
     */
    private static Map<String, Boolean> focusAttrsMap = null;

    public MpqaColonMainModel(MainController controller) {

        this.controller = controller;
        dataset = null;
        documentList = new ArrayList<>();
        documentEnabledList = new ArrayList<>();
        attributeList = new ArrayList<>();

    }

    @Override
    public void loadDataFromDoclist(String doclistPath) {
        loadDataFromDoclist(new File(doclistPath));
    }

    @Override
    public void loadDataFromDoclist(File doclist) {
        dataset = Dataset.loadDatasetFromDoclist(doclist);
        documentList = dataset.getDocuments();
        // by default, enable docs
        enableAllDocuments();
        // get attributes from dataset
        attributeList = dataset.getAllAttributesFromDocs();
        // by default, enable attributes
        enableAllAttributes();
        // build list of selected attrs
        getFocusAttrs();
    }

    @Override
    public void applySimpleStringFilter(String str) {
        // iterate through documents; if doc contains string, enable; otherwise disable
        for (int d = 0; d < documentList.size(); d++) {
            Document doc = documentList.get(d);
            String docText = doc.getText();
            // TODO more robust string searching
            boolean enable = false;
            if (docText.contains(str)) {
                enable = true;
            }
            boolean oldEnabledStatus = documentEnabledList.remove(d);
            documentEnabledList.add(d, enable);
        }
    }

    @Override
    public DocTableModel buildSimpleDocTableModel() {
        DocTableModel docTableModel = new DocTableModel(documentList, documentEnabledList, attributeList, attributeEnabledList);
        return docTableModel;
    }
    
    @Override
    public AttrTableModel buildSimpleAttrSelectionTableModel() {
        AttrTableModel attrTableModel = new AttrTableModel(attributeList, attributeEnabledList, controller);
        return attrTableModel;
    }
    
    @Override
    public AttrTableModel buildSimpleAttrSelectionTableModelFocusOnly() {
        throw new UnsupportedOperationException("Not supported yet.");
//        AttrTableModel attrTableModel = new AttrTableModel(getFocusAttrs(), attributeEnabledList, controller);
//        return attrTableModel;
    }
    
    @Override
    public void setSelectedAttributes(List<Boolean> selectedAttributes) {
        // copy values from old list to new list
        // ensure same length
        if (attributeEnabledList.size() != selectedAttributes.size()) {
            throw new InputMismatchException("lengths not equal: "+attributeEnabledList.size()+" vs "+selectedAttributes.size());
        }
        
        attributeEnabledList = new ArrayList<>();
        for (int a=0; a<selectedAttributes.size(); a++) {
            attributeEnabledList.add(selectedAttributes.get(a));
        }
        
    }

    public void enableAllDocuments() {
        documentEnabledList = new ArrayList<>();
        for (Document doc : documentList) {
            documentEnabledList.add(true);
        }
    }

    
    public void enableAllAttributes() {
        // enable only the focus attrs
        enableAllAttributes(true);
    }
    
    public void enableAllAttributes(boolean focusOnly) {
        
        if (!focusOnly) {
            attributeEnabledList = new ArrayList<>();
            for (String attribute : attributeList) {
                attributeEnabledList.add(true);
            }
        } else {
            attributeEnabledList = new ArrayList<>();
            getFocusAttrsMap();
            for (String attribute : attributeList) {
                if (focusAttrsMap.containsKey(attribute)) {
                    attributeEnabledList.add(true);
                } else {
                    attributeEnabledList.add(false);
                }
            }
        }
        
    }
    

    @Override
    public List<Document> getAllDocuments() {
        return documentList;
    }

    @Override
    public List<Boolean> getAllSelectedDocuments() {
        return documentEnabledList;
    }
    
    @Override
    public List<String> getAllAttributes() {
        return attributeList;
    }

    @Override
    public List<Boolean> getAllSelectedAttributes() {
        return attributeEnabledList;
    }
    
    
    
    public static List<String> getFocusAttrs() {
    	
        if (focusAttrs != null) {
            return focusAttrs;
        }
        
    	focusAttrs = new ArrayList<>();
    	
        focusAttrs.add("name");
        
    	focusAttrs.add("Indicator_19");
    	focusAttrs.add("Indicator_16");
    	focusAttrs.add("Indicator_2");
    	focusAttrs.add("Indicator_17");
    	focusAttrs.add("Indicator_3.1");
    	focusAttrs.add("Indicator_11");
    	focusAttrs.add("Indicator_21");
    	focusAttrs.add("VAR_Withdraw_time");
    	focusAttrs.add("VAR_Procedure_aborted");
    	focusAttrs.add("VAR_ASA");
    	focusAttrs.add("VAR_Prep_adequate");
    	focusAttrs.add("VAR_Indication_type");
    	focusAttrs.add("VAR_Nursing_Reports");
    	focusAttrs.add("VAR_Informed_consent");
    	focusAttrs.add("VAR_Cecum_(reached_it)");
    	focusAttrs.add("VAR_Indication_Type_3");
    	focusAttrs.add("VAR_Indication_Type_2");
    	focusAttrs.add("VAR_Any_adenoma");
    	focusAttrs.add("VAR_cecal_landmark");
    	focusAttrs.add("VAR_Biopsy");
        
        focusAttrs.add("text");
    	
    	return focusAttrs;
    	
    }
    
    public static Map<String, Boolean> getFocusAttrsMap() {
        
        if (focusAttrsMap != null) {
            return focusAttrsMap;
        }
        
        focusAttrsMap = new HashMap<>();
        List<String> focusAttrsList = getFocusAttrs();
        
        for (String focusAttr : focusAttrsList) {
            focusAttrsMap.put(focusAttr, true);
        }
        
        return focusAttrsMap;
        
    }
    
    @Override
    public TableModel buildSimpleTreeMapSelectionTableModel() {
        TableModel treeMapTableModel = new TreeMapSelectorTableModel(attributeList, controller);
        return treeMapTableModel;
    }
    
    @Override
    public TableModel buildSimpleDocGridSelectionTableModel() {
        TableModel docGridTableModel = new DocGridTableSelectorModel(attributeList, controller);
        return docGridTableModel;
    }

    @Override
    public Map<String, PredictionCertaintyTuple> getPredictionsForDoc(int globalDocId) {
        
        // TODO
        
        
        
        
        
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
