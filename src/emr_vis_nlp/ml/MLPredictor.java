package emr_vis_nlp.ml;

import emr_vis_nlp.ml.deprecated.RuntimeIndicatorPrediction;
import emr_vis_nlp.ml.deprecated.SimpleSQMatcher;
import emr_vis_nlp.model.Document;
import emr_vis_nlp.model.MainModel;
import emr_vis_nlp.model.mpqa_colon.DatasetTermTranslator;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    
    public abstract Map<String, Double> getTermWeightsForDocForAttr(int globalDocId, int globalAttrId);
    
    public abstract Map<String, Double> getTermWeightsForDocForAttr(int globalDocId, String globalAttrName);
    
    /** text highlighting methods **/
    
    /**
     * Given a document ID and attribute ID, determines whether the predictor currently supports building of a highlighted version of the document.
     * 
     * @param globalDocId global int identifier for Document; serves as index into documentList
     * @param globalAttrId global int identifier for attribute; serves as index into attributeList
     * @return true if predictor possesses necessary functionality to build highlighted version of document text, false otherwise
     */
    public boolean canWriteDocTextWithHighlights(int globalDocId, int globalAttrId) {
        // if we have a predictor for the doc, return true else false
        String selectedAttr = attributeList.get(globalAttrId);
        // translate old raw name from backend (ie, Indicator_25) into its more-useful extended form
        // TODO : this is colonoscopy-specific; ideally, we should move the entire translation proceedure into the model-specific code, to isolate the front-end from this!
        selectedAttr = DatasetTermTranslator.getRevAttrTranslation(selectedAttr);
//        Map<String, Boolean> abnormalNameMap = DatasetVarsTableModel.getAbnormalNameMap();
        Map<String, Boolean> predictionNameMap = RuntimeIndicatorPrediction.getPredictionNameMap();
//        if (!abnormalNameMap.containsKey(selectedAttr) && predictionNameMap.containsKey(selectedAttr)) {
        if (predictionNameMap.containsKey(selectedAttr)) {
            return true;
        }
        return false;
    }
    
    /**
     * Given a document ID and attribute ID, builds a highlighted version of that document's text. This highlighted text is written into abstDoc.
     * 
     * @param abstDoc AbstractDocument whose content is to be replaced with that of the highlighted document text
     * @param globalDocId global int identifier for Document; serves as index into documentList
     * @param globalAttrId global int identifier for attribute; serves as index into attributeList
     * @see emr_vis_nlp.ml.deprecated.DeprecatedMLPredictor.writeDocTextWithHighlights(AbstractDocument abstDoc, int globalDocId, int globalAttrId)
     */
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
        
//        RuntimeIndicatorPrediction.buildTemporaryFileForText(documentList.get(globalDocId).getText());
//        RuntimeIndicatorPrediction.predictIndicatorsForTempFile();
//        // get top-ranked terms for selected indicator
//        Map<String, Double> termWeightMap = RuntimeIndicatorPrediction.getTermWeightsForIndicator(attributeList.get(globalAttrId));

        Map<String, Double> termWeightMap = getTermWeightsForDocForAttr(globalDocId, globalAttrId);
        
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
    
    public List<WeightedSentence> buildSummaryLines(int globalDocId, int globalAttrId) {
        
        // find important sentences in document with respect to containing highly-weighted features
        
        Map<String, Double> termWeightMap = getTermWeightsForDocForAttr(globalDocId, globalAttrId);
        
        List<WeightedSentence> wSents = new ArrayList<>();
        
        // TODO : parse document into sentences (for now, just use the natural newlines in the text)
        String docText = documentList.get(globalDocId).getText();
        Scanner docSplitter = new Scanner(docText);
        while (docSplitter.hasNextLine()) {
            // find sum of weights of features in the sentences
            double weightSum = 0.;
            String line = docSplitter.nextLine();
            Scanner lineSplitter = new Scanner(line);
            while (lineSplitter.hasNext()) {
                String token = lineSplitter.next();
                if (termWeightMap.containsKey(token)) {
                    weightSum += termWeightMap.get(token);
                }
            }
            WeightedSentence wSent = new WeightedSentence(line, weightSum);
            wSents.add(wSent);
            
        }
        
        // sort sentences
        // return list of sentences in sorted order
        Collections.sort(wSents);
        return wSents;
        
    }
    
    
    
    
    public String buildSummary(int globalDocId, int globalAttrId) {
        
        List<WeightedSentence> summaryLines = buildSummaryLines(globalDocId, globalAttrId);
        
        // concatenate summary lines
        StringBuilder summary = new StringBuilder();
        for (WeightedSentence wSent: summaryLines) {
            String summaryLine = wSent.getText();
            summary.append(summaryLine);
            summary.append("\n");
        }
        return summary.toString();
        
    }
    
    public static class WeightedSentence implements Comparable<WeightedSentence> {
        
        private String text;
        private double weight;

        public WeightedSentence(String text, double weight) {
            this.text = text;
            this.weight = weight;
        }

        public String getText() {
            return text;
        }

        public double getWeight() {
            return weight;
        }
        
        
        @Override
        public int compareTo(WeightedSentence o) {
            if (weight > o.getWeight()) {
                return -1;
            } else if (weight < o.getWeight()) {
                return 1;
            }
            return 0;
        }
        
        
        
    }
    
    
    /** optional methods **/
    
//    public abstract List<Feature> getFeatureListForAttribute(String attributeName);
    
//    public abstract List<Feature> getFeatureListForAttribute(String attributeName, String attributeValue);
    
//    public abstract PredictorStatistics getQualityAssessmentOfPredictor(MainModel model, String attributeName);
    
    
}
