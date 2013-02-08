package emr_vis_nlp.view.doc_grid;

import emr_vis_nlp.controller.MainController;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.RectangularShape;
import java.util.Iterator;
import java.util.List;
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
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
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
    // handles shape sizing
    private DocumentSizeAction docSizeAction;
    // backing table containing data
    private DocumentGridTable t;
    
    
    // controller governing this DocumentGrid
    private MainController controller;
    
    public DocumentGrid(MainController controller, DocumentGridTable t, String xAxisInitName, String yAxisInitName) {
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
        documentGridLayout = new DocumentGridLayout(DATA_GROUP, xAxisInitName, yAxisInitName, xAxisInitCategories, yAxisInitCategories);
        init.add(documentGridLayout);
        // add axes layout action
        docGridAxisLayout = new DocumentGridAxisLayout(ALL_LABEL, documentGridLayout);
        init.add(docGridAxisLayout);
        // set glyph shapes
        // TODO more complex / variad shapes! encode data as glyph?
//        docShapeAction = new DocShapeAction(DATA_GROUP, Constants.SHAPE_RECTANGLE);
        docShapeAction = new DocumentShapeAction(DATA_GROUP, Constants.SHAPE_DIAMOND);
        init.add(docShapeAction);
        // encode size
        docSizeAction = new DocumentSizeAction(DATA_GROUP);
        init.add(docSizeAction);
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
        preforce.add(new DataMountainForceLayout(true));
        m_vis.putAction("preforce", preforce);
        
        // update actionlist: performs coloration, sizing
        // TODO: merge update and init?
//        ActionList update = new ActionList();
        ActionList update = new ActionList(Activity.INFINITY);
        // TODO size action
//        SizeAction sizeActionUpdate = new DocGlyphSizeAction(DATA_GROUP);
//        update.add(sizeActionUpdate);
        update.add(docSizeAction);
        // color action(s)
        ColorAction colorActionUpdate = new DocGlyphColorAction(DATA_GROUP);
        update.add(colorActionUpdate);
        ColorAction borderColorActionUpdate = new DocGlyphBorderColorAction(DATA_GROUP);
        update.add(borderColorActionUpdate);
        // TODO add axes color actions?
//        ColorAction labelColorActionUpdate = new LabelColorAction(ALL_LABEL);
//        update.add(labelColorActionUpdate);
        // repaint action
        update.add(new RepaintAction());
        // add update actionlist to vis
        m_vis.putAction("update", update);


        // force stabilizing action? (borrowed from datamountain)
//        ActionList preforce = new ActionList(1000);
//        preforce.add(new DataMountainForceLayout(true));
//        m_vis.putAction("preforce", preforce);
//
        // force action? (borrowed from datamountain)
//        final ForceDirectedLayout fl = new DataMountainForceLayout(false);
//        ActivityListener fReset = new ActivityAdapter() {
//            public void activityCancelled(Activity a) {
//                fl.reset(); 
//             } 
//        };
//        ActionList forces = new ActionList(Activity.INFINITY);
//        forces.add(fl);
//        forces.add(update);
//        forces.addActivityListener(fReset);
//        m_vis.putAction("forces", forces);
        
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
        m_vis.run("init");
//        m_vis.runAfter("preforce", "update");  // temporarily (?) disable for testing
//        m_vis.run("preforce");
        m_vis.run("update");  // remove if forces ("runAfter") are reenabled
        
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
        m_vis.run("init");
//        m_vis.runAfter("preforce", "update");  // temporarily (?) disable for testing
//        m_vis.run("preforce");
        m_vis.run("update");  // remove if forces ("runAfter") are reenabled
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

    /**
     * ColorAction for assigning border colors to document glyphs.
     */
    public static class DocGlyphBorderColorAction extends ColorAction {

        public DocGlyphBorderColorAction(String group) {
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
    public static class DocGlyphColorAction extends ColorAction {

//        private ColorMap cmap = new ColorMap(
//                ColorLib.getInterpolatedPalette(10,
//                ColorLib.rgb(85, 85, 85), ColorLib.rgb(0, 0, 0)), 0, 9);

        public DocGlyphColorAction(String group) {
            super(group, VisualItem.FILLCOLOR);
        }

        @Override
        public int getColor(VisualItem item) {
            
            // TODO make color based upon doc properties
            Color white = Color.WHITE;
            return white.getRGB();
            
        }
    }
    
    /**
     * Color action for setting appearance of axes labels.
     */
    public static class LabelColorAction extends ColorAction {

//        private ColorMap cmap = new ColorMap(
//                ColorLib.getInterpolatedPalette(10,
//                ColorLib.rgb(85, 85, 85), ColorLib.rgb(0, 0, 0)), 0, 9);

        public LabelColorAction(String group) {
            super(group, VisualItem.FILLCOLOR);
        }

        @Override
        public int getColor(VisualItem item) {
            
            // TODO make color based upon doc properties
            Color white = Color.WHITE;
            return white.getRGB();
            
        }
    }

    /*
     * Handles sizing of document glyphs, controls size change of glyph on
     * selection
     * 
     * note: this is now handled in renderer and doc glyph control?
     */
    public static class DocumentSizeAction extends SizeAction {
        
        public static final double HOVER_SIZE_MULT = 1.5;
        public static final double BASE_SIZE = 10;
        
        public DocumentSizeAction(String group) {
            super(group, BASE_SIZE);
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
        
        public DocumentShapeAction(String group) {
            super(group);
        }
        
        public DocumentShapeAction(String group, int shape) {
            super(group, shape);
        }
        
        @Override
        public int getShape(VisualItem item) {
            return super.getShape(item);
        }
        
    }

    /*
     * Handles rendering of document glyphs and drawing of document text (for
     * selected document[s]). Should also handle drawing of axes. 
     */
    public static class DocGlyphRenderer extends ShapeRenderer {
        
        // base size for visualitems (in pixels)
        public static final int BASE_SIZE = 10;
        
        public DocGlyphRenderer() {
            super(BASE_SIZE);
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
                    Color fillColor = ColorLib.getColor(item.getFillColor());
                    
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
            fsim.addForce(new SpringForce(1e-5f,0f));
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
