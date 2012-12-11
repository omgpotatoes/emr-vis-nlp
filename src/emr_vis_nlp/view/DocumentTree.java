package emr_vis_nlp.view;

import emr_vis_nlp.model.mpqa_colon.Document;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import prefuse.data.Table;
import prefuse.data.Tree;

/**
 * Responsibilitiies include representing a collection of documents as a
 * prefuse-style Tree for visualization as a TreeMap.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocumentTree extends Tree {

    /**
     * list of all documents
     */
    private List<Document> allDocs;
    /**
     * documents to be displayed in table
     */
    private List<Document> visibleDocs;
    /**
     * indices from visible docs back to backing model
     */
    private List<Integer> docIndices;

    public DocumentTree(Table nodes, Table edges, List<Document> allDocs, List<Boolean> allDocsEnabled, List<String> orderedAttributes) {
        super(nodes, edges);

        this.allDocs = allDocs;
        visibleDocs = new ArrayList<>();
        docIndices = new ArrayList<>();

        for (int d = 0; d < allDocs.size(); d++) {
            Document doc = allDocs.get(d);
            boolean isDocEnabled = allDocsEnabled.get(d);
            if (isDocEnabled) {
                visibleDocs.add(doc);
                docIndices.add(d);
            }
        }


    }

    public static DocumentTree buildDocumentTree(List<Document> allDocs, List<Boolean> allDocsEnabled, List<String> orderedAttributes) {

        List<Document> visibleDocs = new ArrayList<>();
        List<Integer> docIndices = new ArrayList<>();

        for (int d = 0; d < allDocs.size(); d++) {
            Document doc = allDocs.get(d);
            boolean isDocEnabled = allDocsEnabled.get(d);
            if (isDocEnabled) {
                visibleDocs.add(doc);
                docIndices.add(d);
            }
        }

        // initialize tables
        Table nodes = new Table();
        nodes.addColumn("index", int.class);
        nodes.addColumn("global_doc_index", int.class);
        nodes.addColumn("local_doc_index", int.class);
        nodes.addColumn("name", String.class);
        nodes.addColumn("parent_sig", String.class);
        nodes.addColumn("is_doc", boolean.class);
        nodes.addColumn("is_root", boolean.class);

        Table edges = new Table();
        edges.addColumn("source", int.class);
        edges.addColumn("target", int.class);

        // assume attribute at 0 is most important, 1 2nd importance, etc. 

        // for each activeDoc, build nodeSignature and store in list
        Map<NodeSignature, List<Document>> nodeSignatureDocMap = new HashMap<>();
        Map<NodeSignature, List<Integer>> nodeSignatureIndexMap = new HashMap<>();
        
        for (int d = 0; d < visibleDocs.size(); d++) {
            Document doc = visibleDocs.get(d);
            int localDocIndex = d;
            int globalDocIndex = docIndices.get(d);
            String name = doc.getName();
            boolean isDoc = true;
            boolean isRoot = false;
            
            // build nodeSignature
            Map<String, String> docAttributes = doc.getAttributes();
            List<String> nodeSigList = new ArrayList<>();
            for (int a=0; a<orderedAttributes.size(); a++) {
                String attribute = orderedAttributes.get(a).replace(',', ' ');
                String value = "";
                if (docAttributes.containsKey(attribute)) {
                    value = docAttributes.get(attribute).replace(',', ' ');
                }
                String element = attribute+"="+value;
                nodeSigList.add(element);
            }
            NodeSignature nodeSignature = new NodeSignature(nodeSigList);
            
            if (!nodeSignatureDocMap.containsKey(nodeSignature)) {
                nodeSignatureDocMap.put(nodeSignature, new ArrayList<Document>());
                nodeSignatureIndexMap.put(nodeSignature, new ArrayList<Integer>());
            }
            
            List<Document> nodeSigDocList = nodeSignatureDocMap.get(nodeSignature);
            List<Integer> nodeSigIndexList = nodeSignatureIndexMap.get(nodeSignature);
            
            nodeSigDocList.add(doc);
            nodeSigIndexList.add(d);
            
            
            
        }


        // connect doc nodes to appropriate nodeSignature nodes



        // iteratively work our way up; for each nodeSignature:
        //  build parent nodeSignature 
        //   (if parent not already created, create and store in list for next iteration)
        //  build edge to parent



    }

    static class NodeSignature {

        private List<String> nodeAttrValues;

        public NodeSignature(List<String> _nodeAttrValues) {
            // copy directly in constructor, to prevent alteration of other objects' arrays
            nodeAttrValues = new ArrayList<>();
            for (String val : _nodeAttrValues) {
                nodeAttrValues.add(val);
            }
        }

        public NodeSignature getParentSignature() {

            if (nodeAttrValues.size() > 0) {
                // if size != 0, remove last element and build new NodeSignature
                List<String> parentNodeAttrValues = new ArrayList<>();
                for (int v = 0; v < nodeAttrValues.size() - 1; v++) {
                    parentNodeAttrValues.add(nodeAttrValues.get(v));
                }
                return new NodeSignature(parentNodeAttrValues);
            } else {
                // if size == 0, we're already at root; return null
                return null;
            }

        }

        public List<String> getNodeAttrValues() {
            return nodeAttrValues;
        }

        @Override
        public String toString() {

            // comma-delim
            String str = "";

            for (int v = 0; v < nodeAttrValues.size(); v++) {
                String val = nodeAttrValues.get(v);
                str += val;
                if (v < nodeAttrValues.size() - 1) {
                    str += ", ";
                }
            }

            return str;

        }

        @Override
        public boolean equals(Object o) {
            try {
                NodeSignature otherNodeSig = (NodeSignature) o;
                List<String> otherNodeAttrValues = otherNodeSig.getNodeAttrValues();
                if (otherNodeAttrValues.size() == nodeAttrValues.size()) {
                    boolean allMatch = true;
                    for (int i = 0; i < nodeAttrValues.size(); i++) {
                        String val = nodeAttrValues.get(i);
                        String otherVal = otherNodeAttrValues.get(i);
                        if (!val.equals(otherVal)) {
                            allMatch = false;
                            break;
                        }
                    }
                    if (allMatch) {
                        return true;
                    }
                }
            } catch (ClassCastException e) {
            }
            return false;
        }
    }
}
