package emr_vis_nlp.ml;

import emr_vis_nlp.model.MainModel;
import java.util.List;

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
public interface MLPredictor {
    
    public List<Feature> getFeatureListForAttribute(String attributeName);
    public List<Feature> getFeatureListForAttribute(String attributeName, String attributeValue);
    
    public Prediction getPredictionForAttributeInDoc(String docText, String attributeName);
    
    public PredictorStatistics getQualityAssessmentOfPredictor(MainModel model, String attributeName);
    
    
}
