
package emr_vis_nlp.ml.colon_vars;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.ml.Attribute;
import emr_vis_nlp.ml.MLPredictor;
import emr_vis_nlp.ml.PredictionCertaintyTuple;
import emr_vis_nlp.model.Document;
import emr_vis_nlp.model.MainModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * MLPredictor oriented around predicting variables for the colonoscopy data. 
 * Parts of code based on Phuong's ``ColonoscopyDemo.java'' class from summer 2012.
 *
 * @author alexander.p.conrad@gmail.com, pnvphuong@gmail.com
 */
public class MLPredictorColonVars extends MLPredictor {
    
    // temporary file to use for communicating with WEKA predictor
    public static final String TEMP_ARFF_FILE = "resources/backend/temp.arff";
    
    private String modellistName;
    private String modelRootPath;
    private String modelFoldName;
    private List<String> modelNames;
    
    private List<CertSVMPredictor> predictors;
    
    private List<Map<String, Double>> predictorsTermWeightMaps;
    
//    private Map<String, Map<String, Double>> memoizedTermWeightMaps;
    
    public MLPredictorColonVars(String modellistName, String modelRootPath, String modelFoldName, List<String> modelNames, List<Attribute> attributeList) {
        this.modellistName = modellistName;
        this.modelRootPath = modelRootPath;
        this.modelFoldName = modelFoldName;
        this.modelNames = modelNames;
        this.attributeList = attributeList;
        
        // load predictors
        predictors = new ArrayList<>();
        attrNameToIndexMap = new HashMap<>();
        attributeEnabledList = new ArrayList<>();
        for (int i = 0; i < modelNames.size(); i++) {
            String modelPath;
            attrNameToIndexMap.put(modelNames.get(i), i);
            if (modelRootPath.length() > 0) {
                modelPath = modelRootPath + File.separator + "models" + File.separator + modelNames.get(i) + "-" + modelFoldName + "-model.model";
            } else {
                modelPath = "models" + File.separator + modelNames.get(i) + "-" + modelFoldName + "-model.model";
            }
            // debug
            System.out.println("debug: "+this.getClass().getName()+": loading model file "+modelPath);
            CertSVMPredictor predictor = new CertSVMPredictor();
            predictor.loadModel(modelPath);
            predictors.add(predictor);
            attributeEnabledList.add(true);
        }
        
        // load feature weights from file for all terms for each predictor
        predictorsTermWeightMaps = new ArrayList<>();
        for (int i = 0; i < modelNames.size(); i++) {
            String termWeightPath;
            if (modelRootPath.length() > 0) {
                termWeightPath = modelRootPath + File.separator + "weights" + File.separator + modelNames.get(i) + "-" + modelFoldName + "-featureWeight-trainSet.csv";
            } else {
                termWeightPath = "weights" + File.separator + modelNames.get(i) + "-" + modelFoldName + "-featureWeight-trainSet.csv";
            }
            // debug
            System.out.println("debug: "+this.getClass().getName()+": loading term weight file "+termWeightPath);
            Map<String, Double> predictorTermWeightMap = new HashMap<>();
            int predictorTermWeightMapCounter = 0;
            // read file; should have two lines
            File termWeightFile = null;
            Scanner termWeightFileReader = null;
            try {
                termWeightFile = new File(termWeightPath);
                termWeightFileReader = new Scanner(termWeightFile);
                String line1 = termWeightFileReader.nextLine();
                String line2 = termWeightFileReader.nextLine();
                Scanner line1Splitter = new Scanner(line1);
                line1Splitter.useDelimiter(",");
                Scanner line2Splitter = new Scanner(line2);
                line2Splitter.useDelimiter(",");
                while (line1Splitter.hasNext()) {
                    String newTerm = line1Splitter.next();
                    double newWeight = line2Splitter.nextDouble();
                    predictorTermWeightMap.put(newTerm, newWeight);
                    predictorTermWeightMapCounter++;
                }
            } catch (FileNotFoundException e) {                
                e.printStackTrace();
                System.out.println("err: "+this.getClass().getName()+": termWeight file not found: "+termWeightPath);
            } catch (NoSuchElementException e) {
                e.printStackTrace();
                System.out.println("err: "+this.getClass().getName()+": termWeight file improperly formatted: "+termWeightPath);
            } finally {
                if (termWeightFileReader != null) {
                    termWeightFileReader.close();
                }
            }
            // debug
            System.out.println("debug: "+this.getClass().getName()+": read "+predictorTermWeightMapCounter+" terms from "+termWeightPath);
            predictorsTermWeightMaps.add(predictorTermWeightMap);
        }
        
    }
    
    public double[][] predictInstance(String fnInstance) {
        // assume binary classifiers
        double[][] predictions = new double[predictors.size()][2];

        // each of these models corresponds to one of the indicators
        for (int i = 0; i < predictors.size(); i++) {
            try {
                predictions[i] = predictors.get(i).predictInstanceDistribution(fnInstance);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("err: "+this.getClass().getName()+": unable to perform prediction on file "+fnInstance+" using model for attr "+modelNames.get(i));
            }
        }

        return predictions;
    }
    
    public double[] predictInstanceSingleAttr(String fnInstance, int p) {
        // assume binary classifiers
        double[] prediction = new double[2];

        // each of these models corresponds to one of the indicators
//        for (int i = 0; i < predictors.size(); i++) {
            try {
                prediction = predictors.get(p).predictInstanceDistribution(fnInstance);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("err: "+this.getClass().getName()+": unable to perform prediction on file "+fnInstance+" using model for attr "+modelNames.get(p));
            }
//        }

        return prediction;
    }

    public Map<String, Double> getTermWeightMapForModelIndex(int modelIndex) {

//        CertSVMPredictor predictor = predictors.get(modelIndex);
//        Map<String, Double> termWeights = predictor.getTermWeights();
//        return termWeights;
        return predictorsTermWeightMaps.get(modelIndex);

    }
    
    @Override
    public void loadPredictions(MainModel model) {
        
        // load predictions for all docs
        predictionMapList = new ArrayList<>();
        List<Document> documentList = model.getAllDocuments();
        for (int d = 0; d < documentList.size(); d++) {
            Map<String, PredictionCertaintyTuple> predictionMap = new HashMap<>();
            for (int p = 0; p < predictorsTermWeightMaps.size(); p++) {

                // build temporary file for this document
                //RuntimeIndicatorPrediction.buildTemporaryFileForText(documentList.get(d).getText());
                String documentText = documentList.get(d).getText();

                File tempFile = null;
                FileWriter tempFileWriter = null;
                StringBuilder tempFileBuilder = new StringBuilder();
                try {
                    tempFile = new File(TEMP_ARFF_FILE);
                    tempFile.delete();
                    tempFileWriter = new FileWriter(tempFile);

//                    String header = "% This is the Colonoscopy problem\n@relation current_working_report\n@attribute [ReportID] numeric\n";
                    String header = "% This is the Colonoscopy problem\n@relation current_working_report\n@attribute [report_identifier] numeric\n";
//                    String header = "% This is the Colonoscopy problem\n@relation current_working_report\n";
//                    tempFileWriter.write(header);
                    tempFileBuilder.append(header);

                    List<Integer> termVals = new ArrayList<>();
                    Map<String, Double> predictorTermWeightMap = predictorsTermWeightMaps.get(p);

                    for (String termName : predictorTermWeightMap.keySet()) {
                        String line = "@attribute \"" + termName + "\" {0, 1}\n";
//                        tempFileWriter.write(line);
                        tempFileBuilder.append(line);

                        if (documentText.contains(termName)) {
                            termVals.add(1);
                        } else {
                            termVals.add(0);
                        }

                    }

//                    String attrFooter = "@attribute \"[classLabel]\" {-1, 0, 1}\n@data\n'4'";
                    String attrFooter = "@attribute \"[classLabel]\" {0, 1}\n@data\n0";
//                    tempFileWriter.write(attrFooter);
                    tempFileBuilder.append(attrFooter);
                    boolean isFirst = true;
                    for (Integer termVal : termVals) {

                        String termValStr = "," + termVal;
//                        if (isFirst) {
//                            termValStr = "" + termVal;
//                            isFirst = false;
//                        }
//                        tempFileWriter.write(termValStr);
                        tempFileBuilder.append(termValStr);

                    }

//                    tempFileWriter.write(",0\n");
                    // append final value for classlabel; doesn't matter, since we're doing prediction on this document?
                    tempFileBuilder.append(",0\n");

                    tempFileWriter.write(tempFileBuilder.toString());
                    tempFileWriter.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("error writing temp file: " + TEMP_ARFF_FILE);
                } finally {
                    if (tempFileWriter != null) {
                        try {
                            tempFileWriter.close();
                        } catch (IOException e) {
                        }
                    }
                }
                
                // d1 = indicator; d2 = probs. for each val [-1, 0, 1]
//            double[][] predictions = RuntimeIndicatorPrediction.predictIndicatorsForTempFile();
//                double[][] predictions = predictInstance(TEMP_ARFF_FILE);
                double[] prediction = predictInstanceSingleAttr(TEMP_ARFF_FILE, p);
//            int[] predictedIndicatorScores = new int[predictions.length];
//                String[] predictedIndicatorScores = new String[predictions.length];
//                double[] predictedIndicatorCerts = new double[predictions.length];
                // find largest value for each indicator; store value and certainty
                //for (int p = 0; p < predictions.length; p++) {
//                String attributeNamePretty = DatasetTermTranslator.getAttrTranslation(RuntimeIndicatorPrediction.predictedIndicatorNames[p]);
//                double negOneVal = predictions[p][0];
//                double zeroVal = predictions[p][1];
//                double oneVal = predictions[p][2];
//                double zeroVal = predictions[p][0];
//                double oneVal = predictions[p][1];
                double zeroVal = prediction[0];
                double oneVal = prediction[1];
                String attributeName = attributeList.get(p).getName();
                PredictionCertaintyTuple predCertTuple = null;
                if (zeroVal >= oneVal) {
//                    predictedIndicatorScores[p] = attributeList.get(p).getLegalValues().get(0);
//                    predictedIndicatorCerts[p] = zeroVal;
                    predCertTuple = new PredictionCertaintyTuple(attributeName, attributeList.get(p).getLegalValues().get(0), zeroVal);
                } else {
//                    predictedIndicatorScores[p] = attributeList.get(p).getLegalValues().get(1);
//                    predictedIndicatorCerts[p] = oneVal;
                    predCertTuple = new PredictionCertaintyTuple(attributeName, attributeList.get(p).getLegalValues().get(1), oneVal);
                }

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

                predictionMap.put(attributeName, predCertTuple);
            }
//            predictionCatList.add(predictedIndicatorScores);
//            predictionCertList.add(predictedIndicatorCerts);
//            predictionList.add(predictions);
            predictionMapList.add(predictionMap);
        }
    }

    @Override
    public Map<String, Double> getTermWeightsForDocForAttr(int globalDocId, int globalAttrId) {
        Map<String, Double> termWeightMap = predictorsTermWeightMaps.get(globalDocId);
        return termWeightMap;
    }

    @Override
    public Map<String, Double> getTermWeightsForDocForAttr(int globalDocId, String globalAttrName) {
        return getTermWeightsForDocForAttr(globalDocId, attrNameToIndexMap.get(globalAttrName));
        
    }
    
    
}
