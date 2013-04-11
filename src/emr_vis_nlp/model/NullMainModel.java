package emr_vis_nlp.model;

import emr_vis_nlp.controller.MainController;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Empty model for use when no dataset is loaded.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class NullMainModel implements MainModel {

    /**
     * this MainModel's governing MainController
     */
    private MainController controller;

    public NullMainModel() {
        this.controller = MainController.getMainController();
    }

//    @Override
//    public void loadDataFromDoclist(String doclistPath) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void loadDataFromDoclist(File doclist) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void applySimpleStringFilter(String str) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public DocTableModel buildSimpleDocTableModel() {
//        throw new UnsupportedOperationException();
//    }
//    
//    @Override
//    public AttrTableModel buildSimpleAttrSelectionTableModel() {
//        throw new UnsupportedOperationException();
//    }
//    
//    @Override
//    public AttrTableModel buildSimpleAttrSelectionTableModelFocusOnly() {
//        throw new UnsupportedOperationException();
//    }
    
    @Override
    public void setEnabledAttributes(List<Boolean> selectedAttributes) {
        ;
    }

    @Override
    public List<Document> getAllDocuments() {
        return new ArrayList<Document>();
    }

    @Override
    public List<Boolean> getIsDocumentEnabledList() {
        return new ArrayList<Boolean>();
    }
    
    @Override
    public List<String> getAllAttributeNames() {
        return new ArrayList<String>();
    }

    @Override
    public List<Boolean> getIsAttributeEnabledList() {
        return new ArrayList<Boolean>();
    }

//    @Override
//    public Map<String, PredictionCertaintyTuple> getPredictionsForDoc(int globalDocId) {
//        throw new UnsupportedOperationException();
//    }
    
//    @Override
//    public TableModel buildSimpleTreeMapSelectionTableModel() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public TableModel buildSimpleDocGridSelectionTableModel() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    @Override
    public Map<String, Integer> getAttributeValueCountMap(String attrName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean updateDocumentAttr(int docID, String docAttr, String docAttrVal) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    @Override
//    public boolean hasPrediction(int globalDocId, String attrName) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    @Override
    public boolean hasManAnn(int globalDocId, String attrName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    @Override
//    public PredictionCertaintyTuple getPrediction(int globalDocId, String attrName) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    @Override
    public String getManAnn(int globalDocId, String attrName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    @Override
//    public boolean canWriteDocTextWithHighlights(int globalDocId, int globalAttrId) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

//    @Override
//    public void writeDocTextWithHighlights(AbstractDocument abstDoc, int globalDocId, int globalAttrId) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    @Override
    public void setIsAttributeEnabled(int globalAttrID, boolean isEnabled) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setIsAttributeEnabled(String attrName, boolean isEnabled) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setIsDocumentEnabled(int globalDocID, boolean isEnabled) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
