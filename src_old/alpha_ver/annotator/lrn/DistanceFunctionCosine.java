
package annotator.lrn;

import java.util.Enumeration;
import java.util.Map;
import weka.core.Attribute;
import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.PerformanceStats;

/**
 *
 * @author conrada
 */
public class DistanceFunctionCosine implements DistanceFunction {

    private Instances instances;
    private Map<String, Integer> datasetTermCounts;
    private boolean invertSelection;
    
    public DistanceFunctionCosine(Map<String, Integer> datasetTermCounts) {
        super();
        this.datasetTermCounts = datasetTermCounts;
        invertSelection = false;
    }
    
    @Override
    public void setInstances(Instances i) {
        instances = i;
    }

    @Override
    public Instances getInstances() {
        return instances;
    }

    @Override
    public void setAttributeIndices(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getAttributeIndices() {
        if (instances == null) {
            return "";
        } else {
            return "0-"+instances.numAttributes();
        }
    }

    @Override
    public void setInvertSelection(boolean bln) {
        
        invertSelection = bln;
        
    }

    @Override
    public boolean getInvertSelection() {
        
        return invertSelection;
        
    }

    @Override
    public double distance(Instance instnc, Instance instnc1) {
        
        // @TODO assumes that all attrs are terms; should fix this eventually
        // @TODO handle invertSelection (?) properly
        int numAttrs = instnc.numAttributes();
        double inst1VecSum = 0;
        double inst2VecSum = 0;
        double prod = 0;
        for (int a=0; a<numAttrs; a++) {
            float inst1Val = (float)instnc.value(a);
            float inst2Val = (float)instnc1.value(a);
            prod += inst1Val * inst2Val;
            inst1VecSum += Math.pow(inst1Val, 2);
            inst2VecSum += Math.pow(inst2Val, 2);
        }
        
        double norm = Math.sqrt(inst1VecSum) * Math.sqrt(inst2VecSum);
        
        double sim = prod / norm;
        
        // debug
        //System.out.println("debug: dotProd="+prod+", d1VecMag="+Math.sqrt(inst1VecSum)+", d2VecMag="+Math.sqrt(inst2VecSum)+", cosineSim="+sim);
        
        // perhaps distances are just too small? need integer-level?
        sim *= 10000;
        
        return sim;
        
    }

    @Override
    public double distance(Instance instnc, Instance instnc1, PerformanceStats ps) throws Exception {
        
        // @TODO properly handle PerformanceStats
        return distance(instnc, instnc1);
        
    }

    @Override
    public double distance(Instance instnc, Instance instnc1, double cutoff) {
        
        double dist = distance(instnc, instnc1);
        if (dist > cutoff) {
            return Double.POSITIVE_INFINITY;
        }
        return dist;
        
    }

    @Override
    public double distance(Instance instnc, Instance instnc1, double cutoff, PerformanceStats ps) {
        
        // @TODO properly handle PerformanceStats
        double dist = distance(instnc, instnc1);
        if (dist > cutoff) {
            return Double.POSITIVE_INFINITY;
        }
        return dist;
        
    }

    @Override
    public void postProcessDistances(double[] doubles) {
        
        // shouldn't need to do anything here
        
    }

    @Override
    public void update(Instance instnc) {
        
        // @TODO should technically update the backing document counts
        
    }

    @Override
    public Enumeration listOptions() {
        
        // @TODO should add options someday
        return null;
        
    }

    @Override
    public void setOptions(String[] strings) throws Exception {
        
        // @TODO should add options someday
        
    }

    @Override
    public String[] getOptions() {
        
        // @TODO should add options someday
        return new String[0];
        
    }
    
}
