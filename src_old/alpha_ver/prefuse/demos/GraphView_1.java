package prefuse.demos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.expression.AbstractPredicate;
import prefuse.data.io.GraphMLReader;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphLib;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.ItemBoundsListener;
import prefuse.util.display.PaintListener;
import prefuse.util.force.ForceSimulator;
import prefuse.util.io.IOLib;
import prefuse.util.ui.JForcePanel;
import prefuse.util.ui.JValueSlider;
import prefuse.util.ui.UILib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

/**
 * 
 * adopted from code presented in http://sourceforge.net/projects/prefuse/forums/forum/343013/topic/1739271
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class GraphView_1 extends JPanel {

    private static final String graph = "graph";
    private static final String nodes = "graph.nodes";
    private static final String edges = "graph.edges";
    private Visualization m_vis;

    public GraphView_1(Graph g, String label) {

// create a new, empty visualization for our data
        m_vis = new Visualization();

// --------------------------------------------------------------------
// set up the renderers

        LabelRenderer tr = new LabelRenderer();
        tr.setRoundedCorner(8, 8);
        m_vis.setRendererFactory(new DefaultRendererFactory(tr));

// --------------------------------------------------------------------
// register the data with a visualization

// adds graph to visualization and sets renderer label field
        setGraph(g, label);

// fix selected focus nodes
        TupleSet focusGroup = m_vis.getGroup(Visualization.FOCUS_ITEMS);
        focusGroup.addTupleSetListener(new TupleSetListener() {

            public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem) {
                for (int i = 0; i < rem.length; ++i) {
                    ((VisualItem) rem[i]).setFixed(false);
                }
                for (int i = 0; i < add.length; ++i) {
                    ((VisualItem) add[i]).setFixed(false);
                    ((VisualItem) add[i]).setFixed(true);
                }
                if (ts.getTupleCount() == 0) {
                    ts.addTuple(rem[0]);
                    ((VisualItem) rem[0]).setFixed(false);
                }
                m_vis.run("draw");
            }
        });



// --------------------------------------------------------------------
// create actions to process the visual data

        int hops = 30;
        final GraphDistanceFilter filter = new GraphDistanceFilter(graph, hops);

        ColorAction fill = new ColorAction(nodes,
                VisualItem.FILLCOLOR, ColorLib.rgb(200, 200, 255));
        fill.add(VisualItem.FIXED, ColorLib.rgb(255, 100, 100));
        fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 200, 125));

        ActionList draw = new ActionList();
        draw.add(filter);
        draw.add(fill);
        draw.add(new ColorAction(nodes, VisualItem.STROKECOLOR, 0));
        draw.add(new ColorAction(nodes, VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0)));
        draw.add(new ColorAction(edges, VisualItem.FILLCOLOR, ColorLib.gray(200)));
        draw.add(new ColorAction(edges, VisualItem.STROKECOLOR, ColorLib.gray(200)));

        ActionList animate = new ActionList(Activity.INFINITY);
        animate.add(new ForceDirectedLayout(graph));
        animate.add(fill);
        animate.add(new RepaintAction());

// finally, we register our ActionList with the Visualization.
// we can later execute our Actions by invoking a method on our
// Visualization, using the name we've chosen below.
        m_vis.putAction("draw", draw);
        m_vis.putAction("layout", animate);

        m_vis.runAfter("draw", "layout");


//Create an instance of SelectionManager
        SelectionManager selectionManager = new SelectionManager();

// --------------------------------------------------------------------
// set up a display to show the visualization

        Display display = new Display(m_vis);
        display.setSize(700, 700);
        display.pan(350, 350);
        display.setForeground(Color.GRAY);
        display.setBackground(Color.WHITE);

// main display controls
        display.addControlListener(new FocusControl(1, "draw"));
        display.addControlListener(new DragControl());
        display.addControlListener(new PanControl(Control.RIGHT_MOUSE_BUTTON));
        display.addControlListener(new ZoomControl(Control.MIDDLE_MOUSE_BUTTON));
        display.addControlListener(new WheelZoomControl());
        display.addControlListener(new ZoomToFitControl());
        display.addControlListener(new NeighborHighlightControl());

// overview display
        Display overview = new Display(m_vis);
        overview.setSize(290, 290);
        overview.addItemBoundsListener(new FitOverviewListener());

        display.setForeground(Color.GRAY);
        display.setBackground(Color.WHITE);

        display.addControlListener(selectionManager.getSelectionListener());
        display.addPaintListener(selectionManager.getSelectionListener());

//Add selectionManager to fill ColorAction
        fill.add(selectionManager.getSelectionPredictate(), ColorLib.color(Color.CYAN));

//Form a ColorAction
        ActionList color = new ActionList();
        color.add(fill);
        color.add(new RepaintAction());
        m_vis.putAction("color", color);

// --------------------------------------------------------------------
// launch the visualization

// create a panel for editing force values
        ForceSimulator fsim = ((ForceDirectedLayout) animate.get(0)).getForceSimulator();
        JForcePanel fpanel = new JForcePanel(fsim);

        JPanel opanel = new JPanel();
        opanel.setBorder(BorderFactory.createTitledBorder("Overview"));
        opanel.setBackground(Color.WHITE);
        opanel.add(overview);

        final JValueSlider slider = new JValueSlider("Distance", 0, hops, hops);
        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                filter.setDistance(slider.getValue().intValue());
                m_vis.run("draw");
            }
        });
        slider.setBackground(Color.WHITE);
        slider.setPreferredSize(new Dimension(300, 30));
        slider.setMaximumSize(new Dimension(300, 30));

        Box cf = new Box(BoxLayout.Y_AXIS);
        cf.add(slider);
        cf.setBorder(BorderFactory.createTitledBorder("Connectivity Filter"));
        fpanel.add(cf);

        fpanel.add(opanel);

        fpanel.add(Box.createVerticalGlue());

// create a new JSplitPane to present the interface
        JSplitPane split = new JSplitPane();
        split.setLeftComponent(display);
        split.setRightComponent(fpanel);
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(false);
        split.setDividerLocation(700);

// now we run our action list
        m_vis.run("draw");

        add(split);
    }

    public void setGraph(Graph g, String label) {
// update labeling
        DefaultRendererFactory drf = (DefaultRendererFactory) m_vis.getRendererFactory();
        ((LabelRenderer) drf.getDefaultRenderer()).setTextField(label);

// update graph
        m_vis.removeGroup(graph);
        VisualGraph vg = m_vis.addGraph(graph, g);
        m_vis.setValue(edges, null, VisualItem.INTERACTIVE, Boolean.FALSE);
        VisualItem f = (VisualItem) vg.getNode(0);
        m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);
        f.setFixed(false);
    }

// ------------------------------------------------------------------------
// Main and demo methods
    public static void main(String[] args) {
        UILib.setPlatformLookAndFeel();

// create graphview
        String datafile = null;
        String label = "label";

        if (args.length > 1) {
            datafile = args[0];
            label = args[1];
        }

        JFrame frame = demo(datafile, label);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static JFrame demo() {
        return demo((String) null, "label");
    }

    public static JFrame demo(String datafile, String label) {
        Graph g = null;
        if (datafile == null) {
            g = GraphLib.getGrid(10, 10);
            label = "label";
        } else {
            try {
                g = new GraphMLReader().readGraph(datafile);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        return demo(g, label);
    }

    public static JFrame demo(Graph g, String label) {
        final GraphView_1 view = new GraphView_1(g, label);

// set up menu
        JMenu dataMenu = new JMenu("Data");
        dataMenu.add(new OpenGraphAction(view));
        dataMenu.add(new GraphMenuAction("Grid", "ctrl 1", view) {

            protected Graph getGraph() {
                return GraphLib.getGrid(15, 15);
            }
        });
        dataMenu.add(new GraphMenuAction("Clique", "ctrl 2", view) {

            protected Graph getGraph() {
                return GraphLib.getClique(10);
            }
        });
        dataMenu.add(new GraphMenuAction("Honeycomb", "ctrl 3", view) {

            protected Graph getGraph() {
                return GraphLib.getHoneycomb(5);
            }
        });
        dataMenu.add(new GraphMenuAction("Balanced Tree", "ctrl 4", view) {

            protected Graph getGraph() {
                return GraphLib.getBalancedTree(3, 5);
            }
        });
        dataMenu.add(new GraphMenuAction("Diamond Tree", "ctrl 5", view) {

            protected Graph getGraph() {
                return GraphLib.getDiamondTree(3, 3, 3);
            }
        });
        JMenuBar menubar = new JMenuBar();
        menubar.add(dataMenu);

// launch window
        JFrame frame = new JFrame("p r e f u s e | g r a p h v i e w");
        frame.setJMenuBar(menubar);
        frame.setContentPane(view);
        frame.pack();
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {

            public void windowActivated(WindowEvent e) {
                view.m_vis.run("layout");
            }

            public void windowDeactivated(WindowEvent e) {
                view.m_vis.cancel("layout");
            }
        });

        return frame;
    }

// ------------------------------------------------------------------------
    /**
     * Swing menu action that loads a graph into the graph viewer.
     */
    public abstract static class GraphMenuAction extends AbstractAction {

        private GraphView_1 m_view;

        public GraphMenuAction(String name, String accel, GraphView_1 view) {
            m_view = view;
            this.putValue(AbstractAction.NAME, name);
            this.putValue(AbstractAction.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(accel));
        }

        public void actionPerformed(ActionEvent e) {
            m_view.setGraph(getGraph(), "label");
        }

        protected abstract Graph getGraph();
    }

    public static class OpenGraphAction extends AbstractAction {

        private GraphView_1 m_view;

        public OpenGraphAction(GraphView_1 view) {
            m_view = view;
            this.putValue(AbstractAction.NAME, "Open File...");
            this.putValue(AbstractAction.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("ctrl O"));
        }

        public void actionPerformed(ActionEvent e) {
            Graph g = IOLib.getGraphFile(m_view);
            if (g == null) {
                return;
            }
            String label = getLabel(m_view, g);
            if (label != null) {
                m_view.setGraph(g, label);
            }
        }

        public static String getLabel(Component c, Graph g) {
// get the column names
            Table t = g.getNodeTable();
            int cc = t.getColumnCount();
            String[] names = new String[cc];
            for (int i = 0; i < cc; ++i) {
                names[i] = t.getColumnName(i);
            }

// where to store the result
            final String[] label = new String[1];

// -- build the dialog -----
// we need to get the enclosing frame first
            while (c != null && !(c instanceof JFrame)) {
                c = c.getParent();
            }
            final JDialog dialog = new JDialog(
                    (JFrame) c, "Choose Label Field", true);

// create the ok/cancel buttons
            final JButton ok = new JButton("OK");
            ok.setEnabled(false);
            ok.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    dialog.setVisible(false);
                }
            });
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    label[0] = null;
                    dialog.setVisible(false);
                }
            });

// build the selection list
            final JList list = new JList(names);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.getSelectionModel().addListSelectionListener(
                    new ListSelectionListener() {

                        public void valueChanged(ListSelectionEvent e) {
                            int sel = list.getSelectedIndex();
                            if (sel >= 0) {
                                ok.setEnabled(true);
                                label[0] = (String) list.getModel().getElementAt(sel);
                            } else {
                                ok.setEnabled(false);
                                label[0] = null;
                            }
                        }
                    });
            JScrollPane scrollList = new JScrollPane(list);

            JLabel title = new JLabel("Choose a field to use for node labels:");

// layout the buttons
            Box bbox = new Box(BoxLayout.X_AXIS);
            bbox.add(Box.createHorizontalStrut(5));
            bbox.add(Box.createHorizontalGlue());
            bbox.add(ok);
            bbox.add(Box.createHorizontalStrut(5));
            bbox.add(cancel);
            bbox.add(Box.createHorizontalStrut(5));

// put everything into a panel
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(title, BorderLayout.NORTH);
            panel.add(scrollList, BorderLayout.CENTER);
            panel.add(bbox, BorderLayout.SOUTH);
            panel.setBorder(BorderFactory.createEmptyBorder(5, 2, 2, 2));

// show the dialog
            dialog.setContentPane(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(c);
            dialog.setVisible(true);
            dialog.dispose();

// return the label field selection
            return label[0];
        }
    }

    public static class FitOverviewListener implements ItemBoundsListener {

        private Rectangle2D m_bounds = new Rectangle2D.Double();
        private Rectangle2D m_temp = new Rectangle2D.Double();
        private double m_d = 15;

        public void itemBoundsChanged(Display d) {
            d.getItemBounds(m_temp);
            GraphicsLib.expand(m_temp, 25 / d.getScale());

            double dd = m_d / d.getScale();
            double xd = Math.abs(m_temp.getMinX() - m_bounds.getMinX());
            double yd = Math.abs(m_temp.getMinY() - m_bounds.getMinY());
            double wd = Math.abs(m_temp.getWidth() - m_bounds.getWidth());
            double hd = Math.abs(m_temp.getHeight() - m_bounds.getHeight());
            if (xd > dd || yd > dd || wd > dd || hd > dd) {
                m_bounds.setFrame(m_temp);
                DisplayLib.fitViewToBounds(d, m_bounds, 0);
            }
        }
    }

    public class SelectionManager {

        private HashSet<NodeItem> selectedNodes;
        private HashSet<EdgeItem> selectedEdges;
        private SelectionListener sl;
        private SelectionPredicate sp;

        public SelectionManager() {
            this.selectedEdges = new HashSet<EdgeItem>();
            this.selectedNodes = new HashSet<NodeItem>();
        }

//methods to add an item
        public void addSelectedItem(VisualItem item) {
            if (item instanceof EdgeItem) {
                this.addSelectedEdge((EdgeItem) item);
            } else if (item instanceof NodeItem) {
                this.addSelectedNode((NodeItem) item);
            }
        }

        public void addSelectedItem(VisualItem item, boolean removeIfInSelection) {
            if (removeIfInSelection) {
                if (this.isSelected(item)) {
                    this.removeSelectedItem(item);
                } else {
                    this.addSelectedItem(item);
                }
            } else {
                this.addSelectedItem(item);
            }
        }

        public void addSelectedNode(NodeItem item) {
            this.selectedNodes.add(item);
        }

        public void addSelectedEdge(EdgeItem item) {
            this.selectedEdges.add(item);
        }

//methods to remove an item
        public void removeSelectedItem(VisualItem item) {
            if (item instanceof EdgeItem) {
                this.removeSelectedEdge((EdgeItem) item);
            } else if (item instanceof NodeItem) {
                this.removeSelectedNode((NodeItem) item);
            }
        }

        public void removeSelectedNode(NodeItem node) {
            this.selectedNodes.remove(node);
        }

        public void removeSelectedEdge(EdgeItem edge) {
            this.selectedEdges.remove(edge);
        }

        public void clearAll() {
            this.selectedEdges.clear();
            this.selectedNodes.clear();
        }

//methods to ask if an item is selected
        public boolean isSelected(VisualItem item) {
            if (item instanceof NodeItem) {
                return isSelected((NodeItem) item);
            } else if (item instanceof EdgeItem) {
                return isSelected((EdgeItem) item);
            } else {
                return false;
            }
        }

        public boolean isSelected(NodeItem node) {
            return this.selectedNodes.contains(node);
        }

        public boolean isSelected(EdgeItem edge) {
            return this.selectedEdges.contains(edge);
        }

        public Iterator<EdgeItem> edges() {
            Iterator<EdgeItem> iter = this.selectedEdges.iterator();
            return iter;
        }

        public Iterator<NodeItem> nodes() {
            Iterator<NodeItem> iter = this.selectedNodes.iterator();
            return iter;
        }

        public SelectionPredicate getSelectionPredictate() {
            if (sp == null) {
                sp = new SelectionPredicate();
            }
            return sp;
        }

        private class SelectionPredicate extends AbstractPredicate {

            public boolean getBoolean(Tuple obj) {
                if (obj instanceof NodeItem) {
                    return selectedNodes.contains(obj);
                } else if (obj instanceof EdgeItem) {
                    return selectedEdges.contains(obj);
                }
                return false;
            }
        }

        public SelectionListener getSelectionListener() {
            if (sl == null) {
                sl = new SelectionListener();
            }
            return sl;
        }

        private class SelectionListener extends ControlAdapter implements PaintListener {

            private Point start = null;
            private Point end = null;
            private boolean buttonPressed = false;
            private int m_button = LEFT_MOUSE_BUTTON;

            public SelectionListener() {
            }

            public SelectionListener(int button) {
                m_button = button;
            }

            private Rectangle createRect(Point2D start, Point2D end) {
                int x = (int) Math.min(start.getX(), end.getX());
                int y = (int) Math.min(start.getY(), end.getY());
                int x2 = (int) Math.max(start.getX(), end.getX());
                int y2 = (int) Math.max(start.getY(), end.getY());
                int width = (int) Math.abs(x2 - x);
                int height = (int) Math.abs(y2 - y);
                return new Rectangle(x, y, width, height);
            }

            public void mousePressed(MouseEvent e) {

                if (UILib.isButtonPressed(e, m_button)) {
                    this.start = e.getPoint();
                    this.buttonPressed = true;
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (UILib.isButtonPressed(e, m_button)) {
                    this.end = e.getPoint();
                    Display display = (Display) e.getComponent();

// when releasing left mouse button mark all nodes and edges in rectangle as selected
                    Iterator it = display.getVisualization().visibleItems();
                    VisualItem item;

                    Point2D absStart = display.getAbsoluteCoordinate(this.start, null);
                    Point2D absEnd = display.getAbsoluteCoordinate(this.end, null);
                    Rectangle rect = createRect(absStart, absEnd);

                    while (it.hasNext()) {
                        item = (VisualItem) it.next();
                        if (rect.contains(item.getBounds())) {
                            if (e.isControlDown()) {
                                removeSelectedItem(item);
                            } else {
                                addSelectedItem(item);
                            }
                        }
                    }

// reset selection rectangle
                    display.getVisualization().run("color");
                    display.repaint();
                    this.end = null;
                    this.buttonPressed = false;
                }
            }

            public void mouseDragged(MouseEvent e) {
//paintRectangle
                if (this.buttonPressed) {
                    this.end = e.getPoint();
                    Display display = (Display) e.getComponent();
                    display.repaint();
                }
            }

            public void itemPressed(VisualItem item, MouseEvent e) {
                if (e.getSource().getClass() == Display.class) {
// add or remove item to/from selection
                    addSelectedItem(item, true);
                    Display display = (Display) e.getComponent();
                    display.repaint();
                }
            }

            public void postPaint(Display d, Graphics2D g) {
                if (this.end != null) {
                    Rectangle rect = createRect(this.start, this.end);
                    g.drawRect(rect.x, rect.y, rect.width, rect.height);
                }
            }

            public void prePaint(Display d, Graphics2D g) {
            }
        }
    }
} // end of class GraphView