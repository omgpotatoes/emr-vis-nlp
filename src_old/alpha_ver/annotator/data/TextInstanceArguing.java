package annotator.data;

import annotator.data.TextInstanceArguing;
import annotator.data.TextInstance;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * Represents a single paragraph within an arguing subjectivity document.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class TextInstanceArguing extends TextInstance {

    /**
     * Creates a new empty text unit (ie, paragraph)
     * 
     */
    public TextInstanceArguing() {
        super();
        
        attributes.put("side", "");
//        attributes.put("aspect", "");
        attributes.put("category", "");
        attributes.put("argument", "");
        attributes.put("unclear", "");
        
        textStr = "";
        
    }

    /**
     * Creates a new TextInstanceArguing (ie, paragraph) from a node read 
     * from an XML Document.
     *
     * @param el node from source XML doc
     */
    public TextInstanceArguing(Element el) {
        super();

        // get attributes (if not present, will return empty str)
        attributes.put("side", el.getAttribute("side"));
//        attributes.put("aspect", el.getAttribute("aspect"));
        attributes.put("category", el.getAttribute("category"));
        attributes.put("argument", el.getAttribute("argument"));
        attributes.put("unclear", el.getAttribute("unclear"));

        // get text value
        textStr = el.getFirstChild().getNodeValue().trim();

        // debug
        //System.out.println("debug: building new TextInstanceArguing: " + toString());

    }

    public static List<TextInstance> buildTextInstancesArguing(Element el) {

        List<TextInstance> textInstances = new ArrayList<>();

        NodeList textNodes = el.getElementsByTagName(TEXT_TYPE_ARGSUBJ);
        if (textNodes != null && textNodes.getLength() > 0) {
            for (int n = 0; n < textNodes.getLength(); n++) {
                Element textNode = (Element) textNodes.item(n);
                TextInstanceArguing textInstance = new TextInstanceArguing(textNode);
                textInstances.add(textInstance);
            }
        } else {
            // no docs are present in doclist; error
            System.err.println("TextInstanceArguing: doclist empty, has no nodes of type \"" + TEXT_TYPE_ARGSUBJ + "\"");

        }

        return textInstances;

    }

    @Override
    public String toString() {

        return "TextInstance: [\"" + textStr + "\", sideStr=\"" + attributes.get("side") + "\", argumentStr=\"" + attributes.get("argument") + "\", unclearStr=\"" + attributes.get("unclear") + "\"]";

    }
}
