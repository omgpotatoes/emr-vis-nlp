
package annotator.data;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author alexander.p.conrad@gmail.com
 */
public class TextInstance {
    
    public static final String TEXT_TYPE_ARGSUBJ = "InstanceArguing";
    
    protected String textStr;
    protected Map<String, String> attributes;
    
    public TextInstance() {
        textStr = "";
        attributes = new HashMap<>();
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getTextStr() {
        return textStr;
    }

    public void setTextStr(String textStr) {
        this.textStr = textStr;
    }
    
    
    
    
    
}
