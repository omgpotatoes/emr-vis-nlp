package annotator.backend;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.filters.Filter;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.*;

public class WekaWrapper {
	/** the classifier used internally */
	protected Classifier m_Classifier = null;

	/** the filter to use */
	protected Filter m_Filter = null;

	/** the training file */
	protected String m_TrainingFile = null;

	/** the training instances */
	protected Instances m_Training = null;

	/** the test file */
	protected String m_TestFile = null;

	/** the test instances */
	protected Instances m_Test = null;	  

	/** for evaluating the classifier */
	protected Evaluation m_Evaluation = null;

	/**
	 * initializes the demo
	 */
	public WekaWrapper() {
		super();
	}
	
	/**
	 * converts option string into String[]
	 * @param optionStr        string of options, e.g. "-R 1"
	 * return String[] options
	 */
	public static String[] optionsToCode(String optionStr) throws Exception {
		return Utils.splitOptions(optionStr);
	}

	/**
	 * sets the classifier to use
	 * @param name        the classname of the classifier
	 * @param options     the options for the classifier
	 */
	public void setClassifier(String name, String[] options) throws Exception {
		m_Classifier = Classifier.forName(name, options);
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
	 * resets filter
	 */
	public void resetFilter() throws Exception {
		m_Filter = null;
	}

	/**
	 * sets the file to use for training
	 */
	public void setTraining(String name) throws Exception {
		this.setTraining(name, m_Training.numAttributes() - 1);
	}	  

	/**
	 * sets the file to use for training
	 */
	public void setTraining(String name, int classIndex) throws Exception {
		m_TrainingFile = name;
		m_Training     = new Instances(
				new BufferedReader(new FileReader(m_TrainingFile)));
		m_Training.setClassIndex(classIndex);
	}	

	/**
	 * sets the file to use for test
	 */
	public void setTest(String name) throws Exception {
		this.setTest(name, m_Test.numAttributes() - 1);
	}

	/**
	 * sets the file to use for test
	 */
	public void setTest(String name, int classIndex) throws Exception {
		m_TestFile = name;
		m_Test     = new Instances(
				new BufferedReader(new FileReader(m_TrainingFile)));
		m_Test.setClassIndex(classIndex);
	}

	/**
	 * runs Kfold CV over the training file
	 */
	public void executeKFold(int k) throws Exception {
		Instances filtered;
		if(m_Filter != null){
			// run filter
			m_Filter.setInputFormat(m_Training);
			filtered = Filter.useFilter(m_Training, m_Filter);	     
		}
		else{
			filtered = m_Training;
		}

		// train classifier on complete file for tree
		m_Classifier.buildClassifier(filtered);

		// Kfold CV with seed=1
		m_Evaluation = new Evaluation(filtered);
		m_Evaluation.crossValidateModel(
				m_Classifier, filtered, k, new Random(1));	    
	}

	/**
	 * runs held-out test file
	 */
	public void executeHeldoutTest() throws Exception {
		Instances filtered;
		if(m_Filter != null){
			// run filter
			m_Filter.setInputFormat(m_Training);
			filtered = Filter.useFilter(m_Training, m_Filter);	     
		}
		else{
			filtered = m_Training;
		}

		// train classifier on complete file for tree
		m_Classifier.buildClassifier(filtered);

		// held-out test set
		m_Evaluation = new Evaluation(filtered);
		m_Evaluation.evaluateModel(m_Classifier, m_Test);	    
	}	
}
