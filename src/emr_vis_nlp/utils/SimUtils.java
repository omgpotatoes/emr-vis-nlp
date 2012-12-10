
package emr_vis_nlp.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility functions for computing similarity scores.
 * 
 * @author alexander.p.conrad@gmail.com
 */
public class SimUtils {
    
    public static double computeVectorMagnitude(Map<String, Integer> termCountMap) {
        
        Set<String> termSet = termCountMap.keySet();
        int sum = 0;
        for (String term : termSet) {
            int termCount = termCountMap.get(term);
            sum += Math.pow(termCount,2);
        }
        return Math.sqrt(sum);
        
    }
    
    public static double computeVectorMagnitude(List<Double> termCountList) {
        
        double sum = 0.;
        for (int i=0; i<termCountList.size(); i++) {
            double termCount = termCountList.get(i);
            sum += Math.pow(termCount,2);
        }
        return Math.sqrt(sum);
        
    }
    
    public static double computeDotProduct(Map<String, Integer> termCountMap1, Map<String, Integer> termCountMap2) {
        
        // 1. build list of all terms from both maps
        Map<String, Boolean> termMap = new HashMap<>();
        Set<String> map1Keys = termCountMap1.keySet();
        Set<String> map2Keys = termCountMap2.keySet();
        
        for (String key : map1Keys) {
            if (!termMap.containsKey(key)) {
                termMap.put(key, true);
            }
        }
        for (String key : map2Keys) {
            if (!termMap.containsKey(key)) {
                termMap.put(key, true);
            }
        }
        
        Set<String> keys = termMap.keySet();
        double sum = 0;
        for (String key : keys) {
            
            double map1Val = 0;
            double map2Val = 0;
            if (termCountMap1.containsKey(key)) {
                map1Val = termCountMap1.get(key);
            }
            if (termCountMap2.containsKey(key)) {
                map2Val = termCountMap2.get(key);
            }
            
            double prod = map1Val*map2Val;
            
            sum += prod;
            
        }
        
        return sum;
        
    }
    
    public static double computeDotProduct(List<Double> termCountList1, List<Double> termCountList2) {
        
        assert termCountList1.size() == termCountList2.size();
        
        
        double sum = 0;
        for (int i=0; i<termCountList1.size(); i++) {
            
            double list1Val = termCountList1.get(i);
            double list2Val = termCountList2.get(i);
            
            double prod = list1Val*list2Val;
            
            sum += prod;
            
        }
        
        return sum;
        
    }
    
    public static double computeCosineSim(List<Double> termCountList1, List<Double> termCountList2) {
        
        double d1VecMag = SimUtils.computeVectorMagnitude(termCountList1);
        double d2VecMag = SimUtils.computeVectorMagnitude(termCountList2);
        
        double dotProd = SimUtils.computeDotProduct(termCountList1, termCountList2);
        double sim = dotProd / (d1VecMag * d2VecMag);

        // debug
//        System.out.println("debug: dotProd="+dotProd+", d1VecMag="+d1VecMag+", d2VecMag="+d2VecMag+", cosineSim="+sim);

        return sim;

        
    }
    
}
