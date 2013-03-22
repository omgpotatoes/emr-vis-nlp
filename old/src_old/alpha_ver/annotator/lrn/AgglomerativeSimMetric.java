package annotator.lrn;


/**
 * interface for generic similarity metric to be used with 
 * AgglomerativeSimClassifier and its descendants. Judges the similarity 
 * of any two arbitraty text units.
 * 
 * @author conrada@cs.pitt.edu
 *
 */
public interface AgglomerativeSimMetric {

	public double computeSimilarity(String text1, String text2);
	
}
