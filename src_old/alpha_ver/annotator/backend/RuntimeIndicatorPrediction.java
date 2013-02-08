package annotator.backend;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * This class is responsible for building temporary datafiles and calling
 * Phuong's back-end models for prediction of indicators. This class serves as
 * an intermediary between the visualization and the back-end models.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class RuntimeIndicatorPrediction {

    public static final String BOW_TERMS_FILE = "resources/backend/arff_attr_bow_file.txt";
    public static final String TEMP_ARFF_FILE = "resources/backend/temp.arff";
    // list of terms (in order) for which a temporary .arff should be consturcted
    private static List<String> bowTerms = null;
    private static ColonoscopyDemo demo = null;
    
    public static String[] predictedIndicatorNames = {"Indicator_21","Indicator_2","Indicator_16","Indicator_24","Indicator_19","Indicator_11","Indicator_23","Indicator_12","Indicator_17","Indicator_3.1"};
    
    
    public static void initRuntimeIndicatorPredictor() {
    	readListOfBOWTerms();
    	demo = new ColonoscopyDemo();
    }
    
    /**
     * Builds a temporary WEKA .arff file
     *
     * @param text
     */
    public static void buildTemporaryFileForText(String text) {

        if (bowTerms == null) {
            // rebuilds list of terms
            readListOfBOWTerms();
        }

        try {

            File tempFile = new File(TEMP_ARFF_FILE);
            tempFile.delete();
            FileWriter tempFileWriter = new FileWriter(tempFile);

            String header = "% This is the Colonoscopy problem\n@relation current_working_report\n@attribute [ReportID] numeric\n";
            tempFileWriter.write(header);

            List<Integer> attrVals = new ArrayList<>();
            for (String attrName : bowTerms) {
                String line = "@attribute \"" + attrName + "\" {0, 1}\n";
                tempFileWriter.write(line);

                if (text.contains(attrName)) {
                    attrVals.add(1);
                } else {
                    attrVals.add(0);
                }

            }

            String attrFooter = "@attribute \"[classLabel]\" {-1, 0, 1}\n@data\n'4'";
            tempFileWriter.write(attrFooter);

            for (Integer attrVal : attrVals) {

                String attrValStr = "," + attrVal;
                tempFileWriter.write(attrValStr);

            }

            tempFileWriter.write(",0\n");

            tempFileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error writing temp file: " + TEMP_ARFF_FILE);
        }

    }

    public static double[][] predictIndicatorsForTempFile() {
        return predictIndicatorsForTempFile(TEMP_ARFF_FILE);
    }

    /**
     * Processes the temporary file
     *
     * @param tempFilePath
     * @return
     */
    public static double[][] predictIndicatorsForTempFile(String tempFilePath) {
    	
    	// @TODO enable use of other experiment configurations, besides the default #1 
    	
        if (demo == null) {
            demo = new ColonoscopyDemo();
        }

        try {
            double[][] predicts = demo.predictInstance(tempFilePath);

            // debug
//            for (int i = 0; i < predicts.length; i++) {
//                for (int j = 0; j < 3; j++) {
//                    System.out.print(predicts[i][j] + ",");
//                }
//                System.out.println();
//            }

            return predicts;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error: something crazy happened while trying to predict indicator values");
        }

        return null;

    }

    public static void resetDemo() {
        demo = null;
    }

    /**
     * Builds an ordered list of the terms that the backend model expects to see
     * in an arff file. This should only need to be run whenever the backend
     * models change in some way.
     *
     */
    public static void buildListOfBOWTermAttrs() {

        try {
            // read temp.arff (may want to make this more robust later)
            Scanner tempReader = new Scanner(new FileReader(TEMP_ARFF_FILE));

            // extract all "@attributes", sans first and last
            List<String> tempTextAttrs = new ArrayList<>();
            int attrCounter = 0;
            while (tempReader.hasNextLine()) {
                String line = tempReader.nextLine();
                if (line.length() >= 10 && line.substring(0, 10).equals("@attribute")) {
                    if (attrCounter != 0) {
                        Scanner lineSplitter = new Scanner(line);
                        lineSplitter.next();
                        String attrName = lineSplitter.next().trim();
                        // remove first, last chars
                        attrName = attrName.substring(1, attrName.length() - 1);
                        tempTextAttrs.add(attrName);
                    }
                    attrCounter++;
                }
            }
            // remove last attr
            tempTextAttrs.remove(tempTextAttrs.size() - 1);
            tempReader.close();

            // write to plaintext
            File bowTermsFile = new File(BOW_TERMS_FILE);
//            try {
            bowTermsFile.delete();
//            } catch (IOException e) {
//                e.printStackTrace();
//                System.out.println("could not delete file: "+BOW_TERMS_FILE);
//            }
            FileWriter textAttrWriter = new FileWriter(bowTermsFile);
            for (String textAttr : tempTextAttrs) {
                textAttrWriter.write(textAttr + "\n");
            }
            textAttrWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error while reading temp.arff and/or building BOW datafile: " + BOW_TERMS_FILE);
        }



    }

    /**
     * Reads list of terms (written by buildListOfBOWTermsAttrs) into memory.
     * File should contain 1 term per line.
     *
     */
    public static void readListOfBOWTerms() {

        bowTerms = new ArrayList<>();
        try {
            Scanner bowTermsReader = new Scanner(new FileReader(BOW_TERMS_FILE));
            while (bowTermsReader.hasNextLine()) {
                String token = bowTermsReader.nextLine().trim();
                if (token.length() > 0 && token.charAt(0) != '#') {
                    bowTerms.add(token);
                }
            }
            bowTermsReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error reading BOW terms file: " + BOW_TERMS_FILE);
        }

    }
    
    public static Map<String, Double> getTermWeightsForIndicator(String indicatorName) {
        
        // note: indicatorName should correspond to a model
        boolean predNameFound = false;
        for (int i=0; i<predictedIndicatorNames.length; i++) {
            if (indicatorName.equals(predictedIndicatorNames[i])) {
                predNameFound = true;
                
                Map<String, Double> termWeights = demo.getTermWeightMapForModelIndex(i);
                return termWeights;
                
            }
            
        }
        
        return null;
        
    }

    public static void resetListOfBOWTerms() {
        bowTerms = null;
    }
    
    public static List<String> getListOfBOWTerms() {
        
        if (bowTerms == null) {
            // rebuilds list of terms
            readListOfBOWTerms();
        }
        
        return bowTerms;
        
    }

    public static void main(String[] args) {

        buildListOfBOWTermAttrs();

    }
}
