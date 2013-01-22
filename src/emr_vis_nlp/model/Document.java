package emr_vis_nlp.model;

import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import emr_vis_nlp.model.mpqa_colon.TextInstance;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Represents a single unit of data (ie, records for 1 patient, single arguing
 * document, etc.). Will generally be decomposed to finer granularity (ie,
 * multiple EMRs, multiple paragraphs, etc.)
 *
 * @author alexander.p.conrad@gmail.com
 */
public abstract class Document {

    public static String DOCUMENT_TYPE_COLON = "documentcolonoscopy";
    
    protected List<TextInstance> textInstances;
    protected Map<String, String> attributes;
    
    protected boolean isActive = false;
    protected String name = "";
    // rawName = what's literally in the file; use this if present instead of rebuilding
    protected String rawName = "";
    protected String docPath = "";
    // contains version of document which has been processed by Stanford parser
    protected String parsedText;

    public Document() {
        isActive = false;
        name = "";
        rawName = "";
        attributes = new HashMap<>();
        docPath = "";
        textInstances = new ArrayList<>();
        parsedText = "";
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDocPath(String docPath) {
        this.docPath = docPath;
    }

    /**
     *
     * @return all of the "TextInstances" (ie, paragraphs) in this document.
     */
    public List<TextInstance> getTextInstances() {
        return textInstances;
    }

    public String getRawName() {
        return rawName;
    }

    public String getDocPath() {
        return docPath;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getText() {

        String text = "";
        for (TextInstance instance : textInstances) {
            text += instance.getTextStr() + "\n";
        }
        return text;

    }
    
    // parsing is no longer a concern of this class
//    /**
//     * builds a version of the document after parsing via Stanford. Purpose:
//     * tokenization, decapitalization, etc.
//     */
//    public void buildParsedText() {
//
//        if (pipeline == null) {
//
//            Properties props = new Properties();
//            props.put("annotators", "tokenize, ssplit, pos, lemma");
//            pipeline = new StanfordCoreNLP(props);
//
//        }
//
//        parsedText = "";
//
//        for (TextInstance textInstance : textInstances) {
//
//            String origText = textInstance.getTextStr();
//
//            edu.stanford.nlp.pipeline.Annotation sentAnnot = new edu.stanford.nlp.pipeline.Annotation(origText);
//            pipeline.annotate(sentAnnot);
//
//            String newText = "";
//            for (CoreLabel token : sentAnnot.get(TokensAnnotation.class)) {
//                newText += token.get(TextAnnotation.class) + " ";
//            }
//
//            parsedText += newText;
//
//        }
//        
//        // debug
//        //System.out.println("debug: built new parsed text:\n"+parsedText);
//
//    }
//
//    public String getParsedText() {
//        if (parsedText.equals("")) {
//            buildParsedText();
//        }
//        return parsedText;
//    }

    // TODO refactor parsing, similarity code
//    /**
//     *
//     * Writes an active doc to the XML file from which it was originally read.
//     *
//     */
//    public abstract void writeDoc();
//
//    /**
//     * returns a vector containing all words present in this document.
//     */
//    public abstract List<String> getBagOfWordsVector();
//
//    /**
//     * returns a map from each term to its count.
//     */
//    public abstract Map<String, Integer> getTermCountMap();
//
//    /**
//     * returns a map from each term to its per-document TF-IDF score.
//     *
//     * @param datasetTermCountMap
//     * @return
//     */
//    public abstract Map<String, Double> getTermTfIdfMap(Map<String, Integer> datasetTermCountMap, Map<String, Integer> datasetTermDocCountMap, int numDocs);
//
//    /**
//     * clears cached scores, such as TF-IDF weights (for example, if # of
//     * documents in a collection changes)
//     */
//    public abstract void resetCachedScores();
}
