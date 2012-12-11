package emr_vis_nlp.model;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.model.mpqa_colon.Dataset;
import emr_vis_nlp.model.mpqa_colon.Document;
import java.io.File;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

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
        attributeEnabledList = new ArrayList<>();
        for (String attribute : attributeList) {
            attributeEnabledList.add(true);
        }
    }
}
