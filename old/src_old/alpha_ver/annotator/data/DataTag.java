
package annotator.data;

import annotator.data.DataTag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a tag from some DataTagset.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DataTag {
    
    protected String type;
    protected String text;
    protected Map<String, String> attributes;
    protected List<DataTag> childTags;
    protected DataTag parentTag;
    
    public DataTag(DataTag parentTag) {
        
        type = "";
        text = "";
        attributes = new HashMap<>();
        this.parentTag = parentTag;
        childTags = new ArrayList<>();
        
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<DataTag> getChildTags() {
        return childTags;
    }

    public DataTag getParentTag() {
        if (parentTag != null) {
            return parentTag;
        }
        return null;
    }
    
    public boolean hasParentTag() {
        if (parentTag != null) {
            return true;
        }
        return false;
    }
    
    public String getElaborationText() {
        
        String elabText = "";
        if (attributes.containsKey("elaboration")) {
            elabText += attributes.get("elaboration") + "\n\n";
        }
        elabText += text;
        
        return elabText;
        
    }
    
    
}
