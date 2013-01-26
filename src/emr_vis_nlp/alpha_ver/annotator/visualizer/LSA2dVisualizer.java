package annotator.visualizer;

import annotator.MainWindow;
import annotator.data.Dataset;
import annotator.data.Document;
import annotator.lrn.LSA2dProjector;
import annotator.lrn.UnsupervisedClusterer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.activity.Activity;
import prefuse.controls.*;
import prefuse.data.Table;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.AxisRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.expression.VisiblePredicate;

/**
 * 2d visualization based on force-directed layouts, clustering, zones.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class LSA2dVisualizer {

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
    
    private static SelectionManager selectionManager;

    public static void rebuildLSA2dVisualizer() {


        Dataset activeDataset = MainWindow.activeDataset;
        Display prefuseLSA2dDisplay = MainWindow.prefuseLSA2dDisplay;
        try {
            prefuseLSA2dDisplay.getVisualization().getAction("layout").cancel();
        } catch (Exception e) {
            System.out.println("debug: " + e.toString());
        }
        prefuseLSA2dDisplay.removeAll();

        // lay out nodes via LSA
        datasetSimilarityMatrix = LSA2dProjector.projectDatasetByTextTfIdf(activeDataset, 2);

        // build prefuse table
//        Table edges = new Table();
////        edges.addColumn("node1", Integer.class);
////        edges.addColumn("node2", Integer.class);
//        edges.addColumn("source", int.class);
//        edges.addColumn("target", int.class);
//        edges.addColumn("similarity", double.class);

        Table nodes = new Table();
        nodes.addColumn("index", int.class);
        nodes.addColumn("name", String.class);
        nodes.addColumn("side", int.class);
        nodes.addColumn("xpos", double.class);
        nodes.addColumn("ypos", double.class);

        // build prefuse graph datastructure from active dataset
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
            nodes.set(d, "xpos", datasetSimilarityMatrix.get(d).get(0));
            nodes.set(d, "ypos", datasetSimilarityMatrix.get(d).get(1));
        }


//        int edgeCounter = 0;
//        for (int d1=0; d1<activeDataset.getDocuments().size(); d1++) {
//            for (int d2=d1+1; d2<activeDataset.getDocuments().size(); d2++) {
//                // for now, only create edges for similar nodes (change later)
//                double similarity = datasetSimilarityMatrix.get(d1).get(d2);
//                if (similarity > 0.75) {
//                    edges.addRow();
//                    edges.set(edgeCounter, "source", d1);
//                    edges.set(edgeCounter, "target", d2);
//                    edges.set(edgeCounter, "similarity", similarity);
//                    edgeCounter++;
//                }
//            }
//        }

        // build visualization
//        Graph graph = new Graph(nodes, edges, false, "index", "source", "target");
        Visualization vis = new Visualization();
//        vis.add("graph", graph);
        vis.add("nodes", nodes);

        LabelRenderer nodeRenderer = new LabelRenderer("name");
        nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
        nodeRenderer.setHorizontalAlignment(Constants.CENTER);
        nodeRenderer.setRoundedCorner(8, 8);
        //EdgeRenderer edgeRenderer = new EdgeRenderer();

        DefaultRendererFactory rf = new DefaultRendererFactory(nodeRenderer);
        rf.add(new InGroupPredicate("ylab"),
                new AxisRenderer(Constants.FAR_LEFT, Constants.CENTER));
        rf.add(new InGroupPredicate("xlab"),
                new AxisRenderer(Constants.CENTER, Constants.FAR_BOTTOM));
        //rf.add(new InGroupPredicate("graph.edges"), edgeRenderer);
//        rf.add(new InGroupPredicate("edgeDeco"), new LabelRenderer("similarity"));

        //vis.setRendererFactory(new DefaultRendererFactory());
        vis.setRendererFactory(rf);

//        Schema decSchema = PrefuseLib.getVisualItemSchema();
//        decSchema.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(128));
//        vis.addDecorators("edgeDeco", "graph.edges", decSchema);

        int[] palette = new int[]{
            ColorLib.rgb(150, 150, 255), ColorLib.rgb(255, 150, 150), //ColorLib.rgb(150,255,150),
        //ColorLib.rgb(180,180,180),
        };

        DataColorAction nodeColor = new DataColorAction("nodes", "side",
                Constants.ORDINAL, VisualItem.FILLCOLOR, palette);

        nodeColor.add("_hover", ColorLib.gray(220, 230));
        //nodeColor.add("selected", ColorLib.gray(220, 230));

        ColorAction textColor = new ColorAction("nodes",
                VisualItem.TEXTCOLOR, ColorLib.gray(0));
        textColor.add("_hover", ColorLib.rgb(255, 0, 0));
        //textColor.add("selected", ColorLib.rgb(255, 0, 0));

//	    ColorAction edgeColor = new ColorAction("graph.edges",
//	          VisualItem.STROKECOLOR, ColorLib.rgb(200,200,200));
//	    edgeColor.add("_hover", ColorLib.rgb(0,255,0));

        FontAction fonts = new FontAction("nodes",
                FontLib.getFont("Tahoma", 10));
        fonts.add("_hover", FontLib.getFont("Tahoma", 14));
        //fonts.add("selected", FontLib.getFont("Tahoma", 14));


        ActionList color = new ActionList();
        color.add(fonts);
        color.add(textColor);
        color.add(nodeColor);
//	    color.add(edgeColor);



//        ActionList layout = new ActionList();
//        RadialTreeLayout treeLayout = new RadialTreeLayout("graph");
//        treeLayout.setAngularBounds(-Math.PI / 2, Math.PI * 3.0);
//        treeLayout.setAutoScale(true);
//        treeLayout.setRadiusIncrement(100);
//        layout.add(treeLayout);
//        layout.add(new CollapsedSubtreeLayout("graph"));
//        ActionList layout = new ActionList(Activity.INFINITY);
//        layout.add(new ForceDirectedLayout("graph", true));
//        ActionList layout = new ActionList(500);
//        layout.add(new FruchtermanReingoldLayout("graph"));
        ActionList layout = new ActionList(Activity.INFINITY);
        AxisLayout xAxis = new AxisLayout("nodes", "xpos", Constants.X_AXIS, VisiblePredicate.TRUE);
        AxisLayout yAxis = new AxisLayout("nodes", "ypos", Constants.Y_AXIS, VisiblePredicate.TRUE);
        AxisLabelLayout x_labels = new AxisLabelLayout("xlab", xAxis);
        AxisLabelLayout y_labels = new AxisLabelLayout("ylab", yAxis);



        layout.add(xAxis);
        layout.add(yAxis);

        layout.add(new RepaintAction());
        layout.add(color);
        //layout.add(new EdgeLabelLayout("edgeDeco"));
        vis.putAction("layout", layout);

//        ActionList animate = new ActionList(1250);
//        animate.setPacingFunction(new SlowInSlowOutPacer());
//        animate.add(new QualityControlAnimator());
//        animate.add(new VisibilityAnimator("graph"));
//        animate.add(new PolarLocationAnimator("graph.nodes", "linear"));
//        //animate.add(new ColorAnimator("graph.nodes"));
//        animate.add(new RepaintAction());
//        vis.putAction("animate", animate);

        //Display display = new Display(vis);
        prefuseLSA2dDisplay.setVisualization(vis);
        //prefuseLSA2dDisplay.addControlListener(new DragControl()); // drag items around
        prefuseLSA2dDisplay.addControlListener(new PanControl());  // pan with background left-drag
        prefuseLSA2dDisplay.addControlListener(new ZoomControl()); // zoom with vertical right-drag
        prefuseLSA2dDisplay.addControlListener(new NodeClickControl());
        prefuseLSA2dDisplay.setBackground(Color.getHSBColor((float) 0.12, (float) 0.05, (float) 0.76));

//        ActionList edgeActions = new ActionList();
//        edgeActions.add(getEdgeActions(0.75, 0.80, 0.85, 0.90, 0.95));
//        vis.putAction("edges", edgeActions);

        vis.run("layout");
//        vis.run("nodes");
//        vis.run("edges");

    }
    private static List<List<Double>> datasetSimilarityMatrix;
    private static List<String> attrNamesForClustering;
    private static List<Integer> datasetClusters;

    /**
     * 2d LSA visualization based on custom controls
     *
     */
    public static void rebuildCustomLSA2dVisualizer() {


        Dataset activeDataset = MainWindow.activeDataset;
        Display prefuseLSA2dDisplay = MainWindow.prefuseLSA2dDisplayControlled;
        try {
            prefuseLSA2dDisplay.getVisualization().getAction("layout").cancel();
        } catch (Exception e) {
            System.out.println("debug: " + e.toString());
        }

        prefuseLSA2dDisplay.removeAll();

        // lay out nodes via LSA
        boolean useTextForPosition = MainWindow.window.get2dTextUsePosition();
        boolean useTextForColor = MainWindow.window.get2dTextUseColor();
        List<String> attrNamesForProjection = MainWindow.window.getAttrNamesFor2dProjection();
        if (useTextForPosition) {
            datasetSimilarityMatrix = LSA2dProjector.projectDatasetByTextTfIdf(activeDataset, 2);
        } else {
            // @TODO fix exceptions being thrown here
            datasetSimilarityMatrix = LSA2dProjector.projectDatasetBySelectedAttrs(activeDataset, 2, attrNamesForProjection);
        }

        // cluster for colors
        int numClusters = MainWindow.window.getNumClustersLDA2dCustom();
        if (useTextForColor) {
        	attrNamesForClustering = new ArrayList<>();
            datasetClusters = UnsupervisedClusterer.predictKNNClustersBOW(activeDataset, numClusters);
        } else if (!useTextForColor && !MainWindow.customSelectionModeEnabled) {
            attrNamesForClustering = ((DatasetVarsTableModel) MainWindow.datasetVarsTableModel).getActiveClusterAttrs();
            datasetClusters = UnsupervisedClusterer.predictKNNClustersSelectedAttrs(activeDataset, numClusters, attrNamesForClustering);
        } else if (!useTextForColor && MainWindow.customSelectionModeEnabled) {
        	// doesn't matter what we assign in this case, since selection will all be custom by user
        	attrNamesForClustering = new ArrayList<>();
        	datasetClusters = new ArrayList<>();
        	for (int i=0; i<activeDataset.getDocuments().size(); i++) {
        		datasetClusters.add(0);
        	}
        }

        refreshCustomLSA2dVisualizer();


    }

    public static void refreshCustomLSA2dVisualizer() {

        Dataset activeDataset = MainWindow.activeDataset;
        Display prefuseLSA2dDisplay = MainWindow.prefuseLSA2dDisplayControlled;
        try {
            prefuseLSA2dDisplay.getVisualization().getAction("layout").cancel();
        } catch (Exception e) {
            System.out.println("debug: " + e.toString());
        }
        prefuseLSA2dDisplay.removeAll();


        int numClusters = MainWindow.window.getNumClustersLDA2dCustom();
        if (!MainWindow.customSelectionModeEnabled) {
        	datasetClusters = UnsupervisedClusterer.predictKNNClustersSelectedAttrs(activeDataset, numClusters, attrNamesForClustering);
        } else {
        	datasetClusters = new ArrayList<>();
        	for (int i=0; i<activeDataset.getDocuments().size(); i++) {
        		datasetClusters.add(0);
        	}
        }

        int[] clusterPalette = new int[]{
            ColorLib.rgba(255, 200, 200, 150),
            ColorLib.rgba(200, 255, 200, 150),
            ColorLib.rgba(200, 200, 255, 150),
            ColorLib.rgba(155, 200, 200, 150),
            ColorLib.rgba(200, 155, 200, 150),
            ColorLib.rgba(200, 200, 155, 150),
            ColorLib.rgba(55, 200, 200, 150),
            ColorLib.rgba(200, 55, 200, 150),
            ColorLib.rgba(200, 200, 55, 150),
            ColorLib.rgba(155, 155, 155, 150),};

        Table nodes = new Table();
        nodes.addColumn("index", int.class);
        nodes.addColumn("name", String.class);
        nodes.addColumn("cluster", int.class);
        nodes.addColumn("xpos", double.class);
        nodes.addColumn("ypos", double.class);



        // build prefuse graph datastructure from active dataset
        for (int d = 0; d < activeDataset.getDocuments().size(); d++) {
            Document doc = activeDataset.getDocuments().get(d);
            nodes.addRow();
            nodes.set(d, "index", d);
            String name = doc.getName();
            nodes.set(d, "name", name);
            int cluster = datasetClusters.get(d);
            nodes.set(d, "cluster", cluster);
            nodes.set(d, "xpos", datasetSimilarityMatrix.get(d).get(0));
            nodes.set(d, "ypos", datasetSimilarityMatrix.get(d).get(1));
        }


        // build visualization
        Visualization vis = new Visualization();
        vis.add("nodes", nodes);

        LabelRenderer nodeRenderer = new LabelRenderer("name");
        nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
        nodeRenderer.setHorizontalAlignment(Constants.CENTER);
        nodeRenderer.setRoundedCorner(8, 8);
        //EdgeRenderer edgeRenderer = new EdgeRenderer();

        DefaultRendererFactory rf = new DefaultRendererFactory(nodeRenderer);
        rf.add(new InGroupPredicate("ylab"),
                new AxisRenderer(Constants.FAR_LEFT, Constants.CENTER));
        rf.add(new InGroupPredicate("xlab"),
                new AxisRenderer(Constants.CENTER, Constants.FAR_BOTTOM));

        //vis.setRendererFactory(new DefaultRendererFactory());
        vis.setRendererFactory(rf);



        ActionList color = new ActionList(Activity.INFINITY);
        //ActionList animate = new ActionList(Activity.INFINITY);
        
        
        ColorAction textColor = new ColorAction("nodes",
                VisualItem.TEXTCOLOR, ColorLib.gray(0));
        textColor.add("_hover", ColorLib.rgb(255, 0, 0));
        //textColor.add("selected", ColorLib.rgb(255, 0, 0));

        FontAction fonts = new FontAction("nodes",
                FontLib.getFont("Tahoma", 10));
        fonts.add("_hover", FontLib.getFont("Tahoma", 14));
        //fonts.add("selected", FontLib.getFont("Tahoma", 14));


        color.add(fonts);
        color.add(textColor);

        if (!MainWindow.customSelectionModeEnabled) {
            DataColorAction nodeColor = new DataColorAction("nodes", "cluster",
                    Constants.ORDINAL, VisualItem.FILLCOLOR, clusterPalette);
            nodeColor.add("_hover", ColorLib.gray(220, 230));
            //nodeColor.add("selected", ColorLib.gray(220, 230));
            color.add(nodeColor);
            //animate.add(nodeColor);
        }

        // selection manager for selecting groups of points
        selectionManager = new SelectionManager();
        if (MainWindow.customSelectionModeEnabled) {
            ColorAction nodeColorSelection = new ColorAction("nodes", VisualItem.FILLCOLOR, ColorLib.rgb(200, 200, 255));
//            nodeColorSelection.add(VisualItem.FIXED, ColorLib.rgb(255, 100, 100));
//            nodeColorSelection.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 200, 125));
            //nodeColorSelection.add("_hover", ColorLib.gray(220, 230));
            nodeColorSelection.add(selectionManager.getSelectionPredictate(), ColorLib.color(Color.CYAN));
            color.add(nodeColorSelection);
            //animate.add(nodeColorSelection);
        }

        color.add(new RepaintAction());
        
        //animate.add(new RepaintAction());

        //ActionList layout = new ActionList(Activity.INFINITY);
        ActionList layout = new ActionList();
        AxisLayout xAxis = new AxisLayout("nodes", "xpos", Constants.X_AXIS, VisiblePredicate.TRUE);
        AxisLayout yAxis = new AxisLayout("nodes", "ypos", Constants.Y_AXIS, VisiblePredicate.TRUE);
        AxisLabelLayout x_labels = new AxisLabelLayout("xlab", xAxis);
        AxisLabelLayout y_labels = new AxisLabelLayout("ylab", yAxis);

        layout.add(xAxis);
        layout.add(yAxis);
        vis.putAction("layout", layout);

//        ActionList repaint = new ActionList(Activity.INFINITY);
//        repaint.add(new RepaintAction());
//        repaint.add(color);
//        repaint.add(animate);
//        vis.putAction("repaint", repaint);
        
        vis.putAction("draw", color);
        //vis.putAction("animate", animate);
        //vis.runAfter("draw", "animate");
        
        // SelectionManager runs "color" after mouseReleased, so make sure to include this action name!
        //vis.putAction("color", animate);
        vis.putAction("color", color);

        //Display display = new Display(vis);
        prefuseLSA2dDisplay.setVisualization(vis);
        //prefuseLSA2dDisplay.addControlListener(new FocusControl(1, "name_of_actionlist_in_viz")); // 
        prefuseLSA2dDisplay.addControlListener(new PanControl(Control.RIGHT_MOUSE_BUTTON));  // pan with background left-drag
        prefuseLSA2dDisplay.addControlListener(new ZoomControl(Control.MIDDLE_MOUSE_BUTTON)); // zoom with vertical right-drag
        prefuseLSA2dDisplay.addControlListener(new WheelZoomControl());
        prefuseLSA2dDisplay.addControlListener(new DragControl()); // drag items around
        prefuseLSA2dDisplay.addControlListener(new NodeClickControl());
        prefuseLSA2dDisplay.setBackground(Color.getHSBColor((float) 0.12, (float) 0.05, (float) 0.76));

        // listeners for selecting groups of points
        prefuseLSA2dDisplay.addControlListener(selectionManager.getSelectionListener());
        prefuseLSA2dDisplay.addPaintListener(selectionManager.getSelectionListener());

        vis.run("layout");
//        vis.run("repaint");
        vis.run("draw");

    }
    
    public static List<Integer> getCustomSelectedPoints() {
    	if (selectionManager != null) {
    		return selectionManager.getSelectedIndices();
    	}
    	return new ArrayList<>();
    }

    public static Action getEdgeActions(double simThresh1, double simThresh2, double simThresh3, double simThresh4, double simThresh5) {
        ActionList edges = new ActionList();

        String pred1 = "similarity >= " + simThresh1 + " AND similarity < " + simThresh2;
        String pred2 = "similarity >= " + simThresh2 + " AND similarity < " + simThresh3;
        String pred3 = "similarity >= " + simThresh3 + " AND similarity < " + simThresh4;
        String pred4 = "similarity >= " + simThresh4 + " AND similarity < " + simThresh5;
        String pred5 = "similarity >= " + simThresh5;


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

    public static List<Integer> getDatasetClusters() {
        if (datasetClusters != null) {
            return datasetClusters;
        }
        return null;
    }
}
