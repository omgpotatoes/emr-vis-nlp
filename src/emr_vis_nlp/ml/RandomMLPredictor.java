
package emr_vis_nlp.ml;

import emr_vis_nlp.model.MainModel;
import emr_vis_nlp.model.PredictionCertaintyTuple;
import java.util.List;
import java.util.Map;
import javax.swing.text.AbstractDocument;

/**
 * MLPredictor which assigns predictions in a random fashion; for testing only.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class RandomMLPredictor extends MLPredictor {

    public RandomMLPredictor(MainModel model) {
        super(model);
    }
    
    @Override
    public List<Feature> getFeatureListForAttribute(String attributeName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Feature> getFeatureListForAttribute(String attributeName, String attributeValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PredictorStatistics getQualityAssessmentOfPredictor(MainModel model, String attributeName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, PredictionCertaintyTuple> getPredictionsForDoc(int globalDocId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasPrediction(int globalDocId, String attrName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PredictionCertaintyTuple getPrediction(int globalDocId, String attrName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canWriteDocTextWithHighlights(int globalDocId, int globalAttrId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeDocTextWithHighlights(AbstractDocument abstDoc, int globalDocId, int globalAttrId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
}
