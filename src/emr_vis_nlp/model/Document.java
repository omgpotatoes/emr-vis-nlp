package emr_vis_nlp.model;

import emr_vis_nlp.model.mpqa_colon.TextInstance;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single unit of data (ie, records for 1 patient, single arguing
 * document, etc.). Will generally be decomposed to finer granularity (ie,
 * multiple EMRs, multiple paragraphs, etc.)
 *
 * @author alexander.p.conrad@gmail.com
 */
public abstract class Document {

    public static String DOCUMENT_TYPE_COLON = "documentcolonoscopy";
    
    protected List<TextInstance> textInstances;
    protected Map<String, String> attributes;
    
    protected boolean isActive = false;
    protected String name = "";
    // rawName = what's literally in the file; use this if present instead of rebuilding
    protected String rawName = "";
    protected String docPath = "";
    // contains version of document which has been processed by Stanford parser
    protected String parsedText;

    public Document() {
        isActive = false;
        name = "";
        rawName = "";
        attributes = new HashMap<>();
        docPath = "";
        textInstances = new ArrayList<>();
        parsedText = "";
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDocPath(String docPath) {
        this.docPath = docPath;
    }

    /**
     *
     * @return all of the "TextInstances" (ie, paragraphs) in this document.
     */
    public List<TextInstance> getTextInstances() {
        return textInstances;
    }

    public String getRawName() {
        return rawName;
    }

    public String getDocPath() {
        return docPath;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getText() {

        String text = "";
        for (TextInstance instance : textInstances) {
            text += instance.getTextStr() + "\n";
        }
        return text;

    }
    
    // parsing is no longer a concern of this class; see previous prototype for parsing code

}
