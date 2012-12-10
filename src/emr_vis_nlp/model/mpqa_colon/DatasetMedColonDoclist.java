
package emr_vis_nlp.model.mpqa_colon;

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
        
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
            // document root
            org.w3c.dom.Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Dataset");
            rootElement.setAttribute("type", DATASET_TYPE_COLON);
            rootElement.setAttribute("name", name);
//            Set<String> docAttrKeys = attributes.keySet();
//            for (String key : docAttrKeys) {
//                rootElement.setAttribute(key, attributes.get(key));
//            }
            doc.appendChild(rootElement);
            
            // tagset node
            //  no tagset in colonoscopy data
//            Element tagsetNode = doc.createElement("DataTagset");
//            tagsetNode.appendChild(doc.createTextNode(tagset.getRawName()));
//            rootElement.appendChild(tagsetNode);
            
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
            Map<String, String> vars = medDoc.getVars();
            for (String key : vars.keySet()) {
                if (!allVarsAndIndisMap.containsKey(key)) {
                    allVarsAndIndisMap.put(key, true);
                    allVarsAndIndis.add(key);
                }
            }
            
            Map<String, Integer> indicators = medDoc.getIndicators();
            for (String key : indicators.keySet()) {
                if (!allVarsAndIndisMap.containsKey(key)) {
                	// debug
                	//System.out.println("debug: adding attr name \""+key+"\" to allVarsAndIndis");
                    allVarsAndIndisMap.put(key, true);
                    allVarsAndIndis.add(key);
                }
            }
            
        }
        
        return allVarsAndIndis;
        
    }
    
    /**
     * 
     * Returns a fixed list of vars and indis assigned beforehand, based on 
     * prior knowledge of what vars/indis we care about
     * 
     * @return
     */
    public List<String> getSelectedVarsAndIndis() {
        
        // TODO remove this ad-hoc attribute selection, replace with more generalizable approach
    	
    	List<String> selectedVarAndIndiList = new ArrayList<>();
    	
    	selectedVarAndIndiList.add("Indicator_19");
    	selectedVarAndIndiList.add("Indicator_16");
    	selectedVarAndIndiList.add("Indicator_2");
    	selectedVarAndIndiList.add("Indicator_17");
    	selectedVarAndIndiList.add("Indicator_3.1");
    	selectedVarAndIndiList.add("Indicator_11");
    	selectedVarAndIndiList.add("Indicator_21");
    	selectedVarAndIndiList.add("VAR_Withdraw_time");
    	selectedVarAndIndiList.add("VAR_Procedure_aborted");
    	selectedVarAndIndiList.add("VAR_ASA");
    	selectedVarAndIndiList.add("VAR_Prep_adequate");
    	selectedVarAndIndiList.add("VAR_Indication_type");
    	selectedVarAndIndiList.add("VAR_Nursing_Reports");
    	selectedVarAndIndiList.add("VAR_Informed_consent");
    	selectedVarAndIndiList.add("VAR_Cecum_(reached_it)");
    	selectedVarAndIndiList.add("VAR_Indication_Type_3");
    	selectedVarAndIndiList.add("VAR_Indication_Type_2");
    	selectedVarAndIndiList.add("VAR_Any_adenoma");
    	selectedVarAndIndiList.add("VAR_cecal_landmark");
    	selectedVarAndIndiList.add("VAR_Biopsy");
    	
    	return selectedVarAndIndiList;
    	
    }
    
}
