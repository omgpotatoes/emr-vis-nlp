package emr_vis_nlp.view.doc_grid;

import emr_vis_nlp.controller.MainController;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.SwingUtilities;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.controls.DragControl;
import prefuse.visual.VisualItem;

/**
 * Provides mouse interaction controls for doc grid view. Partially adopted from
 * DataMountain$DataMountainControl example in Prefuse gallery.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocGridDragControl extends DragControl {

    // temporary faux-Singleton pattern (probably just for testing)
    public static DocGridDragControl control;
    
    /**
     * data group to which this control should apply
     */
    private String m_group;
    private VisualItem activeItem;
    /**
     * pointer to DocumentGridLayout for which this instance is controlling
     * dragging of items
     */
    private DocumentGridLayout docGridLayout;
    private MainController controller;

    public DocGridDragControl(String group, DocumentGridLayout docGridLayout, MainController controller) {
        super();
        m_group = group;
        this.docGridLayout = docGridLayout;
        this.controller = controller;
        control = this;
    }
    
    @Override
    public void itemClicked(VisualItem item, MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
            if (item.getGroup().equals(m_group) && item.canGetInt(DocumentGridTable.NODE_ID)) {
                int docID = item.getInt(DocumentGridTable.NODE_ID);
                controller.displayDocDetailsWindow(docID);
            }
        }
    }
    
    @Override
    public void itemPressed(VisualItem item, MouseEvent e) {
//        if (SwingUtilities.isLeftMouseButton(e)) {
        if (SwingUtilities.isRightMouseButton(e)) {
            // debug
            System.out.println("debug: "+this.getClass().getName()+": item pressed w/ right mouse");
            // drag
            // set the focus to the current node
            Visualization vis = item.getVisualization();
            vis.getFocusGroup(Visualization.FOCUS_ITEMS).setTuple(item);
            item.setFixed(true);
            dragged = false;
//            Display d = (Display) e.getComponent();
            Display d = controller.getDocumentGrid();
            down = d.getAbsoluteCoordinate(e.getPoint(), down);
//            vis.run("forces");
        }
    }

    @Override
    public void itemReleased(VisualItem item, MouseEvent e) {
//        if (!SwingUtilities.isLeftMouseButton(e)) {
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

        vis.cancel("forces");
    }

    @Override
    public void itemDragged(VisualItem item, MouseEvent e) {
//    @Override
//    public void itemReleased(VisualItem item, MouseEvent e) {
//        if (!SwingUtilities.isLeftMouseButton(e)) {
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
            // also need to handle X2, Y2, since we're passing size info through the VisualItem!
            // TODO handle shapes in a more standard way? rather than relying on x2/y2?
            double x2 = (double) item.get(VisualItem.X2);
            double y2 = (double) item.get(VisualItem.Y2);

            item.setStartX(x);
            item.setStartY(y);
            item.setX(x + dx);
            item.setY(y + dy);
            item.setEndX(x + dx);
            item.setEndY(y + dy);

            item.set(VisualItem.X2, x2 + dx);
            item.set(VisualItem.Y2, y2 + dy);

//            item.setBounds(x + dx, y + dy, x2 + dx, y2 + dy);

            if (repaint) {
                item.getVisualization().repaint();
            }

            down.setLocation(temp);
            if (action != null) {
                d.getVisualization().run(action);
            }

            // determine whether item is in same region or new region;
            //  if new region, call controller to update attr vals
            int origRegionX = -1;
            int origRegionY = -1;
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
                if (xRegionStart < x && x < xRegionEnd) {
                    origRegionX = i;
                }
                if (xRegionStart < x + dx && x + dx < xRegionEnd) {
                    newRegionX = i;
                }
            }
            for (int i = 0; i < yCats.size(); i++) {
                int yRegionStart = yCatPositions.get(i);
                int yRegionEnd = yRegionStart + yCatRegionSizes.get(i);
                if (yRegionStart < y && y < yRegionEnd) {
                    origRegionY = i;
                }
                if (yRegionStart < y + dy && y + dy < yRegionEnd) {
                    newRegionY = i;
                }
            }

            // if both regions are same, do nothing
            int docID = item.getInt(DocumentGridTable.NODE_ID);

            // debug
//            System.out.println("debug: item moved: docID="+docID+"xOrig="+xCats.get(origRegionX)+", xNew="+xCats.get(newRegionX)+", yOrig="+yCats.get(origRegionY)+", yNew="+yCats.get(newRegionY));

            // else, invoke controller to adjust document attributes
            // update for x and y separately
            if (origRegionX != newRegionX && newRegionX != -1) {
                String newCat = xCats.get(newRegionX);
                controller.updateDocumentAttr(docID, xAttrName, newCat);
                controller.documentAttributesUpdated(docID);
            }
            if (origRegionY != newRegionY && newRegionY != -1) {
                String newCat = yCats.get(newRegionY);
                controller.updateDocumentAttr(docID, yAttrName, newCat);
                controller.documentAttributesUpdated(docID);
            }


        }
    }
    
    
    @Override
    public void itemEntered(VisualItem item, MouseEvent e) {
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
        
    }
    
    @Override
    public void itemExited(VisualItem item, MouseEvent e) {
        // suspend FDL, run fisheye
//            Visualization vis = item.getVisualization();
//            vis.cancel("forces");
//            vis.run("distort");
    }
    
    
    
}
