
package annotator.data;

import annotator.data.TextInstance;

/**
 * 
 * Represents a single medical record (report or pathology) for the biovis 
 * colonoscopy dataset.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class TextInstanceMedColon extends TextInstance {
    
    protected String type;
    
    /**
     * Creates a new empty text unit (ie, medical record)
     * 
     */
    public TextInstanceMedColon() {
        super();
        
        type = "unknown";
        textStr = "";
        
    }
    
    
    public TextInstanceMedColon(String type, String text) {
        super();
        
        this.type = type;
        textStr = text;
        
    }

    @Override
    public String toString() {
        return "TextInstanceMedColon{" + "type=" + type + ", textStr=\""+textStr+"\"}";
    }
    
    
}
