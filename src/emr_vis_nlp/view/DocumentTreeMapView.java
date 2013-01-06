package emr_vis_nlp.view;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.model.mpqa_colon.Document;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.controls.ControlAdapter;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.query.SearchQueryBinding;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.*;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JSearchPanel;
import prefuse.util.ui.UILib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTree;
import prefuse.visual.sort.TreeDepthItemSorter;

/**
 * Responsibility of class is to represent collection of documents as a treemap
 * responsive to the controller.
 *
 * note: code adopted from the Prefuse TreeMap demo at:
 * http://prefuse.org/gallery/treemap/TreeMap.java
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocumentTreeMapView extends Display {

    public static final String[][] DOCTREEMAP_GROUP_PALETTE = {
        {"red", "maroon", "brown", "lightcoral", "rosybrown"},
        {"indigo", "mediumslateblue", "blueviolet", "darkslateblue", "magenta"},
        {"darkgreen", "limegreen", "darkolivegreen", "palegreen", "olivedrab"},
        {"salmon", "orangered", "chocolate", "coral", "sienna"},};
    public static final Color[][] DOCTREEMAP_GROUP_PALETTE_RGB = {
        {new Color(255, 0, 0), new Color(176, 48, 96), new Color(165, 42, 42), new Color(240, 128, 128), new Color(188, 143, 143)},
        {new Color(75, 0, 130), new Color(123, 104, 238), new Color(138, 43, 226), new Color(72, 61, 139), new Color(255, 0, 255)},
        {new Color(0, 100, 0), new Color(50, 205, 50), new Color(85, 107, 47), new Color(152, 251, 152), new Color(107, 142, 35)},
        {new Color(250, 128, 114), new Color(255, 69, 0), new Color(210, 105, 30), new Color(255, 127, 80), new Color(160, 82, 45)},};
    public static final Color[][] DOCTREEMAP_GROUP_PALETTE_RGB_2 = {
        {new Color(255, 0, 0), new Color(191, 48, 48), new Color(255, 64, 64), new Color(255, 115, 115)},
        {new Color(18, 178, 37), new Color(47, 119, 56), new Color(91, 229, 108), new Color(145, 229, 155)},
        {new Color(230, 138, 23), new Color(154, 113, 61), new Color(246, 181, 98), new Color(246, 207, 156)},
        {new Color(27, 77, 151), new Color(46, 68, 101), new Color(99, 148, 220), new Color(147, 176, 220)},
        {new Color(107, 23, 153), new Color(82, 44, 103), new Color(176, 95, 220), new Color(193, 144, 220)}
//        {new Color, new Color, new Color, new Color, },
    };
    // TODO : revise treemap code in-line with previous ideas: children-on-top, buffer space, click-based interaction, appropriate color schemes, appropriate attribute selection mechanisms, animation
//    private static final String label = "documentTreeMapView";
    private static final String nodeName = "name";
    private static final String tree = "tree";
    private static final String treeNodes = "tree.nodes";
    private static final String treeEdges = "tree.edges";
    public static final double RECT_BUFFER = 10.;
//    public static final double RECT_BUFFER = 50.;
    private SearchQueryBinding searchQ;

    public DocumentTreeMapView(DocumentTree t) {
        super(new Visualization());

        VisualTree vt = m_vis.addTree(tree, t);
        m_vis.setVisible(treeEdges, null, false);

        m_vis.setRendererFactory(
                //            new DefaultRendererFactory(new TreeMapRenderer(label)));
                new DefaultRendererFactory(new TreeMapRenderer(nodeName)));

        // border colors
        final ColorAction borderColor = new BorderColorAction(treeNodes);
        final ColorAction fillColor = new FillColorAction(treeNodes);

        // full paint
        ActionList fullPaint = new ActionList();
        fullPaint.add(fillColor);
        fullPaint.add(borderColor);
        m_vis.putAction("fullPaint", fullPaint);

        // animate paint change
        ActionList animatePaint = new ActionList(400);
        animatePaint.add(new ColorAnimator());
        animatePaint.add(new RepaintAction());
        m_vis.putAction("animatePaint", animatePaint);

        // create the single filtering and layout action list
        ActionList layout = new ActionList();
        TopMarginSquarifiedTreeMapLayout layoutTreeMap = new TopMarginSquarifiedTreeMapLayout(tree, RECT_BUFFER);
//        SquarifiedTreeMapLayout layoutTreeMap = new SquarifiedTreeMapLayout(tree, RECT_BUFFER);
        layout.add(layoutTreeMap);
//        layout.add(new BufferedSquarifiedTreeMapLayout(tree));
//        layout.add(new RadialTreeLayout(tree));
//        layout.add(new NodeLinkTreeLayout(tree));
//        layout.add(new BalloonTreeLayout(tree));
        layout.add(fillColor);
        layout.add(borderColor);
        layout.add(new RepaintAction());
        m_vis.putAction("layout", layout);

        // initialize our display
        setSize(700, 600);
//        setSize(1100, 900);
//        setItemSorter(new TreeDepthItemSorter());   // draws parents on top; appearance is correct, but hovering doesn't work!
        setItemSorter(new TreeDepthItemSorter(true));  // draws children on top; hovering works correctly, but higher-level groupings not visible!
        addControlListener(new ControlAdapter() {

            public void itemEntered(VisualItem item, MouseEvent e) {
//                if (((NodeItem) item).getChildCount() == 0) {
                // only draw border if a leaf
                item.setStrokeColor(borderColor.getColor(item));
                item.getVisualization().repaint();
//                }
            }

            public void itemExited(VisualItem item, MouseEvent e) {
//                if (((NodeItem) item).getChildCount() == 0) {
                // only draw border if a leaf
                item.setStrokeColor(item.getEndStrokeColor());
                item.getVisualization().repaint();
//                }
            }
        });

        searchQ = new SearchQueryBinding(vt.getNodeTable(), nodeName);
        m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, searchQ.getSearchSet());
        searchQ.getPredicate().addExpressionListener(new UpdateListener() {

            public void update(Object src) {
                m_vis.cancel("animatePaint");
                m_vis.run("fullPaint");
                m_vis.run("animatePaint");
            }
        });
        
        // add controls
        addControlListener(new PanControl());  // pan with background left-drag
        addControlListener(new ZoomControl()); // zoom with vertical right-drag

        // perform layout
        m_vis.run("layout");
    }

    public SearchQueryBinding getSearchQuery() {
        return searchQ;
    }

    public void resetSize(int width, int height) {
        setSize(width, height);

        // redo layout ?
        m_vis.run("layout");

    }

    public static DocumentTreeMapView buildNewTreeMapOnly(final MainController controller, java.util.List<Document> allDocs, java.util.List<Boolean> allDocsEnabled, java.util.List<String> orderedAttributes) {

        // create a new treemap
        DocumentTree t = DocumentTree.buildDocumentTree(allDocs, allDocsEnabled, orderedAttributes);
        final DocumentTreeMapView treemap = new DocumentTreeMapView(t);
        treemap.addControlListener(new ControlAdapter() {
            
            @Override
            public void itemClicked(VisualItem item, MouseEvent e) {
                // if a leaf, tell controller to load this document's popup
                
                // get doc id
                int docGlobalID = -1;
                if (item.canGetInt("global_doc_index")) {
                    docGlobalID = item.getInt("global_doc_index");
                }
                
                // only load popup for document nodes
                if (docGlobalID != -1) {
                    controller.displayDocDetailsWindow(docGlobalID);
                }
                
            }
            
        });
        
        return treemap;

    }

    public static JComponent buildNewTreeMapComponent(MainController controller, java.util.List<Document> allDocs, java.util.List<Boolean> allDocsEnabled, java.util.List<String> orderedAttributes) {

        JPanel panel = new JPanel(new BorderLayout());
        updatePanelWithNewTreemap(controller, panel, allDocs, allDocsEnabled, orderedAttributes);
        return panel;

    }

    public static void updatePanelWithNewTreemap(MainController controller, JComponent component, java.util.List<Document> allDocs, java.util.List<Boolean> allDocsEnabled, java.util.List<String> orderedAttributes) {

        // create a new treemap
        final DocumentTreeMapView treemap = buildNewTreeMapOnly(controller, allDocs, allDocsEnabled, orderedAttributes);
        
//        DocumentTree t = DocumentTree.buildDocumentTree(allDocs, allDocsEnabled, orderedAttributes);
//        final DocumentTreeMapView treemap = new DocumentTreeMapView(t);
//
////        // create a search panel for the tree map
////        JSearchPanel search = treemap.getSearchQuery().createSearchPanel();
////        search.setShowResultCount(true);
////        search.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 0));
////        search.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));
////
////        final JFastLabel title = new JFastLabel("                 ");
////        title.setPreferredSize(new Dimension(350, 20));
////        title.setVerticalAlignment(SwingConstants.BOTTOM);
////        title.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
////        title.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));
//
//        treemap.addControlListener(new ControlAdapter() {
//
////            @Override
////            public void itemEntered(VisualItem item, MouseEvent e) {
//////                if (((NodeItem) item).getChildCount() == 0) {
////                // only write title if a leaf?
////                title.setText(item.getString(nodeName));
//////                }
////            }
////
////            @Override
////            public void itemExited(VisualItem item, MouseEvent e) {
//////                if (((NodeItem) item).getChildCount() == 0) {
////                // only write title if a leaf?
////                title.setText(null);
//////                }
////            }
//            
//            @Override
//            public void itemClicked(VisualItem item, MouseEvent e) {
//                // if a leaf, tell controller to load this document's popup
//                
//                
//            }
//            
//        });

//        Box box = UILib.getBox(new Component[]{title, search}, true, 10, 3, 0);

        component.removeAll();
        component.setLayout(new BorderLayout());
        component.add(treemap, BorderLayout.CENTER);
        //component.add(box, BorderLayout.SOUTH);
        UILib.setColor(component, Color.BLACK, Color.GRAY);

    }

    // ------------------------------------------------------------------------
    /**
     * Set the stroke color for drawing treemap node outlines. A graded
     * grayscale ramp is used, with higer nodes in the tree drawn in lighter
     * shades of gray.
     */
    public static class BorderColorAction extends ColorAction {

        public BorderColorAction(String group) {
            super(group, VisualItem.STROKECOLOR);
        }

        public int getColor(VisualItem item) {
            NodeItem nitem = (NodeItem) item;
            if (nitem.isHover()) {
                return ColorLib.rgb(99, 130, 191);
            }

            int depth = nitem.getDepth();
            if (depth < 1) {
                return ColorLib.gray(100);
            } else if (depth < 2) {
                return ColorLib.gray(90);
            } else if (depth < 3) {
                return ColorLib.gray(80);
            } else if (depth < 4) {
                return ColorLib.gray(70);
            } else {
                return ColorLib.gray(50);
            }
        }
    }

    /**
     * Set fill colors for treemap nodes. Search items are colored in pink,
     * while normal nodes are shaded according to their depth in the tree.
     */
    public static class FillColorAction extends ColorAction {

        private ColorMap cmap = new ColorMap(
                ColorLib.getInterpolatedPalette(10,
                ColorLib.rgb(85, 85, 85), ColorLib.rgb(0, 0, 0)), 0, 9);

        public FillColorAction(String group) {
            super(group, VisualItem.FILLCOLOR);
        }

        @Override
        public int getColor(VisualItem item) {

            int attrValIndex = 0;
            if (item.canGetInt("attr_val_index")) {
                attrValIndex = item.getInt("attr_val_index");
            }
            if (attrValIndex == -1) {
                // just return lightgray? for docs
                Color lightgray = Color.LIGHT_GRAY;
                return lightgray.getRGB();
            } else {

                // get color based on depth, mod of category id of item
                int depth = (item instanceof NodeItem ? ((NodeItem) item).getDepth() : 0);
                int colorPaletteIndex = (depth + 1) % DOCTREEMAP_GROUP_PALETTE_RGB_2.length;
//            System.out.println("debug: depth="+depth);
                // depth = simple int giving depth in tree (start at 0 for root)
//                String[] colorPaletteNames = DOCTREEMAP_GROUP_PALETTE[colorPaletteIndex];
                Color[] colorPalette = DOCTREEMAP_GROUP_PALETTE_RGB_2[colorPaletteIndex];
//                String colorName = colorPaletteNames[attrValIndex % colorPaletteNames.length];
                Color color = colorPalette[attrValIndex % colorPalette.length];
                int colorInt = color.getRGB();
                return colorInt;

            }
//            if (m_vis.isInGroup(item, Visualization.SEARCH_ITEMS)) {
//                return ColorLib.rgb(191, 99, 130);
//            }
//
//            double v = (item instanceof NodeItem ? ((NodeItem) item).getDepth() : 0);
//            return cmap.getColor(v);
        }
    } // end of inner class TreeMapColorAction

    /**
     * A renderer for treemap nodes. Draws simple rectangles, but defers the
     * bounds management to the layout. Leaf nodes are drawn fully, higher level
     * nodes only have their outlines drawn. Labels are rendered for top-level
     * (i.e., depth 1) subtrees.
     */
    public static class TreeMapRenderer extends ShapeRenderer {

        private Rectangle2D m_bounds = new Rectangle2D.Double();
        private String m_label;

        public TreeMapRenderer(String label) {
            m_manageBounds = false;
            m_label = label;
        }

        public int getRenderType(VisualItem item) {
            if (((NodeItem) item).getChildCount() == 0) {
                // if a leaf node, both draw and fill the node
                return RENDER_TYPE_DRAW_AND_FILL;
//                return RENDER_TYPE_DRAW;
            } else {
                // if not a leaf, only draw the node outline
                return RENDER_TYPE_DRAW_AND_FILL;
//                return RENDER_TYPE_DRAW;
//                return RENDER_TYPE_NONE;
            }
        }

        protected Shape getRawShape(VisualItem item) {
            // TODO shrink rectangle; leave a border, plus space at top for title
            Rectangle2D rect = item.getBounds();
//            double x = rect.getX() + RECT_BUFFER;
//            double y = rect.getY() + RECT_BUFFER;
//            double w = rect.getWidth() - RECT_BUFFER * 2;
//            double h = rect.getHeight() - RECT_BUFFER * 2;
//            item.setBounds(x, y, w, h);
//            m_bounds.setRect(x, y, w, h);

            m_bounds.setRect(rect);
            return m_bounds;
        }

        public void render(Graphics2D g, VisualItem item) {
            Rectangle2D b = item.getBounds();
            // adjust item bounds
//            double xBox = b.getX() + RECT_BUFFER;
//            double yBox = b.getY() + RECT_BUFFER;
//            double wBox = b.getWidth() - RECT_BUFFER * 2;
//            double hBox = b.getHeight() - RECT_BUFFER * 2;
//            item.setBounds(xBox, yBox, wBox, hBox);
//            super.render(g, item);
            renderWithBuffer(g, item);
            // if a top-level node, draw the category name
//            if ( ((NodeItem)item).getDepth() == 1 ) {
//                String s = item.getString(m_label);
            String s = item.getString(nodeName);

            // TODO set font size based on depth in tree
            //int maxFontSize = 16;
            int maxFontSize = 22;
            int minFontSize = 12;
            int numLevels = 5;  // TODO eliminate this hardcoding
            int fontStep = (maxFontSize - minFontSize) / numLevels;
            int fontSize = maxFontSize - (((NodeItem)item).getDepth() * fontStep);
            item.setFont(FontLib.getFont("Tahoma", Font.PLAIN, fontSize));
            Font f = item.getFont();
//            f.getSize();
            FontMetrics fm = g.getFontMetrics(f);
            int w = fm.stringWidth(s);
            int h = fm.getAscent();
            //g.setColor(Color.LIGHT_GRAY);
            g.setColor(Color.BLACK);
            g.setFont(f);

            // TODO move string to top (if node is non-leaf
//            if ( ((NodeItem)item).getDepth() == 1 ) {
//                g.drawString(s, (float)(b.getCenterX()-w/2.0),
//                                (float)(b.getCenterY()+h/2));
//            } else {
            g.drawString(s, (float) (b.getCenterX() - w / 2.0),
                    (float) (b.getY() + h + RECT_BUFFER));
//            }
        }

//        @Override
//        public Shape rectangle(double x, double y, double width, double height) {
//            // leave a buffer!
//            double xMod = x + RECT_BUFFER;
//            double yMod = y + RECT_BUFFER;
//            double wMod = width + RECT_BUFFER;
//            double hMod = height + RECT_BUFFER;
//            return super.rectangle(xMod, yMod, wMod, hMod);
//        }
        public void renderWithBuffer(Graphics2D g, VisualItem item) {
            Shape shape = getShape(item);
            if (shape != null) {
//                Rectangle2D b = shape.getBounds2D();
//                double xBox = b.getX() + RECT_BUFFER;
//                double yBox = b.getY() + RECT_BUFFER;
//                double wBox = b.getWidth() - RECT_BUFFER * 2;
//                double hBox = b.getHeight() - RECT_BUFFER * 2;
//                Shape bufferShape = new Rectangle((int) xBox, (int) yBox, (int) wBox, (int) hBox);
//                drawShapeWithBuffer(g, item, bufferShape);
//                drawShapeWithBuffer(g, item, shape);
                drawShape(g, item, shape);
            }
        }

        protected void drawShapeWithBuffer(Graphics2D g, VisualItem item, Shape shape) {
            // draw the non-drawn buffer
            GraphicsLib.paint(g, item, shape, new BasicStroke(0), RENDER_TYPE_NONE);
            // draw the actual shape
            Rectangle2D b = shape.getBounds2D();
            double xBox = b.getX() + RECT_BUFFER;
            double yBox = b.getY() + RECT_BUFFER;
            double wBox = b.getWidth() - RECT_BUFFER * 2;
            double hBox = b.getHeight() - RECT_BUFFER * 2;
            Shape nonBufferShape = new Rectangle((int) xBox, (int) yBox, (int) wBox, (int) hBox);
            GraphicsLib.paint(g, item, nonBufferShape, getStroke(item), getRenderType(item));
        }
    }

}
