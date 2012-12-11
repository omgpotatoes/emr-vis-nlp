package emr_vis_nlp.view;

import emr_vis_nlp.model.mpqa_colon.Document;
import java.util.*;
import prefuse.data.Table;
import prefuse.data.Tree;

/**
 * Responsibilitiies include representing a collection of documents as a
 * prefuse-style Tree for visualization as a TreeMap.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocumentTree extends Tree {

    public static final String SOURCE_KEY = "source";
    public static final String TARG_KEY = "target";
    public static final String NODE_KEY = "index";
    
    /**
     * documents to be displayed in table
     */
    private List<Document> visibleDocs;
    /**
     * indices from visible docs back to backing model
     */
    private List<Integer> docIndices;
    /**
     * ordered list of attributes structuring this tree
     */
    private List<String> orderedAttributes;

    public DocumentTree(Table nodes, Table edges, List<Document> visibleDocs, List<Integer> docIndices, List<String> orderedAttributes) {
        super(nodes, edges, NODE_KEY, SOURCE_KEY, TARG_KEY);
        
        this.visibleDocs = visibleDocs;
        this.docIndices = docIndices;
        this.orderedAttributes = orderedAttributes;
        
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
        nodes.addColumn(NODE_KEY, int.class);
        nodes.addColumn("global_doc_index", int.class);
        nodes.addColumn("local_doc_index", int.class);
        nodes.addColumn("name", String.class);
        nodes.addColumn("parent_sig", String.class);
        nodes.addColumn("is_doc", boolean.class);
        nodes.addColumn("is_root", boolean.class);

        Table edges = new Table();
        edges.addColumn(SOURCE_KEY, int.class);
        edges.addColumn(TARG_KEY, int.class);

        // assume attribute at 0 is most important, 1 2nd importance, etc. 

        // for each activeDoc, build nodeSignature and store in list
        Map<NodeSignature, List<Document>> nodeSignatureDocMap = new HashMap<>();
        Map<NodeSignature, List<Integer>> nodeSignatureIndexMap = new HashMap<>();
        
        int nodeCounter = 0;
        for (int d = 0; d < visibleDocs.size(); d++) {
            Document doc = visibleDocs.get(d);
            int localDocIndex = d;
            int globalDocIndex = docIndices.get(d);
            String docName = doc.getName();
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
            nodeSigDocList.add(doc);
            List<Integer> nodeSigIndexList = nodeSignatureIndexMap.get(nodeSignature);
            nodeSigIndexList.add(nodeCounter);
            
            // write to table
            nodes.addRow();
            nodes.set(nodeCounter, NODE_KEY, nodeCounter);
            nodes.set(nodeCounter, "global_doc_index", globalDocIndex);
            nodes.set(nodeCounter, "local_doc_index", localDocIndex);
            nodes.set(nodeCounter, "name", docName);
            nodes.set(nodeCounter, "parent_sig", nodeSignature.toString());
            nodes.set(nodeCounter, "is_doc", true);
            nodes.set(nodeCounter, "is_root", false);
            
            nodeCounter++;
        }
        
        // create initial NodeSignature nodes, connect doc nodes 
        int edgeCounter = 0;
        List<NodeSignature> parentNodes = new ArrayList<>();
        Map<NodeSignature, List<Integer>> parentNodesChildIndexMap = new HashMap<>();
        for (NodeSignature nodeSignature : nodeSignatureDocMap.keySet()) {
            List<Document> nodeSigDocList = nodeSignatureDocMap.get(nodeSignature);
            List<Integer> nodeSigIndexList = nodeSignatureIndexMap.get(nodeSignature);
            String name = nodeSignature.toString();
            NodeSignature parentSignature = nodeSignature.getParentSignature();
            if (!parentNodesChildIndexMap.containsKey(parentSignature)) {
                parentNodesChildIndexMap.put(parentSignature, new ArrayList<Integer>());
                parentNodes.add(parentSignature);
            }
            List<Integer> parentNodeChildIndexList = parentNodesChildIndexMap.get(parentSignature);
            parentNodeChildIndexList.add(nodeCounter);
            
            // build node for attr combination
            nodes.addRow();
            nodes.set(nodeCounter, NODE_KEY, nodeCounter);
            nodes.set(nodeCounter, "global_doc_index", -1);
            nodes.set(nodeCounter, "local_doc_index", -1);
            nodes.set(nodeCounter, "name", name);
            nodes.set(nodeCounter, "parent_sig", parentSignature.toString());
            nodes.set(nodeCounter, "is_doc", false);
            nodes.set(nodeCounter, "is_root", false);
            
            // build edges between node and docs
            for (int d=0; d < nodeSigDocList.size(); d++) {
                int docIndex = nodeSigIndexList.get(d);

                edges.addRow();
                edges.set(edgeCounter, SOURCE_KEY, nodeCounter);
                edges.set(edgeCounter, TARG_KEY, docIndex);

                edgeCounter++;
            }

            nodeCounter++;
        }
        
        
        // iteratively work our way up; for each nodeSignature:
        //  build parent nodeSignature 
        //   (if parent not already created, create and store in list for next iteration)
        //  build edge to parent
        
        boolean hasReachedRoot = false;
        if (parentNodes.size() == 1 && parentNodes.get(0).isRootSignature()) {
            hasReachedRoot = true;
        }
        while (!hasReachedRoot) {
            
            List<NodeSignature> nextItParentNodes = new ArrayList<>();
            Map<NodeSignature, List<Integer>> nextItParentNodesChildIndexMap = new HashMap<>();
            for (int p=0; p<parentNodes.size(); p++) {
                NodeSignature parentNodeSignature = parentNodes.get(p);
                List<Integer> childIndexList = parentNodesChildIndexMap.get(parentNodeSignature);
                String name = parentNodeSignature.toString();
                NodeSignature parentOfParentSignature = parentNodeSignature.getParentSignature();
                
                // create node for this parentNode
                nodes.addRow();
                nodes.set(nodeCounter, NODE_KEY, nodeCounter);
                nodes.set(nodeCounter, "global_doc_index", -1);
                nodes.set(nodeCounter, "local_doc_index", -1);
                nodes.set(nodeCounter, "name", name);
                nodes.set(nodeCounter, "parent_sig", parentOfParentSignature.toString());
                nodes.set(nodeCounter, "is_doc", false);
                nodes.set(nodeCounter, "is_root", false);
            
                // build edge between parentNode and each of its children
                for (int c=0; c<childIndexList.size(); c++) {
                    int childIndex = childIndexList.get(c);
                    
                    edges.addRow();
                    edges.set(edgeCounter, SOURCE_KEY, nodeCounter);
                    edges.set(edgeCounter, TARG_KEY, childIndex);

                    edgeCounter++;
                }
                
                // add parent of parent to nextIt map, if not present
                if (!nextItParentNodesChildIndexMap.containsKey(parentOfParentSignature)) {
                    nextItParentNodesChildIndexMap.put(parentOfParentSignature, new ArrayList<Integer>());
                    nextItParentNodes.add(parentOfParentSignature);
                }
                List<Integer> parentOfParentChildNodeIndexList = nextItParentNodesChildIndexMap.get(parentOfParentSignature);
                parentOfParentChildNodeIndexList.add(nodeCounter);
                
                nodeCounter++;
            }
            
            // copy over new lists, check to see whether we're at the root
            parentNodes = nextItParentNodes;
            parentNodesChildIndexMap = nextItParentNodesChildIndexMap;
            if (parentNodes.size() == 1 && parentNodes.get(0).isRootSignature()) {
                hasReachedRoot = true;
            }
            
        }
        
        // finally, connect the root node to its children
        NodeSignature rootNodeSig = parentNodes.get(0);
        List<Integer> childIndexList = parentNodesChildIndexMap.get(rootNodeSig);

        // create node for this parentNode
        nodes.addRow();
        nodes.set(nodeCounter, NODE_KEY, nodeCounter);
        nodes.set(nodeCounter, "global_doc_index", -1);
        nodes.set(nodeCounter, "local_doc_index", -1);
        nodes.set(nodeCounter, "name", "ROOT");
        nodes.set(nodeCounter, "parent_sig", "");
        nodes.set(nodeCounter, "is_doc", false);
        nodes.set(nodeCounter, "is_root", true);

        // build edge between parentNode and each of its children
        for (int c = 0; c < childIndexList.size(); c++) {
            int childIndex = childIndexList.get(c);

            edges.addRow();
            edges.set(edgeCounter, SOURCE_KEY, nodeCounter);
            edges.set(edgeCounter, TARG_KEY, childIndex);

            edgeCounter++;
        }

        nodeCounter++;

        
        // finally, built and return the Tree object!
        DocumentTree tree = new DocumentTree(nodes, edges, visibleDocs, docIndices, orderedAttributes);
        
        return tree;
        
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
        
        public boolean isRootSignature() {
            if (nodeAttrValues.isEmpty()) {
                return true;
            }
            return false;
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

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + Objects.hashCode(this.nodeAttrValues);
            return hash;
        }
    }
}
