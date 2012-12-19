package emr_vis_nlp.view;

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
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.SquarifiedTreeMapLayout;
import prefuse.controls.ControlAdapter;
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

    // TODO : revise treemap code in-line with previous ideas: children-on-top, buffer space, click-based interaction, appropriate color schemes, appropriate attribute selection mechanisms, animation
//    private static final String label = "documentTreeMapView";
    private static final String nodeName = "name";
    private static final String tree = "tree";
    private static final String treeNodes = "tree.nodes";
    private static final String treeEdges = "tree.edges";
    public static final double RECT_BUFFER = 6.;
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
        SquarifiedTreeMapLayout layoutTreeMap = new SquarifiedTreeMapLayout(tree);
        layoutTreeMap.setFrameWidth(RECT_BUFFER);
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

        // perform layout
        m_vis.run("layout");
    }

    public SearchQueryBinding getSearchQuery() {
        return searchQ;
    }

    public static JComponent buildNewTreeMapComponent(java.util.List<Document> allDocs, java.util.List<Boolean> allDocsEnabled, java.util.List<String> orderedAttributes) {
        DocumentTree t = DocumentTree.buildDocumentTree(allDocs, allDocsEnabled, orderedAttributes);

        // create a new treemap
        final DocumentTreeMapView treemap = new DocumentTreeMapView(t);

        // create a search panel for the tree map
        JSearchPanel search = treemap.getSearchQuery().createSearchPanel();
        search.setShowResultCount(true);
        search.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 0));
        search.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));

        final JFastLabel title = new JFastLabel("                 ");
        title.setPreferredSize(new Dimension(350, 20));
        title.setVerticalAlignment(SwingConstants.BOTTOM);
        title.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        title.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));

        treemap.addControlListener(new ControlAdapter() {

            @Override
            public void itemEntered(VisualItem item, MouseEvent e) {
//                if (((NodeItem) item).getChildCount() == 0) {
                // only write title if a leaf
                title.setText(item.getString(nodeName));
//                }
            }

            @Override
            public void itemExited(VisualItem item, MouseEvent e) {
//                if (((NodeItem) item).getChildCount() == 0) {
                // only write title if a leaf
                title.setText(null);
//                }
            }
        });

        Box box = UILib.getBox(new Component[]{title, search}, true, 10, 3, 0);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(treemap, BorderLayout.CENTER);
        panel.add(box, BorderLayout.SOUTH);
        UILib.setColor(panel, Color.BLACK, Color.GRAY);
        return panel;
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

        public int getColor(VisualItem item) {
            if (m_vis.isInGroup(item, Visualization.SEARCH_ITEMS)) {
                return ColorLib.rgb(191, 99, 130);
            }

            double v = (item instanceof NodeItem ? ((NodeItem) item).getDepth() : 0);
            return cmap.getColor(v);
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
            int maxFontSize = 16;
            int fontSize = maxFontSize - ((NodeItem) item).getDepth();
            item.setFont(FontLib.getFont("Tahoma", Font.PLAIN, fontSize));
            Font f = item.getFont();
            f.getSize();
            FontMetrics fm = g.getFontMetrics(f);
            int w = fm.stringWidth(s);
            int h = fm.getAscent();
            g.setColor(Color.LIGHT_GRAY);

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
