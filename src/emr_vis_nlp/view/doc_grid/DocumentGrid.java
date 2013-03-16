package emr_vis_nlp.view.doc_grid;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.view.MainViewGlassPane;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.assignment.SizeAction;
import prefuse.action.distortion.Distortion;
import prefuse.action.filter.VisibilityFilter;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.activity.ActivityAdapter;
import prefuse.activity.ActivityListener;
import prefuse.controls.AnchorUpdateControl;
import prefuse.controls.ControlAdapter;
import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.UpdateListener;
import prefuse.util.force.*;
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
    private DocumentGridLayout documentGridLayout;
    // handles layout of the attribute value axes
    private DocumentGridAxisLayout docGridAxisLayout;
    // handles generation of glyphs
    private DocumentShapeAction docShapeAction;
    // handles glyph coloration
    private DocumentColorAction docColorAction;
    // handles glyph border coloration
    private DocumentBorderColorAction docBorderColorAction;
    // handles shape sizing
    private DocumentSizeAction docSizeAction;
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
    private double fisheyeDistortScale = 13.;
//    private double fisheyeDistortScale = 4.;
    private double fisheyeDistortSize = 1.1;
    private Distortion feye;
//    private Distortion bifocal;
    private Rectangle2D feyeBoundingBox;
    private AnchorUpdateControl anchorUpdateControl;
    
    // predicate controlling which document glyphs will be rendered
    private Predicate docGlyphVisiblePredicate;
    private GlyphVisibilityFilter docGlyphVisibleFilter;
    
    // binding for string searching
    private SearchQueryBinding searchQ;
    
    private Display display;
    
    public DocumentGrid(DocumentGridTable t, String xAxisInitName, String yAxisInitName, String shapeInitName, String colorInitName) {
        super(new Visualization());
        display = this;
        this.t = t;
        this.controller = MainController.getMainController();
        // add data to visualization (tables, ...)
        VisualTable vt = m_vis.addTable(DATA_GROUP, t);
        colorAttrName = colorInitName;

        // init actionlist: performs initial document layout
        ActionList init = new ActionList();
        // add document layout action
        List<String> xAxisInitCategories = t.getValueListForAttribute(xAxisInitName);
        List<String> yAxisInitCategories = t.getValueListForAttribute(yAxisInitName);
        documentGridLayout = new DocumentGridLayout(controller, DATA_GROUP, xAxisInitName, yAxisInitName, xAxisInitCategories, yAxisInitCategories);
        init.add(documentGridLayout);
        // add axes layout action
        docGridAxisLayout = new DocumentGridAxisLayout(ALL_LABEL, documentGridLayout);
        init.add(docGridAxisLayout);
        // set glyph shapes
//        docShapeAction = new DocShapeAction(DATA_GROUP, Constants.SHAPE_RECTANGLE);
//        docShapeAction = new DocumentShapeAction(DATA_GROUP, Constants.SHAPE_DIAMOND);
//        init.add(docShapeAction);
        // encode size
//        docSizeAction = new DocumentSizeAction(DATA_GROUP);
//        init.add(docSizeAction);
        // add init actionlist to vis
        m_vis.putAction("init", init);
        
        // set up renderer for nodes, set rendererFactory
        nodeRenderer = new DocGlyphRenderer();
        // perform additional optional renderer setup here
        // add primary renderer to visualization
        DefaultRendererFactory rf = new DefaultRendererFactory();
//        rf.setDefaultRenderer(new NullRenderer());
        rf.setDefaultRenderer(nodeRenderer);
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
        ActionList preforce = new ActionList(1000);
//        preforce.add(new DataMountainForceLayout(true));
        preforce.add(new DataMountainForceLayout(false));
        m_vis.putAction("preforce", preforce);
        
        // update actionlist: performs coloration, sizing
        // TODO: merge update and init?
//        ActionList update = new ActionList();
        ActionList update = new ActionList(Activity.INFINITY);
        ActionList updateOnce = new ActionList();
        // actionlist for only updating colors, sizes; not positionings
        ActionList updateColorOnly = new ActionList();
        // size action
//        SizeAction sizeActionUpdate = new DocGlyphSizeAction(DATA_GROUP);
//        update.add(sizeActionUpdate);
        // TODO set size to roughly be a function of ## items in display?
        docSizeAction = new DocumentSizeAction(DATA_GROUP, 1.5);
//        update.add(docSizeAction);
        updateOnce.add(docSizeAction);
        // shape action
        // get current attrs from table
        shapeAttrName = shapeInitName;
        List<String> shapeInitCategories = t.getValueListForAttribute(shapeInitName);
//        docShapeAction = new DocumentShapeAction(DATA_GROUP, Constants.SHAPE_DIAMOND, shapeInitName, shapeInitCategories);
        docShapeAction = new DocumentShapeAction(DATA_GROUP, shapeInitName, shapeInitCategories);
//        update.add(docShapeAction);
        updateOnce.add(docShapeAction);
        // color action(s)
        List<String> colorInitCategories = t.getValueListForAttribute(colorInitName);
        docColorAction = new DocumentColorAction(DATA_GROUP, colorInitName, colorInitCategories);
        update.add(docColorAction);
        updateOnce.add(docColorAction);
        docBorderColorAction = new DocumentBorderColorAction(DATA_GROUP);
        update.add(docBorderColorAction);
        updateOnce.add(docBorderColorAction);
        // TODO add axes color actions?
//        ColorAction labelColorActionUpdate = new LabelColorAction(ALL_LABEL);
//        update.add(labelColorActionUpdate);
        // visibility filter
        docGlyphVisiblePredicate = new InGroupPredicate(DATA_GROUP);
        docGlyphVisibleFilter = new GlyphVisibilityFilter(DATA_GROUP, docGlyphVisiblePredicate);
        update.add(docGlyphVisibleFilter);
        updateOnce.add(docGlyphVisibleFilter);
        // repaint action
        update.add(new RepaintAction());
        updateOnce.add(new RepaintAction());
        // add update actionlist to vis
        m_vis.putAction("update", update);
        m_vis.putAction("updateOnce", updateOnce);

        // force stabilizing action? (borrowed from datamountain)
//        ActionList preforce = new ActionList(1000);
//        preforce.add(new DataMountainForceLayout(true));
//        m_vis.putAction("preforce", preforce);
//
        // force action, to move docs out of the way when dragging (borrowed from datamountain)
        final ForceDirectedLayout fl = new DataMountainForceLayout(false);
        ActivityListener fReset = new ActivityAdapter() {
            public void activityCancelled(Activity a) {
                fl.reset(); 
             } 
        };
        ActionList forces = new ActionList(Activity.INFINITY);
        forces.add(fl);
//        forces.add(update);
        forces.addActivityListener(fReset);
        m_vis.putAction("forces", forces);
        
        
        // ActionList for interspersed FDL iterations with fisheye
        ActionList fishForce = new ActionList();
//        preforce.add(new DataMountainForceLayout(true));
        fishForce.add(new DataMountainForceLayout(false, -0.4f, 30f, 0f, false));
        m_vis.putAction("fishforce", fishForce);
        
        // ActionList for performing fisheye distortion
        // TODO merge with update ActionList?
        ActionList distort = new ActionList();
//        ActionList distort = new ActionList(Activity.INFINITY);
        // manually define boundingbox for fisheye as width, height of view
        feyeBoundingBox = new Rectangle(0, 0, initWidth, initHeight);
        feye = new FisheyeDistortionDocGrid(fisheyeDistortScale, fisheyeDistortSize, feyeBoundingBox);
        feye.setSizeDistorted(true);
//        feye.setGroup(DATA_GROUP);  // don't set group; we want to handle the axes also?
        distort.add(feye);
//        bifocal = new BifocalDistortionDocGrid(0.2,4.5,feyeBoundingBox);
//        distort.add(bifocal);
//        distort.add(docSizeAction);
        distort.add(docColorAction);
        distort.add(docBorderColorAction);
//        distort.add(fishForce);
//        distort.add(forces);
//        distort.add(update);
        distort.add(new RepaintAction());
        m_vis.putAction("distort", distort);
        
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
                // run repaint actions
//                m_vis.cancel("animatePaint");
//                m_vis.run("fullPaint");
//                m_vis.run("animatePaint");
//                m_vis.run("update");
//                // debug
//                System.out.println("\n\ndebug: "+this.getClass().getName()+": in SEARCH_ITEMS group: ");
//                Iterator itemsInGroup = m_vis.getGroup(Visualization.SEARCH_ITEMS).tuples();
//                while (itemsInGroup.hasNext()) {
//                    Object item = itemsInGroup.next();
//                    System.out.println("debug: \t"+item.toString());
//                }
                m_vis.cancel("update");
                m_vis.run("update");
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
//        addControlListener(new DragControl());
        addControlListener(new DocGridDragControl(DATA_GROUP, documentGridLayout, controller));
        // control for loading document details in glasspane
        addControlListener(new DocumentSelectControl(glassPane));
        // control for performing fisheye distortion
        anchorUpdateControl = new AnchorUpdateControl(feye, "distort");
        addControlListener(anchorUpdateControl);
//        addControlListener(new AnchorUpdateControl(bifocal, "distort"));
        
        // run actionlists
//        m_vis.run("init");
//        m_vis.runAfter("preforce", "update");  // temporarily (?) disable for testing
//        m_vis.run("preforce");
//        m_vis.run("update");  // remove if forces ("runAfter") are reenabled
        
        // first pass: before layout, do updateOnce to set sizes
//        m_vis.alwaysRunAfter("init", "preforce");
//        m_vis.alwaysRunAfter("preforce", "update");
        m_vis.alwaysRunAfter("init", "update");
        
        m_vis.runAfter("updateOnce", "init");
        m_vis.run("updateOnce");
        
//        m_vis.run("forces");
        
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
        m_vis.run("updateOnce");
        m_vis.run("init");
        m_vis.run("preforce");
    }
    
    public void resetDocsVisiblePredicate(String predStr) {
        
        if (!predStr.equals("")) {
            docGlyphVisiblePredicate = new AndPredicate(new InGroupPredicate(DATA_GROUP), ExpressionParser.predicate(predStr));
        } else {
            docGlyphVisiblePredicate = new InGroupPredicate(DATA_GROUP);
        }
        
//        DefaultRendererFactory rf = new DefaultRendererFactory();
////        rf.setDefaultRenderer(nodeRenderer);
//        rf.add(docGlyphVisiblePredicate, nodeRenderer);
//        // add auxiliary renderer for axes
//        rf.add(new InGroupPredicate(ALL_LABEL), new DocumentGridAxisRenderer(documentGridLayout));
//        m_vis.setRendererFactory(rf);
        
        if (docGlyphVisibleFilter != null) {
            docGlyphVisibleFilter.updatePredicate(docGlyphVisiblePredicate);
        }
        
    }
    
    public SearchQueryBinding getSearchQuery() {
        return searchQ;
    }
    
    public void resetView() {
//        m_vis.runAfter("init", "preforce");  // temporarily (?) disable for testing
//        m_vis.run("init");
        m_vis.run("updateOnce");
        m_vis.run("init");
        m_vis.run("preforce");
    }
    
    public void updateXAxis(String xAxisAttrName) {
        List<String> xAxisCategories = t.getValueListForAttribute(xAxisAttrName);
//        docGridAxisLayout.updateXAxis
        documentGridLayout.updateXAxis(xAxisAttrName, xAxisCategories);
    }
    
    public void updateYAxis(String yAxisAttrName) {
        List<String> yAxisCategories = t.getValueListForAttribute(yAxisAttrName);
//        docGridAxisLayout.updateXAxis
        documentGridLayout.updateYAxis(yAxisAttrName, yAxisCategories);
    }
    
    public void updateColorAttr(String colorAttrName) {
        List<String> colorCategories = t.getValueListForAttribute(colorAttrName);
        // update colorAction
        this.colorAttrName = colorAttrName;
        docColorAction.updateColorAttr(colorAttrName, colorCategories);
    }
    
    public void updateShapeAttr(String shapeAttrName) {
        this.shapeAttrName = shapeAttrName;
        List<String> shapeCategories = t.getValueListForAttribute(shapeAttrName);
        // update shapeAction
        docShapeAction.updateShapeAttr(shapeAttrName, shapeCategories);
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
//            if ( m_vis.isInGroup(item, Visualization.SEARCH_ITEMS) )
//                return ColorLib.rgb(191,99,130);
            // do (inefficient) manual comparison?
            Iterator itemsInGroup = m_vis.getGroup(Visualization.SEARCH_ITEMS).tuples();
            while (itemsInGroup.hasNext()) {
                Tuple itemInGroup = (Tuple)itemsInGroup.next();
                if (item.getString(DocumentGridTable.NODE_NAME).equals(itemInGroup.getString(DocumentGridTable.NODE_NAME))) {
                    // debug
//                    System.out.println("debug: "+this.getClass().getName()+": item in group! "+item.toString());
                    return ColorLib.rgb(191,99,130);
                }
            }
            
            // TODO set color based panel controls, certainty, selected attrs / values
//            NodeItem nitem = (NodeItem) item;
            if (item.isHover()) {
//                return ColorLib.rgb(99, 130, 191);
                return Color.LIGHT_GRAY.getRGB();
            }
            
//            if (highlightPredicate != null && highlightPredicate.getBoolean(item)) {
            if (highlightPredicate != null && item.canGetString(highlightAttr) && item.getString(highlightAttr).equals(highlightVal)) {
                // is in the hover predicate
//                return Color.CYAN.getRGB();
                return Color.RED.getRGB();
            }
            
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
            
//            if ( m_vis.isInGroup(item, Visualization.SEARCH_ITEMS) )
//                return ColorLib.rgb(191,99,130);
//            
//            // do (inefficient) manual comparison?
//            Iterator itemsInGroup = m_vis.getGroup(Visualization.SEARCH_ITEMS).tuples();
//            
//            while (itemsInGroup.hasNext()) {
//                Tuple itemInGroup = (Tuple)itemsInGroup.next();
//                if (item.getString(DocumentGridTable.NODE_NAME).equals(itemInGroup.getString(DocumentGridTable.NODE_NAME))) {
//                    // debug
//                    System.out.println("debug: "+this.getClass().getName()+": item in group! "+item.toString());
//                    return ColorLib.rgb(191,99,130);
//                }
//            }
            
            // get value for target attr in item
            if (item.canGetString(colorAttrName)) {
                String attrVal = item.getString(colorAttrName);
                Color attrValColor = catToColorMap.get(attrVal);
                return attrValColor.getRGB();
            }
            
            Color white = Color.WHITE;
            return white.getRGB();
            
        }
        
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
    public static class DocumentSizeAction extends SizeAction {
        
        public static final double HOVER_SIZE_MULT = 1.5;
//        public static final double BASE_SIZE = 10;
        
        public DocumentSizeAction(String group, double baseSize) {
            super(group, baseSize);
//            super(group);
        }
        
        public DocumentSizeAction() {
            super();
        }
        
        // size should blow up on hover / click
        @Override
        public double getSize(VisualItem item) {
//            NodeItem nitem = (NodeItem) item;
            if (item.isHover()) {
                return super.getSize(item) * HOVER_SIZE_MULT;
            }
            return super.getSize(item);
        }
        
    }
    
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
            
            return(Constants.SHAPE_TRIANGLE_DOWN);
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
            // refresh?
        }
        
    }
    
    
    public static class NullRenderer implements prefuse.render.Renderer {

        public NullRenderer() {}

        @Override
        public void render(Graphics2D gd, VisualItem vi) {
//            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean locatePoint(Point2D pd, VisualItem vi) {
//            throw new UnsupportedOperationException("Not supported yet.");
            return false;
        }

        @Override
        public void setBounds(VisualItem vi) {
//            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        
        
    }

    /*
     * Handles rendering of document glyphs and drawing of document text (for
     * selected document[s]). Should also handle drawing of axes. 
     */
    public class DocGlyphRenderer extends ShapeRenderer {
        
        // base size for visualitems (in pixels)
//        public static final int BASE_SIZE = 10;
        
        public DocGlyphRenderer() {
//            super(BASE_SIZE);
            super();
            m_manageBounds = false;
        }
        
        @Override
        public void render(Graphics2D g, VisualItem item) {
            
            // execute doc visible predicate on item, to determine whether to render
            // do this in VisibilityFilter instance instead
//            boolean shouldBeVisible = item. ; docGlyphVisiblePredicate.
            
            if (item.isVisible()) {

                // draw basic glyph
                super.render(g, item);

                // idea: rather than rendering fixed-size, can we first compute size of text, then set size of item equal to size of text?

                Shape shape = getShape(item);
//            item.setBounds(shape.getBounds2D().getMinX(), shape.getBounds2D().getMinY(), shape.getBounds2D().getWidth(), shape.getBounds2D().getHeight());
                if (shape != null) {

//                System.out.println("debug: "+item.getString("24.1 Rate of procedures where prep adequate"));
//                double xPos = shape.getBounds().getX();
//                double yPos = shape.getBounds().getY();
//            double xPos = item.getX();
//            double yPos = item.getY();
                    String s = "doc=" + item.getString(NODE_NAME);
//                boolean isHover = false;
//                if (item.isHover()) {
//                    // no longer displaying doc text on hover; instead, display details on rightclick
////                    s = item.getString(NODE_TEXT);
//                    s += " (right-click for highlighted text)";
//                    
//                    isHover = true;
//                }
                    s += "\n" + item.getString(NODE_TEXT);
//                double x1 = item.getBounds().getX();
//                double y1 = item.getBounds().getY();
//                double w = item.getBounds().getWidth();
//                double h = item.getBounds().getHeight();
                    double x1 = (double) item.get(VisualItem.X);
                    double y1 = (double) item.get(VisualItem.Y);
                    double w = (double) this.getShape(item).getBounds2D().getWidth();
                    double h = (double) this.getShape(item).getBounds2D().getHeight();
//                shape.getBounds().setBounds((int)x1, (int)y1, (int)w, (int)h);

                    // TODO set font size based on number of active nodes? based on size of rect?

//            int fontSize = Math.min((int)width, (int) height);
                    int fontSize = 10;
//            item.setFont(FontLib.getFont("Tahoma", Font.PLAIN, maxFontSize));
                    item.setFont(FontLib.getFont("Tahoma", Font.PLAIN, fontSize));

                    // note: this does not draw newlines! we will need to handle this ourselves
                    Font f = item.getFont();
//            FontMetrics fm = g.getFontMetrics(f);
//            int w = fm.stringWidth(s);
//            int h = fm.getAscent();
//            //g.setColor(Color.LIGHT_GRAY);
//            g.setColor(Color.BLACK);
//            g.setFont(f);
//            g.drawString(s, (float) (xPos),
//                    (float) (yPos + h));
//            }

                    // compute width, height for the given text
                    int[] textDims = getTextDims(g, f, s);
                    int textWidth = textDims[0];
                    int textHeight = textDims[1];
//                if ( shape instanceof RectangularShape ) {
//                    RectangularShape r = (RectangularShape) shape;
//                    double x = r.getX();
//                    double y = r.getY();
//                    double x = x1;
//                    double y = y1;

                    // use our own painting code, for fine-grained control of size
                    // TODO move this into separate method, add code for more robust glyph vis.? see prefuse.util.GraphicsLib.paint()
                    Color strokeColor = ColorLib.getColor(item.getStrokeColor());
//                    Color fillColor = ColorLib.getColor(item.getFillColor());
                    Color fillColor = Color.WHITE;

//                    if (isHover) {
//                        item.setBounds(x1, y1, x1+textWidth, y1+textHeight);
//                        g.setPaint(fillColor);
////                        g.fillRect((int)x, (int)y, textWidth, textHeight);
//                        g.fillRect((int)x1, (int)y1, textWidth, textHeight);
//                        g.setPaint(strokeColor);
////                        g.drawRect((int)x, (int)y, textWidth, textHeight);
//                        g.drawRect((int)x1, (int)y1, textWidth, textHeight);
//                    } else {
////                        item.setBounds(x1, y1, x1+w, y1+h);
//                        g.setPaint(fillColor);
//                        g.fillRect((int)x1, (int)y1, (int)(w), (int)(h));
//                        g.setPaint(strokeColor);
//                        g.drawRect((int)x1, (int)y1, (int)(w), (int)(h));
//                    }

//                }

//            super.render(g, item);

//                drawStringMultiline(g, f, s, xPos, yPos);
                    // debug
//                    System.out.println("debug: "+this.getClass().getName()+": drawStringMultiline at x="+x1+", y="+y1+", w="+w+", h="+h);
                    drawStringMultiline(g, f, s, x1, y1, w, h);

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
        // TODO draw as much as can fit in each item
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
            g.drawString(line, (float) (xPos-(width/2.)), (float) (yPos-(height/2.) + h * (t+1)));
        }
        
//        int lineCounter = 1;
//        while (lineSplitter.hasNextLine()) {
//            String line = lineSplitter.nextLine();
//            g.drawString(line, (float) (xPos-(width/2.)), (float) (yPos-(height/2.) + h * lineCounter));
//            lineCounter++;
//        }
    }
    
    /**
     * This control is responsible for drawing document details information onto the glasspane when appropriate.
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
        public void itemPressed(VisualItem item, MouseEvent e) {
            // load (or unload) marked-up text into glasspane on rightclick
            if (SwingUtilities.isRightMouseButton(e) && item.canGetInt(DocumentGridTable.NODE_ID)) {
                int nodeId = item.getInt(DocumentGridTable.NODE_ID);
//                String attrIdStr = documentGridLayout.getXAttr();
                String attrIdStr = shapeAttrName;
                
                if (isPopupLoaded) {
                    // hide glasspane
                    glassPane.hidePane();
                } else {
                    // build text, position text, show glasspane
                    // TODO generalized highlighting; for now, just highlight doc wrt. X-Axis?
                    AbstractDocument doc = glassPane.getAbstDoc();
                    controller.writeDocTextWithHighlights(doc, nodeId, attrIdStr);
                    int x = e.getX();
                    int y = e.getY();
                    // TODO make sure that pane is within window bounds!
                    int x2 = x+glassPane.getPopupWidth();
                    int y2 = y+glassPane.getPopupHeight();
                    Rectangle glassBounds = glassPane.getBounds();
                    double boundsWidth = glassBounds.getWidth();
                    double boundsHeight = glassBounds.getHeight();
                    if (x2 > boundsWidth) {
                        x = x - (int)(x2 - boundsWidth);
                    }
                    if (y2 > boundsHeight) {
                        y = y - (int)(y2 - boundsHeight);
                    }
                    glassPane.displayPaneAtPoint(x, y);
                }
            }
        }
        
        @Override
    public void itemEntered(VisualItem item, MouseEvent e) {
            
            
            if (item.canGetInt(DocumentGridTable.NODE_ID)) {
                // suspend fisheye, run FDL
                // set the focus to the current node
//            Visualization vis = item.getVisualization();
//            vis.getFocusGroup(Visualization.FOCUS_ITEMS).setTuple(item);
//            item.setFixed(true);
//            dragged = false;
//            Display d = (Display) e.getComponent();
//            down = d.getAbsoluteCoordinate(e.getPoint(), down);
//            vis.cancel("distort");
//            vis.run("forces");


                // freeze the screen?
                m_vis.cancel("distort");
//            display.removeControlListener(anchorUpdateControl);
//            synchronized (anchorUpdateControl) {
//                try {
//                    anchorUpdateControl.wait();
//                } catch (InterruptedException ex) {
//                    // debug
//                    System.out.println("debug: " + this.getClass().getName() + ": anchorUpdateControl.wait() interrupted");
//                }
//            }
                anchorUpdateControl.setEnabled(false);

                // appear the glasspane at appropriate size & location
                // get relative location of Visualization
                int xOffset = 0;
                int yOffset = 0;
                JComponent component = display;
                do {
                    Point visLocation = component.getLocation();
                    xOffset += visLocation.x;
                    yOffset += visLocation.y;
//            } while ((!component.getParent().getClass().equals(MainTabbedView.class)) && (component = (JComponent)component.getParent()) != null); // TODO more general, not just mainTabbedView!
                } while ((!component.getParent().getClass().equals(JRootPane.class)) && (component = (JComponent) component.getParent()) != null); // TODO more general, not just mainTabbedView!
                // debug
                System.out.println("debug: " + this.getClass().getName() + ": offsets: " + xOffset + ", " + yOffset);

                int nodeId = item.getInt(DocumentGridTable.NODE_ID);
                String attrIdStr = colorAttrName;  // TODO make highlighting more general, not just based on color!
                AbstractDocument doc = glassPane.getAbstDoc();
                controller.writeDocTextWithHighlights(doc, nodeId, attrIdStr);
                double x = item.getX() + xOffset - (nodeRenderer.getShape(item).getBounds2D().getWidth()) / 2;
                double y = item.getY() + yOffset - (nodeRenderer.getShape(item).getBounds2D().getHeight()) / 2;
                double w = nodeRenderer.getShape(item).getBounds2D().getWidth();
                double h = nodeRenderer.getShape(item).getBounds2D().getHeight();
                glassPane.displaySizedPane((int) x, (int) y, (int) w, (int) h);
            }
        }

        @Override
        public void itemExited(VisualItem item, MouseEvent e) {
            // resume the fisheye distortions
//            display.addControlListener(anchorUpdateControl);
//            synchronized (anchorUpdateControl) {
//                anchorUpdateControl.notify();
//            }
//            anchorUpdateControl.setEnabled(true);
        }
 
    }
    
    /*
     * datamountain force controllers, temporarily (?) adopted for this graph layout
     */
    private static final String ANCHORITEM = "_anchorItem";
    private static final Schema ANCHORITEM_SCHEMA = new Schema();
    static {
        ANCHORITEM_SCHEMA.addColumn(ANCHORITEM, ForceItem.class);
    }
    public class DataMountainForceLayout extends ForceDirectedLayout {
        
        public DataMountainForceLayout(boolean enforceBounds) {
            super("data",enforceBounds,false);
            
            ForceSimulator fsim = new ForceSimulator();
            // (gravConstant, minDistance, theta)
            fsim.addForce(new NBodyForce(-0.6f, 40f, NBodyForce.DEFAULT_THETA));
//            fsim.addForce(new SpringForce(1e-5f,0f));
            fsim.addForce(new DragForce(0.08f));
            setForceSimulator(fsim);
            
            m_nodeGroup = "data";
            m_edgeGroup = null;
        }
        
        public DataMountainForceLayout(boolean enforceBounds, float gravConstant, float minDistance, float dragCoeff, boolean runOnce) {
            super("data",enforceBounds,runOnce);
            
            ForceSimulator fsim = new ForceSimulator();
            // (gravConstant, minDistance, theta)
            fsim.addForce(new NBodyForce(gravConstant, minDistance, NBodyForce.DEFAULT_THETA));
//            fsim.addForce(new SpringForce(1e-5f,0f));
            fsim.addForce(new DragForce(dragCoeff));
            setForceSimulator(fsim);
            
            m_nodeGroup = "data";
            m_edgeGroup = null;
        }
        
        protected float getMassValue(VisualItem n) {
            return n.isHover() ? 5f : 1f;
        }

        public void reset() {
            Iterator iter = m_vis.visibleItems(m_nodeGroup);
            while ( iter.hasNext() ) {
                VisualItem item = (VisualItem)iter.next();
                ForceItem aitem = (ForceItem)item.get(ANCHORITEM);
                if ( aitem != null ) {
                    aitem.location[0] = (float)item.getEndX();
                    aitem.location[1] = (float)item.getEndY();
                }
            }
            super.reset();
        }
        protected void initSimulator(ForceSimulator fsim) {
            // make sure we have force items to work with
            TupleSet t = (TupleSet)m_vis.getGroup(m_group);
            t.addColumns(ANCHORITEM_SCHEMA);
            t.addColumns(FORCEITEM_SCHEMA);
            
            Iterator iter = m_vis.visibleItems(m_nodeGroup);
            while ( iter.hasNext() ) {
                VisualItem item = (VisualItem)iter.next();
                // get force item
                ForceItem fitem = (ForceItem)item.get(FORCEITEM);
                if ( fitem == null ) {
                    fitem = new ForceItem();
                    item.set(FORCEITEM, fitem);
                }
                fitem.location[0] = (float)item.getEndX();
                fitem.location[1] = (float)item.getEndY();
                fitem.mass = getMassValue(item);
                
                // get spring anchor
                ForceItem aitem = (ForceItem)item.get(ANCHORITEM);
                if ( aitem == null ) {
                    aitem = new ForceItem();
                    item.set(ANCHORITEM, aitem);
                    aitem.location[0] = fitem.location[0];
                    aitem.location[1] = fitem.location[1];
                }
                
                fsim.addItem(fitem);
                fsim.addSpring(fitem, aitem, 0);
            }     
        }   
        
        @Override
        public void setX(VisualItem item, VisualItem referrer, double x) {
            // ensure that the item is not pushed over a boundary
            double width = nodeRenderer.getShape(item).getBounds2D().getWidth();
            double oldX = item.getX();
            double newX = x;
            List<Integer> boundaryPositions = documentGridLayout.getXCatPositions();

            boolean crossesBoundary = false;
            if (boundaryPositions != null) {
                for (int i = 0; i < boundaryPositions.size(); i++) {
                    int boundaryPosition = boundaryPositions.get(i);
                    if ((oldX - width / 2 > boundaryPosition && newX - width / 2 < boundaryPosition)
                            || (oldX + width / 2 < boundaryPosition && newX + width / 2 > boundaryPosition)) {
                        crossesBoundary = true;
                    }
                }

                // ensure it didn't go beyond maximum as well!
                if (boundaryPositions.size() > 0) {
                    int maxBoundaryPosition = boundaryPositions.get(boundaryPositions.size() - 1);
                    maxBoundaryPosition += documentGridLayout.getXCatRegionSizes().get(documentGridLayout.getXCatRegionSizes().size() - 1);
                    if ((oldX - width / 2 > maxBoundaryPosition && newX - width / 2 < maxBoundaryPosition)
                            || (oldX + width / 2 < maxBoundaryPosition && newX + width / 2 > maxBoundaryPosition)) {
                        crossesBoundary = true;
                    }
                }
            }
            
            if (!crossesBoundary) {
                super.setX(item, referrer, x);
            }
            
        }
        
        @Override
        public void setY(VisualItem item, VisualItem referrer, double y) {
            // ensure that the item is not pushed over a boundary
            double height = nodeRenderer.getShape(item).getBounds2D().getHeight();
            double oldY = item.getY();
            double newY = y;
            List<Integer> boundaryPositions = documentGridLayout.getYCatPositions();
            
            boolean crossesBoundary = false;
            if (boundaryPositions != null) {
                for (int i = 0; i < boundaryPositions.size(); i++) {
                    int boundaryPosition = boundaryPositions.get(i);
                    if ((oldY - height / 2 > boundaryPosition && newY - height / 2 < boundaryPosition)
                            || (oldY + height / 2 < boundaryPosition && newY + height / 2 > boundaryPosition)) {
                        crossesBoundary = true;
                    }
                }

                // ensure it didn't go beyond maximum as well!
                if (boundaryPositions.size() > 0) {
                    int maxBoundaryPosition = boundaryPositions.get(boundaryPositions.size() - 1);
                    maxBoundaryPosition += documentGridLayout.getYCatRegionSizes().get(documentGridLayout.getYCatRegionSizes().size() - 1);
                    if ((oldY - height / 2 > maxBoundaryPosition && newY - height / 2 < maxBoundaryPosition)
                            || (oldY + height / 2 < maxBoundaryPosition && newY + height / 2 > maxBoundaryPosition)) {
                        crossesBoundary = true;
                    }
                }
            }
            
            if (!crossesBoundary) {
                super.setY(item, referrer, y);
            }
            
        }
        
    }
    
    public class GlyphVisibilityFilter extends VisibilityFilter {
        
        
        public GlyphVisibilityFilter(String group, Predicate p) {
            super(group, p);
        }
        
        public void updatePredicate(Predicate p) {
            super.setPredicate(p);
        }
        
    }
    
}
