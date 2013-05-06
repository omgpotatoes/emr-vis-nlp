
package emr_vis_nlp.ml.colon_vars;

import emr_vis_nlp.ml.Attribute;
import emr_vis_nlp.ml.MLPredictor;
import emr_vis_nlp.ml.PredictionCertaintyTuple;
import emr_vis_nlp.model.Document;
import emr_vis_nlp.model.MainModel;
import java.io.*;
import java.util.*;

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
                // need to use a fileinputstream here, else weird things may happen due to overful buffers?
                termWeightFileReader = new Scanner(new FileInputStream(termWeightFile));
                String line1 = termWeightFileReader.nextLine();
                // debug
//                System.out.println("debug: line1: "+line1);
                String line2 = termWeightFileReader.nextLine();
//                System.out.println("debug: line2: "+line2);
                Scanner line1Splitter = new Scanner(line1);
                line1Splitter.useDelimiter(",");
                Scanner line2Splitter = new Scanner(line2);
                line2Splitter.useDelimiter(",");
                while (line1Splitter.hasNext()) {
                    String newTerm = line1Splitter.next();
                    // debug
//                    System.out.print("debug: newTerm=\""+newTerm+"\", ");
                    double newWeight = line2Splitter.nextDouble();
//                    System.out.print(" newWeight="+newWeight+"\n");
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
    
    public double[][] predictInstance(Reader reader) {
        // assume binary classifiers
        double[][] predictions = new double[predictors.size()][2];

        // each of these models corresponds to one of the indicators
        for (int i = 0; i < predictors.size(); i++) {
            try {
                predictions[i] = predictors.get(i).predictInstanceDistribution(reader);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("err: "+this.getClass().getName()+": unable to perform prediction using reader "+reader.toString()+" using model for attr "+modelNames.get(i));
            }
        }

        return predictions;
    }
    
    public double[] predictInstanceSingleAttr(Reader reader, int p) {
        // assume binary classifiers
        double[] prediction = new double[2];

            try {
                prediction = predictors.get(p).predictInstanceDistribution(reader);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("err: "+this.getClass().getName()+": unable to perform prediction using reader "+reader.toString()+" using model for attr "+modelNames.get(p));
            }

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
                String documentText = documentList.get(d).getText();

                // eliminate tempFile, build reader directly without writing to disk
                StringBuilder tempFileBuilder = new StringBuilder();
                StringReader strReader = null;

                String header = "% This is the Colonoscopy problem\n@relation current_working_report\n@attribute [report_identifier] numeric\n";

                tempFileBuilder.append(header);

                List<Integer> termVals = new ArrayList<>();
                Map<String, Double> predictorTermWeightMap = predictorsTermWeightMaps.get(p);

                for (String termName : predictorTermWeightMap.keySet()) {
                    String line = "@attribute \"" + termName + "\" {0, 1}\n";
                    tempFileBuilder.append(line);

                    if (documentText.contains(termName)) {
                        termVals.add(1);
                    } else {
                        termVals.add(0);
                    }

                }

                String attrFooter = "@attribute \"[classLabel]\" {0, 1}\n@data\n0";
                tempFileBuilder.append(attrFooter);
                for (Integer termVal : termVals) {

                    String termValStr = "," + termVal;
                    tempFileBuilder.append(termValStr);

                }

                // append final value for classlabel; doesn't matter, since we're doing prediction on this document?
                tempFileBuilder.append(",0\n");

                strReader = new StringReader(tempFileBuilder.toString());
                
                // d1 = indicator; d2 = probs. for each val [0, 1]
                double[] prediction = predictInstanceSingleAttr(strReader, p);
                double zeroVal = prediction[0];
                double oneVal = prediction[1];
                String attributeName = attributeList.get(p).getName();
                PredictionCertaintyTuple predCertTuple = null;
                if (zeroVal >= oneVal) {
                    predCertTuple = new PredictionCertaintyTuple(attributeName, attributeList.get(p).getLegalValues().get(0), zeroVal);
                } else {
                    predCertTuple = new PredictionCertaintyTuple(attributeName, attributeList.get(p).getLegalValues().get(1), oneVal);
                }

                predictionMap.put(attributeName, predCertTuple);
            }
            predictionMapList.add(predictionMap);
        }
    }

    @Override
    public Map<String, Double> getTermWeightsForDocForAttr(int globalDocId, int globalAttrId) {
        if (globalAttrId == -1) {
            return new HashMap<>();
        }
        Map<String, Double> termWeightMap = predictorsTermWeightMaps.get(globalAttrId);
        return termWeightMap;
    }

    @Override
    public Map<String, Double> getTermWeightsForDocForAttr(int globalDocId, String globalAttrName) {
        if (!attrNameToIndexMap.containsKey(globalAttrName)) {
            return new HashMap<>();
        }
        return getTermWeightsForDocForAttr(globalDocId, attrNameToIndexMap.get(globalAttrName));
    }
    
    @Override
    public List<String> getAttributeValues(String attrName) {
        List<String> vals = new ArrayList<>();
        vals.add("False");
        vals.add("True");
        return vals;
    }
    
}
