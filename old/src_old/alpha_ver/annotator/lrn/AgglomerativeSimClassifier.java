package annotator.lrn;

import annotator.lrn.AgglomerativeSimMetric;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import annotator.data.DataTag;
import annotator.data.DataTagset;
import annotator.data.Dataset;
import annotator.data.Document;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * 
 * Using an initial seed set of paragraphs for each argument, gathers 
 * additional unlabeled paragraphs based on similarity scores. Combines 
 * both initial seed and additional unlabeled into a training set. 
 * (optionally) Performs additional classification based on similarity and/or 
 * machine learning.
 * 
 * One instance of this class should be used for each dataset.
 * 
 * @author conrada@cs.pitt.edu
 *
 */
public class AgglomerativeSimClassifier {
	
    public static StanfordCoreNLP pipeline = null;
	
    

	/**
	 * true -> arguments as classes,
	 * false -> categories as classes
	 */
	protected boolean useArguments;
	
	/**
	 * classes may be either argument or (more likely?) procon.org categories
	 */
	protected List<String> classNames;
	
	/**
	 * maps from a class name (assert: each key is also present in classNames!) 
	 * to a list of all initial text units for the class.
	 */
	protected Map<String, List<String>> classNameToInitList;

	/**
	 * maps from a class name (assert: each key is also present in classNames!) 
	 * to a list of all additional text units for the class. (assert: an empty 
	 * list should initially be added for each class name as the class names 
	 * are added to classNames and classNameToInitList)
	 */
	protected Map<String, List<String>> classNameToAddiList;
	
	/**
	 * metric with which to judge the similarity of any two text units
	 */
	protected AgglomerativeSimMetric simMetric;
	
	
	
	public AgglomerativeSimClassifier(String datasetPath, int simMetricIndex, boolean useArguments) {
		
		// set up stanford pipeline (will probably need this later)
		if (pipeline == null) {

            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit, pos, lemma");
            pipeline = new StanfordCoreNLP(props);

        }
		
		this.useArguments = useArguments;
		
		// set up similarity metric
		
		
		// read, parse doclist
		Dataset activeDataset = Dataset.loadDatasetFromDoclist(new File(datasetPath));
		DataTagset stanceStruct = activeDataset.getTagset();
        DataTag stanceStructRoot = stanceStruct.getTopLevelTag();
        
        classNames = new ArrayList<>();
        classNameToInitList = new HashMap<>();
        classNameToAddiList = new HashMap<>();
        
        List<DataTag> sideTags = stanceStructRoot.getChildTags();

        for (int s = 0; s < sideTags.size(); s++) {

            DataTag sideTag = sideTags.get(s);
            String sideName = sideTag.getAttributes().get("name");
            
            List<DataTag> sideCatTags = sideTag.getChildTags();

            for (int c = 0; c < sideCatTags.size(); c++) {

                DataTag sideCatTag = sideCatTags.get(c);
                String catName = sideCatTag.getAttributes().get("name");
                String catPlusSideName = sideName + ": " + catName;
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
            }
            
            
        }
		
		
		// build inital list of classes, read initial text units
		
        
        
        
        
        
        

        List<Document> documents = activeDataset.getDocuments();
		
		
		
	}
	
	/**
	 * Constructs the initial lists of classes, text instances using the 
	 * currently active doclist. 
	 * 
	 * 
	 */
	private void buildInitLists() {
		
		
	}
	
	/**
	 * Constructs the additional lists of text units based on similarity 
	 * to the initial text units. 
	 * 
	 * 
	 */
	public void buildAddiLists(double simThreshold) {
		
		
		
	}
	
	
	
	
	

}
