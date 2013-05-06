package emr_vis_nlp.ml.deprecated;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.filters.Filter;

/**
 * @deprecated 
 */
public class NLP_Visual_Interface {
	protected Classifier m_Classifier = null;
	
	public NLP_Visual_Interface(){
		super();
	}
	
	public void loadModel(String fnModel) throws Exception{
		this.m_Classifier = (Classifier) weka.core.SerializationHelper.read(fnModel);
	}
	
	public void saveModel(String fnModel) throws Exception{
		weka.core.SerializationHelper.write(fnModel, this.m_Classifier);
	}
	
	public Instances predictData(Instances unlabeled) throws Exception{
		// set class attribute
		unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

		// create copy
		Instances labeled = new Instances(unlabeled);

		// label instances
		for (int i = 0; i < unlabeled.numInstances(); i++) {
			double clsLabel = this.m_Classifier.classifyInstance(unlabeled.instance(i));
			labeled.instance(i).setClassValue(clsLabel);
		}

		return labeled;
	}
	
	public String predictInstance(String fnInstances) throws Exception{
		// assume that the file contains only 1 instance
		// load instances
		Instances data = new Instances(new BufferedReader(new FileReader(fnInstances)));
		// remove reportID attribute
		String[] options = weka.core.Utils.splitOptions("-R 1");
		String filterName = "weka.filters.unsupervised.attribute.Remove";
		Filter filter = (Filter) Class.forName(filterName).newInstance();
	    if (filter instanceof OptionHandler)
	      ((OptionHandler) filter).setOptions(options);
	    filter.setInputFormat(data);
	    // make the instances
	    Instances unlabeled = Filter.useFilter(data, filter);
	    Instances labeled = this.predictData(unlabeled);
	    double predict = labeled.instance(0).classValue(); 
	    return unlabeled.classAttribute().value((int) predict);
	}
	
	public double[][] predictDataDistribution(Instances unlabeled) throws Exception{
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
	
	public double[] predictInstanceDistribution(String fnInstances) throws Exception{		
		// assume that the file contains only 1 instance
		// load instances
		Instances data = new Instances(new BufferedReader(new FileReader(fnInstances)));
		// remove reportID attribute
		String[] options = weka.core.Utils.splitOptions("-R 1");
		String filterName = "weka.filters.unsupervised.attribute.Remove";
		Filter filter = (Filter) Class.forName(filterName).newInstance();
	    if (filter instanceof OptionHandler)
	      ((OptionHandler) filter).setOptions(options);
	    filter.setInputFormat(data);
	    // make the instances
	    Instances unlabeled = Filter.useFilter(data, filter);
	    
	    double[][] dist = this.predictDataDistribution(unlabeled);
	    return dist[0];
	}
	
	public void trainModel(Instances trainData) throws Exception{
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
	
	public void trainModelFromFile(String fnTrainData) throws Exception{
		// load instances
		Instances data = new Instances(new BufferedReader(new FileReader(fnTrainData)));
		// preprocess instances
		String[] options = weka.core.Utils.splitOptions("-R 1");
		String filterName = "weka.filters.unsupervised.attribute.Remove";
		Filter filter = (Filter) Class.forName(filterName).newInstance();
	    if (filter instanceof OptionHandler)
	      ((OptionHandler) filter).setOptions(options);
	    filter.setInputFormat(data);
	    // make the instances
	    Instances unlabeled = Filter.useFilter(data, filter);
	    // train model
	    this.trainModel(unlabeled);
	}  
	
	public static void main(String[] args) throws Exception {
		NLP_Visual_Interface tester = new NLP_Visual_Interface();
//		// learn model
//		String fnTrainData = "Exp1_7.arff";
//		tester.trainModelFromFile(fnTrainData);
//		// save model
//		String fnModel = "Indicator7.model";
//		tester.saveModel(fnModel);
//		// load model
//		String fnModel = "Indicator7.model";
//		tester.loadModel(fnModel);
//		// predict 1 instance
//		String fnInstances = "test_7.arff";
//		double[][] dist = tester.predictInstanceDistribution(fnInstances);
//		for(int i = 0; i < dist.length; i++) {
//			for(int j = 0; j < 3; j++){
//				if(dist[i][j] == 1){
//					switch(j){
//						case 0:
//							System.out.print("-1");
//							break;
//						case 1:
//							System.out.print("0");
//							break;
//						case 2:
//							System.out.print("1");
//							break;
//					}
//					break;
//				}
//			}				
//			System.out.print(",");
//		}
//		System.out.println();
//		String[] inds = {"2", "3", "6", "7", "9", "10", "11", "12", "15", "17"};
//		for(int i = 0; i < inds.length; i++){
//			// load data
//			String fnTrainData = "Exp1_" + inds[i] + ".arff";
//			Instances data = new Instances(new BufferedReader(new FileReader(fnTrainData)));
//			// remove reportID attribute
//			String[] options = weka.core.Utils.splitOptions("-R 1");
//			String filterName = "weka.filters.unsupervised.attribute.Remove";
//			Filter filter = (Filter) Class.forName(filterName).newInstance();
//			if (filter instanceof OptionHandler)
//				((OptionHandler) filter).setOptions(options);
//			filter.setInputFormat(data);
//			// make the instances
//			Instances unlabeled = Filter.useFilter(data, filter);
//			// train a model
//			Instances trainSet = unlabeled.trainCV(10, 0);
//			trainSet.setClassIndex(trainSet.numAttributes() - 1);
//			// save test set
//			String fnTrainSet = "Data\\train" + inds[i] + ".arff";
//			BufferedWriter writer = new BufferedWriter(new FileWriter(fnTrainSet));
//			writer.write(trainSet.toString());
//			writer.flush();
//			writer.close();
//			tester.trainModel(trainSet);
//			String fnModel = "Models\\Indicator" + inds[i] + ".model";
//			tester.saveModel(fnModel);
//			Instances testSet = unlabeled.testCV(10, 0);
//			testSet.setClassIndex(testSet.numAttributes() - 1);
//			// save test set
//			String fnTestSet = "Data\\test" + inds[i] + ".arff";
//			writer = new BufferedWriter(new FileWriter(fnTestSet));
//			writer.write(testSet.toString());
//			writer.flush();
//			writer.close();
//		}
//		System.out.println("Done");		
		
//		// load model		
//		NLP_Visual_Interface[] models = loadModels();
//		// load instance
//		String fnReportVector = args[0];
//		Instances instance = new Instances(new BufferedReader(new FileReader(fnReportVector)));
//		instance.setClassIndex(instance.numAttributes() - 1);
//		//predict
//		int indicatorNumber = 10;
//		double[][] predictions = new double[indicatorNumber][instance.numClasses()];
//		for (int i = 0; i < 10; i++){
//			predictions[i] = models[i].predictInstanceDistribution(fnReportVector);
//		}
//		
//		// export results
//		for (int i = 0; i < 10; i++){
//			System.out.print("Indicator " + (i + 1) + ": ");
//			for (int j = 0; j < instance.numClasses(); j++){
//				System.out.print(predictions[i][j] + ", ");
//			}
//			System.out.println();
//		}
		ColonoscopyDemo demo = new ColonoscopyDemo();
		double[][] predicts = demo.predictInstance("temp.arff");
		for(int i = 0; i < predicts.length; i++){
			for(int j = 0; j < 3; j++){
				System.out.print(predicts[i][j] + ",");
			}
			System.out.println();
		}
	}
	
	public static NLP_Visual_Interface[] loadModels() throws Exception{
		NLP_Visual_Interface[] testers = new NLP_Visual_Interface[10];
		testers[0] = new NLP_Visual_Interface();
		testers[0].loadModel("Models\\Indicator2.model");
		testers[1] = new NLP_Visual_Interface();
		testers[1].loadModel("Models\\Indicator2.model");
		testers[2] = new NLP_Visual_Interface();
		testers[2].loadModel("Models\\Indicator2.model");
		testers[3] = new NLP_Visual_Interface();
		testers[3].loadModel("Models\\Indicator2.model");
		testers[4] = new NLP_Visual_Interface();
		testers[4].loadModel("Models\\Indicator2.model");
		testers[5] = new NLP_Visual_Interface();
		testers[5].loadModel("Models\\Indicator2.model");
		testers[6] = new NLP_Visual_Interface();
		testers[6].loadModel("Models\\Indicator2.model");
		testers[7] = new NLP_Visual_Interface();
		testers[7].loadModel("Models\\Indicator2.model");
		testers[8] = new NLP_Visual_Interface();
		testers[8].loadModel("Models\\Indicator2.model");
		testers[9] = new NLP_Visual_Interface();
		testers[9].loadModel("Models\\Indicator2.model");
		
		return testers;
	}
}
