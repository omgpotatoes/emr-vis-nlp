package emr_vis_nlp.ml;

import emr_vis_nlp.model.Document;
import emr_vis_nlp.model.MainModel;
import emr_vis_nlp.model.PredictionCertaintyTuple;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.text.AbstractDocument;

/**
 * Interface for machine learning prediction models for use with the emr-vis-nlp
 * interface. MLPredictors are responsible for acting as interface between
 * machine learning and interface by: supplying predictions for given attributes
 * on-demand, providing features along with measures of importance for each
 * predictor, providing overall summary information for quality of each
 * predictor, providing certainty information for each prediction, and accepting
 * new user input in the form of labels (and possibly additional hints as to
 * relevant parts of the document) to refine the back-end predictor models.
 *
 * @author alexander.p.conrad@gmail.com
 */
public abstract class MLPredictor {
    
    private MainModel model;
    
    /*
     * maps from attributes to predictions for each doc
     */
    protected List<Map<String, PredictionCertaintyTuple>> predictionMapList;
    
    /**
     * list of all attributes in dataset
     */
    protected List<String> attributeList;
    
    /**
     * list of all documents in dataset
     */
    protected List<Document> documentList;
    
    public MLPredictor(MainModel model) {
        this.model = model;
        predictionMapList = new ArrayList<>();
        attributeList = new ArrayList<>();
    }
    
    public abstract List<Feature> getFeatureListForAttribute(String attributeName);
    public abstract List<Feature> getFeatureListForAttribute(String attributeName, String attributeValue);
    
    
    public abstract PredictorStatistics getQualityAssessmentOfPredictor(MainModel model, String attributeName);
    
    
    // from MainModel:
    // (where should manAnn methods go? come over here or go to main)
    public abstract Map<String, PredictionCertaintyTuple> getPredictionsForDoc(int globalDocId);
    public abstract boolean hasPrediction(int globalDocId, String attrName);
//    public boolean hasManAnn(int globalDocId, String attrName);
    public abstract PredictionCertaintyTuple getPrediction(int globalDocId, String attrName);
//    public String getManAnn(int globalDocId, String attrName);
    public abstract boolean canWriteDocTextWithHighlights(int globalDocId, int globalAttrId);
    public abstract void writeDocTextWithHighlights(AbstractDocument abstDoc, int globalDocId, int globalAttrId);
    
    
    
}
