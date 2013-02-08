package annotator.data;

import annotator.data.DataTag;
import annotator.data.DataTagStanceStruct;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Represents a tag from a stance structure.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DataTagStanceStruct extends DataTag {
    
    // list of var names that we care about
    protected List<String> taglistNames;
    
    public DataTagStanceStruct(Element el, List<String> taglistNames) {
        this(el, null, taglistNames);
    }

    public DataTagStanceStruct(Element el, DataTagStanceStruct parentTag, List<String> taglistNames) {
        super(parentTag);
        
        this.taglistNames = taglistNames;

        type = el.getAttribute("type");
        attributes.put("name", el.getAttribute("name"));
        attributes.put("elaboration", el.getAttribute("elaboration"));
        text = el.getTextContent();
        
        // debug
        //System.out.println("debug: " + this.toStringVerbose());

        // now, build child nodes
        NodeList nl = el.getChildNodes();
        for (int i = 1; i < nl.getLength(); i += 2) {

            Element childNode = (Element) nl.item(i);

            if (childNode != null) {

                childTags.add(new DataTagStanceStruct(childNode, this, taglistNames));

            }

        }

    }

    @Override
    public String toString() {
        String str = "";
        if (!type.equals("") && attributes.containsKey("name")) {
            str = type+": "+attributes.get("name");
        } else if (!type.equals("")) {
            str = type;
        } else {
            str = "(unknown)";
        }
        return str;
    }
    
    public String toStringVerbose() {

        String str = "";
        str += "DataTagStanceStruct: [" + "type=" + type + ", name=" + attributes.get("name") + ", elaboration=" + attributes.get("elaboration") + "]";
        return str;

    }
    
    public List<String> getTaglistNames() {
        return taglistNames;
    }
    
    
    
    
    

    public static DataTag buildTagsStanceStruct(Element structRoot) {
        
        // first, read taglist
        //  purpose of taglist: determines what cols should be displayed in 
        //  text instance chart only; tree label chooser unaffected by this
        NodeList taglist = structRoot.getElementsByTagName("Taglist");
        
        List<String> taglistNames = new ArrayList<>();
        
        if (taglist != null && taglist.getLength() == 1) { // should only execute once
            Element taglistNode = (Element) taglist.item(0);
            NodeList taglistChildren = taglistNode.getChildNodes();
            while (taglistChildren.getLength() != 0) {
                // debug
//                System.out.println("debug: taglistChildren.getLength()="+taglistChildren.getLength());
//                for (int i=0; i<taglistChildren.getLength(); i++) {
//                    System.out.println("debug: "+taglistChildren.item(i).toString());
//                }
                // 0 == text, 1 == node, 2 == text

                taglistNode = (Element) taglistChildren.item(1);
                if (taglistNode != null) {
                    String taglistName = taglistNode.getAttribute("type");
                    // debug
                    System.out.println("debug: DataTagStanceStruct: adding tagName \"" + taglistName + "\"");
                    taglistNames.add(taglistName);
                    taglistChildren = taglistNode.getChildNodes();
                } else {
                    break;
                }
            }
        } else if (taglist != null && taglist.getLength() > 1) {
            assert false;
            System.err.println("DataTagStanceStruct: multiple taglists detected in stance structure!");
        } else {
            assert false;
            System.err.println("DataTagStanceStruct: no taglist detected in stance structure!");
        }

        // now, read tags
        //  purpose of tags: tree label chooser
        NodeList tagroot = structRoot.getElementsByTagName("Tagroot");
        DataTag topLevelDataTag = null;
        // should only contain 1 node
        if (tagroot != null && tagroot.getLength() == 1) { // should only execute once
            Element tagrootNode = (Element) tagroot.item(0);

            topLevelDataTag = new DataTagStanceStruct(tagrootNode, taglistNames);
//
//            NodeList tagrootChildren = tagrootNode.getChildNodes();
//
//            // for each node, we should build a separate DataTag; 
//            // start at 1, increment +2
//
//            System.out.println("debug: taglistChildren.getLength()=" + tagrootChildren.getLength());
//            for (int i = 1; i < tagrootChildren.getLength(); i += 2) {
//                System.out.println("debug: " + tagrootChildren.item(i).toString());
//
//                Element childTag = (Element) tagrootChildren.item(i);
//
//                DataTag dataTag = new DataTagStanceStruct(childTag);
//
//            }

        } else if (tagroot != null && tagroot.getLength() > 1) {
            assert false;
            System.err.println("DataTagStanceStruct: multiple tagroots detected in stance structure!");
        } else {
            assert false;
            System.err.println("DataTagStanceStruct: no tagroot detected in stance structure!");
        }

        // read in all docs from xml
//        String doclistRootName = doclistRoot.getTagName().trim().toLowerCase();
//        NodeList documentNodes = doclistRoot.getElementsByTagName("Document");
//        List<Document> documentList = new ArrayList<>();
//        if (documentNodes != null && documentNodes.getLength() > 0) {
//            for (int n=0; n<documentNodes.getLength(); n++) {
//                Element documentNode = (Element)documentNodes.item(n);
//                DocumentArguing document = new DocumentArguing(documentNode, rootPath);
//                documentList.add(document);
//            }
//        } else {
//            // no docs are present in doclist; error
//            System.err.println("DocumentArguing: doclist empty, has no nodes of type \"Document\"");
//            
//        }
//        
//        return documentList;



        //return dataTags;
        return topLevelDataTag;

    }
    
}
