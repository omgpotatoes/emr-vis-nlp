package emr_vis_nlp.model;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.ml.MLPredictor;
import emr_vis_nlp.ml.deprecated.RuntimeIndicatorPrediction;
import emr_vis_nlp.ml.deprecated.SimpleSQMatcher;
import emr_vis_nlp.model.mpqa_colon.Dataset;
import emr_vis_nlp.model.mpqa_colon.DatasetTermTranslator;
import java.awt.Color;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * MainModel designed to represent the MPQA-style dataset (as referenced by XML doclist).
 *
 * @author alexander.p.conrad@gmail.com
 */
public class MpqaColonMainModel implements MainModel {

    /**
     * legacy code representing the active dataset; just use this to load the
     * documents
     */
    private Dataset dataset;
    /**
     * list of all documents in dataset
     */
    private List<Document> documentList;
//    /*
//     * list of all predictions for each document
//     */
//    private List<double[][]> predictionList;
//    /*
//     * list of all predicted categories for each doc
//     */
//    private List<String[]> predictionCatList;
//    /*
//     * list of all predicted certainties for each doc
//     */
//    private List<double[]> predictionCertList;
    /*
     * maps from attributes to predictions for each doc
     * (moved to MLPredictor
     */
//    private List<Map<String, PredictionCertaintyTuple>> predictionMapList;
    /**
     * list of indicators as to which documents are currently enabled
     */
    private List<Boolean> documentEnabledList;
    /**
     * list of all attributes in dataset
     */
    private List<String> attributeList;
    /**
     * list of indicators as to which documents are currently enabled
     */
    private List<Boolean> attributeEnabledList;
    /**
     * this MainModel's governing MainController
     */
    private MainController controller;
    /**
     * MLPredictor associated with predictions for this dataset
     */
    private MLPredictor mlPredictor;
    private Map<String, Integer> predictedAttrNameToIndexMap;
    
    /**
     * Attributes to be passed along to the front-end
     */
    private static List<String> focusAttrs = null;
    /**
     * Map of focus attrs
     */
    private static Map<String, Boolean> focusAttrsMap = null;
    
    public MpqaColonMainModel(MainController controller) {

        this.controller = controller;
        dataset = null;
        documentList = new ArrayList<>();
        documentEnabledList = new ArrayList<>();
        attributeList = new ArrayList<>();
//        predictionList = new ArrayList<>();
//        predictionCatList = new ArrayList<>();
//        predictionCertList = new ArrayList<>();
//        predictionMapList = new ArrayList<>();
        
        // for now, just load the deprecatedPredictor
        // TODO proper loading of different model types!
        RuntimeIndicatorPrediction.initRuntimeIndicatorPredictor();

    }

//    @Override
    public void loadDataFromDoclist(String doclistPath) {
        loadDataFromDoclist(new File(doclistPath));
    }

//    @Override
    public void loadDataFromDoclist(File doclist) {
        dataset = Dataset.loadDatasetFromDoclist(doclist);
        documentList = dataset.getDocuments();
        // by default, enable docs
        enableAllDocuments();
        // get attributes from dataset
        attributeList = dataset.getAllAttributesFromDocs();
        // by default, enable attributes
        enableAllAttributes();
        // build list of selected attrs
        getFocusAttrs();
        // build list of predictions for all docs
//        predictionList = new ArrayList<>();
//        predictionCatList = new ArrayList<>();
//        predictionCertList = new ArrayList<>();
//        predictionMapList = new ArrayList<>();
        List<String> defaultValList = DatasetTermTranslator.getDefaultValList();
        
        // prediction code moved to MLPredictor
//        for (int d=0; d<documentList.size(); d++) {
//            Map<String, PredictionCertaintyTuple> predictionMap = new HashMap<>();
//            RuntimeIndicatorPrediction.buildTemporaryFileForText(documentList.get(d).getText());
//            
//            // d1 = indicator; d2 = probs. for each val [-1, 0, 1]
//            double[][] predictions = RuntimeIndicatorPrediction.predictIndicatorsForTempFile();
////            int[] predictedIndicatorScores = new int[predictions.length];
//            String[] predictedIndicatorScores = new String[predictions.length];
//            double[] predictedIndicatorCerts = new double[predictions.length];
//            // find largest value for each indicator; store value and certainty
//            for (int p = 0; p < predictions.length; p++) {
//                String attributeNamePretty = DatasetTermTranslator.getAttrTranslation(RuntimeIndicatorPrediction.predictedIndicatorNames[p]);
//                double negOneVal = predictions[p][0];
//                double zeroVal = predictions[p][1];
//                double oneVal = predictions[p][2];
//                PredictionCertaintyTuple predCertTuple = null;
//                if (negOneVal >= zeroVal && negOneVal >= oneVal) {
////                    predictedIndicatorScores[p] = -1;
//                    predictedIndicatorScores[p] = defaultValList.get(0);
//                    predictedIndicatorCerts[p] = negOneVal;
//                    predCertTuple = new PredictionCertaintyTuple(attributeNamePretty, defaultValList.get(0), negOneVal);
//                } else if (zeroVal >= negOneVal && zeroVal >= oneVal) {
//                    predictedIndicatorScores[p] = defaultValList.get(1);
////                    predictedIndicatorScores[p] = 0;
//                    predictedIndicatorCerts[p] = zeroVal;
//                    predCertTuple = new PredictionCertaintyTuple(attributeNamePretty, defaultValList.get(1), zeroVal);
//                } else {
//                    predictedIndicatorScores[p] = defaultValList.get(2);
////                    predictedIndicatorScores[p] = 1;
//                    predictedIndicatorCerts[p] = oneVal;
//                    predCertTuple = new PredictionCertaintyTuple(attributeNamePretty, defaultValList.get(2), oneVal);
//                }
//                predictionMap.put(attributeNamePretty, predCertTuple);
//            }
////            predictionCatList.add(predictedIndicatorScores);
////            predictionCertList.add(predictedIndicatorCerts);
////            predictionList.add(predictions);
//            predictionMapList.add(predictionMap);
//        }
    }

//    @Override
//    public void applySimpleStringFilter(String str) {
//        // iterate through documents; if doc contains string, enable; otherwise disable
//        for (int d = 0; d < documentList.size(); d++) {
//            Document doc = documentList.get(d);
//            String docText = doc.getText();
//            // TODO more robust string searching
//            boolean enable = false;
//            if (docText.contains(str)) {
//                enable = true;
//            }
//            boolean oldEnabledStatus = documentEnabledList.remove(d);
//            documentEnabledList.add(d, enable);
//        }
//    }

//    @Override
//    public DocTableModel buildSimpleDocTableModel() {
//        DocTableModel docTableModel = new DocTableModel(documentList, documentEnabledList, attributeList, attributeEnabledList);
//        return docTableModel;
//    }
//    
//    @Override
//    public AttrTableModel buildSimpleAttrSelectionTableModel() {
//        AttrTableModel attrTableModel = new AttrTableModel(attributeList, attributeEnabledList, controller);
//        return attrTableModel;
//    }
//    
//    @Override
//    public AttrTableModel buildSimpleAttrSelectionTableModelFocusOnly() {
//        throw new UnsupportedOperationException("Not supported yet.");
////        AttrTableModel attrTableModel = new AttrTableModel(getFocusAttrs(), attributeEnabledList, controller);
////        return attrTableModel;
//    }
    
    @Override
    public void setSelectedAttributes(List<Boolean> selectedAttributes) {
        // copy values from old list to new list
        // ensure same length
        if (attributeEnabledList.size() != selectedAttributes.size()) {
            throw new InputMismatchException("lengths not equal: "+attributeEnabledList.size()+" vs "+selectedAttributes.size());
        }
        
        attributeEnabledList = new ArrayList<>();
        for (int a=0; a<selectedAttributes.size(); a++) {
            attributeEnabledList.add(selectedAttributes.get(a));
        }
        
    }

    public void enableAllDocuments() {
        documentEnabledList = new ArrayList<>();
        for (Document doc : documentList) {
            documentEnabledList.add(true);
        }
    }

    
    public void enableAllAttributes() {
        // enable only the focus attrs
        enableAllAttributes(true);
    }
    
    public void enableAllAttributes(boolean focusOnly) {
        
        if (!focusOnly) {
            attributeEnabledList = new ArrayList<>();
            for (String attribute : attributeList) {
                attributeEnabledList.add(true);
            }
        } else {
            attributeEnabledList = new ArrayList<>();
//            getFocusAttrsMap();
//            for (String attribute : attributeList) {
//                if (focusAttrsMap.containsKey(attribute)) {
//                    attributeEnabledList.add(true);
//                } else {
//                    attributeEnabledList.add(false);
//                }
//            }
            
            // by default, enable "text", "name", and any attribute starting with a ##
            for (String attribute : attributeList) {
                if (attribute.equalsIgnoreCase("name") || attribute.equalsIgnoreCase("text") || (attribute.trim().length() > 0 && attribute.trim().charAt(0) >= '0' && attribute.trim().charAt(0) <= '9')) {
                    attributeEnabledList.add(true);
                } else {
                    attributeEnabledList.add(false);
                }
            }
            
        }
        
    }
    

    @Override
    public List<Document> getAllDocuments() {
        return documentList;
    }

    @Override
    public List<Boolean> getAllSelectedDocuments() {
        return documentEnabledList;
    }
    
    @Override
    public List<String> getAllAttributes() {
        return attributeList;
    }

    @Override
    public List<Boolean> getAllSelectedAttributes() {
        return attributeEnabledList;
    }
    
    
    
    public static List<String> getFocusAttrs() {
    	
        if (focusAttrs != null) {
            return focusAttrs;
        }
        
    	focusAttrs = new ArrayList<>();
    	
        focusAttrs.add("name");
        
    	focusAttrs.add("Indicator_19");
    	focusAttrs.add("Indicator_16");
    	focusAttrs.add("Indicator_2");
    	focusAttrs.add("Indicator_17");
    	focusAttrs.add("Indicator_3.1");
    	focusAttrs.add("Indicator_11");
    	focusAttrs.add("Indicator_21");
    	focusAttrs.add("VAR_Withdraw_time");
    	focusAttrs.add("VAR_Procedure_aborted");
    	focusAttrs.add("VAR_ASA");
    	focusAttrs.add("VAR_Prep_adequate");
    	focusAttrs.add("VAR_Indication_type");
    	focusAttrs.add("VAR_Nursing_Reports");
    	focusAttrs.add("VAR_Informed_consent");
    	focusAttrs.add("VAR_Cecum_(reached_it)");
    	focusAttrs.add("VAR_Indication_Type_3");
    	focusAttrs.add("VAR_Indication_Type_2");
    	focusAttrs.add("VAR_Any_adenoma");
    	focusAttrs.add("VAR_cecal_landmark");
    	focusAttrs.add("VAR_Biopsy");
        
        focusAttrs.add("text");
    	
    	return focusAttrs;
    	
    }
    
    @Override
    public Map<String, Integer> getAttributeValueCountMap(String attrName) {
        // first, see if attribute is in dataset
        boolean attrInDataset = false;
        for (String attr : attributeList) {
            if (attr.equalsIgnoreCase(attrName)) {
                attrInDataset = true; break;
            }
        }
        if (!attrInDataset) {
            System.err.print("MpqaColonMainModel.getAttributeValues: attr "+attrName+" not in dataset");
            return new HashMap<>();
        }
        
        // iterate over all docs, finding all possible vals
        Map<String, Integer> valCountMap = new HashMap<>();
//        List<String> vals = new ArrayList<>();
        for (Document doc : documentList) {
            // get val from doc
            String val = "";
            Map<String, String> attrMap = doc.getAttributes();
            if (attrMap.containsKey(attrName)) {
                val = attrMap.get(attrName);
            }
            // insert / increment val
            if (!valCountMap.containsKey(val)) {
                valCountMap.put(val, 1);
//                vals.add(val);
            } else {
                valCountMap.put(val, valCountMap.get(val)+1);
            }
        }
        return valCountMap;
        
    }
    
//    @Override
//    public TableModel buildSimpleTreeMapSelectionTableModel() {
//        TreeMapSelectorTableModel treeMapTableModel = new TreeMapSelectorTableModel(attributeList, controller);
//        return treeMapTableModel;
//    }
//    
//    @Override
//    public TableModel buildSimpleDocGridSelectionTableModel() {
//        TableModel docGridTableModel = new DocGridTableSelectorModel(attributeList, controller);
//        return docGridTableModel;
//    }
    
    public static Map<String, Boolean> getFocusAttrsMap() {
        
        if (focusAttrsMap != null) {
            return focusAttrsMap;
        }
        
        focusAttrsMap = new HashMap<>();
        List<String> focusAttrsList = getFocusAttrs();
        
        for (String focusAttr : focusAttrsList) {
            focusAttrsMap.put(focusAttr, true);
        }
        
        return focusAttrsMap;
        
    }
    
    @Override
    public boolean updateDocumentAttr(int docID, String docAttr, String docAttrVal) {
        
        try {
            documentList.get(docID).getAttributes().put(docAttr, docAttrVal);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("MpqaColonMainModel: err: no document /w idNum="+docID);
            return false;
        }
        return true;
        
    }
    
//    @Override
//    public Map<String, PredictionCertaintyTuple> getPredictionsForDoc(int globalDocId) {
//        return predictionMapList.get(globalDocId);
//    }
//
//    @Override
//    public boolean hasPrediction(int globalDocId, String attrName) {
//        Map<String, PredictionCertaintyTuple> docPredMap = predictionMapList.get(globalDocId);
//        if (docPredMap.containsKey(attrName)) {
//            return true;
//        }
//        return false;
//    }

    @Override
    public boolean hasManAnn(int globalDocId, String attrName) {
        if (documentList.get(globalDocId).getAttributes().containsKey(attrName)) {
            return true;
        }
        return false;
    }

//    @Override
//    public PredictionCertaintyTuple getPrediction(int globalDocId, String attrName) {
//        Map<String, PredictionCertaintyTuple> docPredMap = predictionMapList.get(globalDocId);
//        if (docPredMap.containsKey(attrName)) {
//            return docPredMap.get(attrName);
//        }
//        return null;
//    }

    @Override
    public String getManAnn(int globalDocId, String attrName) {
        String val = "";
        if (documentList.get(globalDocId).getAttributes().containsKey(attrName)) {
            val = documentList.get(globalDocId).getAttributes().get(attrName);
        }
        return val;
    }

//    @Override
//    public boolean canWriteDocTextWithHighlights(int globalDocId, int globalAttrId) {
//        
//        // if we have a predictor for the doc, return true else false
//        String selectedAttr = attributeList.get(globalAttrId);
//        selectedAttr = DatasetTermTranslator.getRevAttrTranslation(selectedAttr);
////        Map<String, Boolean> abnormalNameMap = DatasetVarsTableModel.getAbnormalNameMap();
//        Map<String, Boolean> predictionNameMap = RuntimeIndicatorPrediction.getPredictionNameMap();
////        if (!abnormalNameMap.containsKey(selectedAttr) && predictionNameMap.containsKey(selectedAttr)) {
//        if (predictionNameMap.containsKey(selectedAttr)) {
//            return true;
//        }
//        return false;
//        
//    }

    // TODO move into MLPredictor? since it depends on feat. weights?
//    @Override
//    public void writeDocTextWithHighlights(AbstractDocument abstDoc, int globalDocId, int globalAttrId) {
//        
//        // if we have a predictor for the doc, build else just populate with plaintext
//        // if model doesn't support highlighting for this text, just insert plaintext
//        
//        // clear doc
//        try {
//            abstDoc.remove(0, abstDoc.getLength());
//        } catch (BadLocationException e) {
//            e.printStackTrace();
//            System.out.println("err: could not reset doc text for doc " + globalDocId + " focus window");
//        }
//
//        String docText = documentList.get(globalDocId).getText();
//        int minFontSize = 12;
//        int maxFontSize = 32;
//        
//        
//            // from annotator.MainWindow: (code to adapt:)
//            
//        RuntimeIndicatorPrediction.buildTemporaryFileForText(documentList.get(globalDocId).getText());
//        RuntimeIndicatorPrediction.predictIndicatorsForTempFile();
//
//        // get top-ranked terms for selected indicator
//        Map<String, Double> termWeightMap = RuntimeIndicatorPrediction.getTermWeightsForIndicator(attributeList.get(globalAttrId));
//
//        // find max, min (abs?) vals
//        double maxWeight = Double.MIN_VALUE;
//        double minWeight = 0;
//
//        // TODO make sure it's an indicator for which we're doing prediction! else we get an exception here
//        // translate selected attr into its deprecated form
//        String selectedAttr = attributeList.get(globalAttrId);
//        selectedAttr = DatasetTermTranslator.getRevAttrTranslation(selectedAttr);
////        Map<String, Boolean> abnormalNameMap = DatasetVarsTableModel.getAbnormalNameMap();
//        Map<String, Boolean> predictionNameMap = RuntimeIndicatorPrediction.getPredictionNameMap();
//        boolean isSimpleSQVar = false;
//        String simpleSQVarRegExpStr = "";
////        if (!abnormalNameMap.containsKey(selectedAttr) && predictionNameMap.containsKey(selectedAttr)) {
//        if (predictionNameMap.containsKey(selectedAttr)) {
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
//            // if feature is a regexp
//            if (isSimpleSQVar) {
//                // highlight regexp
//                Pattern varRegExpPattern = Pattern.compile(simpleSQVarRegExpStr);
//                Matcher varRegExpMatcher = varRegExpPattern.matcher(docText);
//                boolean hasPattern = varRegExpMatcher.find();
//
//
//                // strategy: while hasPattern == true, continue to look for matches; 
//                //  store start, end match indices in a list
//                List<Integer> startIndices = new ArrayList<>();
//                List<Integer> endIndices = new ArrayList<>();
//
//                while (hasPattern) {
//
//                    int start = varRegExpMatcher.start();
//                    int end = varRegExpMatcher.end();
//                    String matchedSubstring = varRegExpMatcher.group();
//
//                    // debug
//                    System.out.println("debug: found match in doc " + documentList.get(globalDocId).getName() + " for attr " + selectedAttr + ": \"" + matchedSubstring + "\"");
//
//                    startIndices.add(start);
//                    endIndices.add(end);
//
//                    hasPattern = varRegExpMatcher.find();
//                }
//
//                int lastEndIndex = 0;
//                if (startIndices.size() > 0) {
//
//                    while (startIndices.size() > 0) {
//
//                        // iterate through indices, writing the previous unmatched
//                        // portion and following matched portion
//
//                        int plainIndexStart = lastEndIndex;
//                        int plainIndexEnd = startIndices.remove(0);
//                        int matchedIndexEnd = endIndices.remove(0);
//
//                        // unmatched
//                        try {
//                            int fontSize = minFontSize;
//                            SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                            StyleConstants.setFontSize(attrSet, fontSize);
//                            abstDoc.insertString(abstDoc.getLength(), docText.substring(plainIndexStart, plainIndexEnd),
//                                    attrSet);
//                        } catch (BadLocationException e) {
//                            e.printStackTrace();
//                            System.out.println("err: could not add unweighted term to report panel: "
//                                    + docText.substring(plainIndexStart,
//                                    plainIndexEnd));
//                        }
//
//                        // matched
//                        try {
////						double weight = 1.0;
//                            double weight = 0.8;
//                            int fontSize = (int) (maxFontSize * weight);
//                            if (fontSize < minFontSize) {
//                                fontSize = minFontSize;
//                            }
//                            SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                            StyleConstants.setFontSize(attrSet, fontSize);
//                            StyleConstants.setBackground(attrSet, new Color(0, 255,
//                                    255, (int) (255 * weight)));
//                            abstDoc.insertString(abstDoc.getLength(), docText.substring(plainIndexEnd, matchedIndexEnd),
//                                    attrSet);
//                        } catch (BadLocationException e) {
//                            e.printStackTrace();
//                            System.out.println("err: could not add weighted term to report panel: "
//                                    + docText.substring(plainIndexEnd,
//                                    matchedIndexEnd));
//                        }
//
//                        lastEndIndex = matchedIndexEnd;
//
//                    }
//
//                    // print the last bit of unmatched text, if present (should be)
//                    try {
//                        int fontSize = minFontSize;
//                        SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                        StyleConstants.setFontSize(attrSet, fontSize);
//                        abstDoc.insertString(abstDoc.getLength(), docText.substring(lastEndIndex),
//                                attrSet);
//                    } catch (BadLocationException e) {
//                        e.printStackTrace();
//                        System.out.println("err: could not add unweighted term to report panel: "
//                                + docText.substring(lastEndIndex));
//                    }
//
//                } else {
//                    // regexp doesn't match, so just load plain doc
//                    try {
//                        int fontSize = minFontSize;
//                        SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                        StyleConstants.setFontSize(attrSet, fontSize);
//                        abstDoc.insertString(abstDoc.getLength(), docText, attrSet);
//                    } catch (BadLocationException e) {
//                        e.printStackTrace();
//                        System.out.println("err: could not load plain doc in panel (regexp not matched)");
//                    }
//                }
//            } else {
//                // highlight sufficiently-weighted terms, if any
//                Scanner docTextLineSplitter = new Scanner(docText);
//                while (docTextLineSplitter.hasNextLine()) {
//                    String line = docTextLineSplitter.nextLine();
//                    Scanner lineSplitter = new Scanner(line);
//                    while (lineSplitter.hasNext()) {
//
//                        String term = lineSplitter.next();
//
//                        // if term is highly weighted, draw with emphasis;
//                        // otherwise, draw normally
//                        double weight = 0.;
//                        double weightDiffMult = 1.3; // the larger this is, the
//                        // higher the threshold for
//                        // highlighting
////                        if (!abnormalNameMap.containsKey(selectedAttr) && 
//                          if ( predictionNameMap.containsKey(selectedAttr)
//                                && ((termWeightMap.containsKey(term) && (weight = termWeightMap.get(term)) > (maxWeight - ((maxWeight - minWeight) / weightDiffMult))) || (termWeightMap.containsKey(term.toLowerCase()) && (weight = termWeightMap.get(term.toLowerCase())) > (maxWeight - ((maxWeight - minWeight) / weightDiffMult))))) {
//
//                            // if term is weighted sufficiently for the indicator
//                            try {
//                                int fontSize = (int) (maxFontSize * (weight / maxWeight));
//                                if (fontSize < minFontSize) {
//                                    fontSize = minFontSize;
//                                }
//                                SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                                StyleConstants.setFontSize(attrSet, fontSize);
//                                StyleConstants.setBackground(attrSet, new Color(0,
//                                        255, 255,
//                                        (int) (255 * (weight / maxWeight))));
//                                abstDoc.insertString(abstDoc.getLength(), term
//                                        + " ", attrSet);
//                            } catch (BadLocationException e) {
//                                e.printStackTrace();
//                                System.out.println("err: could not add weighted term to report panel: "
//                                        + term);
//                            }
//
//                        } else {
//
//                            // term is not weighted sufficiently for indicator
//                            try {
//                                int fontSize = minFontSize;
//                                SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                                StyleConstants.setFontSize(attrSet, fontSize);
//                                abstDoc.insertString(abstDoc.getLength(), term
//                                        + " ", attrSet);
//                            } catch (BadLocationException e) {
//                                e.printStackTrace();
//                                System.out.println("err: could not add unweighted term to report panel: "
//                                        + term);
//                            }
//
//                        }
//
//                    }
//
//                    // add a newline at the end of the line
//                    try {
//                        int fontSize = minFontSize;
//                        SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                        StyleConstants.setFontSize(attrSet, fontSize);
//                        abstDoc.insertString(abstDoc.getLength(), "\n", attrSet);
//                    } catch (BadLocationException e) {
//                        e.printStackTrace();
//                        System.out.println("err: could not add newline to report panel");
//                    }
//
//                }
//            }
//        
//        
//    }
    
}
