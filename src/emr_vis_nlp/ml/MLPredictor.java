package emr_vis_nlp.ml;

import emr_vis_nlp.model.Document;
import emr_vis_nlp.model.MainModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.text.AbstractDocument;

/**
 * Abstract class for machine learning prediction models for use with the
 * emr-vis-nlp interface. MLPredictors are responsible for acting as interface
 * between machine learning and front-end by: supplying predictions for given
 * attributes on-demand, providing features along with measures of importance
 * for each predictor, providing overall summary information for quality of each
 * predictor, providing certainty information for each prediction, and accepting
 * new user input in the form of labels (and possibly additional hints as to
 * relevant parts of the document) to refine the back-end predictor models.
 *
 * To implement a new MLPredictor, extend this class by overriding the
 * loadPredictions() method. Optionally, the two text highlighting methods can
 * also be implemented with proper functionality in order to provide highlighted
 * documents for use by the front-end.
 * 
 * @author alexander.p.conrad@gmail.com
 */
public abstract class MLPredictor {
    
    /**
     * Backing data source for which to build predictions
     */
    protected MainModel model;
    
    /*
     * For each Document, map from each attribute name to prediction
     */
    protected List<Map<String, PredictionCertaintyTuple>> predictionMapList;
    
    /**
     * List of all attributes in dataset
     */
    protected List<String> attributeList;
    
    /**
     * List of all documents in dataset
     */
    protected List<Document> documentList;
    
    public MLPredictor(MainModel model) {
        this.model = model;
        attributeList = model.getAllAttributes();
        documentList = model.getAllDocuments();
        predictionMapList = new ArrayList<>();
        loadPredictions();
    }
    
    public boolean hasPrediction(int globalDocId, String attrName) {
        Map<String, PredictionCertaintyTuple> docPredMap = predictionMapList.get(globalDocId);
        if (docPredMap.containsKey(attrName)) {
            return true;
        }
        return false;
    }
    
    public PredictionCertaintyTuple getPrediction(int globalDocId, String attrName) {
        Map<String, PredictionCertaintyTuple> docPredMap = predictionMapList.get(globalDocId);
        if (docPredMap.containsKey(attrName)) {
            return docPredMap.get(attrName);
        }
        return null;
    }
    
    public Map<String, PredictionCertaintyTuple> getPredictionsForDoc(int globalDocId) {
        return predictionMapList.get(globalDocId);
    }
    
    
    /**
     * Loads and/or generates appropriate attribute predictions for the model
     * data. Namely, this method is responsible for appropriately populating
     * predictionMapList.
     */
    public abstract void loadPredictions();
    
    
    /** text highlighting methods **/
    
    /**
     * Given a document ID and attribute ID, determines whether the predictor currently supports building of a highlighted version of the document.
     * 
     * @param globalDocId global int identifier for Document; serves as index into documentList
     * @param globalAttrId global int identifier for attribute; serves as index into attributeList
     * @return true if predictor possesses necessary functionality to build highlighted version of document text, false otherwise
     */
    public abstract boolean canWriteDocTextWithHighlights(int globalDocId, int globalAttrId);
    
    /**
     * Given a document ID and attribute ID, builds a highlighted version of that document's text. This highlighted text is written into abstDoc.
     * 
     * @param abstDoc AbstractDocument whose content is to be replaced with that of the highlighted document text
     * @param globalDocId global int identifier for Document; serves as index into documentList
     * @param globalAttrId global int identifier for attribute; serves as index into attributeList
     * @see emr_vis_nlp.ml.deprecated.DeprecatedMLPredictor.writeDocTextWithHighlights(AbstractDocument abstDoc, int globalDocId, int globalAttrId)
     */
    public abstract void writeDocTextWithHighlights(AbstractDocument abstDoc, int globalDocId, int globalAttrId);
    
    
    /** optional methods **/
    
//    public abstract List<Feature> getFeatureListForAttribute(String attributeName);
    
//    public abstract List<Feature> getFeatureListForAttribute(String attributeName, String attributeValue);
    
//    public abstract PredictorStatistics getQualityAssessmentOfPredictor(MainModel model, String attributeName);
    
    
}
