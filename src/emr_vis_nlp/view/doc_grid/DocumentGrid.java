package emr_vis_nlp.view.doc_grid;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.view.glasspane.MainViewGlassPane;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.List;
import java.util.*;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.filter.VisibilityFilter;
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
 * could also select additional auxiliary attributes and/or attribute values for
 * display, represented as glyphs / color stripes / etc.
 *
 * Originally was loosely based (in part) on "DataMountain" example in the Prefuse toolkit.
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
    
    // text for dividing focus sentence excerpts
    public static final String FOCUS_SENT_SPLITTER = " ... ";
    
    // initial size
    private int initWidth = 700;
    private int initHeight = 600;
    
    // renderer for doc glyphs
    private DocGlyphRenderer nodeRenderer;
    // handles layout of the document glyphs
    private DocumentGridLayoutNested documentGridLayout;
    // handles layout of the attribute value axes
    private DocumentGridAxisLayout docGridAxisLayout;
    // handles generation of glyphs
//    private DocumentShapeAction docShapeAction;   // (note: now consistently using squares)
    // handles glyph coloration
    private DocumentColorAction docColorAction;
    // handles glyph border coloration
    private DocumentBorderColorAction docBorderColorAction;
    // handles shape sizing
//    private DocumentSizeAction docSizeAction;  // (note: sizing now handled in DocumentGridNestedLayout, since sizing is bound up with placement)
    // backing table containing data
    private DocumentGridTable t;
    
    // glasspane for drawing styles docs on popup
    private MainViewGlassPane glassPane;
    
    // attributes for various visual indicators
    // TODO : support highlighting
    private String highlightAttr = null;
    private String highlightVal = null;
    private Predicate highlightPredicate = null;
    // attribute for color selection
    private String colorAttrName = null;
    
    // controller governing this DocumentGrid
    private MainController controller;
    
    // control for drag-to-reassign functionality
    private DocGridDragControl docDragControl;
    // control for click-to-zoom functionality
    private DocumentSelectControl docSelectControl;
    
    // predicate controlling which document glyphs will be rendered
    private Predicate docGlyphVisiblePredicate;
    private GlyphVisibilityFilter docGlyphVisibleFilter;
    
    // binding for string searching
    private SearchQueryBinding searchQ;
    
    // pixel buffer for visually separating document glyphs
    private int bufferPx = 2;
    
    // TODO : animators
    
    // reference to the currently-loaded display
    private static Display display;
    
    public DocumentGrid(final DocumentGridTable t, String xAxisInitName, String yAxisInitName, String colorInitName) {
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
        // rather than taking only the values which are present, let's pull the valid values directly from the ML module, since some applicable values might not be present within the current documents, but we will need these regions to be present in the visualization for dragging?
//        List<String> xAxisInitCategories = t.getValueListForAttribute(xAxisInitName);
        List<String> xAxisInitCategories = controller.getValuesForAttribute(xAxisInitName);
//        List<String> yAxisInitCategories = t.getValueListForAttribute(yAxisInitName);
        List<String> yAxisInitCategories = controller.getValuesForAttribute(yAxisInitName);
        documentGridLayout = new DocumentGridLayoutNested(DATA_GROUP, xAxisInitName, yAxisInitName, xAxisInitCategories, yAxisInitCategories);
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
        // add auxiliary renderer for axes
        rf.add(new InGroupPredicate(ALL_LABEL), new DocumentGridAxisRenderer(documentGridLayout));
        m_vis.setRendererFactory(rf);

        // ActionList for simple repaint (for simple controls)
        ActionList repaint = new ActionList();
        repaint.add(new RepaintAction());
        m_vis.putAction("repaint", repaint);
        
        // update actionlist: performs coloration, sizing
        ActionList updateOnce = new ActionList();
        // size action
        // note: sizing is now controlled by the layout action
//        SizeAction sizeActionUpdate = new DocGlyphSizeAction(DATA_GROUP);
//        updateOnce.add(sizeActionUpdate);
        // shape action
        // note: now using a constant square shape
        ShapeAction squareShapeAction = new SquareShapeAction();
        updateOnce.add(squareShapeAction);
        // color action(s)
        List<String> colorInitCategories = t.getValueListForAttribute(colorInitName);
        docColorAction = new DocumentColorAction(DATA_GROUP, colorInitName, colorInitCategories);
        updateOnce.add(docColorAction);
        docBorderColorAction = new DocumentBorderColorAction(DATA_GROUP);
        updateOnce.add(docBorderColorAction);
        // visibility filter
        docGlyphVisiblePredicate = new InGroupPredicate(DATA_GROUP);
        docGlyphVisibleFilter = new GlyphVisibilityFilter(DATA_GROUP, docGlyphVisiblePredicate);
        updateOnce.add(docGlyphVisibleFilter);
        // repaint action
        updateOnce.add(new RepaintAction());
        // add update actionlist to vis
        m_vis.putAction("updateOnce", updateOnce);
        
        // TODO : enable proper animation
//        ActionList animate = new ActionList(1250);
//        animate.setPacingFunction(new SlowInSlowOutPacer());
//        animate.add(new LocationAnimator(DATA_GROUP));
//        animate.add(new SizeAction(DATA_GROUP));
//        animate.add(new RepaintAction());
//        m_vis.putAction("animate", animate);
        
        // get reference to glasspane
        glassPane = controller.getGlassPane();
        
        // set initial / basic properties of the display
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
                // update focus text for all items in visualtable, wrt. query
                // TODO : improve efficiency of this predicate updating method
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
                        // TODO : proper sentence parsing
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
                            focusText.append(FOCUS_SENT_SPLITTER);
                            for (String focusSent : focusSents) {
                                focusText.append(focusSent);
                                focusText.append(FOCUS_SENT_SPLITTER+"\n");
                            }
                            t.setString(i, DocumentGridTable.NODE_FOCUS_TEXT, focusText.toString());
                        }
//                    }
                }
                
                // run repaint actions
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
        docSelectControl = new DocumentSelectControl(glassPane);
        addControlListener(docSelectControl);
        
        // run actionlists
        
        m_vis.alwaysRunAfter("init", "updateOnce");
        
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
        
        // redo layout
        m_vis.run("init");
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
     * Simply rerun the basic layout, painting actions for this view
     */
    public void updateView() {
        m_vis.run("init");
    }
    
    public void updateXAxis(String xAxisAttrName) {
        // pull applicable values from ML module
        List<String> xAxisCategories = MainController.getMainController().getValuesForAttribute(xAxisAttrName);
        
        documentGridLayout.updateXAxis(xAxisAttrName, xAxisCategories);
        // update the visual axis indicator as well
        docGridAxisLayout.docGridLayoutUpdated();
//        m_vis.run("repaint");  // (don't yet run update actions, since the Controller may be modifying multiple axes; let controller call updateView() itself once it's done updating the attributes of the view)
    }
    
    public void updateYAxis(String yAxisAttrName) {
        // pull applicable values from ML module
        List<String> yAxisCategories = MainController.getMainController().getValuesForAttribute(yAxisAttrName);
        
        documentGridLayout.updateYAxis(yAxisAttrName, yAxisCategories);
        // update the visual axis indicator as well
        docGridAxisLayout.docGridLayoutUpdated();
//        m_vis.run("repaint");  // (don't yet run update actions, since the Controller may be modifying multiple axes; let controller call updateView() itself once it's done updating the attributes of the view)
    }
    
    public void updateColorAttr(String colorAttrName) {
        // pull applicable values from ML module
        List<String> colorCategories = MainController.getMainController().getValuesForAttribute(colorAttrName);
        // update colorAction
        this.colorAttrName = colorAttrName;
        docColorAction.updateColorAttr(colorAttrName, colorCategories);
        //        m_vis.run("repaint");  // (don't yet run update actions, since the Controller may be modifying multiple axes; let controller call updateView() itself once it's done updating the attributes of the view)
    }
    
//    public void updateShapeAttr(String shapeAttrName) {
//        updateShapeAttr(shapeAttrName, false);
//    }
//    
//    public void updateShapeAttr(String shapeAttrName, boolean doUpdate) {
//        if (docShapeAction != null) {
//            this.shapeAttrName = shapeAttrName;
//            List<String> shapeCategories = t.getValueListForAttribute(shapeAttrName);
//            // update shapeAction
//            docShapeAction.updateShapeAttr(shapeAttrName, shapeCategories);
//            if (doUpdate)
//                m_vis.run("repaint");
//        }
//    }
    
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
//        anchorUpdateControl.setEnabled(true);
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
     * Set fill color based on various criteria.
     */
    public static class DocumentColorAction extends ColorAction {
        
        // lighter pastel palette
        // (adopetd from http://www.colorschemer.com/schemes/viewscheme.php?id=5128)
        public static final Color[] GLYPH_COLOR_PALETTE = {
            new Color(204, 204, 255), 
            new Color(204, 255, 204), 
            new Color(255, 204, 204),
            new Color(204, 255, 255), 
            new Color(255, 255, 204), 
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
                if (attrValColor == null) {
                    return Color.CYAN.getRGB();
                }
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

    /*
     * Handles rendering of document glyphs and drawing of document text (for
     * selected document[s]). 
     */
    public class DocGlyphRenderer extends ShapeRenderer {
        
        public DocGlyphRenderer() {
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

//                    String s = "doc=" + item.getString(NODE_NAME) + "\n";
                    String s = "";

                    // set text: full document if no search term, else excerpts containing the search term
                    String queryStr = searchQ.getSearchSet().getQuery();
                    String focusText = item.getString(DocumentGridTable.NODE_FOCUS_TEXT);
                    if (queryStr != null && !queryStr.isEmpty() && focusText != null && !focusText.equals("null") && !focusText.equals("")) {
                        // if search query and terms present in document, use term-containing spans
                        s += focusText;
                    } else if (queryStr != null && !queryStr.isEmpty() && focusText.equals(FOCUS_SENT_SPLITTER)) {
                        // if search query but no terms present in document, use blank
                        s += "";
                    } else if ((queryStr == null || queryStr.isEmpty()) && item.canGetInt(NODE_ID)) {
                        // if no search query, build feature-oriented summary based on color attribute
                        s = controller.getDocumentSummary(item.getInt(NODE_ID), colorAttrName);
                    }

                    // TODO : idea: set font size dynamically based on number of active nodes? based on size of rect?
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
    
    /**
     * Draws as much as possible of a given string into a 2d square region in a graphical space.
     * 
     * @param g component onto which to draw
     * @param f font to use in drawing the string
     * @param s string to be drawn
     * @param xPos
     * @param yPos
     * @param width
     * @param height 
     */
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
        
//        private boolean isPopupLoaded;
        private MainViewGlassPane glassPane;
        
        /**
         * indicates that we should not zoom on next mouseevent; needed in order to prevent glasspane from immediately reopening after a click-to-close
         */
        private VisualItem disableNextZoomItem = null;
        
        public DocumentSelectControl(MainViewGlassPane glassPane) {
            super();
            this.glassPane = glassPane;
//            isPopupLoaded = false;
        }
        
        public void disableNextZoomOnItem(VisualItem item) {
            disableNextZoomItem = item;
        }
        
        /**
         * Given a MouseEvent, finds the VisualItem (if any) on which the event occurred.
         * 
         * @param e
         * @return item on which the MouseEvent occurred, or null if it did not occur on any item.
         */
        public VisualItem findClickedItem(MouseEvent e) {
            
            // get click coordinates
            int x = e.getX();
            int y = e.getY();
            // translate coordinates to Prefuse region
            int xOffset = 0;
            int yOffset = 0;
            JComponent component = display;
            // recursively go through this Component's ancestors, summing offset information in order to get the absolute position relative to window
            do {
                Point visLocation = component.getLocation();
                xOffset += visLocation.x;
                yOffset += visLocation.y;
            } while ((!component.getParent().getClass().equals(JRootPane.class)) && (component = (JComponent) component.getParent()) != null);
            x -= xOffset;
            y -= yOffset;
            
            // debug
//            System.out.println("debug: "+this.getClass().getName()+": mouse click at ("+x+", "+y+")");
            
            // search each item, determining which was clicked on
            Iterator items = m_vis.getGroup(DATA_GROUP).tuples();
            while (items.hasNext()) {
                VisualItem item = (VisualItem)items.next();
                double itemX = item.getX();
                double itemY = item.getY();
                double itemW = item.getDouble(WIDTH);
                double itemH = item.getDouble(HEIGHT);
                if (x >= itemX && x <= (itemX+itemW) && y >= itemY && y <= (itemY+itemH)) {
                    // debug
//                    System.out.println("debug: "+this.getClass().getName()+": match: ("+itemX+", "+itemY+", "+itemW+", "+itemH+")");
                    return item;
                } else {
                    // debug
//                    System.out.println("debug: "+this.getClass().getName()+": no-match: ("+itemX+", "+itemY+", "+itemW+", "+itemH+")");
                }
            }
            return null;
            
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
                    
                    // ensure that the layout has been reprocessed before loading glasspane
                    documentGridLayout.categoricalLayout();
                    
                    m_vis.run("init");  // init is needed to run here, since sizing is tightly-bound with our faux-fisheye zooming
//                    m_vis.run("repaint");
                    
                    // appear the glasspane at appropriate size & location
                    // get relative location of Visualization
                    int xOffset = 0;
                    int yOffset = 0;
                    JComponent component = display;
                    // recursively go through this Component's ancestors, summing offset information in order to get the absolute position relative to window
                    do {
                        Point visLocation = component.getLocation();
                        xOffset += visLocation.x;
                        yOffset += visLocation.y;
                    } while ((!component.getParent().getClass().equals(JRootPane.class)) && (component = (JComponent) component.getParent()) != null);
                    // debug
//                    System.out.println("debug: " + this.getClass().getName() + ": offsets: " + xOffset + ", " + yOffset);
                    
                    String attrIdStr = colorAttrName;  // TODO make highlighting more general, not just based on color!
                    
                    // make sure that the clicked item is not temporarily ``disabled'' (ie, zoom state was not immediately toggled by a glasspane-oriented class)
                    if (disableNextZoomItem == null || disableNextZoomItem != item) {
                        disableNextZoomItem = null;
                        int x = (int) item.getEndX() + bufferPx + xOffset;
                        int y = (int) item.getEndY() + bufferPx + yOffset;
                        int w = (int) item.getDouble(WIDTH_END) - 2 * bufferPx;
                        int h = (int) item.getDouble(HEIGHT_END) - 2 * bufferPx;
                        // debug
                        System.out.println("debug: " + this.getClass().getName() + ": displaying sized glasspane at x=" + x + ", y=" + y + ", w=" + w + ", h=" + h);
                        glassPane.displaySizedPane((int) x, (int) y, (int) w, (int) h, item);

                        AbstractDocument doc = glassPane.getAbstDoc();
                        controller.writeDocTextWithHighlights(doc, nodeId, attrIdStr);

                        glassPane.setBackgroundColor(new Color(docColorAction.getColor(item)));
                    } else {
                        disableNextZoomItem = null;
                    }
                    
                }
            }
            
        }

        @Override
        public void itemEntered(VisualItem item, MouseEvent e) {
        }

        @Override
        public void itemExited(VisualItem item, MouseEvent e) {
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
     * events directly to it. 
     * TODO : refactor this clunky way of passing around mouse events.
     *
     * @return 
     */
    public DocGridDragControl getDragControl() {
        if (docDragControl != null)
            return docDragControl;
        return null;
    }
    public DocumentSelectControl getSelectControl() {
        if (docSelectControl != null)
            return docSelectControl;
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
        }

        @Override
        public void itemPressed(VisualItem item, MouseEvent e) {
            // on right-mouse press, start dragging the document
            if (SwingUtilities.isRightMouseButton(e)) {
                // debug
                System.out.println("debug: " + this.getClass().getName() + ": item pressed w/ right mouse");
                // drag, set the focus to the current node
                Visualization vis = item.getVisualization();
                vis.getFocusGroup(Visualization.FOCUS_ITEMS).setTuple(item);
                item.setFixed(true);
                dragged = false;
                Display d = controller.getDocumentGrid();
                down = d.getAbsoluteCoordinate(e.getPoint(), down);
            }
        }

        @Override
        public void itemReleased(VisualItem item, MouseEvent e) {
            // when right-mouse released, release the dragged document glyph
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
                double x = item.getX();
                double y = item.getY();
                double w = item.getDouble(WIDTH);
                double h = item.getDouble(HEIGHT);
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
                
        }

        @Override
        public void itemDragged(VisualItem item, MouseEvent e) {
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

                item.setStartX(x);
                item.setStartY(y);
                item.setX(x + dx);
                item.setY(y + dy);
                item.setEndX(x + dx);
                item.setEndY(y + dy);
                
                item.setBounds(x+dx, y+dy, w, h);

                if (repaint) {
                    item.getVisualization().repaint();
                }

                down.setLocation(temp);
                if (action != null) {
                    d.getVisualization().run(action);
                }

            }
        }

        @Override
        public void itemEntered(VisualItem item, MouseEvent e) {
        }

        @Override
        public void itemExited(VisualItem item, MouseEvent e) {
        }
    }
    
    /*
     * Force controllers adpoted (and expanded) from the DataMountain example, in order to prevent / reduce initial occlusion on layout, and to reduce occlusion on dogument glyph dragging (when distortions are not active)
     */
    // (eliminated from design; see archive branch of github repo)
    
    
    
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
    
    /**
     * Renderer for the lines separating each of the attribute value regions
     */
    public class DocumentGridAxisRenderer extends AbstractShapeRenderer {

        private Line2D m_line = new Line2D.Double();
        private Rectangle2D m_box = new Rectangle2D.Double();
        
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
        @Override
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
        @Override
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
    
}
