
package annotator.lrn;

import annotator.lrn.TermScoreTuple;
import java.util.List;

/**
 * Simple sortable tuple for representing a term, tf*idf pair
 *
 * @author alexander.p.conrad
 */
public class TermScoreTuple implements Comparable {

    String term;
    double score;

    public TermScoreTuple(String term, double score) {
        this.term = term;
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    @Override
    public int compareTo(Object o) {
//            throw new UnsupportedOperationException("Not supported yet.");
        TermScoreTuple tuple2 = null;
        try {
            tuple2 = (TermScoreTuple) o;
        } catch (ClassCastException e) {
            return -1;
        }
        double score2 = tuple2.getScore();

        if (score2 == score) {
            return 0;
        } else if (score2 > score) {
            return 1;
        }
        return -1;

    }

    @Override
    public String toString() {
        return "TermScoreTuple{" + "term=" + term + ", score=" + score + '}';
    }
    
    
    public static void removeDupTuples(List<TermScoreTuple> tuples, int cutoff) {
    	
    	// strategy: if a term is fully contained within a higher-ranked tuple,
    	//  remove the first term
    	
    	// warning: O(n^2)! use cutoff to keep list at computationally-managable size
    	for (int i=cutoff; i<tuples.size(); i++) {
    		tuples.remove(i);
    	}
    	
    	for (int i=tuples.size()-1; i>0; i--) {
    		String firstNGram = tuples.get(i).getTerm();
    		for (int j=i-1; j>=0; j--) {
    			String secondNGram = tuples.get(j).getTerm();
    			if (secondNGram.contains(firstNGram)) {
    				tuples.remove(i);
    				break;
    			}
    		}
    		
    		
    	}
    	
    	
    }
    
    
}
