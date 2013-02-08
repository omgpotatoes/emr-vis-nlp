
package annotator.annotator;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Simple tuple class for use with JTreeVarCorrs. Stores a var/indi name, 
 * list of related var/indis, correlation with parent.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class VarIndiCorr {
    
    protected String name;
    protected VarIndiCorr parent;
    protected double globalCorr;
    protected List<VarIndiCorr> relatedVarIndis;
    protected boolean isLeaf;  // only display corrs if true
    
    public VarIndiCorr(String name, VarIndiCorr parent) {
        this.name = name;
        this.parent = parent;
        isLeaf = false;
        globalCorr = 0.0;
        relatedVarIndis = new ArrayList<>();
        
    }
    
    public VarIndiCorr(String name, VarIndiCorr parent, double globalCorr) {
        this.name = name;
        this.parent = parent;
        isLeaf = true;
        this.globalCorr = globalCorr;
        relatedVarIndis = new ArrayList<>();
    }
    
    public void addRelatedVarIndi(VarIndiCorr varIndiCorr) {
        relatedVarIndis.add(varIndiCorr);
    }
    
    @Override
    public String toString() {
    	
    	if (!isLeaf) {
    		return name;
    	} else {
    		String line = name +":  global_correlation="+globalCorr+",  selection_correlation=";
    		return line;
    	}
    	
    }
    
}
