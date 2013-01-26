package annotator.explor;

import annotator.explor.ConvertDoclistFilesToTextFiles;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import annotator.data.Dataset;
import annotator.data.DatasetDoclist;
import annotator.data.DatasetMedColonDoclist;
import annotator.data.Document;
import annotator.data.DocumentArguing;

/**
 * Class responsible for cleaning up XML document files, improving plaintext readability, fixing encoding issues, etc.
 * 
 * @author alexander.p.conrad
 *
 */
public class XmlDocCleaner {
	
	
	public static void spruceUpXMLDoc(String pathToXMLDoc) {
		
		System.out.println("sprucing up doc \""+pathToXMLDoc+"\"");
		
		try {
			
			File xmlDocFile = new File(pathToXMLDoc);
			// read original doc, do preprocessing
			Scanner oldDocReader = new Scanner(new FileReader(xmlDocFile));
			String fullDoc = "";
			while (oldDocReader.hasNextLine()) {
				String line = oldDocReader.nextLine();
				line = line.replaceAll(Pattern.quote("\n\t<InstanceArguing"), "<InstanceArguing");
				line = line.replaceAll(Pattern.quote("<InstanceArguing"), "\n\t<InstanceArguing");
//				line = line.replaceAll(Pattern.quote("Q\n\tQ\n\t\n\t\n\t"), "");
//				line = line.replaceAll(Pattern.quote("\n\t<InstanceArguing"), "<InstanceArguing");
//				line = line.replaceAll(Pattern.quote("<InstanceArguing"), "\n\t<InstanceArguing");
				line = ConvertDoclistFilesToTextFiles.fixEncoding(line);
				fullDoc += line+"\n";
			}
			
			oldDocReader.close();
			
			// delete original doc, write processed version of doc
			xmlDocFile.delete();
			
			FileWriter writer = new FileWriter(xmlDocFile);
			writer.write(fullDoc);
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("error while spricing up doc "+pathToXMLDoc);
		}
		
		
	}
	
	public static void spruceUpXMLDocsInXMLDoclist(String pathToXMLDoclist) {
		

        Dataset dataset = null;
        
        // store root path
        String doclistRootPath = new File(pathToXMLDoclist).getParent()+"/";
        // debug
        System.out.println("debug: doclist directory is \""+doclistRootPath+"\"");
        
        // load doclist file for reading
        org.w3c.dom.Document dom = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(pathToXMLDoclist);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        // document root
        Element doclistRoot = dom.getDocumentElement();
        String doclistTypeName = doclistRoot.getAttribute("type").trim().toLowerCase();
        
        String doclistRootName = doclistRoot.getTagName().trim().toLowerCase();
        NodeList documentNodes = doclistRoot.getElementsByTagName("Document");
        List<Document> documentList = new ArrayList<>();
        if (documentNodes != null && documentNodes.getLength() > 0) {
            for (int n=0; n<documentNodes.getLength(); n++) {
                Element documentNode = (Element)documentNodes.item(n);
                //DocumentArguing document = new DocumentArguing(documentNode, doclistRootPath);
                String docPath = documentNode.getFirstChild().getNodeValue().trim();
                //String rawName = docPath;
                if (docPath.charAt(0)!='\\' && docPath.charAt(0)!='/' && docPath.charAt(1)!=':') {
                    docPath = doclistRootPath + docPath;
                }
                spruceUpXMLDoc(docPath);
            }
        } else {
            // no docs are present in doclist; error
            System.err.println("DocumentArguing: doclist empty, has no nodes of type \"Document\"");
            
        }
		
	}
	

	
	public static String fixEncoding(String origStr) {
        origStr = origStr.replaceAll("â€™", "'");
        origStr = origStr.replaceAll("â€“", "-");
        origStr = origStr.replaceAll("â€”", "-");
        origStr = origStr.replaceAll("â€œa", "\"");
        origStr = origStr.replaceAll("â€œ", "\"");
        origStr = origStr.replaceAll("â€&#157", "\"");
        origStr = origStr.replaceAll("â€&", "\"");
        origStr = origStr.replaceAll("â€¦", "...");
        origStr = origStr.replaceAll("Â", " ");
        origStr = origStr.replaceAll("â€²", ":");
        //origStr = origStr.replaceAll("", "");
        return origStr;
    }
	
	public static void main(String[] args) {
		
		spruceUpXMLDocsInXMLDoclist("/afs/cs.pitt.edu/usr0/conrada/private/Dropbox/doclist_deathpenalty_devel.xml");
		
	}

}
