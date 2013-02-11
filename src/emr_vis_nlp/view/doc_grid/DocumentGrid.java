package emr_vis_nlp.view.doc_grid;

import emr_vis_nlp.controller.MainController;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.swing.BorderFactory;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.assignment.SizeAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.activity.ActivityAdapter;
import prefuse.activity.ActivityListener;
import prefuse.controls.Control;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.data.Schema;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.force.*;
import prefuse.visual.VisualItem;
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
    
    public static final String X_LABEL = "xlab";
    public static final String Y_LABEL = "ylab";
    public static final String ALL_LABEL = "all_label";
    
    public static final String DATA_GROUP = "data";
    
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
    
    
    // controller governing this DocumentGrid
    private MainController controller;
    
    public DocumentGrid(MainController controller, DocumentGridTable t, String xAxisInitName, String yAxisInitName, String shapeInitName, String colorInitName) {
        super(new Visualization());
        this.t = t;
        this.controller = controller;
        // add data to visualization (tables, ...)
        m_vis.addTable(DATA_GROUP, t);


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
        DocGlyphRenderer nodeRenderer = new DocGlyphRenderer();
        // perform additional optional renderer setup here
        // add primary renderer to visualization
        DefaultRendererFactory rf = new DefaultRendererFactory();
        rf.setDefaultRenderer(nodeRenderer);
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
        // size action
//        SizeAction sizeActionUpdate = new DocGlyphSizeAction(DATA_GROUP);
//        update.add(sizeActionUpdate);
        // TODO set size to roughly be a function of ## items in display?
        docSizeAction = new DocumentSizeAction(DATA_GROUP, 3);
        update.add(docSizeAction);
        updateOnce.add(docSizeAction);
        // shape action
        // get current attrs from table
        List<String> shapeInitCategories = t.getValueListForAttribute(shapeInitName);
//        docShapeAction = new DocumentShapeAction(DATA_GROUP, Constants.SHAPE_DIAMOND, shapeInitName, shapeInitCategories);
        docShapeAction = new DocumentShapeAction(DATA_GROUP, shapeInitName, shapeInitCategories);
        update.add(docShapeAction);
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
        // repaint action
        update.add(new RepaintAction());
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
        
        // set basic properties of the display
        setSize(700, 600);
        setBackground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createEmptyBorder(30,20,5,20));
        
        // set up control listeners
        // zoom with wheel
        addControlListener(new WheelZoomControl());
        // zoom with background right-drag
//        addControlListener(new ZoomControl(Control.RIGHT_MOUSE_BUTTON));
        // pan with background left-drag
        addControlListener(new PanControl(Control.RIGHT_MOUSE_BUTTON));
        // drag control for moving items to new cells
//        addControlListener(new DragControl());
        addControlListener(new DocGridDragControl(DATA_GROUP, documentGridLayout, controller));
        
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
        
    }
    
    /**
     * To be called when the pixel area of this Display changes.
     * 
     * @param newWidth
     * @param newHeight 
     */
    public void resetSize(int newWidth, int newHeight) {
        setSize(newWidth, newHeight);
        
        // redo layout, force initialization
//        m_vis.runAfter("preforce", "update");  // temporarily (?) disable for testing
//        m_vis.run("preforce");
//        m_vis.run("update");  // remove if forces ("runAfter") are reenabled
//        m_vis.runAfter("init", "preforce");  // temporarily (?) disable for testing
        m_vis.run("init");
    }
    
    public void resetView() {
//        m_vis.runAfter("init", "preforce");  // temporarily (?) disable for testing
//        m_vis.run("init");
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
        docColorAction.updateColorAttr(colorAttrName, colorCategories);
    }
    
    public void updateShapeAttr(String shapeAttrName) {
        List<String> shapeCategories = t.getValueListForAttribute(shapeAttrName);
        // update shapeAction
        docShapeAction.updateShapeAttr(shapeAttrName, shapeCategories);
    }

    
    
    
    /**
     * ColorAction for assigning border colors to document glyphs.
     */
    public static class DocumentBorderColorAction extends ColorAction {

        public DocumentBorderColorAction(String group) {
            super(group, VisualItem.STROKECOLOR);
        }

        @Override
        public int getColor(VisualItem item) {
            // TODO set color based panel controls, certainty, selected attrs / values
//            NodeItem nitem = (NodeItem) item;
            if (item.isHover()) {
//                return ColorLib.rgb(99, 130, 191);
                return Color.LIGHT_GRAY.getRGB();
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
        
        public static final int[] GLYPH_SHAPE_PALETTE = {
            Constants.SHAPE_RECTANGLE, Constants.SHAPE_DIAMOND, Constants.SHAPE_ELLIPSE, 
            Constants.SHAPE_HEXAGON, Constants.SHAPE_CROSS, Constants.SHAPE_STAR
        };
        
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

    /*
     * Handles rendering of document glyphs and drawing of document text (for
     * selected document[s]). Should also handle drawing of axes. 
     */
    public static class DocGlyphRenderer extends ShapeRenderer {
        
        // base size for visualitems (in pixels)
//        public static final int BASE_SIZE = 10;
        
        public DocGlyphRenderer() {
//            super(BASE_SIZE);
            super();
            m_manageBounds = false;
        }
        
        @Override
        public void render(Graphics2D g, VisualItem item) {
            super.render(g, item);
            
            // idea: rather than rendering fixed-size, can we first compute size of text, then set size of item equal to size of text?
            
            Shape shape = getShape(item);
            
            if (shape != null) {

//                double xPos = shape.getBounds().getX();
//                double yPos = shape.getBounds().getY();
//            double xPos = item.getX();
//            double yPos = item.getY();
                String s = item.getString(NODE_NAME);
                boolean isHover = false;
                if (item.isHover()) {
                    s = item.getString(NODE_TEXT);
                    isHover = true;
                }
                
//                double x1 = item.getBounds().getX();
//                double y1 = item.getBounds().getY();
//                double w = item.getBounds().getWidth();
//                double h = item.getBounds().getHeight();
                double x1 = (double)item.get(VisualItem.X);
                double y1 = (double) item.get(VisualItem.Y);
//                double w = (double) item.get(VisualItem.X2) - x1;
//                double h = (double) item.get(VisualItem.Y2) - y1;
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
                    double x = x1;
                    double y = y1;
                    
                    // use our own painting code, for fine-grained control of size
                    // TODO move this into separate method, add code for more robust glyph vis.? see prefuse.util.GraphicsLib.paint()
                    Color strokeColor = ColorLib.getColor(item.getStrokeColor());
//                    Color fillColor = ColorLib.getColor(item.getFillColor());
                    Color fillColor = Color.WHITE;
                    
//                    if (isHover) {
//                        item.setBounds(x1, y1, x1+textWidth, y1+textHeight);
                        g.setPaint(fillColor);
//                        g.fillRect((int)x, (int)y, textWidth, textHeight);
                        g.fillRect((int)x1, (int)y1, textWidth, textHeight);
                        g.setPaint(strokeColor);
//                        g.drawRect((int)x, (int)y, textWidth, textHeight);
                        g.drawRect((int)x1, (int)y1, textWidth, textHeight);
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
                drawStringMultiline(g, f, s, x1, y1);

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
    
    public static void drawStringMultiline(Graphics2D g, Font f, String s, double xPos, double yPos) {
        FontMetrics fm = g.getFontMetrics(f);
        int w = fm.stringWidth(s);
        int h = fm.getAscent();
        //g.setColor(Color.LIGHT_GRAY);
        g.setColor(Color.BLACK);
        g.setFont(f);
        
        Scanner lineSplitter = new Scanner(s);
        int lineCounter = 1;
        while (lineSplitter.hasNextLine()) {
            String line = lineSplitter.nextLine();
            g.drawString(line, (float) (xPos), (float) (yPos + h*lineCounter));
            lineCounter++;
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
            fsim.addForce(new NBodyForce(-0.4f, 25f, NBodyForce.DEFAULT_THETA));
//            fsim.addForce(new SpringForce(1e-5f,0f));
            fsim.addForce(new DragForce());
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
    } // end of inner class DataMountainForceLayout
    
}
