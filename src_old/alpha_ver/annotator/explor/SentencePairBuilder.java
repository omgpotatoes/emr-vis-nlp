package annotator.explor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import annotator.data.DataTag;
import annotator.data.DataTagset;
import annotator.data.Dataset;
import annotator.data.Document;
import annotator.data.TextInstance;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 *
 * Class responsible for building sentence pairs from *new* argument data
 * (automatically gathered from blogs, annotated at paragraph level using
 * procon.org stance structure). Purpose of class is to build input files for
 * Mihai's tools for similarity metric computation.
 *
 * Categories we care about: case 1: 1 sent from paragraph, 1 sent from stance
 * structure examples 1a: para and ex have same argument 1b: para and ex have
 * same category (same argument optional), same side 1c: para and ex have same
 * category (same argument optional), different side 1d: para and ex randomly
 * chosen (have no restriction) (will serve as base sim measure)
 *
 * case 2: both sents from paragraphs 1a: both have same argument 1b: both have
 * same category (same argument optional), same side 1c: both have same category
 * (same argument optional), different side 1d: both randomly chosen (have no
 * restriction) (will serve as base sim measure)
 *
 *
 * thought: can mihai's system handle paragraph-level too?
 *
 *
 * @author conrada
 *
 */
public class SentencePairBuilder {

    public static StanfordCoreNLP pipeline = null;

    
    public static List<String> lastParaList = null;
    
    /**
     *
     * strategy: while we need more sents from paras choose a paragraph choose a
     * sent from the paragraph while we need more sents from stancestruct choose
     * a argument choose a sentence from argument examples
     *
     *
     *
     * @param datasetPath
     * @param numParaSents
     * @param numStanceStructSentsPerPara
     * @param seed
     * @return
     */
    public static List<String> buildSimParaAndStructSentPairsWithMetric(String datasetPath, int numParaSents, int numStanceStructSentsPerPara, long seed) {
        
        if (pipeline == null) {

            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit, pos, lemma");
            pipeline = new StanfordCoreNLP(props);

        }

        // load dataset sentences, stance structure
        Dataset activeDataset = Dataset.loadDatasetFromDoclist(new File(datasetPath));
        List<Document> documents = activeDataset.getDocuments();

        // for now, work at quazi-paragraph level?: 1. choose paragraph, 2. choose sent from para ?
        //  revision: stick entirely with paragraph level
        List<TextInstance> paragraphs = new ArrayList<>();


        for (int d = 0; d < documents.size(); d++) {
            Document doc = documents.get(d);
            List<TextInstance> docTextInstances = doc.getTextInstances();
            for (int t = 0; t < docTextInstances.size(); t++) {
                TextInstance textInstance = docTextInstances.get(t);
                // for now, just add the textInstance to paragraph list
                paragraphs.add(textInstance);

            }
        }

        // prepare stance structure
        DataTagset stanceStruct = activeDataset.getTagset();
        DataTag stanceStructRoot = stanceStruct.getTopLevelTag();
        List<DataTag> sideTags = stanceStructRoot.getChildTags();

        Map<String, String> argumentToTextMap = new HashMap<>();
        List<String> argumentNameList = new ArrayList<>();
//        Map<String, String> categoryToTextMap = new HashMap<>();
//        List<String> categoryNameList = new ArrayList<>();
//        Map<String, String> categoryNoSideToTextMap = new HashMap<>();
//        List<String> categoryNoSideNameList = new ArrayList<>();
        
        Map<String, String> textToArgumentMap = new HashMap<>();
        List<String> textArgumentList = new ArrayList<>();

        Map<String, String> argumentToCatMap = new HashMap<>();
        Map<String, String> argumentToSideMap = new HashMap<>();

        for (int s = 0; s < sideTags.size(); s++) {

            DataTag sideTag = sideTags.get(s);
            String sideName = sideTag.getAttributes().get("name");
            List<DataTag> sideCatTags = sideTag.getChildTags();

            for (int c = 0; c < sideCatTags.size(); c++) {

                DataTag sideCatTag = sideCatTags.get(c);
                String catName = sideCatTag.getAttributes().get("name");
                String catPlusSideName = sideName + ": " + catName;
                List<DataTag> sideCatArgTags = sideCatTag.getChildTags();

                for (int a = 0; a < sideCatArgTags.size(); a++) {

                    DataTag sideCatArgTag = sideCatArgTags.get(a);
                    String argName = sideCatArgTag.getAttributes().get("name");
                    String argTextExamples = sideCatArgTag.getElaborationText();

                    
                    // store text in appropriate maps
                    argumentToTextMap.put(argName, argTextExamples);
                    argumentNameList.add(argName);

                    argumentToCatMap.put(argName, catName);
                    argumentToSideMap.put(argName, sideName);
                    
                    
                    // @TODO break up the elaborationText examples by paragraph; look for 2 or more adjacent newlines?
                    
                    List<String> elabTextParas = new ArrayList<>();
                    Scanner elabTextSplitter = new Scanner(argTextExamples);
                    
                    boolean lastLineEmpty = false;
                    String currentPara = "";
                    while (elabTextSplitter.hasNextLine()) {
                        
                        String line = elabTextSplitter.nextLine().trim();
                        
                        // debug
//                        System.out.println("debug: line: "+line);
                        
                        if (line.equals("")) {
                            
                            if (lastLineEmpty) {
                                // paragraph has ended
                                if (!currentPara.equals("")) {
                                    // debug
//                                    System.out.println("debug: adding new paragraph: "+currentPara);
                                    
                                    elabTextParas.add(currentPara);
                                    currentPara = "";
                                }
                                
                            } else {
                                lastLineEmpty = true;
                            }
                            
                            
                            
                        } else {
                            // line not empty
                            lastLineEmpty = false;
                            currentPara += line+" ";
                            
                        }
                        
                    }
                    
                    for (int p=0; p<elabTextParas.size(); p++) {
                        
                        String argumentText = elabTextParas.get(p);
                        
                        // debug
//                        System.out.println("debug: adding argument paragraph: "+argumentText);
                        textToArgumentMap.put(argumentText, argName);
                        textArgumentList.add(argumentText);
                    
                    }
                    
                    // debug
                    //System.out.println("debug: for argument: "+argName+", side: "+sideName+", cat: "+catName+"\n text: "+argTextExamples+"\n\n\n\n\n\n\n\n\n");
                    
//                    if (!categoryNoSideToTextMap.containsKey(catName)) {
//                        categoryNoSideToTextMap.put(catName, argTextExamples);
//                        categoryNoSideNameList.add(catName);
//                    } else {
//                        categoryNoSideToTextMap.put(catName, categoryNoSideToTextMap.get(catName) + "\n" + argTextExamples);
//                    }
//
//                    if (!categoryToTextMap.containsKey(catPlusSideName)) {
//                        categoryToTextMap.put(catPlusSideName, argTextExamples);
//                        categoryNameList.add(catPlusSideName);
//                    } else {
//                        categoryToTextMap.put(catPlusSideName, categoryToTextMap.get(catPlusSideName) + "\n" + argTextExamples);
//                    }

                }

            }

        }


        Random rand = new Random(seed);
//        Map<Integer, Boolean> chosenParaMap = new HashMap<>();

        List<String> pairLines = new ArrayList<>();
        List<String> paraList = new ArrayList<>();
        Map<String, Integer> paraToIndexMap = new HashMap<>();
        int paraIndexCounter = 0;
        
        int sameArgCounter = 0;
        int sameCatSameSideCounter = 0;
        int sameCatDiffSideCounter = 0;
        int diffCatSameSideCounter = 0;
        int allDiffCounter = 0;
        
        int sameArgCounterUnclear = 0;
        int sameCatSameSideCounterUnclear = 0;
        int sameCatDiffSideCounterUnclear = 0;
        int diffCatSameSideCounterUnclear = 0;
        int allDiffCounterUnclear = 0;
        
        if (numParaSents == -1) {
            numParaSents = paragraphs.size();
        }
        
        if (numStanceStructSentsPerPara == -1) {
//            numStanceStructSentsPerPara = argumentNameList.size();
            numStanceStructSentsPerPara = textArgumentList.size();
        }
        
        for (int p = 0; p < numParaSents; p++) {

            // choose a paragraph (ensure that each para is only chosen once?
//            int paraIndex = -1;
//            do {
//                paraIndex = rand.nextInt(paragraphs.size());
//            } while (chosenParaMap.containsKey(paraIndex));
//            chosenParaMap.put(paraIndex, true);
//            TextInstance para = paragraphs.get(paraIndex);
            int paraIndex = rand.nextInt(paragraphs.size());
            TextInstance para = paragraphs.remove(paraIndex);
            String argument = "";
            String cat = "";
            String side = "";
            Map<String, String> attrs = para.getAttributes();
            
            boolean unclear = false;
            if (attrs.containsKey("unclear") && attrs.get("unclear").equals("true")) {
                unclear = true;
            }
            
            if (attrs.containsKey("argument")) {
                argument = attrs.get("argument");
            }
            
//            if (attrs.containsKey("category")) {
//                cat = attrs.get("category");
//            }
//            if (attrs.containsKey("aspect")) {
//                cat = attrs.get("aspect");
//            } else {
                // seems these aren't being stored properly in docs currently;
                //  for now, get cats from stance struct
                if (argumentToCatMap.containsKey(argument)) {
                    cat = argumentToCatMap.get(argument);
                    
                    // debug
//                    System.out.println("debug: cat \""+cat+"\" found for argument \""+argument+"\"");
                    
                } else {
                    // debug
                    if (!argument.trim().equals("")) {
                        System.out.println("debug: no category found for argument: "+argument);
                    }
                }
//            }
            
            if (attrs.containsKey("side")) {
                side = attrs.get("side");
            }

            String text = para.getTextStr();

            if (!paraToIndexMap.containsKey(text)) {
                paraToIndexMap.put(text, paraIndexCounter);
                paraList.add(text);
                paraIndexCounter++;
            }
            
            int textIndex = paraToIndexMap.get(text);
            
            // debug
            //System.out.println("\n\n\n\ndebug: paragraph side: "+side+", cat: "+cat+", arg: "+argument+"\ntext: "+text+"\n");

//            // build sents, choose a sent from the paragraph (if we could do para-level sim, we wouldn't need this step)
//            edu.stanford.nlp.pipeline.Annotation sentAnnot = new edu.stanford.nlp.pipeline.Annotation(text);
//            pipeline.annotate(sentAnnot);
//            List<CoreMap> sentencesCoreMap = sentAnnot.get(SentencesAnnotation.class);
//            
//            List<String> sentences = new ArrayList<>();
//            
//            for (CoreMap sentence : sentencesCoreMap) {
//                
//                String sentText = "";
//                for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
//                    
//                    String word = token.get(TextAnnotation.class);
//                    sentText += " "+word;
//                    
//                }
//                sentences.add(sentText);
//                
//            }
//            
//            int sentIndex = rand.nextInt(sentences.size());
//            String sentText = sentences.get(sentIndex);



            Map<Integer, Boolean> chosenArgMap = new HashMap<>();
            
            for (int s = 0; s < numStanceStructSentsPerPara; s++) {

                // choose an argument
                int ssIndex = -1;
                do {
//                    ssIndex = rand.nextInt(argumentNameList.size());
                    ssIndex = rand.nextInt(textArgumentList.size());
                } while (chosenArgMap.containsKey(ssIndex));
                chosenArgMap.put(ssIndex, true);

//                String ssArgument = argumentNameList.get(ssIndex);
//                String ssText = argumentToTextMap.get(ssArgument);
                String ssText = textArgumentList.get(s);
                String ssArgument = textToArgumentMap.get(ssText);
                String ssCat = "";
                if (argumentToCatMap.containsKey(ssArgument)) {
                    ssCat = argumentToCatMap.get(ssArgument);
                }
                String ssSide = "";
                if (argumentToSideMap.containsKey(ssArgument)) {
                    ssSide = argumentToSideMap.get(ssArgument);
                }

                if (!paraToIndexMap.containsKey(ssText)) {
                    paraToIndexMap.put(ssText, paraIndexCounter);
                    paraList.add(ssText);
                    paraIndexCounter++;
                }
                
                int ssTextIndex = paraToIndexMap.get(ssText);
                
                //System.out.println("debug: stancestruct side: "+ssSide+", cat: "+ssCat+", arg: "+ssArgument+"\ntext: "+ssText+"\n");
                
                double sim = 0.0;
                // debug
//                System.out.println("debug: cat: "+cat+", ssCat: "+ssCat);
                if (argument.equalsIgnoreCase(ssArgument)) {
                    // same argument: sim == 1.0
                    if (!unclear) {
                    sim = 1.0;
                    sameArgCounter++;
                    // debug:
//                    System.out.println("debug: same argument:\n\t--"+text+"\n\t--"+ssText);
                    } else {
                    sim = 0.99;
                    sameArgCounterUnclear++;
                    // debug:
//                    System.out.println("debug: same argument (unclear):\n\t--"+text+"\n\t--"+ssText);
                    }
                } else if (cat.equalsIgnoreCase(ssCat) && side.equalsIgnoreCase(ssSide)) {
                    // same cat & side: sim == 0.8
                    if (!unclear) {
                    sim = 0.8;
                    sameCatSameSideCounter++;
                    // debug:
//                    System.out.println("debug: same cat:\n\t--"+text+"\n\t--"+ssText);
                    } else {
                    sim = 0.79;
                    sameCatSameSideCounterUnclear++;
                    // debug:
//                    System.out.println("debug: same cat (unclear):\n\t--"+text+"\n\t--"+ssText);
                    }
                } else if (cat.equalsIgnoreCase(ssCat)) {
                    // same cat, diff side: sim == 0.6 
                    if (!unclear) {
                    sim = 0.6;
                    sameCatDiffSideCounter++;
                    // debug:
//                    System.out.println("debug: same cat diff side:\n\t--"+text+"\n\t--"+ssText);
                    } else {
                    sim = 0.59;
                    sameCatDiffSideCounterUnclear++;
                    // debug:
//                    System.out.println("debug: same cat diff side (unclear):\n\t--"+text+"\n\t--"+ssText);
                    }
                } else if (side.equalsIgnoreCase(ssSide)) {
                    // same side, diff cat: sim == 0.4
                    if (!unclear) {
                    sim = 0.4;
                    diffCatSameSideCounter++;
                    // debug:
//                    System.out.println("debug: same side:\n\t--"+text+"\n\t--"+ssText);
                    } else {
                    sim = 0.39;
                    diffCatSameSideCounterUnclear++;
                    // debug:
//                    System.out.println("debug: same side (unclear):\n\t--"+text+"\n\t--"+ssText);
                    }
                } else {
                    // diff side, diff cat: sim == 0
                    sim = 0.0;
                    allDiffCounter++;
                    // debug:
//                    System.out.println("debug: all diff:\n\t--"+text+"\n\t--"+ssText);
                }
                
//                String pairLine = text + "\t" + ssText + "\t" + sim;
                String pairLine = textIndex + "\t" + ssTextIndex + "\t" + sim;
                pairLines.add(pairLine);

//                // build sents, choose a sent from argument elaboration (if we could do para-level sim, we wouldn't need this step)
//                // @TODO if needed, speed this up through caching
//                edu.stanford.nlp.pipeline.Annotation ssAnnot = new edu.stanford.nlp.pipeline.Annotation(ssText);
//                pipeline.annotate(ssAnnot);
//
//                List<String> sentencesSS = new ArrayList<>();
//                List<CoreMap> sentencesSSCoreMap = ssAnnot.get(SentencesAnnotation.class);
//
//
//                for (CoreMap sentence : sentencesSSCoreMap) {
//
//                    String sentText = "";
//                    for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
//
//                        String word = token.get(TextAnnotation.class);
//                        sentText += " " + word;
//
//                    }
//                    sentencesSS.add(sentText);
//
//                }
                
            }
            
        }
        
        // need to write pairLines to one file, paraList to another (index+"\t"+value+"\n")
        //  this can be handled elsewhere
        
        // numbers
        System.out.println("sameArgCounter: "+sameArgCounter);
        System.out.println("sameArgCounterUnclear: "+sameArgCounterUnclear);
        System.out.println("sameCatSameSideCounter: "+sameCatSameSideCounter);
        System.out.println("sameCatSameSideCounterUnclear: "+sameCatSameSideCounterUnclear);
        System.out.println("sameCatDiffSideCounter: "+sameCatDiffSideCounter);
        System.out.println("sameCatDiffSideCounterUnclear: "+sameCatDiffSideCounterUnclear);
        System.out.println("diffCatSameSideCounter: "+diffCatSameSideCounter);
        System.out.println("diffCatSameSideCounterUnclear: "+diffCatSameSideCounterUnclear);
        System.out.println("allDiffCounter: "+allDiffCounter);
        System.out.println("allDiffCounterUnclear: "+allDiffCounterUnclear);
        System.out.println("total: "+(sameArgCounter+sameCatSameSideCounter+sameCatDiffSideCounter+diffCatSameSideCounter+allDiffCounter + sameArgCounterUnclear+sameCatSameSideCounterUnclear+sameCatDiffSideCounterUnclear+diffCatSameSideCounterUnclear+allDiffCounterUnclear));
        
        lastParaList = paraList;
        return pairLines;
        
    }
    
    public static void writePairLinesAndParaList(String pairLinesFilePath, List<String> pairLines, String paraIndexFilePath) {
        
        try {
            
            FileWriter pairLinesWriter = new FileWriter(pairLinesFilePath);
            
            for (int l=0; l<pairLines.size(); l++) {
                pairLinesWriter.write(pairLines.get(l)+"\n");
            }
            
            pairLinesWriter.close();
            
            if (lastParaList != null) {
                
                FileWriter paraIndexWriter = new FileWriter(paraIndexFilePath);
                
                for (int p=0; p<lastParaList.size(); p++) {
                    paraIndexWriter.write(p+"\t"+lastParaList.get(p)+"\n");
                }
                
                paraIndexWriter.close();
                
            } else {
                // shouldn't happen, there should be a lastParaList
                System.err.println("lastParaList == null");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    /**
     * 
     * gathers info concerning # of docs, paras, annotations, etc.
     * 
     * @param datasetPath 
     */
    public static void printDatasetStats(String datasetPath) {
        
        int paraCounter = 0;
        int sentCounter = 0;
        Map<String, Integer> paraArgmntCountMap = new HashMap<>();
        
        if (pipeline == null) {

            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit, pos, lemma");
            pipeline = new StanfordCoreNLP(props);

        }

        // load dataset sentences, stance structure
        Dataset activeDataset = Dataset.loadDatasetFromDoclist(new File(datasetPath));
        List<Document> documents = activeDataset.getDocuments();

        for (int d = 0; d < documents.size(); d++) {
            Document doc = documents.get(d);
            List<TextInstance> docTextInstances = doc.getTextInstances();
            for (int t = 0; t < docTextInstances.size(); t++) {
                TextInstance textInstance = docTextInstances.get(t);
                String text = textInstance.getTextStr();
                
                

            }
        }

        // prepare stance structure
        DataTagset stanceStruct = activeDataset.getTagset();
        DataTag stanceStructRoot = stanceStruct.getTopLevelTag();
        List<DataTag> sideTags = stanceStructRoot.getChildTags();

        Map<String, String> argumentToTextMap = new HashMap<>();
        List<String> argumentNameList = new ArrayList<>();
//        Map<String, String> categoryToTextMap = new HashMap<>();
//        List<String> categoryNameList = new ArrayList<>();
//        Map<String, String> categoryNoSideToTextMap = new HashMap<>();
//        List<String> categoryNoSideNameList = new ArrayList<>();

        Map<String, String> argumentToCatMap = new HashMap<>();
        Map<String, String> argumentToSideMap = new HashMap<>();

        for (int s = 0; s < sideTags.size(); s++) {

            DataTag sideTag = sideTags.get(s);
            String sideName = sideTag.getAttributes().get("name");
            List<DataTag> sideCatTags = sideTag.getChildTags();

            for (int c = 0; c < sideCatTags.size(); c++) {

                DataTag sideCatTag = sideCatTags.get(c);
                String catName = sideCatTag.getAttributes().get("name");
                String catPlusSideName = sideName + ": " + catName;
                List<DataTag> sideCatArgTags = sideCatTag.getChildTags();

                for (int a = 0; a < sideCatArgTags.size(); a++) {

                    DataTag sideCatArgTag = sideCatArgTags.get(a);
                    String argName = sideCatArgTag.getAttributes().get("name");
                    String argTextExamples = sideCatArgTag.getElaborationText();

                    // store text in appropriate maps
                    argumentToTextMap.put(argName, argTextExamples);
                    argumentNameList.add(argName);

                    argumentToCatMap.put(argName, catName);
                    argumentToSideMap.put(argName, sideName);

//                    if (!categoryNoSideToTextMap.containsKey(catName)) {
//                        categoryNoSideToTextMap.put(catName, argTextExamples);
//                        categoryNoSideNameList.add(catName);
//                    } else {
//                        categoryNoSideToTextMap.put(catName, categoryNoSideToTextMap.get(catName) + "\n" + argTextExamples);
//                    }
//
//                    if (!categoryToTextMap.containsKey(catPlusSideName)) {
//                        categoryToTextMap.put(catPlusSideName, argTextExamples);
//                        categoryNameList.add(catPlusSideName);
//                    } else {
//                        categoryToTextMap.put(catPlusSideName, categoryToTextMap.get(catPlusSideName) + "\n" + argTextExamples);
//                    }

                }

            }

        }

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    }
    
    
    
    
    public static void main(String[] args) {
        
//        String deathpenaltyDevelPath = "D:\\Users\\conrada\\Dropbox\\doclist_deathpenalty_devel.xml";
//        String illimmigrationDevelPath = "D:\\Users\\conrada\\Dropbox\\doclist_illimmigration_devel.xml";
//        String altenergyDevelPath = "D:\\Users\\conrada\\Dropbox\\doclist_altenergy_devel.xml";
//    	  String outDirPath = "";
    	
    	String deathpenaltyDevelPath = "/afs/cs.pitt.edu/usr0/conrada/private/Dropbox/doclist_deathpenalty_devel.xml";
    	String illimmigrationDevelPath = "/afs/cs.pitt.edu/usr0/conrada/private/Dropbox/doclist_illimmigration_devel.xml";
    	String altenergyDevelPath = "/afs/cs.pitt.edu/usr0/conrada/private/Dropbox/doclist_altenergy_devel.xml";
    	String outDirPath = "/afs/cs.pitt.edu/usr0/conrada/public/html/research/similarity_data/";
    	
        
        long seed = System.currentTimeMillis();
        System.out.println("seed is "+seed);
        
        int numParaSents = -1;
        int numStanceStructSentsPerPara = -1;
        
        String name = "";
        name = "altenergy";
        System.out.println("*** "+name+" ***");
        List<String> lines = buildSimParaAndStructSentPairsWithMetric(altenergyDevelPath, numParaSents, numStanceStructSentsPerPara, seed);
        writePairLinesAndParaList(outDirPath+"pairLines_"+name+".txt", lines, outDirPath+"paraIndex_"+name+".txt");
        
        name = "illimmigration";
        System.out.println("*** "+name+" ***");
        lines = buildSimParaAndStructSentPairsWithMetric(illimmigrationDevelPath, numParaSents, numStanceStructSentsPerPara, seed);
        writePairLinesAndParaList(outDirPath+"pairLines_"+name+".txt", lines, outDirPath+"paraIndex_"+name+".txt");
        
        name = "deathpenalty";
        System.out.println("*** "+name+" ***");
        lines = buildSimParaAndStructSentPairsWithMetric(deathpenaltyDevelPath, numParaSents, numStanceStructSentsPerPara, seed);
        writePairLinesAndParaList(outDirPath+"pairLines_"+name+".txt", lines, outDirPath+"paraIndex_"+name+".txt");
        
        
    }
    
}
