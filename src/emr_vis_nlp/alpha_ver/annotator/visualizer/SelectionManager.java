
package annotator.visualizer;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import annotator.MainWindow;

import prefuse.Display;
import prefuse.controls.ControlAdapter;
import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;
import prefuse.demos.GraphView_1;
import prefuse.util.display.PaintListener;
import prefuse.util.ui.UILib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * adopted from code presented in http://sourceforge.net/projects/prefuse/forums/forum/343013/topic/1739271
 * @author <a href="http://jheer.org">jeffrey heer</a>, 
 */
public class SelectionManager {

    //private HashSet<NodeItem> selectedNodes;
    private HashSet<VisualItem> selectedNodes;
    private HashSet<EdgeItem> selectedEdges;
    private SelectionListener sl;
    private SelectionPredicate sp;

    public SelectionManager() {
        this.selectedEdges = new HashSet<EdgeItem>();
        //this.selectedNodes = new HashSet<NodeItem>();
        this.selectedNodes = new HashSet<VisualItem>();
    }

//methods to add an item
    public void addSelectedItem(VisualItem item) {
        if (item instanceof EdgeItem) {
            this.addSelectedEdge((EdgeItem) item);
        } else if (item instanceof NodeItem) {
            this.addSelectedNode((NodeItem) item);
            // debug
            //System.out.println("debug: SelectionManager.addSelectedItem: adding NodeItem: "+item.toString());
        } else {
            // debug
            //System.out.println("debug: SelectionManager.addSelectedItem: anomalous item, not adding: "+item.toString());
            // just assume it's a node
            // debug
            //System.out.println("debug: SelectionManager.addSelectedItem: anomalous item, adding to nodes: "+item.toString());
            this.addSelectedNode(item);
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
    
    public void addSelectedNode(VisualItem item) {
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
            // debug
            //System.out.println("debug: SelectionManager.removeSelectedItem: removing NodeItem: "+item.toString());
        } else {
            // debug
            //System.out.println("debug: SelectionManager.removeSelectedItem: anomalous item, not removing: "+item.toString());
            // just assume it's a node
            // debug
            //System.out.println("debug: SelectionManager.removeSelectedItem: anomalous item, removing from nodes: "+item.toString());
            this.removeSelectedNode(item);
        }
    }

    public void removeSelectedNode(NodeItem node) {
        this.selectedNodes.remove(node);
    }
    
    public void removeSelectedNode(VisualItem node) {
        this.selectedNodes.remove(node);
    }

    public void removeSelectedEdge(EdgeItem edge) {
        this.selectedEdges.remove(edge);
    }

    public void clearAll() {
        this.selectedEdges.clear();
        this.selectedNodes.clear();
        // debug
        //System.out.println("debug: SelectionManager.clearAll: clearing all nodes and edges");
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

    public Iterator<VisualItem> nodes() {
        //Iterator<NodeItem> iter = this.selectedNodes.iterator();
        Iterator<VisualItem> iter = this.selectedNodes.iterator();
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
            } else if (obj instanceof VisualItem) {
            	// treat it as a node
            	return selectedNodes.contains(obj);
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
    
    public List<Integer> getSelectedIndices() {
    	// 1 index per doc, val == 0 if not selected, == 1 if selected
    	int numDocs = MainWindow.activeDataset.getDocuments().size();
    	List<Integer> clusterIndices = new ArrayList<Integer>(numDocs);
    	for (int i=0; i<numDocs; i++) {
    		clusterIndices.add(0);
    	}
    	Iterator<VisualItem> nodeIter = nodes();
        while (nodeIter.hasNext()) {
            VisualItem node = nodeIter.next();
            int index = (Integer)(node.get("index"));
            //System.out.println("debug:\tselectedNode: "+node.toString()+", index="+index);
            clusterIndices.remove(index);
            clusterIndices.add(index, 1);
        }
    	return clusterIndices;
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
                
                // debug
//                System.out.println("debug: SelectionListener.mouseReleased: selectedNodes:");
//                Iterator<VisualItem> nodeIter = nodes();
//                while (nodeIter.hasNext()) {
//                    VisualItem node = nodeIter.next();
//                    System.out.println("debug:\t"+node.toString());
//                }
                
                // update barcharts, term cloud, list of selected docs
                MainWindow.window.updateTermCloudAndBarCharts();
                
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

                // update barcharts, term cloud, list of selected docs
                MainWindow.window.updateTermCloudAndBarCharts();
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
