
package annotator.data;

import annotator.data.TextInstanceArguing;
import annotator.data.Document;
import annotator.data.TextInstance;
import annotator.data.DocumentArguing;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * Document for use in the paragraph-level arguing subjectivity experiments.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocumentArguing extends Document {
    
    private List<String> bowVector = null;
    private Map<String, Integer> termCountMap = null;
    private Map<String, Double> termTfIdfMap = null;
    int numTermsInDoc = -1;
    
    /**
     * creates a new empty DocumentArguing
     * 
     * @param path where the datafile should be saved
     */
    public DocumentArguing(String name, String path) {
        super();
        
        docPath = path;
        this.name = name;
        textInstances = new ArrayList<>();
        isActive = true;
        
    }
    
    /**
     * creates a new DocumentArguing from an XML file, as specified in a doclist
     * 
     * @param el node in an XML doclist specifying path to XML doc datafile
     * @param rootPath directory in which source XML doclist is contained
     */
    public DocumentArguing(Element el, String rootPath) {
        super();
        
        // simply contains path to XML for this document; open
        docPath = el.getFirstChild().getNodeValue().trim();
        rawName = docPath;
        if (docPath.charAt(0)!='\\' && docPath.charAt(0)!='/' && docPath.charAt(1)!=':') {
            docPath = rootPath + docPath;
        }
        // debug
        System.out.println("debug: reading new document \""+docPath+"\"");
        
        // load document file for reading
        org.w3c.dom.Document dom = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();;
            dom = db.parse(docPath);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        // document root
        Element docRoot = dom.getDocumentElement();
        String docTypeName = docRoot.getAttribute("type").trim().toLowerCase();
        if (docTypeName.equals(DOCUMENT_TYPE_ARGSUBJ)) {
            // type is arguing, process nodes accordingly
            // debug
            //System.out.println("debug: document type is \""+DOCUMENT_TYPE_ARGSUBJ+"\"");
            
            // read metadata
            name = docRoot.getAttribute("name").trim();
            attributes.put("url", docRoot.getAttribute("url").trim());
            attributes.put("date", docRoot.getAttribute("date").trim());
            // debug
            //System.out.println("debug: url=\""+attributes.get("url")+"\", date=\""+attributes.get("date")+"\"");
            
            // process text instance nodes
            textInstances = TextInstanceArguing.buildTextInstancesArguing(docRoot);
            
            
            
        } else {
            // type is not recognized
            System.err.println("DocumentArguing.loadDatasetFromDoclist: document type \""+docTypeName+"\" not recognized");
            
        }
        
        isActive = true;
        
    }
    
    public static List<Document> buildDatasetArguing(Element doclistRoot, String rootPath) {
        
        // read in all docs from xml
        String doclistRootName = doclistRoot.getTagName().trim().toLowerCase();
        NodeList documentNodes = doclistRoot.getElementsByTagName("Document");
        List<Document> documentList = new ArrayList<>();
        if (documentNodes != null && documentNodes.getLength() > 0) {
            for (int n=0; n<documentNodes.getLength(); n++) {
                Element documentNode = (Element)documentNodes.item(n);
                DocumentArguing document = new DocumentArguing(documentNode, rootPath);
                documentList.add(document);
            }
        } else {
            // no docs are present in doclist; error
            System.err.println("DocumentArguing: doclist empty, has no nodes of type \"Document\"");
            
        }
        
        return documentList;
        
    }
    
    @Override
    public void writeDoc() {
        
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            
            // document root
            org.w3c.dom.Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Document");
            rootElement.setAttribute("type", DOCUMENT_TYPE_ARGSUBJ);
            Set<String> docAttrKeys = attributes.keySet();
            for (String key : docAttrKeys) {
                rootElement.setAttribute(key, attributes.get(key));
            }
            rootElement.setAttribute("name", name);
            doc.appendChild(rootElement);
            
            // text nodes
            for (TextInstance textInstance : textInstances) {
                
                Element textElement = doc.createElement(TextInstance.TEXT_TYPE_ARGSUBJ);
                Set<String> textElementKeys = textInstance.getAttributes().keySet();
                for (String key : textElementKeys) {
                    textElement.setAttribute(key, textInstance.getAttributes().get(key));
                }
                textElement.appendChild(doc.createTextNode(textInstance.getTextStr()));
                rootElement.appendChild(textElement);
                
            }
            
            // write to original file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(docPath));

            // Output to console for testing
            StreamResult streamResult = new StreamResult(System.out);

            transformer.transform(source, result);

            System.out.println("document "+name+" saved to "+docPath);


        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }

    }
    
    @Override
    public List<String> getBagOfWordsVector() {
        
        if (bowVector != null) {
            return bowVector;
        }
        Map<String, Integer> bowMap = new HashMap<>();
        
        for (TextInstance textInst : textInstances) {
            
            String text = textInst.getTextStr().toLowerCase();
            Scanner textSplitter = new Scanner(text);
            while (textSplitter.hasNext()) {
                String token = textSplitter.next();
                if (!bowMap.containsKey(token)) {
                    bowMap.put(token, 1);
                    bowVector.add(token);
                }
            }
            
        }
        
        return bowVector;
        
    }
    
    @Override
    public Map<String, Integer> getTermCountMap() {
        return getTermCountMap(true);
    }
    
    public Map<String, Integer> getTermCountMap(boolean useCounts) {
        
        if (termCountMap != null) {
            return termCountMap;
        }
        termCountMap = new HashMap<>();
        
        boolean countTerms = false;
        if (numTermsInDoc == -1) {
            numTermsInDoc = 0;
            countTerms = true;
        }
        
        for (TextInstance textInst : textInstances) {
            
            String text = textInst.getTextStr().toLowerCase();
            
            // preprocessing before learning: quick n' durty punctuation parsing
            text = quickPunctPreproc(text);
            
            Scanner textSplitter = new Scanner(text);
            while (textSplitter.hasNext()) {
                String token = textSplitter.next();
                if (countTerms) {
                    numTermsInDoc++;
                }
                
                if (!termCountMap.containsKey(token)) {
                    termCountMap.put(token, 1);
                } else {
                    if (useCounts) {
                        termCountMap.put(token, termCountMap.get(token)+1);
                    } else {
                        // val of 1 is already stored, so don't do anything else
                        //termCountMap.put(token, termCountMap.get(token)+1);
                    }
                }
            }
            
        }
        
        removeStopwords(termCountMap);
        
        return termCountMap;
    }
    
    /**
     * clears cached TF-IDF scores (for example, if new documents were added to corpus)
     */
    @Override
    public void resetCachedScores() {
        termTfIdfMap = null;
        numTermsInDoc = -1;
    }
    
    @Override
    public Map<String, Double> getTermTfIdfMap(Map<String, Integer> datasetTermCountMap, Map<String, Integer> datasetTermDocCountMap, int numDocs) {
        
        if (termTfIdfMap != null) {
            return termTfIdfMap;
        }
        termTfIdfMap = new HashMap<>();
        
        if (termCountMap == null) {
            getTermCountMap();
        }
        
        // get length of document
        if (numTermsInDoc == -1) {
            numTermsInDoc = 0;
            for (TextInstance textInst : textInstances) {

                String text = textInst.getTextStr().toLowerCase();

                // preprocessing before learning: quick n' durty punctuation parsing
                text = quickPunctPreproc(text);

                Scanner textSplitter = new Scanner(text);
                while (textSplitter.hasNext()) {
                    String token = textSplitter.next();
                    numTermsInDoc++;
                }
            }
        }
        
        for (TextInstance textInst : textInstances) {
            
            String text = textInst.getTextStr().toLowerCase();
            
            // preprocessing before learning: quick n' durty punctuation parsing
            text = quickPunctPreproc(text);
            
            Scanner textSplitter = new Scanner(text);
            while (textSplitter.hasNext()) {
                String token = textSplitter.next();

                // idf: log ( [ total # of docs in corpus ] / [ # of docs containing term ] )
                int numDocsContainingTerm = 1;
                if (datasetTermDocCountMap.containsKey(token)) {
                    numDocsContainingTerm = datasetTermDocCountMap.get(token);
                }
                double idf = Math.log(((double)numDocs)/((double)numDocsContainingTerm));

                // tf: # times term appears in document (should probably be normalized by doc length, to prevent bias towards long docs)
                int termCount = 1;
                if (termCountMap.containsKey(token)) {
                    termCount = termCountMap.get(token);
                }
                double tf = (double)termCount/(double)numTermsInDoc;
                
                double tfIdf = tf * idf;
                
                if (!termTfIdfMap.containsKey(token)) {
                    // debug
                    System.out.println("debug: doc "+name+", term "+token+" TF*IDF="+tfIdf+" (tf="+tf+", idf="+idf+")");
                    termTfIdfMap.put(token, tfIdf);
                } else {
                    // if term is contained then we don't need to re-compute (actually, don't need to do the above computation either; could refactor to make method more efficient)
                    // sanity check: ensure that secondary computation reaches same value!
                    double oldTfIdf = termTfIdfMap.get(token);
                    assert oldTfIdf == tfIdf : "error: old tfidf ("+oldTfIdf+") does not match new tfidf ("+tfIdf+")";
                    
                }
                
            }
            
        }
        
        removeStopwords(termTfIdfMap);

        return termTfIdfMap;
        
    }

    public static String quickPunctPreproc(String origStr) {

        String text = origStr;
        text = text.replaceAll(Pattern.quote(". "), " . ");
        text = text.replaceAll(Pattern.quote("..."), " ... ");
        text = text.replaceAll(Pattern.quote(", "), " , ");
        text = text.replaceAll(Pattern.quote("! "), " ! ");
        text = text.replaceAll(Pattern.quote("? "), " ? ");
        text = text.replaceAll(Pattern.quote("\""), " \" ");
        text = text.replaceAll(Pattern.quote("“"), " “ ");
        text = text.replaceAll(Pattern.quote("”"), " ” ");
        text = text.replaceAll(Pattern.quote("("), " ( ");
        text = text.replaceAll(Pattern.quote(")"), " ) ");
        text = text.replaceAll(Pattern.quote(">"), " > ");
        text = text.replaceAll(Pattern.quote("<"), " < ");
        text = text.replaceAll(Pattern.quote("-"), " - ");
        text = text.replaceAll(Pattern.quote("—"), " — ");
        return text;
        
    }
    
    public void removeStopwords(Map<String, ?> map) {
        
        if (stopwordList == null) {
            
            stopwordList = new ArrayList<>();
            
            try {
                
                Scanner stopwordsIn = new Scanner(new FileReader(stopwordsFilePath));
                while (stopwordsIn.hasNextLine()) {
                    String line = stopwordsIn.nextLine().trim();
                    if (line.length() > 0 && line.charAt(0) != '#') {
                        stopwordList.add(line);
                    }
                    
                }
                
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("error reading stopwords file");
                stopwordList = null;
                return;
            }
            
        }
        
        for (String stopword : stopwordList) {
            if (map.containsKey(stopword)) {
                map.remove(stopword);
            }
        }
        
    }
    
}
