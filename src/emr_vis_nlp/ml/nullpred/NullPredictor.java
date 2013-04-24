
package emr_vis_nlp.ml.nullpred;

import emr_vis_nlp.ml.MLPredictor;
import emr_vis_nlp.ml.PredictionCertaintyTuple;
import emr_vis_nlp.model.MainModel;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.AbstractDocument;

/**
 * null predictor to eliminate null-pointer references when no real predictor is loaded.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class NullPredictor extends MLPredictor {

    public NullPredictor() {
    }
    
//    @Override
//    public List<Feature> getFeatureListForAttribute(String attributeName) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public List<Feature> getFeatureListForAttribute(String attributeName, String attributeValue) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public PredictorStatistics getQualityAssessmentOfPredictor(MainModel model, String attributeName) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

    @Override
    public Map<String, PredictionCertaintyTuple> getPredictionsForDoc(int globalDocId) {
        return new HashMap<>();
    }

    @Override
    public boolean hasPrediction(int globalDocId, String attrName) {
        return false;
    }

    @Override
    public PredictionCertaintyTuple getPrediction(int globalDocId, String attrName) {
        return null;
    }

    @Override
    public boolean canWriteDocTextWithHighlights(int globalDocId, int globalAttrId) {
        return false;
    }

    @Override
    public void writeDocTextWithHighlights(AbstractDocument abstDoc, int globalDocId, int globalAttrId) {
        ;
    }
    

    @Override
    public Map<String, Double> getTermWeightsForDocForAttr(int globalDocId, int globalAttrId) {
        return new HashMap<>();
    }

    @Override
    public Map<String, Double> getTermWeightsForDocForAttr(int globalDocId, String globalAttrName) {
        return new HashMap<>();
    }

    @Override
    public void loadPredictions(MainModel model) {
        ;
    }
    
}
