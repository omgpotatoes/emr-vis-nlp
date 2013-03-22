
package annotator.data;

import annotator.data.DataTag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Optional secondary datafile containing relevant tags, formatting info, etc. 
 * (ie, stance structure, argument list, ...)
 *
 * @author alexander.p.conrad@gmail.com
 */
public abstract class DataTagset {
    
    public static final String TAGSET_TYPE_STANCESTRUCT = "stancestruct";
    
    protected DataTag topLevelTag;
    protected String dataset;
    protected Map<String, String> attributes;
    protected String rawName;
    
    public DataTagset() {
        
        topLevelTag = null;
        attributes = new HashMap<>();
        dataset = "";
        rawName = "";
        
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getDataset() {
        return dataset;
    }

    public DataTag getTopLevelTag() {
        return topLevelTag;
    }
    
    public String getRawName() {
        return rawName;
    }
    
}
