package annotator.lrn;

import annotator.MainWindow;
import annotator.data.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.NotImplementedException;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.*;

/**
 * Clusters documents based on textual similarity.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class TextSimilarityClustering {

    // set of terms to consider when clustering
    protected List<String> terms;
    // vector of terms for weka
    protected FastVector termVector;
    // vector of attributes for weka
    protected FastVector attrVector;

    public TextSimilarityClustering(Set<String> terms) {
        this.terms = new ArrayList<>();
        termVector = new FastVector();
        attrVector = new FastVector();
        for (String term : terms) {
            this.terms.add(term);
            termVector.addElement(term);
            Attribute termAttr = new Attribute(term);
            attrVector.addElement(termAttr);
        }
    }

    public void removeStopwordsFromTermset() {

        // remove stopwords

        // rebuild term vector, attribute list


        throw new NotImplementedException();
    }

    public List<Integer> clusterDocumentsByCosine(List<Document> documents, int numClusters) {

        // build instances for documents
        Instances instances = new Instances("documents", attrVector, documents.size());
        for (int d = 0; d < documents.size(); d++) {
            Document document = documents.get(d);
            Instance instance = buildInstanceForDocument(document);
            instances.add(instance);
        }

        // call weka
//        SimpleKMeans clusterer = new SimpleKMeans();
//        try {
//            clusterer.setNumClusters(numClusters);
//            clusterer.setDistanceFunction(MainWindow.distFuncCosine);
//            clusterer.setPreserveInstancesOrder(true);
//            clusterer.setDistanceFunction(null);
//            clusterer.buildClusterer(instances);
//            int[] assignments = clusterer.getAssignments();
//            
//            // build list with cluster memberships for each doc
//            List<Integer> clusterMemberships = new ArrayList<>();
//            for (int i=0; i<assignments.length; i++) {
//                clusterMemberships.add(assignments[i]);
//            }
//            return clusterMemberships;
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("error: weka did something funky");
//            return null;
//        }

        try {
            HierarchicalClusterer clusterer = new HierarchicalClusterer();
            clusterer.setNumClusters(numClusters);
            clusterer.setDistanceFunction(MainWindow.distFuncCosine);
            clusterer.buildClusterer(instances);
            
            // build list with cluster memberships for each doc
            List<Integer> clusterMemberships = new ArrayList<>();
            for (int i=0; i<instances.numInstances(); i++) {
                Instance instance = instances.instance(i);
                double[] predictions = clusterer.distributionForInstance(instance);
                int cluster = findLargestPrediction(predictions);
                clusterMemberships.add(cluster);
            }
            return clusterMemberships;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error: weka did something funky");
            return null;
        }

    }
    
    public static int findLargestPrediction(double[] predictions) {
        
        double maxPred = 0.0;
        int maxPredIndex = -1;
        for (int d=0; d<predictions.length; d++) {
            double prediction = predictions[d];
            if (prediction > maxPred) {
                maxPred = prediction;
                maxPredIndex = d;
            }
        }
        
        return maxPredIndex;
        
    }
    
    protected Instance buildInstanceForDocument(Document doc) {

        Instance instance = new Instance(attrVector.size());

        Map<String, Integer> termCounts = doc.getTermCountMap();

        for (int t = 0; t < termVector.size(); t++) {

            String term = terms.get(t);

            if (termCounts.containsKey(term) && termCounts.get(term) != 0) {
                int termCount = termCounts.get(term);
                instance.setValue((Attribute) attrVector.elementAt(t), (float) termCount);
                // debug
                
            } else {
                instance.setValue((Attribute) attrVector.elementAt(t), 0);
            }

        }

        return instance;

    }
}
