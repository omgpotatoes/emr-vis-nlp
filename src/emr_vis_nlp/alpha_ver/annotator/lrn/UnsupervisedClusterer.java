
package annotator.lrn;

import annotator.lrn.SimUtils;
import annotator.MainWindow;
import annotator.data.Dataset;
import annotator.data.Document;
import annotator.data.DocumentMedColon;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Performs unsupervised clustering of documents based on either text similarity 
 * or a (sub)set of quality measures. (note: weka's clustering code cannot 
 * handle complex distance measures, such as cosine. Hence the need for 
 * re-implementing)
 *
 * @author alexander.p.conrad@gmail.com
 */
public class UnsupervisedClusterer {
    
    protected static Random rand = new Random();
    protected static List<Integer> lastClusterIndices = new ArrayList<>();
    
    public static List<Integer> predictKNNClustersBOW(Dataset dataset, int k) {
        
        // build term vectors based on termDataset keyset 
        List<String> termVector = new ArrayList<>();
        for (String key : dataset.getDatasetTermCountMap().keySet()) {
            termVector.add(key);
        }
        
        // build a term vector for each document
        // vectors = boolean bag-of-words (presence / absence)
        List<List<Double>> docVectors = new ArrayList<>();
        List<Document> docs = dataset.getDocuments();
        for (Document doc : docs) {
            List<Double> docVector = new ArrayList<>();
            docVectors.add(docVector);
            Map<String, Integer> docTerms = doc.getTermCountMap();
            for (String term : termVector) {
                if (docTerms.containsKey(term) && docTerms.get(term) != 0) {
                    docVector.add(1.);
                } else {
                    docVector.add(0.);
                }
            }
        }
        
        // initialize the k centroids using random point selection
        assert k <= docs.size() : "error: fewer docs ("+docs.size()+") than clusters ("+k+")";
        
        // use random partition initialization to keep the clusters nearer to the center, 
        //  help reduce clusters being taken over by outliers
        
        // d1=doc
        List<Integer> clusterAssignments = new ArrayList<>();
        for (int d=0; d<docs.size(); d++) {
            clusterAssignments.add(rand.nextInt(k));
        }
        
        List<List<Double>> centroids = new ArrayList<>(k);
        List<Integer> numDocsInCluster = new ArrayList<>(k);
        for (int c=0; c<k; c++) {
            
            numDocsInCluster.add(0);
            List<Double> centroid = new ArrayList<>();
            // initialize centroid
            for (int t=0; t<termVector.size(); t++) {
                centroid.add(0.);
            }
            centroids.add(centroid);
            for (int d=0; d<docs.size(); d++) {
                if (clusterAssignments.get(d) == c) {
                    int prevNumInCluster = numDocsInCluster.remove(c);
                    numDocsInCluster.add(c, prevNumInCluster+1);
                    // increment current vector by document's vals (will normalize later)
                    for (int t=0; t<termVector.size(); t++) {
                        double docTermCount = docVectors.get(d).get(t);
                        double clusterTermCount = centroid.remove(t);
                        centroid.add(t, docTermCount+clusterTermCount);                        
                    }
                }
            }
            // normalize cluster size
            for (int t=0; t<termVector.size(); t++) {
                double clusterTermCount = centroid.remove(t);
                double newClusterTermCount = clusterTermCount / numDocsInCluster.get(c);
                centroid.add(t, newClusterTermCount);
            }
        }
        
        // clusters finally initialized, time to iterate
        boolean hasChanged = true;
        int maxIterations = 10000;
        int curIteration = 0;
        while (curIteration < maxIterations && hasChanged) {
            hasChanged = false;
            // debug
            System.out.println("debug: beginning clustering iteration "+curIteration);
            
            // for each document, find the centroid to which it is closest and reassign
            // @TODO bug in here somewhere, some docs are being assigned cluster == -1 which should not happen!
            for (int d=0; d<docVectors.size(); d++) {
                List<Double> docVector = docVectors.get(d);
                double maxSim = Double.MIN_VALUE;
                int maxSimIndex = -1;
                for (int c=0; c<centroids.size(); c++) {
                    List<Double> centroidVector = centroids.get(c);
                    double sim = SimUtils.computeCosineSim(docVector, centroidVector);
                    if (sim > maxSim) {
                        maxSim = sim;
                        maxSimIndex = c;
                    }
                }
                
                // reassign document to closest cluster
                int oldCluster = clusterAssignments.remove(d);
                clusterAssignments.add(d, maxSimIndex);

                if (oldCluster != maxSimIndex) {
                    hasChanged = true;
                    // debug
//                    System.out.println("debug: reassigning doc " + docs.get(d).getName() + " from cluster " + oldCluster + " to " + maxSimIndex);
                } else {
                    // debug
//                    System.out.println("debug: keeping doc "+docs.get(d).getName()+" in cluster "+oldCluster);
                }
                
            }
            // move centroids so that they match the new doc assignments

            centroids = new ArrayList<>(k);
            numDocsInCluster = new ArrayList<>(k);
            for (int c = 0; c < k; c++) {
                numDocsInCluster.add(0);
                List<Double> centroid = new ArrayList<>();
                // initialize centroid
                for (int t = 0; t < termVector.size(); t++) {
                    centroid.add(0.);
                }
                centroids.add(centroid);
                for (int d = 0; d < docs.size(); d++) {
                    if (clusterAssignments.get(d) == c) {
                        int prevNumInCluster = numDocsInCluster.remove(c);
                        numDocsInCluster.add(c, prevNumInCluster + 1);
                        // increment current vector by document's vals (will normalize later)
                        for (int t = 0; t < termVector.size(); t++) {
                            double docTermCount = docVectors.get(d).get(t);
                            double clusterTermCount = centroid.remove(t);
                            centroid.add(t, docTermCount + clusterTermCount);
                        }
                    }
                }
                // normalize cluster size
                for (int t = 0; t < termVector.size(); t++) {
                    double clusterTermCount = centroid.remove(t);
                    double newClusterTermCount = clusterTermCount / numDocsInCluster.get(c);
                    centroid.add(t, newClusterTermCount);
                }
            }


            curIteration++;
        }
        
        lastClusterIndices = clusterAssignments;
        return clusterAssignments;
        
    }
    
    
    public static List<Integer> predictKNNClustersSelectedAttrs(Dataset dataset, int k, List<String> attrKeys) {
        
        
        // build term vectors based on list of selected attribute keys
        
        // build a term vector for each document
        // vectors = boolean bag-of-words (presence / absence)
        List<List<Integer>> docVectors = new ArrayList<>();
        List<Document> docs = dataset.getDocuments();
        for (Document doc : docs) {
            // debug
//            System.out.println("debug: building vector for bioviz doc "+doc.getName());
            List<Integer> docVector = new ArrayList<>();
            docVectors.add(docVector);
            Map<String, Integer> docIndis = ((DocumentMedColon)doc).getIndicators();
            Map<String, String> docVars = ((DocumentMedColon)doc).getVars();
            for (String attr : attrKeys) {
                // only handle numeric attrs for now; ignore the str attrs
                int attrValue = -1;
                if (docIndis.containsKey(attr)) {
                    attrValue = docIndis.get(attr);
                } else if (docVars.containsKey(attr)) {
                    try {
                        attrValue = Integer.parseInt(docVars.get(attr));
                    } catch (NumberFormatException e) {
                        // attribute was a str one, that's ok
                    }
                }
                // increment attrVal, since it'll be -1 if not present, not applicable
                attrValue++;
                docVector.add(attrValue);
                // debug
//                System.out.println("debug:\tattr=\""+attr+"\", val="+attrValue);
            }
        }
        
        
        // use weka's clustering; should be adequate for our purposes
        
        // build attribute vector
        FastVector attrVector = new FastVector();
        for (String attr : attrKeys) {
            Attribute attribute = new Attribute(attr);
            attrVector.addElement(attribute);
        }
        
        // build instances
        Instances instances = new Instances("documents", attrVector, docs.size());
        for (int i=0; i<docs.size(); i++) {
            Instance instance = new Instance(attrVector.size());
            for (int a=0; a<attrVector.size(); a++) {
                int val = docVectors.get(i).get(a);
                instance.setValue((Attribute) attrVector.elementAt(a), (float) val);
            }
            instances.add(instance);
        }
        
        // run clusterer
        SimpleKMeans clusterer = new SimpleKMeans();
        try {
            clusterer.setNumClusters(k);
            clusterer.setPreserveInstancesOrder(true);
            clusterer.buildClusterer(instances);
            int[] assignments = clusterer.getAssignments();
            
            // build list with cluster memberships for each doc
            List<Integer> clusterMemberships = new ArrayList<>();
            for (int i=0; i<assignments.length; i++) {
                clusterMemberships.add(assignments[i]);
            }
            
            lastClusterIndices = clusterMemberships;
            return clusterMemberships;
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error: weka did something funky");
            
            // build simple list with all docs set to cluster 1
//            return null;
            
            List<Integer> clusterMemberships = new ArrayList<>();
            for (int i=0; i<docs.size(); i++) {
                clusterMemberships.add(0);
            }
            
            lastClusterIndices = clusterMemberships;
            return clusterMemberships;
            
        }
    }
    
    public static List<Integer> getLastClusterAssignments() {
        return lastClusterIndices;
    }
    
}
