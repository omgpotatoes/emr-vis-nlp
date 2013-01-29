package emr_vis_nlp.view.doc_grid;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.model.mpqa_colon.DatasetTermTranslator;
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
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.controls.PanControl;
import prefuse.data.Schema;
import prefuse.data.query.ObjectRangeModel;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AxisRenderer;
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
    
//    private AxisLayout axisLayoutX;
//    private AxisLabelLayout axisLabelLayoutX;
//    private AxisLayout axisLayoutY;
//    private AxisLabelLayout axisLabelLayoutY;
    private DocumentGridLayout documentGridLayout;
    private DocumentGridAxisLayout docGridAxisLayout;
    
    // controller governing this DocumentGrid
    private MainController controller;
    
//    private AxisLabelLayout axisLabelLayoutX;
//    private AxisLabelLayout axisLabelLayoutY;
    
    public DocumentGrid(MainController controller, DocumentGridTable t, String xAxisInitName, String yAxisInitName) {
        super(new Visualization());
        this.controller = controller;
        // add data to visualization (tables, ...)
        m_vis.addTable(DATA_GROUP, t);

        // set up renderer for nodes, set rendererFactory
        DocGlyphRenderer nodeRenderer = new DocGlyphRenderer();
        // perform additional optional renderer setup here
        // add renderer to visualization
//        m_vis.setRendererFactory(new DefaultRendererFactory(nodeRenderer));
        DefaultRendererFactory rf = new DefaultRendererFactory();
        rf.setDefaultRenderer(nodeRenderer);
//        rf.setDefaultRenderer(new ShapeRenderer());
//        rf.add(new InGroupPredicate(Y_LABEL), new AxisRenderer(Constants.FAR_LEFT, Constants.CENTER));
//        rf.add(new InGroupPredicate(X_LABEL), new AxisRenderer(Constants.CENTER, Constants.FAR_BOTTOM));
        rf.add(new InGroupPredicate(ALL_LABEL), new DocumentGridAxisRenderer(Constants.CENTER, Constants.CENTER));
        m_vis.setRendererFactory(rf);

        // setup init actionlist
        ActionList init = new ActionList();
        // set-up initial layout
        // layout action
        List<String> xAxisInitCategories = t.getValueListForAttribute(xAxisInitName);
        List<String> yAxisInitCategories = t.getValueListForAttribute(yAxisInitName);
        documentGridLayout = new DocumentGridLayout(DATA_GROUP, xAxisInitName, yAxisInitName, xAxisInitCategories, yAxisInitCategories);
        init.add(documentGridLayout);
        // set up axes
////        Object[] xAxisInitObjs = new Object[xAxisInitCategories.size()+1];
//        Object[] xAxisInitObjs = new Object[xAxisInitCategories.size()];
//        for (int i=0; i<xAxisInitCategories.size(); i++) {
//            xAxisInitObjs[i] = xAxisInitCategories.get(i);
////            xAxisInitObjs[i+1] = xAxisInitCategories.get(i);
//        }
////        xAxisInitObjs[xAxisInitObjs.length-1] = "";
////        xAxisInitObjs[0] = "";
////        Object[] yAxisInitObjs = new Object[yAxisInitCategories.size()+1];
//        Object[] yAxisInitObjs = new Object[yAxisInitCategories.size()];
//        for (int i=0; i<yAxisInitCategories.size(); i++) {
//            yAxisInitObjs[i] = yAxisInitCategories.get(i);
////            yAxisInitObjs[i+1] = yAxisInitCategories.get(i);
//        }
////        yAxisInitObjs[yAxisInitObjs.length-1] = "";
////        yAxisInitObjs[0] = "";
////        Rectangle bounds = getBounds();
////        List<String> defaultVals = DatasetTermTranslator.getDefaultValList();
////        // ensure that we add a blank to the beginning!
////        ObjectRangeModel xAxisRangeModel = new ObjectRangeModel(xAxisInitObjs);
////        ObjectRangeModel yAxisRangeModel = new ObjectRangeModel(yAxisInitObjs);
//        ObjectRangeModel xAxisRangeModel = new ObjectRangeModel(xAxisInitObjs);
//        ObjectRangeModel yAxisRangeModel = new ObjectRangeModel(yAxisInitObjs);
//        axisLabelLayoutX = new AxisLabelLayout(X_LABEL, Constants.X_AXIS, xAxisRangeModel);
//        axisLabelLayoutY = new AxisLabelLayout(Y_LABEL, Constants.Y_AXIS, yAxisRangeModel);
//        init.add(axisLabelLayoutX);
//        init.add(axisLabelLayoutY);
        docGridAxisLayout = new DocumentGridAxisLayout(ALL_LABEL, documentGridLayout);
        init.add(docGridAxisLayout);
        // add init actionlist to vis
        m_vis.putAction("init", init);

        // ActionList for repainting (for controls)
        ActionList repaint = new ActionList();
        repaint.add(new RepaintAction());
        m_vis.putAction("repaint", repaint);
        
        // ActionList for initially stabilizing the forces
        ActionList preforce = new ActionList(1000);
        preforce.add(new DataMountainForceLayout(true));
        m_vis.putAction("preforce", preforce);
        
        // setup update actionlist
        ActionList update = new ActionList();
        // size action
//        SizeAction sizeActionUpdate = new DocGlyphSizeAction(DATA_GROUP);
//        update.add(sizeActionUpdate);
        // color action(s)
        ColorAction colorActionUpdate = new DocGlyphColorAction(DATA_GROUP);
        update.add(colorActionUpdate);
        ColorAction borderColorActionUpdate = new DocGlyphBorderColorAction(DATA_GROUP);
        update.add(borderColorActionUpdate);
        // repaint action
        update.add(new RepaintAction());
        
//        axisLayoutX = new AxisLayout(DATA_GROUP, xAxisInitField, Constants.X_AXIS);
////        axisLayoutX.setRangeModel(new ObjectRangeModel(new Integer[]{0,1,2,3}));
////        axisLayoutX.setDataType(Constants.ORDINAL);
//        axisLayoutY = new AxisLayout(DATA_GROUP, yAxisInitField, Constants.Y_AXIS);
////        axisLayoutY.setRangeModel(new ObjectRangeModel(new Integer[]{0,1,2,3}));
////        axisLayoutY.setDataType(Constants.ORDINAL);
//        axisLabelLayoutY = new AxisLabelLayout(Y_LABEL, axisLayoutY);
//        update.add(axisLayoutX);
//        update.add(axisLayoutY);
//        update.add(axisLabelLayoutX);
//        update.add(axisLabelLayoutY);
        // add update actionlist to vis
        m_vis.putAction("update", update);


        // force stabilizing action? (borrowed from datamountain)
//        ActionList preforce = new ActionList(1000);
//        preforce.add(new DataMountainForceLayout(true));
//        m_vis.putAction("preforce", preforce);

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
        
        setSize(700, 600);
        // set borders, etc.
        setBorder(BorderFactory.createEmptyBorder(30,20,5,20));
        
        // set up control listeners
        // set up various doc-glyph interaction controls
        addControlListener(new DocGlyphControl());
        // zoom with wheel
//        addControlListener(new WheelZoomControl());
        // zoom with background right-drag
//        addControlListener(new ZoomControl(Control.RIGHT_MOUSE_BUTTON));
        // pan with background left-drag
        addControlListener(new PanControl(Control.RIGHT_MOUSE_BUTTON));
        
        // run actionlists
        m_vis.run("init");
//        m_vis.runAfter("preforce", "update");  // temporarily (?) disable for testing
//        m_vis.run("preforce");
        m_vis.run("update");  // remove if forces ("runAfter") are reenabled
        
    }
    
    public void resetSize(int newWidth, int newHeight) {
        setSize(newWidth, newHeight);
        // update axes' sizes?
//        axisLabelLayoutX.setLayoutBounds(getBounds());
//        axisLabelLayoutY.setLayoutBounds(getBounds());
        
        // redo layout, force initialization
        m_vis.run("init");
//        m_vis.runAfter("preforce", "update");  // temporarily (?) disable for testing
//        m_vis.run("preforce");
        m_vis.run("update");  // remove if forces ("runAfter") are reenabled
    }

    public static class DocGlyphBorderColorAction extends ColorAction {

        public DocGlyphBorderColorAction(String group) {
            super(group, VisualItem.STROKECOLOR);
        }

        @Override
        public int getColor(VisualItem item) {
//            NodeItem nitem = (NodeItem) item;
            if (item.isHover()) {
                return ColorLib.rgb(99, 130, 191);
            }
            return ColorLib.gray(50);
            
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

                // for now, just return lightgray
                Color lightgray = Color.LIGHT_GRAY;
                return lightgray.getRGB();
            
//            int attrValIndex = 0;
//            if (item.canGetInt("attr_val_index")) {
//                attrValIndex = item.getInt("attr_val_index");
//            }
//            if (attrValIndex == -1) {
//                // just return lightgray? for docs
//                Color lightgray = Color.LIGHT_GRAY;
//                return lightgray.getRGB();
//            } else {
//
//                // get color based on depth, mod of category id of item
//                int depth = (item instanceof NodeItem ? ((NodeItem) item).getDepth() : 0);
//                int colorPaletteIndex = (depth + 1) % DOCTREEMAP_GROUP_PALETTE_RGB_2.length;
////            System.out.println("debug: depth="+depth);
//                // depth = simple int giving depth in tree (start at 0 for root)
////                String[] colorPaletteNames = DOCTREEMAP_GROUP_PALETTE[colorPaletteIndex];
//                Color[] colorPalette = DOCTREEMAP_GROUP_PALETTE_RGB_2[colorPaletteIndex];
////                String colorName = colorPaletteNames[attrValIndex % colorPaletteNames.length];
//                Color color = colorPalette[attrValIndex % colorPalette.length];
//                int colorInt = color.getRGB();
//                return colorInt;
//
//            }
////            if (m_vis.isInGroup(item, Visualization.SEARCH_ITEMS)) {
////                return ColorLib.rgb(191, 99, 130);
////            }
////
////            double v = (item instanceof NodeItem ? ((NodeItem) item).getDepth() : 0);
////            return cmap.getColor(v);
        }
    }

    /*
     * Handles sizing of document glyphs, controls size change of glyph on
     * selection
     * 
     * note: this is now handled in renderer and doc glyph control
     */
//    public static class DocGlyphSizeAction extends SizeAction {
//        
//        public static final double HOVER_SIZE_MULT = 4.5;
//        public static final double BASE_SIZE = 10;
//        
//        public DocGlyphSizeAction(String group) {
//            // TODO set size relative to # of docs in grid?
//            super(group, BASE_SIZE);
////            super(group);
//        }
//        
//        public DocGlyphSizeAction() {
//            super();
//        }
//        
//        // size should blow up on hover / click
//        @Override
//        public double getSize(VisualItem item) {
////            NodeItem nitem = (NodeItem) item;
//            if (item.isHover()) {
//                return super.getSize(item) * HOVER_SIZE_MULT;
//            }
//            return super.getSize(item);
//        }
//        
//    }

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

            // idea: rather than rendering fixed-size, can we first compute size of text, then set size of item equal to size of text?

            Shape shape = getShape(item);
            if (shape != null) {

                double xPos = shape.getBounds().getX();
                double yPos = shape.getBounds().getY();
//            double xPos = item.getX();
//            double yPos = item.getY();
                String s = item.getString(NODE_NAME);
                if (item.isHover()) {
                    s = item.getString(NODE_TEXT);
                }

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
                if ( shape instanceof RectangularShape ) {
                    RectangularShape r = (RectangularShape) shape;
                    double x = r.getX();
                    double y = r.getY();
                    
                    // use our own painting code, for fine-grained control of size
                    // TODO move this into separate method, add code for more robust glyph vis.? see prefuse.util.GraphicsLib.paint()
                    Color strokeColor = ColorLib.getColor(item.getStrokeColor());
                    Color fillColor = ColorLib.getColor(item.getFillColor());
                    
                    g.setPaint(fillColor);
                    g.fillRect((int)x, (int)y, textWidth, textHeight);
                    g.setPaint(strokeColor);
                    g.drawRect((int)x, (int)y, textWidth, textHeight);
                    
                }
                
//            super.render(g, item);
                
                drawStringMultiline(g, f, s, xPos, yPos);

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
    
    public class DocGlyphControl extends ControlAdapter {

        public static final double SELECTED_MULT = 30.;

        public DocGlyphControl() {
            super();
        }

        @Override
        public void itemEntered(VisualItem item, MouseEvent e) {
            Display d = (Display) e.getSource();

//            if (item instanceof NodeItem) {
                // blowup box on hover, populate with doc text
//            double x = item.getBounds().getX();
//            double y = item.getBounds().getY();
//            double width = item.getBounds().getWidth();
//            double height = item.getBounds().getHeight();
//            
//            double blowupWidth = width*SELECTED_MULT;
//            double blowupHeight = height*SELECTED_MULT;
//            
//            item.getBounds().setRect(x, y, blowupWidth, blowupHeight);

                item.setSize(item.getSize() * SELECTED_MULT);

                // redraw
//            m_vis.run("update");
                m_vis.run("repaint");

//            }

        }

        @Override
        public void itemExited(VisualItem item, MouseEvent e) {
            Display d = (Display) e.getSource();

//            if (item instanceof NodeItem) {
                // shrink box to orig size

//            double x = item.getBounds().getX();
//            double y = item.getBounds().getY();
//            double blowupWidth = item.getBounds().getWidth();
//            double blowupHeight = item.getBounds().getHeight();
//            
//            double width = blowupWidth/SELECTED_MULT;
//            double height = blowupHeight/SELECTED_MULT;
//            
//            item.getBounds().setRect(x, y, width, height);

                item.setSize(item.getSize() / SELECTED_MULT);

                // redraw
//            m_vis.run("update");
                m_vis.run("repaint");

//            }
            
        }
        
        @Override
        public void itemPressed(VisualItem item, MouseEvent e) {
            Display d = (Display)e.getSource();
            
            // TODO complete control code
            
            
            
        }
        
        @Override
        public void itemReleased(VisualItem item, MouseEvent e) {
        }

        @Override
        public void itemClicked(VisualItem item, MouseEvent e) {
            Display d = (Display) e.getSource();

            // temporarily load text popup on click
            // eventually, we want to embed these functions into the display itself
            int itemId = -1;
            if (item.canGetInt(NODE_ID)) {
                itemId = item.getInt(NODE_ID);
                controller.displayDocDetailsWindow(itemId);
            }
            

////            if (item instanceof NodeItem) {
//                String text = item.getString(NODE_TEXT);
//
//                JPopupMenu jpub = new JPopupMenu();
//                jpub.add(text);
//                jpub.show(e.getComponent(), (int) item.getX(),
//                        (int) item.getY());
////            }

                
        }

        @Override
        public void itemDragged(VisualItem item, MouseEvent e) {}
        
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
