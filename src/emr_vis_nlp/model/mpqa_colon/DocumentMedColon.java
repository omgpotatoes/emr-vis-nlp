package emr_vis_nlp.model.mpqa_colon;

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
    private List<String> bowVector = null;
    private Map<String, Integer> termCountMap = null;
    private Map<String, Double> termTfIdfMap = null;
    int numTermsInDoc = -1;
    private String databaseRoot;
    
    // TODO remove these maps, migrate to attributes map
//    private Map<String, String> vars;
//    private Map<String, Integer> indicators;
    
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
//        vars = new HashMap<>();
//        indicators = new HashMap<>();
        
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
//        vars = new HashMap<>();
//        indicators = new HashMap<>();
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

                    try {

                        // don't care about first 3 items (for now)
                        lineSplitter.next();
                        lineSplitter.next();
                        lineSplitter.next();
                        String var = lineSplitter.next();
                        String val = lineSplitter.next();

                        Scanner varSplitter = new Scanner(var);
                        varSplitter.useDelimiter("_");
                        String varType = varSplitter.next().toLowerCase();

                        if (varType.equals("var")) {
//                            vars.put(var, val);
                            attributes.put(var, val);
                        } else if (varType.equals("indicator")) {
                            try {
                                int valInt = Integer.parseInt(val);
//                                indicators.put(var, valInt);
                                attributes.put(var, val);
                            } catch (ClassCastException e) {
                                assert false;
                                e.printStackTrace();
                                System.out.println("DocumentMedColon: could not cast indicator val to int: " + val);
                            }
                        } else {
                            // unrecognized type; toss it in vars for now?
                            assert false;
                            System.out.println("DocumentMedColon: unrecognized varType: " + varType);
//                            vars.put(var, val);
                                attributes.put(var, val);

                        }

                    } catch (NoSuchElementException e) {
                        // will happen if a value is not present
                        // @TODO how should we handle this? ask harry?
//                        assert false;
//                        System.err.println("DocumentMedColon: anomalous man_anns line:   "+nextLine);
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

                    try {

                        // don't care about first 3 items (for now)
                        lineSplitter.next();
                        lineSplitter.next();
                        lineSplitter.next();
                        String var = lineSplitter.next();
                        String val = lineSplitter.next();

                        Scanner varSplitter = new Scanner(var);
                        varSplitter.useDelimiter("_");
                        String varType = varSplitter.next().toLowerCase();

                        if (varType.equals("var")) {
//                            vars.put(var, val);
                                attributes.put(var, val);
                        } else if (varType.equals("indicator")) {
                            try {
                                int valInt = Integer.parseInt(val);
//                                indicators.put(var, valInt);
                                attributes.put(var, val);
                            } catch (ClassCastException e) {
                                assert false;
                                e.printStackTrace();
                                System.out.println("DocumentMedColon: could not cast indicator val to int: " + val);
                            }
                        } else {
                            // unrecognized type; toss it in vars for now?
                            assert false;
                            System.out.println("DocumentMedColon: unrecognized varType: " + varType);
//                            vars.put(var, val);
                                attributes.put(var, val);

                        }

                    } catch (NoSuchElementException e) {
                        // will happen if a value is not present
                        // @TODO how should we handle this? ask harry?
//                        assert false;
//                        System.err.println("DocumentMedColon: anomalous man_anns line:   "+nextLine);
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

//    public Map<String, String> getVars() {
//        return vars;
//    }
//
//    public Map<String, Integer> getIndicators() {
//        return indicators;
//    }

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

    // TODO refactor parsing, similarity code
//    @Override
//    public void writeDoc() {
//
//        // shouldn't be editing the medical reports; only need to worry about
//        //  changes to the man_anns
//
//        // we should rewrite both report and pathology if present, otherwise just report
//
//
//
//
//        throw new UnsupportedOperationException();
//
//    }
//
//    @Override
//    public List<String> getBagOfWordsVector() {
//
//        if (bowVector != null) {
//            return bowVector;
//        }
//        Map<String, Integer> bowMap = new HashMap<>();
//
////        for (TextInstance textInst : textInstances) {
////
////            String text = textInst.getTextStr().toLowerCase();
////            
//        String text = getParsedText();
//        Scanner textSplitter = new Scanner(text);
//        while (textSplitter.hasNext()) {
//            String token = textSplitter.next();
//            if (!bowMap.containsKey(token)) {
//                bowMap.put(token, 1);
//                //bowVector.add(token);  // do this after removing stopwords
//            }
//        }
////
////        }
//
//        // remove any stopwords
//        removeStopwords(bowMap);
//
//        bowVector = new ArrayList<>();
//        for (String key : bowMap.keySet()) {
//            bowVector.add(key);
//        }
//
//        return bowVector;
//
//    }
//
//    @Override
//    public Map<String, Integer> getTermCountMap() {
//        return getTermCountMap(true, 5);
//    }
//
//    public Map<String, Integer> getTermCountMap(boolean useCounts, int lenNGram) {
//
//        if (termCountMap != null) {
//            return termCountMap;
//        }
//        termCountMap = new HashMap<>();
//
//        boolean countTerms = false;
//        if (numTermsInDoc == -1) {
//            numTermsInDoc = 0;
//            countTerms = true;
//        }
//
////        for (TextInstance textInst : textInstances) {
////
////            String text = textInst.getTextStr().toLowerCase();
////
////            // preprocessing before learning: quick n' durty punctuation parsing
////            text = quickPunctPreproc(text);
////            
//        String text = getParsedText();
//        List<String> ngramTerms = new ArrayList<>();
//        for (int n=0; n<lenNGram; n++) {
//        	ngramTerms.add("");
//        }
//        
//        Scanner textSplitter = new Scanner(text);
//        
//        while (textSplitter.hasNext()) {
//            String token = textSplitter.next().toLowerCase();
//
//            // only process terms if they are 'words'
//            
//            Matcher wordMatcher = BIO_WORD_PATTERN.matcher(token);
//            Matcher capsMatcher = BIO_CAPWORD_PATTERN.matcher(token);
//            Matcher numMatcher = BIO_NUM_PATTERN.matcher(token);
//            boolean isStopword = isStopword(token);
//            
//            if (!isStopword && wordMatcher.matches() && !capsMatcher.matches() && !numMatcher.matches()) {
//
//                if (countTerms) {
//                    numTermsInDoc++;
//                }
//
//                if (!termCountMap.containsKey(token)) {
//                    termCountMap.put(token, 1);
//                } else {
//                    if (useCounts) {
//                        termCountMap.put(token, termCountMap.get(token) + 1);
//                    } else {
//                        // val of 1 is already stored, so don't do anything else
//                        //termCountMap.put(token, termCountMap.get(token)+1);
//                    }
//                }
//                
//                // process ngrams
//                
//                // shift everything down
//                for (int n = ngramTerms.size()-1; n>0; n--) {
//                	ngramTerms.remove(n);
//                	ngramTerms.add(n, ngramTerms.get(n-1));
//                }
//                ngramTerms.remove(0);
//                ngramTerms.add(0, token);
//                
//                // build ngrams, add
//                List<String> ngrams = new ArrayList<>();
//                int numNGrams = lenNGram-1;
//                String ngram = ngramTerms.get(0);
//                for (int n=1; n<lenNGram; n++) {
//                	String nextTerm = ngramTerms.get(n);
//                	if (!nextTerm.trim().equals("")) {
//                		ngram = ngramTerms.get(n) + " " + ngram;
//                		
//                		// add the ngram
//                		if (!termCountMap.containsKey(ngram)) {
//                            termCountMap.put(ngram, 1);
//                        } else {
//                            if (useCounts) {
//                                termCountMap.put(ngram, termCountMap.get(ngram) + 1);
//                            } else {
//                                // val of 1 is already stored, so don't do anything else
//                                //termCountMap.put(token, termCountMap.get(token)+1);
//                            }
//                        }
//                		
//                	} else {
//                		break;
//                	}
//                }
//                
//
//            } else {
//                // debug
//                //System.out.println("debug: nonword token detected: " + token);
//            	
//            	// break the ngram chain
//            	for (int n=0; n<lenNGram; n++) {
//            		ngramTerms.remove(n);
//                	ngramTerms.add(n, "");
//                }
//            	
//            }
//        }
////
////        }
//
////        removeStopwords(termCountMap);
//
//        return termCountMap;
//    }
//
//    /**
//     * clears cached TF-IDF scores (for example, if new documents were added to
//     * corpus)
//     */
//    @Override
//    public void resetCachedScores() {
//        termTfIdfMap = null;
//        bowVector = null;
//        numTermsInDoc = -1;
//    }
//
//    @Override
//    public Map<String, Double> getTermTfIdfMap(Map<String, Integer> datasetTermCountMap, Map<String, Integer> datasetTermDocCountMap, int numDocs) {
//
//        if (termTfIdfMap != null) {
//            return termTfIdfMap;
//        }
//        termTfIdfMap = new HashMap<>();
//
//        if (termCountMap == null) {
//            getTermCountMap();
//        }
//
//        // get length of document
//        if (numTermsInDoc == -1) {
//            numTermsInDoc = 0;
////            for (TextInstance textInst : textInstances) {
////
////                String text = textInst.getTextStr().toLowerCase();
//
//            String text = getParsedText();
//
//            // preprocessing before learning: quick n' durty punctuation parsing
////            text = quickPunctPreproc(text);
//
//            Scanner textSplitter = new Scanner(text);
//            while (textSplitter.hasNext()) {
//                String token = textSplitter.next();
//                numTermsInDoc++;
//            }
////            }
//        }
//
////        for (TextInstance textInst : textInstances) {
////
////            String text = textInst.getTextStr().toLowerCase();
//
//        String text = getParsedText();
//
//        // preprocessing before learning: quick n' durty punctuation parsing
////        text = quickPunctPreproc(text);
//
//        Scanner textSplitter = new Scanner(text);
//        while (textSplitter.hasNext()) {
//            String token = textSplitter.next().toLowerCase().toLowerCase();
//
//            Matcher wordMatcher = BIO_WORD_PATTERN.matcher(token);
//            Matcher capsMatcher = BIO_CAPWORD_PATTERN.matcher(token);
//            Matcher numMatcher = BIO_NUM_PATTERN.matcher(token);
//            boolean isStopword = isStopword(token);
//            
//            if (!isStopword && wordMatcher.matches() && !capsMatcher.matches() && !numMatcher.matches()) {
//                // idf: log ( [ total # of docs in corpus ] / [ # of docs containing term ] )
//                int numDocsContainingTerm = 1;
//                if (datasetTermDocCountMap.containsKey(token)) {
//                    numDocsContainingTerm = datasetTermDocCountMap.get(token);
//                }
//                double idf = Math.log(((double) numDocs) / ((double) numDocsContainingTerm));
//
//                // tf: # times term appears in document (should probably be normalized by doc length, to prevent bias towards long docs)
//                int termCount = 1;
//                if (termCountMap.containsKey(token)) {
//                    termCount = termCountMap.get(token);
//                }
//                double tf = (double) termCount / (double) numTermsInDoc;
//
//                double tfIdf = tf * idf;
//
//                if (!termTfIdfMap.containsKey(token)) {
//                    // debug
//                    System.out.println("debug: doc " + name + ", term " + token + " TF*IDF=" + tfIdf + " (tf=" + tf + ", idf=" + idf + ")");
//                    termTfIdfMap.put(token, tfIdf);
//                } else {
//                    // if term is contained then we don't need to re-compute (actually, don't need to do the above computation either; could refactor to make method more efficient)
//                    // sanity check: ensure that secondary computation reaches same value!
//                    double oldTfIdf = termTfIdfMap.get(token);
//                    assert oldTfIdf == tfIdf : "error: old tfidf (" + oldTfIdf + ") does not match new tfidf (" + tfIdf + ")";
//
//                }
//
//            } else {
//            	// clear the ngram list thus far
//            	
//                // debug
//                //System.out.println("debug: nonword token detected: " + token);
//            }
//
//        }
//
////        }
//
////        removeStopwords(termTfIdfMap);
//
//        return termTfIdfMap;
//
//    }
//
////    public static String quickPunctPreproc(String origStr) {
////
////        String text = origStr;
////        text = text.replaceAll(Pattern.quote(". "), " . ");
////        text = text.replaceAll(Pattern.quote("..."), " ... ");
////        text = text.replaceAll(Pattern.quote(", "), " , ");
////        text = text.replaceAll(Pattern.quote("! "), " ! ");
////        text = text.replaceAll(Pattern.quote("? "), " ? ");
////        text = text.replaceAll(Pattern.quote("\""), " \" ");
////        text = text.replaceAll(Pattern.quote("���"), " ��� ");
////        text = text.replaceAll(Pattern.quote("���"), " ��� ");
////        text = text.replaceAll(Pattern.quote("("), " ( ");
////        text = text.replaceAll(Pattern.quote(")"), " ) ");
////        text = text.replaceAll(Pattern.quote(">"), " > ");
////        text = text.replaceAll(Pattern.quote("<"), " < ");
////        text = text.replaceAll(Pattern.quote("-"), " - ");
////        text = text.replaceAll(Pattern.quote("���"), " ��� ");
////        return text;
////
////    }
//
//    private void loadStopwordList() {
//    	
//    	stopwordList = new ArrayList<>();
//
//        try {
//
//            Scanner stopwordsIn = new Scanner(new FileReader(stopwordsEHRFilePath));
//            while (stopwordsIn.hasNextLine()) {
//                String line = stopwordsIn.nextLine().trim().toLowerCase();
//                if (line.length() > 0 && line.charAt(0) != '#') {
//                    stopwordList.add(line);
//                }
//
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("error reading stopwords file");
//            stopwordList = null;
//            return;
//        }
//    	
//    }
//    
//    // uses a different list: biomed (CTAKES)
//    public void removeStopwords(Map<String, ?> map) {
//
//        if (stopwordList == null) {
//            loadStopwordList();
//        }
//
//        for (String stopword : stopwordList) {
//            if (map.containsKey(stopword) || map.containsKey(stopword.toLowerCase())) {
//                map.remove(stopword);
//            }
//        }
//
//    }
//    
//    public boolean isStopword(String word) {
//    	
//        if (stopwordList == null) {
//            loadStopwordList();
//        }
//        
//        for (String stopword : stopwordList) {
//            if (stopword.equalsIgnoreCase(word)) {
//                return true;
//            }
//        }
//        return false;
//    	
//    }
//
////    public void writeSelectedIndicatorsToFile() {
////        writeSelectedIndicatorsToFile(RuntimeIndicatorPrediction.predictedIndicatorNames, databaseRoot + "man_anns/" + docPath + "/revised_indicators.txt");
////    }
//
//    public void writeSelectedIndicatorsToFile(String[] indicatorNames, String filePath) {
//
//        try {
//
//            System.out.println("writing named indicators for document " + name + " to file " + filePath);
//
//            File file = new File(filePath);
//            file.delete();
//            FileWriter writer = new FileWriter(file);
//
//            // write header
//            Date date = new Date();
//            String header = "# colonoscopy revised indicator file for report # " + name + "\n# created on " + date.toString() + "\n# by textviz_explorer EMR visualizer program";
//            writer.write(header);
//
//            // for each name in indicatorNames, if present in indicators write a MPQA-style line
//            int indicatorCounter = 0;
//            // @TODO replace lenOfText with an actual text span designated by the user as being relevant for the indicator
//            int lenOfText = getText().length();
//            for (String indicatorName : indicatorNames) {
//                if (indicators.containsKey(indicatorName)) {
//                    String line = "\n" + indicatorCounter + "\t0," + lenOfText + "\tstring\t" + indicatorName + "\t" + indicators.get(indicatorName);
//                    writer.write(line);
//                    indicatorCounter++;
//                }
//            }
//
//            writer.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("error while writing revised indicators to file: " + filePath);
//        }
//
//    }
}
