package annotator.visualizer;

import annotator.MainWindow;
import annotator.data.Dataset;
import annotator.data.Document;
import annotator.lrn.UnsupervisedClusterer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.animate.PolarLocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

/**
 * 2d visualization based on force-directed layouts, clustering, zones.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class ForceZoneVisualizer {

    // make sure data/ is on your classpath :)
    public static final String NODES_FILE = "data/nodes.csv";
    public static final String EDGES_FILE = "data/edges.csv";
    public static final float HOST_HUE = (float) 0.0;  // hue for dispute edges
    public static final float ALLY_HUE = (float) 0.6;  // hue for alliances
    public static final int LV_1_SIZE = 1;  // size for the level 1 alliances / disputes
    public static final int LV_2_SIZE = 2;
    public static final int LV_3_SIZE = 3;
    public static final int LV_4_SIZE = 4;
    public static final int LV_5_SIZE = 5;
    public static final float LV_1_SAT = (float) 0.30;  // saturation for the level 1 alliances / disputes
    public static final float LV_2_SAT = (float) 0.45;
    public static final float LV_3_SAT = (float) 0.60;
    public static final float LV_4_SAT = (float) 0.75;
    public static final float LV_5_SAT = (float) 1.00;
    public static final float FG_B = (float) 1.00;  // brightness of edges
    public static final float BG_B = (float) 0.30;  // brightness of the edge outline
    //public static final float HOST_ALPHA = (float)0.65;
    //public static final float ALLY_ALPHA = (float)0.5;
    public static final float HOST_ALPHA_1 = (float) 0.25;
    public static final float ALLY_ALPHA_1 = (float) 0.1;
    public static final float HOST_ALPHA_2 = (float) 0.35;
    public static final float ALLY_ALPHA_2 = (float) 0.2;
    public static final float HOST_ALPHA_3 = (float) 0.45;
    public static final float ALLY_ALPHA_3 = (float) 0.3;
    public static final float HOST_ALPHA_4 = (float) 0.55;
    public static final float ALLY_ALPHA_4 = (float) 0.4;
    public static final float HOST_ALPHA_5 = (float) 0.65;
    public static final float ALLY_ALPHA_5 = (float) 0.5;
    public static final double TREE_ANGULAR_DIST = 3.0; // angular spacing of nodes
    public static final double TREE_RADIUS_DIST = 100.0; // this value is only used if TREE_AUTODIST is set to false
    public static final boolean TREE_AUTODIST = true;
    public static final int START_YEAR = 1816;
    public static final int END_YEAR = 2000;
    public static final double BASE_NODE_SIZE = 1;
    public static final double NODE_SCALE_FACTOR = 7;
    private static Table edges;
    private static Table nodes;
    private static List<Integer> docClusters;
    private static List<List<Double>> datasetSimilarityMatrix;
    private static int numClusters;

    public static void rebuildForceZoneVisualizer() {

        Dataset activeDataset = MainWindow.activeDataset;
        Display prefuseZoneForceDisplay = MainWindow.prefuseZoneForceDisplay;

        // build backing data structures
//        numClusters = MainWindow.window.getNumClustersForceVis();
//        docClusters = UnsupervisedClusterer.predictKNNClustersBOW(activeDataset, numClusters);  // clusters will be built in refresh**
        datasetSimilarityMatrix = activeDataset.getSimilarityMatrixCosineCounts();

        // build visualization
        refreshForceZoneVisualizer();

    }

    // old rebuildForceZoneVisualizer code, in case we need it
//        // build prefuse table
//        edges = new Table();
////        edges.addColumn("node1", Integer.class);
////        edges.addColumn("node2", Integer.class);
//        edges.addColumn("source", int.class);
//        edges.addColumn("target", int.class);
//        edges.addColumn("similarity", double.class);
//
//        nodes = new Table();
//        nodes.addColumn("index", int.class);
//        nodes.addColumn("name", String.class);
//        nodes.addColumn("side", int.class);
//        nodes.addColumn("cluster", int.class);
//
//        // note: @TODO later: use AggregateTables for each cluster, since we will have too many docs to draw each. Adds level between top-level clusters and docs.
//
//        // build prefuse graph datastructure from active dataset
//        // d1 = doc id
//        int numClusters = 10;
//        int edgeCounter = 0;
//        int nodeCounter = 0;
//        int invizNodeCounter = 0;
//        docClusters = UnsupervisedClusterer.predictKNNClustersBOW(activeDataset, numClusters);
//        for (int d = 0; d < activeDataset.getDocuments().size(); d++) {
//            Document doc = activeDataset.getDocuments().get(d);
//            nodes.addRow();
//            nodes.set(d, "index", d);
//            String name = doc.getName();
//            nodes.set(d, "name", name);
//            if (name.contains("alternet") || name.contains("huffpost")) {
//                nodes.set(d, "side", 0);
//            } else {
//                nodes.set(d, "side", 1);
//            }
//            nodes.set(d, "cluster", docClusters.get(d).intValue());
//            nodeCounter++;
//        }
//
//        datasetSimilarityMatrix = activeDataset.getSimilarityMatrixCosineCounts();
////        for (int d1=0; d1<activeDataset.getDocuments().size(); d1++) {
////            for (int d2=d1+1; d2<activeDataset.getDocuments().size(); d2++) {
////                // for now, only create edges for similar nodes (change later)
////                double similarity = datasetSimilarityMatrix.get(d1).get(d2);
////                if (similarity > 0.40) {
////                    edges.addRow();
////                    edges.set(edgeCounter, "source", d1);
////                    edges.set(edgeCounter, "target", d2);
////                    edges.set(edgeCounter, "similarity", similarity);
////                    edgeCounter++;
////                }
////            }
////        }
//
//
//        for (int c = 0; c < numClusters; c++) {
//            List<Integer> clusterMembers = new ArrayList<>();
//            // for each cluster, loop through all members and build edges
//            for (int n=0; n<nodes.getRowCount(); n++) {
//                int cluster = (int) nodes.get(n, "cluster");
//                int nodeIndex = (int) nodes.get(n, "index");
//                if (cluster == c) {
//                    clusterMembers.add(nodeIndex);
//                }
//            }
////            for (int d = 0; d < docClusters.size(); d++) {
////                int cluster = docClusters.get(d);
////                if (cluster == c) {
////                    clusterMembers.add(d);
////                }
////            }
//            
//            // @TODO create hidden centroid nodes for each cluster
////            nodes.addRow();
////            nodes.set(nodeCounter, "index", Integer.MAX_VALUE - invizNodeCounter);
////            nodes.set(nodeCounter, "name", "centroid_" + c);
////            nodes.set(nodeCounter, "side", 2);
////            nodes.set(nodeCounter, "cluster", c);
////            invizNodeCounter++;
////            nodeCounter++;
//
//
//            // build edges for each doc in cluster
//            for (int i = 0; i < clusterMembers.size(); i++) {
//                int doc1 = clusterMembers.get(i);
//                for (int j = i + 1; j < clusterMembers.size(); j++) {
//                    int doc2 = clusterMembers.get(j);
//                    edges.addRow();
//                    edges.set(edgeCounter, "source", doc1);
//                    edges.set(edgeCounter, "target", doc2);
//                    double similarity = datasetSimilarityMatrix.get(doc1).get(doc2);
//                    edges.set(edgeCounter, "similarity", similarity);
//                    edgeCounter++;
//                    // debug
//                    System.out.println("debug: similarity between " + activeDataset.getDocuments().get(doc1).getName() + ", " + activeDataset.getDocuments().get(doc1).getName() + ": " + similarity);
//                }
//                
//                // @TODO connect each node in cluster to a central (invisible) node acting as a centroid for the cluster (should be specified by nodeCounter)
////                edges.addRow();
////                edges.set(edgeCounter, "source", doc1);
////                edges.set(edgeCounter, "target", Integer.MAX_VALUE - invizNodeCounter);
////                edges.set(edgeCounter, "similarity", 999);
////                edgeCounter++;
//
//
//
//            }
//
//
//
//
//
//        }
//
//        // @TODO create edges between all "centroid" nodes
//        // loop through all nodes, connect if both have index == -1
////        for (int n1 = 0; n1 < nodes.getRowCount(); n1++) {
////            if ((int) (nodes.get(n1, "side")) == -1) {
////                for (int n2 = n1 + 1; n2 < nodes.getRowCount(); n2++) {
////                    if ((int) (nodes.get(n2, "side")) == -1) {
////                        // both nodes are centroids; create edge
////                        int sent1Index = (int) nodes.get(n1, "index");
////                        int sent2Index = (int) nodes.get(n2, "index");
////                        edges.addRow();
////                        edges.set(edgeCounter, "source", sent1Index);
////                        edges.set(edgeCounter, "target", sent2Index);
////                        edges.set(edgeCounter, "similarity", 999);
////                        edgeCounter++;
////                    }
////                }
////            }
////        }
//
//        // build visualization
//        Graph graph = new Graph(nodes, edges, false, "index", "source", "target");
//        Visualization vis = new Visualization();
//        //vis.add("graph", graph);
//
//        VisualGraph vGraph = vis.addGraph("graph", graph);
//
//        AggregateTable at = vis.addAggregates("cluster");
//        at.addColumn(VisualItem.POLYGON, float[].class);
//        at.addColumn("id", int.class);
//        List<AggregateItem> aggItems = new ArrayList<>();
//        for (int a = 0; a < numClusters; a++) {
//            AggregateItem aitem = (AggregateItem) at.addItem();
//            aggItems.add(aitem);
//            aitem.setInt("id", a);
//            for (int n = 0; n < nodes.getRowCount(); n++) {
//                int cluster = (int) nodes.get(n, "cluster");
//                if (cluster == a) {
//                    aitem.addItem((VisualItem) vGraph.getNode(n));
//                }
//            }
////            for (int d=0; d<docClusters.size(); d++) {
////                int clusterId = docClusters.get(d);
////                if (clusterId == a) {
////                    aitem.addItem((VisualItem)vGraph.getNode(d));
////                }
////            }
//        }
//
//        LabelRenderer nodeRenderer = new LabelRenderer("name");
//        nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
//        nodeRenderer.setHorizontalAlignment(Constants.CENTER);
//        nodeRenderer.setRoundedCorner(8, 8);
//        //EdgeRenderer edgeRenderer = new EdgeRenderer();
//
//
//        // draw aggregates as polygons with curved edges
//        Renderer polyR = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
//        ((PolygonRenderer) polyR).setCurveSlack(0.15f);
//
//        DefaultRendererFactory rf = new DefaultRendererFactory(nodeRenderer);
//        //rf.add(new InGroupPredicate("graph.edges"), edgeRenderer);
////        rf.add(new InGroupPredicate("edgeDeco"), new LabelRenderer("similarity"));
//        rf.add("ingroup('cluster')", polyR);
//
//
//        //vis.setRendererFactory(new DefaultRendererFactory());
//        vis.setRendererFactory(rf);
//
////        Schema decSchema = PrefuseLib.getVisualItemSchema();
////        decSchema.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(128));
////        vis.addDecorators("edgeDeco", "graph.edges", decSchema);
//
//        int[] palette = new int[]{
//            ColorLib.rgb(150, 150, 255), ColorLib.rgb(255, 150, 150), //ColorLib.rgb(150,255,150),
//        //ColorLib.rgb(180,180,180),
////            ColorLib.rgba(150, 150, 255, 0),   // inviz color
//        };
//
//        DataColorAction nodeColor = new DataColorAction("graph.nodes", "side",
//                Constants.ORDINAL, VisualItem.FILLCOLOR, palette);
//
//        nodeColor.add("_hover", ColorLib.gray(220, 230));
//
//        ColorAction textColor = new ColorAction("graph.nodes",
//                VisualItem.TEXTCOLOR, ColorLib.gray(0));
//        textColor.add("_hover", ColorLib.rgb(255, 0, 0));
//
////	    ColorAction edgeColor = new ColorAction("graph.edges",
////	          VisualItem.STROKECOLOR, ColorLib.rgb(200,200,200));
////	    edgeColor.add("_hover", ColorLib.rgb(0,255,0));
//
//        FontAction fonts = new FontAction("graph.nodes",
//                FontLib.getFont("Tahoma", 10));
//        fonts.add("_hover", FontLib.getFont("Tahoma", 14));
//
//        ColorAction aStroke = new ColorAction("cluster", VisualItem.STROKECOLOR);
//        aStroke.setDefaultColor(ColorLib.gray(200));
//        aStroke.add("_hover", ColorLib.rgb(255, 100, 100));
//        int[] palette2 = new int[]{
//            ColorLib.rgba(255, 200, 200, 150),
//            ColorLib.rgba(200, 255, 200, 150),
//            ColorLib.rgba(200, 200, 255, 150),
//            ColorLib.rgba(155, 200, 200, 150),
//            ColorLib.rgba(200, 155, 200, 150),
//            ColorLib.rgba(200, 200, 155, 150),
//            ColorLib.rgba(55, 200, 200, 150),
//            ColorLib.rgba(200, 55, 200, 150),
//            ColorLib.rgba(200, 200, 55, 150),
//            ColorLib.rgba(155, 155, 155, 150),
////            ColorLib.rgba(0, 0, 0, 0),   // the invisible color?
//        };
//        ColorAction aFill = new DataColorAction("cluster", "id",
//                Constants.NOMINAL, VisualItem.FILLCOLOR, palette2);
//
//        ActionList color = new ActionList();
//        color.add(fonts);
//        color.add(textColor);
//        color.add(nodeColor);
////	    color.add(edgeColor);
//        color.add(aStroke);
//        color.add(aFill);
//
//
////        ActionList layout = new ActionList();
////        RadialTreeLayout treeLayout = new RadialTreeLayout("graph");
////        treeLayout.setAngularBounds(-Math.PI / 2, Math.PI * 3.0);
////        treeLayout.setAutoScale(true);
////        treeLayout.setRadiusIncrement(100);
////        layout.add(treeLayout);
////        layout.add(new CollapsedSubtreeLayout("graph"));
//        ActionList layout = new ActionList(Activity.INFINITY);
//        layout.add(new ForceDirectedLayout("graph", true));
////        ActionList layout = new ActionList(500);
////        layout.add(new FruchtermanReingoldLayout("graph"));
//        layout.add(color);
//        //layout.add(new EdgeLabelLayout("edgeDeco"));
//        //layout.add(new NodeClickAction("graph", vis));
//        layout.add(new AggregateLayout("cluster"));
//        layout.add(new RepaintAction());
//        vis.putAction("layout", layout);
//
//        ActionList animate = new ActionList(1250);
//        animate.setPacingFunction(new SlowInSlowOutPacer());
//        animate.add(new QualityControlAnimator());
//        animate.add(new VisibilityAnimator("graph"));
//        animate.add(new PolarLocationAnimator("graph.nodes", "linear"));
//        //animate.add(new ColorAnimator("graph.nodes"));
//        animate.add(new RepaintAction());
//        vis.putAction("animate", animate);
//
//        //Display display = new Display(vis);
//        prefuseZoneForceDisplay.setVisualization(vis);
//        prefuseZoneForceDisplay.addControlListener(new DragControl()); // drag items around
//        prefuseZoneForceDisplay.addControlListener(new PanControl());  // pan with background left-drag
//        prefuseZoneForceDisplay.addControlListener(new ZoomControl()); // zoom with vertical right-drag
//        prefuseZoneForceDisplay.addControlListener(new NodeClickControl());
//        //prefuseZoneForceDisplay.addControlListener(new AggregateDragControl());
//        prefuseZoneForceDisplay.setBackground(Color.getHSBColor((float) 0.12, (float) 0.05, (float) 0.76));
//
//        ActionList edgeActions = new ActionList();
//        edgeActions.add(getEdgeActions(0.4, 0.45, 0.5, 0.55, 0.6));
//        vis.putAction("edges", edgeActions);
//
//        vis.run("layout");
////        vis.run("nodes");
//        vis.run("edges");
//
//    }
    /**
     * to be used as a quick, low-cost way to rebuild the visualization when a
     * param changes (such as threshold for edges)
     *
     */
    public static void refreshForceZoneVisualizer() {

        Dataset activeDataset = MainWindow.activeDataset;
        Display prefuseZoneForceDisplay = MainWindow.prefuseZoneForceDisplay;

        numClusters = MainWindow.window.getNumClustersForceVis();
        docClusters = UnsupervisedClusterer.predictKNNClustersBOW(activeDataset, numClusters);
        
        // build prefuse table
        edges = new Table();
        edges.addColumn("source", int.class);
        edges.addColumn("target", int.class);
        edges.addColumn("similarity", double.class);

        nodes = new Table();
        nodes.addColumn("index", int.class);
        nodes.addColumn("name", String.class);
        nodes.addColumn("side", int.class);
        nodes.addColumn("cluster", int.class);

        // note: @TODO later: use AggregateTables for each cluster, since we will have too many docs to draw each. Adds level between top-level clusters and docs.

        // build prefuse graph datastructure from active dataset
        // d1 = doc id
//        numClusters = 10;
        int edgeCounter = 0;
        int nodeCounter = 0;
//        int invizNodeCounter = 0;
        
        
        for (int d = 0; d < activeDataset.getDocuments().size(); d++) {
            Document doc = activeDataset.getDocuments().get(d);
            nodes.addRow();
            nodes.set(d, "index", d);
            String name = doc.getName();
            nodes.set(d, "name", name);
            if (name.contains("alternet") || name.contains("huffpost")) {
                nodes.set(d, "side", 0);
            } else {
                nodes.set(d, "side", 1);
            }
            nodes.set(d, "cluster", docClusters.get(d).intValue());
            nodeCounter++;
        }
        
        // add a central hidden node with which to connect all other nodes
//        int hiddenNodeIndex = nodeCounter;
//        nodes.addRow();
//        nodes.set(hiddenNodeIndex, "index", hiddenNodeIndex);
//        //nodes.set(hiddenNodeIndex, "name", "hidden");
//        nodes.set(hiddenNodeIndex, "name", "");
//        nodes.set(hiddenNodeIndex, "side", 2);
//        nodes.set(hiddenNodeIndex, "cluster", -1);
        //nodeCounter++;
        

//        for (int c = 0; c < numClusters; c++) {
//            List<Integer> clusterMembers = new ArrayList<>();
//            // for each cluster, loop through all members and build edges
//            for (int n=0; n<nodes.getRowCount(); n++) {
//                int cluster = (int) nodes.get(n, "cluster");
//                int nodeIndex = (int) nodes.get(n, "index");
//                if (cluster == c) {
//                    clusterMembers.add(nodeIndex);
//                }
//            }
//
//            // build edges for each doc in cluster
//            for (int i = 0; i < clusterMembers.size(); i++) {
//                int doc1 = clusterMembers.get(i);
//                for (int j = i + 1; j < clusterMembers.size(); j++) {
//                    int doc2 = clusterMembers.get(j);
//                    edges.addRow();
//                    edges.set(edgeCounter, "source", doc1);
//                    edges.set(edgeCounter, "target", doc2);
//                    double similarity = datasetSimilarityMatrix.get(doc1).get(doc2);
//                    edges.set(edgeCounter, "similarity", similarity);
//                    edgeCounter++;
//                    // debug
//                    System.out.println("debug: similarity between " + activeDataset.getDocuments().get(doc1).getName() + ", " + activeDataset.getDocuments().get(doc1).getName() + ": " + similarity);
//                }
//                
//            }
//
//        }

        // build cluster map; nodeIndex -> cluster
        Map<Integer, Integer> clusterMap = new HashMap<>();
        // -1 as a safeguard to agglomerate points, in case some do not get a cluster assignment (may need to address this in the clustering code)
        for (int c = -1; c < numClusters; c++) {
            // debug
            //System.out.println("debug: building cluster "+c+" ...");
            List<Integer> clusterMembers = new ArrayList<>();
            // for each cluster, loop through all members and build edges
            for (int n = 0; n < nodes.getRowCount(); n++) {
                // #nodes == #docs in activeDocuments; "index" == index in activeDocuments
                int cluster = (int) nodes.get(n, "cluster");
                int nodeIndex = (int) nodes.get(n, "index");
                // debug
                //System.out.println("debug: node "+nodeIndex+" belongs to cluster "+cluster);
                if (cluster == c) {
                    // debug
                    //System.out.println("debug: adding node "+nodeIndex+" to clusterMap with cluster == "+c);
                    clusterMap.put(nodeIndex, c);
//                    clusterMembers.add(nodeIndex);
                }
            }
        }

        // build an edge for each pair of nodes within same cluster, 
        //  build an edge for each pair exceeding threshold
        //  NOTE: inter-cluster edges lead to too much chaos, abstain for now
        for (int i = 0; i < activeDataset.getDocuments().size(); i++) {
//                int doc1 = activeDataset.getDocuments().get(i);
            int doc1 = i;
            int doc1Cluster = clusterMap.get(doc1);
            
            for (int j = i + 1; j < activeDataset.getDocuments().size(); j++) {
//                    int doc2 = activeDataset.getDocuments().get(j);
                int doc2 = j;
                int doc2Cluster = clusterMap.get(doc2);

                double similarity = datasetSimilarityMatrix.get(doc1).get(doc2);
                //if (similarity > MainWindow.window.getEdgeSimThreshold() || doc1Cluster == doc2Cluster) {
                if (doc1Cluster == doc2Cluster) {
                    edges.addRow();
                    edges.set(edgeCounter, "source", doc1);
                    edges.set(edgeCounter, "target", doc2);
                    edges.set(edgeCounter, "similarity", similarity);
                    edgeCounter++;
                    // debug
//                    System.out.println("debug: similarity between " + activeDataset.getDocuments().get(doc1).getName() + ", " + activeDataset.getDocuments().get(doc1).getName() + ": " + similarity);
                }
            }
            
            // also, connect node to the hidden central node
//            edges.addRow();
//            edges.set(edgeCounter, "source", doc1);
//            edges.set(edgeCounter, "target", hiddenNodeIndex);
//            edges.set(edgeCounter, "similarity", -1);
//            edgeCounter++;
            
        }

        // build visualization
        Graph graph = new Graph(nodes, edges, false, "index", "source", "target");
        Visualization vis = new Visualization();
        //vis.add("graph", graph);

        VisualGraph vGraph = vis.addGraph("graph", graph);

        AggregateTable at = vis.addAggregates("cluster");
        at.addColumn(VisualItem.POLYGON, float[].class);
        at.addColumn("id", int.class);
        List<AggregateItem> aggItems = new ArrayList<>();
        for (int a = 0; a < numClusters; a++) {
            AggregateItem aitem = (AggregateItem) at.addItem();
            aggItems.add(aitem);
            aitem.setInt("id", a);
            for (int n = 0; n < nodes.getRowCount(); n++) {
                int cluster = (int) nodes.get(n, "cluster");
                if (cluster == a) {
                    aitem.addItem((VisualItem) vGraph.getNode(n));
                }
            }
//            for (int d=0; d<docClusters.size(); d++) {
//                int clusterId = docClusters.get(d);
//                if (clusterId == a) {
//                    aitem.addItem((VisualItem)vGraph.getNode(d));
//                }
//            }
        }

        LabelRenderer nodeRenderer = new LabelRenderer("name");
        nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
        nodeRenderer.setHorizontalAlignment(Constants.CENTER);
        nodeRenderer.setRoundedCorner(8, 8);
        //EdgeRenderer edgeRenderer = new EdgeRenderer();


        // draw aggregates as polygons with curved edges
        Renderer polyR = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
        ((PolygonRenderer) polyR).setCurveSlack(0.15f);

        DefaultRendererFactory rf = new DefaultRendererFactory(nodeRenderer);
        //rf.add(new InGroupPredicate("graph.edges"), edgeRenderer);
//        rf.add(new InGroupPredicate("edgeDeco"), new LabelRenderer("similarity"));
        rf.add("ingroup('cluster')", polyR);


        //vis.setRendererFactory(new DefaultRendererFactory());
        vis.setRendererFactory(rf);

//        Schema decSchema = PrefuseLib.getVisualItemSchema();
//        decSchema.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(128));
//        vis.addDecorators("edgeDeco", "graph.edges", decSchema);

        int[] palette = new int[]{
            ColorLib.rgb(150, 150, 255), ColorLib.rgb(255, 150, 150), //ColorLib.rgb(150,255,150),
        //ColorLib.rgb(180,180,180),
//                    ColorLib.rgba(150, 150, 255, 0),   // inviz color
        };

        DataColorAction nodeColor = new DataColorAction("graph.nodes", "side",
                Constants.ORDINAL, VisualItem.FILLCOLOR, palette);

        nodeColor.add("_hover", ColorLib.gray(220, 230));

        ColorAction textColor = new ColorAction("graph.nodes",
                VisualItem.TEXTCOLOR, ColorLib.gray(0));
        textColor.add("_hover", ColorLib.rgb(255, 0, 0));

//	    ColorAction edgeColor = new ColorAction("graph.edges",
//	          VisualItem.STROKECOLOR, ColorLib.rgb(200,200,200));
//	    edgeColor.add("_hover", ColorLib.rgb(0,255,0));

        FontAction fonts = new FontAction("graph.nodes",
                FontLib.getFont("Tahoma", 10));
        fonts.add("_hover", FontLib.getFont("Tahoma", 14));

        ColorAction aStroke = new ColorAction("cluster", VisualItem.STROKECOLOR);
        aStroke.setDefaultColor(ColorLib.gray(200));
        aStroke.add("_hover", ColorLib.rgb(255, 100, 100));
        int[] palette2 = new int[]{
            ColorLib.rgba(255, 200, 200, 150),
            ColorLib.rgba(200, 255, 200, 150),
            ColorLib.rgba(200, 200, 255, 150),
            ColorLib.rgba(155, 200, 200, 150),
            ColorLib.rgba(200, 155, 200, 150),
            ColorLib.rgba(200, 200, 155, 150),
            ColorLib.rgba(55, 200, 200, 150),
            ColorLib.rgba(200, 55, 200, 150),
            ColorLib.rgba(200, 200, 55, 150),
            ColorLib.rgba(155, 155, 155, 150), 
        };
        ColorAction aFill = new DataColorAction("cluster", "id",
                Constants.NOMINAL, VisualItem.FILLCOLOR, palette2);

        ActionList color = new ActionList();
        color.add(fonts);
        color.add(textColor);
        color.add(nodeColor);
//	    color.add(edgeColor);
        color.add(aStroke);
        color.add(aFill);


//        ActionList layout = new ActionList();
//        RadialTreeLayout treeLayout = new RadialTreeLayout("graph");
//        treeLayout.setAngularBounds(-Math.PI / 2, Math.PI * 3.0);
//        treeLayout.setAutoScale(true);
//        treeLayout.setRadiusIncrement(100);
//        layout.add(treeLayout);
//        layout.add(new CollapsedSubtreeLayout("graph"));
        ActionList layout = new ActionList(Activity.INFINITY);
        layout.add(new ForceDirectedLayout("graph", true));
//        ActionList layout = new ActionList(500);
//        layout.add(new FruchtermanReingoldLayout("graph"));
        layout.add(color);
        //layout.add(new EdgeLabelLayout("edgeDeco"));
        //layout.add(new NodeClickAction("graph", vis));
        layout.add(new AggregateLayout("cluster"));
        layout.add(new RepaintAction());
        vis.putAction("layout", layout);

        ActionList animate = new ActionList(1250);
        animate.setPacingFunction(new SlowInSlowOutPacer());
        animate.add(new QualityControlAnimator());
        animate.add(new VisibilityAnimator("graph"));
        animate.add(new PolarLocationAnimator("graph.nodes", "linear"));
        //animate.add(new ColorAnimator("graph.nodes"));
        animate.add(new RepaintAction());
        vis.putAction("animate", animate);

        //Display display = new Display(vis);
        prefuseZoneForceDisplay.setVisualization(vis);
        prefuseZoneForceDisplay.addControlListener(new DragControl()); // drag items around
        prefuseZoneForceDisplay.addControlListener(new PanControl());  // pan with background left-drag
        prefuseZoneForceDisplay.addControlListener(new ZoomControl()); // zoom with vertical right-drag
        prefuseZoneForceDisplay.addControlListener(new NodeClickControl());
        //prefuseZoneForceDisplay.addControlListener(new AggregateDragControl());
        prefuseZoneForceDisplay.setBackground(Color.getHSBColor((float) 0.12, (float) 0.05, (float) 0.76));

        ActionList edgeActions = new ActionList();
        edgeActions.add(getEdgeActions());
        vis.putAction("edges", edgeActions);

        vis.run("layout");
//        vis.run("nodes");
        vis.run("edges");

    }

    public static Action getEdgeActions(double simThresh1, double simThresh2, double simThresh3, double simThresh4, double simThresh5) {
        ActionList edges = new ActionList();

        String pred1 = "similarity >= " + simThresh1 + " AND similarity < " + simThresh2;
        String pred2 = "similarity >= " + simThresh2 + " AND similarity < " + simThresh3;
        String pred3 = "similarity >= " + simThresh3 + " AND similarity < " + simThresh4;
        String pred4 = "similarity >= " + simThresh4 + " AND similarity < " + simThresh5;
        String pred5 = "similarity >= " + simThresh5 + " AND similarity < " + 1.0;

        //TODO: refactor		
        // foreground edges
//        ColorAction edgeColor1d = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 1 AND Isdata == 'yes'"), VisualItem.STROKECOLOR, ColorLib.hsba(HOST_HUE, LV_1_SAT, FG_B, HOST_ALPHA_1));
//        ColorAction edgeSize1d = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 1 AND Isdata == 'yes'"), VisualItem.SIZE, LV_1_SIZE);
        ColorAction edgeColor1a = new ColorAction("graph.edges", ExpressionParser.predicate(pred1), VisualItem.STROKECOLOR, ColorLib.hsba(ALLY_HUE, LV_1_SAT, FG_B, ALLY_ALPHA_1));
        ColorAction edgeSize1a = new ColorAction("graph.edges", ExpressionParser.predicate(pred1), VisualItem.SIZE, LV_1_SIZE);

//        ColorAction edgeColor2d = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 2 AND Isdata == 'yes'"), VisualItem.STROKECOLOR, ColorLib.hsba(HOST_HUE, LV_2_SAT, FG_B, HOST_ALPHA_2));
//        ColorAction edgeSize2d = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 2 AND Isdata == 'yes'"), VisualItem.SIZE, LV_2_SIZE);
        ColorAction edgeColor2a = new ColorAction("graph.edges", ExpressionParser.predicate(pred2), VisualItem.STROKECOLOR, ColorLib.hsba(ALLY_HUE, LV_2_SAT, FG_B, ALLY_ALPHA_2));
        ColorAction edgeSize2a = new ColorAction("graph.edges", ExpressionParser.predicate(pred2), VisualItem.SIZE, LV_2_SIZE);

//        ColorAction edgeColor3d = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 3 AND Isdata == 'yes'"), VisualItem.STROKECOLOR, ColorLib.hsba(HOST_HUE, LV_3_SAT, FG_B, HOST_ALPHA_3));
//        ColorAction edgeSize3d = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 3 AND Isdata == 'yes'"), VisualItem.SIZE, LV_3_SIZE);
        ColorAction edgeColor3a = new ColorAction("graph.edges", ExpressionParser.predicate(pred3), VisualItem.STROKECOLOR, ColorLib.hsba(ALLY_HUE, LV_3_SAT, FG_B, ALLY_ALPHA_3));
        ColorAction edgeSize3a = new ColorAction("graph.edges", ExpressionParser.predicate(pred3), VisualItem.SIZE, LV_3_SIZE);

//        ColorAction edgeColor4d = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 4 AND Isdata == 'yes'"), VisualItem.STROKECOLOR, ColorLib.hsba(HOST_HUE, LV_4_SAT, FG_B, HOST_ALPHA_4));
//        ColorAction edgeSize4d = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 4 AND Isdata == 'yes'"), VisualItem.SIZE, LV_4_SIZE);
        ColorAction edgeColor4a = new ColorAction("graph.edges", ExpressionParser.predicate(pred4), VisualItem.STROKECOLOR, ColorLib.hsba(ALLY_HUE, LV_4_SAT, FG_B, ALLY_ALPHA_4));
        ColorAction edgeSize4a = new ColorAction("graph.edges", ExpressionParser.predicate(pred4), VisualItem.SIZE, LV_4_SIZE);

//        ColorAction edgeColor5d = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 5 AND Isdata == 'yes'"), VisualItem.STROKECOLOR, ColorLib.hsba(HOST_HUE, LV_5_SAT, FG_B, HOST_ALPHA_5));
//        ColorAction edgeSize5d = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 5 AND Isdata == 'yes'"), VisualItem.SIZE, LV_5_SIZE);
        ColorAction edgeColor5a = new ColorAction("graph.edges", ExpressionParser.predicate(pred5), VisualItem.STROKECOLOR, ColorLib.hsba(ALLY_HUE, LV_5_SAT, FG_B, ALLY_ALPHA_5));
        ColorAction edgeSize5a = new ColorAction("graph.edges", ExpressionParser.predicate(pred5), VisualItem.SIZE, LV_5_SIZE);

        // background edges
//        ColorAction edgeColor1dBg = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 1 AND Isdata == 'no'"), VisualItem.STROKECOLOR, ColorLib.hsba(HOST_HUE, LV_1_SAT, BG_B, HOST_ALPHA_1));
//        ColorAction edgeSize1dBg = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 1 AND Isdata == 'no'"), VisualItem.SIZE, LV_1_SIZE + 1);
        ColorAction edgeColor1aBg = new ColorAction("graph.edges", ExpressionParser.predicate(pred1), VisualItem.STROKECOLOR, ColorLib.hsba(ALLY_HUE, LV_1_SAT, BG_B, ALLY_ALPHA_1));
        ColorAction edgeSize1aBg = new ColorAction("graph.edges", ExpressionParser.predicate(pred1), VisualItem.SIZE, LV_1_SIZE + 1);

//        ColorAction edgeColor2dBg = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 2 AND Isdata == 'no'"), VisualItem.STROKECOLOR, ColorLib.hsba(HOST_HUE, LV_2_SAT, BG_B, HOST_ALPHA_2));
//        ColorAction edgeSize2dBg = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 2 AND Isdata == 'no'"), VisualItem.SIZE, LV_2_SIZE + 1);
        ColorAction edgeColor2aBg = new ColorAction("graph.edges", ExpressionParser.predicate(pred2), VisualItem.STROKECOLOR, ColorLib.hsba(ALLY_HUE, LV_2_SAT, BG_B, ALLY_ALPHA_2));
        ColorAction edgeSize2aBg = new ColorAction("graph.edges", ExpressionParser.predicate(pred2), VisualItem.SIZE, LV_2_SIZE + 1);

//        ColorAction edgeColor3dBg = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 3 AND Isdata == 'no'"), VisualItem.STROKECOLOR, ColorLib.hsba(HOST_HUE, LV_3_SAT, BG_B, HOST_ALPHA_3));
//        ColorAction edgeSize3dBg = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 3 AND Isdata == 'no'"), VisualItem.SIZE, LV_3_SIZE + 1);
        ColorAction edgeColor3aBg = new ColorAction("graph.edges", ExpressionParser.predicate(pred3), VisualItem.STROKECOLOR, ColorLib.hsba(ALLY_HUE, LV_3_SAT, BG_B, ALLY_ALPHA_3));
        ColorAction edgeSize3aBg = new ColorAction("graph.edges", ExpressionParser.predicate(pred3), VisualItem.SIZE, LV_3_SIZE + 1);

//        ColorAction edgeColor4dBg = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 4 AND Isdata == 'no'"), VisualItem.STROKECOLOR, ColorLib.hsba(HOST_HUE, LV_4_SAT, BG_B, HOST_ALPHA_4));
//        ColorAction edgeSize4dBg = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 4 AND Isdata == 'no'"), VisualItem.SIZE, LV_4_SIZE + 1);
        ColorAction edgeColor4aBg = new ColorAction("graph.edges", ExpressionParser.predicate(pred4), VisualItem.STROKECOLOR, ColorLib.hsba(ALLY_HUE, LV_4_SAT, BG_B, ALLY_ALPHA_4));
        ColorAction edgeSize4aBg = new ColorAction("graph.edges", ExpressionParser.predicate(pred4), VisualItem.SIZE, LV_4_SIZE + 1);

//        ColorAction edgeColor5dBg = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 5 AND Isdata == 'no'"), VisualItem.STROKECOLOR, ColorLib.hsba(HOST_HUE, LV_5_SAT, BG_B, HOST_ALPHA_5));
//        ColorAction edgeSize5dBg = new ColorAction("graph.edges", ExpressionParser.predicate("Type == 'dispute' AND Level == 5 AND Isdata == 'no'"), VisualItem.SIZE, LV_5_SIZE + 1);
        ColorAction edgeColor5aBg = new ColorAction("graph.edges", ExpressionParser.predicate(pred5), VisualItem.STROKECOLOR, ColorLib.hsba(ALLY_HUE, LV_5_SAT, BG_B, ALLY_ALPHA_5));
        ColorAction edgeSize5aBg = new ColorAction("graph.edges", ExpressionParser.predicate(pred5), VisualItem.SIZE, LV_5_SIZE + 1);

//        edges.add(edgeColor1d);
//        edges.add(edgeSize1d);
//        edges.add(edgeColor2d);
//        edges.add(edgeSize2d);
//        edges.add(edgeColor3d);
//        edges.add(edgeSize3d);
//        edges.add(edgeColor4d);
//        edges.add(edgeSize4d);
//        edges.add(edgeColor5d);
//        edges.add(edgeSize5d);
        edges.add(edgeColor1a);
        edges.add(edgeSize1a);
        edges.add(edgeColor2a);
        edges.add(edgeSize2a);
        edges.add(edgeColor3a);
        edges.add(edgeSize3a);
        edges.add(edgeColor4a);
        edges.add(edgeSize4a);
        edges.add(edgeColor5a);
        edges.add(edgeSize5a);

//        edges.add(edgeColor1dBg);
//        edges.add(edgeSize1dBg);
//        edges.add(edgeColor2dBg);
//        edges.add(edgeSize2dBg);
//        edges.add(edgeColor3dBg);
//        edges.add(edgeSize3dBg);
//        edges.add(edgeColor4dBg);
//        edges.add(edgeSize4dBg);
//        edges.add(edgeColor5dBg);
//        edges.add(edgeSize5dBg);
        edges.add(edgeColor1aBg);
        edges.add(edgeSize1aBg);
        edges.add(edgeColor2aBg);
        edges.add(edgeSize2aBg);
        edges.add(edgeColor3aBg);
        edges.add(edgeSize3aBg);
        edges.add(edgeColor4aBg);
        edges.add(edgeSize4aBg);
        edges.add(edgeColor5aBg);
        edges.add(edgeSize5aBg);

        return edges;
    }

    public static Action getEdgeActions() {
        ActionList edges = new ActionList();

        double threshold = MainWindow.window.getEdgeSimThreshold();

        // divide the space between threshold and 1 into 5 parts
        double simThresh1 = threshold;
        double simThresh2 = threshold + (1 - threshold) * 1.0 / 5.0;
        double simThresh3 = threshold + (1 - threshold) * 2.0 / 5.0;
        double simThresh4 = threshold + (1 - threshold) * 3.0 / 5.0;
        double simThresh5 = threshold + (1 - threshold) * 4.0 / 5.0;
        
        return getEdgeActions(simThresh1, simThresh2, simThresh3, simThresh4, simThresh5);
        
    }
}
