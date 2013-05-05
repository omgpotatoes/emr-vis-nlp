package emr_vis_nlp.ml;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.ml.colon_vars.MLPredictorColonVars;
import emr_vis_nlp.ml.deprecated.RuntimeIndicatorPrediction;
import emr_vis_nlp.ml.deprecated.SimpleSQMatcher;
import emr_vis_nlp.model.Document;
import emr_vis_nlp.model.MainModel;
import emr_vis_nlp.model.mpqa_colon.DatasetMedColonDoclist;
import emr_vis_nlp.model.mpqa_colon.DatasetTermTranslator;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
    
    protected List<Attribute> attributeList;
    protected List<Boolean> attributeEnabledList;
    protected Map<String, Integer> attrNameToIndexMap;
    
    /*
     * For each Document, map from each attribute name to prediction
     */
    protected List<Map<String, PredictionCertaintyTuple>> predictionMapList;
    
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
    
    public List<Attribute> getAttributes() {
        return attributeList;
    }
    
    public List<String> getAttributeNames() {
        if (attributeList == null || attributeList.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> attrNames = new ArrayList<>();
        for (Attribute attribute : attributeList) {
            attrNames.add(attribute.getName());
        }
        return attrNames;
        
    }
    
    public List<Boolean> getAttributeEnabledList() {
        return attributeEnabledList;
    }

    public List<String> getValuesForAttribute(String xAxisAttrName) {
        if (attrNameToIndexMap != null && attrNameToIndexMap.containsKey(xAxisAttrName)) {
            int attrId = attrNameToIndexMap.get(xAxisAttrName);
            return attributeList.get(attrId).getLegalValues();
        }
        return new ArrayList<>();
    }
    
    
    
    /**
     * Loads and/or generates appropriate attribute predictions for the model
     * data. Namely, this method is responsible for appropriately populating
     * predictionMapList.
     */
    public abstract void loadPredictions(MainModel model);
    
    public abstract Map<String, Double> getTermWeightsForDocForAttr(int globalDocId, int globalAttrId);
    
    public abstract Map<String, Double> getTermWeightsForDocForAttr(int globalDocId, String globalAttrName);
    
    public abstract List<String> getAttributeValues(String attrName);
    
    
    /******* text highlighting methods *******/
    
    /**
     * Given a document ID and attribute ID, determines whether the predictor currently supports building of a highlighted version of the document.
     * 
     * @param globalDocId global int identifier for Document; serves as index into documentList
     * @param globalAttrId global int identifier for attribute; serves as index into attributeList
     * @return true if predictor possesses necessary functionality to build highlighted version of document text, false otherwise
     */
//    public boolean canWriteDocTextWithHighlights(int globalDocId, int globalAttrId) {
//        // if we have a predictor for the doc, return true else false
//        String selectedAttr = attributeList.get(globalAttrId).getName();
//        // translate old raw name from backend (ie, Indicator_25) into its more-useful extended form
//        // TODO : this is colonoscopy-specific; ideally, we should move the entire translation proceedure into the model-specific code, to isolate the front-end from this!
//        selectedAttr = DatasetTermTranslator.getRevAttrTranslation(selectedAttr);
////        Map<String, Boolean> abnormalNameMap = DatasetVarsTableModel.getAbnormalNameMap();
//        Map<String, Boolean> predictionNameMap = RuntimeIndicatorPrediction.getPredictionNameMap();
////        if (!abnormalNameMap.containsKey(selectedAttr) && predictionNameMap.containsKey(selectedAttr)) {
//        if (predictionNameMap.containsKey(selectedAttr)) {
//            return true;
//        }
//        return false;
//    }
    
    /**
     * Given a document ID and attribute ID, builds a highlighted version of
     * that document's text. This highlighted text is written into abstDoc. TODO
     * : move this out of MLPredictor?
     *
     * @param abstDoc AbstractDocument whose content is to be replaced with that
     * of the highlighted document text
     * @param globalDocId global int identifier for Document; serves as index
     * into documentList
     * @param globalAttrId global int identifier for attribute; serves as index
     * into attributeList
     * @see
     * emr_vis_nlp.ml.deprecated.DeprecatedMLPredictor.writeDocTextWithHighlights(AbstractDocument
     * abstDoc, int globalDocId, int globalAttrId)
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

        String docText = MainController.getMainController().getDocument(globalDocId).getText();
        int minFontSize = 12;
        int maxFontSize = 32;

        Map<String, Double> termWeightMap = getTermWeightsForDocForAttr(globalDocId, globalAttrId);

        // find max, min (abs?) vals
        double maxWeight = Double.MIN_VALUE;
        double minWeight = 0;

        // TODO make sure it's an indicator for which we're doing prediction! else we get an exception here
        // translate selected attr into its deprecated form
//        String selectedAttr = attributeList.get(globalAttrId).getName();

//        if (predictionNameMap.containsKey(selectedAttr)) {
        for (Double weight : termWeightMap.values()) {

            if (weight > maxWeight) {
                maxWeight = weight;
            }

            if (Math.abs(weight) < minWeight) {
                minWeight = Math.abs(weight);
            }

        }

        // debug
//        System.out.println("debug: max and min weights for attr " + globalAttrId + ": " + maxWeight + ", " + minWeight);

        // perform highlighting for terms above ~x% of range bewteen min and max?
        // for now, use all nonzero weights? (since the vast majority of terms in the current model near zero weight)
        // TODO : decide on highlight threshold in more principled manner!
//        double highlightCutoff = (maxWeight / 20.0) + minWeight;
        double highlightCutoff = 0;

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
                // check both raw and lower-case version of term
                if ((termWeightMap.containsKey(term) && (weight = termWeightMap.get(term)) > highlightCutoff) || (termWeightMap.containsKey(term.toLowerCase()) && (weight = termWeightMap.get(term.toLowerCase())) > highlightCutoff)) {

                    // if term is weighted sufficiently for the indicator
                    try {
                        int fontSize = (int) (minFontSize + (maxFontSize - minFontSize) * (weight / maxWeight));
                        SimpleAttributeSet attrSet = new SimpleAttributeSet();
                        StyleConstants.setFontSize(attrSet, fontSize);
                        int minBackgroundAlpha = 50;
                        int maxBackgroundAlpha = 255;
                        StyleConstants.setBackground(attrSet, new Color(0,
                                255, 255,
                                (int) (minBackgroundAlpha + (maxBackgroundAlpha - minBackgroundAlpha) * (weight / maxWeight))));
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
    
    
    
    /******* summary-related methods *******/
    
    public List<WeightedSentence> buildSummaryLines(int globalDocId, int globalAttrId) {
        
        // find important sentences in document with respect to containing highly-weighted features
        
        Map<String, Double> termWeightMap = getTermWeightsForDocForAttr(globalDocId, globalAttrId);
        
        List<WeightedSentence> wSents = new ArrayList<>();
        
        // TODO : parse document into sentences (for now, just use the natural newlines in the text)
        String docText = MainController.getMainController().getDocument(globalDocId).getText();
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
    
    //// static methods for loading in predictors from file
    public static enum MLPredictorType {
        mlpredictor_colonoscopy_deprecated,
        mlpredictor_colonoscopy_vars
    }
    
    public static MLPredictor buildPredictorFromXMLModelList(File predictorFile) {
        
        MLPredictor predictor = null;
        
        String modellistRootPath = predictorFile.getParent() +"/";
        // debug
        System.out.println("debug: modellist directory is \""+modellistRootPath+"\"");
        
        // load modellist file for reading
        org.w3c.dom.Document dom = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(predictorFile);
        } catch (ParserConfigurationException | SAXException | IOException pce) {
            pce.printStackTrace();
        }

        // document root
        Element modellistRoot = dom.getDocumentElement();
        String modellistTypeName = modellistRoot.getAttribute("type").trim().toLowerCase();
        if (modellistTypeName.equals(MLPredictorType.mlpredictor_colonoscopy_deprecated.toString())) {
            // type is deprecated colonosopy from summer 2012
            // debug
            System.out.println("debug: modellist type is \"" + MLPredictorType.mlpredictor_colonoscopy_deprecated + "\"");
            // build the appropriate MLPredictor
            throw new UnsupportedOperationException();  // todo : do we care about the old predictor enough to implement this?
            
        } else if (modellistTypeName.equals(MLPredictorType.mlpredictor_colonoscopy_vars.toString())) {
            // type is variable-based, from spring 2013
            // debug
            System.out.println("debug: modellist type is \"" + MLPredictorType.mlpredictor_colonoscopy_vars + "\"");
            // build the appropriate MLPredictor
            String modellistName = modellistRoot.getAttribute("name").trim().toLowerCase();
            String modelRootPath = modellistRoot.getAttribute("modelroot").trim().toLowerCase();
            // if modelRootPath is relative, tack on location of the modellist xml
            if (modelRootPath.length() < 2 || modelRootPath.charAt(0) == '.' || (modelRootPath.charAt(0) != '/' && modelRootPath.charAt(1) != ':')) {
                String parentDir = predictorFile.getParent();
                modelRootPath = parentDir + File.separator + modelRootPath;
            }
            
            String modelFoldName = modellistRoot.getAttribute("foldname").trim().toLowerCase();
            NodeList attributeNodes = modellistRoot.getElementsByTagName("Attr");
            List<Attribute> attributeList = new ArrayList<>();
            List<String> modelNames = new ArrayList<>();
            if (attributeNodes != null && attributeNodes.getLength() > 0) {
                for (int n=0; n<attributeNodes.getLength(); n++) {
                    Element attributeNode = (Element) attributeNodes.item(n);
                    NodeList attributeValNodes = attributeNode.getElementsByTagName("Vals");
                    Element attributeValNode = (Element) attributeValNodes.item(0);
                    String valString = attributeValNode.getFirstChild().getNodeValue().trim();
                    Scanner valSplitter = new Scanner(valString);
                    List<String> valList = new ArrayList<>();
                    while (valSplitter.hasNext()) {
                        String nextVal = valSplitter.next().trim();
                        if (!nextVal.isEmpty()) {
                            valList.add(nextVal);
                        }
                    }
                    String attrName = attributeNode.getAttribute("name").trim().toLowerCase();
                    String attrTypeStr = attributeNode.getAttribute("type").trim().toLowerCase();
                    Attribute.AttributeType attrType = Attribute.AttributeType.getType(attrTypeStr);
                    
                    // build attribute
                    Attribute newAttr = new Attribute(attrType, attrName, attrName, valList);
                    // debug
                    System.out.println("debug: MLPredictor: built new attribute: "+newAttr.toString());
                    attributeList.add(newAttr);
                    modelNames.add(attrName);
                    // build model
                    
                    
                }
            }
            predictor = new MLPredictorColonVars(modellistName, modelRootPath, modelFoldName, modelNames, attributeList);
            
        } else {
            // type is not recognized
            System.err.println("modellist type \"" + modellistTypeName + "\" not recognized");

        }
        
        return predictor;
    }
    
}
