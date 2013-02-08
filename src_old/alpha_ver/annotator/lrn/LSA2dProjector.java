package annotator.lrn;

import annotator.lrn.TermScoreTuple;
import annotator.data.Dataset;
import annotator.data.DocumentMedColon;
import edu.ucla.sspace.lsa.LatentSemanticAnalysis;
import edu.ucla.sspace.text.Document;
import edu.ucla.sspace.text.IteratorFactory;
import edu.ucla.sspace.util.CombinedIterator;
import edu.ucla.sspace.vector.DoubleVector;
import java.io.IOError;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Acts as an interface to the SSPACE LatentSemanticAnalysis.java module.
 * Projects a dataset into a 2d space based either on textual content or on a
 * (sub)set of quality measures.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class LSA2dProjector {
	
	/*
	 * max range by which to randomly artificially jitter points 
	 * only use this for attr projection, since it leads to many points on top of each other
	 */
	public static final double JITTER = 0.007;
	
	/*
	 * cached last 2d positions returned from this class.
	 */
	private static List<List<Double>> lastDocDimVals = null;
	
    /**
     *
     * Projects documents from a dataset into a numDims-dimensional space based
     * on the text of the documents.
     *
     * @param dataset
     * @param numDims
     * @return d1 = doc indices, d2 = dimension indices
     */
    public static List<List<Double>> projectDatasetByText(Dataset dataset, int numDims) {

        int numThreads = 1;
        Properties props = System.getProperties();

        IteratorFactory.setProperties(props);

        LatentSemanticAnalysis space = null;
        try {
            space = new LatentSemanticAnalysis();
        } catch (IOException ioe) {
            throw new IOError(ioe);
        }

        Collection<Iterator<Document>> docIters =
                new LinkedList<Iterator<Document>>();

        // add documents to iterator
        // @TODO go around documents, directly pass in text contents

        Iterator<Document> docIter = new CombinedIterator<Document>(docIters);


        //parseDocumentsMultiThreaded(space, docIter, numThreads);  // leave out multiple threads for now, for simplicity
//        Collection<Thread> threads = new LinkedList<Thread>();

        final AtomicInteger count = new AtomicInteger(0);

//        for (int i = 0; i < numThreads; ++i) {
//            Thread t = new Thread() {
//                public void run() {
//                    // repeatedly try to process documents while some still
//                    // remain
//                    while (docIter.hasNext()) {
        for (annotator.data.Document doc : dataset.getDocuments()) {
//                        long startTime = System.currentTimeMillis();
//                        Document doc = docIter.next();
            int docNumber = count.incrementAndGet();
            int terms = 0;
            try {
                //space.processAnnotatorDocument(doc);
                space.processAnnotatorDocument(doc.getTermCountMap());
            } catch (Throwable t) {
                t.printStackTrace();
            }
//                        long endTime = System.currentTimeMillis();
//                        verbose("parsed document #%d in %.3f seconds",
//                                docNumber, ((endTime - startTime) / 1000d));
//                    }
//                }
//            };
//            threads.add(t);
        }

//        long processStart = System.currentTimeMillis();
//        
//        // start all the threads processing
//        for (Thread t : threads)
//            t.start();
//
//        verbose("Beginning processing using %d threads", numThreads);
//
//        // wait until all the documents have been parsed
//        for (Thread t : threads)
//            t.join();
//
//        verbose("Processed all %d documents in %.3f total seconds",
//                count.get(),
//                ((System.currentTimeMillis() - processStart) / 1000d));            





//        long startTime = System.currentTimeMillis();
        props.setProperty(LatentSemanticAnalysis.LSA_DIMENSIONS_PROPERTY, numDims + "");
        props.setProperty(LatentSemanticAnalysis.RETAIN_DOCUMENT_SPACE_PROPERTY, true + "");
        space.processSpace(props);
//        long endTime = System.currentTimeMillis();
//        verbose("processed space in %.3f seconds",
//                ((endTime - startTime) / 1000d));

        // get dimension vals for each doc
        Set<String> words = space.getWords();
        // determine how many dimensions are used by the vectors
//        int dimensions = 0;
//        if (words.size() > 0) {
//            dimensions = space.getVectorLength();
//        }
//        writeHeader(os, SemanticSpaceIO.SSpaceFormat.TEXT);
//        // write out how many vectors there are and the number of dimensions
//        pw.println(words.size() + " " + dimensions);
//        LOGGER.fine("saving text S-Space with " + words.size() + 
//                    " words with " + dimensions + "-dimensional vectors");

        int wordIndex = 0;
        for (String word : words) {
            edu.ucla.sspace.vector.Vector wordVector = space.getVector(word);
            // debug
//            System.out.println("debug: word | value matrix:");
//            System.out.println(word + "|" + VectorIO.toString(wordVector));
            wordIndex++;
        }


        List<List<Double>> docDimVals = new ArrayList<>();
        for (int d = 0; d < dataset.getDocuments().size(); d++) {
            DoubleVector documentVector = space.getDocumentVector(d);
            List<Double> docVals = new ArrayList<>();
            docDimVals.add(docVals);
            // debug
//            System.out.print("debug: doc "+d+": "+dataset.getDocuments().get(d).getName()+", new dimensions: ("+documentVector.length()+" dims total)");
            for (int i = 0; i < numDims; i++) {
//                System.out.print(" "+documentVector.get(i));
                docVals.add(documentVector.get(i));
            }
//            System.out.println();
        }

        // need to normalize vecs, preferably onto (0,1)
        // find max val for each dim
        List<Double> maxDimVals = new ArrayList<>();
        List<Integer> maxDimIndices = new ArrayList<>();
        List<Double> minDimVals = new ArrayList<>();
        List<Integer> minDimIndices = new ArrayList<>();
        // initialize mins / maxs
        for (int i = 0; i < numDims; i++) {
            maxDimVals.add(Double.MIN_VALUE);
            maxDimIndices.add(-1);
            minDimVals.add(Double.MAX_VALUE);
            minDimIndices.add(-1);
        }
        for (int d = 0; d < dataset.getDocuments().size(); d++) {
            List<Double> docVals = docDimVals.get(d);
            for (int i = 0; i < numDims; i++) {
                double val = docVals.get(i);
                // find max
                if (val > maxDimVals.get(i)) {
                    maxDimVals.remove(i);
                    maxDimVals.add(i, val);
                    maxDimIndices.remove(i);
                    maxDimIndices.add(i, d);
                }
                // find min
                if (val < minDimVals.get(i)) {
                    minDimVals.remove(i);
                    minDimVals.add(i, val);
                    minDimIndices.remove(i);
                    minDimIndices.add(i, d);
                }
            }
        }

        // debug
//        System.out.println("debug: max & min vals: ");
//        for (int i=0; i<numDims; i++) {
//            System.out.println("debug:\tdim "+i+": min="+minDimVals.get(i)+", max="+maxDimVals.get(i));
//        }

        // adjust all vals; add abs of min to all (incl. max), divide by max
        for (int i = 0; i < numDims; i++) {
            double minVal = minDimVals.get(i);
            double maxVal = maxDimVals.get(i);
            double increment = minVal;
            if (increment < 0) {
                increment = -increment;
            }
            double divisor = increment + maxVal;
            for (int d = 0; d < docDimVals.size(); d++) {
                double oldVal = docDimVals.get(d).remove(i);
                double newVal = (oldVal + increment) / divisor;
                docDimVals.get(d).add(i, newVal);
                // debug
//                System.out.println("debug: normalizing doc "+d+", val "+i+": oldVal="+oldVal+", newVal="+newVal);
            }

        }
        
        lastDocDimVals = docDimVals;

        return docDimVals;

    }

    /**
     *
     * Projects documents from a dataset into a numDims-dimensional space based
     * on the text of the documents. Use tf-idf scores rather than frequency
     * counts.
     *
     * @param dataset
     * @param numDims
     * @return d1 = doc indices, d2 = dimension indices
     */
    public static List<List<Double>> projectDatasetByTextTfIdf(Dataset dataset, int numDims) {

//        int numThreads = 1;
        Properties props = System.getProperties();

        IteratorFactory.setProperties(props);

        LatentSemanticAnalysis space = null;
        try {
            space = new LatentSemanticAnalysis();
        } catch (IOException ioe) {
            throw new IOError(ioe);
        }

        Collection<Iterator<Document>> docIters =
                new LinkedList<Iterator<Document>>();

        // add documents to iterator
        // @TODO go around documents, directly pass in text contents

        Iterator<Document> docIter = new CombinedIterator<Document>(docIters);


        //parseDocumentsMultiThreaded(space, docIter, numThreads);  // leave out multiple threads for now, for simplicity
//        Collection<Thread> threads = new LinkedList<Thread>();

        final AtomicInteger count = new AtomicInteger(0);

//        for (int i = 0; i < numThreads; ++i) {
//            Thread t = new Thread() {
//                public void run() {
//                    // repeatedly try to process documents while some still
//                    // remain
//                    while (docIter.hasNext()) {

        Map<String, Integer> datasetTermCountMap = dataset.getDatasetTermCountMap();
        Map<String, Integer> datasetTermDocCountMap = dataset.getDatasetDocTermCountMap();
        int numDocs = dataset.getDocuments().size();

        for (annotator.data.Document doc : dataset.getDocuments()) {
//                        long startTime = System.currentTimeMillis();
//                        Document doc = docIter.next();
            int docNumber = count.incrementAndGet();
            int terms = 0;
            try {
//                            Map<String, Double> docTermTfIdfMap = doc.getTermTfIdfMap(datasetTermCountMap, datasetTermDocCountMap, numDocs);
//                            // build new map containing only ints (since processAnnotatorDocument can only handle ints
//                            // strategy: multiply each entry by a medium-sized power of 10, cut off remainder
//                            Map<String, Integer> docTermTfIdfMapInts = new HashMap<>();
//                            int multiple = 100000;
//                            for (String term : docTermTfIdfMap.keySet()) {
//                                docTermTfIdfMapInts.put(term, (int)(docTermTfIdfMap.get(term) * multiple));
//                            }
//                            space.processAnnotatorDocument(docTermTfIdfMapInts);
                space.processAnnotatorDocument(doc.getTermCountMap());
            } catch (Throwable t) {
                t.printStackTrace();
            }
//                        long endTime = System.currentTimeMillis();
//                        verbose("parsed document #%d in %.3f seconds",
//                                docNumber, ((endTime - startTime) / 1000d));
//                    }
//                }
//            };
//            threads.add(t);
        }

//        long processStart = System.currentTimeMillis();
//        
//        // start all the threads processing
//        for (Thread t : threads)
//            t.start();
//
//        verbose("Beginning processing using %d threads", numThreads);
//
//        // wait until all the documents have been parsed
//        for (Thread t : threads)
//            t.join();
//
//        verbose("Processed all %d documents in %.3f total seconds",
//                count.get(),
//                ((System.currentTimeMillis() - processStart) / 1000d));            





//        long startTime = System.currentTimeMillis();
        props.setProperty(LatentSemanticAnalysis.LSA_DIMENSIONS_PROPERTY, numDims + "");
        props.setProperty(LatentSemanticAnalysis.RETAIN_DOCUMENT_SPACE_PROPERTY, true + "");
        space.processSpace(props);
//        long endTime = System.currentTimeMillis();
//        verbose("processed space in %.3f seconds",
//                ((endTime - startTime) / 1000d));

        // get dimension vals for each doc
        Set<String> words = space.getWords();
        // determine how many dimensions are used by the vectors
//        int dimensions = 0;
//        if (words.size() > 0) {
//            dimensions = space.getVectorLength();
//        }
//        writeHeader(os, SemanticSpaceIO.SSpaceFormat.TEXT);
//        // write out how many vectors there are and the number of dimensions
//        pw.println(words.size() + " " + dimensions);
//        LOGGER.fine("saving text S-Space with " + words.size() + 
//                    " words with " + dimensions + "-dimensional vectors");

        int wordIndex = 0;
        for (String word : words) {
            edu.ucla.sspace.vector.Vector wordVector = space.getVector(word);
            // debug
//            System.out.println("debug: word | value matrix:");
//            System.out.println(word + "|" + VectorIO.toString(wordVector));
            wordIndex++;
        }


        List<List<Double>> docDimVals = new ArrayList<>();
        for (int d = 0; d < dataset.getDocuments().size(); d++) {
            DoubleVector documentVector = space.getDocumentVector(d);
            List<Double> docVals = new ArrayList<>();
            docDimVals.add(docVals);
            // debug
//            System.out.print("debug: doc "+d+": "+dataset.getDocuments().get(d).getName()+", new dimensions: ("+documentVector.length()+" dims total)");
            for (int i = 0; i < numDims; i++) {
//                System.out.print(" "+documentVector.get(i));
                docVals.add(documentVector.get(i));
            }
//            System.out.println();
        }

        // need to normalize vecs, preferably onto (0,1)
        // find max val for each dim
        List<Double> maxDimVals = new ArrayList<>();
        List<Integer> maxDimIndices = new ArrayList<>();
        List<Double> minDimVals = new ArrayList<>();
        List<Integer> minDimIndices = new ArrayList<>();
        // initialize mins / maxs
        for (int i = 0; i < numDims; i++) {
            maxDimVals.add(Double.MIN_VALUE);
            maxDimIndices.add(-1);
            minDimVals.add(Double.MAX_VALUE);
            minDimIndices.add(-1);
        }
        for (int d = 0; d < dataset.getDocuments().size(); d++) {
            List<Double> docVals = docDimVals.get(d);
            for (int i = 0; i < numDims; i++) {
                double val = docVals.get(i);
                // find max
                if (val > maxDimVals.get(i)) {
                    maxDimVals.remove(i);
                    maxDimVals.add(i, val);
                    maxDimIndices.remove(i);
                    maxDimIndices.add(i, d);
                }
                // find min
                if (val < minDimVals.get(i)) {
                    minDimVals.remove(i);
                    minDimVals.add(i, val);
                    minDimIndices.remove(i);
                    minDimIndices.add(i, d);
                }
            }
        }

        // debug
//        System.out.println("debug: max & min vals: ");
//        for (int i=0; i<numDims; i++) {
//            System.out.println("debug:\tdim "+i+": min="+minDimVals.get(i)+", max="+maxDimVals.get(i));
//        }

        // adjust all vals; add abs of min to all (incl. max), divide by max
        for (int i = 0; i < numDims; i++) {
            double minVal = minDimVals.get(i);
            double maxVal = maxDimVals.get(i);
            double increment = minVal;
            if (increment < 0) {
                increment = -increment;
            }
            double divisor = increment + maxVal;
            for (int d = 0; d < docDimVals.size(); d++) {
                double oldVal = docDimVals.get(d).remove(i);
                double newVal = (oldVal + increment) / divisor;
                docDimVals.get(d).add(i, newVal);
                // debug
//                System.out.println("debug: normalizing doc "+d+", val "+i+": oldVal="+oldVal+", newVal="+newVal);
            }

        }

        return docDimVals;

    }

    public static List<List<Double>> projectDatasetBySelectedAttrs(Dataset dataset, int numDims, List<String> selectedAttrs) {

//        int numThreads = 1;
        Properties props = System.getProperties();

        IteratorFactory.setProperties(props);

        LatentSemanticAnalysis space = null;
        try {
            space = new LatentSemanticAnalysis();
        } catch (IOException ioe) {
            throw new IOError(ioe);
        }

        Collection<Iterator<Document>> docIters =
                new LinkedList<Iterator<Document>>();

        // add documents to iterator
        // @TODO go around documents, directly pass in text contents

        Iterator<Document> docIter = new CombinedIterator<Document>(docIters);


        //parseDocumentsMultiThreaded(space, docIter, numThreads);  // leave out multiple threads for now, for simplicity
//        Collection<Thread> threads = new LinkedList<Thread>();

        final AtomicInteger count = new AtomicInteger(0);

//        for (int i = 0; i < numThreads; ++i) {
//            Thread t = new Thread() {
//                public void run() {
//                    // repeatedly try to process documents while some still
//                    // remain
//                    while (docIter.hasNext()) {

        Map<String, Integer> datasetTermCountMap = dataset.getDatasetTermCountMap();
        Map<String, Integer> datasetTermDocCountMap = dataset.getDatasetDocTermCountMap();
        int numDocs = dataset.getDocuments().size();

        for (annotator.data.Document doc : dataset.getDocuments()) {
//                        long startTime = System.currentTimeMillis();
//                        Document doc = docIter.next();
            int docNumber = count.incrementAndGet();
            int terms = 0;
            try {
//                            Map<String, Double> docTermTfIdfMap = doc.getTermTfIdfMap(datasetTermCountMap, datasetTermDocCountMap, numDocs);
//                            // build new map containing only ints (since processAnnotatorDocument can only handle ints
//                            // strategy: multiply each entry by a medium-sized power of 10, cut off remainder
//                            Map<String, Integer> docTermTfIdfMapInts = new HashMap<>();
//                            int multiple = 100000;
//                            for (String term : docTermTfIdfMap.keySet()) {
//                                docTermTfIdfMapInts.put(term, (int)(docTermTfIdfMap.get(term) * multiple));
//                            }
//                            space.processAnnotatorDocument(docTermTfIdfMapInts);
//                            space.processAnnotatorDocument(doc.getTermCountMap());
                // build attr count map
                Map<String, Integer> attrValMap = new HashMap<>();
                for (String attr : selectedAttrs) {
                    // only handle numeric attrs for now; ignore the str attrs
                    int attrValue = -1;
                    Map<String, Integer> docIndis = ((DocumentMedColon)doc).getIndicators();
                    Map<String, String> docVars = ((DocumentMedColon)doc).getVars();
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
                    attrValMap.put(attr, attrValue);
                    
                }
                space.processAnnotatorDocument(attrValMap);
            } catch (Throwable t) {
                t.printStackTrace();
            }
//                        long endTime = System.currentTimeMillis();
//                        verbose("parsed document #%d in %.3f seconds",
//                                docNumber, ((endTime - startTime) / 1000d));
//                    }
//                }
//            };
//            threads.add(t);
        }

//        long processStart = System.currentTimeMillis();
//        
//        // start all the threads processing
//        for (Thread t : threads)
//            t.start();
//
//        verbose("Beginning processing using %d threads", numThreads);
//
//        // wait until all the documents have been parsed
//        for (Thread t : threads)
//            t.join();
//
//        verbose("Processed all %d documents in %.3f total seconds",
//                count.get(),
//                ((System.currentTimeMillis() - processStart) / 1000d));            





//        long startTime = System.currentTimeMillis();
        props.setProperty(LatentSemanticAnalysis.LSA_DIMENSIONS_PROPERTY, numDims + "");
        props.setProperty(LatentSemanticAnalysis.RETAIN_DOCUMENT_SPACE_PROPERTY, true + "");
        space.processSpace(props);
//        long endTime = System.currentTimeMillis();
//        verbose("processed space in %.3f seconds",
//                ((endTime - startTime) / 1000d));

        // get dimension vals for each doc
        Set<String> words = space.getWords();
        // determine how many dimensions are used by the vectors
//        int dimensions = 0;
//        if (words.size() > 0) {
//            dimensions = space.getVectorLength();
//        }
//        writeHeader(os, SemanticSpaceIO.SSpaceFormat.TEXT);
//        // write out how many vectors there are and the number of dimensions
//        pw.println(words.size() + " " + dimensions);
//        LOGGER.fine("saving text S-Space with " + words.size() + 
//                    " words with " + dimensions + "-dimensional vectors");

        int wordIndex = 0;
        for (String word : words) {
            edu.ucla.sspace.vector.Vector wordVector = space.getVector(word);
            // debug
//            System.out.println("debug: word | value matrix:");
//            System.out.println(word + "|" + VectorIO.toString(wordVector));
            wordIndex++;
        }


        List<List<Double>> docDimVals = new ArrayList<>();
        for (int d = 0; d < dataset.getDocuments().size(); d++) {
        	DoubleVector documentVector = null;
        	try {
        		documentVector = space.getDocumentVector(d);
        	} catch (IllegalArgumentException e) {
        		// this could be thrown if we have no position attributes selected
        		//  just return the cached lastDocDimVals for now
        		e.printStackTrace();
        		System.out.println("err: LSA2dProjector.projectDatasetBySelectedAttrs: something went wrong, possibly no attrs selected? returning last 2d positions");
        		return lastDocDimVals;
        	}
            List<Double> docVals = new ArrayList<>();
            docDimVals.add(docVals);
            // debug
//            System.out.print("debug: doc "+d+": "+dataset.getDocuments().get(d).getName()+", new dimensions: ("+documentVector.length()+" dims total)");
            for (int i = 0; i < numDims; i++) {
//                System.out.print(" "+documentVector.get(i));
                docVals.add(documentVector.get(i));
            }
//            System.out.println();
        }

        // need to normalize vecs, preferably onto (0,1)
        // find max val for each dim
        List<Double> maxDimVals = new ArrayList<>();
        List<Integer> maxDimIndices = new ArrayList<>();
        List<Double> minDimVals = new ArrayList<>();
        List<Integer> minDimIndices = new ArrayList<>();
        // initialize mins / maxs
        for (int i = 0; i < numDims; i++) {
            maxDimVals.add(Double.MIN_VALUE);
            maxDimIndices.add(-1);
            minDimVals.add(Double.MAX_VALUE);
            minDimIndices.add(-1);
        }
        for (int d = 0; d < dataset.getDocuments().size(); d++) {
            List<Double> docVals = docDimVals.get(d);
            for (int i = 0; i < numDims; i++) {
                double val = docVals.get(i);
                // find max
                if (val > maxDimVals.get(i)) {
                    maxDimVals.remove(i);
                    maxDimVals.add(i, val);
                    maxDimIndices.remove(i);
                    maxDimIndices.add(i, d);
                }
                // find min
                if (val < minDimVals.get(i)) {
                    minDimVals.remove(i);
                    minDimVals.add(i, val);
                    minDimIndices.remove(i);
                    minDimIndices.add(i, d);
                }
            }
        }

        // debug
//        System.out.println("debug: max & min vals: ");
//        for (int i=0; i<numDims; i++) {
//            System.out.println("debug:\tdim "+i+": min="+minDimVals.get(i)+", max="+maxDimVals.get(i));
//        }

        // adjust all vals; add abs of min to all (incl. max), divide by max
        for (int i = 0; i < numDims; i++) {
            double minVal = minDimVals.get(i);
            double maxVal = maxDimVals.get(i);
            double increment = minVal;
            if (increment < 0) {
                increment = -increment;
            }
            double divisor = increment + maxVal;
            for (int d = 0; d < docDimVals.size(); d++) {
                double oldVal = docDimVals.get(d).remove(i);
                double newVal = (oldVal + increment) / divisor;
                docDimVals.get(d).add(i, newVal);
                // debug
//                System.out.println("debug: normalizing doc "+d+", val "+i+": oldVal="+oldVal+", newVal="+newVal);
            }

        }
        
        // add artificial jitter
        Random rand = new Random();
        for (int d = 0; d < docDimVals.size(); d++) {
        	for (int i = 0; i < numDims; i++) {
        		double oldVal = docDimVals.get(d).remove(i);
        		double jitter = (rand.nextDouble()*JITTER) - (JITTER/2);
        		double newVal = oldVal+jitter;
                docDimVals.get(d).add(i, newVal);
        	}
        }
        
        lastDocDimVals = docDimVals;

        return docDimVals;

    }

    public static List<TermScoreTuple> getTopTfIdfTermsForCluster(Dataset dataset, List<Integer> clusterIndices, int numClusters, int selectedCluster) {

        // look at all docs in cluster; find average tf*idf scores for docs in cluster
        List<annotator.data.Document> docs = dataset.getDocuments();
        Map<String, Integer> docTermCountMap = dataset.getDatasetDocTermCountMap();
        Map<String, Double> termIdfMap = new HashMap<>();
        int numDocs = docs.size();

        for (String key : docTermCountMap.keySet()) {

            double idf = Math.log((double) numDocs / (double) docTermCountMap.get(key));
            termIdfMap.put(key, idf);

        }


        Map<String, Double> termScoresForClusterMap = new HashMap<>();

        int numDocsInCluster = 0;
        for (int d = 0; d < numDocs; d++) {
            annotator.data.Document doc = docs.get(d);
            int cluster = clusterIndices.get(d);
            if (cluster == selectedCluster) {
                Map<String, Integer> docTermCounts = doc.getTermCountMap();
                for (String key : docTermCounts.keySet()) {
                    int count = docTermCounts.get(key);
                    // just count 1 appearance per doc, so that individual docs don't throw off the whole cluster
                    if (count > 1) {
                        count = 1;
                    }
                    // compute tf*idf
                    double tfIdf = count * termIdfMap.get(key);
                    if (!termScoresForClusterMap.containsKey(key)) {
                        termScoresForClusterMap.put(key, 0.);
                    }
                    termScoresForClusterMap.put(key, termScoresForClusterMap.get(key) + tfIdf);
                }
                numDocsInCluster++;
            }
        }

        List<TermScoreTuple> termScoresForCluster = new ArrayList<>();

        // for each term, create a TermScoreTuple, normalizing for # of docs
        for (String key : termScoresForClusterMap.keySet()) {
            double score = termScoresForClusterMap.get(key) / (double) numDocsInCluster;
            TermScoreTuple tuple = new TermScoreTuple(key, score);
            termScoresForCluster.add(tuple);
        }

        Collections.sort(termScoresForCluster);

        return termScoresForCluster;

    }
}
