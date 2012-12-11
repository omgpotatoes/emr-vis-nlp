package emr_vis_nlp.model.mpqa_colon;

import emr_vis_nlp.utils.SimUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * Represents a set of documents loaded from disk. Is optionally accompanied by
 * a schema datafile.
 *
 * @author alexander.p.conrad@gmail.com
 */
public abstract class Dataset {

//    public static String DATASET_TYPE_ARGSUBJ = "datasetarguing";
    public static String DATASET_TYPE_COLON = "datasetcolonoscopy";
    protected List<Document> documents;
//    protected boolean isActive = false;
    protected String name = "";
    protected String path = "";
    protected String rootDir = "";
    protected String type = "";
    // for similarity computations
//    protected Map<String, Integer> datasetTermCounts = null;
//    protected Map<String, Integer> datasetDocTermCounts = null;
//    protected List<List<Double>> similarityMatrixCosineCounts = null;
//    protected List<List<Double>> similarityMatrixCosineTfIdf = null;

    public Dataset(String path) {
        this.path = path;
    }

//    public boolean isActive() {
//        return isActive;
//    }
//
//    public void setActive(boolean isActive) {
//        this.isActive = isActive;
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public String getPath() {
        return path;
    }

    public String getRootDir() {
        return rootDir;
    }

    public String getType() {
        return type;
    }
    
    public List<String> getAllAttributesFromDocs() {
        
        // loop through all docs, keeping track of keys for both 
        List<String> allAttrs = new ArrayList<>();
        Map<String, Boolean> allAttrsMap = new HashMap<>();
        
        // ensure that "name" attr is 1st
        allAttrs.add("name");
        allAttrsMap.put("name", true);
        
        for (Document doc : documents) {
            
            Map<String, String> attributes = doc.getAttributes();
            for (String key : attributes.keySet()) {
                if (!allAttrsMap.containsKey(key)) {
                    allAttrsMap.put(key, true);
                    allAttrs.add(key);
                }
            }
            
        }
        
        return allAttrs;
        
    }

    public abstract void writeDoclist();

    // TODO refactor parsing, similarity code
//    /**
//     *
//     * @return map from terms to counts for entire dataset
//     */
//    public Map<String, Integer> getDatasetTermCountMap() {
//
//        if (datasetTermCounts != null) {
//            return datasetTermCounts;
//        }
//
//        datasetTermCounts = new HashMap<>();
//
//        for (Document doc : documents) {
//
//            Map<String, Integer> docTermCountMap = doc.getTermCountMap();
//            Set<String> docTerms = docTermCountMap.keySet();
//            for (String docTerm : docTerms) {
//                int docTermCount = docTermCountMap.get(docTerm);
//                if (!datasetTermCounts.containsKey(docTerm)) {
//                    datasetTermCounts.put(docTerm, docTermCount);
//                } else {
//                    datasetTermCounts.put(docTerm, datasetTermCounts.get(docTerm) + docTermCount);
//                }
//            }
//
//        }
//
//        return datasetTermCounts;
//
//    }
//
//    /**
//     *
//     * @return map from terms to # of docs in which they occur for entire
//     * dataset
//     */
//    public Map<String, Integer> getDatasetDocTermCountMap() {
//
//        if (datasetDocTermCounts != null) {
//            return datasetDocTermCounts;
//        }
//
//        datasetDocTermCounts = new HashMap<>();
//
//        for (Document doc : documents) {
//
//            Map<String, Integer> docTermCountMap = doc.getTermCountMap();
//            Set<String> docTerms = docTermCountMap.keySet();
//            for (String docTerm : docTerms) {
//                if (!datasetDocTermCounts.containsKey(docTerm)) {
//                    datasetDocTermCounts.put(docTerm, 1);
//                } else {
//                    datasetDocTermCounts.put(docTerm, datasetDocTermCounts.get(docTerm) + 1);
//                }
//            }
//
//        }
//
//        return datasetDocTermCounts;
//
//    }
//
//    /**
//     * similarity matrices should be reset whenever document collection is
//     * altered
//     */
//    public void resetSimilarityMatrices() {
//        similarityMatrixCosineCounts = null;
//        similarityMatrixCosineTfIdf = null;
//    }
//
//    /**
//     *
//     * @return a matrix containing cosine similarity scores on term frequencies
//     * between each pair of docs
//     */
//    public List<List<Double>> getSimilarityMatrixCosineCounts() {
//
//        if (similarityMatrixCosineCounts != null) {
//            return similarityMatrixCosineCounts;
//        }
//
//        similarityMatrixCosineCounts = new ArrayList<>();
//
//        for (int d1 = 0; d1 < documents.size(); d1++) {
//
//            Document doc1 = documents.get(d1);
//            Map<String, Integer> doc1TermCountMap = doc1.getTermCountMap();
//            double d1VecMag = SimUtils.computeVectorMagnitude(doc1TermCountMap);
//
//            List<Double> similarityScores = new ArrayList<>();
//            similarityMatrixCosineCounts.add(similarityScores);
//
//            for (int d2 = 0; d2 < documents.size(); d2++) {
//
//                Document doc2 = documents.get(d2);
//                Map<String, Integer> doc2TermCountMap = doc2.getTermCountMap();
//                double d2VecMag = SimUtils.computeVectorMagnitude(doc2TermCountMap);
//
//                // if d1 == d2, sim=1 (debug: compute for sanity check)
//                // if d2 < d1, sim=matrix.get(d2).get(d1) (if symmetric) (debug: compute for sanity check)
//                double dotProd = SimUtils.computeDotProduct(doc1TermCountMap, doc2TermCountMap);
//                double sim = dotProd / (d1VecMag * d2VecMag);
//
//                // debug
//                //System.out.println("debug: d1="+d1+", d2="+d2+", dotProd="+dotProd+", d1VecMag="+d1VecMag+", d2VecMag="+d2VecMag+", cosineSim="+sim);
//
//                similarityScores.add(sim);
//
//            }
//
//        }
//
//        return similarityMatrixCosineCounts;
//
//    }
//
//    /**
//     *
//     * @return a matrix containing cosine similarity scores on term frequencies
//     * between each pair of docs
//     */
//    public List<List<Double>> getSimilarityMatrixCosineTfIdf() {
//
//        throw new UnsupportedOperationException();
//
//
//
//
//    }

    
    
    
    
    public static Dataset loadDatasetFromDoclist(File doclistFile) {
        return loadDatasetFromDoclist(doclistFile.getAbsolutePath());
    }

    public static Dataset loadDatasetFromDoclist(String doclistFilePath) {

        Dataset dataset = null;

        // store root path
        String doclistRootPath = new File(doclistFilePath).getParent() + "/";
        // debug
        System.out.println("debug: doclist directory is \"" + doclistRootPath + "\"");

        // load doclist file for reading
        org.w3c.dom.Document dom = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(doclistFilePath);
        } catch (ParserConfigurationException | SAXException | IOException pce) {
            pce.printStackTrace();
        }

        // document root
        Element doclistRoot = dom.getDocumentElement();
        String doclistTypeName = doclistRoot.getAttribute("type").trim().toLowerCase();
        if (doclistTypeName.equals(DATASET_TYPE_COLON)) {
            // type is arguing, call appropriate builders
            // debug
            System.out.println("debug: doclist type is \"" + DATASET_TYPE_COLON + "\"");
            dataset = new DatasetMedColonDoclist(doclistRoot, doclistRootPath, doclistFilePath);

        } else {
            // type is not recognized
            System.err.println("Doclist.loadDatasetFromDoclist: doclist type \"" + doclistTypeName + "\" not recognized");

        }

        return dataset;

    }
    
    
    
}
