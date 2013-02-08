package annotator.data;

import annotator.data.DataTagset;
import annotator.data.DataTagStanceStruct;
import annotator.data.DataTagsetStanceStruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * DataTagset representing a stance structure for use with an arguing dataset.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DataTagsetStanceStruct extends DataTagset {

    public DataTagsetStanceStruct(Element el, String rootPath) {

        // open document listed in 
        // simply contains path to XML for this document; open
        String docPath = el.getFirstChild().getNodeValue().trim();
        rawName = docPath;
        if (docPath.charAt(0) != '\\' && docPath.charAt(0) != '/' && docPath.charAt(1) != ':') {
            docPath = rootPath + docPath;
        }
        // debug
        System.out.println("debug: reading stance structure: \"" + docPath + "\"");

        // load document file for reading
        org.w3c.dom.Document dom = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(docPath);

//            Reader reader = new InputStreamReader(new FileInputStream(new File(docPath)), "UTF-8");
//            InputSource is = new InputSource(reader);
//            is.setEncoding("UTF-8");
//            
//            SAXParserFactory factory = SAXParserFactory.newInstance();
//            SAXParser saxParser = factory.newSAXParser();
//            saxParser.parse()

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // stance struct root
        Element structRoot = dom.getDocumentElement();
        String structTypeName = structRoot.getAttribute("type").trim().toLowerCase();
        if (structTypeName.equals(TAGSET_TYPE_STANCESTRUCT)) {
            // type is arguing, process nodes accordingly
            // debug
            System.out.println("debug: stance struct type is \"" + TAGSET_TYPE_STANCESTRUCT + "\"");

            // read metadata
            dataset = structRoot.getAttribute("dataset").trim();
            attributes.put("comment", structRoot.getAttribute("comment").trim());
            // debug
            System.out.println("debug: comment=\"" + attributes.get("comment") + "\"");

            // process text instance nodes
            topLevelTag = DataTagStanceStruct.buildTagsStanceStruct(structRoot);



        } else {
            // type is not recognized
            System.err.println("DataTagsetStanceStruct: tagset type \"" + structTypeName + "\" not recognized");

        }



    }

    public static DataTagset buildStanceStructure(Element doclistRoot, String rootPath) {

        DataTagset tagset = null;

        // debug
        System.out.println("debug: building stance struct");

        // find reference to DataTagset in doclist
        String doclistRootName = doclistRoot.getTagName().trim().toLowerCase();
        NodeList tagsetNodes = doclistRoot.getElementsByTagName("DataTagset");
        if (tagsetNodes != null && tagsetNodes.getLength() == 1) {  // should only be 1 tagset entry
            for (int n = 0; n < tagsetNodes.getLength(); n++) {
                Element tagsetNode = (Element) tagsetNodes.item(n);
                tagset = new DataTagsetStanceStruct(tagsetNode, rootPath);
            }
        } else {
            if (tagsetNodes.getLength() == 0) {
                // no tagset present; guess we will be building a new one
                System.out.println("debug: no tagset present in doclist; initializing new tagset");
            } else if (tagsetNodes.getLength() > 1) {
                System.err.println("DataTagsetStanceStruct.buildStanceStructure: multiple tagset nodes present: " + tagsetNodes.getLength());
            }

        }

        return tagset;

    }
}
