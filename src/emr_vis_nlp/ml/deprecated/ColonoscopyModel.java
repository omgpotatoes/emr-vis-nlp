package emr_vis_nlp.ml.deprecated;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.filters.Filter;

/**
 * @deprecated 
 */
public class ColonoscopyModel {

    protected Classifier m_Classifier = null;

    public ColonoscopyModel() {
        super();
    }

    public void loadModel(String fnModel) throws Exception {
        this.m_Classifier = (Classifier) weka.core.SerializationHelper.read(fnModel);
    }

    public void saveModel(String fnModel) throws Exception {
        weka.core.SerializationHelper.write(fnModel, this.m_Classifier);
    }

    protected double[][] predictDataDistribution(Instances unlabeled) throws Exception {
        // set class attribute
        unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

        // distribution for instance
        double[][] dist = new double[unlabeled.numInstances()][unlabeled.numClasses()];

        // label instances
        for (int i = 0; i < unlabeled.numInstances(); i++) {
            double[] instanceDist = this.m_Classifier.distributionForInstance(unlabeled.instance(i));
            dist[i] = instanceDist;
        }

        return dist;
    }

    public double[] predictInstanceDistribution(String fnInstances) throws Exception {
        // assume that the file contains only 1 instance
        // load instances
        Instances data = new Instances(new BufferedReader(new FileReader(fnInstances)));
        // remove reportID attribute
        String[] options = weka.core.Utils.splitOptions("-R 1");
        String filterName = "weka.filters.unsupervised.attribute.Remove";
        Filter filter = (Filter) Class.forName(filterName).newInstance();
        if (filter instanceof OptionHandler) {
            ((OptionHandler) filter).setOptions(options);
        }
        filter.setInputFormat(data);
        // make the instances
        Instances unlabeled = Filter.useFilter(data, filter);

        double[][] dist = this.predictDataDistribution(unlabeled);
        return dist[0];
    }

    protected void trainModel(Instances trainData) throws Exception {
        // set class attribute
        trainData.setClassIndex(trainData.numAttributes() - 1);
        // set classifier: use linear SVM only
        String[] options = weka.core.Utils.splitOptions("-K 0");
        String classifierName = "weka.classifiers.functions.LibSVM";
        this.m_Classifier = Classifier.forName(classifierName, options);
        // get probability instead of explicit prediction
        LibSVM libsvm = (LibSVM) this.m_Classifier;
        libsvm.setProbabilityEstimates(true);
        // build model
        this.m_Classifier.buildClassifier(trainData);
    }

    public void trainModelFromFile(String fnTrainData) throws Exception {
        // load instances
        Instances data = new Instances(new BufferedReader(new FileReader(fnTrainData)));
        // preprocess instances
        String[] options = weka.core.Utils.splitOptions("-R 1");
        String filterName = "weka.filters.unsupervised.attribute.Remove";
        Filter filter = (Filter) Class.forName(filterName).newInstance();
        if (filter instanceof OptionHandler) {
            ((OptionHandler) filter).setOptions(options);
        }
        filter.setInputFormat(data);
        // make the instances
        Instances unlabeled = Filter.useFilter(data, filter);
        // train model
        this.trainModel(unlabeled);
    }

    // get training set in K-fold cross validation
    // e.g the first fold of 10-fold CV would be 
    // foldNumber = 0; foldTotal = 10
    public Instances getTrainSet(int foldNumber, int foldTotal, String fnData) throws Exception {
        // load instances
        Instances data = new Instances(new BufferedReader(new FileReader(fnData)));
        data.setClassIndex(data.numAttributes() - 1);
        Instances trainSet = data.trainCV(foldTotal, foldNumber);

        return trainSet;
    }

    public void saveTrainSet(int foldNumber, int foldTotal, String fnData, String fnTrainSet) throws Exception {
        Instances trainSet = this.getTrainSet(foldNumber, foldTotal, fnData);
        BufferedWriter writer = new BufferedWriter(new FileWriter(fnTrainSet));
        writer.write(trainSet.toString());
        writer.flush();
        writer.close();
    }

    // get testing set in K-fold cross validation
    // e.g the first fold of 10-fold CV would be 
    // foldNumber = 0; foldTotal = 10
    public Instances getTestSet(int foldNumber, int foldTotal, String fnData) throws Exception {
        // load instances
        Instances data = new Instances(new BufferedReader(new FileReader(fnData)));
        data.setClassIndex(data.numAttributes() - 1);
        Instances testSet = data.trainCV(foldTotal, foldNumber);

        return testSet;
    }

    public void saveTestSet(int foldNumber, int foldTotal, String fnData, String fnTestSet) throws Exception {
        Instances testSet = this.getTestSet(foldNumber, foldTotal, fnData);
        BufferedWriter writer = new BufferedWriter(new FileWriter(fnTestSet));
        writer.write(testSet.toString());
        writer.flush();
        writer.close();
    }

    public Map<String, Double> getTermWeights() {

        try {
            
            // get weights
            double[] weights = ((LibSVM) m_Classifier).getFeatureWeights();
//            String weightStr = ((LibSVM) m_Classifier).getWeights();
//            // debug
//            System.out.println("debug: ColonsopyModel.getTermWeights: weightStr: "+weightStr);
//            List<Double> weightList = new ArrayList<>();
//            Scanner weightStrSplitter = new Scanner(weightStr);
//            while (weightStrSplitter.hasNextDouble()) {
//                weightList.add(weightStrSplitter.nextDouble());
//            }
//            double[] weights = new double[weightList.size()];
//            for (int w=0; w<weightList.size(); w++)  {
//                weights[w] = weightList.get(w);
//            }

            // map weights to terms
            List<String> bowTerms = RuntimeIndicatorPrediction.getListOfBOWTerms();

            assert weights.length == bowTerms.size();
            
            if (weights.length != bowTerms.size()) {
                
                System.err.println("weights and bowTerms not of same length! "+weights.length+" vs "+bowTerms.size());
                return null;
                
            }
            
            Map<String, Double> termWeightMap = new HashMap<>();
            for (int t=0; t<bowTerms.size(); t++) {
                termWeightMap.put(bowTerms.get(t), weights[t]);
            }
            
            return termWeightMap;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("err: could not get feature weights from model: " + m_Classifier.toString());
        }

        return null;

    }
}
