package emr_vis_nlp.model.mpqa_colon;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.ml.MLPredictor;
import emr_vis_nlp.ml.deprecated.RuntimeIndicatorPrediction;
import emr_vis_nlp.ml.deprecated.SimpleSQMatcher;
import emr_vis_nlp.model.Document;
import emr_vis_nlp.model.MainModel;
import emr_vis_nlp.model.mpqa_colon.Dataset;
import emr_vis_nlp.model.mpqa_colon.DatasetTermTranslator;
import java.awt.Color;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * MainModel designed to represent the MPQA-style dataset (as referenced by XML doclist).
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
     * this MainModel's governing MainController
     */
    private MainController controller;
    
    public MpqaColonMainModel(MainController controller) {

        this.controller = controller;
        dataset = null;
        documentList = new ArrayList<>();
        documentEnabledList = new ArrayList<>();
        attributeList = new ArrayList<>();

    }

    public void loadDataFromDoclist(String doclistPath) {
        loadDataFromDoclist(new File(doclistPath));
    }

    public void loadDataFromDoclist(File doclist) {
        dataset = Dataset.loadDatasetFromDoclist(doclist);
        if (dataset != null) {
            documentList = dataset.getDocuments();
            // by default, enable docs
            enableAllDocuments();
            // get attributes from dataset
            attributeList = dataset.getAllAttributesFromDocs();
        }

    }
    
    public void enableAllDocuments() {
        documentEnabledList = new ArrayList<>();
        for (Document doc : documentList) {
            documentEnabledList.add(true);
        }
    }

    @Override
    public List<Document> getAllDocuments() {
        return documentList;
    }

    @Override
    public List<Boolean> getIsDocumentEnabledList() {
        return documentEnabledList;
    }
    
    @Override
    public List<String> getAllAttributeNames() {
        return attributeList;
    }
    
    @Override
    public Map<String, Integer> getAttributeValueCountMap(String attrName) {
        // first, see if attribute is in dataset
        boolean attrInDataset = false;
        for (String attr : attributeList) {
            if (attr.equalsIgnoreCase(attrName)) {
                attrInDataset = true; break;
            }
        }
        if (!attrInDataset) {
            System.err.print("MpqaColonMainModel.getAttributeValues: attr "+attrName+" not in dataset");
            return new HashMap<>();
        }
        
        // iterate over all docs, finding all possible vals
        Map<String, Integer> valCountMap = new HashMap<>();
//        List<String> vals = new ArrayList<>();
        for (Document doc : documentList) {
            // get val from doc
            String val = "";
            Map<String, String> attrMap = doc.getAttributes();
            if (attrMap.containsKey(attrName)) {
                val = attrMap.get(attrName);
            }
            // insert / increment val
            if (!valCountMap.containsKey(val)) {
                valCountMap.put(val, 1);
//                vals.add(val);
            } else {
                valCountMap.put(val, valCountMap.get(val)+1);
            }
        }
        return valCountMap;
        
    }
    
    @Override
    public boolean updateDocumentAttr(int docID, String docAttr, String docAttrVal) {
        
        try {
            documentList.get(docID).getAttributes().put(docAttr, docAttrVal);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("MpqaColonMainModel: err: no document /w idNum="+docID);
            return false;
        }
        return true;
        
    }
    
    @Override
    public boolean hasManAnn(int globalDocId, String attrName) {
        if (documentList.get(globalDocId).getAttributes().containsKey(attrName)) {
            return true;
        }
        return false;
    }

    @Override
    public String getManAnn(int globalDocId, String attrName) {
        String val = "";
        if (documentList.get(globalDocId).getAttributes().containsKey(attrName)) {
            val = documentList.get(globalDocId).getAttributes().get(attrName);
        }
        return val;
    }

    @Override
    public void setIsDocumentEnabled(int globalDocID, boolean isEnabled) {
        documentEnabledList.remove(globalDocID);
        documentEnabledList.add(globalDocID, isEnabled);
    }
    
}
