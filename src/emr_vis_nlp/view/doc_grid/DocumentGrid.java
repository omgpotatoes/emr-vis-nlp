package emr_vis_nlp.view.doc_grid;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.view.MainViewGlassPane;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.swing.*;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.animate.LocationAnimator;
import prefuse.action.animate.SizeAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.assignment.SizeAction;
import prefuse.action.distortion.Distortion;
import prefuse.action.filter.VisibilityFilter;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.AnchorUpdateControl;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.data.Tuple;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.query.SearchQueryBinding;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.GraphicsLib;
import prefuse.util.UpdateListener;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefuse.visual.expression.InGroupPredicate;

/**
 * 2d grid-based layout for documents; user selects attributes for X and Y axes,
 * documents arranged according to values for these attributes. Optionally, user
 * also selects additional auxiliary attributes and/or attribute values for
 * display, represented as glyphs / color stripes / etc.
 *
 * Loosely based (in part) on "DataMountain" example in the Prefuse toolkit.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocumentGrid extends Display {

    // table column names used by DocumentGridTable
    public static final String NODE_ID = DocumentGridTable.NODE_ID;
    public static final String NODE_NAME = DocumentGridTable.NODE_NAME;
    public static final String NODE_TEXT = DocumentGridTable.NODE_TEXT;
    public static final String NODE_ISACTIVE = DocumentGridTable.NODE_ISACTIVE;
    public static final String CONTINUOUS_SUFFIX = DocumentGridTable.CONTINUOUS_SUFFIX;
    public static final String WIDTH = DocumentGridTable.WIDTH;
    public static final String HEIGHT = DocumentGridTable.HEIGHT;
    public static final String WIDTH_START = DocumentGridTable.WIDTH_START;
    public static final String HEIGHT_START = DocumentGridTable.HEIGHT_START;
    public static final String WIDTH_END = DocumentGridTable.WIDTH_END;
    public static final String HEIGHT_END = DocumentGridTable.HEIGHT_END;
    public static final String HEIGHT_FOCUS = DocumentGridTable.HEIGHT_FOCUS;
    public static final String WIDTH_FOCUS = DocumentGridTable.WIDTH_FOCUS;
    public static final String SELECT_FOCUS = DocumentGridTable.SELECT_FOCUS;
    
    // names for prefuse-specific groups
    public static final String X_LABEL = "xlab";
    public static final String Y_LABEL = "ylab";
    public static final String ALL_LABEL = "all_label";
    
    public static final String DATA_GROUP = "data";
    
    // initial size
    private int initWidth = 700;
    private int initHeight = 600;
    
    // renderer for doc glyphs
    private DocGlyphRenderer nodeRenderer;
    // handles layout of the document glyphs
//    private DocumentGridLayout documentGridLayout;
    private DocumentGridLayoutNested documentGridLayout;
    // handles layout of the attribute value axes
//    private DocumentGridAxisLayout docGridAxisLayout;
    private DocumentGridAxisLayout docGridAxisLayout;
    // handles generation of glyphs
    private DocumentShapeAction docShapeAction;
    // handles glyph coloration
    private DocumentColorAction docColorAction;
    // handles glyph border coloration
    private DocumentBorderColorAction docBorderColorAction;
    // handles shape sizing
//    private DocumentSizeAction docSizeAction;
    // backing table containing data
    private DocumentGridTable t;
    
    // glasspane for drawing styles docs on popup
    private MainViewGlassPane glassPane;
    
    // do bad manual matching for now
    private String highlightAttr = null;
    private String highlightVal = null;
    private String shapeAttrName = null;
    private String colorAttrName = null;
    private Predicate highlightPredicate = null;
    
    // controller governing this DocumentGrid
    private MainController controller;
    
    // scale for fisheye distortion
    // important: these two numbers control the distance&size and size distortions of fisheye!
    private double fisheyeDistortScale = 25.;
    private double fisheyeDistortSize = 0.5;
    private Distortion feye;
//    private Distortion bifocal;
    private Rectangle2D feyeBoundingBox;
    private AnchorUpdateControl anchorUpdateControl;
    private DocGridDragControl docDragControl;
    private boolean fisheyeEnabled = true;
    
    // predicate controlling which document glyphs will be rendered
    private Predicate docGlyphVisiblePredicate;
    private GlyphVisibilityFilter docGlyphVisibleFilter;
    
    // binding for string searching
    private SearchQueryBinding searchQ;
    
    // buffer for document glyphs
    private int bufferPx = 2;
    
    // animators
    

    // 
    private Display display;
    
    public DocumentGrid(final DocumentGridTable t, String xAxisInitName, String yAxisInitName, String shapeInitName, String colorInitName) {
        super(new Visualization());
        display = this;
        this.t = t;
        this.controller = MainController.getMainController();
        // add data to visualization (tables, ...)
        VisualTable vt = m_vis.addTable(DATA_GROUP, t);
        colorAttrName = colorInitName;

        // init actionlist: performs initial positioning of the glyphs, axes
        ActionList init = new ActionList();
        // add document layout action
        // TODO : rather than taking only the values which are present, let's pull the valid values directly from the ML module, since some applicable values might not be present within the current documents, but we will need these regions to be present in the visualization for dragging
        List<String> xAxisInitCategories = t.getValueListForAttribute(xAxisInitName);
        List<String> yAxisInitCategories = t.getValueListForAttribute(yAxisInitName);
        documentGridLayout = new DocumentGridLayoutNested(controller, DATA_GROUP, xAxisInitName, yAxisInitName, xAxisInitCategories, yAxisInitCategories);
        init.add(documentGridLayout);
        // add axes layout action
        docGridAxisLayout = new DocumentGridAxisLayout(ALL_LABEL, documentGridLayout);
        documentGridLayout.setAxisLayout(docGridAxisLayout);
//        init.add(docGridAxisLayout);  // because of race conditions (DGAL requiring info from DGL which it doesn't have until its layout proceedure has finished), DGAL is now called directly by DGL, no longer as a separate member of the actionlist
        // add init actionlist to vis
        m_vis.putAction("init", init);
        
        
        // set up renderer for nodes, set rendererFactory
        nodeRenderer = new DocGlyphRenderer();
        // perform additional optional renderer setup here
        // add primary renderer to visualization
        DefaultRendererFactory rf = new DefaultRendererFactory();
        rf.setDefaultRenderer(nodeRenderer);
        // add predicate to control visibility of documents? (note: document visibility is now being handled by a VisibilityFilter rather than as a predicate on the renderer)
//        docGlyphVisiblePredicate = new InGroupPredicate(DATA_GROUP);
//        docGlyphVisiblePredicate = new InGroupPredicate(ALL_LABEL);
//        docGlyphVisiblePredicate = new AndPredicate(new InGroupPredicate(DATA_GROUP), ExpressionParser.predicate("[24.1 Rate of procedures where prep adequate] != \"Pass\""));
//        rf.add(docGlyphVisiblePredicate, nodeRenderer);
        // add auxiliary renderer for axes
        rf.add(new InGroupPredicate(ALL_LABEL), new DocumentGridAxisRenderer(documentGridLayout));
        m_vis.setRendererFactory(rf);

        // ActionList for simple repaint (for simple controls)
        ActionList repaint = new ActionList();
        repaint.add(new RepaintAction());
        m_vis.putAction("repaint", repaint);
        
        // ActionList for initially stabilizing the forces
//        ActionList preforce = new ActionList(1000);
////        preforce.add(new DataMountainForceLayout(true));
//        preforce.add(new DocGlyphForceLayout(false));
//        m_vis.putAction("preforce", preforce);
        
        // update actionlist: performs coloration, sizing
//        ActionList update = new ActionList(Activity.INFINITY);
        ActionList updateOnce = new ActionList();
        // size action
        // note: dynamic sizing is now controlled by the fisheye distortion
//        SizeAction sizeActionUpdate = new DocGlyphSizeAction(DATA_GROUP);
//        update.add(sizeActionUpdate);
        // IDEA: set initial sizing to roughly be a function of ## items in display, rather than as fixed coefficient?
//        docSizeAction = new DocumentSizeAction(DATA_GROUP, 1.5);
//        update.add(docSizeAction);
//        updateOnce.add(docSizeAction);
        // shape action
        // get current attrs from table
//        shapeAttrName = shapeInitName;
//        List<String> shapeInitCategories = t.getValueListForAttribute(shapeInitName);
//        docShapeAction = new DocumentShapeAction(DATA_GROUP, shapeInitName, shapeInitCategories);
//        update.add(docShapeAction);
//        updateOnce.add(docShapeAction);
        // for now, disable shape selection; simply use square glyphs to facilitate ease of text presentation
        ShapeAction squareShapeAction = new SquareShapeAction();
//        update.add(squareShapeAction);
        updateOnce.add(squareShapeAction);
        // color action(s)
        List<String> colorInitCategories = t.getValueListForAttribute(colorInitName);
        docColorAction = new DocumentColorAction(DATA_GROUP, colorInitName, colorInitCategories);
//        update.add(docColorAction);
        updateOnce.add(docColorAction);
        docBorderColorAction = new DocumentBorderColorAction(DATA_GROUP);
//        update.add(docBorderColorAction);
        updateOnce.add(docBorderColorAction);
        // visibility filter
        docGlyphVisiblePredicate = new InGroupPredicate(DATA_GROUP);
        docGlyphVisibleFilter = new GlyphVisibilityFilter(DATA_GROUP, docGlyphVisiblePredicate);
//        update.add(docGlyphVisibleFilter);
        updateOnce.add(docGlyphVisibleFilter);
        // repaint action
//        update.add(new RepaintAction());
        updateOnce.add(new RepaintAction());
        // add update actionlist to vis
//        m_vis.putAction("update", update);
        m_vis.putAction("updateOnce", updateOnce);

//
        // force action, to move docs out of the way when dragging (borrowed from datamountain)
//        final ForceDirectedLayout fl = new DocGlyphForceLayout(false);
//        ActivityListener fReset = new ActivityAdapter() {
//            @Override
//            public void activityCancelled(Activity a) {
//                fl.reset(); 
//             } 
//        };
//        ActionList forces = new ActionList(Activity.INFINITY);
//        forces.add(fl);
//        forces.addActivityListener(fReset);
//        m_vis.putAction("forces", forces);
        
        // ActionList for interspersed FDL iterations with fisheye
        // WARNING: force-directed layout and fisheye distortion do not play nicely with each other! both should NOT be simultaneously active, only one at a time at most!
//        ActionList fishForce = new ActionList();
////        preforce.add(new DataMountainForceLayout(true));
//        fishForce.add(new DataMountainForceLayout(false, -0.4f, 30f, 0f, false));
//        m_vis.putAction("fishforce", fishForce);
        
        // ActionList for performing fisheye distortion
        ActionList distort = new ActionList();
//        ActionList distort = new ActionList(Activity.INFINITY);  // high resource overhead in having this always run; should only run actionlist in response to anchor (mouse pointer) adjustment
        // manually define boundingbox for fisheye as width, height of view
        feyeBoundingBox = new Rectangle(0, 0, initWidth, initHeight);
        feye = new FisheyeDistortionDocGrid(fisheyeDistortScale, fisheyeDistortSize, feyeBoundingBox);
        feye.setSizeDistorted(true);
//        feye.setGroup(DATA_GROUP);  // don't set group; we want to handle the axes also?
        distort.add(feye);
//        bifocal = new BifocalDistortionDocGrid(0.2,4.5,feyeBoundingBox);
//        distort.add(bifocal);
//        distort.add(fishForce);
//        distort.add(forces);
        distort.add(new RepaintAction());
//        m_vis.putAction("distort", distort);
        
        ActionList animate = new ActionList(1250);
        animate.setPacingFunction(new SlowInSlowOutPacer());
        animate.add(new LocationAnimator(DATA_GROUP));
        animate.add(new SizeAction(DATA_GROUP));
        animate.add(new RepaintAction());
        m_vis.putAction("animate", animate);
        
        // get reference to glasspane
        glassPane = controller.getGlassPane();
        
        // set basic properties of the display
        setSize(700, 600);
        setBackground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createEmptyBorder(30,20,5,20));
        
        // for doc highlighting on search (partially adapted from TreeMap.java in Prefuse demo gallery)
        searchQ = new SearchQueryBinding(t, DocumentGridTable.NODE_TEXT);
        m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, searchQ.getSearchSet());
        searchQ.getPredicate().addExpressionListener(new UpdateListener() {
            @Override
            public void update(Object src) {
//                // debug
//                System.out.println("\n\ndebug: "+this.getClass().getName()+": in SEARCH_ITEMS group: ");
//                Iterator itemsInGroup = m_vis.getGroup(Visualization.SEARCH_ITEMS).tuples();
//                while (itemsInGroup.hasNext()) {
//                    Object item = itemsInGroup.next();
//                    System.out.println("debug: \t"+item.toString());
//                }
                // update focus text for all items in visualtable, wrt. query?
                // if no query, just set to all text
                // TODO
                // convert predicate to 'and' predicate (quick and dirty method for now)
                String queryStr = searchQ.getSearchSet().getQuery();
                Scanner querySplitter = new Scanner(queryStr);
                List<String> terms = new ArrayList<>();
                while (querySplitter.hasNext()) {
                    String term = querySplitter.next();
                    terms.add(term.trim());
                }
                
                // debug
//                System.out.println("debug: "+this.getClass().getName()+": query string: "+queryStr);
                int numRows = t.getRowCount();
                for (int i=0; i<numRows; i++) {
                    t.setString(i, DocumentGridTable.NODE_FOCUS_TEXT, "");
                    
                    String text = t.getString(i, DocumentGridTable.NODE_TEXT).toLowerCase();
                    boolean containsAllTerms = true;
                    for (String term : terms) {
                        if (!text.contains(term)) {
                            containsAllTerms = false;
                            break;
                        }
                    }
//                    if (!containsAllTerms) {
                        // TODO properly remove non-matching items from search set?
//                        searchQ.getSearchSet().removeTuple(t.getTuple(i));  // throws UnsupportedOperationException
//                    } else {
                        // set highlight text
                        // do very coarse "sentence-splitting"
                        List<String> focusSents = new ArrayList<>();
                        Scanner sentSplitter = new Scanner(text);
//                        sentSplitter.useDelimiter("[\\.\n]");  // split on period or newline
                        sentSplitter.useDelimiter("[\\.]");  // split on period only
                        while (sentSplitter.hasNext()) {
                            String sent = sentSplitter.next();
                            for (String term : terms) {
                                if (sent.contains(term)) {
                                    focusSents.add(sent);
                                }
                            }
                        }
                        if (focusSents.size() > 0) {
                            StringBuilder focusText = new StringBuilder();
                            focusText.append(" ... ");
                            for (String focusSent : focusSents) {
                                focusText.append(focusSent);
                                focusText.append(" ... \n");
                            }
                            t.setString(i, DocumentGridTable.NODE_FOCUS_TEXT, focusText.toString());
                        }
//                    }
                }
                
                // run repaint actions
                // TODO add animation, incorporate paint animation here
//                m_vis.cancel("animatePaint");
//                m_vis.run("fullPaint");
//                m_vis.run("animatePaint");
                m_vis.run("updateOnce");
            }
        });
        
        // set up control listeners
        // zoom with wheel
//        addControlListener(new WheelZoomControl());
        // zoom with background right-drag
//        addControlListener(new ZoomControl(Control.RIGHT_MOUSE_BUTTON));
        // pan with background left-drag
//        addControlListener(new PanControl(Control.RIGHT_MOUSE_BUTTON));
        // drag control for moving items to new cells
        docDragControl = new DocGridDragControl(DATA_GROUP, documentGridLayout, controller);
        addControlListener(docDragControl);
        // control for loading document details in glasspane
        addControlListener(new DocumentSelectControl(glassPane));
        // control for performing fisheye distortion
        anchorUpdateControl = new AnchorUpdateControl(feye, "distort");
//        addControlListener(anchorUpdateControl);
        
        // run actionlists
        
//        m_vis.alwaysRunAfter("init", "animate");
//        m_vis.alwaysRunAfter("animate", "updateOnce");
        m_vis.alwaysRunAfter("init", "updateOnce");
        
//        m_vis.alwaysRunAfter("init", "preforce");
//        m_vis.alwaysRunAfter("preforce", "updateOnce");
        
//        m_vis.runAfter("updateOnce", "init");
//        m_vis.run("updateOnce");
        
        m_vis.run("init");
        
    }
    
    /**
     * To be called when the pixel area of this Display changes.
     * 
     * @param newWidth
     * @param newHeight 
     */
    public void resetSize(int newWidth, int newHeight) {
        setSize(newWidth, newHeight);
        
        // update bounds for fisheye distortion
        feyeBoundingBox.setRect(0, 0, newWidth, newHeight);
        
        // redo layout, force initialization
//        m_vis.runAfter("preforce", "update");  // temporarily (?) disable for testing
//        m_vis.run("preforce");
//        m_vis.run("update");  // remove if forces ("runAfter") are reenabled
//        m_vis.runAfter("init", "preforce");  // temporarily (?) disable for testing
//        m_vis.run("updateOnce");
        m_vis.run("init");
//        m_vis.run("preforce");
    }
    
    /**
     * To be called when the visibility filtering predicate changes.
     * 
     * @param predStr string representation of the predicate on which to filter document visibility, or "" if no filtering should be applied
     */
    public void resetDocsVisiblePredicate(String predStr) {
        
        if (!predStr.equals("")) {
            docGlyphVisiblePredicate = new AndPredicate(new InGroupPredicate(DATA_GROUP), ExpressionParser.predicate(predStr));
        } else {
            docGlyphVisiblePredicate = new InGroupPredicate(DATA_GROUP);
        }
        
        if (docGlyphVisibleFilter != null) {
            docGlyphVisibleFilter.updatePredicate(docGlyphVisiblePredicate);
        }
        
//        m_vis.run("repaint");
        m_vis.run("updateOnce");
    }
    
    public SearchQueryBinding getSearchQuery() {
        return searchQ;
    }
    
    /**
     * Re-runs the initial layout actions for this Display's Visualization.
     * 
     */
    public void resetView() {
        m_vis.run("init");
    }
    
    public void updateView(boolean doAnimate) {
        if (doAnimate)
            m_vis.run("animate");
        else 
            m_vis.run("repaint");
    }
    
    public void updateXAxis(String xAxisAttrName) {
        updateXAxis(xAxisAttrName, false);
    }
    
    public void updateXAxis(String xAxisAttrName, boolean doUpdate) {
        // TODO : pull applicable values from ML module
        List<String> xAxisCategories = t.getValueListForAttribute(xAxisAttrName);
//        docGridAxisLayout.updateXAxis
        documentGridLayout.updateXAxis(xAxisAttrName, xAxisCategories);
        // update the visual axis indicator as well
        docGridAxisLayout.docGridLayoutUpdated();
//        m_vis.run("repaint");
        if (doUpdate)
            m_vis.run("animate");
    }
    
    public void updateYAxis(String yAxisAttrName) {
        updateYAxis(yAxisAttrName, false);
    }
    
    public void updateYAxis(String yAxisAttrName, boolean doUpdate) {
        // TODO : pull applicable values from ML module
        List<String> yAxisCategories = t.getValueListForAttribute(yAxisAttrName);
//        docGridAxisLayout.updateXAxis
        documentGridLayout.updateYAxis(yAxisAttrName, yAxisCategories);
        // update the visual axis indicator as well
        docGridAxisLayout.docGridLayoutUpdated();
//        m_vis.run("repaint");
        if (doUpdate)
            m_vis.run("animate");
    }
    
    public void updateColorAttr(String colorAttrName) {
        updateColorAttr(colorAttrName, false);
    }
    
    public void updateColorAttr(String colorAttrName, boolean doUpdate) {
        List<String> colorCategories = t.getValueListForAttribute(colorAttrName);
        // update colorAction
        this.colorAttrName = colorAttrName;
        docColorAction.updateColorAttr(colorAttrName, colorCategories);
        if (doUpdate)
            m_vis.run("repaint");
    }
    
    public void updateShapeAttr(String shapeAttrName) {
        updateShapeAttr(shapeAttrName, false);
    }
    
    public void updateShapeAttr(String shapeAttrName, boolean doUpdate) {
        if (docShapeAction != null) {
            this.shapeAttrName = shapeAttrName;
            List<String> shapeCategories = t.getValueListForAttribute(shapeAttrName);
            // update shapeAction
            docShapeAction.updateShapeAttr(shapeAttrName, shapeCategories);
            if (doUpdate)
                m_vis.run("repaint");
        }
    }

    public void resetHighlightPredicate() {
        highlightPredicate = null;
        m_vis.run("repaint");
    }
    
    public void setHighlightPredicate(String attrName, String attrVal) {
        highlightPredicate = ExpressionParser.predicate("'"+attrName+"' == '"+attrVal+"'");
        highlightAttr = attrName;
        highlightVal = attrVal;
        m_vis.run("repaint");
    }
    
    public void enableMouseListeners() {
        anchorUpdateControl.setEnabled(true);
    }
    
    public void setFisheyeEnabled(boolean fisheyeEnabled) {
        this.fisheyeEnabled = fisheyeEnabled;
        if (fisheyeEnabled) {
            // enable the fisheye distortion
            m_vis.run("distort");
            anchorUpdateControl.setEnabled(true);
        } else {
            // freeze the fisheye distortion
            m_vis.cancel("distort");
            anchorUpdateControl.setEnabled(false);
        }
    }
    
    
    
    /**
     * ColorAction for assigning border colors to document glyphs.
     */
    public class DocumentBorderColorAction extends ColorAction {

        public DocumentBorderColorAction(String group) {
            super(group, VisualItem.STROKECOLOR);
        }

        @Override
        public int getColor(VisualItem item) {
            
            // highlight border of glyphs for which search is true
            // TODO: thicker borders? more outstanding highlighting?
//            if ( m_vis.isInGroup(item, Visualization.SEARCH_ITEMS) )
//                return ColorLib.rgb(191,99,130);
            // do (inefficient) manual comparison (for now)
            Iterator itemsInGroup = m_vis.getGroup(Visualization.SEARCH_ITEMS).tuples();
            while (itemsInGroup.hasNext()) {
                Tuple itemInGroup = (Tuple)itemsInGroup.next();
                if (item.getString(DocumentGridTable.NODE_NAME).equals(itemInGroup.getString(DocumentGridTable.NODE_NAME))) {
                    // debug
//                    System.out.println("debug: "+this.getClass().getName()+": item in group! "+item.toString());
                    return ColorLib.rgb(191,99,130);
                }
            }
            
            if (item.isHover()) {
                return Color.LIGHT_GRAY.getRGB();
            }

            // default border color
//            return ColorLib.gray(50);
            return Color.DARK_GRAY.getRGB();
        }
    }

    /**
     * Set fill and border colors based on various criteria, and font colors.
     */
    public static class DocumentColorAction extends ColorAction {

//        private ColorMap cmap = new ColorMap(
//                ColorLib.getInterpolatedPalette(10,
//                ColorLib.rgb(85, 85, 85), ColorLib.rgb(0, 0, 0)), 0, 9);
        
        // TODO: assign color based on other properties, such as whether or not a manual annotation is present?
        public static final Color[] GLYPH_COLOR_PALETTE = {
            new Color(191, 48, 48), new Color(18, 178, 37), new Color(176, 95, 220), 
            new Color(160, 82, 45), new Color(91, 229, 108),  new Color(99, 148, 220)
        };
        
        private String colorAttrName;
        private List<String> colorAttrCategories;
        private Map<String, Color> catToColorMap;
        
        public DocumentColorAction(String group, String colorAttrName, List<String> colorAttrCategories) {
            super(group, VisualItem.FILLCOLOR);
            this.colorAttrName = colorAttrName;
            this.colorAttrCategories = colorAttrCategories;
            // build map from category value to color for quick lookup
            catToColorMap = new HashMap<>();
            for (int i=0; i<colorAttrCategories.size(); i++) {
                int paletteIndex = i % GLYPH_COLOR_PALETTE.length;
                String category = colorAttrCategories.get(i);
                catToColorMap.put(category, GLYPH_COLOR_PALETTE[paletteIndex]);
            }
        }

        @Override
        public int getColor(VisualItem item) {
                       
            // get value for target attr in item
            if (item.canGetString(colorAttrName)) {
                String attrVal = item.getString(colorAttrName);
                Color attrValColor = catToColorMap.get(attrVal);
                return attrValColor.getRGB();
            }
            
            Color white = Color.WHITE;
            return white.getRGB();
            
        }
        
        /**
         * Update which attribute is currently being used for color assignment.
         * 
         * @param colorAttrName
         * @param colorAttrCategories 
         */
        public void updateColorAttr(String colorAttrName, List<String> colorAttrCategories) {
            this.colorAttrName = colorAttrName;
            this.colorAttrCategories = colorAttrCategories;
            // refresh?
            catToColorMap = new HashMap<>();
            for (int i=0; i<colorAttrCategories.size(); i++) {
                int paletteIndex = i % GLYPH_COLOR_PALETTE.length;
                String category = colorAttrCategories.get(i);
                catToColorMap.put(category, GLYPH_COLOR_PALETTE[paletteIndex]);
            }
        }
        
    }
    
//    /**
//     * Color action for setting appearance of axes labels.
//     */
//    public static class LabelColorAction extends ColorAction {
//
////        private ColorMap cmap = new ColorMap(
////                ColorLib.getInterpolatedPalette(10,
////                ColorLib.rgb(85, 85, 85), ColorLib.rgb(0, 0, 0)), 0, 9);
//
//        public LabelColorAction(String group) {
//            super(group, VisualItem.FILLCOLOR);
//        }
//
//        @Override
//        public int getColor(VisualItem item) {
//            
//            // TODO make color based upon doc properties
//            Color white = Color.WHITE;
//            return white.getRGB();
//            
//        }
//    }

    /*
     * Handles sizing of document glyphs, controls size change of glyph on
     * selection
     * 
     * note: this is now handled in renderer and doc glyph control?
     */
//    public static class DocumentSizeAction extends SizeAction {
//        
//        public static final double HOVER_SIZE_MULT = 1.5;
////        public static final double BASE_SIZE = 10;
//        
//        public DocumentSizeAction(String group, double baseSize) {
//            super(group, baseSize);
////            super(group);
//        }
//        
//        public DocumentSizeAction() {
//            super();
//        }
//        
//        // size should blow up on hover / click
//        // NOTE: dynamic sizing is now being handled by the FisheyeDistortion
//        @Override
//        public double getSize(VisualItem item) {
////            if (item.isHover()) {
////                return super.getSize(item) * HOVER_SIZE_MULT;
////            }
//            return super.getSize(item);
//        }
//        
//    }
    
    /*
     * Assigns appropriate shapes to document glyphs.
     */
    public static class DocumentShapeAction extends ShapeAction {
        
//        public static final int[] GLYPH_SHAPE_PALETTE = {
//            Constants.SHAPE_RECTANGLE, Constants.SHAPE_DIAMOND, Constants.SHAPE_ELLIPSE, 
//            Constants.SHAPE_HEXAGON, Constants.SHAPE_CROSS, Constants.SHAPE_STAR
//        };
        public static final int[] GLYPH_SHAPE_PALETTE = {Constants.SHAPE_RECTANGLE};
        
        private String shapeAttrName;
        private List<String> shapeAttrCategories;
        private Map<String, Integer> catToShapeMap;
        
        public DocumentShapeAction(String group, String shapeAttrName, List<String> shapeAttrCategories) {
            super(group);
            this.shapeAttrName = shapeAttrName;
            this.shapeAttrCategories = shapeAttrCategories;
            catToShapeMap = new HashMap<>();
            for (int i=0; i<shapeAttrCategories.size(); i++) {
                int paletteIndex = i % GLYPH_SHAPE_PALETTE.length;
                String category = shapeAttrCategories.get(i);
                catToShapeMap.put(category, GLYPH_SHAPE_PALETTE[paletteIndex]);
            }
        }
        
//        public DocumentShapeAction(String group, int shape, String shapeAttrName, List<String> shapeAttrCategories) {
//            super(group, shape);
//            this.shapeAttrName = shapeAttrName;
//            this.shapeAttrCategories = shapeAttrCategories;
//        }
        
        @Override
        public int getShape(VisualItem item) {
            
            if (item.canGetString(shapeAttrName)) {
                String attrVal = item.getString(shapeAttrName);
                int shape = catToShapeMap.get(attrVal);
                return shape;
            }
            
            return(Constants.SHAPE_RECTANGLE);
            //return super.getShape(item);
        }
        
        public void updateShapeAttr(String shapeAttrName, List<String> shapeAttrCategories) {
            this.shapeAttrName = shapeAttrName;
            this.shapeAttrCategories = shapeAttrCategories;
            catToShapeMap = new HashMap<>();
            for (int i=0; i<shapeAttrCategories.size(); i++) {
                int paletteIndex = i % GLYPH_SHAPE_PALETTE.length;
                String category = shapeAttrCategories.get(i);
                catToShapeMap.put(category, GLYPH_SHAPE_PALETTE[paletteIndex]);
            }
        }
        
    }
    
    /*
     * Simply assigns a rectangle shape to each glyph, to facilitate ease of text drawing.
     */
    public static class SquareShapeAction extends ShapeAction {
        
        public SquareShapeAction() {
            super();
        }
        
        @Override
        public int getShape(VisualItem item) {
            // disable shape selection, since shapes beyond rectangles pose additional challenges for drawing text (which are of relatively low priority for now)
            return(Constants.SHAPE_RECTANGLE);
        }
        
    }
    
//    // "renderer" which does not actually render anything; 
//    public static class NullRenderer implements prefuse.render.Renderer {
//
//        public NullRenderer() {}
//
//        @Override
//        public void render(Graphics2D gd, VisualItem vi) {
////            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public boolean locatePoint(Point2D pd, VisualItem vi) {
////            throw new UnsupportedOperationException("Not supported yet.");
//            return false;
//        }
//
//        @Override
//        public void setBounds(VisualItem vi) {
////            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }

    /*
     * Handles rendering of document glyphs and drawing of document text (for
     * selected document[s]). 
     */
    public class DocGlyphRenderer extends ShapeRenderer {
        
        // base size for visualitems (in pixels)
        // sizing is now handled in the SizeAction
//        public static final int BASE_SIZE = 10;
        
        public DocGlyphRenderer() {
//            super(BASE_SIZE);
            super();
            m_manageBounds = false;
        }
        
        @Override
        public void render(Graphics2D g, VisualItem item) {
            
            if (item.isVisible()) {
                item.setShape(Constants.SHAPE_RECTANGLE);
                RectangularShape shape = (RectangularShape) getShape(item);
                if (shape != null) {

                    shape.getBounds2D().setRect((double) item.get(VisualItem.X), (double) item.get(VisualItem.Y), item.getSize(), item.getSize());

                    // draw basic glyph

//                    super.render(g, item);
                    Color strokeColor = ColorLib.getColor(item.getStrokeColor());
                    Color fillColor = ColorLib.getColor(item.getFillColor());
                    
//                    int size = (int)item.getSize();
                    int x = (int)item.getX()+bufferPx;
                    int y = (int)item.getY()+bufferPx;
                    int w = (int)item.getDouble(WIDTH)-2*bufferPx;
                    int h = (int)item.getDouble(HEIGHT)-2*bufferPx;
                    g.setPaint(fillColor);
                    g.fillRect(x, y, w, h);
                    
                    // draw string on-top of glyph, filling the glyph's area

                    String s = "doc=" + item.getString(NODE_NAME);

                    String focusText = item.getString(DocumentGridTable.NODE_FOCUS_TEXT);
                    if (focusText != null && !focusText.equals("null") && !focusText.equals("")) {
                        s += "\n" + focusText;
                    } else {
                        s += "\n" + item.getString(NODE_TEXT);
                    }
                    
//                    double x1 = (double) item.get(VisualItem.X);
//                    double y1 = (double) item.get(VisualItem.Y);
//                    double w = (double) this.getShape(item).getBounds2D().getWidth();
//                    double h = (double) this.getShape(item).getBounds2D().getHeight();

                    // IDEA: set font size based on number of active nodes? based on size of rect?
                    int fontSize = 10;
                    
                    item.setFont(FontLib.getFont("Tahoma", Font.PLAIN, fontSize));
                    
                    Font f = item.getFont();
                    
                    // compute width, height for the given text
                    // NOTE: this logic has been moved into drawStringMultiline
//                    int[] textDims = getTextDims(g, f, s);
                    
                    // debug
//                    System.out.println("debug: "+this.getClass().getName()+": drawStringMultiline at x="+x1+", y="+y1+", w="+w+", h="+h);
                    drawStringMultiline(g, f, s, x, y, w, h);

                }

            }

        }

    }
    
    public static int[] getTextDims(Graphics2D g, Font f, String s) {

        // [0] == max width of all lines
        // [1] == total height
        int[] textDims = new int[2];

        FontMetrics fm = g.getFontMetrics(f);
        int lineH = fm.getAscent();

        Scanner lineSplitter = new Scanner(s);
        int maxW = -1;
        int lineCounter = 0;
        while (lineSplitter.hasNextLine()) {
            String line = lineSplitter.nextLine();
            int w = fm.stringWidth(line);
            if (w > maxW) {
                maxW = w;
            }
            lineCounter++;
        }
        int h = lineH * lineCounter;

        textDims[0] = maxW;
        textDims[1] = h;
        return textDims;
        
    }
    
    public static void drawStringMultiline(Graphics2D g, Font f, String s, double xPos, double yPos, double width, double height) {
        FontMetrics fm = g.getFontMetrics(f);
        int w = fm.stringWidth(s);
        int h = fm.getAscent();
        //g.setColor(Color.LIGHT_GRAY);
        g.setColor(Color.BLACK);
        g.setFont(f);
        
        Scanner lineSplitter = new Scanner(s);
        // draw as much as can fit in each item
        // read all content from scanner, storing in string lists (where each string == 1 line), each string should be as long as possible without overflowing the space
        int maxRows = (int)height/h;
        List<String> textRows = new ArrayList<>();
        while (lineSplitter.hasNextLine() && textRows.size() < maxRows) {
            String line = lineSplitter.nextLine();
            // if line is blank, insert to maintain paragraph seps
            if (line.trim().equals("")) {
                textRows.add("");
            }
            // else, pass to inner loop
            StringBuilder currentBuilder = new StringBuilder();
            int currentStrWidth = 0;
            Scanner splitter = new Scanner(line);
            while (splitter.hasNext() && textRows.size() < maxRows) {
                String token = splitter.next()+" ";
                // TODO incorporate weight detection, formatting for token?
                currentStrWidth += fm.stringWidth(token);
                if (currentStrWidth >= width) {
                    // if string length >= glyph width, build row
                    textRows.add(currentBuilder.toString());
                    currentBuilder = new StringBuilder();
                    currentBuilder.append(token);
                    currentStrWidth = fm.stringWidth(token);
                } else {
                    // if not yet at end of row, append to builder
                    currentBuilder.append(token);
                }
                
            }
            
            // if we've still space and still have things to write, add them here
            if (textRows.size() < maxRows) {
                textRows.add(currentBuilder.toString());
                currentBuilder = new StringBuilder();
                currentStrWidth = 0;
            }
            
        }
        
        // write each line to object
        for (int t=0; t<textRows.size(); t++) {
            String line = textRows.get(t);
            if (fm.stringWidth(line) <= width) {
                // ensure that string doesn't overflow the box
//                g.drawString(line, (float) (xPos-(width/2.)), (float) (yPos-(height/2.) + h * (t+1)));
                g.drawString(line, (float)xPos, (float)(yPos + h * (t+1)));
            }
        }
        
    }
    
    /**
     * This control is responsible for drawing document details information onto
     * the glasspane when appropriate.
     */
    public class DocumentSelectControl extends ControlAdapter {
        
        private boolean isPopupLoaded;
        private MainViewGlassPane glassPane;
        
        public DocumentSelectControl(MainViewGlassPane glassPane) {
            super();
            this.glassPane = glassPane;
            isPopupLoaded = false;
        }
        
        @Override
//        public void itemPressed(VisualItem item, MouseEvent e) {
        public void itemClicked(VisualItem item, MouseEvent e) {
            // load (or unload) marked-up text into glasspane on rightclick
            // glasspane text is now loaded on mouseover instead
            
            // temp: zoom on selection
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (item.canGetInt(DocumentGridTable.NODE_ID)) {
                    int nodeId = item.getInt(DocumentGridTable.NODE_ID);
                    boolean hasSelectFocus = item.getBoolean(SELECT_FOCUS);
                    boolean hasWidthFocus = item.getBoolean(WIDTH_FOCUS);
                    boolean hasHeightFocus = item.getBoolean(HEIGHT_FOCUS);
                    if (hasSelectFocus) {
                        // simply deselect the node
                        resetGlyphFocus();
                    } else {
                        // clear all other node selections
                        resetGlyphFocus();
                        // select this node
                        item.setBoolean(SELECT_FOCUS, true);
                        item.setBoolean(WIDTH_FOCUS, true);
                        item.setBoolean(HEIGHT_FOCUS, true);
                    }
                    m_vis.run("init");  // init is needed to run here, since sizing is tightly-bound with our faux-fisheye zooming
//                    m_vis.run("repaint");
                }
            }
            
        }

        @Override
        public void itemEntered(VisualItem item, MouseEvent e) {

//            if (!glassPane.wasMouseClicked() && fisheyeEnabled) {  // check to see whether this entering is due to a glasspane hiding-click; if so, don't immediately re-load glasspane!
//                if (item.canGetInt(DocumentGridTable.NODE_ID)) {
//                    // suspend fisheye, run FDL
//                    // set the focus to the current node
//                    
//                    // freeze the fisheye distortion
//                    m_vis.cancel("distort");
//                    anchorUpdateControl.setEnabled(false);
//
//                    // appear the glasspane at appropriate size & location
//                    // get relative location of Visualization
//                    int xOffset = 0;
//                    int yOffset = 0;
//                    JComponent component = display;
//                    // recursively go through this Component's ancestors, summing offset information in order to get the absolute position relative to window
//                    do {
//                        Point visLocation = component.getLocation();
//                        xOffset += visLocation.x;
//                        yOffset += visLocation.y;
//                    } while ((!component.getParent().getClass().equals(JRootPane.class)) && (component = (JComponent) component.getParent()) != null);
//                    // debug
////                    System.out.println("debug: " + this.getClass().getName() + ": offsets: " + xOffset + ", " + yOffset);
//
//                    int nodeId = item.getInt(DocumentGridTable.NODE_ID);
//                    String attrIdStr = colorAttrName;  // TODO make highlighting more general, not just based on color!
//                    AbstractDocument doc = glassPane.getAbstDoc();
//                    controller.writeDocTextWithHighlights(doc, nodeId, attrIdStr);
//                    double x = item.getX() + xOffset - (nodeRenderer.getShape(item).getBounds2D().getWidth()) / 2;
//                    double y = item.getY() + yOffset - (nodeRenderer.getShape(item).getBounds2D().getHeight()) / 2;
//                    double w = nodeRenderer.getShape(item).getBounds2D().getWidth();
//                    double h = nodeRenderer.getShape(item).getBounds2D().getHeight();
//                    glassPane.displaySizedPane((int) x, (int) y, (int) w, (int) h, item);
//                }
//            } else {
//                glassPane.setWasMouseClicked(false);
//            }
        }

        @Override
        public void itemExited(VisualItem item, MouseEvent e) {
            // resume the fisheye distortions
            //  NOTE: this functionality (resuming the distortions) is now handled by the glasspane, since the mouse 

        }
        
        
        public void resetGlyphFocus() {
            // iterate over table, reset the focus columns
            int rowCount = t.getRowCount();
            for (int r=0; r<rowCount; r++) {
                t.setBoolean(r, SELECT_FOCUS, false);
                t.setBoolean(r, WIDTH_FOCUS, false);
                t.setBoolean(r, HEIGHT_FOCUS, false);
            }
        }

    }
    
    /**
     * Provides access to the DragControl, so that glasspane can pass mouse
     * events directly to it. TODO: refactor this clunky way of passing around
     * mouse events.
     *
     * @return 
     */
    public DocGridDragControl getDragControl() {
        if (docDragControl != null)
            return docDragControl;
        return null;
    }

    /**
     * Provides mouse interaction controls for doc grid view. Partially adopted
     * from DataMountain$DataMountainControl example in Prefuse gallery.
     *
     */
    public class DocGridDragControl extends DragControl {

        /**
         * data group to which this control should apply
         */
        private String m_group;
        private VisualItem activeItem;
        /**
         * pointer to DocumentGridLayout for which this instance is controlling
         * dragging of items
         */
//        private DocumentGridLayout docGridLayout;
        private DocumentGridLayoutNested docGridLayout;
        private MainController controller;

        
        public DocGridDragControl(String group, DocumentGridLayoutNested docGridLayout, MainController controller) {
            super();
            m_group = group;
            this.docGridLayout = docGridLayout;
            this.controller = controller;
        }

        @Override
        public void itemClicked(VisualItem item, MouseEvent e) {
            // on left-mouse click, open the old-school document details popup
//            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
//                if (item.getGroup().equals(m_group) && item.canGetInt(DocumentGridTable.NODE_ID)) {
//                    int docID = item.getInt(DocumentGridTable.NODE_ID);
//                    controller.displayDocDetailsWindow(docID);
//                }
//            }
        }

        @Override
        public void itemPressed(VisualItem item, MouseEvent e) {
            // on right-mouse press, start dragging the document
            // TODO: fisheye distortion and document dragging don't play nicely together; region boundaries are not recomputed! perhaps we should disable this dragging, only enabling it when the fisheye isn't active? Or, dynamically recomputing region boundaries?
            // current: dragging only functions if fisheye is currently disabled
//        if (SwingUtilities.isLeftMouseButton(e)) {
//            if (SwingUtilities.isRightMouseButton(e) && !fisheyeEnabled) {
            if (SwingUtilities.isRightMouseButton(e)) {
                // debug
                System.out.println("debug: " + this.getClass().getName() + ": item pressed w/ right mouse");
                // drag
                // set the focus to the current node
                Visualization vis = item.getVisualization();
                vis.getFocusGroup(Visualization.FOCUS_ITEMS).setTuple(item);
                item.setFixed(true);
                dragged = false;
                Display d = controller.getDocumentGrid();
                down = d.getAbsoluteCoordinate(e.getPoint(), down);
                // FDL forces prevent minor occlusion while dragging. However, we don't want to run FDL if the fisheye is active!
//                vis.run("forces");
            }
        }

        @Override
        public void itemReleased(VisualItem item, MouseEvent e) {
            // when right-mouse released, release the dragged document glyph
//            if (!SwingUtilities.isRightMouseButton(e) || fisheyeEnabled) {
            if (!SwingUtilities.isRightMouseButton(e)) {
                return;
            }
            // debug
            System.out.println("debug: " + this.getClass().getName() + ": item released");
            if (dragged) {
                activeItem = null;
                item.setFixed(wasFixed);
                dragged = false;
            }
            // clear the focus
            Visualization vis = item.getVisualization();
            vis.getFocusGroup(Visualization.FOCUS_ITEMS).clear();

            
            // determine whether item is in same region or new region;
            //  if new region, call controller to update attr vals
            // TODO: compute boundaries with respect to fisheye distortion, not absolute positioning!
            // TODO : move this into onRelease, not onDrag!
            // TODO : assign region based on center of glyph, not (x,y) origin of glyph
                double x = item.getX();
                double y = item.getY();
                double w = item.getDouble(WIDTH);
                double h = item.getDouble(HEIGHT);
//            int origRegionX = -1;
//            int origRegionY = -1;
            int newRegionX = -1;
            int newRegionY = -1;
            String xAttrName = docGridLayout.getXAttr();
            String yAttrName = docGridLayout.getYAttr();
            List<String> xCats = docGridLayout.getXCats();
            List<String> yCats = docGridLayout.getYCats();
            List<Integer> xCatRegionSizes = docGridLayout.getXCatRegionSizes();
            List<Integer> yCatRegionSizes = docGridLayout.getYCatRegionSizes();
            List<Integer> xCatPositions = docGridLayout.getXCatPositions();
            List<Integer> yCatPositions = docGridLayout.getYCatPositions();
            // for each region, get start and range;
            for (int i = 0; i < xCats.size(); i++) {
                int xRegionStart = xCatPositions.get(i);
                int xRegionEnd = xRegionStart + xCatRegionSizes.get(i);
                if (xRegionStart < x + (w/2.) && x + (w/2.) < xRegionEnd) {
                    newRegionX = i;
                }
            }
            for (int i = 0; i < yCats.size(); i++) {
                int yRegionStart = yCatPositions.get(i);
                int yRegionEnd = yRegionStart + yCatRegionSizes.get(i);
                if (yRegionStart < y + (h/2.) && y + (h/2.) < yRegionEnd) {
                    newRegionY = i;
                }
            }

            int docID = item.getInt(DocumentGridTable.NODE_ID);

            // debug
//            System.out.println("debug: item moved: docID="+docID+"xOrig="+xCats.get(origRegionX)+", xNew="+xCats.get(newRegionX)+", yOrig="+yCats.get(origRegionY)+", yNew="+yCats.get(newRegionY));

            // else, invoke controller to adjust document attributes
            // update for x and y separately
//            if (origRegionX != newRegionX && newRegionX != -1) {
                String newCatX = xCats.get(newRegionX);
                controller.updateDocumentAttr(docID, xAttrName, newCatX);
                controller.documentAttributesUpdated(docID);
//            }
//            if (origRegionY != newRegionY && newRegionY != -1) {
                String newCatY = yCats.get(newRegionY);
                controller.updateDocumentAttr(docID, yAttrName, newCatY);
                controller.documentAttributesUpdated(docID);
//            }

            
            
            vis.cancel("forces");
        }

        @Override
        public void itemDragged(VisualItem item, MouseEvent e) {
            // listen during dragging via right-mouse, adjust glyph positions and monitor for crossing of boundary regions
//            if (!SwingUtilities.isRightMouseButton(e) || fisheyeEnabled) {
            if (!SwingUtilities.isRightMouseButton(e)) {
                return;
            }
            if (item.getGroup().equals(m_group)) {
                dragged = true;
//            Display d = (Display) e.getComponent();
                Display d = controller.getDocumentGrid();
                d.getAbsoluteCoordinate(e.getPoint(), temp);
                double dx = temp.getX() - down.getX();
                double dy = temp.getY() - down.getY();
                double x = item.getX();
                double y = item.getY();
                double w = item.getDouble(WIDTH);
                double h = item.getDouble(HEIGHT);
                
                // x2, y2 no longer needed, since size and positioning is now handled in a more standard manner
//                double x2 = (double) item.get(VisualItem.X2);
//                double y2 = (double) item.get(VisualItem.Y2);

                item.setStartX(x);
                item.setStartY(y);
                item.setX(x + dx);
                item.setY(y + dy);
                item.setEndX(x + dx);
                item.setEndY(y + dy);
                
                item.setBounds(x+dx, y+dy, w, h);

//                item.set(VisualItem.X2, x2 + dx);
//                item.set(VisualItem.Y2, y2 + dy);

//            item.setBounds(x + dx, y + dy, x2 + dx, y2 + dy);

                if (repaint) {
                    item.getVisualization().repaint();
                }

                down.setLocation(temp);
                if (action != null) {
                    d.getVisualization().run(action);
                }

//                // determine whether item is in same region or new region;
//                //  if new region, call controller to update attr vals
//                // TODO: compute boundaries with respect to fisheye distortion, not absolute positioning!
//                // TODO : move this into onRelease, not onDrag!
//                // TODO : assign region based on center of glyph, not (x,y) origin of glyph
//                int origRegionX = -1;
//                int origRegionY = -1;
//                int newRegionX = -1;
//                int newRegionY = -1;
//                String xAttrName = docGridLayout.getXAttr();
//                String yAttrName = docGridLayout.getYAttr();
//                List<String> xCats = docGridLayout.getXCats();
//                List<String> yCats = docGridLayout.getYCats();
//                List<Integer> xCatRegionSizes = docGridLayout.getXCatRegionSizes();
//                List<Integer> yCatRegionSizes = docGridLayout.getYCatRegionSizes();
//                List<Integer> xCatPositions = docGridLayout.getXCatPositions();
//                List<Integer> yCatPositions = docGridLayout.getYCatPositions();
//                // for each region, get start and range;
//                for (int i = 0; i < xCats.size(); i++) {
//                    int xRegionStart = xCatPositions.get(i);
//                    int xRegionEnd = xRegionStart + xCatRegionSizes.get(i);
//                    if (xRegionStart < x && x < xRegionEnd) {
//                        origRegionX = i;
//                    }
//                    if (xRegionStart < x + dx && x + dx < xRegionEnd) {
//                        newRegionX = i;
//                    }
//                }
//                for (int i = 0; i < yCats.size(); i++) {
//                    int yRegionStart = yCatPositions.get(i);
//                    int yRegionEnd = yRegionStart + yCatRegionSizes.get(i);
//                    if (yRegionStart < y && y < yRegionEnd) {
//                        origRegionY = i;
//                    }
//                    if (yRegionStart < y + dy && y + dy < yRegionEnd) {
//                        newRegionY = i;
//                    }
//                }
//
//                // if both regions are same, do nothing
//                int docID = item.getInt(DocumentGridTable.NODE_ID);
//
//                // debug
////            System.out.println("debug: item moved: docID="+docID+"xOrig="+xCats.get(origRegionX)+", xNew="+xCats.get(newRegionX)+", yOrig="+yCats.get(origRegionY)+", yNew="+yCats.get(newRegionY));
//
//                // else, invoke controller to adjust document attributes
//                // update for x and y separately
//                if (origRegionX != newRegionX && newRegionX != -1) {
//                    String newCat = xCats.get(newRegionX);
//                    controller.updateDocumentAttr(docID, xAttrName, newCat);
//                    controller.documentAttributesUpdated(docID);
//                }
//                if (origRegionY != newRegionY && newRegionY != -1) {
//                    String newCat = yCats.get(newRegionY);
//                    controller.updateDocumentAttr(docID, yAttrName, newCat);
//                    controller.documentAttributesUpdated(docID);
//                }


            }
        }

        @Override
        public void itemEntered(VisualItem item, MouseEvent e) {
            // suspend fisheye, run FDL
            // set the focus to the current node
            // NOTE: because of interference from the glasspane, this doesn't currently work
//            Visualization vis = item.getVisualization();
//            vis.getFocusGroup(Visualization.FOCUS_ITEMS).setTuple(item);
//            item.setFixed(true);
//            dragged = false;
//            Display d = (Display) e.getComponent();
//            down = d.getAbsoluteCoordinate(e.getPoint(), down);
//            vis.cancel("distort");
////            vis.run("forces");
        }

        @Override
        public void itemExited(VisualItem item, MouseEvent e) {
            // suspend FDL, run fisheye
            // NOTE: because of interference from the glasspane, this doesn't currently work
//            Visualization vis = item.getVisualization();
////            vis.cancel("forces");
//            vis.run("distort");
        }
    }
    
    
    /*
     * Force controllers adpoted (and expanded) from the DataMountain example, in order to prevent / reduce initial occlusion on layout, and to reduce occlusion on dogument glyph dragging (when distortions are not active)
     */
//    private static final String ANCHORITEM = "_anchorItem";
//    private static final Schema ANCHORITEM_SCHEMA = new Schema();
//
//    static {
//        ANCHORITEM_SCHEMA.addColumn(ANCHORITEM, ForceItem.class);
//    }
//
//    public class DocGlyphForceLayout extends ForceDirectedLayout {
//
//        public DocGlyphForceLayout(boolean enforceBounds) {
//            super("data", enforceBounds, false);
//
//            ForceSimulator fsim = new ForceSimulator();
//            // (gravConstant, minDistance, theta)
//            fsim.addForce(new NBodyForce(-0.6f, 40f, NBodyForce.DEFAULT_THETA));
////            fsim.addForce(new SpringForce(1e-5f,0f));
//            fsim.addForce(new DragForce(0.08f));
//            setForceSimulator(fsim);
//            
//            m_nodeGroup = "data";
//            m_edgeGroup = null;
//        }
//        
//        public DocGlyphForceLayout(boolean enforceBounds, float gravConstant, float minDistance, float dragCoeff, boolean runOnce) {
//            super("data",enforceBounds,runOnce);
//            
//            ForceSimulator fsim = new ForceSimulator();
//            // (gravConstant, minDistance, theta)
//            fsim.addForce(new NBodyForce(gravConstant, minDistance, NBodyForce.DEFAULT_THETA));
////            fsim.addForce(new SpringForce(1e-5f,0f));
//            fsim.addForce(new DragForce(dragCoeff));
//            setForceSimulator(fsim);
//            
//            m_nodeGroup = "data";
//            m_edgeGroup = null;
//        }
//        
//        @Override
//        protected float getMassValue(VisualItem n) {
//            return n.isHover() ? 5f : 1f;
//        }
//
//        @Override
//        public void reset() {
//            Iterator iter = m_vis.visibleItems(m_nodeGroup);
//            while ( iter.hasNext() ) {
//                VisualItem item = (VisualItem)iter.next();
//                ForceItem aitem = (ForceItem)item.get(ANCHORITEM);
//                if ( aitem != null ) {
//                    aitem.location[0] = (float)item.getEndX();
//                    aitem.location[1] = (float)item.getEndY();
//                }
//            }
//            super.reset();
//        }
//        
//        @Override
//        protected void initSimulator(ForceSimulator fsim) {
//            // make sure we have force items to work with
//            TupleSet t = (TupleSet)m_vis.getGroup(m_group);
//            t.addColumns(ANCHORITEM_SCHEMA);
//            t.addColumns(FORCEITEM_SCHEMA);
//            
//            Iterator iter = m_vis.visibleItems(m_nodeGroup);
//            while ( iter.hasNext() ) {
//                VisualItem item = (VisualItem)iter.next();
//                // get force item
//                ForceItem fitem = (ForceItem)item.get(FORCEITEM);
//                if ( fitem == null ) {
//                    fitem = new ForceItem();
//                    item.set(FORCEITEM, fitem);
//                }
//                fitem.location[0] = (float)item.getEndX();
//                fitem.location[1] = (float)item.getEndY();
//                fitem.mass = getMassValue(item);
//                
//                // get spring anchor
//                ForceItem aitem = (ForceItem)item.get(ANCHORITEM);
//                if ( aitem == null ) {
//                    aitem = new ForceItem();
//                    item.set(ANCHORITEM, aitem);
//                    aitem.location[0] = fitem.location[0];
//                    aitem.location[1] = fitem.location[1];
//                }
//                
//                fsim.addItem(fitem);
//                fsim.addSpring(fitem, aitem, 0);
//            }     
//        }   
//        
//        @Override
//        public void setX(VisualItem item, VisualItem referrer, double x) {
//            // ensure that the item is not pushed over a boundary
//            double width = nodeRenderer.getShape(item).getBounds2D().getWidth();
//            double oldX = item.getX();
//            double newX = x;
//            List<Integer> boundaryPositions = documentGridLayout.getXCatPositions();
//
//            boolean crossesBoundary = false;
//            if (boundaryPositions != null) {
//                for (int i = 0; i < boundaryPositions.size(); i++) {
//                    int boundaryPosition = boundaryPositions.get(i);
//                    if ((oldX - width / 2 > boundaryPosition && newX - width / 2 < boundaryPosition)
//                            || (oldX + width / 2 < boundaryPosition && newX + width / 2 > boundaryPosition)) {
//                        crossesBoundary = true;
//                    }
//                }
//
//                // ensure it didn't go beyond maximum as well!
//                if (boundaryPositions.size() > 0) {
//                    int maxBoundaryPosition = boundaryPositions.get(boundaryPositions.size() - 1);
//                    maxBoundaryPosition += documentGridLayout.getXCatRegionSizes().get(documentGridLayout.getXCatRegionSizes().size() - 1);
//                    if ((oldX - width / 2 > maxBoundaryPosition && newX - width / 2 < maxBoundaryPosition)
//                            || (oldX + width / 2 < maxBoundaryPosition && newX + width / 2 > maxBoundaryPosition)) {
//                        crossesBoundary = true;
//                    }
//                }
//            }
//            
//            if (!crossesBoundary) {
//                super.setX(item, referrer, x);
//            }
//            
//        }
//        
//        @Override
//        public void setY(VisualItem item, VisualItem referrer, double y) {
//            // ensure that the item is not pushed over a boundary
//            double height = nodeRenderer.getShape(item).getBounds2D().getHeight();
//            double oldY = item.getY();
//            double newY = y;
//            List<Integer> boundaryPositions = documentGridLayout.getYCatPositions();
//            
//            boolean crossesBoundary = false;
//            if (boundaryPositions != null) {
//                for (int i = 0; i < boundaryPositions.size(); i++) {
//                    int boundaryPosition = boundaryPositions.get(i);
//                    if ((oldY - height / 2 > boundaryPosition && newY - height / 2 < boundaryPosition)
//                            || (oldY + height / 2 < boundaryPosition && newY + height / 2 > boundaryPosition)) {
//                        crossesBoundary = true;
//                    }
//                }
//
//                // ensure it didn't go beyond maximum as well!
//                if (boundaryPositions.size() > 0) {
//                    int maxBoundaryPosition = boundaryPositions.get(boundaryPositions.size() - 1);
//                    maxBoundaryPosition += documentGridLayout.getYCatRegionSizes().get(documentGridLayout.getYCatRegionSizes().size() - 1);
//                    if ((oldY - height / 2 > maxBoundaryPosition && newY - height / 2 < maxBoundaryPosition)
//                            || (oldY + height / 2 < maxBoundaryPosition && newY + height / 2 > maxBoundaryPosition)) {
//                        crossesBoundary = true;
//                    }
//                }
//            }
//            
//            if (!crossesBoundary) {
//                super.setY(item, referrer, y);
//            }
//            
//        }
//        
//    }
    
    /**
     * Simple VisibilityFilter to control which documents are currently visible. Extended to support dynamic predicate updating.
     */
    public class GlyphVisibilityFilter extends VisibilityFilter {        
        
        public GlyphVisibilityFilter(String group, Predicate p) {
            super(group, p);
        }
        
        public void updatePredicate(Predicate p) {
            super.setPredicate(p);
        }
        
    }
    
    public class DocumentGridAxisRenderer extends AbstractShapeRenderer {

        private Line2D m_line = new Line2D.Double();
        private Rectangle2D m_box = new Rectangle2D.Double();
        // don't need to worry about alignment;
        //  !isX == left & center (with offsets to put labels in middle!)
        //  isX == center & bottom (again, with appropriate offsets!)
//    private int m_xalign;
//    private int m_yalign;
        private int m_ascent;
        private int m_xalign_vert;
        private int m_yalign_vert;
        private int m_xalign_horiz;
        private int m_yalign_horiz;
//    private DocumentGridLayout docGridLayout;  // pointer to DocumentGridLayout for which this instance is drawing the axes
        private DocumentGridLayoutNested docGridLayout;

        public DocumentGridAxisRenderer(DocumentGridLayoutNested docGridLayout) {
            this.docGridLayout = docGridLayout;
            m_xalign_horiz = Constants.LEFT;
            m_yalign_horiz = Constants.CENTER;
            m_xalign_vert = Constants.CENTER;
            m_yalign_vert = Constants.BOTTOM;
        }

        /**
         * @see
         * prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
         */
        @Override
        protected Shape getRawShape(VisualItem item) {
            double x1 = item.getDouble(VisualItem.X);
            double y1 = item.getDouble(VisualItem.Y);
            double x2 = item.getDouble(VisualItem.X2);
            double y2 = item.getDouble(VisualItem.Y2);
            boolean isX = item.getBoolean(DocumentGridAxisLayout.IS_X);
            double midPoint = item.getDouble(DocumentGridAxisLayout.MID_POINT);
//        m_line.setLine(x1,y1,x2,y2);
            // horizontal or vertical coords should be manually held constant so that fisheye works properly
            if (isX) {
                // vertical line
                m_line.setLine(x1, y1, x1, y2);
            } else {
                // horizontal line 
                m_line.setLine(x1, y1, x2, y1);
            }

            if (!item.canGetString(VisualItem.LABEL)) {
                return m_line;
            }

            String label = item.getString(VisualItem.LABEL);
            if (label == null) {
                return m_line;
            }

            FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(item.getFont());
            m_ascent = fm.getAscent();
            int h = fm.getHeight();
            int w = fm.stringWidth(label);

            double tx, ty;

            int labelOffset = 10;
            if (isX) {
                // vertical axis
                // get text x-coord, center at midPoint
//            tx = x1 + (x2-x1)/2 - w/2;
//            tx = midPoint + (x1+midPoint)/2 - w/2;
//            tx = x1 + midPoint/2 - w/2;
                // simpler approach: just add a fixed distance
                tx = x1 + labelOffset;
                // get text y-coord
                ty = y2 - h;
            } else {
                // horiz axis
                // get text x-coord
                tx = x1 - w - 2;
                // get text y-coord, center at midPoint
//            ty = y1 + (y2-y1)/2 - h/2;
//            ty = y1 + midPoint/2 - h/2;
                // simpler approach: just add a fixed distance
                ty = y1 + labelOffset;
            }

            // don't have to worry about switching;
//        // get text x-coord
//        switch ( m_xalign ) {
//        case Constants.FAR_RIGHT:
//            tx = x2 + 2;
//            break;
//        case Constants.FAR_LEFT:
//            tx = x1 - w - 2;
//            break;
//        case Constants.CENTER:
//            tx = x1 + (x2-x1)/2 - w/2;
//            break;
//        case Constants.RIGHT:
//            tx = x2 - w;
//            break;
//        case Constants.LEFT:
//        default:
//            tx = x1;
//        }
//        // get text y-coord
//        switch ( m_yalign ) {
//        case Constants.FAR_TOP:
//            ty = y1-h;
//            break;
//        case Constants.FAR_BOTTOM:
//            ty = y2;
//            break;
//        case Constants.CENTER:
//            ty = y1 + (y2-y1)/2 - h/2;
//            break;
//        case Constants.TOP:
//            ty = y1;
//            break;
//        case Constants.BOTTOM:
//        default:
//            ty = y2-h; 
//        }

            m_box.setFrame(tx, ty, w, h);
            return m_box;
        }

        /**
         * @see prefuse.render.Renderer#render(java.awt.Graphics2D,
         * prefuse.visual.VisualItem)
         */
        @Override
        public void render(Graphics2D g, VisualItem item) {
            Shape s = getShape(item);
            GraphicsLib.paint(g, item, m_line, getStroke(item), getRenderType(item));

            // check if we have a text label, if so, render it 
            String str;
            if (item.canGetString(VisualItem.LABEL)) {
                str = (String) item.getString(VisualItem.LABEL);
                if (str != null && !str.equals("")) {
                    float x = (float) m_box.getMinX();
                    float y = (float) m_box.getMinY() + m_ascent;

                    // draw label background 
                    GraphicsLib.paint(g, item, s, null, RENDER_TYPE_FILL);

                    AffineTransform origTransform = g.getTransform();
                    AffineTransform transform = this.getTransform(item);
                    if (transform != null) {
                        g.setTransform(transform);
                    }

                    g.setFont(item.getFont());
                    g.setColor(ColorLib.getColor(item.getTextColor()));

                    if (!(str.length() > 5 && str.substring(str.length() - 5, str.length()).equals("_last"))) {

                        g.setColor(Color.WHITE);
                        // TODO properly hunt down source of null str! for now, triage
                        if (str != null) {
                            // bump y down by appropriate amount
                            FontMetrics fm = g.getFontMetrics(item.getFont());
                            int strHeight = fm.getAscent();
//                        g.drawString(str, x, y);
                            g.drawString(str, x, y + strHeight);
                        }

                        if (transform != null) {
                            g.setTransform(origTransform);
                        }
                    }
                }
            }
        }

        /**
         * @see prefuse.render.Renderer#locatePoint(java.awt.geom.Point2D,
         * prefuse.visual.VisualItem)
         */
        public boolean locatePoint(Point2D p, VisualItem item) {
            Shape s = getShape(item);
            if (s == null) {
                return false;
            } else if (s == m_box && m_box.contains(p)) {
                return true;
            } else {
                double width = Math.max(2, item.getSize());
                double halfWidth = width / 2.0;
                return s.intersects(p.getX() - halfWidth,
                        p.getY() - halfWidth,
                        width, width);
            }
        }

        /**
         * @see prefuse.render.Renderer#setBounds(prefuse.visual.VisualItem)
         */
        public void setBounds(VisualItem item) {
            if (!m_manageBounds) {
                return;
            }
            Shape shape = getShape(item);
            if (shape == null) {
                item.setBounds(item.getX(), item.getY(), 0, 0);
            } else if (shape == m_line) {
                GraphicsLib.setBounds(item, shape, getStroke(item));
            } else {
                m_box.add(m_line.getX1(), m_line.getY1());
                m_box.add(m_line.getX2(), m_line.getY2());
                item.setBounds(m_box.getMinX(), m_box.getMinY(),
                        m_box.getWidth(), m_box.getHeight());
            }
        }
    }
    
//    public class GlyphLocationAnimator extends LocationAnimator {
//        
//        
//        
//    }
//    
//    public class GlyphSizeAnimator extends SizeAnimator {
//        
//        
//        
//    }
    
}
