package emr_vis_nlp.ml.deprecated;

import emr_vis_nlp.ml.*;
import emr_vis_nlp.model.MainModel;
import emr_vis_nlp.model.Document;
import emr_vis_nlp.model.mpqa_colon.DatasetTermTranslator;
import java.awt.Color;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * 
 * MLPredictor which uses models from Summer 2012 to assign predictions on
 * colonoscopy dataset. WARNING: some of these prediction models may have been
 * trained on some of the same data that's being visualized in the current demo
 * dataset!
 * 
 * @deprecated 
 * @author alexander.p.conrad@gmail.com
 */
public class DeprecatedMLPredictor extends MLPredictor {
    
    private Map<String, Map<String, Double>> memoizedTermWeightMaps;
    
    /**
     * Backing data source for which to build predictions
     */
    protected MainModel model;
    
    /**
     * List of all documents in dataset
     */
    protected List<Document> documentList;
    
    public DeprecatedMLPredictor(MainModel model) {
        this.model = model;
        
        List<String> attributeNameList = model.getAllAttributeNames();  // do we necessarily want to do this? perhaps sometimes we may want to get the attributes from the prediction model instead?
        attributeList = new ArrayList<>();
        attributeEnabledList = new ArrayList<>();
        int counter = 0;
        attrNameToIndexMap = new HashMap<>();
        for (String attributeName : attributeNameList) {
            List<String> attrVals = new ArrayList<>(); attrVals.add("Pass"); attrVals.add("Fail"); attrVals.add("N/A"); 
            Attribute attribute = new Attribute(Attribute.AttributeType.INDICATOR_CATEGORICAL, attributeName, attributeName, attrVals);
            attributeList.add(attribute);
            attributeEnabledList.add(true);
            attrNameToIndexMap.put(attributeName, counter);
            counter++;
        }
        
        documentList = model.getAllDocuments();
        predictionMapList = new ArrayList<>();
        
        memoizedTermWeightMaps = new HashMap<>();
        
        loadPredictions(model);
        
    }
    
    @Override
    public void loadPredictions(MainModel model) {
        
        // load predictions for all docs
        List<String> defaultValList = DatasetTermTranslator.getDefaultValList();
        for (int d=0; d<documentList.size(); d++) {
            Map<String, PredictionCertaintyTuple> predictionMap = new HashMap<>();
            RuntimeIndicatorPrediction.buildTemporaryFileForText(documentList.get(d).getText());
            
            // d1 = indicator; d2 = probs. for each val [-1, 0, 1]
            double[][] predictions = RuntimeIndicatorPrediction.predictIndicatorsForTempFile();
//            int[] predictedIndicatorScores = new int[predictions.length];
            String[] predictedIndicatorScores = new String[predictions.length];
            double[] predictedIndicatorCerts = new double[predictions.length];
            // find largest value for each indicator; store value and certainty
            for (int p = 0; p < predictions.length; p++) {
                String attributeNamePretty = DatasetTermTranslator.getAttrTranslation(RuntimeIndicatorPrediction.predictedIndicatorNames[p]);
                double negOneVal = predictions[p][0];
                double zeroVal = predictions[p][1];
                double oneVal = predictions[p][2];
                PredictionCertaintyTuple predCertTuple = null;
                if (negOneVal >= zeroVal && negOneVal >= oneVal) {
//                    predictedIndicatorScores[p] = -1;
                    predictedIndicatorScores[p] = defaultValList.get(0);
                    predictedIndicatorCerts[p] = negOneVal;
                    predCertTuple = new PredictionCertaintyTuple(attributeNamePretty, defaultValList.get(0), negOneVal);
                } else if (zeroVal >= negOneVal && zeroVal >= oneVal) {
                    predictedIndicatorScores[p] = defaultValList.get(1);
//                    predictedIndicatorScores[p] = 0;
                    predictedIndicatorCerts[p] = zeroVal;
                    predCertTuple = new PredictionCertaintyTuple(attributeNamePretty, defaultValList.get(1), zeroVal);
                } else {
                    predictedIndicatorScores[p] = defaultValList.get(2);
//                    predictedIndicatorScores[p] = 1;
                    predictedIndicatorCerts[p] = oneVal;
                    predCertTuple = new PredictionCertaintyTuple(attributeNamePretty, defaultValList.get(2), oneVal);
                }
                predictionMap.put(attributeNamePretty, predCertTuple);
            }
            predictionMapList.add(predictionMap);
        }
    }
    
    @Override
    public Map<String, Double> getTermWeightsForDocForAttr(int globalDocId, int globalAttrId) {
        
        return getTermWeightsForDocForAttr(globalDocId, attributeList.get(globalAttrId).getName());
        
    }
    
    @Override
    public Map<String, Double> getTermWeightsForDocForAttr(int globalDocId, String attrName) {
        
        // we could cache these results from initial running of model, but we'd need a map for every document x attribute pair; for now, just rerun on demand
        String memoizationKey = globalDocId+"_"+attrName;
        
        if (memoizedTermWeightMaps.containsKey(memoizationKey) && memoizedTermWeightMaps.get(memoizationKey) != null) {
            return memoizedTermWeightMaps.get(memoizationKey);
        } else if (memoizedTermWeightMaps.containsKey(memoizationKey) && memoizedTermWeightMaps.get(memoizationKey) == null) {
            return new HashMap<>();
        }
        
        RuntimeIndicatorPrediction.buildTemporaryFileForText(documentList.get(globalDocId).getText());
        RuntimeIndicatorPrediction.predictIndicatorsForTempFile();
        // get top-ranked terms for selected indicator
        Map<String, Double> termWeightMap = RuntimeIndicatorPrediction.getTermWeightsForIndicator(attrName);
        memoizedTermWeightMaps.put(memoizationKey, termWeightMap);
        if (termWeightMap != null) {
            return termWeightMap;
        }
        // termWeightMap will be null if we don't have a model for the selected attribute
        return new HashMap<>();
        
    }
    
    @Override
    public List<String> getAttributeValues(String attrName) {
        List<String> vals = new ArrayList<>();
        vals.add("N/A");
        vals.add("Fail");
        vals.add("Pass");
        return vals;
    }
    
}
