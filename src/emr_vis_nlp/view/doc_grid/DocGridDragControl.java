
package emr_vis_nlp.view.doc_grid;

import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import prefuse.Display;
import prefuse.controls.DragControl;
import prefuse.visual.VisualItem;

/**
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocGridDragControl extends DragControl {

    private String m_group;
    
    public DocGridDragControl(String group) {
        super();
        m_group = group;
    }

    @Override
    public void itemDragged(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }
        if (item.getGroup().equals(m_group)) {
            dragged = true;
            Display d = (Display) e.getComponent();
            d.getAbsoluteCoordinate(e.getPoint(), temp);
            double dx = temp.getX() - down.getX();
            double dy = temp.getY() - down.getY();
            double x = item.getX();
            double y = item.getY();
            // also need to handle X2, Y2, since we're passing size info through the VisualItem!
            double x2 = (double)item.get(VisualItem.X2);
            double y2 = (double)item.get(VisualItem.Y2);

            item.setStartX(x);
            item.setStartY(y);
            item.setX(x + dx);
            item.setY(y + dy);
            item.setEndX(x + dx);
            item.setEndY(y + dy);
            
            item.set(VisualItem.X2, x2 + dx);
            item.set(VisualItem.Y2, y2 + dy);

            if (repaint) {
                item.getVisualization().repaint();
            }

            down.setLocation(temp);
            if (action != null) {
                d.getVisualization().run(action);
            }
        }
    }
}
