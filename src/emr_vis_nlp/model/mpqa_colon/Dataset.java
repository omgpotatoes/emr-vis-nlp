package emr_vis_nlp.model.mpqa_colon;

import emr_vis_nlp.model.Document;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * Represents a set of documents loaded from disk. Is optionally accompanied by
 * a schema datafile.
 *
 * @author alexander.p.conrad@gmail.com
 */
public abstract class Dataset {

//    public static String DATASET_TYPE_ARGSUBJ = "datasetarguing";
    public static String DATASET_TYPE_COLON = "datasetcolonoscopy";
    protected List<Document> documents;
//    protected boolean isActive = false;
    protected String name = "";
    protected String path = "";
    protected String rootDir = "";
    protected String type = "";

    public Dataset(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public String getPath() {
        return path;
    }

    public String getRootDir() {
        return rootDir;
    }

    public String getType() {
        return type;
    }
    
    public List<String> getAllAttributesFromDocs() {
        
        // loop through all docs, keeping track of keys for both 
        List<String> allAttrs = new ArrayList<>();
        Map<String, Boolean> allAttrsMap = new HashMap<>();
        
        // ensure that "name" attr is 1st
        allAttrs.add("name");
        allAttrsMap.put("name", true);
        
        for (Document doc : documents) {
            
            Map<String, String> attributes = doc.getAttributes();
            for (String key : attributes.keySet()) {
                if (!allAttrsMap.containsKey(key)) {
                    allAttrsMap.put(key, true);
                    allAttrs.add(key);
                }
            }
            
        }
        
        return allAttrs;
        
    }

    public abstract void writeDoclist();
    
    public static Dataset loadDatasetFromDoclist(File doclistFile) {
        return loadDatasetFromDoclist(doclistFile.getAbsolutePath());
    }

    public static Dataset loadDatasetFromDoclist(String doclistFilePath) {

        Dataset dataset = null;

        // store root path
        String doclistRootPath = new File(doclistFilePath).getParent() + "/";
        // debug
        System.out.println("debug: doclist directory is \"" + doclistRootPath + "\"");

        // load doclist file for reading
        org.w3c.dom.Document dom = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(doclistFilePath);
        } catch (ParserConfigurationException | SAXException | IOException pce) {
            pce.printStackTrace();
        }

        // document root
        Element doclistRoot = dom.getDocumentElement();
        String doclistTypeName = doclistRoot.getAttribute("type").trim().toLowerCase();
        if (doclistTypeName.equals(DATASET_TYPE_COLON)) {
            // type is arguing, call appropriate builders
            // debug
            System.out.println("debug: doclist type is \"" + DATASET_TYPE_COLON + "\"");
            dataset = new DatasetMedColonDoclist(doclistRoot, doclistRootPath, doclistFilePath);

        } else {
            // type is not recognized
            System.err.println("Doclist.loadDatasetFromDoclist: doclist type \"" + doclistTypeName + "\" not recognized");

        }

        return dataset;

    }
    
}
