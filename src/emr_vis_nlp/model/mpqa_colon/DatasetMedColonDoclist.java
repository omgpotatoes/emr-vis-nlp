
package emr_vis_nlp.model.mpqa_colon;

import emr_vis_nlp.model.Document;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;

/**
 * 
 * Doclist for med report datasets.
 * (adopted from 1st prototype emr explorer)
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DatasetMedColonDoclist extends Dataset {
    
    // path to the root of the MPQA-style "database"
    protected String databaseRoot;
    protected List<String> allVarsAndIndis;
    protected List<String> allAttributeNames;
    
    public DatasetMedColonDoclist(Element el, String rootPath, String path) {
        super(path);
        rootDir = rootPath;
        databaseRoot = el.getAttribute("databaseroot").trim();
        documents = DocumentMedColon.buildDatasetMed(el, rootPath, databaseRoot);
//        isActive = true;
        name = el.getAttribute("name").trim();
        type = DATASET_TYPE_COLON;
        allVarsAndIndis = null;
    }
    
    @Override
    public void writeDoclist() {
        
        // build the XML of the doclist
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
            // document root
            org.w3c.dom.Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Dataset");
            rootElement.setAttribute("type", DATASET_TYPE_COLON);
            rootElement.setAttribute("name", name);
            doc.appendChild(rootElement);
            
            // tagset node
            //  no tagset in colonoscopy data
            
            // document nodes
            for (Document document : documents) {
                
                Element docElement = doc.createElement("Document");
                String docPath = document.getRawName();
                if (docPath.equals("")) {
                    docPath = document.getDocPath();
                }
                docElement.appendChild(doc.createTextNode(docPath));
                rootElement.appendChild(docElement);
                
            }
            
            // write to original file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));

            // Output to console for testing
            StreamResult streamResult = new StreamResult(System.out);

            transformer.transform(source, result);

            System.out.println("doclist saved to "+path);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }

    }
    
    public List<String> getAllVarsAndIndis() {
        
        if (allVarsAndIndis != null) {
            return allVarsAndIndis;
        }
        
        // loop through all docs, keeping track of keys for both 
        allVarsAndIndis = new ArrayList<>();
        Map<String, Boolean> allVarsAndIndisMap = new HashMap<>();
        
        for (Document doc : documents) {
            
            DocumentMedColon medDoc = (DocumentMedColon)doc;
            
            Map<String, String> attributes = medDoc.getAttributes();
            for (String key : attributes.keySet()) {
                if (!allVarsAndIndisMap.containsKey(key)) {
                    allVarsAndIndisMap.put(key, true);
                    allVarsAndIndis.add(key);
                }
            }
            
        }
        
        return allVarsAndIndis;
        
    }
    
}
