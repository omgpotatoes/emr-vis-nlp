package emr_vis_nlp.ml.deprecated;

import emr_vis_nlp.ml.Feature;
import emr_vis_nlp.ml.MLPredictor;
import emr_vis_nlp.ml.Prediction;
import emr_vis_nlp.ml.PredictorStatistics;
import emr_vis_nlp.model.MainModel;
import emr_vis_nlp.model.PredictionCertaintyTuple;
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
 * /**
 * MLPredictor which uses models from Summer 2012 to assign predictions on
 * colonoscopy dataset. WARNING: some of these prediction models may have been
 * trained on some of the same data that's being visualized in the current demo
 * dataset!
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DeprecatedMLPredictor extends MLPredictor {
    
    
    public DeprecatedMLPredictor(MainModel model) {
        super(model);
    }
    
    @Override
    public void loadPredictions() {
        
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
//            predictionCatList.add(predictedIndicatorScores);
//            predictionCertList.add(predictedIndicatorCerts);
//            predictionList.add(predictions);
            predictionMapList.add(predictionMap);
        }
    }
    
//    @Override
//    public List<Feature> getFeatureListForAttribute(String attributeName) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

//    @Override
//    public List<Feature> getFeatureListForAttribute(String attributeName, String attributeValue) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

//    @Override
//    public Prediction getPredictionForAttributeInDoc(String docText, String attributeName) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

//    @Override
//    public PredictorStatistics getQualityAssessmentOfPredictor(MainModel model, String attributeName) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
    
//    // old code used to update text region:
//    
//        // get top-ranked terms for selected indicator
//        Map<String, Double> termWeightMap = RuntimeIndicatorPrediction.getTermWeightsForIndicator(selectedAttr);
//
//        // find max, min (abs?) vals
//        double maxWeight = Double.MIN_VALUE;
//        double minWeight = 0;
//
//        // @TODO make sure it's an indicator for which we're doing prediction! else we get an exception here
//        Map<String, Boolean> abnormalNameMap = DatasetVarsTableModel.getAbnormalNameMap();
//        Map<String, Boolean> predictionNameMap = DatasetVarsTableModel.getPredictionNameMap();
//        boolean isSimpleSQVar = false;
//        String simpleSQVarRegExpStr = "";
//        if (!abnormalNameMap.containsKey(selectedAttr) && predictionNameMap.containsKey(selectedAttr)) {
//            for (Double weight : termWeightMap.values()) {
//
//                if (weight > maxWeight) {
//                    maxWeight = weight;
//                }
//
//                if (Math.abs(weight) < minWeight) {
//                    minWeight = Math.abs(weight);
//                }
//
//            }
//
//            // debug
//            System.out.println("debug: max and min weights for attr " + selectedAttr + ": " + maxWeight + ", " + minWeight);
//
//        } else {
//            // not an indicator for which we're doing prediction; so, detect for being a SimpleSQ var with patterns
//            Map<String, String> simpleSQVarsToRegExp = SimpleSQMatcher.getVarNameToPatternMap();
//            if (simpleSQVarsToRegExp.containsKey(selectedAttr)) {
//                isSimpleSQVar = true;
//                simpleSQVarRegExpStr = simpleSQVarsToRegExp.get(selectedAttr);
//            }
//
//        }
//
//
//        AbstractDocument abstDoc = (AbstractDocument) (jTextPaneSelectedDocText.getStyledDocument());
//        try {
//            abstDoc.remove(0, abstDoc.getLength());
//        } catch (BadLocationException e) {
//            e.printStackTrace();
//            System.out.println("err: could not reset doc text");
//        }
//
//        String docText = activeDataset.getDocuments().get(selectedDocumentIndex).getText();
//
//        int maxFontSize = 28;
//        int minFontSize = 12;
//
//        if (isSimpleSQVar) {
//            // highlight regexp
//            Pattern varRegExpPattern = Pattern.compile(simpleSQVarRegExpStr);
//            Matcher varRegExpMatcher = varRegExpPattern.matcher(docText);
//            boolean hasPattern = varRegExpMatcher.find();
//
//
//            // strategy: while hasPattern == true, continue to look for matches; 
//            //  store start, end match indices in a list
//            List<Integer> startIndices = new ArrayList<>();
//            List<Integer> endIndices = new ArrayList<>();
//
//            while (hasPattern) {
//
//                int start = varRegExpMatcher.start();
//                int end = varRegExpMatcher.end();
//                String matchedSubstring = varRegExpMatcher.group();
//
//                // debug
//                System.out.println("debug: found match in doc " + activeDataset.getDocuments().get(selectedDocumentIndex).getName() + " for attr " + selectedAttr + ": \"" + matchedSubstring + "\"");
//
//                startIndices.add(start);
//                endIndices.add(end);
//
//                hasPattern = varRegExpMatcher.find();
//            }
//
//            int lastEndIndex = 0;
//            if (startIndices.size() > 0) {
//
//                while (startIndices.size() > 0) {
//
//                    // iterate through indices, writing the previous unmatched
//                    // portion and following matched portion
//
//                    int plainIndexStart = lastEndIndex;
//                    int plainIndexEnd = startIndices.remove(0);
//                    int matchedIndexEnd = endIndices.remove(0);
//
//                    // unmatched
//                    try {
//                        int fontSize = minFontSize;
//                        SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                        StyleConstants.setFontSize(attrSet, fontSize);
//                        abstDoc.insertString(abstDoc.getLength(), docText.substring(plainIndexStart, plainIndexEnd),
//                                attrSet);
//                    } catch (BadLocationException e) {
//                        e.printStackTrace();
//                        System.out.println("err: could not add unweighted term to report panel: "
//                                + docText.substring(plainIndexStart,
//                                plainIndexEnd));
//                    }
//
//                    // matched
//                    try {
////						double weight = 1.0;
//                        double weight = 0.8;
//                        int fontSize = (int) (maxFontSize * weight);
//                        if (fontSize < minFontSize) {
//                            fontSize = minFontSize;
//                        }
//                        SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                        StyleConstants.setFontSize(attrSet, fontSize);
//                        StyleConstants.setBackground(attrSet, new Color(0, 255,
//                                255, (int) (255 * weight)));
//                        abstDoc.insertString(abstDoc.getLength(), docText.substring(plainIndexEnd, matchedIndexEnd),
//                                attrSet);
//                    } catch (BadLocationException e) {
//                        e.printStackTrace();
//                        System.out.println("err: could not add weighted term to report panel: "
//                                + docText.substring(plainIndexEnd,
//                                matchedIndexEnd));
//                    }
//
//                    lastEndIndex = matchedIndexEnd;
//
//                }
//
//                // print the last bit of unmatched text, if present (should be)
//                try {
//                    int fontSize = minFontSize;
//                    SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                    StyleConstants.setFontSize(attrSet, fontSize);
//                    abstDoc.insertString(abstDoc.getLength(), docText.substring(lastEndIndex),
//                            attrSet);
//                } catch (BadLocationException e) {
//                    e.printStackTrace();
//                    System.out.println("err: could not add unweighted term to report panel: "
//                            + docText.substring(lastEndIndex));
//                }
//
//            } else {
//                // regexp doesn't match, so just load plain doc
//                try {
//                    int fontSize = minFontSize;
//                    SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                    StyleConstants.setFontSize(attrSet, fontSize);
//                    abstDoc.insertString(abstDoc.getLength(), docText, attrSet);
//                } catch (BadLocationException e) {
//                    e.printStackTrace();
//                    System.out.println("err: could not load plain doc in panel (regexp not matched)");
//                }
//            }
//        } else {
//            // highlight sufficiently-weighted terms, if any
//            Scanner docTextLineSplitter = new Scanner(docText);
//            while (docTextLineSplitter.hasNextLine()) {
//                String line = docTextLineSplitter.nextLine();
//                Scanner lineSplitter = new Scanner(line);
//                while (lineSplitter.hasNext()) {
//
//                    String term = lineSplitter.next();
//
//                    // if term is highly weighted, draw with emphasis;
//                    // otherwise, draw normally
//                    double weight = 0.;
//                    double weightDiffMult = 1.3; // the larger this is, the
//                    // higher the threshold for
//                    // highlighting
//                    if (!abnormalNameMap.containsKey(selectedAttr)
//                            && predictionNameMap.containsKey(selectedAttr)
//                            && ((termWeightMap.containsKey(term) && (weight = termWeightMap.get(term)) > (maxWeight - ((maxWeight - minWeight) / weightDiffMult))) || (termWeightMap.containsKey(term.toLowerCase()) && (weight = termWeightMap.get(term.toLowerCase())) > (maxWeight - ((maxWeight - minWeight) / weightDiffMult))))) {
//
//                        // if term is weighted sufficiently for the indicator
//                        try {
//                            int fontSize = (int) (maxFontSize * (weight / maxWeight));
//                            if (fontSize < minFontSize) {
//                                fontSize = minFontSize;
//                            }
//                            SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                            StyleConstants.setFontSize(attrSet, fontSize);
//                            StyleConstants.setBackground(attrSet, new Color(0,
//                                    255, 255,
//                                    (int) (255 * (weight / maxWeight))));
//                            abstDoc.insertString(abstDoc.getLength(), term
//                                    + " ", attrSet);
//                        } catch (BadLocationException e) {
//                            e.printStackTrace();
//                            System.out.println("err: could not add weighted term to report panel: "
//                                    + term);
//                        }
//
//                    } else {
//
//                        // term is not weighted sufficiently for indicator
//                        try {
//                            int fontSize = minFontSize;
//                            SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                            StyleConstants.setFontSize(attrSet, fontSize);
//                            abstDoc.insertString(abstDoc.getLength(), term
//                                    + " ", attrSet);
//                        } catch (BadLocationException e) {
//                            e.printStackTrace();
//                            System.out.println("err: could not add unweighted term to report panel: "
//                                    + term);
//                        }
//
//                    }
//
//                }
//
//                // add a newline at the end of the line
//                try {
//                    int fontSize = minFontSize;
//                    SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                    StyleConstants.setFontSize(attrSet, fontSize);
//                    abstDoc.insertString(abstDoc.getLength(), "\n", attrSet);
//                } catch (BadLocationException e) {
//                    e.printStackTrace();
//                    System.out.println("err: could not add newline to report panel");
//                }
//
//            }
//        }
    
    @Override
    public boolean canWriteDocTextWithHighlights(int globalDocId, int globalAttrId) {
        // if we have a predictor for the doc, return true else false
        String selectedAttr = attributeList.get(globalAttrId);
        selectedAttr = DatasetTermTranslator.getRevAttrTranslation(selectedAttr);
//        Map<String, Boolean> abnormalNameMap = DatasetVarsTableModel.getAbnormalNameMap();
        Map<String, Boolean> predictionNameMap = RuntimeIndicatorPrediction.getPredictionNameMap();
//        if (!abnormalNameMap.containsKey(selectedAttr) && predictionNameMap.containsKey(selectedAttr)) {
        if (predictionNameMap.containsKey(selectedAttr)) {
            return true;
        }
        return false;
    }

    @Override
    public void writeDocTextWithHighlights(AbstractDocument abstDoc, int globalDocId, int globalAttrId) {
        
        // if we have a predictor for the doc, build else just populate with plaintext
        // if model doesn't support highlighting for this text, just insert plaintext
        
        // clear doc
        try {
            abstDoc.remove(0, abstDoc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
            System.out.println("err: could not reset doc text for doc " + globalDocId + " focus window");
        }

        String docText = documentList.get(globalDocId).getText();
        int minFontSize = 12;
        int maxFontSize = 32;
        
        
            // from annotator.MainWindow: (code to adapt:)
            
        RuntimeIndicatorPrediction.buildTemporaryFileForText(documentList.get(globalDocId).getText());
        RuntimeIndicatorPrediction.predictIndicatorsForTempFile();

        // get top-ranked terms for selected indicator
        Map<String, Double> termWeightMap = RuntimeIndicatorPrediction.getTermWeightsForIndicator(attributeList.get(globalAttrId));

        // find max, min (abs?) vals
        double maxWeight = Double.MIN_VALUE;
        double minWeight = 0;

        // TODO make sure it's an indicator for which we're doing prediction! else we get an exception here
        // translate selected attr into its deprecated form
        String selectedAttr = attributeList.get(globalAttrId);
        selectedAttr = DatasetTermTranslator.getRevAttrTranslation(selectedAttr);
//        Map<String, Boolean> abnormalNameMap = DatasetVarsTableModel.getAbnormalNameMap();
        Map<String, Boolean> predictionNameMap = RuntimeIndicatorPrediction.getPredictionNameMap();
        boolean isSimpleSQVar = false;
        String simpleSQVarRegExpStr = "";
//        if (!abnormalNameMap.containsKey(selectedAttr) && predictionNameMap.containsKey(selectedAttr)) {
        if (predictionNameMap.containsKey(selectedAttr)) {
            for (Double weight : termWeightMap.values()) {

                if (weight > maxWeight) {
                    maxWeight = weight;
                }

                if (Math.abs(weight) < minWeight) {
                    minWeight = Math.abs(weight);
                }

            }

            // debug
            System.out.println("debug: max and min weights for attr " + selectedAttr + ": " + maxWeight + ", " + minWeight);

        } else {
            // not an indicator for which we're doing prediction; so, detect for being a SimpleSQ var with patterns
            Map<String, String> simpleSQVarsToRegExp = SimpleSQMatcher.getVarNameToPatternMap();
            if (simpleSQVarsToRegExp.containsKey(selectedAttr)) {
                isSimpleSQVar = true;
                simpleSQVarRegExpStr = simpleSQVarsToRegExp.get(selectedAttr);
            }

        }
        
            // if feature is a regexp
            if (isSimpleSQVar) {
                // highlight regexp
                Pattern varRegExpPattern = Pattern.compile(simpleSQVarRegExpStr);
                Matcher varRegExpMatcher = varRegExpPattern.matcher(docText);
                boolean hasPattern = varRegExpMatcher.find();


                // strategy: while hasPattern == true, continue to look for matches; 
                //  store start, end match indices in a list
                List<Integer> startIndices = new ArrayList<>();
                List<Integer> endIndices = new ArrayList<>();

                while (hasPattern) {

                    int start = varRegExpMatcher.start();
                    int end = varRegExpMatcher.end();
                    String matchedSubstring = varRegExpMatcher.group();

                    // debug
                    System.out.println("debug: found match in doc " + documentList.get(globalDocId).getName() + " for attr " + selectedAttr + ": \"" + matchedSubstring + "\"");

                    startIndices.add(start);
                    endIndices.add(end);

                    hasPattern = varRegExpMatcher.find();
                }

                int lastEndIndex = 0;
                if (startIndices.size() > 0) {

                    while (startIndices.size() > 0) {

                        // iterate through indices, writing the previous unmatched
                        // portion and following matched portion

                        int plainIndexStart = lastEndIndex;
                        int plainIndexEnd = startIndices.remove(0);
                        int matchedIndexEnd = endIndices.remove(0);

                        // unmatched
                        try {
                            int fontSize = minFontSize;
                            SimpleAttributeSet attrSet = new SimpleAttributeSet();
                            StyleConstants.setFontSize(attrSet, fontSize);
                            abstDoc.insertString(abstDoc.getLength(), docText.substring(plainIndexStart, plainIndexEnd),
                                    attrSet);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                            System.out.println("err: could not add unweighted term to report panel: "
                                    + docText.substring(plainIndexStart,
                                    plainIndexEnd));
                        }

                        // matched
                        try {
//						double weight = 1.0;
                            double weight = 0.8;
                            int fontSize = (int) (maxFontSize * weight);
                            if (fontSize < minFontSize) {
                                fontSize = minFontSize;
                            }
                            SimpleAttributeSet attrSet = new SimpleAttributeSet();
                            StyleConstants.setFontSize(attrSet, fontSize);
                            StyleConstants.setBackground(attrSet, new Color(0, 255,
                                    255, (int) (255 * weight)));
                            abstDoc.insertString(abstDoc.getLength(), docText.substring(plainIndexEnd, matchedIndexEnd),
                                    attrSet);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                            System.out.println("err: could not add weighted term to report panel: "
                                    + docText.substring(plainIndexEnd,
                                    matchedIndexEnd));
                        }

                        lastEndIndex = matchedIndexEnd;

                    }

                    // print the last bit of unmatched text, if present (should be)
                    try {
                        int fontSize = minFontSize;
                        SimpleAttributeSet attrSet = new SimpleAttributeSet();
                        StyleConstants.setFontSize(attrSet, fontSize);
                        abstDoc.insertString(abstDoc.getLength(), docText.substring(lastEndIndex),
                                attrSet);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                        System.out.println("err: could not add unweighted term to report panel: "
                                + docText.substring(lastEndIndex));
                    }

                } else {
                    // regexp doesn't match, so just load plain doc
                    try {
                        int fontSize = minFontSize;
                        SimpleAttributeSet attrSet = new SimpleAttributeSet();
                        StyleConstants.setFontSize(attrSet, fontSize);
                        abstDoc.insertString(abstDoc.getLength(), docText, attrSet);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                        System.out.println("err: could not load plain doc in panel (regexp not matched)");
                    }
                }
            } else {
                // highlight sufficiently-weighted terms, if any
                Scanner docTextLineSplitter = new Scanner(docText);
                while (docTextLineSplitter.hasNextLine()) {
                    String line = docTextLineSplitter.nextLine();
                    Scanner lineSplitter = new Scanner(line);
                    while (lineSplitter.hasNext()) {

                        String term = lineSplitter.next();

                        // if term is highly weighted, draw with emphasis;
                        // otherwise, draw normally
                        double weight = 0.;
                        double weightDiffMult = 1.3; // the larger this is, the
                        // higher the threshold for
                        // highlighting
//                        if (!abnormalNameMap.containsKey(selectedAttr) && 
                          if ( predictionNameMap.containsKey(selectedAttr)
                                && ((termWeightMap.containsKey(term) && (weight = termWeightMap.get(term)) > (maxWeight - ((maxWeight - minWeight) / weightDiffMult))) || (termWeightMap.containsKey(term.toLowerCase()) && (weight = termWeightMap.get(term.toLowerCase())) > (maxWeight - ((maxWeight - minWeight) / weightDiffMult))))) {

                            // if term is weighted sufficiently for the indicator
                            try {
                                int fontSize = (int) (maxFontSize * (weight / maxWeight));
                                if (fontSize < minFontSize) {
                                    fontSize = minFontSize;
                                }
                                SimpleAttributeSet attrSet = new SimpleAttributeSet();
                                StyleConstants.setFontSize(attrSet, fontSize);
                                StyleConstants.setBackground(attrSet, new Color(0,
                                        255, 255,
                                        (int) (255 * (weight / maxWeight))));
                                abstDoc.insertString(abstDoc.getLength(), term
                                        + " ", attrSet);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                                System.out.println("err: could not add weighted term to report panel: "
                                        + term);
                            }

                        } else {

                            // term is not weighted sufficiently for indicator
                            try {
                                int fontSize = minFontSize;
                                SimpleAttributeSet attrSet = new SimpleAttributeSet();
                                StyleConstants.setFontSize(attrSet, fontSize);
                                abstDoc.insertString(abstDoc.getLength(), term
                                        + " ", attrSet);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                                System.out.println("err: could not add unweighted term to report panel: "
                                        + term);
                            }

                        }

                    }

                    // add a newline at the end of the line
                    try {
                        int fontSize = minFontSize;
                        SimpleAttributeSet attrSet = new SimpleAttributeSet();
                        StyleConstants.setFontSize(attrSet, fontSize);
                        abstDoc.insertString(abstDoc.getLength(), "\n", attrSet);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                        System.out.println("err: could not add newline to report panel");
                    }

                }
            }
        
        
    }
}
