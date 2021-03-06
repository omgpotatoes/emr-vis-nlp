package emr_vis_nlp.model.mpqa_colon;

import emr_vis_nlp.model.TextInstance;
import emr_vis_nlp.model.Document;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * Document for use in the colonoscopy report project
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocumentMedColon extends Document {
    
    public static final Pattern BIO_WORD_PATTERN = Pattern.compile("[a-zA-Z\\-0-9]{3,50}");
    public static final Pattern BIO_CAPWORD_PATTERN = Pattern.compile("[A-Z\\-0-9]{1,50}");
    public static final Pattern BIO_NUM_PATTERN = Pattern.compile("[a-zA-Z]?[0-9]*");
    
    // note: DocMedColon will only have 1 (or 2?) text instances, representing the medical record
    int numTermsInDoc = -1;
    private String databaseRoot;
    
    private boolean isPathologyPresent;
    
    /**
     * creates a new empty DocumentMedColon
     *
     * @param path where the datafile should be saved
     */
    public DocumentMedColon(String name, String path) {
        super();

        docPath = path;
        this.name = name;
        textInstances = new ArrayList<>();
        isActive = true;
        databaseRoot = "";
        
        isPathologyPresent = false;
        
        attributes.put("name", name);

    }

    /**
     * creates a new DocumentMedColon from a MPQA-style "database" set of files,
     * as specified in a doclist
     *
     * @param el node in an XML doclist specifying path to "database", id of doc
     * @param rootPath directory in which source XML doclist is contained
     */
    public DocumentMedColon(Element el, String rootPath, String databaseRoot) {
        super();
        
        // simply contains id # for this document
        docPath = el.getFirstChild().getNodeValue().trim();
        rawName = docPath;
        name = docPath;
        attributes.put("name", name);

        if (databaseRoot.charAt(0) != '\\' && databaseRoot.charAt(0) != '/' && databaseRoot.charAt(1) != ':') {
            databaseRoot = rootPath + databaseRoot;
        }

        this.databaseRoot = databaseRoot;

        // debug
        System.out.println("debug: reading new document id=" + docPath);

        // read the files: everything in docs/id/ and man_anns/id/
        //  will definitely be report.txt, may also be pathology.txt
        textInstances = new ArrayList<>();
        try {

            // read docs/id/report.txt
            String docReportPath = databaseRoot + "docs/" + docPath + "/report.txt";
//            System.out.println("DocumentMedColon: reading "+docReportPath);
            BufferedReader docReportReader = new BufferedReader(new FileReader(docReportPath));

            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = docReportReader.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }

            docReportReader.close();

            String docReaderText = sb.toString();
            TextInstance docReportInstance = new TextInstanceMedColon("report", docReaderText);
            textInstances.add(docReportInstance);

            // read man_anns/id/report.txt
            String manAnnsReportPath = databaseRoot + "man_anns/" + docPath + "/report.txt";
//            System.out.println("DocumentMedColon: reading "+manAnnsReportPath);
            Scanner manAnnsReportReader = new Scanner(new BufferedReader(new FileReader(manAnnsReportPath)));

            while (manAnnsReportReader.hasNextLine()) {

                String nextLine = manAnnsReportReader.nextLine().trim();
                if (nextLine.charAt(0) != '#') {

                    Scanner lineSplitter = new Scanner(nextLine);
                    lineSplitter.useDelimiter("\t");
                    
                    String var = "";
                    String val = "";
                    try {

                        // don't care about first 3 items (for now)
                        lineSplitter.next();
                        lineSplitter.next();
                        lineSplitter.next();
                        // read attrs
                        var = lineSplitter.next();
                        val = lineSplitter.next();
                        
                        // if name begins with ``VAR_'' , remove; necessary so that names match with what the predictor model expects
                        if (var.length() >= 4 && var.substring(0,4).equalsIgnoreCase("VAR_")) {
                            var = var.substring(4).toLowerCase();
                        }
                        
                        if (val != null) {
                            val = DatasetTermTranslator.getValTranslation(val);
                        }
                        

                    } catch (NoSuchElementException e) {
                        // will happen if a value is not present; 
//                        assert false;
//                        System.err.println("DocumentMedColon: anomalous man_anns line:   "+nextLine);
                    }
                    
                    if (!var.equals("")) {
                        attributes.put(var, val);
                    }

                }

            }

            manAnnsReportReader.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error: DocumentMedColon: could not find docs/" + docPath + "/report.txt");
        }

        isPathologyPresent = false;
        try {

            // read docs/id/pathology.txt
            String docPathologyPath = databaseRoot + "docs/" + docPath + "/pathology.txt";
//            System.out.println("DocumentMedColon: reading "+docPathologyPath);
            BufferedReader docPathologyReader = new BufferedReader(new FileReader(docPathologyPath));

            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = docPathologyReader.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }

            docPathologyReader.close();

            String docPathologyText = sb.toString();
            TextInstance docPathologyInstance = new TextInstanceMedColon("pathology", docPathologyText);
            textInstances.add(docPathologyInstance);

            // read man_anns/id/pathology.txt
            String manAnnsPathologyPath = databaseRoot + "man_anns/" + docPath + "/pathology.txt";
//            System.out.println("DocumentMedColon: reading "+manAnnsPathologyPath);
            Scanner manAnnsPathologyReader = new Scanner(new BufferedReader(new FileReader(manAnnsPathologyPath)));

            while (manAnnsPathologyReader.hasNextLine()) {

                String nextLine = manAnnsPathologyReader.nextLine().trim();
                if (nextLine.charAt(0) != '#') {

                    Scanner lineSplitter = new Scanner(nextLine);
                    lineSplitter.useDelimiter("\t");
                    
                    String var = "";
                    String val = "";
                    try {

                        // don't care about first 3 items (for now)
                        lineSplitter.next();
                        lineSplitter.next();
                        lineSplitter.next();
                        var = lineSplitter.next();
                        val = lineSplitter.next();
                        
                        // if name begins with ``VAR_'' , remove; necessary so that names match with what the predictor model expects
                        if (var.length() >= 4 && var.substring(0,4).equalsIgnoreCase("VAR_")) {
                            var = var.substring(4).toLowerCase();
                        }
                        
                        if (val != null) {
                            val = DatasetTermTranslator.getValTranslation(val);
                        }

                    } catch (NoSuchElementException e) {
                        // will happen if a value is not present
//                        assert false;
//                        System.err.println("DocumentMedColon: anomalous man_anns line:   "+nextLine);
                    }

                    if (!var.equals("")) {
                        attributes.put(var, val);
                    }

                }

            }

            manAnnsPathologyReader.close();
            isPathologyPresent = true;

        } catch (FileNotFoundException e) {
            // no pathology for this report, that's ok
            // debug
//            System.out.println("debug: document "+docPath+" contains no pathology");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("DocumentMedColon: error while reading pathology files");
        }

        isActive = true;
        
        // add text as attr
        attributes.put("text", getText());

    }

    public static List<Document> buildDatasetMed(Element doclistRoot, String rootPath, String databaseRoot) {

        // read in all docs from xml
        String doclistRootName = doclistRoot.getTagName().trim().toLowerCase();
        NodeList documentNodes = doclistRoot.getElementsByTagName("Document");
        List<Document> documentList = new ArrayList<>();
        if (documentNodes != null && documentNodes.getLength() > 0) {
            for (int n = 0; n < documentNodes.getLength(); n++) {
                Element documentNode = (Element) documentNodes.item(n);
                DocumentMedColon document = new DocumentMedColon(documentNode, rootPath, databaseRoot);
                documentList.add(document);
            }
        } else {
            // no docs are present in doclist; error
            System.err.println("DocumentArguing: doclist empty, has no nodes of type \"Document\"");

        }

        return documentList;

    }

    // NOTE: parsing, similarity code removed; see alpha document types if needed
    
}
