
package emr_vis_nlp.ml;

import emr_vis_nlp.model.MainModel;
import java.util.List;

/**
 * MLPredictor which assigns predictions in a random fashion; for testing only.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class RandomMLPredictor implements MLPredictor {

    @Override
    public List<Feature> getFeatureListForAttribute(String attributeName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Feature> getFeatureListForAttribute(String attributeName, String attributeValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Prediction getPredictionForAttributeInDoc(String docText, String attributeName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PredictorStatistics getQualityAssessmentOfPredictor(MainModel model, String attributeName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
