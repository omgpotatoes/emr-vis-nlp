
package annotator.documents;

/**
 * Simple identifier for different webpage formats. Extraction of content will 
 * depend on format utilized for particular site.
 *
 * @author alexander.p.conrad@gmail.com
 */
public enum WebsiteContentFormat {
    
    TOWNHALL ("article-body", true, true),
    REDSTATE ("entry", true, true),
    HUFFPOST ("entry_body_text", true, true), 
    ALTERNET ("body_", true, true),
    FIREDOGLAKE ("postContent", true, true);
    
    private final String divClass;  // most pages simply use a unique divClass
    private final boolean usesRegularP;  // most pages use <p> tags similarly
    private final boolean usesRegularDiv;  // most pages use <div> tags similarly
    // for pages with usesRegularDiv, can find content by looking for <divs> with 
    //  class="divClass"; be warned, may be other junk in the <div>! (ie, ids);
    //  no guarantee that class is first item!

    
    private WebsiteContentFormat(String divClass, boolean usesRegularP, boolean usesRegularDiv) {
        this.divClass = divClass;
        this.usesRegularP = usesRegularP;
        this.usesRegularDiv = usesRegularDiv;
    }

    public String getDivClass() {
        return divClass;
    }

    public boolean usesRegularDiv() {
        return usesRegularDiv;
    }

    public boolean usesRegularP() {
        return usesRegularP;
    }
    
    
    
}
