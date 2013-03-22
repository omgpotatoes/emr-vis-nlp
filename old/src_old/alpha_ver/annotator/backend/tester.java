package annotator.backend;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.filters.Filter;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.*;

public class tester {
	protected Instances m_Training = null;
	protected Filter m_Filter = null;
	protected Classifier m_Classifier = null;
	protected String m_expCounter;
	protected String m_evaluationPath;
	protected String m_statisticPath;	
	protected double m_nTopFeature;
	protected Boolean m_evaluate;
	
	public tester(){
		super();
	}
	
	public tester(String expCounter, double nTopFeature, String evaluationPath, String statisticPath,
			String evaluate){
		super();
		m_expCounter = expCounter;
		m_nTopFeature = nTopFeature;
		m_evaluationPath = evaluationPath;
		m_statisticPath = statisticPath;
		m_evaluate = evaluate == "eval" ? true : false;
		m_evaluate = true;
	}
	
	public void setTraining(String name) throws Exception {
	    m_Training     = new Instances(
	                        new BufferedReader(new FileReader(name)));
	    m_Training.setClassIndex(m_Training.numAttributes() - 1);
	}
	
	/**
	   * sets the filter to use
	   * @param name        the classname of the filter
	   * @param options     the options for the filter
	   */
	  public void setFilter(String name, String[] options) throws Exception {
	    m_Filter = (Filter) Class.forName(name).newInstance();
	    if (m_Filter instanceof OptionHandler)
	      ((OptionHandler) m_Filter).setOptions(options);
	  }
	  
	  /**
	   * sets the classifier to use
	   * @param name        the classname of the classifier
	   * @param options     the options for the classifier
	   */
	  public void setClassifier(String name, String[] options) throws Exception {
	    m_Classifier = Classifier.forName(name, options);
	  }
	
	//load data
	//learn model for each file
	//export results
	public void execute() throws Exception{
		if(m_evaluate == true){
			evaluate();
		}
		else{
			predict();
		}
	}
	
	private void evaluate() throws Exception{
		ArrayList<String> fileNames = loadFileList();
		int nFile = fileNames.size();
		String confusionMatrix = "";
		
		for(int iFile = 0; iFile < nFile; iFile++){
//		for(int iFile = 0; iFile < 1; iFile++){			
			String fileName = fileNames.get(iFile);
			String measureCounter = getMeasureCounter(fileName);
			
			//load data
			setTraining(fileName);
			
			//save weights if this exp is run for the first time
			String[] paths = {m_statisticPath, "Weights","Exp" + m_expCounter + "_" + measureCounter + "_weights.csv"};
			String fnWeight = Common.getOSPath(paths);
			if(!Common.fileExists(fnWeight)){
				runFirstTimeGetWeight(fileName);
			}
			
			String removeFeaturesString = getRemovedFeaturesString(fnWeight, m_nTopFeature);
			Instances filtered = filterDataSet(removeFeaturesString);
			//use linear SVM only
			String[] options = weka.core.Utils.splitOptions("-K 0");
			setClassifier("weka.classifiers.functions.LibSVM", options);
//			m_Classifier.buildClassifier(filtered);
			//run experiment with removed features
			//init ExportResult object
			ExportResult exporter = new ExportResult(m_expCounter, m_statisticPath);						
			//evaluate model
			Evaluation eval = evaluateModel(filtered);			
			confusionMatrix += exporter.toConfustionMatrixString(eval.confusionMatrix(), eval.getClassNames(), measureCounter)
					+ "\n";
			System.out.println("Completed " + Integer.toString(iFile + 1) + "/" + Integer.toString(nFile));
		}
		
		//save result of using top features
		String[] paths = {m_statisticPath, "Exp" + m_expCounter + "_confustionMatrix_" + 
							Double.toString(m_nTopFeature) + ".csv"};
		String fileName = Common.getOSPath(paths);
		
		Common.saveTextFile(fileName, confusionMatrix);
		System.out.println("Experiment " + m_expCounter + " with " + Double.toString(m_nTopFeature) + " top features completed");
	}
	
	private void predict() throws Exception{
		ArrayList<String> fileNames = loadFileList();
		int nFile = fileNames.size();
		
		for(int iFile = 0; iFile < nFile; iFile++){
//		for(int iFile = 0; iFile < 1; iFile++){			
			String fileName = fileNames.get(iFile);
			String measureCounter = getMeasureCounter(fileName);
			
			//load data
			setTraining(fileName);
			
			//save weights if this exp is run for the first time
			String[] paths = {m_statisticPath, "Weights","Exp" + m_expCounter + "_" + measureCounter + "_weights.csv"};
			String fnWeight = Common.getOSPath(paths);
			if(!Common.fileExists(fnWeight)){
				runFirstTimeGetWeight(fileName);
			}
			
			String removeFeaturesString = getRemovedFeaturesString(fnWeight, m_nTopFeature);
			Instances filtered = filterDataSet(removeFeaturesString);	
			//use linear SVM only
			String[] options = weka.core.Utils.splitOptions("-K 0");
			setClassifier("weka.classifiers.functions.LibSVM", options);
			m_Classifier.buildClassifier(filtered);
			//predict class label
			String 	classValue = predictInstances(m_Training, removeFeaturesString);
			//save result of using top features
			String[] outputPaths = {m_statisticPath, "Exp" + m_expCounter + "_classLabel_measure_" + 
								measureCounter + "_" + Double.toString(m_nTopFeature) + ".csv"};
			fileName = Common.getOSPath(outputPaths);
			
			Common.saveTextFile(fileName, classValue);
			System.out.println("Completed " + Integer.toString(iFile + 1) + "/" + Integer.toString(nFile));
		}
//		ArrayList<String> fileNames = new ArrayList<String>();
//		fileNames.add("C:\\Users\\Phuong Pham\\Desktop\\Document\\Workplace\\DropboxPortableAHK\\Dropbox\\Summer 2012\\Workspace\\Data\\Colonoscopy\\Exp\\Evaluation\\Exp28_train_10.arff");
//		int nFile = fileNames.size();
//		
//		for(int iFile = 0; iFile < nFile; iFile++){
////		for(int iFile = 0; iFile < 1; iFile++){			
//			String fileName = fileNames.get(iFile);
//			String measureCounter = getMeasureCounter(fileName);
//			
//			//load data
//			setTraining(fileName);
//			
//			//save weights if this exp is run for the first time
//			String[] paths = {m_statisticPath, "Weights","Exp" + m_expCounter + "_" + measureCounter + "_weights.csv"};
//			String fnWeight = Common.getOSPath(paths);
//			if(!Common.fileExists(fnWeight)){
//				runFirstTimeGetWeight(fileName);
//			}
//			
//			String removeFeaturesString = getRemovedFeaturesString(fnWeight, m_nTopFeature);
//			Instances filtered = buildModel(removeFeaturesString);
//			//create testing Instances
//			Instances testingData     = new Instances(
//                     new BufferedReader(new FileReader("C:\\Users\\Phuong Pham\\Desktop\\Document\\Workplace\\DropboxPortableAHK\\Dropbox\\Summer 2012\\Workspace\\Data\\Colonoscopy\\Exp\\Evaluation\\Exp28_test_10.arff")));
//			testingData.setClassIndex(testingData.numAttributes() - 1);
//			
//			//predict class label
//			String 	classValue = predictInstances(testingData, removeFeaturesString);
//			//save result of using top features
//			String[] outputPaths = {m_statisticPath, "Exp" + m_expCounter + "_classLabel_measure_" + 
//								measureCounter + "_" + Double.toString(m_nTopFeature) + ".csv"};
//			fileName = Common.getOSPath(outputPaths);
//			
//			Common.saveTextFile(fileName, classValue);			
//			System.out.println("Completed " + Integer.toString(iFile + 1) + "/" + Integer.toString(nFile));
//		}
	}
	
	public Instances predictData(Classifier trainedModel, Instances unlabeled) throws Exception{
		// set class attribute
		unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

		// create copy
		Instances labeled = new Instances(unlabeled);

		// label instances
		for (int i = 0; i < unlabeled.numInstances(); i++) {
			double clsLabel = trainedModel.classifyInstance(unlabeled.instance(i));
			labeled.instance(i).setClassValue(unlabeled.classAttribute().value((int) clsLabel));
		}
		
		return labeled;
	}
	
	public Instance predictDataIns(String fnModel, String fnDataIns) throws Exception{
		// fnDataIns contains 1 instance, because Weka does not support manually create Instances or Instance easily
		// load instances
		Instances data     = new Instances(
                new BufferedReader(new FileReader(fnDataIns)));
		// remove reportID attribute
		String[] options = weka.core.Utils.splitOptions("-R 1");
		setFilter("weka.filters.unsupervised.attribute.Remove", options);
		m_Filter.setInputFormat(data);
		Instances unlabeled = Filter.useFilter(data, m_Filter);
		// load model
		Classifier trainedModel = loadModel(fnModel);
		Instances labeled = this.predictData(trainedModel, unlabeled);
		// return the first, the only instance in this dataset
		return labeled.instance(0);
	}
	
	public Classifier loadModel(String fnModel) throws Exception{
		return (Classifier) weka.core.SerializationHelper.read(fnModel);
	}
	
	public void saveModel(String fnModel, Classifier model) throws Exception{
		weka.core.SerializationHelper.write(fnModel, model);
	}

	private String getRemovedFeaturesString(String fileName, double nRemoved) throws Exception{
		int offset = 2;//ReportID is skipped and attribute in Weka starts from 1 not 0
		FeatureWeight[] fWeight = ExportResult.getWeights(fileName);
		int zeroWeightIndex = 0;
		for(int i = 0; i < fWeight.length; i++){
			if(fWeight[i].weight == 0){
				zeroWeightIndex = i;
				break;
			}
		}
		
		int nNonZeroWeight = zeroWeightIndex;
		//compute number of top feature needed to keep
		int nKeptWeight = nRemoved > 1 ? (int)nRemoved : (int)Math.round(nRemoved * nNonZeroWeight);
		int iStart = nKeptWeight;
		String removedFeature = "";
		for(int i = iStart; i < fWeight.length - 1; i++){
			removedFeature += Integer.toString(fWeight[i].index + offset) + ",";
		}
		
		if(iStart < fWeight.length - 1){
			removedFeature += Integer.toString(fWeight[fWeight.length - 1].index + offset);
		}
		
		return removedFeature;
	}
	
	private void runFirstTimeGetWeight(String fileName) throws Exception{		
		//build model and filter data
		//in this project, filter = remove attr
		Instances filtered = filterDataSet("");
		//use linear SVM only
		String[] options = weka.core.Utils.splitOptions("-K 0");
		setClassifier("weka.classifiers.functions.LibSVM", options);
		m_Classifier.buildClassifier(filtered);
		//get features' weights
		double[] weights = getFeatureWeights();
		//init ExportResult object
		ExportResult exporter = new ExportResult(m_expCounter, m_statisticPath);
		//export result into a file
		String measureCounter = getMeasureCounter(fileName);
		//export weight into a file			
		exporter.saveFeatureWeights(weights, measureCounter);
	}
	
	private String getMeasureCounter(String fileName) throws Exception{
		int start = fileName.lastIndexOf("_") + 1;
		int end = fileName.lastIndexOf(".");
		return fileName.substring(start, end);
	}
	
	private Instances filterDataSet(String removedFeature) throws Exception{		
		//filter the first attribute (reportID)
		String removedOption = removedFeature == "" ? "-R 1" : "-R 1," + removedFeature;
		String[] options = weka.core.Utils.splitOptions(removedOption);
		setFilter("weka.filters.unsupervised.attribute.Remove", options);
		m_Filter.setInputFormat(m_Training);
		Instances filtered = Filter.useFilter(m_Training, m_Filter);
		//if used unfiltered
//		Instances filtered = m_Training;	
		return filtered;
	}
	
	private Evaluation evaluateModel(Instances filtered) throws Exception{
		Evaluation eval = new Evaluation(filtered);
		eval.crossValidateModel(m_Classifier, filtered, 10, new Random(1));
		return eval;
	}
	
	private String predictInstances(Instances predictedData, String removedFeature) throws Exception{
		String classValue = "";
		//filter the first attribute (reportID)
		String removedOption = removedFeature == "" ? "-R 1" : "-R 1," + removedFeature;
		String[] options = weka.core.Utils.splitOptions(removedOption);
		setFilter("weka.filters.unsupervised.attribute.Remove", options);
		m_Filter.setInputFormat(predictedData);
		Instances filtered = Filter.useFilter(predictedData, m_Filter);
		
		for(int i = 0; i < filtered.numInstances(); i++){
			double pred = m_Classifier.classifyInstance(filtered.instance(i));
			classValue += m_Training.instance(i).value(0) + "," + 
					filtered.classAttribute().value((int)pred) + "," + 
					filtered.classAttribute().value((int)filtered.instance(i).classValue()) + "\n";
		}
		
		return classValue;
	}
	
	private double[] getFeatureWeights() throws Exception{
		LibSVM libsvm = (LibSVM) m_Classifier;
		return libsvm.getFeatureWeights();
	}
	
	private ArrayList<String> loadFileList(){
		File directory = new File(m_evaluationPath);
		File[] files = directory.listFiles();
		ArrayList<String> fileNames = new ArrayList<String>();
		
		for(int iFile = 0; iFile < files.length; iFile++){
			String fileName = files[iFile].toString();
			//filter according to expCounter
			if(fileName.contains("Exp" + m_expCounter + "_")){
				fileNames.add(fileName);
			}
		}
		
		return fileNames;
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */	
	public static void main(String[] args) throws Exception {
		// 1st arg: expCounter
		// 2nd arg: number of top feature to keep
		//homePath = Workspace
		// TODO Auto-generated method stub
		//assume that calling this program has no mistakes on argument value
		String expCounter = args[0];
		double nTopFeature = Double.parseDouble(args[1]);
		String executingPath = Common.getExecutingPath();
//		String homePath = Common.getParentDirectoryRecursive(executingPath, 1);//run this line in Eclipse
		String homePath = Common.getParentDirectoryRecursive(executingPath, 2);//run this line in command line
		String[] paths = {homePath, "Data", "Colonoscopy", "Exp", "Evaluation"};
		String evaluationPath = Common.getOSPath(paths);
		paths = new String[]{homePath, "Data", "Colonoscopy", "Exp", "Statistic"};
		String statisticPath = Common.getOSPath(paths);		
		
		//init learner
		tester test = new tester(expCounter, nTopFeature, evaluationPath, statisticPath, args[2]);
		
		test.execute();
	}
}
