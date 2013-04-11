
package emr_vis_nlp.ml.colon_vars;

import emr_vis_nlp.ml.Attribute;
import emr_vis_nlp.ml.MLPredictor;
import java.util.List;
import java.util.Map;

/**
 * MLPredictor oriented around predicting variables for the colonoscopy data. 
 *
 * @author alexander.p.conrad@gmail.com
 */
public class MLPredictorColonVars extends MLPredictor {
    
    public MLPredictorColonVars() {
        
    }

    @Override
    public void loadPredictions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, Double> getTermWeightsForDocForAttr(int globalDocId, int globalAttrId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, Double> getTermWeightsForDocForAttr(int globalDocId, String globalAttrName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
