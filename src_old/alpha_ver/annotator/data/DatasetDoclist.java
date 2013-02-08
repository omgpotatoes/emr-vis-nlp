
package annotator.data;

import annotator.data.Dataset;
import annotator.data.Document;
import annotator.data.DataTagsetStanceStruct;
import annotator.data.DocumentArguing;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * 
 * Encapsulates dataset for a doclist of arguing-type documents
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DatasetDoclist extends Dataset {
    
    public DatasetDoclist(Element el, String rootPath, String path) {
        super(path);
        rootDir = rootPath;
        documents = DocumentArguing.buildDatasetArguing(el, rootPath);
        tagset = DataTagsetStanceStruct.buildStanceStructure(el, rootPath);
        isActive = true;
        name = el.getAttribute("name").trim();
        type = DATASET_TYPE_ARGSUBJ;
    }
    
    
    @Override
    public void writeDoclist() {
        
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
            // document root
            org.w3c.dom.Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Dataset");
            rootElement.setAttribute("type", DATASET_TYPE_ARGSUBJ);
            rootElement.setAttribute("name", name);
//            Set<String> docAttrKeys = attributes.keySet();
//            for (String key : docAttrKeys) {
//                rootElement.setAttribute(key, attributes.get(key));
//            }
            doc.appendChild(rootElement);
            
            // tagset node
            Element tagsetNode = doc.createElement("DataTagset");
            tagsetNode.appendChild(doc.createTextNode(tagset.getRawName()));
            rootElement.appendChild(tagsetNode);
            
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
        
    
    /**
     * for testing only; this method should be disabled before distribution
     * @param args 
     */
    public static void main(String[] args) {
        String doclistPath = "D:/Users/conrada/Dropbox/healthcare_data/devel3_doclist.xml";
        Dataset dataset = loadDatasetFromDoclist(doclistPath);
    }
    
    
}
